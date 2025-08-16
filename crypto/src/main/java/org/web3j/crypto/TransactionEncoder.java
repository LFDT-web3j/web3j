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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

import org.web3j.crypto.exception.CryptoWeb3jException;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import static org.web3j.crypto.Sign.CHAIN_ID_INC;
import static org.web3j.crypto.Sign.LOWER_REAL_V;

/**
 * Create RLP encoded transaction, implementation as per p4 of the <a
 * href="http://gavwood.com/paper.pdf">yellow paper</a>.
 */
public class TransactionEncoder {

    /**
     * Use for new transactions Eip1559 (this txs has a new field chainId) or an old one before
     * Eip155
     *
     * @return signature
     */
    public static byte[] signMessage(RawTransaction rawTransaction, Credentials credentials) {
        byte[] encodedTransaction;
        if (rawTransaction.getTransaction().getType().isEip4844()) {
            encodedTransaction = encode4844(rawTransaction);
        } else {
            encodedTransaction = encode(rawTransaction);
        }
        Sign.SignatureData signatureData =
                Sign.signMessage(encodedTransaction, credentials.getEcKeyPair());

        return encode(rawTransaction, signatureData);
    }

    /**
     * Use for legacy txs (after Eip155 before Eip1559)
     *
     * @return signature
     */
    public static byte[] signMessage(
            RawTransaction rawTransaction, long chainId, Credentials credentials) {

        // Eip1559: Tx has ChainId inside
        if (rawTransaction.getType().isEip1559()) {
            return signMessage(rawTransaction, credentials);
        }

        byte[] encodedTransaction = encode(rawTransaction, chainId);
        Sign.SignatureData signatureData =
                Sign.signMessage(encodedTransaction, credentials.getEcKeyPair());

        Sign.SignatureData eip155SignatureData = createEip155SignatureData(signatureData, chainId);
        return encode(rawTransaction, eip155SignatureData);
    }

    @Deprecated
    public static byte[] signMessage(
            RawTransaction rawTransaction, byte chainId, Credentials credentials) {
        return signMessage(rawTransaction, (long) chainId, credentials);
    }

    public static Sign.SignatureData createEip155SignatureData(
            Sign.SignatureData signatureData, long chainId) {
        BigInteger v = Numeric.toBigInt(signatureData.getV());
        v = v.subtract(BigInteger.valueOf(LOWER_REAL_V));
        v = v.add(BigInteger.valueOf(chainId).multiply(BigInteger.valueOf(2)));
        v = v.add(BigInteger.valueOf(CHAIN_ID_INC));

        return new Sign.SignatureData(v.toByteArray(), signatureData.getR(), signatureData.getS());
    }

    @Deprecated
    public static Sign.SignatureData createEip155SignatureData(
            Sign.SignatureData signatureData, byte chainId) {
        return createEip155SignatureData(signatureData, (long) chainId);
    }

    public static byte[] encode(RawTransaction rawTransaction) {
        return encode(rawTransaction, null);
    }

    /**
     * Encode transaction with chainId together, it makes sense only for Legacy transactions
     *
     * @return encoded bytes
     */
    public static byte[] encode(RawTransaction rawTransaction, long chainId) {
        if (!rawTransaction.getType().isLegacy()) {
            throw new CryptoWeb3jException("Incorrect transaction type. Tx type should be Legacy.");
        }

        Sign.SignatureData signatureData =
                new Sign.SignatureData(longToBytes(chainId), new byte[] {}, new byte[] {});
        return encode(rawTransaction, signatureData);
    }

    @Deprecated
    public static byte[] encode(RawTransaction rawTransaction, byte chainId) {
        return encode(rawTransaction, (long) chainId);
    }

    public static byte[] encode(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        byte[] encoded = RlpEncoder.encode(rlpList);

        if (rawTransaction.getType().isEip1559()
                || rawTransaction.getType().isEip2930()
                || rawTransaction.getType().isEip4844()
                || rawTransaction.getType().isEip7702()) {
            return ByteBuffer.allocate(encoded.length + 1)
                    .put(rawTransaction.getType().getRlpType())
                    .put(encoded)
                    .array();
        }
        return encoded;
    }

    public static byte[] encode4844(RawTransaction rawTransaction) {
        List<RlpType> values = asRlpValues(rawTransaction, null);
        RlpList rlpList = new RlpList(values);

        byte[] encoded = RlpEncoder.encode(rlpList);

        return ByteBuffer.allocate(encoded.length + 1)
                .put(rawTransaction.getType().getRlpType())
                .put(encoded)
                .array();
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static List<RlpType> asRlpValues(
            RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        return rawTransaction.getTransaction().asRlpValues(signatureData);
    }
}
