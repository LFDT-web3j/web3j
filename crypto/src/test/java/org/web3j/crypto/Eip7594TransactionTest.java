/*
 * Copyright 2026 Web3 Labs Ltd.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import org.web3j.crypto.transaction.type.Transaction4844;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Production-grade test suite for EIP-7594 (PeerDAS / Fusaka) blob transaction support.
 *
 * <p>Covers:
 *
 * <ul>
 *   <li>Spec-compliant flat cell_proofs encoding/decoding round-trip
 *   <li>CELLS_PER_EXT_BLOB = 128 proofs per blob validation
 *   <li>wrapper_version == 1 enforcement
 *   <li>Proof and commitment byte-length (48 bytes) enforcement
 *   <li>Multi-blob EIP-7594 transaction
 *   <li>Access list round-trip preservation
 *   <li>EIP-4844 backward compatibility (no regression)
 *   <li>Decoder rejection of malformed payloads
 *   <li>Signing payload isolation (tx hash not contaminated by sidecar)
 * </ul>
 */
public class Eip7594TransactionTest {

    private static final String PRIVATE_KEY =
            "0x45a915e4d060149eb43658c930b24c694a91a91a91a91a91a91a91a91a91a91a";

    private static final String TO_ADDRESS = "0x0000000000000000000000000000000000000001";

    private static final int CELLS_PER_EXT_BLOB = Transaction4844.CELLS_PER_EXT_BLOB; // 128
    private static final int PROOF_BYTES = Transaction4844.KZG_PROOF_BYTE_LENGTH; // 48

    // =========================================================================
    // Helper builders
    // =========================================================================

