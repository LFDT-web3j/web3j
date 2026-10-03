/*
 * Copyright 2026 Web3 Labs Ltd.
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

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicEIP1559GasProviderTest {

    @Test
    public void testMaxPriorityFeePerGasAppliesPriority() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.FAST);

        EthMaxPriorityFeePerGas ethMaxPriorityFeePerGas = mock(EthMaxPriorityFeePerGas.class);
        // Base Priority Fee: 10
        when(ethMaxPriorityFeePerGas.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(10));
        when(ethMaxPriorityFeePerGas.hasError()).thenReturn(false);

        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        when(priorityFeeRequest.send()).thenReturn(ethMaxPriorityFeePerGas);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        // For Priority.FAST, it multiplies by 2
        BigInteger maxPriorityFee = provider.getMaxPriorityFeePerGas();

        org.junit.jupiter.api.Assertions.assertEquals(BigInteger.valueOf(20), maxPriorityFee);
    }

    @Test
    public void testMaxPriorityFeePerGasAppliesCustomMultiplier() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(
                        web3j,
                        1,
                        PriorityGasProvider.Priority.CUSTOM,
                        new java.math.BigDecimal("1.5"));

        EthMaxPriorityFeePerGas ethMaxPriorityFeePerGas = mock(EthMaxPriorityFeePerGas.class);
        // Base Priority Fee: 10
        when(ethMaxPriorityFeePerGas.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(10));
        when(ethMaxPriorityFeePerGas.hasError()).thenReturn(false);

        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        when(priorityFeeRequest.send()).thenReturn(ethMaxPriorityFeePerGas);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        // For Priority.CUSTOM with multiplier 1.5, it should be 15
        BigInteger maxPriorityFee = provider.getMaxPriorityFeePerGas();

        org.junit.jupiter.api.Assertions.assertEquals(BigInteger.valueOf(15), maxPriorityFee);
    }
}
