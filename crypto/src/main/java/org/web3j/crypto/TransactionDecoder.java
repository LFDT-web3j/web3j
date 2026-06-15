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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;

import org.web3j.crypto.transaction.type.TransactionType;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import static java.util.stream.Collectors.toList;

public class TransactionDecoder {
    private static final int UNSIGNED_EIP1559TX_RLP_LIST_SIZE = 9;
    private static final int UNSIGNED_EIP2930TX_RLP_LIST_SIZE = 8;
    private static final int UNSIGNED_EIP4844TX_RLP_LIST_SIZE = 11;
    private static final int UNSIGNED_EIP7702TX_RLP_LIST_SIZE = 10;

    public static RawTransaction decode(final String hexTransaction) {
        final byte[] transaction = Numeric.hexStringToByteArray(hexTransaction);
        TransactionType transactionType = getTransactionType(transaction);

        switch (transactionType) {
            case EIP1559:
                return decodeEIP1559Transaction(transaction);
            case EIP4844:
                return decodeEIP4844Transaction(transaction);
            case EIP2930:
                return decodeEIP2930Transaction(transaction);
            case EIP7702:
                return decodeEIP7702Transaction(transaction);
            default:
                return decodeLegacyTransaction(transaction);
        }
    }

    private static RawTransaction decodeEIP7702Transaction(final byte[] transaction) {
        // Strip off the type byte (0x04) before decoding the RLP list
        final byte[] encodedTx = Arrays.copyOfRange(transaction, 1, transaction.length);
        final RlpList rlpList = RlpDecoder.decode(encodedTx);
        final RlpList values = (RlpList) rlpList.getValues().get(0);
        final List<RlpType> fields = values.getValues();

        final long chainId = ((RlpString) fields.get(0)).asPositiveBigInteger().longValue();
        final BigInteger nonce = ((RlpString) fields.get(1)).asPositiveBigInteger();
        final BigInteger maxPriorityFeePerGas = ((RlpString) fields.get(2)).asPositiveBigInteger();
        final BigInteger maxFeePerGas = ((RlpString) fields.get(3)).asPositiveBigInteger();
        final BigInteger gasLimit = ((RlpString) fields.get(4)).asPositiveBigInteger();
        final String to = ((RlpString) fields.get(5)).asString();
        final BigInteger value = ((RlpString) fields.get(6)).asPositiveBigInteger();
        final String data = ((RlpString) fields.get(7)).asString();
        final List<RlpType> accessListRlp = ((RlpList) fields.get(8)).getValues();
        final List<AccessListObject> accessList = decodeAccessList(accessListRlp);
        final List<AuthorizationTuple> authorizationList =
                decodeAuthorizationList(((RlpList) fields.get(9)).getValues());
        // INV: Per the EIP, authorization list should be nonempty. We don't
        // enforce that here.

        final RawTransaction rawTransaction =
                RawTransaction.createTransaction(
                        chainId,
                        nonce,
                        maxPriorityFeePerGas,
                        maxFeePerGas,
                        gasLimit,
                        to,
                        value,
                        data,
                        accessList,
                        authorizationList);

        if (fields.size() == UNSIGNED_EIP7702TX_RLP_LIST_SIZE) {
            return rawTransaction;
        } else {
            final int yParity =
                    Numeric.toBigInt(((RlpString) fields.get(10)).getBytes()).intValue();
            final byte[] rBytes =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) fields.get(11)).getBytes()), 32);
            final byte[] sBytes =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) fields.get(12)).getBytes()), 32);

            final byte[] vBytes = Sign.getVFromRecId(yParity);

            final Sign.SignatureData signatureData = new Sign.SignatureData(vBytes, rBytes, sBytes);

            return new SignedRawTransaction(rawTransaction.getTransaction(), signatureData);
        }
    }

    private static TransactionType getTransactionType(final byte[] transaction) {
        // The first byte indicates a transaction type.
        byte firstByte = transaction[0];
        if (firstByte == TransactionType.EIP1559.getRlpType()) return TransactionType.EIP1559;
        else if (firstByte == TransactionType.EIP4844.getRlpType()) return TransactionType.EIP4844;
        else if (firstByte == TransactionType.EIP2930.getRlpType()) return TransactionType.EIP2930;
        else if (firstByte == TransactionType.EIP7702.getRlpType()) return TransactionType.EIP7702;
        else return TransactionType.LEGACY;
    }

    private static RawTransaction decodeEIP4844Transaction(final byte[] transaction) {
        final byte[] encodedTx = Arrays.copyOfRange(transaction, 1, transaction.length);
        final RlpList rlpList = RlpDecoder.decode(encodedTx);
        final RlpList outerList = (RlpList) rlpList.getValues().get(0);

        final RlpList txPayload;
        final List<Blob> blobs;
        final List<Bytes> kzgCommitments;
        final List<Bytes> kzgProofs;
        final BigInteger wrapperVersion;
        // EIP-7594: flat list of CELLS_PER_EXT_BLOB * len(blobs) proofs
        final List<Bytes> cellProofs;
        final List<AccessListObject> accessList;

        final List<RlpType> outerValues = outerList.getValues();
        if (outerValues.isEmpty()) {
            throw new IllegalArgumentException(
                    "Malformed EIP-4844 transaction: outer list is empty");
        }

        // Dispatch: network wrapper if first element is a list (tx_payload_body),
        // otherwise the entire outerList IS the tx_payload_body (no sidecar).
        if (outerValues.get(0) instanceof RlpList) {
            int size = outerValues.size();
            if (size == 5) {
                // EIP-7594 format: [tx_payload_body, wrapper_version, blobs, commitments,
                // cell_proofs]
                txPayload = (RlpList) outerValues.get(0);

                if (!(outerValues.get(1) instanceof RlpString)) {
                    throw new IllegalArgumentException(
                            "Malformed EIP-7594 transaction: wrapper_version must be RlpString");
                }
                wrapperVersion = ((RlpString) outerValues.get(1)).asPositiveBigInteger();

                if (!(outerValues.get(2) instanceof RlpList)) {
                    throw new IllegalArgumentException(
                            "Malformed EIP-7594 transaction: blobs must be RlpList");
                }
                blobs = decodeBlobs(((RlpList) outerValues.get(2)).getValues());

                if (!(outerValues.get(3) instanceof RlpList)) {
                    throw new IllegalArgumentException(
                            "Malformed EIP-7594 transaction: commitments must be RlpList");
                }
                kzgCommitments = decodeBytesList(((RlpList) outerValues.get(3)).getValues());

                if (!(outerValues.get(4) instanceof RlpList)) {
                    throw new IllegalArgumentException(
                            "Malformed EIP-7594 transaction: cell_proofs must be RlpList");
                }
                // cell_proofs is a flat list of 48-byte proof entries
                cellProofs = decodeBytesList(((RlpList) outerValues.get(4)).getValues());
                kzgProofs = null;
            } else if (size == 4) {
                // EIP-4844 format: [tx_payload_body, blobs, commitments, proofs]
                txPayload = (RlpList) outerValues.get(0);
                blobs = decodeBlobs(((RlpList) outerValues.get(1)).getValues());
                kzgCommitments = decodeBytesList(((RlpList) outerValues.get(2)).getValues());
                kzgProofs = decodeBytesList(((RlpList) outerValues.get(3)).getValues());
                wrapperVersion = null;
                cellProofs = null;
            } else {
                throw new IllegalArgumentException(
                        "Malformed EIP-4844 network wrapper: expected 4 (EIP-4844) or 5 "
                                + "(EIP-7594) elements, got "
                                + size);
            }

            // Decode access list from inside tx_payload_body (field index 8)
            final List<RlpType> tvForAL = txPayload.getValues();
            if (tvForAL.size() <= 8 || !(tvForAL.get(8) instanceof RlpList)) {
                throw new IllegalArgumentException(
                        "Malformed EIP-4844 tx_payload_body: missing access list at index 8");
            }
            accessList = decodeAccessList(((RlpList) tvForAL.get(8)).getValues());
        } else {
            // No sidecar — outerList is the tx_payload_body itself
            txPayload = outerList;
            blobs = null;
            kzgCommitments = null;
            kzgProofs = null;
            wrapperVersion = null;
            cellProofs = null;
            final List<RlpType> tvForAL = txPayload.getValues();
            if (tvForAL.size() > 8 && tvForAL.get(8) instanceof RlpList) {
                accessList = decodeAccessList(((RlpList) tvForAL.get(8)).getValues());
            } else {
                accessList = Collections.emptyList();
            }
        }

        // Decode the transaction payload fields
        final List<RlpType> txValues = txPayload.getValues();
        if (txValues.size() < 11) {
            throw new IllegalArgumentException(
                    "Malformed EIP-4844 tx_payload_body: expected at least 11 fields, got "
                            + txValues.size());
        }

        final long chainId = ((RlpString) txValues.get(0)).asPositiveBigInteger().longValue();
        final BigInteger nonce = ((RlpString) txValues.get(1)).asPositiveBigInteger();
        final BigInteger maxPriorityFeePerGas =
                ((RlpString) txValues.get(2)).asPositiveBigInteger();
        final BigInteger maxFeePerGas = ((RlpString) txValues.get(3)).asPositiveBigInteger();
        final BigInteger gasLimit = ((RlpString) txValues.get(4)).asPositiveBigInteger();
        final String to = ((RlpString) txValues.get(5)).asString();
        final BigInteger value = ((RlpString) txValues.get(6)).asPositiveBigInteger();
        final String data = ((RlpString) txValues.get(7)).asString();
        // index 8 = access list (already decoded above)
        final BigInteger maxFeePerBlobGas = ((RlpString) txValues.get(9)).asPositiveBigInteger();
        final List<Bytes> versionedHashes =
                decodeVersionedHashes(((RlpList) txValues.get(10)).getValues());

        // Reconstruct the RawTransaction object
        final RawTransaction rawTransaction;
        if (wrapperVersion != null) {
            // EIP-7594 path
            rawTransaction =
                    RawTransaction.createTransaction(
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
        } else {
            // EIP-4844 path (with or without sidecar)
            rawTransaction =
                    RawTransaction.createTransaction(
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
                            versionedHashes,
                            accessList);
        }

        // Handle signature if present (fields 11, 12, 13 = y_parity, r, s)
        if (txValues.size() > UNSIGNED_EIP4844TX_RLP_LIST_SIZE) {
            final byte[] v =
                    Sign.getVFromRecId(
                            Numeric.toBigInt(((RlpString) txValues.get(11)).getBytes()).intValue());
            final byte[] r =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) txValues.get(12)).getBytes()), 32);
            final byte[] s =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) txValues.get(13)).getBytes()), 32);
            final Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
            return new SignedRawTransaction(rawTransaction.getTransaction(), signatureData);
        }

        return rawTransaction;
    }

    private static List<Blob> decodeBlobs(List<RlpType> rlpBlobs) {
        return rlpBlobs.stream()
                .map(
                        r -> {
                            if (!(r instanceof RlpString)) {
                                throw new IllegalArgumentException(
                                        "Malformed blob entry: expected RlpString");
                            }
                            return new Blob(((RlpString) r).getBytes());
                        })
                .collect(Collectors.toList());
    }

    // Decoding logic for commitments, proofs, and EIP-7594 flat cell_proofs
    private static List<Bytes> decodeBytesList(List<RlpType> rlpBytesList) {
        return rlpBytesList.stream()
                .map(
                        r -> {
                            if (!(r instanceof RlpString)) {
                                throw new IllegalArgumentException(
                                        "Malformed bytes list: expected RlpString, got "
                                                + r.getClass().getSimpleName());
                            }
                            return Bytes.wrap(((RlpString) r).getBytes());
                        })
                .collect(Collectors.toList());
    }

    private static RawTransaction decodeEIP1559Transaction(final byte[] transaction) {
        final byte[] encodedTx = Arrays.copyOfRange(transaction, 1, transaction.length);
        final RlpList rlpList = RlpDecoder.decode(encodedTx);
        final RlpList values = (RlpList) rlpList.getValues().get(0);

        final long chainId =
                ((RlpString) values.getValues().get(0)).asPositiveBigInteger().longValue();
        final BigInteger nonce = ((RlpString) values.getValues().get(1)).asPositiveBigInteger();
        final BigInteger maxPriorityFeePerGas =
                ((RlpString) values.getValues().get(2)).asPositiveBigInteger();
        final BigInteger maxFeePerGas =
                ((RlpString) values.getValues().get(3)).asPositiveBigInteger();
        final BigInteger gasLimit = ((RlpString) values.getValues().get(4)).asPositiveBigInteger();
        final String to = ((RlpString) values.getValues().get(5)).asString();

        final BigInteger value = ((RlpString) values.getValues().get(6)).asPositiveBigInteger();
        final String data = ((RlpString) values.getValues().get(7)).asString();
        List<AccessListObject> accessList =
                decodeAccessList(((RlpList) values.getValues().get(8)).getValues());

        final RawTransaction rawTransaction =
                RawTransaction.createTransaction(
                        chainId,
                        nonce,
                        gasLimit,
                        to,
                        value,
                        data,
                        maxPriorityFeePerGas,
                        maxFeePerGas,
                        accessList);

        if (values.getValues().size() == UNSIGNED_EIP1559TX_RLP_LIST_SIZE) {
            return rawTransaction;
        } else {
            final byte[] v =
                    Sign.getVFromRecId(
                            Numeric.toBigInt(((RlpString) values.getValues().get(9)).getBytes())
                                    .intValue());
            final byte[] r =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(10)).getBytes()),
                            32);
            final byte[] s =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(11)).getBytes()),
                            32);
            final Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
            return new SignedRawTransaction(rawTransaction.getTransaction(), signatureData);
        }
    }

    private static RawTransaction decodeLegacyTransaction(final byte[] transaction) {
        final RlpList rlpList = RlpDecoder.decode(transaction);
        final RlpList values = (RlpList) rlpList.getValues().get(0);
        final BigInteger nonce = ((RlpString) values.getValues().get(0)).asPositiveBigInteger();
        final BigInteger gasPrice = ((RlpString) values.getValues().get(1)).asPositiveBigInteger();
        final BigInteger gasLimit = ((RlpString) values.getValues().get(2)).asPositiveBigInteger();
        final String to = ((RlpString) values.getValues().get(3)).asString();
        final BigInteger value = ((RlpString) values.getValues().get(4)).asPositiveBigInteger();
        final String data = ((RlpString) values.getValues().get(5)).asString();
        if (values.getValues().size() == 6
                || (values.getValues().size() == 8
                        && ((RlpString) values.getValues().get(7)).getBytes().length == 10)
                || (values.getValues().size() == 9
                        && ((RlpString) values.getValues().get(8)).getBytes().length == 10)) {
            // the 8th or 9nth element is the hex
            // representation of "restricted" for private transactions
            return RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
        } else {
            final byte[] v = ((RlpString) values.getValues().get(6)).getBytes();
            final byte[] r =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(7)).getBytes()),
                            32);
            final byte[] s =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(8)).getBytes()),
                            32);
            final Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
            return new SignedRawTransaction(
                    nonce, gasPrice, gasLimit, to, value, data, signatureData);
        }
    }

    private static RawTransaction decodeEIP2930Transaction(final byte[] transaction) {
        final byte[] encodedTx = Arrays.copyOfRange(transaction, 1, transaction.length);
        final RlpList rlpList = RlpDecoder.decode(encodedTx);
        final RlpList values = (RlpList) rlpList.getValues().get(0);

        final long chainId =
                ((RlpString) values.getValues().get(0)).asPositiveBigInteger().longValue();
        final BigInteger nonce = ((RlpString) values.getValues().get(1)).asPositiveBigInteger();
        final BigInteger gasPrice = ((RlpString) values.getValues().get(2)).asPositiveBigInteger();
        final BigInteger gasLimit = ((RlpString) values.getValues().get(3)).asPositiveBigInteger();
        final String to = ((RlpString) values.getValues().get(4)).asString();
        final BigInteger value = ((RlpString) values.getValues().get(5)).asPositiveBigInteger();
        final String data = ((RlpString) values.getValues().get(6)).asString();
        List<AccessListObject> accessList =
                decodeAccessList(((RlpList) values.getValues().get(7)).getValues());

        final RawTransaction rawTransaction =
                RawTransaction.createTransaction(
                        chainId, nonce, gasPrice, gasLimit, to, value, data, accessList);

        if (values.getValues().size() == UNSIGNED_EIP2930TX_RLP_LIST_SIZE) {
            return rawTransaction;
        } else {
            final byte[] v =
                    Sign.getVFromRecId(
                            Numeric.toBigInt(((RlpString) values.getValues().get(8)).getBytes())
                                    .intValue());
            final byte[] r =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(9)).getBytes()),
                            32);
            final byte[] s =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(10)).getBytes()),
                            32);
            final Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
            return new SignedRawTransaction(rawTransaction.getTransaction(), signatureData);
        }
    }

    private static List<AccessListObject> decodeAccessList(List<RlpType> rlp) {
        return rlp.stream()
                .map(rawEntry -> ((RlpList) rawEntry).getValues())
                .map(
                        values -> {
                            return new AccessListObject(
                                    ((RlpString) values.get(0)).asString(),
                                    ((RlpList) values.get(1))
                                            .getValues().stream()
                                                    .map(rawKey -> ((RlpString) rawKey).asString())
                                                    .collect(toList()));
                        })
                .collect(toList());
    }

    public static List<Bytes> decodeVersionedHashes(List<RlpType> rlp) {
        return rlp.stream()
                .map(
                        rlpType -> {
                            if (rlpType instanceof RlpString) {
                                RlpString rlpString = (RlpString) rlpType;
                                return Bytes.wrap(rlpString.getBytes());
                            } else {
                                throw new IllegalArgumentException(
                                        "List contains non-RlpString elements");
                            }
                        })
                .collect(Collectors.toList());
    }

    private static List<AuthorizationTuple> decodeAuthorizationList(final List<RlpType> rlpList) {
        return rlpList.stream()
                .map(item -> (RlpList) item) // each authorization tuple is an RlpList
                .map(
                        tuple -> {
                            final List<RlpType> elements = tuple.getValues();
                            final BigInteger authChainId =
                                    ((RlpString) elements.get(0)).asPositiveBigInteger();
                            final String address = ((RlpString) elements.get(1)).asString();
                            final BigInteger authNonce =
                                    ((RlpString) elements.get(2)).asPositiveBigInteger();
                            final BigInteger yParity =
                                    ((RlpString) elements.get(3)).asPositiveBigInteger();
                            final BigInteger rValue =
                                    ((RlpString) elements.get(4)).asPositiveBigInteger();
                            final BigInteger sValue =
                                    ((RlpString) elements.get(5)).asPositiveBigInteger();

                            // Convert or store them as desired; for example:
                            return new AuthorizationTuple(
                                    authChainId, address, authNonce, yParity, rValue, sValue);
                        })
                .collect(Collectors.toList());
    }
}
