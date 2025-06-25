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
package org.web3j.protocol.core.methods.request;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.web3j.utils.Numeric;

/**
 * Bundler Transaction request object used the below methods. This is a specific user operation
 * transaction for account transaction.
 *
 * <ol>
 *   <li>eth_sendUserOperation
 * </ol>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EIP4337Transaction {

    private String sender;
    private BigInteger nonce;
    private String initCode;
    private String callData;
    private String callGasLimit;
    private String verificationGasLimit;
    private String preVerificationGas;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String signature;
    private String paymasterAndData;

    public EIP4337Transaction(
            String sender,
            BigInteger nonce,
            String initCode,
            String callData,
            String callGasLimit,
            String verificationGasLimit,
            String preVerificationGas,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            String signature,
            String paymasterAndData) {
        this.sender = sender;
        this.nonce = nonce;
        this.initCode = initCode;
        this.callData = callData;
        this.callGasLimit = callGasLimit;
        this.verificationGasLimit = verificationGasLimit;
        this.preVerificationGas = preVerificationGas;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.signature = signature;
        this.paymasterAndData = paymasterAndData;
    }

    public static EIP4337Transaction createEthSendUserOperationTransaction(
            String sender,
            BigInteger nonce,
            String initCode,
            String callData,
            String callGasLimit,
            String verificationGasLimit,
            String preVerificationGas,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            String signature,
            String paymasterAndData) {
        return new EIP4337Transaction(
                sender,
                nonce,
                initCode,
                callData,
                callGasLimit,
                verificationGasLimit,
                preVerificationGas,
                maxFeePerGas,
                maxPriorityFeePerGas,
                signature,
                paymasterAndData);
    }

    public static EIP4337Transaction createEthEstimateUserOperationGasTransaction(
            String sender,
            BigInteger nonce,
            String initCode,
            String callData,
            String callGasLimit,
            String verificationGasLimit,
            String preVerificationGas,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            String signature,
            String paymasterAndData) {
        return createEthSendUserOperationTransaction(
                sender,
                nonce,
                initCode,
                callData,
                callGasLimit,
                verificationGasLimit,
                preVerificationGas,
                maxFeePerGas,
                maxPriorityFeePerGas,
                signature,
                paymasterAndData);
    }

    public String getSender() {
        return sender;
    }

    public String getCallData() {
        return callData;
    }

    public String getNonce() {
        if (nonce != null) {
            return Numeric.encodeQuantity(nonce);
        } else {
            return null; // we don't want the field to be encoded if not present
        }
    }

    public String getInitCode() {
        return initCode;
    }

    public String getCallGasLimit() {
        return callGasLimit;
    }

    public String getVerificationGasLimit() {
        return verificationGasLimit;
    }

    public String getPreVerificationGas() {
        return preVerificationGas;
    }

    public String getMaxFeePerGas() {
        return maxFeePerGas;
    }

    public String getMaxPriorityFeePerGas() {
        return maxPriorityFeePerGas;
    }

    public String getSignature() {
        return signature;
    }

    public String getPaymasterAndData() {
        return paymasterAndData;
    }
}
