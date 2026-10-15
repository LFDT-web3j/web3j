/*
 * Copyright 2025 Web3 Labs Ltd.
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
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.web3j.crypto.transaction.type.Transaction7702;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for EIP-7702 authorization signing and recovery ({@link Sign}). */
public class AuthorizationSigningTest {

    private static final String DELEGATION_TARGET = "0x5ce9454909639d2d17a3f753ce7d93fa0b9ab12e";

    @Test
    public void testRoundTripRecoversSigner() throws Exception {
        AuthorizationTuple tuple =
                Sign.signAuthorization(
                        BigInteger.ONE, DELEGATION_TARGET, BigInteger.TEN, SampleKeys.KEY_PAIR);

        assertEquals(BigInteger.ONE, tuple.getChainId());
        assertEquals(DELEGATION_TARGET, tuple.getAddress());
        assertEquals(BigInteger.TEN, tuple.getNonce());
        assertTrue(
                SampleKeys.ADDRESS.equalsIgnoreCase(Sign.recoverAuthorizationSigner(tuple)),
                "recovered authority must equal the signer");
    }

    @Test
    public void testChainIdZeroRoundTrips() throws Exception {
        // chainId 0 means the authorization is valid on any chain.
        AuthorizationTuple tuple =
                Sign.signAuthorization(
                        BigInteger.ZERO, DELEGATION_TARGET, BigInteger.ZERO, SampleKeys.KEY_PAIR);
        assertEquals(BigInteger.ZERO, tuple.getChainId());
        assertTrue(SampleKeys.ADDRESS.equalsIgnoreCase(Sign.recoverAuthorizationSigner(tuple)));
    }

    @Test
    public void testDeterministicSignature() {
        // web3j ECDSA is RFC-6979 deterministic: the same inputs yield the same (r, s, yParity).
        AuthorizationTuple a =
                Sign.signAuthorization(
                        BigInteger.valueOf(42),
                        DELEGATION_TARGET,
                        BigInteger.valueOf(7),
                        SampleKeys.KEY_PAIR);
        AuthorizationTuple b =
                Sign.signAuthorization(
                        BigInteger.valueOf(42),
                        DELEGATION_TARGET,
                        BigInteger.valueOf(7),
                        SampleKeys.KEY_PAIR);
        assertEquals(a, b);
    }

    @Test
    public void testSignatureIsLowS() {
        AuthorizationTuple tuple =
                Sign.signAuthorization(
                        BigInteger.valueOf(11155111),
                        DELEGATION_TARGET,
                        BigInteger.ONE,
                        SampleKeys.KEY_PAIR);
        assertTrue(
                tuple.getS().compareTo(Sign.HALF_CURVE_ORDER) <= 0,
                "s must be canonical (<= secp256k1n/2) per EIP-2");
    }

    @Test
    public void testInvalidAddressLengthRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Sign.signAuthorization(
                                BigInteger.ONE,
                                "0x5ce9454909639d2d17a3f753ce7d93fa0b9ab1", // 19 bytes
                                BigInteger.ZERO,
                                SampleKeys.KEY_PAIR));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Sign.signAuthorization(
                                BigInteger.ONE,
                                "0x5ce9454909639d2d17a3f753ce7d93fa0b9ab12e11", // 21 bytes
                                BigInteger.ZERO,
                                SampleKeys.KEY_PAIR));
    }

    @Test
    public void testNonceOutOfRangeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Sign.signAuthorization(
                                BigInteger.ONE,
                                DELEGATION_TARGET,
                                BigInteger.ONE.shiftLeft(64), // 2^64, out of range
                                SampleKeys.KEY_PAIR));
    }

    /**
     * Cross-implementation vector from eth-account (Python) {@code sign_authorization} docs:
     * private key 0xaa..aa, chainId 1337, address 0x5ce9..b12e, nonce 1. web3j must match the
     * authorization hash and (yParity, r, s) byte-for-byte.
     */
    @Test
    public void testCrossImplVectorEthAccount() throws Exception {
        Credentials credentials =
                Credentials.create(
                        "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        BigInteger chainId = BigInteger.valueOf(1337);
        String address = "0x5ce9454909639d2d17a3f753ce7d93fa0b9ab12e";
        BigInteger nonce = BigInteger.ONE;

        byte[] hash = Sign.authorizationHash(chainId, address, nonce);
        assertEquals(
                "0x9026f77ed6740d6d08f0cdc0591a86b2232700020a816718fbf760785e9ca2f2",
                Numeric.toHexString(hash));

        AuthorizationTuple tuple = Sign.signAuthorization(chainId, address, nonce, credentials);
        assertEquals(BigInteger.ZERO, tuple.getYParity());
        assertEquals(
                new BigInteger(
                        "52163433520757118830640642673035732532535423029712132518776649895118143897479"),
                tuple.getR());
        assertEquals(
                new BigInteger(
                        "57576671166887700066365341925867052133948674355067837907255957076179513983345"),
                tuple.getS());
        assertTrue(
                credentials.getAddress().equalsIgnoreCase(Sign.recoverAuthorizationSigner(tuple)));
    }

    @Test
    public void testAuthorizationTupleFromFactory() {
        AuthorizationTuple viaSign =
                Sign.signAuthorization(
                        BigInteger.valueOf(5),
                        DELEGATION_TARGET,
                        BigInteger.TWO,
                        SampleKeys.KEY_PAIR);
        AuthorizationTuple viaFactory =
                AuthorizationTuple.from(
                        BigInteger.valueOf(5),
                        DELEGATION_TARGET,
                        BigInteger.TWO,
                        SampleKeys.KEY_PAIR);
        assertEquals(viaSign, viaFactory);
    }

    /**
     * End-to-end: sign an authorization, embed it in a full EIP-7702 transaction, encode + decode
     * the transaction, and recover the authority from the decoded tuple — proving the whole
     * pipeline.
     */
    @Test
    public void testEndToEndSetCodeTransaction() throws Exception {
        AuthorizationTuple authorization =
                Sign.signAuthorization(
                        1L, DELEGATION_TARGET, BigInteger.valueOf(3), SampleKeys.KEY_PAIR);

        RawTransaction rawTransaction =
                RawTransaction.createTransaction(
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        "0x0000000000000000000000000000000000000001",
                        BigInteger.ZERO,
                        "",
                        Collections.emptyList(),
                        Collections.singletonList(authorization));

        byte[] signed = TransactionEncoder.signMessage(rawTransaction, SampleKeys.CREDENTIALS);
        RawTransaction decoded = TransactionDecoder.decode(Numeric.toHexString(signed));

        Transaction7702 tx = (Transaction7702) decoded.getTransaction();
        AuthorizationTuple decodedAuthorization = tx.getAuthorizationList().get(0);
        assertEquals(authorization, decodedAuthorization);
        assertTrue(
                SampleKeys.ADDRESS.equalsIgnoreCase(
                        Sign.recoverAuthorizationSigner(decodedAuthorization)),
                "authority recovered from the decoded tuple must equal the signer");
    }
}
