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
package org.web3j.crypto.transaction.type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.tuweni.bytes.Bytes;

import org.web3j.crypto.AccessListObject;
import org.web3j.crypto.Blob;
import org.web3j.crypto.BlobUtils;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

/**
 * EIP-4844 blob transaction (also extended for EIP-7594 / Fusaka PeerDAS).
 *
 * <p>Wire format variants:
 *
 * <ul>
 *   <li>EIP-4844 network wrapper (Cancun/Prague): {@code rlp([tx_payload_body, blobs, commitments,
 *       proofs])}
 *   <li>EIP-7594 network wrapper (Fusaka/Osaka): {@code rlp([tx_payload_body, wrapper_version,
 *       blobs, commitments, cell_proofs])}
 * </ul>
 *
 * <p>EIP-7594 cell_proofs is a <strong>flat</strong> list of {@code CELLS_PER_EXT_BLOB ×
 * len(blobs)} 48-byte KZG proofs. {@code CELLS_PER_EXT_BLOB = 128}
 * (FIELD_ELEMENTS_PER_EXT_BLOB=8192 / FIELD_ELEMENTS_PER_CELL=64).
 */
public class Transaction4844 extends Transaction1559 implements ITransaction {

    /** Number of cells in an extended blob (EIP-7594 / PeerDAS). */
    public static final int CELLS_PER_EXT_BLOB = 128;

    /** Expected byte length of each KZG proof / commitment (G1 compressed point). */
    public static final int KZG_PROOF_BYTE_LENGTH = 48;

    /** Only supported wrapper_version value per EIP-7594. */
    public static final BigInteger WRAPPER_VERSION_1 = BigInteger.ONE;

    /** Maximum number of blobs per transaction (EIP-7594 spec requirement). */
    public static final int MAX_BLOBS_PER_TRANSACTION = 6;

    private final BigInteger maxFeePerBlobGas;
    private final List<Bytes> versionedHashes;
    private final Optional<List<Blob>> blobs;
    private final Optional<List<Bytes>> kzgCommitments;

    // EIP-4844: one proof per blob
    private final Optional<List<Bytes>> kzgProofs;

    // EIP-7594: flat list of CELLS_PER_EXT_BLOB proofs per blob
    private final Optional<List<Bytes>> cellProofs;

    // Present only for EIP-7594 wrappers
    private final Optional<BigInteger> wrapperVersion;

    // -------------------------------------------------------------------------
    // Constructor 1: unsigned tx-only (no sidecar) — used by RPC callers
    // -------------------------------------------------------------------------
    protected Transaction4844(
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas,
            List<Bytes> versionedHashes) {
        super(chainId, nonce, gasLimit, to, value, data, maxPriorityFeePerGas, maxFeePerGas);
        this.maxFeePerBlobGas = Objects.requireNonNull(maxFeePerBlobGas, "maxFeePerBlobGas");
        this.versionedHashes =
                Collections.unmodifiableList(
                        Objects.requireNonNull(versionedHashes, "versionedHashes"));
        this.blobs = Optional.empty();
        this.kzgCommitments = Optional.empty();
        this.kzgProofs = Optional.empty();
        this.wrapperVersion = Optional.empty();
        this.cellProofs = Optional.empty();
    }

    // -------------------------------------------------------------------------
    // Constructor 2: EIP-4844 network wrapper (Cancun/Prague)
    //   blobs + kzgCommitments + kzgProofs (one proof per blob)
    // -------------------------------------------------------------------------
    protected Transaction4844(
            List<Blob> blobs,
            List<Bytes> kzgCommitments,
            List<Bytes> kzgProofs,
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas,
            List<Bytes> versionedHashes) {
        super(chainId, nonce, gasLimit, to, value, data, maxPriorityFeePerGas, maxFeePerGas);
        this.maxFeePerBlobGas = Objects.requireNonNull(maxFeePerBlobGas, "maxFeePerBlobGas");
        this.versionedHashes =
                Collections.unmodifiableList(
                        Objects.requireNonNull(versionedHashes, "versionedHashes"));
        this.blobs = Optional.ofNullable(blobs);
        this.kzgCommitments = Optional.ofNullable(kzgCommitments);
        this.kzgProofs = Optional.ofNullable(kzgProofs);
        this.wrapperVersion = Optional.empty();
        this.cellProofs = Optional.empty();

        validateEip4844Sidecar();
    }

