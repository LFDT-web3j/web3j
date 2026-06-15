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
package org.web3j.protocol.core.filters;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.RpcErrors;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilterReinstallTest {

    @Test
    public void testReinstallFailureDoesNotStopFilter() throws Exception {
        Web3jService web3jService = mock(Web3jService.class);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Web3j web3j = Web3j.build(web3jService, 100, executor);

        EthFilter ethFilter1 = new EthFilter();
        ethFilter1.setResult("0x1");

        EthFilter ethFilter2 = new EthFilter();
        ethFilter2.setResult("0x2");

        EthLog ethLogSuccess = new EthLog();
        ethLogSuccess.setResult(Collections.singletonList(new EthLog.LogObject()));

        EthLog ethLogNotFound = new EthLog();
        ethLogNotFound.setError(
                new org.web3j.protocol.core.Response.Error(
                        RpcErrors.FILTER_NOT_FOUND, "filter not found"));

        // Sequence of returns for EthLog (polling and initial logs)
        when(web3jService.send(any(Request.class), eq(EthLog.class)))
                .thenReturn(ethLogSuccess) // Initial run logs
                .thenReturn(ethLogNotFound) // Poll 1 -> triggers reinstall
                .thenThrow(new IOException("Reinstall failed")) // reinstall logs failure (inside
                // getInitialFilterLogs)
                .thenReturn(ethLogSuccess); // Poll 2 -> success!

        // Sequence of returns for EthFilter (creation)
        when(web3jService.send(any(Request.class), eq(EthFilter.class)))
                .thenReturn(ethFilter1) // Initial
                .thenReturn(ethFilter2); // Reinstall

        CountDownLatch latch = new CountDownLatch(2); // Expected processing events

        LogFilter filter =
                new LogFilter(
                        web3j,
                        log -> latch.countDown(),
                        new org.web3j.protocol.core.methods.request.EthFilter());
        filter.run(executor, 100);

        // We wait for the latch.
        // If the bug exists:
        // 1. reinstallFilter calls schedule.cancel()
        // 2. reinstallFilter calls run()
        // 3. run() fails at getInitialFilterLogs() (throws FilterException)
        // 4. Exception is caught in the poll loop lambda, but the schedule was already cancelled.
        // 5. Final poll never happens. latch stays at 1.

        boolean success = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(success, "Filter should continue polling even after a re-installation failure");
    }
}