    /**
     * Creates a list of N zero-filled 48-byte proofs (structurally valid, not cryptographically).
     */
    private static List<Bytes> makeProofs(int count) {
        List<Bytes> proofs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            proofs.add(Bytes.wrap(new byte[PROOF_BYTES]));
        }
        return proofs;
    }

    /** Creates a single blob filled with zeros. */
    private static Blob zeroBlob() {
        return new Blob(new byte[131072]);
    }

    /** Creates N zero blobs. */
    private static List<Blob> blobs(int n) {
        List<Blob> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(zeroBlob());
        return list;
    }

    /** Creates N 48-byte commitments. */
    private static List<Bytes> commitments(int n) {
        List<Bytes> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(Bytes.wrap(new byte[PROOF_BYTES]));
        return list;
    }

    /** Creates N 32-byte versioned hashes. */
    private static List<Bytes> versionedHashes(int n) {
        List<Bytes> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(Bytes.wrap(new byte[32]));
        return list;
    }

    /** A sample single-entry access list (one address, one storage key). */
    private static List<AccessListObject> sampleAccessList() {
        return Collections.singletonList(
                new AccessListObject(
                        "0xde0B295669a9FD93d5F28D9Ec85E40f4cb697BAe",
                        Collections.singletonList(
                                "0x0000000000000000000000000000000000000000000000000000000000000003")));
    }

    /**
     * Asserts that a decoded access list matches the expected one. The decoder normalises hex to
     * lower case (RlpString.asString -> Numeric.toHexString), so addresses/keys are compared
     * case-insensitively.
     */
    private static void assertAccessListEquals(
            List<AccessListObject> expected, List<AccessListObject> actual) {
        assertNotNull(actual, "decoded access list must not be null");
        assertEquals(expected.size(), actual.size(), "access list size mismatch");
        for (int i = 0; i < expected.size(); i++) {
            AccessListObject e = expected.get(i);
            AccessListObject a = actual.get(i);
            assertTrue(
                    e.getAddress().equalsIgnoreCase(a.getAddress()),
                    "access list address mismatch: expected "
                            + e.getAddress()
                            + " got "
                            + a.getAddress());
            assertEquals(
                    e.getStorageKeys().size(),
                    a.getStorageKeys().size(),
                    "storage key count mismatch");
            for (int k = 0; k < e.getStorageKeys().size(); k++) {
                assertTrue(
                        e.getStorageKeys().get(k).equalsIgnoreCase(a.getStorageKeys().get(k)),
                        "storage key mismatch at "
                                + k
                                + ": expected "
                                + e.getStorageKeys().get(k)
                                + " got "
                                + a.getStorageKeys().get(k));
            }
        }
    }

    // =========================================================================
    // 1. Core encode/decode round-trip — single blob
    // =========================================================================

    @Test
    public void testSingleBlobOsakaRoundTrip() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        int blobCount = 1;
        List<Blob> blobs = blobs(blobCount);
        List<Bytes> kzgCommitments = commitments(blobCount);
        // EIP-7594: FLAT list of CELLS_PER_EXT_BLOB * blobCount proofs
        List<Bytes> cellProofs = makeProofs(CELLS_PER_EXT_BLOB * blobCount);

        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs,
                        kzgCommitments,
                        cellProofs,
                        Transaction4844.WRAPPER_VERSION_1,
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(blobCount));

        byte[] signed = TransactionEncoder.signMessage(rawTx, credentials);
        RawTransaction decoded = TransactionDecoder.decode(Numeric.toHexString(signed));

        assertTrue(decoded.getTransaction() instanceof Transaction4844);
        Transaction4844 tx = (Transaction4844) decoded.getTransaction();

        // Verify EIP-7594 fields
        assertTrue(tx.getWrapperVersion().isPresent(), "wrapperVersion must be present");
        assertEquals(BigInteger.ONE, tx.getWrapperVersion().get());

        assertTrue(tx.getCellProofs().isPresent(), "cellProofs must be present");
        assertEquals(
                CELLS_PER_EXT_BLOB,
                tx.getCellProofs().get().size(),
                "cell_proofs must be a flat list of CELLS_PER_EXT_BLOB entries");

        // Each proof must be 48 bytes
        for (Bytes proof : tx.getCellProofs().get()) {
            assertEquals(PROOF_BYTES, proof.size(), "each cell proof must be 48 bytes");
        }

        // EIP-4844 kzgProofs must be absent
        assertFalse(
                tx.getKzgProofs().isPresent() && !tx.getKzgProofs().get().isEmpty(),
                "EIP-7594 tx must not carry kzgProofs");

        // Blobs and commitments
        assertTrue(tx.getBlobs().isPresent());
        assertEquals(blobCount, tx.getBlobs().get().size());
        assertTrue(tx.getKzgCommitments().isPresent());
        assertEquals(blobCount, tx.getKzgCommitments().get().size());
    }

    // =========================================================================
    // 2. Multi-blob EIP-7594 transaction (3 blobs = 384 cell proofs)
    // =========================================================================

    @Test
    public void testMultiBlobOsakaRoundTrip() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        int blobCount = 3;
        List<Bytes> cellProofs = makeProofs(CELLS_PER_EXT_BLOB * blobCount); // 384

        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs(blobCount),
                        commitments(blobCount),
                        cellProofs,
                        Transaction4844.WRAPPER_VERSION_1,
                        1L,
                        BigInteger.ONE,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(blobCount));

        byte[] signed = TransactionEncoder.signMessage(rawTx, credentials);
        RawTransaction decoded = TransactionDecoder.decode(Numeric.toHexString(signed));

        Transaction4844 tx = (Transaction4844) decoded.getTransaction();
        assertEquals(
                CELLS_PER_EXT_BLOB * blobCount,
                tx.getCellProofs().get().size(),
                "3-blob tx must have 384 cell proofs");
        assertEquals(blobCount, tx.getBlobs().get().size());
        assertEquals(blobCount, tx.getKzgCommitments().get().size());
    }

    // =========================================================================
    // 3. Access list round-trip preservation
    // =========================================================================

    @Test
    public void testAccessListPreservation() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        List<AccessListObject> accessList = sampleAccessList();

        int blobCount = 1;
        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs(blobCount),
                        commitments(blobCount),
                        makeProofs(CELLS_PER_EXT_BLOB),
                        Transaction4844.WRAPPER_VERSION_1,
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(blobCount),
                        accessList);

        byte[] signed = TransactionEncoder.signMessage(rawTx, credentials);
        RawTransaction decoded = TransactionDecoder.decode(Numeric.toHexString(signed));
        assertTrue(decoded instanceof SignedRawTransaction);

        // The access list must round-trip unchanged through encode/decode.
        Transaction4844 tx = (Transaction4844) decoded.getTransaction();
        assertAccessListEquals(accessList, tx.getAccessList());
    }

    // =========================================================================
    // 4. Signing payload isolation — EIP-7594 sidecar must NOT affect tx hash
    // =========================================================================

    @Test
    public void testSigningPayloadExcludesSidecar() {
        // Two transactions: one with sidecar, one without (versioned hashes only).
        // The signing payload (encode4844 output) must be identical since the
        // tx_payload_body fields are the same.
        List<Bytes> vh = versionedHashes(1);

        RawTransaction txNoSidecar =
                RawTransaction.createTransaction(
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        vh);

        RawTransaction txWithSidecar =
                RawTransaction.createTransaction(
                        blobs(1),
                        commitments(1),
                        makeProofs(CELLS_PER_EXT_BLOB),
                        Transaction4844.WRAPPER_VERSION_1,
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        vh);

        // encode4844 gives the signing payload (no sidecar)
        byte[] payloadNoSidecar = TransactionEncoder.encode4844(txNoSidecar);
        byte[] payloadWithSidecar = TransactionEncoder.encode4844(txWithSidecar);

        assertArrayEquals(
                payloadNoSidecar,
                payloadWithSidecar,
                "Signing payload must be identical regardless of sidecar presence");
    }

    // =========================================================================
    // 5. EIP-4844 backward compatibility round-trip
    // =========================================================================

    @Test
    public void testEip4844BackwardCompatibilityRoundTrip() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        int blobCount = 2;
        // EIP-4844: one proof per blob (not per cell)
        List<Bytes> kzgProofs = makeProofs(blobCount);

        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs(blobCount),
                        commitments(blobCount),
                        kzgProofs,
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(blobCount));

        byte[] signed = TransactionEncoder.signMessage(rawTx, credentials);
        RawTransaction decoded = TransactionDecoder.decode(Numeric.toHexString(signed));

        assertTrue(decoded.getTransaction() instanceof Transaction4844);
        Transaction4844 tx = (Transaction4844) decoded.getTransaction();

        // EIP-4844: wrapperVersion and cellProofs must be absent
        assertFalse(tx.getWrapperVersion().isPresent(), "EIP-4844 must not have wrapperVersion");
        assertFalse(
                tx.getCellProofs().isPresent() && !tx.getCellProofs().get().isEmpty(),
                "EIP-4844 must not have cellProofs");

        // kzgProofs must be present with one proof per blob
        assertTrue(tx.getKzgProofs().isPresent());
        assertEquals(blobCount, tx.getKzgProofs().get().size());
        assertEquals(blobCount, tx.getBlobs().get().size());
        assertEquals(blobCount, tx.getKzgCommitments().get().size());
    }

    // =========================================================================
    // 6. Validation: wrong wrapper_version rejected
    // =========================================================================

    @Test
    public void testInvalidWrapperVersionRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(1),
                                commitments(1),
                                makeProofs(CELLS_PER_EXT_BLOB),
                                BigInteger.valueOf(2), // invalid: only 1 is valid
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(1)),
                "wrapper_version != 1 must throw IllegalArgumentException");
    }

    @Test
    public void testZeroWrapperVersionRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(1),
                                commitments(1),
                                makeProofs(CELLS_PER_EXT_BLOB),
                                BigInteger.ZERO,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(1)));
    }

    // =========================================================================
    // 7. Validation: wrong cell_proofs count rejected
    // =========================================================================

    @Test
    public void testTooFewCellProofsRejected() {
        // 1 blob needs 128 cell proofs; providing 127 must fail
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(1),
                                commitments(1),
                                makeProofs(CELLS_PER_EXT_BLOB - 1),
                                Transaction4844.WRAPPER_VERSION_1,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(1)),
                "127 cell proofs for 1 blob must be rejected");
    }

    @Test
    public void testTooManyCellProofsRejected() {
        // 1 blob needs 128 cell proofs; providing 129 must fail
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(1),
                                commitments(1),
                                makeProofs(CELLS_PER_EXT_BLOB + 1),
                                Transaction4844.WRAPPER_VERSION_1,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(1)),
                "129 cell proofs for 1 blob must be rejected");
    }

    @Test
    public void testWrongCellProofCountMultiBlob() {
        // 2 blobs need 256 proofs; providing 128 must fail
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(2),
                                commitments(2),
                                makeProofs(CELLS_PER_EXT_BLOB), // only 128, need 256
                                Transaction4844.WRAPPER_VERSION_1,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(2)));
    }

    // =========================================================================
    // 8. Validation: proof byte length (48 bytes) enforced
    // =========================================================================

    @Test
    public void testProofWrongByteLengthRejected() {
        // Build 128 proofs but make one 47 bytes
        List<Bytes> badProofs = makeProofs(CELLS_PER_EXT_BLOB);
        badProofs.set(0, Bytes.wrap(new byte[47])); // wrong length

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(1),
                                commitments(1),
                                badProofs,
                                Transaction4844.WRAPPER_VERSION_1,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(1)),
                "47-byte proof must be rejected");
    }

    @Test
    public void testCommitmentWrongByteLengthRejected() {
        List<Bytes> badCommitments = commitments(1);
        badCommitments.set(0, Bytes.wrap(new byte[49])); // wrong length

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(1),
                                badCommitments,
                                makeProofs(CELLS_PER_EXT_BLOB),
                                Transaction4844.WRAPPER_VERSION_1,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(1)),
                "49-byte commitment must be rejected");
    }

    // =========================================================================
    // 9. Validation: mismatched blobs vs commitments count rejected
    // =========================================================================

    @Test
    public void testBlobCommitmentCountMismatchRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(1),
                                commitments(2), // mismatch: 1 blob but 2 commitments
                                makeProofs(CELLS_PER_EXT_BLOB),
                                Transaction4844.WRAPPER_VERSION_1,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(1)));
    }

    // =========================================================================
    // 10. Validation: EIP-4844 blobs/commitments/proofs count mismatch
    // =========================================================================

    @Test
    public void testEip4844ProofCountMismatchRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(1),
                                commitments(1),
                                makeProofs(2), // 2 kzgProofs for 1 blob — mismatch
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(1)),
                "2 kzgProofs for 1 blob must be rejected");
    }

    // =========================================================================
    // 11. Validation: null blobsData explicit rejection
    // =========================================================================

    @Test
    public void testNullBlobsDataRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Transaction4844.createTransaction(
                                (List<Blob>) null,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN));
    }

    // =========================================================================
    // 12. cell_proofs flat structure verified in encoded bytes
    //     After encoding, decode the raw RLP and verify cell_proofs entries
    //     are direct RlpString elements (not nested RlpLists)
    // =========================================================================

    @Test
    public void testCellProofsAreEncodedAsFlat() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);
        int blobCount = 1;

        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs(blobCount),
                        commitments(blobCount),
                        makeProofs(CELLS_PER_EXT_BLOB),
                        Transaction4844.WRAPPER_VERSION_1,
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(blobCount));

        byte[] encoded = TransactionEncoder.signMessage(rawTx, credentials);
        // Strip type byte 0x03
        byte[] rlpBytes = java.util.Arrays.copyOfRange(encoded, 1, encoded.length);
        org.web3j.rlp.RlpList outer =
                (org.web3j.rlp.RlpList)
                        org.web3j.rlp.RlpDecoder.decode(rlpBytes).getValues().get(0);

        // outer = [tx_payload_body, wrapper_version, blobs, commitments, cell_proofs]
        assertEquals(5, outer.getValues().size(), "EIP-7594 outer wrapper must have 5 elements");

        org.web3j.rlp.RlpList cellProofsList = (org.web3j.rlp.RlpList) outer.getValues().get(4);
        assertEquals(
                CELLS_PER_EXT_BLOB,
                cellProofsList.getValues().size(),
                "cell_proofs list must have CELLS_PER_EXT_BLOB entries");

        // Each entry must be an RlpString (flat), NOT an RlpList (nested)
        for (int i = 0; i < cellProofsList.getValues().size(); i++) {
            assertTrue(
                    cellProofsList.getValues().get(i) instanceof org.web3j.rlp.RlpString,
                    "cell_proofs[" + i + "] must be RlpString (flat), not RlpList (nested)");
        }
    }

    // =========================================================================
    // 13. wrapper_version encoded as single byte 0x01
    // =========================================================================

    @Test
    public void testWrapperVersionEncodedCorrectly() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs(1),
                        commitments(1),
                        makeProofs(CELLS_PER_EXT_BLOB),
                        Transaction4844.WRAPPER_VERSION_1,
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(1));

        byte[] encoded = TransactionEncoder.signMessage(rawTx, credentials);
        byte[] rlpBytes = java.util.Arrays.copyOfRange(encoded, 1, encoded.length);
        org.web3j.rlp.RlpList outer =
                (org.web3j.rlp.RlpList)
                        org.web3j.rlp.RlpDecoder.decode(rlpBytes).getValues().get(0);

        // wrapper_version is at index 1
        org.web3j.rlp.RlpString wv = (org.web3j.rlp.RlpString) outer.getValues().get(1);
        assertEquals(
                BigInteger.ONE,
                wv.asPositiveBigInteger(),
                "wrapper_version must decode as BigInteger.ONE");
        assertEquals(1, wv.getBytes().length, "wrapper_version must be encoded as 1 byte");
    }

    // =========================================================================
    // 14. EIP-7594 constants sanity check
    // =========================================================================

    @Test
    public void testEip7594ConstantsAreCorrect() {
        // FIELD_ELEMENTS_PER_EXT_BLOB = 8192, FIELD_ELEMENTS_PER_CELL = 64
        // CELLS_PER_EXT_BLOB = 8192 / 64 = 128
        assertEquals(128, Transaction4844.CELLS_PER_EXT_BLOB);
        assertEquals(48, Transaction4844.KZG_PROOF_BYTE_LENGTH);
        assertEquals(BigInteger.ONE, Transaction4844.WRAPPER_VERSION_1);
    }

    // =========================================================================
    // 15. MAX_BLOBS_PER_TRANSACTION = 6 enforcement (EIP-7594 spec)
    // =========================================================================

    @Test
    public void testMaxBlobsPerTransactionEnforced() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RawTransaction.createTransaction(
                                blobs(7),
                                commitments(7),
                                makeProofs(CELLS_PER_EXT_BLOB * 7),
                                Transaction4844.WRAPPER_VERSION_1,
                                1L,
                                BigInteger.ZERO,
                                BigInteger.TEN,
                                BigInteger.TEN,
                                BigInteger.valueOf(21000),
                                TO_ADDRESS,
                                BigInteger.ZERO,
                                "",
                                BigInteger.TEN,
                                versionedHashes(7)),
                "7 blobs must be rejected (max 6)");
    }

    @Test
    public void testSixBlobsAccepted() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);
        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs(6),
                        commitments(6),
                        makeProofs(CELLS_PER_EXT_BLOB * 6),
                        Transaction4844.WRAPPER_VERSION_1,
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(6));
        byte[] signed = TransactionEncoder.signMessage(rawTx, credentials);
        RawTransaction decoded = TransactionDecoder.decode(Numeric.toHexString(signed));
        Transaction4844 tx = (Transaction4844) decoded.getTransaction();
        assertEquals(6, tx.getBlobs().get().size());
        assertEquals(CELLS_PER_EXT_BLOB * 6, tx.getCellProofs().get().size());
    }

    // =========================================================================
    // 16. Access list properly passed in EIP-7594 transactions
    // =========================================================================

    @Test
    public void testAccessListPassedThroughInEip7594() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);
        List<AccessListObject> accessList = sampleAccessList();
        int blobCount = 1;
        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs(blobCount),
                        commitments(blobCount),
                        makeProofs(CELLS_PER_EXT_BLOB),
                        Transaction4844.WRAPPER_VERSION_1,
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(blobCount),
                        accessList);
        byte[] signed = TransactionEncoder.signMessage(rawTx, credentials);
        RawTransaction decoded = TransactionDecoder.decode(Numeric.toHexString(signed));
        assertTrue(decoded instanceof SignedRawTransaction);
        Transaction4844 tx = (Transaction4844) decoded.getTransaction();
        assertTrue(tx.getWrapperVersion().isPresent());
        assertEquals(BigInteger.ONE, tx.getWrapperVersion().get());
        assertTrue(tx.getCellProofs().isPresent());
        assertAccessListEquals(accessList, tx.getAccessList());
    }

    // =========================================================================
    // 17. Access list round-trip on the EIP-4844 (kzgProofs) path
    //     Regression test: the 14-arg createTransaction overload must thread the
    //     access list through to tx_payload_body. Before the fix it was dropped,
    //     yielding a different tx_payload_body (and thus a different tx hash).
    // =========================================================================

    @Test
    public void testEip4844AccessListRoundTrip() throws Exception {
        Credentials credentials = Credentials.create(PRIVATE_KEY);
        List<AccessListObject> accessList = sampleAccessList();

        int blobCount = 1;
        // EIP-4844: one proof per blob (not per cell), via the kzgProofs overload.
        RawTransaction rawTx =
                RawTransaction.createTransaction(
                        blobs(blobCount),
                        commitments(blobCount),
                        makeProofs(blobCount),
                        1L,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        TO_ADDRESS,
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        versionedHashes(blobCount),
                        accessList);

        byte[] signed = TransactionEncoder.signMessage(rawTx, credentials);
        RawTransaction decoded = TransactionDecoder.decode(Numeric.toHexString(signed));

        Transaction4844 tx = (Transaction4844) decoded.getTransaction();
        // This is an EIP-4844 tx: no wrapper_version, no cell_proofs.
        assertFalse(tx.getWrapperVersion().isPresent(), "EIP-4844 must not have wrapperVersion");
        // The access list must survive the round-trip (regression for the dropped-access-list bug).
        assertAccessListEquals(accessList, tx.getAccessList());
    }

    // =========================================================================
    // 18. Decoder rejection of malformed payloads
    //     These feed crafted RLP directly to TransactionDecoder.decode and assert
    //     the guarded branches throw IllegalArgumentException (not a raw cast).
    // =========================================================================

    /** Encodes {@code outer} as a type-0x03 transaction and runs it through the decoder. */
    private static RawTransaction decodeType3(RlpType outer) {
        byte[] rlp = RlpEncoder.encode(outer);
        byte[] tx = new byte[rlp.length + 1];
        tx[0] = 0x03;
        System.arraycopy(rlp, 0, tx, 1, rlp.length);
        return TransactionDecoder.decode(Numeric.toHexString(tx));
    }

    /** Builds a tx_payload_body RlpList with {@code n} empty RlpString fields. */
    private static RlpList bodyWithFields(int n) {
        List<RlpType> fields = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            fields.add(RlpString.create(""));
        }
        return new RlpList(fields);
    }

    @Test
    public void testDecoderRejectsWrongWrapperElementCount() {
        // First element is a list (network wrapper), but total size is neither 4 nor 5.
        RlpList outer = new RlpList(bodyWithFields(11), RlpString.create(""), RlpString.create(""));
        assertThrows(IllegalArgumentException.class, () -> decodeType3(outer));
    }

    @Test
    public void testDecoderRejectsNonStringWrapperVersion() {
        // 5-element wrapper, but wrapper_version (index 1) is an RlpList, not an RlpString.
        RlpList outer =
                new RlpList(
                        bodyWithFields(11),
                        new RlpList(),
                        new RlpList(),
                        new RlpList(),
                        new RlpList());
        assertThrows(IllegalArgumentException.class, () -> decodeType3(outer));
    }

    @Test
    public void testDecoderRejectsNonListBlobs() {
        // 5-element wrapper, valid version, but blobs (index 2) is an RlpString, not an RlpList.
        RlpList outer =
                new RlpList(
                        bodyWithFields(11),
                        RlpString.create(BigInteger.ONE),
                        RlpString.create("blobs"),
                        new RlpList(),
                        new RlpList());
        assertThrows(IllegalArgumentException.class, () -> decodeType3(outer));
    }

    @Test
    public void testDecoderRejectsMissingAccessList() {
        // 5-element wrapper with valid types, but tx_payload_body has < 9 fields (no index 8).
        RlpList outer =
                new RlpList(
                        bodyWithFields(3),
                        RlpString.create(BigInteger.ONE),
                        new RlpList(),
                        new RlpList(),
                        new RlpList());
        assertThrows(IllegalArgumentException.class, () -> decodeType3(outer));
    }

    @Test
    public void testDecoderRejectsTooFewPayloadFields() {
        // No-sidecar body (first element is an RlpString) with fewer than 11 fields.
        RlpList outer = bodyWithFields(5);
        assertThrows(IllegalArgumentException.class, () -> decodeType3(outer));
    }
}