    // -------------------------------------------------------------------------
    // Constructor 3: EIP-7594 network wrapper (Fusaka/Osaka)
    //   blobs + kzgCommitments + cellProofs (flat, 128×N proofs) + wrapperVersion
    // -------------------------------------------------------------------------
    protected Transaction4844(
            List<Blob> blobs,
            List<Bytes> kzgCommitments,
            List<Bytes> cellProofs,
            BigInteger wrapperVersion,
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas,
            List<Bytes> versionedHashes,
            List<AccessListObject> accessList) {
        super(
                chainId,
                nonce,
                gasLimit,
                to,
                value,
                data,
                maxPriorityFeePerGas,
                maxFeePerGas,
                accessList != null ? accessList : Collections.emptyList());
        this.maxFeePerBlobGas = Objects.requireNonNull(maxFeePerBlobGas, "maxFeePerBlobGas");
        this.versionedHashes =
                Collections.unmodifiableList(
                        Objects.requireNonNull(versionedHashes, "versionedHashes"));
        this.blobs = Optional.ofNullable(blobs);
        this.kzgCommitments = Optional.ofNullable(kzgCommitments);
        this.kzgProofs = Optional.empty(); // EIP-7594 does not use per-blob kzgProofs
        this.wrapperVersion = Optional.ofNullable(wrapperVersion);
        this.cellProofs = Optional.ofNullable(cellProofs);

        validateEip7594Sidecar();
    }

    // -------------------------------------------------------------------------
    // Constructor 4: auto-compute commitments + proofs from raw blobs (EIP-4844)
    // -------------------------------------------------------------------------
    protected Transaction4844(
            List<Blob> blobsData,
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas) {
        super(chainId, nonce, gasLimit, to, value, data, maxPriorityFeePerGas, maxFeePerGas);
        this.maxFeePerBlobGas = Objects.requireNonNull(maxFeePerBlobGas, "maxFeePerBlobGas");

        if (blobsData == null) {
            throw new IllegalArgumentException("blobsData must not be null");
        }

        this.blobs = Optional.of(blobsData);

        List<Bytes> commitments =
                blobsData.stream().map(BlobUtils::getCommitment).collect(Collectors.toList());
        this.kzgCommitments = Optional.of(commitments);

        this.kzgProofs =
                Optional.of(
                        IntStream.range(0, blobsData.size())
                                .mapToObj(
                                        i ->
                                                BlobUtils.getProof(
                                                        blobsData.get(i), commitments.get(i)))
                                .collect(Collectors.toList()));

        this.versionedHashes =
                commitments.stream()
                        .map(BlobUtils::kzgToVersionedHash)
                        .collect(Collectors.toList());

        this.wrapperVersion = Optional.empty();
        this.cellProofs = Optional.empty();
    }

    // =========================================================================
    // Validation helpers
    // =========================================================================

    private void validateEip4844Sidecar() {
        if (!blobs.isPresent() || !kzgCommitments.isPresent() || !kzgProofs.isPresent()) {
            return; // partial construction allowed (some fields may be absent)
        }
        int blobCount = blobs.get().size();
        if (kzgCommitments.get().size() != blobCount) {
            throw new IllegalArgumentException(
                    "EIP-4844: kzgCommitments count ("
                            + kzgCommitments.get().size()
                            + ") must equal blob count ("
                            + blobCount
                            + ")");
        }
        if (kzgProofs.get().size() != blobCount) {
            throw new IllegalArgumentException(
                    "EIP-4844: kzgProofs count ("
                            + kzgProofs.get().size()
                            + ") must equal blob count ("
                            + blobCount
                            + ")");
        }
        validateProofByteLengths(kzgProofs.get(), "kzgProofs");
        validateCommitmentByteLengths(kzgCommitments.get());
    }

