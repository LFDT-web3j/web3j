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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class DynamicEIP1559GasProviderTest {

    @Test
    public void testMaxPriorityFeePerGasConsistency() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.NORMAL);

        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

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

        when(priorityFeeRequest.send())
                .thenReturn(ethMaxPriorityFeePerGasHigh, ethMaxPriorityFeePerGasLow);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        BigInteger maxPriorityFee = provider.getMaxPriorityFeePerGas();
        BigInteger maxFee = provider.getMaxFeePerGas();

        assertTrue(
                maxPriorityFee.compareTo(maxFee) <= 0,
                "maxPriorityFeePerGas ("
                        + maxPriorityFee
                        + ") should not exceed maxFeePerGas ("
                        + maxFee
                        + ")");
        assertEquals(BigInteger.valueOf(100), maxFee);
    }

    @Test
    public void testMaxFeePerGasDecreasesAfterSpike() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.NORMAL);

        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.valueOf(10));
        when(ethBlock.getBlock()).thenReturn(block);

        Request<?, EthBlock> ethBlockRequest = mock(Request.class);
        when(ethBlockRequest.send()).thenReturn(ethBlock);
        when(web3j.ethGetBlockByNumber(any(), Mockito.anyBoolean()))
                .thenReturn((Request) ethBlockRequest);

        EthMaxPriorityFeePerGas spikePriorityFee = mock(EthMaxPriorityFeePerGas.class);
        when(spikePriorityFee.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(100));
        when(spikePriorityFee.hasError()).thenReturn(false);

        EthMaxPriorityFeePerGas normalPriorityFee = mock(EthMaxPriorityFeePerGas.class);
        when(normalPriorityFee.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(2));
        when(normalPriorityFee.hasError()).thenReturn(false);

        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);

        when(priorityFeeRequest.send())
                .thenReturn(
                        spikePriorityFee, normalPriorityFee, normalPriorityFee, normalPriorityFee);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        BigInteger spikeMaxFee = provider.getMaxFeePerGas();
        assertEquals(BigInteger.valueOf(120), spikeMaxFee);

        BigInteger normalMaxFee = provider.getMaxFeePerGas();
        assertEquals(BigInteger.valueOf(22), normalMaxFee);

        BigInteger normalPriority = provider.getMaxPriorityFeePerGas();
        BigInteger normalContractMaxFee = provider.getMaxFeePerGas();
        assertEquals(BigInteger.valueOf(2), normalPriority);
        assertEquals(BigInteger.valueOf(22), normalContractMaxFee);
    }

    @Test
    public void testPendingPriorityFeeIsClearedWhenMaxFeeFetchFails() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.NORMAL);

        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.valueOf(10));
        when(ethBlock.getBlock()).thenReturn(block);

        Request<?, EthBlock> ethBlockRequest = mock(Request.class);
        when(ethBlockRequest.send())
                .thenThrow(new IOException("temporary block RPC failure"))
                .thenReturn(ethBlock);
        when(web3j.ethGetBlockByNumber(any(), Mockito.anyBoolean()))
                .thenReturn((Request) ethBlockRequest);

        EthMaxPriorityFeePerGas highTip = mock(EthMaxPriorityFeePerGas.class);
        when(highTip.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(100));
        when(highTip.hasError()).thenReturn(false);

        EthMaxPriorityFeePerGas lowTip = mock(EthMaxPriorityFeePerGas.class);
        when(lowTip.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(2));
        when(lowTip.hasError()).thenReturn(false);

        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        when(priorityFeeRequest.send()).thenReturn(highTip, lowTip);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        assertEquals(BigInteger.valueOf(100), provider.getMaxPriorityFeePerGas());
        assertThrows(RuntimeException.class, () -> provider.getMaxFeePerGas());

        assertEquals(BigInteger.valueOf(22), provider.getMaxFeePerGas());
    }

    @Test
    public void testPriorityAndCustomMultiplierApplyToEip1559Fees() throws Exception {
        Web3j web3j = mock(Web3j.class);
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.TEN);
        when(ethBlock.getBlock()).thenReturn(block);

        Request<?, EthBlock> ethBlockRequest = mock(Request.class);
        when(ethBlockRequest.send()).thenReturn(ethBlock);
        when(web3j.ethGetBlockByNumber(any(), Mockito.anyBoolean()))
                .thenReturn((Request) ethBlockRequest);

        EthMaxPriorityFeePerGas tip = mock(EthMaxPriorityFeePerGas.class);
        when(tip.getMaxPriorityFeePerGas()).thenReturn(BigInteger.TEN);
        when(tip.hasError()).thenReturn(false);

        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        when(priorityFeeRequest.send()).thenReturn(tip);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        assertEip1559Fees(
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.FAST),
                BigInteger.valueOf(20),
                BigInteger.valueOf(40));
        assertEip1559Fees(
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.NORMAL),
                BigInteger.valueOf(10),
                BigInteger.valueOf(30));
        assertEip1559Fees(
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.SLOW),
                BigInteger.valueOf(5),
                BigInteger.valueOf(25));
        assertEip1559Fees(
                new DynamicEIP1559GasProvider(
                        web3j, 1, PriorityGasProvider.Priority.CUSTOM, new BigDecimal("1.5")),
                BigInteger.valueOf(15),
                BigInteger.valueOf(35));
    }

    private void assertEip1559Fees(
            DynamicEIP1559GasProvider provider,
            BigInteger expectedPriorityFee,
            BigInteger expectedMaxFee) {
        assertEquals(expectedPriorityFee, provider.getMaxPriorityFeePerGas());
        assertEquals(expectedMaxFee, provider.getMaxFeePerGas());
    }

    @Test
    public void testMaxPriorityFeePerGasPropagatesRpcError() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.NORMAL);

        EthMaxPriorityFeePerGas errorResponse = mock(EthMaxPriorityFeePerGas.class);
        Response.Error error = new Response.Error(429, "rate limited by node");
        when(errorResponse.hasError()).thenReturn(true);
        when(errorResponse.getError()).thenReturn(error);

        Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
        when(priorityFeeRequest.send()).thenReturn(errorResponse);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn((Request) priorityFeeRequest);

        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> provider.getMaxPriorityFeePerGas());
        assertTrue(ex.getMessage().contains("rate limited by node"));
    }

    @Test
    public void testConcurrentAccessMaintainsInvariant() throws Exception {
        Web3j web3j = mock(Web3j.class);
        DynamicEIP1559GasProvider provider =
                new DynamicEIP1559GasProvider(web3j, 1, PriorityGasProvider.Priority.NORMAL);

        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.valueOf(10));
        when(ethBlock.getBlock()).thenReturn(block);

        Request<?, EthBlock> ethBlockRequest = mock(Request.class);
        when(ethBlockRequest.send()).thenReturn(ethBlock);
        when(web3j.ethGetBlockByNumber(any(), Mockito.anyBoolean()))
                .thenReturn((Request) ethBlockRequest);

        EthMaxPriorityFeePerGas tip100 = mock(EthMaxPriorityFeePerGas.class);
        when(tip100.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(100));
        when(tip100.hasError()).thenReturn(false);

        EthMaxPriorityFeePerGas tip1 = mock(EthMaxPriorityFeePerGas.class);
        when(tip1.getMaxPriorityFeePerGas()).thenReturn(BigInteger.ONE);
        when(tip1.hasError()).thenReturn(false);

        Request<?, EthMaxPriorityFeePerGas> req100 = mock(Request.class);
        when(req100.send()).thenReturn(tip100);

        Request<?, EthMaxPriorityFeePerGas> req1 = mock(Request.class);
        when(req1.send()).thenReturn(tip1);

        AtomicBoolean thread1Started = new AtomicBoolean(false);
        CountDownLatch thread1GotTip = new CountDownLatch(1);
        CountDownLatch thread2Completed = new CountDownLatch(1);

        when(web3j.ethMaxPriorityFeePerGas())
                .thenAnswer(invocation -> thread1Started.get() ? req1 : req100);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<BigInteger[]> thread1 =
                    executor.submit(
                            () -> {
                                BigInteger tip = provider.getMaxPriorityFeePerGas();
                                thread1Started.set(true);
                                thread1GotTip.countDown();
                                assertTrue(
                                        thread2Completed.await(5, TimeUnit.SECONDS),
                                        "Thread 2 did not complete before the timeout");
                                return new BigInteger[] {tip, provider.getMaxFeePerGas()};
                            });

            Future<?> thread2 =
                    executor.submit(
                            () -> {
                                assertTrue(
                                        thread1GotTip.await(5, TimeUnit.SECONDS),
                                        "Thread 1 did not fetch its tip before the timeout");
                                provider.getMaxPriorityFeePerGas();
                                provider.getMaxFeePerGas();
                                thread2Completed.countDown();
                                return null;
                            });

            thread2.get(10, TimeUnit.SECONDS);
            BigInteger[] thread1Fees = thread1.get(10, TimeUnit.SECONDS);

            assertTrue(
                    thread1Fees[1].compareTo(thread1Fees[0]) >= 0,
                    "Thread 1 maxFee ("
                            + thread1Fees[1]
                            + ") should be >= tip ("
                            + thread1Fees[0]
                            + ")");
        } finally {
            executor.shutdownNow();
            assertTrue(
                    executor.awaitTermination(5, TimeUnit.SECONDS),
                    "Concurrency-test workers did not terminate");
        }
    }
}
