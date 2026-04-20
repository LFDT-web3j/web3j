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

    private static ExecutorService executor;
    private static Thread shutdownHook;

    static {
        initialiseExecutor();
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
                        } catch (Throwable e) {
                            result.completeExceptionally(e);
                        }
                    },
                    executor());
        } catch (RejectedExecutionException e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Shutdown the shared executor used by {@link #run(Callable)}.
     *
     * <p>This is useful for container-managed applications that need to release Web3j resources
     * during undeploy or redeploy without waiting for JVM shutdown. Future calls to {@link
     * #run(Callable)} will create a new shared executor.
     */
    public static synchronized void shutdown() {
        ExecutorService executorService = executor;
        executor = null;
        removeShutdownHook();

        if (executorService != null) {
            shutdown(executorService);
        }
    }

    private static synchronized ExecutorService executor() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            initialiseExecutor();
        }
        return executor;
    }

    private static void initialiseExecutor() {
        executor = Executors.newCachedThreadPool();
        shutdownHook = new Thread(Async::shutdown);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static void removeShutdownHook() {
        if (shutdownHook == null) {
            return;
        }

        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ignored) {
            // The JVM is already shutting down and is running registered hooks.
        }
        shutdownHook = null;
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
                Executors.newScheduledThreadPool(getCpuCount());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(scheduledExecutorService)));

        return scheduledExecutorService;
    }

    /**
     * Shutdown as per {@link ExecutorService} Javadoc recommendation.
     *
     * @param executorService executor service we wish to shut down.
     */
    private static void shutdown(ExecutorService executorService) {
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
