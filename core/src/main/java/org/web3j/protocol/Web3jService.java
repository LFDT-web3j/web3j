/*
 * Copyright 2020 Web3 Labs Ltd.
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
package org.web3j.protocol;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import io.reactivex.Flowable;

import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.BatchResponse;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.websocket.events.Notification;

/** Services API. */
public interface Web3jService extends AutoCloseable {

    /**
     * Perform a synchronous JSON-RPC request.
     *
     * @param request request to perform
     * @param responseType class of a data item returned by the request
     * @param <T> type of a data item returned by the request
     * @return deserialized JSON-RPC response
     * @throws IOException thrown if failed to perform a request
     */
    <T extends Response> T send(Request request, Class<T> responseType) throws IOException;

    /**
     * Performs an asynchronous JSON-RPC request.
     *
     * @param request request to perform
     * @param responseType class of a data item returned by the request
     * @param <T> type of a data item returned by the request
     * @return CompletableFuture that will be completed when a result is returned or if a request
     *     has failed
     */
    <T extends Response> CompletableFuture<T> sendAsync(Request request, Class<T> responseType);

    /**
     * Perform a synchronous JSON-RPC batch requests.
     *
     * @param batchRequest requests to perform
     * @return deserialized JSON-RPC responses
     * @throws IOException thrown if failed to perform a batch request
     */
    BatchResponse sendBatch(BatchRequest batchRequest) throws IOException;

    /**
     * Performs an asynchronous JSON-RPC batch requests.
     *
     * @param batchRequest batch request to perform
     * @return CompletableFuture that will be completed when a result is returned or if a request
     *     has failed
     */
    CompletableFuture<BatchResponse> sendBatchAsync(BatchRequest batchRequest);

    /**
     * Subscribe to a stream of notifications. A stream of notifications is opened by by performing
     * a specified JSON-RPC request and is closed by calling the unsubscribe method. Different
     * WebSocket implementations use different pair of subscribe/unsubscribe methods.
     *
     * <p>This method creates an Flowable that can be used to subscribe to new notifications. When a
     * client unsubscribes from this Flowable the service unsubscribes from the underlying stream of
     * events.
     *
     * @param request JSON-RPC request that will be send to subscribe to a stream of events
     * @param unsubscribeMethod method that will be called to unsubscribe from a stream of
     *     notifications
     * @param responseType class of incoming events objects in a stream
     * @param <T> type of incoming event objects
     * @return a {@link Flowable} instance that emits incoming events
     */
    <T extends Notification<?>> Flowable<T> subscribe(
            Request request, String unsubscribeMethod, Class<T> responseType);

    /**
     * Closes resources used by the service.
     *
     * @throws IOException thrown if a service failed to close all resources
     */
    void close() throws IOException;
}
