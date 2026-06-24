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
package org.web3j.protocol;

import java.util.concurrent.ScheduledExecutorService;

import org.web3j.protocol.core.Batcher;
import org.web3j.protocol.core.BlobFee;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.rx.Web3jRx;

/** JSON-RPC Request object building factory. */
public interface Web3j extends Ethereum, Web3jRx, Batcher, BlobFee, AutoCloseable {

    /**
     * Construct a new Web3j instance.
     *
     * @param web3jService web3j service instance - i.e. HTTP or IPC
     * @return new Web3j instance
     */
    static Web3j build(Web3jService web3jService) {
        return new JsonRpc2_0Web3j(web3jService);
    }

    /**
     * Construct a new Web3j instance.
     *
     * @param web3jService web3j service instance - i.e. HTTP or IPC
     * @param pollingInterval polling interval for responses from network nodes
     * @param scheduledExecutorService executor service to use for scheduled tasks. <strong>You are
     *     responsible for terminating this thread pool</strong>
     * @return new Web3j instance
     */
    static Web3j build(
            Web3jService web3jService,
            long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Web3j(web3jService, pollingInterval, scheduledExecutorService);
    }

    /** Shutdowns a Web3j instance and closes opened resources. */
    void shutdown();

    @Override
    default void close() {
        shutdown();
    }
}