    private void validateEip7594Sidecar() {
        // wrapper_version must be 1
        if (wrapperVersion.isPresent() && !WRAPPER_VERSION_1.equals(wrapperVersion.get())) {
            throw new IllegalArgumentException(
                    "EIP-7594: unsupported wrapper_version "
                            + wrapperVersion.get()
                            + ". Only version 1 is supported.");
        }

        if (!blobs.isPresent() || !kzgCommitments.isPresent() || !cellProofs.isPresent()) {
            return;
        }

        int blobCount = blobs.get().size();
        validateBlobCount(blobCount);

        if (kzgCommitments.get().size() != blobCount) {
            throw new IllegalArgumentException(
                    "EIP-7594: kzgCommitments count ("
                            + kzgCommitments.get().size()
                            + ") must equal blob count ("
                            + blobCount
                            + ")");
        }

        // cell_proofs must be a flat list of exactly CELLS_PER_EXT_BLOB * blobCount entries
        int expectedProofCount = CELLS_PER_EXT_BLOB * blobCount;
        if (cellProofs.get().size() != expectedProofCount) {
            throw new IllegalArgumentException(
                    "EIP-7594: cell_proofs count ("
                            + cellProofs.get().size()
                            + ") must equal CELLS_PER_EXT_BLOB * blobCount = "
                            + expectedProofCount);
        }

        validateProofByteLengths(cellProofs.get(), "cellProofs");
        validateCommitmentByteLengths(kzgCommitments.get());
    }

    private static void validateBlobCount(int blobCount) {
        if (blobCount > MAX_BLOBS_PER_TRANSACTION) {
            throw new IllegalArgumentException(
                    "Blob count ("
                            + blobCount
                            + ") exceeds MAX_BLOBS_PER_TRANSACTION ("
                            + MAX_BLOBS_PER_TRANSACTION
                            + ")");
        }
    }

    private static void validateProofByteLengths(List<Bytes> proofs, String fieldName) {
        for (int i = 0; i < proofs.size(); i++) {
            if (proofs.get(i).size() != KZG_PROOF_BYTE_LENGTH) {
                throw new IllegalArgumentException(
                        fieldName
                                + "["
                                + i
                                + "] must be "
                                + KZG_PROOF_BYTE_LENGTH
                                + " bytes (G1 compressed point), got "
                                + proofs.get(i).size());
            }
        }
    }

    private static void validateCommitmentByteLengths(List<Bytes> commitments) {
        for (int i = 0; i < commitments.size(); i++) {
            if (commitments.get(i).size() != KZG_PROOF_BYTE_LENGTH) {
                throw new IllegalArgumentException(
                        "kzgCommitments["
                                + i
                                + "] must be "
                                + KZG_PROOF_BYTE_LENGTH
                                + " bytes (G1 compressed point), got "
                                + commitments.get(i).size());
            }
        }
    }

    // =========================================================================
    // RLP serialization
    //
    // asRlpValues(signatureData) — produces the SIGNING payload (tx_payload_body).
    //   - signatureData == null  →  unsigned fields for hash-signing
    //   - signatureData != null  →  signed fields (with v, r, s) for tx_payload_body
    //
    // asNetworkRlpValues(signatureData) — produces the full NETWORK WRAPPER for P2P gossip:
    //   EIP-4844: rlp([tx_payload_body, blobs, commitments, proofs])
    //   EIP-7594: rlp([tx_payload_body, wrapper_version, blobs, commitments, cell_proofs])
    //
    // TransactionEncoder.signMessage() calls:
    //   1. encode4844(tx)          → asRlpValues(null)         → hash-signing payload
    //   2. encode(tx, sigData)     → asRlpValues(sigData)      → full signed network wrapper
    //       (which internally delegates to asNetworkRlpValues when sidecar present)
    // =========================================================================

    /**
     * Returns the RLP field list for the transaction payload body only (tx_payload_body). When
     * signatureData is not null, includes v/r/s and then delegates to {@link
     * #buildNetworkWrapper(Sign.SignatureData)} to append the blob sidecar.
     */
    @Override
    public List<RlpType> asRlpValues(Sign.SignatureData signatureData) {
        List<RlpType> txPayloadBody = buildTxPayloadBody(signatureData);

        if (signatureData != null && hasAnySidecar()) {
            // Produce the full network wrapper as a flat list so TransactionEncoder
            // wraps it in a single RlpList → 0x03 || rlp([...])
            return buildNetworkWrapper(signatureData);
        }

        return txPayloadBody;
    }

