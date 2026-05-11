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
package org.web3j.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsyncTest {

    @Test
    void testRun() throws Exception {
        assertEquals("", Async.run(() -> "").get());
    }

    @Test
    void testRunException() {

        assertThrows(
                ExecutionException.class,
                () -> {
                    Async.run(
                                    () -> {
                                        throw new RuntimeException("");
                                    })
                            .get();
                });
    }

    @Test
    void testAsyncLifecycleReuse() throws ExecutionException, InterruptedException {
        // 1. call Async.run()
        CompletableFuture<String> future1 = Async.run(() -> "first");
        assertEquals("first", future1.get());

        // 2. call Async.shutdown()
        Async.shutdown();

        // 3. call Async.run() again
        CompletableFuture<String> future2 = Async.run(() -> "second");

        // EXPECT: works again, no exception
        assertEquals("second", future2.get());
    }

    @Test
    void testConcurrentCalls() throws Exception {
        int count = 100;
        java.util.List<CompletableFuture<Integer>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            final int index = i;
            futures.add(Async.run(() -> index));
        }
        for (int i = 0; i < count; i++) {
            assertEquals(i, futures.get(i).get());
        }
    }
}
