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
package org.web3j.tx.gas;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;

public class DynamicEIP1559GasProvider implements ContractEIP1559GasProvider, PriorityGasProvider {
    private Web3j web3j;
    private long chainId;
    private final Priority priority;
    private final BigDecimal customMultiplier;
    private BigInteger maxGasLimit = BigInteger.valueOf(9_000_000);
    private BigInteger lastMaxPriorityFeePerGas = BigInteger.ZERO;

    public DynamicEIP1559GasProvider(Web3j web3j, long chainId) {
        this(web3j, chainId, Priority.NORMAL);
    }

    public DynamicEIP1559GasProvider(Web3j web3j, long chainId, Priority priority) {
        this(web3j, chainId, priority, BigDecimal.ONE);
    }

    public DynamicEIP1559GasProvider(
            Web3j web3j, long chainId, Priority priority, BigDecimal customMultiplier) {
        this.web3j = web3j;
        this.chainId = chainId;
        this.priority = priority;
        this.customMultiplier = customMultiplier;
    }

    @Override
    public long getChainId() {
        return chainId;
    }

    @Override
    public BigInteger getMaxFeePerGas() {
        try {
            EthBlock ethBlock =
                    web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
            if (ethBlock == null
                    || ethBlock.getBlock() == null
                    || ethBlock.getBlock().getBaseFeePerGas() == null) {
                throw new RuntimeException("Failed to fetch base fee from latest block");
            }
            BigInteger baseFee = ethBlock.getBlock().getBaseFeePerGas();

            BigInteger maxPriorityFeePerGas = getMaxPriorityFeePerGas();
            BigInteger maxFee =
                    baseFee.multiply(BigInteger.valueOf(2))
                            .add(maxPriorityFeePerGas)
                            .max(lastMaxPriorityFeePerGas)
                            .max(BigInteger.ZERO);
            return maxFee;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ethMaxFeePerGas", e);
        }
    }

    @Override
    public BigInteger getMaxPriorityFeePerGas() {
        BigInteger currentPriorityFee =
                applyPriority(fetchMaxPriorityFeePerGas(), priority, customMultiplier);

        if (currentPriorityFee.compareTo(BigInteger.ZERO) < 0) {
            currentPriorityFee = BigInteger.ZERO;
        }

        if (currentPriorityFee.compareTo(lastMaxPriorityFeePerGas) > 0) {
            lastMaxPriorityFeePerGas = currentPriorityFee;
        }
        return currentPriorityFee;
    }

    private BigInteger fetchMaxPriorityFeePerGas() {
        try {
            EthMaxPriorityFeePerGas ethMaxPriorityFeePerGas =
                    web3j.ethMaxPriorityFeePerGas().send();
            if (ethMaxPriorityFeePerGas.hasError()) {
                throw new RuntimeException(
                        "Error fetching ethMaxPriorityFeePerGas: "
                                + ethMaxPriorityFeePerGas.getError().getMessage());
            }
            return ethMaxPriorityFeePerGas.getMaxPriorityFeePerGas();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get ethMaxPriorityFeePerGas", e);
        }
    }

    @Override
    public BigInteger getGasPrice() {
        return applyPriority(fetchCurrentGasPrice(), priority, customMultiplier);
    }

    @Override
    public BigInteger getGasLimit(Transaction transaction) {
        try {
            EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
            if (ethEstimateGas.hasError()) {
                throw new RuntimeException(
                        "Error estimating gas limit: " + ethEstimateGas.getError().getMessage());
            }
            return ethEstimateGas.getAmountUsed();
        } catch (Exception e) {
            throw new RuntimeException("Failed to estimate gas limit", e);
        }
    }

    @Override
    public BigInteger getGasLimit() {
        return maxGasLimit;
    }

    public void setMaxGasLimit(BigInteger gasLimit) {
        maxGasLimit = gasLimit;
    }

    private BigInteger fetchCurrentGasPrice() {
        try {
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            if (ethGasPrice.hasError()) {
                throw new RuntimeException(
                        "Error fetching gas price: " + ethGasPrice.getError().getMessage());
            }
            return ethGasPrice.getGasPrice();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch gas price", e);
        }
    }
}
