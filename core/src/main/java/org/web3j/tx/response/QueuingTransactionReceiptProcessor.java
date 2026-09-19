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
package org.web3j.tx.response;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.utils.Async;

/**
 * Transaction receipt processor that uses a single thread to query for transaction receipts.
 *
 * <p><em>Note:</em>When initially invoked, this processor returns a transaction receipt containing
 * only the transaction hash of the submitted transaction. This is encapsulated in an {@link
 * EmptyTransactionReceipt}.
 */
public class QueuingTransactionReceiptProcessor extends TransactionReceiptProcessor
        implements AutoCloseable {

    private final int pollingAttemptsPerTxHash;

    private final ScheduledExecutorService scheduledExecutorService;
    private final Callback callback;
    private final BlockingQueue<RequestWrapper> pendingTransactions;

    public QueuingTransactionReceiptProcessor(
            Web3j web3j, Callback callback, int pollingAttemptsPerTxHash, long pollingFrequency) {
        super(web3j);
        this.scheduledExecutorService = Async.defaultExecutorService();
        this.callback = callback;
        this.pendingTransactions = new LinkedBlockingQueue<>();
        this.pollingAttemptsPerTxHash = pollingAttemptsPerTxHash;

        scheduledExecutorService.scheduleAtFixedRate(
                this::sendTransactionReceiptRequests,
                pollingFrequency,
                pollingFrequency,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public TransactionReceipt waitForTransactionReceipt(String transactionHash)
            throws IOException, TransactionException {
        pendingTransactions.add(new RequestWrapper(transactionHash));

        return new EmptyTransactionReceipt(transactionHash);
    }

    /**
     * Terminate the internal executor service. This should be called when the processor is no
     * longer needed, to ensure that threads are properly released and the JVM can shut down.
     */
    public void shutdown() {
        Async.shutdown(scheduledExecutorService);
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }

    private void sendTransactionReceiptRequests() {
        for (RequestWrapper requestWrapper : pendingTransactions) {
            try {
                String transactionHash = requestWrapper.getTransactionHash();
                Optional<? extends TransactionReceipt> transactionReceipt =
                        sendTransactionReceiptRequest(transactionHash);
                if (transactionReceipt.isPresent()) {
                    callback.accept(transactionReceipt.get());
                    pendingTransactions.remove(requestWrapper);
                } else {
                    if (requestWrapper.getCount() == pollingAttemptsPerTxHash) {
                        throw new TransactionException(
                                "No transaction receipt for txHash: "
                                        + transactionHash
                                        + "received after "
                                        + pollingAttemptsPerTxHash
                                        + " attempts",
                                transactionHash);
                    } else {
                        requestWrapper.incrementCount();
                    }
                }
            } catch (IOException | TransactionException e) {
                pendingTransactions.remove(requestWrapper);
                callback.exception(e);
            }
        }
    }

    /**
     * Java doesn't provide a concurrent linked hash set, so we use a simple wrapper to store
     * details of the number of requests we've made against this specific transaction hash. This is
     * so we can preserve submission order as we integrate over the outstanding transactions.
     *
     * <p>Note - the equals/hashcode methods only operate on the transactionHash field. This is
     * intentional.
     */
    private static class RequestWrapper {
        private final String transactionHash;
        private int count;

        RequestWrapper(String transactionHash) {
            this.transactionHash = transactionHash;
            this.count = 0;
        }

        String getTransactionHash() {
            return transactionHash;
        }

        int getCount() {
            return count;
        }

        void incrementCount() {
            this.count += 1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RequestWrapper that = (RequestWrapper) o;

            return transactionHash.equals(that.transactionHash);
        }

        @Override
        public int hashCode() {
            return transactionHash.hashCode();
        }
    }
}
