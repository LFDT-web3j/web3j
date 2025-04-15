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
import java.util.Collections;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

import org.web3j.crypto.transaction.type.ITransaction;
import org.web3j.crypto.transaction.type.LegacyTransaction;
import org.web3j.crypto.transaction.type.Transaction1559;
import org.web3j.crypto.transaction.type.Transaction2930;
import org.web3j.crypto.transaction.type.Transaction4844;
import org.web3j.crypto.transaction.type.Transaction7702;
import org.web3j.crypto.transaction.type.TransactionType;

/**
 * Transaction class used for signing transactions locally.<br>
 * For the specification, refer to p4 of the <a href="http://gavwood.com/paper.pdf">yellow
 * paper</a>.
 */
public class RawTransaction {

    private final ITransaction transaction;

    protected RawTransaction(final ITransaction transaction) {
        this.transaction = transaction;
    }

    protected RawTransaction(
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data) {
        this(new LegacyTransaction(nonce, gasPrice, gasLimit, to, value, data));
    }

    public static RawTransaction createContractTransaction(
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            BigInteger value,
            String init) {
        return new RawTransaction(
                LegacyTransaction.createContractTransaction(
                        nonce, gasPrice, gasLimit, value, init));
    }

    public static RawTransaction createEtherTransaction(
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            BigInteger value) {

        return new RawTransaction(
                LegacyTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, value));
    }

    public static RawTransaction createEtherTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas) {
        return new RawTransaction(
                Transaction1559.createEtherTransaction(
                        chainId, nonce, gasLimit, to, value, maxPriorityFeePerGas, maxFeePerGas));
    }

    public static RawTransaction createTransaction(
            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, String data) {
        return createTransaction(nonce, gasPrice, gasLimit, to, BigInteger.ZERO, data);
    }

    public static RawTransaction createTransaction(
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data) {

        return new RawTransaction(
                LegacyTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data));
    }

    public static RawTransaction createTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas) {

        return new RawTransaction(
                Transaction1559.createTransaction(
                        chainId,
                        nonce,
                        gasLimit,
                        to,
                        value,
                        data,
                        maxPriorityFeePerGas,
                        maxFeePerGas,
                        Collections.emptyList()));
    }

    public static RawTransaction createTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            List<AccessListObject> accessList) {

        return new RawTransaction(
                Transaction1559.createTransaction(
                        chainId,
                        nonce,
                        gasLimit,
                        to,
                        value,
                        data,
                        maxPriorityFeePerGas,
                        maxFeePerGas,
                        accessList));
    }

    public static RawTransaction createTransaction(
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

        return new RawTransaction(
                Transaction4844.createTransaction(
                        chainId,
                        nonce,
                        maxPriorityFeePerGas,
                        maxFeePerGas,
                        gasLimit,
                        to,
                        value,
                        data,
                        maxFeePerBlobGas,
                        versionedHashes));
    }

    public static RawTransaction createTransaction(
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

        return new RawTransaction(
                Transaction4844.createTransaction(
                        blobs,
                        chainId,
                        nonce,
                        maxPriorityFeePerGas,
                        maxFeePerGas,
                        gasLimit,
                        to,
                        value,
                        data,
                        maxFeePerBlobGas));
    }

    public static RawTransaction createTransaction(
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

        return new RawTransaction(
                Transaction4844.createTransaction(
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
                        versionedHashes));
    }

    public static RawTransaction createTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            List<AccessListObject> accessList) {
        return new RawTransaction(
                Transaction2930.createTransaction(
                        chainId, nonce, gasPrice, gasLimit, to, value, data, accessList));
    }

    public static RawTransaction createTransactionEIP7702(
        long chainId,
        BigInteger nonce,
        BigInteger maxPriorityFeePerGas,
        BigInteger maxFeePerGas,
        BigInteger gasLimit,
        String to,
        BigInteger value,
        String data,
        List<AccessListObject> accessList,
        List<AuthorizationTuple> authorizationList
) {
    Transaction7702 tx = Transaction7702.createTransaction(
            chainId,
            nonce,
            maxPriorityFeePerGas,
            maxFeePerGas,
            gasLimit,
            to,
            value,
            data,
            accessList,
            authorizationList
    );
    return new RawTransaction(tx);
}

    public BigInteger getNonce() {
        return transaction.getNonce();
    }

    public BigInteger getGasPrice() {
        return transaction.getGasPrice();
    }

    public BigInteger getGasLimit() {
        return transaction.getGasLimit();
    }

    public String getTo() {
        return transaction.getTo();
    }

    public BigInteger getValue() {
        return transaction.getValue();
    }

    public String getData() {
        return transaction.getData();
    }

    public TransactionType getType() {
        return transaction.getType();
    }

    public ITransaction getTransaction() {
        return transaction;
    }
}
