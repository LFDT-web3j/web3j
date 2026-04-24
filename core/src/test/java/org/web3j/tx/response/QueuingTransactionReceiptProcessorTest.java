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
package org.web3j.tx.response;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.Web3j;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class QueuingTransactionReceiptProcessorTest {

    @Test
    public void testShutdown() throws Exception {
        Web3j web3j = mock(Web3j.class);
        Callback callback = mock(Callback.class);

        QueuingTransactionReceiptProcessor processor =
                new QueuingTransactionReceiptProcessor(web3j, callback, 10, 1000);

        ScheduledExecutorService executor = getExecutor(processor);

        assertFalse(executor.isShutdown(), "Executor should not be shutdown initially");

        processor.shutdown();

        assertTrue(executor.isShutdown(), "Executor should be shutdown after calling shutdown()");
    }

    @Test
    public void testIdempotentShutdown() throws Exception {
        Web3j web3j = mock(Web3j.class);
        Callback callback = mock(Callback.class);

        QueuingTransactionReceiptProcessor processor =
                new QueuingTransactionReceiptProcessor(web3j, callback, 10, 1000);

        processor.shutdown();
        assertTrue(getExecutor(processor).isShutdown());

        // Calling shutdown again should not throw any exception
        processor.shutdown();
        assertTrue(getExecutor(processor).isShutdown());
    }

    @Test
    public void testClose() throws Exception {
        Web3j web3j = mock(Web3j.class);
        Callback callback = mock(Callback.class);

        ScheduledExecutorService executor;
        try (QueuingTransactionReceiptProcessor processor =
                new QueuingTransactionReceiptProcessor(web3j, callback, 10, 1000)) {
            executor = getExecutor(processor);
            assertFalse(executor.isShutdown());
        }

        assertTrue(executor.isShutdown(), "Executor should be shutdown after try-with-resources block");
    }

    @Test
    public void testDaemonThreads() throws Exception {
        Web3j web3j = mock(Web3j.class);
        Callback callback = mock(Callback.class);

        try (QueuingTransactionReceiptProcessor processor =
                new QueuingTransactionReceiptProcessor(web3j, callback, 10, 1000)) {
            ScheduledExecutorService executor = getExecutor(processor);

            // We submit a task and check the thread properties
            Boolean isDaemon =
                    executor.submit(() -> Thread.currentThread().isDaemon()).get(5, java.util.concurrent.TimeUnit.SECONDS);

            assertTrue(isDaemon, "Threads created by Async.defaultExecutorService() should be daemons");
        }
    }

    private ScheduledExecutorService getExecutor(QueuingTransactionReceiptProcessor processor)
            throws Exception {
        Field executorField =
                QueuingTransactionReceiptProcessor.class.getDeclaredField(
                        "scheduledExecutorService");
        executorField.setAccessible(true);
        return (ScheduledExecutorService) executorField.get(processor);
    }
}
