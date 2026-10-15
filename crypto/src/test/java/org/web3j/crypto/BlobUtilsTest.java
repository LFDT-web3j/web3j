/*
 * Copyright 2024 Web3 Labs Ltd.
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ethereum.ckzg4844.CKZG4844JNI;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

import org.web3j.crypto.transaction.type.Transaction4844;
import org.web3j.utils.Numeric;

import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobUtilsTest {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Test
    public void testBlobToCommitmentProofVersionedHashes() throws Exception {
        // Use the shared trusted setup loaded once by BlobUtils' static initializer (do not
        // manually load/free it here — that conflicts with every other BlobUtils-based test).
        Blob blob =
                new Blob(
                        Numeric.hexStringToByteArray(
                                loadResourceAsString("blob_data/blob_data_1.txt")));
        Bytes commitment = BlobUtils.getCommitment(blob);
        Bytes proofs = BlobUtils.getProof(blob, commitment);

        assertTrue(BlobUtils.checkProofValidity(blob, commitment, proofs));
        assertEquals(
                "0xc00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                commitment.toHexString());
        assertEquals(
                "0xc00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                proofs.toHexString());
    }

    @Test
    public void testBlobUtils() throws Exception {
        Blob blob =
                new Blob(
                        Numeric.hexStringToByteArray(
                                loadResourceAsString("blob_data/blob_data_2.txt")));
        Bytes commitment = BlobUtils.getCommitment(blob);
        Bytes proofs = BlobUtils.getProof(blob, commitment);
        Bytes versionedHashes = BlobUtils.kzgToVersionedHash(commitment);

        assertTrue(BlobUtils.checkProofValidity(blob, commitment, proofs));
        assertEquals(
                "0xb44bafc7381d7ba2072cfbb7091c1fa1fdabcf3999270a551fe54a6741ddebc1bdfbeeabe1b74f5c3935aeedf6b2db84",
                commitment.toHexString());
        assertEquals(
                "0x963150f3ee4d5e5f065429f587b4fa199cd8a866b8f6388eb52372870052603c98194c6521077c3260c41bf3b796c833",
                proofs.toHexString());
        assertEquals(
                "0x018ef96865998238a5e1783b6cafbc1253235d636f15d318f1fb50ef6a5b8f6a",
                versionedHashes.toHexString());
    }

    @Test
    public void testGetCellProofsSingleBlob() throws Exception {
        Blob blob =
                new Blob(
                        Numeric.hexStringToByteArray(
                                loadResourceAsString("blob_data/blob_data_1.txt")));

        List<Bytes> cellProofs = BlobUtils.getCellProofs(blob);

        assertEquals(
                Transaction4844.CELLS_PER_EXT_BLOB,
                cellProofs.size(),
                "a single blob must yield CELLS_PER_EXT_BLOB (128) cell proofs");
        for (Bytes proof : cellProofs) {
            assertEquals(
                    Transaction4844.KZG_PROOF_BYTE_LENGTH,
                    proof.size(),
                    "each cell proof must be 48 bytes");
        }
    }

    @Test
    public void testCheckCellProofsValidity() throws Exception {
        // Use the non-trivial blob (blob_data_2) so its 128 cell proofs are distinct — a zero blob
        // would have identical proofs, making the mis-assignment negative case a no-op.
        Blob blob =
                new Blob(
                        Numeric.hexStringToByteArray(
                                loadResourceAsString("blob_data/blob_data_2.txt")));
        Bytes commitment = BlobUtils.getCommitment(blob);
        List<Bytes> cellProofs = BlobUtils.getCellProofs(blob);

        assertTrue(
                BlobUtils.checkCellProofsValidity(
                        java.util.Collections.singletonList(blob),
                        java.util.Collections.singletonList(commitment),
                        cellProofs),
                "freshly generated cell proofs must verify");

        // Mis-assign two proofs (both still valid G1 points, but for the wrong cells): the batch
        // must now fail verification cleanly (rather than throwing on a malformed point).
        Bytes p0 = cellProofs.get(0);
        cellProofs.set(0, cellProofs.get(1));
        cellProofs.set(1, p0);
        assertFalse(
                BlobUtils.checkCellProofsValidity(
                        java.util.Collections.singletonList(blob),
                        java.util.Collections.singletonList(commitment),
                        cellProofs),
                "mis-assigned cell proofs must fail verification");
    }

    @Test
    public void testGetCellProofsMultiBlobOrdering() throws Exception {
        Blob blobA =
                new Blob(
                        Numeric.hexStringToByteArray(
                                loadResourceAsString("blob_data/blob_data_1.txt")));
        Blob blobB =
                new Blob(
                        Numeric.hexStringToByteArray(
                                loadResourceAsString("blob_data/blob_data_2.txt")));

        List<Bytes> proofsA = BlobUtils.getCellProofs(blobA);
        List<Bytes> proofsB = BlobUtils.getCellProofs(blobB);
        List<Bytes> combined = BlobUtils.getCellProofs(Arrays.asList(blobA, blobB));

        int cells = Transaction4844.CELLS_PER_EXT_BLOB;
        assertEquals(2 * cells, combined.size(), "two blobs must yield 256 cell proofs");
        // Blob-major order: first 128 == blobA's proofs, next 128 == blobB's proofs.
        assertEquals(proofsA, combined.subList(0, cells));
        assertEquals(proofsB, combined.subList(cells, 2 * cells));

        // Cross-check the combined flat list verifies as a batch.
        Bytes commitmentA = BlobUtils.getCommitment(blobA);
        Bytes commitmentB = BlobUtils.getCommitment(blobB);
        assertTrue(
                BlobUtils.checkCellProofsValidity(
                        Arrays.asList(blobA, blobB),
                        Arrays.asList(commitmentA, commitmentB),
                        combined),
                "blob-major batch of cell proofs must verify");
    }

    public static String loadResourceAsString(String filePath) throws Exception {
        try (InputStream inputStream =
                BlobUtilsTest.class.getClassLoader().getResourceAsStream(filePath)) {
            assert inputStream != null;
            try (BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    private static UInt256 zeroBLSFieldElement() {
        return UInt256.ZERO;
    }

    private static UInt256 randomBLSFieldElement() {
        final BigInteger attempt = new BigInteger(CKZG4844JNI.BLS_MODULUS.bitLength(), RANDOM);
        if (attempt.compareTo(CKZG4844JNI.BLS_MODULUS) < 0) {
            return UInt256.valueOf(attempt);
        }
        return randomBLSFieldElement();
    }

    private static byte[] flatten(final byte[]... bytes) {
        final int capacity = Arrays.stream(bytes).mapToInt(b -> b.length).sum();
        final ByteBuffer buffer = ByteBuffer.allocate(capacity);
        Arrays.stream(bytes).forEach(buffer::put);
        return buffer.array();
    }

    public static byte[] createRandomBlob() {
        final byte[][] blob =
                range(0, CKZG4844JNI.FIELD_ELEMENTS_PER_BLOB)
                        .mapToObj(__ -> randomBLSFieldElement())
                        .map(fieldElement -> fieldElement.toArray(ByteOrder.BIG_ENDIAN))
                        .toArray(byte[][]::new);
        return flatten(blob);
    }

    public static byte[] createZeroBlob() {
        final byte[][] blob =
                range(0, CKZG4844JNI.FIELD_ELEMENTS_PER_BLOB)
                        .mapToObj(__ -> zeroBLSFieldElement())
                        .map(fieldElement -> fieldElement.toArray(ByteOrder.BIG_ENDIAN))
                        .toArray(byte[][]::new);
        return flatten(blob);
    }
}
