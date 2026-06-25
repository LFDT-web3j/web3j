/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Arrays;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

import static org.bouncycastle.util.BigIntegers.TWO;
import static org.web3j.utils.Assertions.verifyPrecondition;

/**
 * Transaction signing logic.
 *
 * <p>Adapted from the <a
 * href="https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/ECKey.java">
 * BitcoinJ ECKey</a> implementation.
 */
public class Sign {

    public static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    public static final int CHAIN_ID_INC = 35;
    public static final int LOWER_REAL_V = 27;
    // The v signature parameter starts at 37 because 1 is the first valid chainId so:
    // chainId >= 1 implies that 2 * chainId + CHAIN_ID_INC >= 37.
    // https://eips.ethereum.org/EIPS/eip-155
    public static final int REPLAY_PROTECTED_V_MIN = 37;
    static final ECDomainParameters CURVE =
            new ECDomainParameters(
                    CURVE_PARAMS.getCurve(),
                    CURVE_PARAMS.getG(),
                    CURVE_PARAMS.getN(),
                    CURVE_PARAMS.getH());
    static final BigInteger HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);

    static final String MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    static byte[] getEthereumMessagePrefix(int messageLength) {
        return MESSAGE_PREFIX
                .concat(String.valueOf(messageLength))
                .getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] getEthereumMessageHash(byte[] message) {
        byte[] prefix = getEthereumMessagePrefix(message.length);

        byte[] result = new byte[prefix.length + message.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(message, 0, result, prefix.length, message.length);

        return Hash.sha3(result);
    }

    public static SignatureData signPrefixedMessage(byte[] message, ECKeyPair keyPair) {
        return signMessage(getEthereumMessageHash(message), keyPair, false);
    }

    public static SignatureData signMessage(byte[] message, ECKeyPair keyPair) {
        return signMessage(message, keyPair, true);
    }

    public static SignatureData signTypedData(String jsonData, ECKeyPair keyPair)
            throws IOException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonData);
        byte[] hashStructuredData = dataEncoder.hashStructuredData();

        return signMessage(hashStructuredData, keyPair, false);
    }

    public static SignatureData signMessage(byte[] message, ECKeyPair keyPair, boolean needToHash) {
        BigInteger publicKey = keyPair.getPublicKey();
        byte[] messageHash;
        if (needToHash) {
            messageHash = Hash.sha3(message);
        } else {
            messageHash = message;
        }

        ECDSASignature sig = keyPair.sign(messageHash);

        return createSignatureData(sig, publicKey, messageHash);
    }

    /**
     * Signature without EIP-155 (Simple replay attack protection)
     * https://eips.ethereum.org/EIPS/eip-155 To add EIP-155 call
     * TransactionEncoder.createEip155SignatureData after that.
     */
    public static Sign.SignatureData createSignatureData(
            ECDSASignature sig, BigInteger publicKey, byte[] messageHash) {
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            BigInteger k = recoverFromSignature(i, sig, messageHash);
            if (k != null && k.equals(publicKey)) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException(
                    "Could not construct a recoverable key. Are your credentials valid?");
        }

        int headerByte = recId + 27;

        // 1 header + 32 bytes for R + 32 bytes for S
        byte[] v = new byte[] {(byte) headerByte};
        byte[] r = Numeric.toBytesPadded(sig.r, 32);
        byte[] s = Numeric.toBytesPadded(sig.s, 32);

        return new Sign.SignatureData(v, r, s);
    }

    /**
     * Returns SignatureData from hex signature.
     *
     * @param hexSignature hex representation of signature
     * @return SignatureData
     * @throws RuntimeException if signature has invalid format
     */
    public static SignatureData signatureDataFromHex(String hexSignature)
            throws SignatureException {
        byte[] sigBytes = Numeric.hexStringToByteArray(hexSignature);
        byte v;
        byte[] r, s;
        if (sigBytes.length == 64) {
            // EIP-2098; pull the v from the top bit of s and clear it
            v = (byte) (27 + (sigBytes[32] >> 7));
            sigBytes[32] &= 0x7f;
            r = Arrays.copyOfRange(sigBytes, 0, 32);
            s = Arrays.copyOfRange(sigBytes, 32, 64);

        } else if (sigBytes.length == 65) {
            r = Arrays.copyOfRange(sigBytes, 0, 32);
            s = Arrays.copyOfRange(sigBytes, 32, 64);
            v = sigBytes[64];
        } else {
            throw new SignatureException("invalid signature string");
        }
        // Allow a recid to be used as the v
        if (v < 27) {
            if (v == 0 || v == 1) {
                v = (byte) (v + 27);
            } else {
                throw new SignatureException("signature invalid v byte");
            }
        }
        return new Sign.SignatureData(v, r, s);
    }

    /**
     * Given the components of a signature and a selector value, recover and return the public key
     * that generated the signature according to the algorithm in SEC1v2 section 4.1.6.
     *
     * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the
     * correct one. Because the key recovery operation yields multiple potential keys, the correct
     * key must either be stored alongside the signature, or you must be willing to try each recId
     * in turn until you find one that outputs the key you are expecting.
     *
     * <p>If this method returns null it means recovery was not possible and recId should be
     * iterated.
     *
     * <p>Given the above two points, a correct usage of this method is inside a for loop from 0 to
     * 3, and if the output is null OR a key that is not the one you expect, you try again with the
     * next recId.
     *
     * @param recId Which possible key to recover.
     * @param sig the R and S components of the signature, wrapped.
     * @param message Hash of the data that was signed.
     * @return An ECKey containing only the public part, or null if recovery wasn't possible.
     */
    public static BigInteger recoverFromSignature(int recId, ECDSASignature sig, byte[] message) {
        verifyPrecondition(recId >= 0 && recId <= 3, "recId must be in the range of [0, 3]");
        verifyPrecondition(sig.r.signum() >= 0, "r must be positive");
        verifyPrecondition(sig.s.signum() >= 0, "s must be positive");
        verifyPrecondition(message != null, "message cannot be null");

        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        BigInteger n = CURVE.getN(); // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion
        //        routine specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R
        //        using the conversion routine specified in Section 2.3.4. If this conversion
        //        routine outputs "invalid", then do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are
        // two possibilities. So it's encoded in the recId.
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
        //        responsibility).
        if (!R.multiply(n).isInfinity()) {
            return null;
        }
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        BigInteger e = new BigInteger(1, message);
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via
        //        iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation ** is point multiplication and + is point addition (the EC group
        // operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For
        // example the additive inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and
        // -3 mod 11 = 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);

        byte[] qBytes = q.getEncoded(false);
        // We remove the prefix
        return new BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.length));
    }

    /** Decompress a compressed public key (x co-ord and low-bit of y-coord). */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    /**
     * Given an arbitrary piece of text and an Ethereum message signature encoded in bytes, returns
     * the public key that was used to sign it. This can then be compared to the expected public key
     * to determine if the signature was correct.
     *
     * @param message RLP encoded message.
     * @param signatureData The message signature components
     * @return the public key used to sign the message
     * @throws SignatureException If the public key could not be recovered or if there was a
     *     signature format error.
     */
    public static BigInteger signedMessageToKey(byte[] message, SignatureData signatureData)
            throws SignatureException {
        return signedMessageHashToKey(Hash.sha3(message), signatureData);
    }

    /**
     * Given an arbitrary message and an Ethereum message signature encoded in bytes, returns the
     * public key that was used to sign it. This can then be compared to the expected public key to
     * determine if the signature was correct.
     *
     * @param message The message.
     * @param signatureData The message signature components
     * @return the public key used to sign the message
     * @throws SignatureException If the public key could not be recovered or if there was a
     *     signature format error.
     */
    public static BigInteger signedPrefixedMessageToKey(byte[] message, SignatureData signatureData)
            throws SignatureException {
        return signedMessageHashToKey(getEthereumMessageHash(message), signatureData);
    }

    /**
     * Given an arbitrary message hash and an Ethereum message signature encoded in bytes, returns
     * the public key that was used to sign it. This can then be compared to the expected public key
     * to determine if the signature was correct.
     *
     * @param messageHash The message hash.
     * @param signatureData The message signature components
     * @return the public key used to sign the message
     * @throws SignatureException If the public key could not be recovered or if there was a
     *     signature format error.
     */
    public static BigInteger signedMessageHashToKey(byte[] messageHash, SignatureData signatureData)
            throws SignatureException {

        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        verifyPrecondition(r != null && r.length == 32, "r must be 32 bytes");
        verifyPrecondition(s != null && s.length == 32, "s must be 32 bytes");

        int header = signatureData.getV()[0] & 0xFF;
        // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
        //                  0x1D = second key with even y, 0x1E = second key with odd y
        if (header < 27 || header > 34) {
            throw new SignatureException("Header byte out of range: " + header);
        }

        ECDSASignature sig =
                new ECDSASignature(
                        new BigInteger(1, signatureData.getR()),
                        new BigInteger(1, signatureData.getS()));

        int recId = header - 27;
        BigInteger key = recoverFromSignature(recId, sig, messageHash);
        if (key == null) {
            throw new SignatureException("Could not recover public key from signature");
        }
        return key;
    }

    /**
     * Returns recovery ID.
     *
     * @param signatureData The message signature components
     * @param chainId of the network
     * @return int recovery ID
     */
    public static int getRecId(SignatureData signatureData, long chainId) {
        BigInteger v = Numeric.toBigInt(signatureData.getV());
        BigInteger lowerRealV = BigInteger.valueOf(LOWER_REAL_V);
        BigInteger lowerRealVPlus1 = BigInteger.valueOf(LOWER_REAL_V + 1);
        BigInteger lowerRealVReplayProtected = BigInteger.valueOf(REPLAY_PROTECTED_V_MIN);
        BigInteger chainIdInc = BigInteger.valueOf(CHAIN_ID_INC);
        if (v.equals(lowerRealV) || v.equals(lowerRealVPlus1)) {
            return v.subtract(lowerRealV).intValue();
        } else if (v.compareTo(lowerRealVReplayProtected) >= 0) {
            return v.subtract(BigInteger.valueOf(chainId).multiply(TWO))
                    .subtract(chainIdInc)
                    .intValue();
        } else {
            throw new IllegalArgumentException(String.format("Unsupported v parameter: %s", v));
        }
    }

    /**
     * Returns the header 'v'.
     *
     * @param recId The recovery id.
     * @return byte[] header 'v'.
     */
    public static byte[] getVFromRecId(int recId) {
        return new byte[] {(byte) (LOWER_REAL_V + recId)};
    }

    /**
     * Returns public key from the given private key.
     *
     * @param privKey the private key to derive the public key from
     * @return BigInteger encoded public key
     */
    public static BigInteger publicKeyFromPrivate(BigInteger privKey) {
        ECPoint point = publicPointFromPrivate(privKey);

        byte[] encoded = point.getEncoded(false);
        return new BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.length)); // remove prefix
    }

    /**
     * Returns public key point from the given private key.
     *
     * @param privKey the private key to derive the public key from
     * @return ECPoint public key
     */
    public static ECPoint publicPointFromPrivate(BigInteger privKey) {
        /*
         * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group
         * order, but that could change in future versions.
         */
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }

    /**
     * Returns public key point from the given curve.
     *
     * @param bits representing the point on the curve
     * @return BigInteger encoded public key
     */
    public static BigInteger publicFromPoint(byte[] bits) {
        return new BigInteger(1, Arrays.copyOfRange(bits, 1, bits.length)); // remove prefix
    }

    // =========================================================================
    // EIP-7702 authorization signing / recovery
    // =========================================================================

    /** EIP-7702 authorization signing magic byte. */
    public static final byte EIP7702_MAGIC = 0x05;

    private static final BigInteger NONCE_MAX = BigInteger.ONE.shiftLeft(64); // 2^64, exclusive
    private static final BigInteger CHAIN_ID_MAX =
            BigInteger.ONE.shiftLeft(256); // 2^256, exclusive
    private static final int ADDRESS_LENGTH_IN_BYTES = 20;

    /**
     * Computes the EIP-7702 authorization digest: {@code keccak256(0x05 ‖ rlp([chainId, address,
     * nonce]))}. This is the value signed (and recovered against) for a set-code authorization.
     *
     * @param chainId chain id the authorization is bound to ({@code 0} = valid on any chain)
     * @param address 20-byte delegation target address (0x-prefixed)
     * @param nonce authority account nonce the authorization is valid for
     * @return the 32-byte authorization hash
     */
    public static byte[] authorizationHash(BigInteger chainId, String address, BigInteger nonce) {
        byte[] addressBytes = validateAuthorizationFields(chainId, address, nonce);
        byte[] rlpEncoded =
                RlpEncoder.encode(
                        new RlpList(
                                RlpString.create(chainId),
                                RlpString.create(addressBytes),
                                RlpString.create(nonce)));
        byte[] toHash =
                ByteBuffer.allocate(1 + rlpEncoded.length)
                        .put(EIP7702_MAGIC)
                        .put(rlpEncoded)
                        .array();
        return Hash.sha3(toHash);
    }

    /**
     * Signs an EIP-7702 authorization with the given key pair, returning a fully populated (signed)
     * {@link AuthorizationTuple}. The signature is canonical (low-s) per EIP-2.
     *
     * @param chainId chain id the authorization is bound to ({@code 0} = valid on any chain)
     * @param address 20-byte delegation target address (0x-prefixed)
     * @param nonce authority account nonce the authorization is valid for
     * @param keyPair the authority's key pair
     * @return a signed authorization tuple
     */
    public static AuthorizationTuple signAuthorization(
            BigInteger chainId, String address, BigInteger nonce, ECKeyPair keyPair) {
        byte[] hash = authorizationHash(chainId, address, nonce);
        // needToHash = false: sign the precomputed digest directly. ECKeyPair.sign() canonicalises
        // to low-s, so EIP-2 is satisfied. v is the raw recovery id + LOWER_REAL_V (27).
        SignatureData sig = signMessage(hash, keyPair, false);
        BigInteger yParity = BigInteger.valueOf((sig.getV()[0] & 0xFF) - LOWER_REAL_V);
        BigInteger r = new BigInteger(1, sig.getR());
        BigInteger s = new BigInteger(1, sig.getS());
        return new AuthorizationTuple(chainId, address, nonce, yParity, r, s);
    }

    /** Convenience overload taking {@link Credentials}. */
    public static AuthorizationTuple signAuthorization(
            BigInteger chainId, String address, BigInteger nonce, Credentials credentials) {
        return signAuthorization(chainId, address, nonce, credentials.getEcKeyPair());
    }

    /** Convenience overload taking a {@code long} chainId. */
    public static AuthorizationTuple signAuthorization(
            long chainId, String address, BigInteger nonce, ECKeyPair keyPair) {
        return signAuthorization(BigInteger.valueOf(chainId), address, nonce, keyPair);
    }

    /** Convenience overload taking a {@code long} chainId and {@link Credentials}. */
    public static AuthorizationTuple signAuthorization(
            long chainId, String address, BigInteger nonce, Credentials credentials) {
        return signAuthorization(
                BigInteger.valueOf(chainId), address, nonce, credentials.getEcKeyPair());
    }

    /**
     * Recovers the authorizing account ("authority") that signed the given EIP-7702 authorization
     * tuple.
     *
     * @param authorization a signed authorization tuple
     * @return the 0x-prefixed authority address
     * @throws SignatureException if the signature is malformed or the key cannot be recovered
     */
    public static String recoverAuthorizationSigner(AuthorizationTuple authorization)
            throws SignatureException {
        BigInteger yParity = authorization.getYParity();
        if (yParity == null || yParity.signum() < 0 || yParity.compareTo(BigInteger.ONE) > 0) {
            throw new SignatureException("y_parity must be 0 or 1, got: " + yParity);
        }
        BigInteger s = authorization.getS();
        if (s != null && s.compareTo(HALF_CURVE_ORDER) > 0) {
            throw new SignatureException("s value is not canonical (must be <= secp256k1n/2)");
        }
        byte[] hash =
                authorizationHash(
                        authorization.getChainId(),
                        authorization.getAddress(),
                        authorization.getNonce());
        SignatureData signatureData =
                new SignatureData(
                        (byte) (yParity.intValue() + LOWER_REAL_V),
                        Numeric.toBytesPadded(authorization.getR(), 32),
                        Numeric.toBytesPadded(authorization.getS(), 32));
        BigInteger publicKey = signedMessageHashToKey(hash, signatureData);
        return Numeric.prependHexPrefix(Keys.getAddress(publicKey));
    }

    private static byte[] validateAuthorizationFields(
            BigInteger chainId, String address, BigInteger nonce) {
        if (chainId == null || chainId.signum() < 0 || chainId.compareTo(CHAIN_ID_MAX) >= 0) {
            throw new IllegalArgumentException("chainId must be in [0, 2^256): " + chainId);
        }
        if (nonce == null || nonce.signum() < 0 || nonce.compareTo(NONCE_MAX) >= 0) {
            throw new IllegalArgumentException("nonce must be in [0, 2^64): " + nonce);
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        byte[] addressBytes = Numeric.hexStringToByteArray(address);
        if (addressBytes.length != ADDRESS_LENGTH_IN_BYTES) {
            throw new IllegalArgumentException(
                    "address must be 20 bytes, got " + addressBytes.length + ": " + address);
        }
        return addressBytes;
    }

    public static class SignatureData {
        private final byte[] v;
        private final byte[] r;
        private final byte[] s;

        public SignatureData(byte v, byte[] r, byte[] s) {
            this(new byte[] {v}, r, s);
        }

        public SignatureData(byte[] v, byte[] r, byte[] s) {
            this.v = v;
            this.r = r;
            this.s = s;
        }

        public byte[] getV() {
            return v;
        }

        public byte[] getR() {
            return r;
        }

        public byte[] getS() {
            return s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SignatureData that = (SignatureData) o;

            if (!Arrays.equals(v, that.v)) {
                return false;
            }
            if (!Arrays.equals(r, that.r)) {
                return false;
            }
            return Arrays.equals(s, that.s);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(v);
            result = 31 * result + Arrays.hashCode(r);
            result = 31 * result + Arrays.hashCode(s);
            return result;
        }
    }
}