    /**
     * Builds tx_payload_body: the 11 unsigned fields (+ optional v/r/s when signing). This is also
     * the hash-signing input when signatureData is null.
     */
    private List<RlpType> buildTxPayloadBody(Sign.SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();

        result.add(RlpString.create(getChainId()));
        result.add(RlpString.create(getNonce()));
        result.add(RlpString.create(getMaxPriorityFeePerGas()));
        result.add(RlpString.create(getMaxFeePerGas()));
        result.add(RlpString.create(getGasLimit()));

        String to = getTo();
        if (to != null && to.length() > 0) {
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(getValue()));
        result.add(RlpString.create(Numeric.hexStringToByteArray(getData())));

        // access list (field index 8)
        result.add(new RlpList(rlpAccessListRlp()));

        // max_fee_per_blob_gas (field index 9)
        result.add(RlpString.create(getMaxFeePerBlobGas()));
        // blob_versioned_hashes (field index 10)
        result.add(new RlpList(getRlpVersionedHashes()));

        if (signatureData != null) {
            result.add(RlpString.create(Sign.getRecId(signatureData, getChainId())));
            result.add(
                    RlpString.create(
                            org.web3j.utils.Bytes.trimLeadingZeroes(signatureData.getR())));
            result.add(
                    RlpString.create(
                            org.web3j.utils.Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }

    /**
     * Builds the full network wrapper list (for P2P gossip / pooled transactions).
     *
     * <p>EIP-4844: {@code [tx_payload_body, blobs, commitments, proofs]} <br>
     * EIP-7594: {@code [tx_payload_body, wrapper_version, blobs, commitments, cell_proofs]}
     *
     * <p>The returned list is the direct content of the top-level RlpList, so TransactionEncoder
     * will produce: {@code 0x03 || rlp([...])}
     */
    private List<RlpType> buildNetworkWrapper(Sign.SignatureData signatureData) {
        List<RlpType> wrapper = new ArrayList<>();
        // Element 0: signed tx_payload_body
        wrapper.add(new RlpList(buildTxPayloadBody(signatureData)));

        if (wrapperVersion.isPresent()) {
            // EIP-7594 format
            wrapper.add(RlpString.create(wrapperVersion.get())); // wrapper_version
            wrapper.add(new RlpList(getRlpBlobs())); // blobs
            wrapper.add(new RlpList(getRlpKzgCommitments())); // commitments
            wrapper.add(new RlpList(getRlpCellProofs())); // cell_proofs (flat)
        } else {
            // EIP-4844 format
            wrapper.add(new RlpList(getRlpBlobs()));
            wrapper.add(new RlpList(getRlpKzgCommitments()));
            wrapper.add(new RlpList(getRlpKzgProofs()));
        }

        return wrapper;
    }

    /** Returns true if this transaction has any blob sidecar attached. */
    private boolean hasAnySidecar() {
        return blobs.isPresent()
                || kzgCommitments.isPresent()
                || kzgProofs.isPresent()
                || cellProofs.isPresent();
    }

    // =========================================================================
    // Factory methods
    // =========================================================================

    /**
     * Create an EIP-4844 transaction with an explicit blob sidecar.
     *
     * @param blobs list of blobs (one per versioned hash)
     * @param kzgCommitments KZG commitments matching blobs
     * @param kzgProofs one KZG proof per blob
     * @param versionedHashes blob versioned hashes derived from commitments
     */
    public static Transaction4844 createTransaction(
            List<Blob> blobs,
            List<Bytes> kzgCommitments,
            List<Bytes> kzgProofs,
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas,
            List<Bytes> versionedHashes) {
        return new Transaction4844(
                blobs,
                kzgCommitments,
                kzgProofs,
                chainId,
                nonce,
                maxPriorityFeePerGas,
                maxFeePerGas,
                gasLimit,
                to,
                value,
                data,
                maxFeePerBlobGas,
                versionedHashes);
    }

    /**
     * Create an EIP-7594 (Osaka/Fusaka) transaction with PeerDAS cell proofs.
     *
     * @param blobs list of blobs
     * @param kzgCommitments KZG commitments matching blobs (one per blob)
     * @param cellProofs flat list of cell proofs: {@code CELLS_PER_EXT_BLOB * len(blobs)} 48-byte
     *     proofs in blob-major order
     * @param wrapperVersion must be {@link #WRAPPER_VERSION_1} (BigInteger.ONE)
     * @param versionedHashes blob versioned hashes derived from commitments
     */
    public static Transaction4844 createTransaction(
            List<Blob> blobs,
            List<Bytes> kzgCommitments,
            List<Bytes> cellProofs,
            BigInteger wrapperVersion,
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas,
            List<Bytes> versionedHashes) {
        return createTransaction(
                blobs,
                kzgCommitments,
                cellProofs,
                wrapperVersion,
                chainId,
                nonce,
                maxPriorityFeePerGas,
                maxFeePerGas,
                gasLimit,
                to,
                value,
                data,
                maxFeePerBlobGas,
                versionedHashes,
                Collections.emptyList());
    }

    public static Transaction4844 createTransaction(
            List<Blob> blobs,
            List<Bytes> kzgCommitments,
            List<Bytes> cellProofs,
            BigInteger wrapperVersion,
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas,
            List<Bytes> versionedHashes,
            List<AccessListObject> accessList) {
        return new Transaction4844(
                blobs,
                kzgCommitments,
                cellProofs,
                wrapperVersion,
                chainId,
                nonce,
                maxPriorityFeePerGas,
                maxFeePerGas,
                gasLimit,
                to,
                value,
                data,
                maxFeePerBlobGas,
                versionedHashes,
                accessList);
    }

    /** Create a blob transaction with auto-computed commitments/proofs. */
    public static Transaction4844 createTransaction(
            List<Blob> blobs,
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas) {
        return new Transaction4844(
                blobs,
                chainId,
                nonce,
                maxPriorityFeePerGas,
                maxFeePerGas,
                gasLimit,
                to,
                value,
                data,
                maxFeePerBlobGas);
    }

    /** Create a tx-only EIP-4844 transaction (no sidecar, versioned hashes only). */
    public static Transaction4844 createTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxFeePerBlobGas,
            List<Bytes> versionedHashes) {
        return new Transaction4844(
                chainId,
                nonce,
                maxPriorityFeePerGas,
                maxFeePerGas,
                gasLimit,
                to,
                value,
                data,
                maxFeePerBlobGas,
                versionedHashes);
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public BigInteger getMaxFeePerBlobGas() {
        return maxFeePerBlobGas;
    }

    public List<Bytes> getVersionedHashes() {
        return versionedHashes;
    }

    public Optional<List<Blob>> getBlobs() {
        return blobs;
    }

    public Optional<List<Bytes>> getKzgCommitments() {
        return kzgCommitments;
    }

    public Optional<List<Bytes>> getKzgProofs() {
        return kzgProofs;
    }

    public Optional<BigInteger> getWrapperVersion() {
        return wrapperVersion;
    }

    /**
     * Returns the flat cell_proofs list for EIP-7594 transactions. Size = {@code CELLS_PER_EXT_BLOB
     * * len(blobs)} when present.
     */
    public Optional<List<Bytes>> getCellProofs() {
        return cellProofs;
    }

    // =========================================================================
    // RLP helpers
    // =========================================================================

    public List<RlpType> getRlpVersionedHashes() {
        return versionedHashes.stream()
                .map(hash -> RlpString.create(hash.toArray()))
                .collect(Collectors.toList());
    }

    public List<RlpType> getRlpKzgCommitments() {
        return kzgCommitments
                .<List<RlpType>>map(
                        bytesList ->
                                bytesList.stream()
                                        .map(bytes -> RlpString.create(bytes.toArray()))
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public List<RlpType> getRlpKzgProofs() {
        return kzgProofs
                .<List<RlpType>>map(
                        bytesList ->
                                bytesList.stream()
                                        .map(bytes -> RlpString.create(bytes.toArray()))
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public List<RlpType> getRlpBlobs() {
        return blobs.<List<RlpType>>map(
                        blobList ->
                                blobList.stream()
                                        .map(blob -> RlpString.create(blob.getData().toArray()))
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Returns the RLP encoding of cell_proofs as a flat list of RlpStrings. Each element is one
     * 48-byte proof. EIP-7594 spec: {@code cell_proofs} is a flat list, NOT a list of lists.
     */
    public List<RlpType> getRlpCellProofs() {
        return cellProofs
                .<List<RlpType>>map(
                        proofList ->
                                proofList.stream()
                                        .map(bytes -> RlpString.create(bytes.toArray()))
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EIP4844;
    }
}
