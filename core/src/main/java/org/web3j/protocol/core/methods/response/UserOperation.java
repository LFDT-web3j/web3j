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
package org.web3j.protocol.core.methods.response;

/** UserOperation object used by {@link EthGetUserOperationByHash}. */
public class UserOperation {
    private String sender;
    private String nonce;
    private String initCode;
    private String callData;
    private String callGasLimit;
    private String verificationGasLimit;
    private String preVerificationGas;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String paymasterAndData;
    private String signature;
    private String entryPoint;
    private String blockNumber;
    private String blockHash;
    private String transactionHash;

    public UserOperation() {}

    public UserOperation(
            String sender,
            String nonce,
            String initCode,
            String callData,
            String callGasLimit,
            String verificationGasLimit,
            String preVerificationGas,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            String paymasterAndData,
            String signature,
            String entryPoint,
            String blockNumber,
            String blockHash,
            String transactionHash) {
        this.sender = sender;
        this.nonce = nonce;
        this.initCode = initCode;
        this.callData = callData;
        this.callGasLimit = callGasLimit;
        this.verificationGasLimit = verificationGasLimit;
        this.preVerificationGas = preVerificationGas;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.paymasterAndData = paymasterAndData;
        this.signature = signature;
        this.entryPoint = entryPoint;
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
        this.transactionHash = transactionHash;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getInitCode() {
        return initCode;
    }

    public void setInitCode(String initCode) {
        this.initCode = initCode;
    }

    public String getCallData() {
        return callData;
    }

    public void setCallData(String callData) {
        this.callData = callData;
    }

    public String getCallGasLimit() {
        return callGasLimit;
    }

    public void setCallGasLimit(String callGasLimit) {
        this.callGasLimit = callGasLimit;
    }

    public String getVerificationGasLimit() {
        return verificationGasLimit;
    }

    public void setVerificationGasLimit(String verificationGasLimit) {
        this.verificationGasLimit = verificationGasLimit;
    }

    public String getPreVerificationGas() {
        return preVerificationGas;
    }

    public void setPreVerificationGas(String preVerificationGas) {
        this.preVerificationGas = preVerificationGas;
    }

    public String getMaxFeePerGas() {
        return maxFeePerGas;
    }

    public void setMaxFeePerGas(String maxFeePerGas) {
        this.maxFeePerGas = maxFeePerGas;
    }

    public String getMaxPriorityFeePerGas() {
        return maxPriorityFeePerGas;
    }

    public void setMaxPriorityFeePerGas(String maxPriorityFeePerGas) {
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    public String getPaymasterAndData() {
        return paymasterAndData;
    }

    public void setPaymasterAndData(String paymasterAndData) {
        this.paymasterAndData = paymasterAndData;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserOperation other)) {
            return false;
        }

        if (sender != null && !sender.equals(other.sender)) return false;
        if (nonce != null && !nonce.equals(other.nonce)) return false;
        if (initCode != null && !initCode.equals(other.initCode)) return false;
        if (callData != null && !callData.equals(other.callData)) return false;
        if (callGasLimit != null && !callGasLimit.equals(other.callGasLimit)) return false;
        if (verificationGasLimit != null
                && !verificationGasLimit.equals(other.verificationGasLimit)) return false;
        if (preVerificationGas != null && !preVerificationGas.equals(other.preVerificationGas))
            return false;
        if (maxFeePerGas != null && !maxFeePerGas.equals(other.maxFeePerGas)) return false;
        if (maxPriorityFeePerGas != null
                && !maxPriorityFeePerGas.equals(other.maxPriorityFeePerGas)) return false;
        if (paymasterAndData != null && !paymasterAndData.equals(other.paymasterAndData))
            return false;
        if (signature != null && !signature.equals(other.signature)) return false;
        if (entryPoint != null && !entryPoint.equals(other.entryPoint)) return false;
        if (blockNumber != null && !blockNumber.equals(other.blockNumber)) return false;
        if (blockHash != null && !blockHash.equals(other.blockHash)) return false;
        return transactionHash == null || transactionHash.equals(other.transactionHash);
    }

    @Override
    public int hashCode() {
        int result = getCallGasLimit() != null ? getCallGasLimit().hashCode() : 0;
        result =
                31 * result
                        + (getVerificationGasLimit() != null
                                ? getVerificationGasLimit().hashCode()
                                : 0);
        result =
                31 * result
                        + (getPreVerificationGas() != null
                                ? getPreVerificationGas().hashCode()
                                : 0);
        result =
                31 * result
                        + (getPaymasterAndData() != null ? getPaymasterAndData().hashCode() : 0);
        result = 31 * result + (getSender() != null ? getSender().hashCode() : 0);
        result = 31 * result + (getNonce() != null ? getNonce().hashCode() : 0);
        result = 31 * result + (getInitCode() != null ? getInitCode().hashCode() : 0);
        result = 31 * result + (getCallData() != null ? getCallData().hashCode() : 0);
        result = 31 * result + (getMaxFeePerGas() != null ? getMaxFeePerGas().hashCode() : 0);
        result =
                31 * result
                        + (getMaxPriorityFeePerGas() != null
                                ? getMaxPriorityFeePerGas().hashCode()
                                : 0);
        result = 31 * result + (getSignature() != null ? getSignature().hashCode() : 0);
        result = 31 * result + (getEntryPoint() != null ? getEntryPoint().hashCode() : 0);
        result = 31 * result + (getBlockNumber() != null ? getBlockNumber().hashCode() : 0);
        result = 31 * result + (getBlockHash() != null ? getBlockHash().hashCode() : 0);
        result = 31 * result + (getTransactionHash() != null ? getTransactionHash().hashCode() : 0);
        return result;
    }
}
