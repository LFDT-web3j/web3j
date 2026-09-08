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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import org.web3j.protocol.Web3j;

/**
 * {@link DynamicEIP1559GasProvider} that enforces a configurable upper limit (cap) on the EIP-1559
 * gas fees. This protects against paying excessive fees during gas price spikes on public networks.
 */
public class CappedDynamicEIP1559GasProvider extends DynamicEIP1559GasProvider {

    /** Upper limit (in wei) applied to the calculated gas fees. */
    private final BigInteger maxFeePerGas;

    public CappedDynamicEIP1559GasProvider(Web3j web3j, long chainId, BigInteger maxFeePerGas) {
        this(web3j, chainId, Priority.NORMAL, BigDecimal.ONE, maxFeePerGas);
    }

    public CappedDynamicEIP1559GasProvider(
            Web3j web3j,
            long chainId,
            PriorityGasProvider.Priority priority,
            BigInteger maxFeePerGas) {
        this(web3j, chainId, priority, BigDecimal.ONE, maxFeePerGas);
    }

    public CappedDynamicEIP1559GasProvider(
            Web3j web3j,
            long chainId,
            PriorityGasProvider.Priority priority,
            BigDecimal customMultiplier,
            BigInteger maxFeePerGas) {
        super(web3j, chainId, priority, customMultiplier);
        Objects.requireNonNull(maxFeePerGas, "maxFeePerGas must not be null");
        if (maxFeePerGas.signum() <= 0) {
            throw new IllegalArgumentException("maxFeePerGas must be positive");
        }
        this.maxFeePerGas = maxFeePerGas;
    }

    /**
     * Returns the dynamically calculated max fee per gas, capped at the configured upper limit.
     *
     * @return the smaller of the calculated fee and the configured maximum
     */
    @Override
    public BigInteger getMaxFeePerGas() {
        return super.getMaxFeePerGas().min(maxFeePerGas);
    }

    /**
     * Returns the dynamically calculated max priority fee per gas, capped at the configured upper
     * limit so it never exceeds the overall max fee per gas.
     *
     * @return the smaller of the calculated priority fee and the configured maximum
     */
    @Override
    public BigInteger getMaxPriorityFeePerGas() {
        return super.getMaxPriorityFeePerGas().min(maxFeePerGas);
    }

    @Override
    public BigInteger getGasPrice() {
        return super.getGasPrice().min(maxFeePerGas);
    }
}
