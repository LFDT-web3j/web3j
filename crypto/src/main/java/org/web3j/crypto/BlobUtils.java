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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ethereum.ckzg4844.CKZG4844JNI;
import ethereum.ckzg4844.CellsAndProofs;
import org.apache.tuweni.bytes.Bytes;

public class BlobUtils {

    private static final byte blobCommitmentVersionKZG = 0x01;
    private static final String trustedSetupFilePath = "/trusted_setup.txt";

    static {
        CKZG4844JNI.loadNativeLibrary();
        loadTrustedSetupParameters();
    }

    private static void loadTrustedSetupParameters() {
        CKZG4844JNI.loadTrustedSetupFromResource(
                BlobUtils.trustedSetupFilePath, BlobUtils.class, 0);
    }

    public static Bytes getCommitment(Blob blobData) {
        return Bytes.wrap(CKZG4844JNI.blobToKzgCommitment(blobData.data.toArray()));
    }

    public static Bytes getProof(Blob blobData, Bytes commitment) {
        return Bytes.wrap(
                CKZG4844JNI.computeBlobKzgProof(blobData.data.toArray(), commitment.toArray()));
    }

    public static boolean checkProofValidity(Blob blobData, Bytes commitment, Bytes proof) {
        return CKZG4844JNI.verifyBlobKzgProof(
                blobData.data.toArray(), commitment.toArray(), proof.toArray());
    }

    /**
     * Computes the EIP-7594 (PeerDAS) cell proofs for a single blob: a flat list of {@code
     * CELLS_PER_EXT_BLOB} (128) 48-byte KZG proofs, one per cell of the extended blob.
     *
     * @param blobData the blob to compute cell proofs for
     * @return the 128 cell proofs for this blob, in cell-index order
     */
    public static List<Bytes> getCellProofs(Blob blobData) {
        CellsAndProofs cellsAndProofs =
                CKZG4844JNI.computeCellsAndKzgProofs(blobData.data.toArray());
        return splitFlatProofs(cellsAndProofs.getProofs());
    }

    /**
     * Computes the EIP-7594 cell proofs for multiple blobs as a single flat list in
     * <strong>blob-major order</strong>: {@code [blob0.proof0..127, blob1.proof0..127, ...]}, total
     * {@code CELLS_PER_EXT_BLOB * blobs.size()} proofs. This is the exact layout the EIP-7594
     * network wrapper expects for its flat {@code cell_proofs} field.
     *
     * @param blobs the blobs to compute cell proofs for
     * @return the flat, blob-major list of cell proofs
     */
    public static List<Bytes> getCellProofs(List<Blob> blobs) {
        List<Bytes> all = new ArrayList<>(CKZG4844JNI.CELLS_PER_EXT_BLOB * blobs.size());
        for (Blob blob : blobs) {
            all.addAll(getCellProofs(blob));
        }
        return all;
    }

    /**
     * Verifies a flat, blob-major list of EIP-7594 cell proofs against the blobs and their
     * commitments using {@link CKZG4844JNI#verifyCellKzgProofBatch}. The cells are recomputed from
     * the blobs.
     *
     * @param blobs the blobs
     * @param commitments one 48-byte KZG commitment per blob
     * @param cellProofs the flat {@code CELLS_PER_EXT_BLOB * blobs.size()} cell proofs in
     *     blob-major order (as produced by {@link #getCellProofs(List)})
     * @return true if every cell proof verifies
     */
    public static boolean checkCellProofsValidity(
            List<Blob> blobs, List<Bytes> commitments, List<Bytes> cellProofs) {
        int cellsPerBlob = CKZG4844JNI.CELLS_PER_EXT_BLOB;
        int totalCells = cellsPerBlob * blobs.size();

        long[] cellIndices = new long[totalCells];
        byte[] commitmentsBytes = new byte[totalCells * CKZG4844JNI.BYTES_PER_COMMITMENT];
        byte[] cellsBytes = new byte[totalCells * CKZG4844JNI.BYTES_PER_CELL];
        byte[] proofsBytes = new byte[totalCells * CKZG4844JNI.BYTES_PER_PROOF];

        for (int b = 0; b < blobs.size(); b++) {
            CellsAndProofs cellsAndProofs =
                    CKZG4844JNI.computeCellsAndKzgProofs(blobs.get(b).data.toArray());
            System.arraycopy(
                    cellsAndProofs.getCells(),
                    0,
                    cellsBytes,
                    b * cellsPerBlob * CKZG4844JNI.BYTES_PER_CELL,
                    cellsPerBlob * CKZG4844JNI.BYTES_PER_CELL);

            byte[] commitment = commitments.get(b).toArray();
            for (int i = 0; i < cellsPerBlob; i++) {
                int cellGlobal = b * cellsPerBlob + i;
                cellIndices[cellGlobal] = i;
                System.arraycopy(
                        commitment,
                        0,
                        commitmentsBytes,
                        cellGlobal * CKZG4844JNI.BYTES_PER_COMMITMENT,
                        CKZG4844JNI.BYTES_PER_COMMITMENT);
                System.arraycopy(
                        cellProofs.get(cellGlobal).toArray(),
                        0,
                        proofsBytes,
                        cellGlobal * CKZG4844JNI.BYTES_PER_PROOF,
                        CKZG4844JNI.BYTES_PER_PROOF);
            }
        }

        return CKZG4844JNI.verifyCellKzgProofBatch(
                commitmentsBytes, cellIndices, cellsBytes, proofsBytes);
    }

    /** Splits the flat per-blob proofs byte array into {@code CELLS_PER_EXT_BLOB} equal chunks. */
    private static List<Bytes> splitFlatProofs(byte[] flatProofs) {
        int cells = CKZG4844JNI.CELLS_PER_EXT_BLOB;
        int chunk = flatProofs.length / cells; // == BYTES_PER_PROOF (48)
        List<Bytes> proofs = new ArrayList<>(cells);
        for (int i = 0; i < cells; i++) {
            proofs.add(Bytes.wrap(Arrays.copyOfRange(flatProofs, i * chunk, (i + 1) * chunk)));
        }
        return proofs;
    }

    public static Bytes kzgToVersionedHash(Bytes commitment) {
        byte[] hash = Hash.sha256(commitment.toArray());
        hash[0] = blobCommitmentVersionKZG;
        return Bytes.wrap(hash);
    }
}
