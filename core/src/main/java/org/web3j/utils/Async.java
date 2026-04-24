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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Async task facilitation. */
public class Async {

    private Async() {}

    /**
     * Shared executor used for async operations. Use {@link #shutdown()} to stop it and prevent
     * ClassLoader leaks during web app undeployment.
     */
    private static volatile ExecutorService executor;

    private static ExecutorService getExecutor() {
        ExecutorService result = executor;
        if (result == null || result.isShutdown() || result.isTerminated()) {
            synchronized (Async.class) {
                result = executor;
                if (result == null || result.isShutdown() || result.isTerminated()) {
                    executor =
                            result =
                                    Executors.newCachedThreadPool(
                                            r -> {
                                                Thread t = new Thread(r);
                                                t.setName("web3j-async");
                                                t.setDaemon(true);
                                                return t;
                                            });
                }
            }
        }
        return result;
    }

    /**
     * Stops the shared executor service. Useful in web containers to release threads and prevent
     * ClassLoader leaks.
     */
    public static void shutdown() {
        synchronized (Async.class) {
            if (executor != null && !executor.isShutdown()) {
                shutdown(executor);
                executor = null;
            }
        }
    }

    public static <T> CompletableFuture<T> run(Callable<T> callable) {
        CompletableFuture<T> result = new CompletableFuture<>();
        try {
            CompletableFuture.runAsync(
                    () -> {
                        // we need to explicitly catch any exceptions,
                        // otherwise they will be silently discarded
                        try {
                            result.complete(callable.call());
                        } catch (Throwable throwable) {
                            result.completeExceptionally(throwable);
                        }
                    },
                    getExecutor());
        } catch (RejectedExecutionException e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static int getCpuCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Provide a new ScheduledExecutorService instance.
     *
     * <p>A shutdown hook is created to terminate the thread pool on application termination.
     *
     * @return new ScheduledExecutorService
     */
    public static ScheduledExecutorService defaultExecutorService() {
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(
                        getCpuCount(),
                        r -> {
                            Thread t = new Thread(r);
                            t.setDaemon(true);
                            return t;
                        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(scheduledExecutorService)));

        return scheduledExecutorService;
    }

    /**
     * Shutdown as per {@link ExecutorService} Javadoc recommendation.
     *
     * @param executorService executor service we wish to shut down.
     */
    public static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Thread pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
