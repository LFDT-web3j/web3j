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

import org.junit.jupiter.api.Test;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CappedDynamicEIP1559GasProviderTest {

    @Test
    void capsMaxFeePerGas() throws Exception {
        Web3j web3j = mock(Web3j.class);
        Request<?, EthBlock> blockRequest = mock(Request.class);
        EthBlock blockResponse = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        EthMaxPriorityFeePerGas priorityFeeResponse = mock(EthMaxPriorityFeePerGas.class);

        doReturn(blockRequest)
                .when(web3j)
                .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false);
        when(blockRequest.send()).thenReturn(blockResponse);
        when(blockResponse.getBlock()).thenReturn(block);
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.valueOf(100));
        doReturn(priorityFeeRequest).when(web3j).ethMaxPriorityFeePerGas();
        when(priorityFeeRequest.send()).thenReturn(priorityFeeResponse);
        when(priorityFeeResponse.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(20));

        CappedDynamicEIP1559GasProvider gasProvider =
                new CappedDynamicEIP1559GasProvider(
                        web3j,
                        1L,
                        PriorityGasProvider.Priority.NORMAL,
                        BigDecimal.ONE,
                        BigInteger.valueOf(150));

        assertEquals(BigInteger.valueOf(150), gasProvider.getMaxFeePerGas());
    }

    @Test
    void leavesMaxFeePerGasUncappedWhenBelowCap() throws Exception {
        Web3j web3j = mock(Web3j.class);
        Request<?, EthBlock> blockRequest = mock(Request.class);
        EthBlock blockResponse = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        EthMaxPriorityFeePerGas priorityFeeResponse = mock(EthMaxPriorityFeePerGas.class);

        doReturn(blockRequest)
                .when(web3j)
                .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false);
        when(blockRequest.send()).thenReturn(blockResponse);
        when(blockResponse.getBlock()).thenReturn(block);
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.valueOf(100));
        doReturn(priorityFeeRequest).when(web3j).ethMaxPriorityFeePerGas();
        when(priorityFeeRequest.send()).thenReturn(priorityFeeResponse);
        when(priorityFeeResponse.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(20));

        CappedDynamicEIP1559GasProvider gasProvider =
                new CappedDynamicEIP1559GasProvider(web3j, 1L, BigInteger.valueOf(500));

        assertEquals(BigInteger.valueOf(220), gasProvider.getMaxFeePerGas());
    }

    @Test
    void capsMaxPriorityFeePerGas() throws Exception {
        Web3j web3j = mock(Web3j.class);
        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        EthMaxPriorityFeePerGas priorityFeeResponse = mock(EthMaxPriorityFeePerGas.class);

        doReturn(priorityFeeRequest).when(web3j).ethMaxPriorityFeePerGas();
        when(priorityFeeRequest.send()).thenReturn(priorityFeeResponse);
        when(priorityFeeResponse.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(20));

        CappedDynamicEIP1559GasProvider gasProvider =
                new CappedDynamicEIP1559GasProvider(web3j, 1L, BigInteger.valueOf(15));

        assertEquals(BigInteger.valueOf(15), gasProvider.getMaxPriorityFeePerGas());
    }

    @Test
    void capsLegacyGasPrice() throws Exception {
        Web3j web3j = mock(Web3j.class);
        Request<?, EthGasPrice> gasPriceRequest = mock(Request.class);
        EthGasPrice gasPriceResponse = mock(EthGasPrice.class);

        doReturn(gasPriceRequest).when(web3j).ethGasPrice();
        when(gasPriceRequest.send()).thenReturn(gasPriceResponse);
        when(gasPriceResponse.getGasPrice()).thenReturn(BigInteger.valueOf(100));

        CappedDynamicEIP1559GasProvider gasProvider =
                new CappedDynamicEIP1559GasProvider(web3j, 1L, BigInteger.valueOf(90));

        assertEquals(BigInteger.valueOf(90), gasProvider.getGasPrice());
    }

    @Test
    void rejectsNullMaxFeePerGas() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new CappedDynamicEIP1559GasProvider(
                                mock(Web3j.class),
                                1L,
                                PriorityGasProvider.Priority.NORMAL,
                                BigDecimal.ONE,
                                null));
    }

    @Test
    void rejectsNonPositiveMaxFeePerGas() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new CappedDynamicEIP1559GasProvider(
                                mock(Web3j.class),
                                1L,
                                PriorityGasProvider.Priority.NORMAL,
                                BigDecimal.ONE,
                                BigInteger.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new CappedDynamicEIP1559GasProvider(
                                mock(Web3j.class),
                                1L,
                                PriorityGasProvider.Priority.NORMAL,
                                BigDecimal.ONE,
                                BigInteger.valueOf(-1)));
    }
}
