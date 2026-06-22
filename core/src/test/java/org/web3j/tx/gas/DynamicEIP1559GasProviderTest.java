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
import org.mockito.Mockito;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicEIP1559GasProviderTest {

    @Test
    public void testMaxPriorityFeePerGasConsistency() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.NORMAL);

        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        // Base Fee: 10
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.valueOf(10));
        when(ethBlock.getBlock()).thenReturn(block);

        Request<?, EthBlock> ethBlockRequest = mock(Request.class);
        when(ethBlockRequest.send()).thenReturn(ethBlock);
        when(web3j.ethGetBlockByNumber(any(), Mockito.anyBoolean()))
                .thenReturn((Request) ethBlockRequest);

        EthMaxPriorityFeePerGas ethMaxPriorityFeePerGasHigh = mock(EthMaxPriorityFeePerGas.class);
        when(ethMaxPriorityFeePerGasHigh.getMaxPriorityFeePerGas())
                .thenReturn(BigInteger.valueOf(100));
        when(ethMaxPriorityFeePerGasHigh.hasError()).thenReturn(false);

        EthMaxPriorityFeePerGas ethMaxPriorityFeePerGasLow = mock(EthMaxPriorityFeePerGas.class);
        when(ethMaxPriorityFeePerGasLow.getMaxPriorityFeePerGas())
                .thenReturn(BigInteger.valueOf(1));
        when(ethMaxPriorityFeePerGasLow.hasError()).thenReturn(false);

        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        // First call returns 100, second call returns 1
        when(priorityFeeRequest.send())
                .thenReturn(ethMaxPriorityFeePerGasHigh, ethMaxPriorityFeePerGasLow);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        // TransactionManager logic often retrieves priority fee first
        BigInteger maxPriorityFee = provider.getMaxPriorityFeePerGas(); // returns 100
        BigInteger maxFee =
                provider.getMaxFeePerGas(); // calls getMaxPriorityFeePerGas() again, returns 1.
        // 2*10 + 1 = 21

        // 100 > 21. This will cause "max priority fee per gas higher than max fee per gas" error at
        // the node level.
        assertTrue(
                maxPriorityFee.compareTo(maxFee) <= 0,
                "maxPriorityFeePerGas ("
                        + maxPriorityFee
                        + ") should not exceed maxFeePerGas ("
                        + maxFee
                        + ")");
    }

    @Test
    public void testMaxPriorityFeeExceedingMaxFee() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.NORMAL);

        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        // Base Fee: 1
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.ONE);
        when(ethBlock.getBlock()).thenReturn(block);

        Request<?, EthBlock> ethBlockRequest = mock(Request.class);
        when(ethBlockRequest.send()).thenReturn(ethBlock);
        when(web3j.ethGetBlockByNumber(any(), Mockito.anyBoolean()))
                .thenReturn((Request) ethBlockRequest);

        EthMaxPriorityFeePerGas ethMaxPriorityFeePerGas = mock(EthMaxPriorityFeePerGas.class);
        // Priority Fee: 100
        when(ethMaxPriorityFeePerGas.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(100));
        when(ethMaxPriorityFeePerGas.hasError()).thenReturn(false);

        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        when(priorityFeeRequest.send()).thenReturn(ethMaxPriorityFeePerGas);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        BigInteger maxPriorityFee = provider.getMaxPriorityFeePerGas(); // 100
        BigInteger maxFee = provider.getMaxFeePerGas(); // 2*1 + 100 = 102

        assertTrue(
                maxPriorityFee.compareTo(maxFee) <= 0,
                "maxPriorityFeePerGas ("
                        + maxPriorityFee
                        + ") should not exceed maxFeePerGas ("
                        + maxFee
                        + ")");

        // Even with base fee of 0
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.ZERO);
        maxFee = provider.getMaxFeePerGas(); // 2*0 + 100 = 100
        assertTrue(
                maxPriorityFee.compareTo(maxFee) <= 0,
                "maxPriorityFeePerGas ("
                        + maxPriorityFee
                        + ") should not exceed maxFeePerGas ("
                        + maxFee
                        + ") with zero base fee");
    }
}
