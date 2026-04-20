/*
 * Copyright 2019 Web3 Labs Ltd.
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
package org.web3j.protocol.core.filters;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.BeforeEach;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthUninstallFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class FilterTester {

    private Web3jService web3jService;
    Web3j web3j;

    final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    @BeforeEach
    public void setUp() {
        web3jService = mock(Web3jService.class);
        web3j = Web3j.build(web3jService, 1000, scheduledExecutorService);
    }

    <T> void runTest(EthLog ethLog, Flowable<T> flowable) throws Exception {
        EthFilter ethFilter =
                objectMapper.readValue(
                        "{\n"
                                + "  \"id\":1,\n"
                                + "  \"jsonrpc\": \"2.0\",\n"
                                + "  \"result\": \"0x1\"\n"
                                + "}",
                        EthFilter.class);

        EthUninstallFilter ethUninstallFilter =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":true}", EthUninstallFilter.class);

        EthLog notFoundFilter =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,"
                                + "\"error\":{\"code\":-32000,\"message\":\"filter not found\"}}",
                        EthLog.class);

        @SuppressWarnings("unchecked")
        List<T> expected = createExpected(ethLog);
        Set<T> results = Collections.synchronizedSet(new HashSet<T>());

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());

        CountDownLatch completedLatch = new CountDownLatch(1);

        when(web3jService.send(any(Request.class), eq(EthFilter.class))).thenReturn(ethFilter);
        when(web3jService.send(any(Request.class), eq(EthLog.class)))
                .thenReturn(ethLog)
                .thenReturn(notFoundFilter)
                .thenReturn(ethLog);
        when(web3jService.send(any(Request.class), eq(EthUninstallFilter.class)))
                .thenReturn(ethUninstallFilter);

        Disposable subscription =
                flowable.subscribe(
                        result -> {
                            results.add(result);
                            transactionLatch.countDown();
                        },
                        throwable -> fail(throwable.getMessage()),
                        () -> completedLatch.countDown());

        transactionLatch.await(1, TimeUnit.SECONDS);
        assertEquals(results, (new HashSet<>(expected)));

        subscription.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(subscription.isDisposed());
    }

    @SuppressWarnings("unchecked")
    <T> List<T> createExpected(EthLog ethLog) {
        List<EthLog.LogResult<?>> logResults = ethLog.getLogs();
        if (logResults.isEmpty()) {
            fail("Results cannot be empty");
        }

        return (List<T>) ethLog.getLogs().stream().map(EthLog.LogResult::get).toList();
    }
}
