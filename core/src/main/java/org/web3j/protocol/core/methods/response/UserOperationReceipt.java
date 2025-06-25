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

import java.util.List;

/** UserOperation object used by {@link EthGetUserOperationReceipt}. */
public class UserOperationReceipt {
    private String userOpHash;
    private String entryPoint;
    private String sender;
    private String nonce;
    private String paymaster;
    private String actualGasCost;
    private String actualGasUsed;
    private boolean success;
    private String reason;
    private List<Log> logs;
    private TransactionReceipt receipt;

    public String getUserOpHash() {
        return userOpHash;
    }

    public void setUserOpHash(String userOpHash) {
        this.userOpHash = userOpHash;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getPaymaster() {
        return paymaster;
    }

    public void setPaymaster(String paymaster) {
        this.paymaster = paymaster;
    }

    public String getActualGasCost() {
        return actualGasCost;
    }

    public void setActualGasCost(String actualGasCost) {
        this.actualGasCost = actualGasCost;
    }

    public String getActualGasUsed() {
        return actualGasUsed;
    }

    public void setActualGasUsed(String actualGasUsed) {
        this.actualGasUsed = actualGasUsed;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public void setReceipt(TransactionReceipt receipt) {
        this.receipt = receipt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof UserOperationReceipt that)) {
            return false;
        }

        if (success != that.success) return false;
        if (userOpHash != null ? !userOpHash.equals(that.userOpHash) : that.userOpHash != null)
            return false;
        if (entryPoint != null ? !entryPoint.equals(that.entryPoint) : that.entryPoint != null)
            return false;
        if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
        if (nonce != null ? !nonce.equals(that.nonce) : that.nonce != null) return false;
        if (paymaster != null ? !paymaster.equals(that.paymaster) : that.paymaster != null)
            return false;
        if (actualGasCost != null
                ? !actualGasCost.equals(that.actualGasCost)
                : that.actualGasCost != null) return false;
        if (actualGasUsed != null
                ? !actualGasUsed.equals(that.actualGasUsed)
                : that.actualGasUsed != null) return false;
        if (reason != null ? !reason.equals(that.reason) : that.reason != null) return false;
        if (logs != null ? !logs.equals(that.logs) : that.logs != null) return false;
        return receipt != null ? receipt.equals(that.receipt) : that.receipt == null;
    }
}
