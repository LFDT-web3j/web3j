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
package org.web3j.protocol.rx;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthUninstallFilter;
import org.web3j.protocol.exceptions.ClientConnectionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A filter that fails to uninstall when its flowable is cancelled must not report the failure to
 * {@link RxJavaPlugins#onError}, where it would surface as an UndeliverableException.
 */
class JsonRpc2_0RxFilterCancelTest {

    private static final String HASH =
            "0x31c2342b1e0b8ffda1507fbffddf213c4b3c1e819ff6a84b943faabb0ebf2403";

    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private final List<Throwable> undeliverable = new CopyOnWriteArrayList<>();

    private final List<ScheduledFuture<?>> polling = new CopyOnWriteArrayList<>();

    private final AtomicInteger uninstalls = new AtomicInteger();

    private ScheduledThreadPoolExecutor executor;

    private Web3jService web3jService;

    private Web3j web3j;

    enum UninstallFailure {
        NOT_UNINSTALLED,
        RPC_ERROR,
        IO_EXCEPTION,
        HTTP_ERROR
    }

    @BeforeEach
    void setUp() {
        RxJavaPlugins.setErrorHandler(undeliverable::add);
        // keep the future of the polling task: the queue is empty while the task runs, so only
        // the future tells whether the filter was cancelled
        executor =
                new ScheduledThreadPoolExecutor(1) {
                    @Override
                    public ScheduledFuture<?> scheduleAtFixedRate(
                            Runnable command, long initialDelay, long period, TimeUnit unit) {
                        ScheduledFuture<?> future =
                                super.scheduleAtFixedRate(command, initialDelay, period, unit);
                        polling.add(future);
                        return future;
                    }
                };
        web3jService = mock(Web3jService.class);
        web3j = Web3j.build(web3jService, 10, executor);
    }

    @AfterEach
    void tearDown() {
        RxJavaPlugins.reset();
        executor.shutdownNow();
    }

    @ParameterizedTest
    @EnumSource(UninstallFailure.class)
    void disposeWhenUninstallFails(UninstallFailure failure) throws Exception {
        CountDownLatch polled = new CountDownLatch(2);
        mockNode(polled, failure);

        Disposable subscription = web3j.ethBlockHashFlowable().subscribe(hash -> {});
        assertTrue(polled.await(5, TimeUnit.SECONDS), "filter was not polled");

        subscription.dispose();

        assertTrue(subscription.isDisposed());
        assertFilterCancelled();
        assertEquals(Collections.emptyList(), undeliverable);
    }

    static Stream<Arguments> filterFlowables() {
        Function<Web3j, Flowable<?>> blockHashes = Web3j::ethBlockHashFlowable;
        Function<Web3j, Flowable<?>> pendingTransactionHashes =
                Web3j::ethPendingTransactionHashFlowable;
        Function<Web3j, Flowable<?>> logs =
                w -> w.ethLogFlowable(new org.web3j.protocol.core.methods.request.EthFilter());
        return Stream.of(
                Arguments.of("ethBlockHashFlowable", blockHashes),
                Arguments.of("ethPendingTransactionHashFlowable", pendingTransactionHashes),
                Arguments.of("ethLogFlowable", logs));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filterFlowables")
    void disposeFilterFlowableWhenUninstallFails(String name, Function<Web3j, Flowable<?>> flowable)
            throws Exception {
        CountDownLatch polled = new CountDownLatch(2);
        mockNode(polled, UninstallFailure.IO_EXCEPTION);

        Disposable subscription = flowable.apply(web3j).subscribe(item -> {});
        assertTrue(polled.await(5, TimeUnit.SECONDS), "filter was not polled");

        subscription.dispose();

        assertFilterCancelled();
        assertEquals(Collections.emptyList(), undeliverable);
    }

    static Stream<Arguments> lookupFlowables() {
        Function<Web3j, Flowable<?>> blocks = w -> w.blockFlowable(false);
        Function<Web3j, Flowable<?>> transactions = Web3j::transactionFlowable;
        Function<Web3j, Flowable<?>> pendingTransactions = Web3j::pendingTransactionFlowable;
        return Stream.of(
                Arguments.of("blockFlowable", blocks),
                Arguments.of("transactionFlowable", transactions),
                Arguments.of("pendingTransactionFlowable", pendingTransactions));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lookupFlowables")
    void lookupFailureWhenUninstallFails(String name, Function<Web3j, Flowable<?>> flowable)
            throws Exception {
        IOException lookupFailure = new IOException("Lookup failed");
        mockNode(new CountDownLatch(0), UninstallFailure.IO_EXCEPTION, HASH, lookupFailure);

        AtomicReference<Throwable> received = new AtomicReference<>();
        CountDownLatch failed = new CountDownLatch(1);
        flowable.apply(web3j)
                .subscribe(
                        item -> {},
                        error -> {
                            received.set(error);
                            failed.countDown();
                        });

        assertTrue(failed.await(5, TimeUnit.SECONDS), "subscriber did not receive the error");
        assertSame(lookupFailure, received.get());
        // flatMap cancels its upstream before it passes the error on, so this is deterministic
        assertFilterCancelled();
        assertEquals(Collections.emptyList(), undeliverable);
    }

    private void assertFilterCancelled() {
        assertFalse(polling.isEmpty(), "filter was not polled");
        assertTrue(polling.stream().allMatch(Future::isCancelled), "filter is still polled");
        assertEquals(1, uninstalls.get(), "filter was not uninstalled");
    }

    private void mockNode(CountDownLatch polled, UninstallFailure failure) throws IOException {
        mockNode(polled, failure, null, null);
    }

    private void mockNode(
            CountDownLatch polled, UninstallFailure failure, String hash, IOException lookupFailure)
            throws IOException {
        when(web3jService.send(any(Request.class), any()))
                .thenAnswer(
                        invocation -> {
                            Request<?, ?> request = invocation.getArgument(0);
                            switch (request.getMethod()) {
                                case "eth_newFilter":
                                case "eth_newBlockFilter":
                                case "eth_newPendingTransactionFilter":
                                    return objectMapper.readValue(
                                            "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"0x1\"}",
                                            EthFilter.class);
                                case "eth_getFilterLogs":
                                    return ethLog(null);
                                case "eth_getFilterChanges":
                                    polled.countDown();
                                    return ethLog(hash);
                                case "eth_getBlockByHash":
                                case "eth_getTransactionByHash":
                                    throw lookupFailure;
                                case "eth_uninstallFilter":
                                    uninstalls.incrementAndGet();
                                    return uninstall(failure);
                                default:
                                    throw new IOException("Unexpected " + request.getMethod());
                            }
                        });
    }

    private EthLog ethLog(String hash) throws IOException {
        String result = hash == null ? "[]" : "[\"" + hash + "\"]";
        return objectMapper.readValue(
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":" + result + "}", EthLog.class);
    }

    private static EthUninstallFilter uninstall(UninstallFailure failure) throws IOException {
        EthUninstallFilter response = new EthUninstallFilter();
        switch (failure) {
            case NOT_UNINSTALLED:
                response.setResult(false);
                return response;
            case RPC_ERROR:
                response.setError(new Response.Error(-32000, "filter not found"));
                return response;
            case IO_EXCEPTION:
                throw new IOException("Connection refused");
            default:
                throw new ClientConnectionException("Invalid response received: 502");
        }
    }
}
