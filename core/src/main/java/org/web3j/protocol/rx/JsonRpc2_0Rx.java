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
package org.web3j.protocol.rx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.filters.BlockFilter;
import org.web3j.protocol.core.filters.LogFilter;
import org.web3j.protocol.core.filters.PendingTransactionFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Flowables;

/** web3j reactive API implementation. */
public class JsonRpc2_0Rx {

    private static final Logger log = LoggerFactory.getLogger(JsonRpc2_0Rx.class);

    private final Web3j web3j;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Scheduler scheduler;

    public JsonRpc2_0Rx(Web3j web3j, ScheduledExecutorService scheduledExecutorService) {
        this.web3j = web3j;
        this.scheduledExecutorService = scheduledExecutorService;
        this.scheduler = Schedulers.from(scheduledExecutorService);
    }

    public Flowable<String> ethBlockHashFlowable(long pollingInterval) {
        return Flowable.create(
                subscriber -> {
                    BlockFilter blockFilter = new BlockFilter(web3j, subscriber::onNext);
                    run(blockFilter, subscriber, pollingInterval);
                },
                BackpressureStrategy.BUFFER);
    }

    public Flowable<String> ethPendingTransactionHashFlowable(long pollingInterval) {
        return Flowable.create(
                subscriber -> {
                    PendingTransactionFilter pendingTransactionFilter =
                            new PendingTransactionFilter(web3j, subscriber::onNext);

                    run(pendingTransactionFilter, subscriber, pollingInterval);
                },
                BackpressureStrategy.BUFFER);
    }

    public Flowable<Log> ethLogFlowable(
            org.web3j.protocol.core.methods.request.EthFilter ethFilter, long pollingInterval) {
        return Flowable.create(
                subscriber -> {
                    LogFilter logFilter = new LogFilter(web3j, subscriber::onNext, ethFilter);

                    run(logFilter, subscriber, pollingInterval);
                },
                BackpressureStrategy.BUFFER);
    }

    private <T> void run(
            org.web3j.protocol.core.filters.Filter<T> filter,
            FlowableEmitter<? super T> emitter,
            long pollingInterval) {

        filter.run(scheduledExecutorService, pollingInterval);
        emitter.setCancellable(() -> cancel(filter));
    }

    private static void cancel(org.web3j.protocol.core.filters.Filter<?> filter) {
        // The cancellable runs on dispose, when no subscriber can receive an error anymore, so a
        // failure would end up in RxJavaPlugins.onError as an UndeliverableException. The polling
        // is already stopped at this point, and the node expires an unused filter on its own.
        try {
            filter.cancel();
        } catch (RuntimeException e) {
            log.debug("Could not uninstall filter: {}", e.getMessage(), e);
        }
    }

    public Flowable<Transaction> transactionFlowable(long pollingInterval) {
        return blockFlowable(true, pollingInterval).flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<Transaction> pendingTransactionFlowable(long pollingInterval) {
        return ethPendingTransactionHashFlowable(pollingInterval)
                .flatMap(transactionHash -> send(web3j.ethGetTransactionByHash(transactionHash)))
                .filter(ethTransaction -> ethTransaction.getTransaction().isPresent())
                .map(ethTransaction -> ethTransaction.getTransaction().get());
    }

    public Flowable<EthBlock> blockFlowable(boolean fullTransactionObjects, long pollingInterval) {
        return ethBlockHashFlowable(pollingInterval)
                .flatMap(
                        blockHash ->
                                send(web3j.ethGetBlockByHash(blockHash, fullTransactionObjects)));
    }

    private static <T extends Response<?>> Flowable<T> send(Request<?, T> request) {
        // flatMap does not cancel its upstream when a Callable source such as Request.flowable()
        // fails, so the filter would keep polling after the subscriber received the error. hide()
        // makes flatMap subscribe to the request, and a failure then cancels the filter.
        return request.flowable().hide();
    }

    public Flowable<EthBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksFlowable(startBlock, endBlock, fullTransactionObjects, true);
    }

    public Flowable<EthBlock> replayBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects,
            boolean ascending) {
        // We use a scheduler to ensure this Flowable runs asynchronously for users to be
        // consistent with the other Flowables
        return replayBlocksFlowableSync(startBlock, endBlock, fullTransactionObjects, ascending)
                .subscribeOn(scheduler);
    }

    private Flowable<EthBlock> replayBlocksFlowableSync(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return replayBlocksFlowableSync(startBlock, endBlock, fullTransactionObjects, true);
    }

    private Flowable<EthBlock> replayBlocksFlowableSync(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean containsFullTransactionObjects,
            boolean isAscending) {
        BigInteger startBlockNumber;
        BigInteger endBlockNumber;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            endBlockNumber = getBlockNumber(endBlock);
        } catch (IOException e) {
            return Flowable.error(e);
        }

        return Flowables.range(startBlockNumber, endBlockNumber, isAscending)
                .map(DefaultBlockParameterNumber::new)
                .map(number -> web3j.ethGetBlockByNumber(number, containsFullTransactionObjects))
                .flatMap(Request::flowable);
    }

    public Flowable<Transaction> replayTransactionsFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return replayBlocksFlowable(startBlock, endBlock, true)
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            Flowable<EthBlock> onCompleteFlowable) {
        // We use a scheduler to ensure this Flowable runs asynchronously for users to be
        // consistent with the other Flowables
        return replayPastBlocksFlowableSync(startBlock, fullTransactionObjects, onCompleteFlowable)
                .subscribeOn(scheduler);
    }

    public Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return replayPastBlocksFlowable(startBlock, fullTransactionObjects, Flowable.empty());
    }

    private Flowable<EthBlock> replayPastBlocksFlowableSync(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            Flowable<EthBlock> onCompleteFlowable) {

        BigInteger startBlockNumber;
        BigInteger latestBlockNumber;
        try {
            startBlockNumber = getBlockNumber(startBlock);
            latestBlockNumber = getLatestBlockNumber();
        } catch (IOException e) {
            return Flowable.error(e);
        }

        if (startBlockNumber.compareTo(latestBlockNumber) > -1) {
            return onCompleteFlowable;
        } else {
            return Flowable.concat(
                    replayBlocksFlowableSync(
                            new DefaultBlockParameterNumber(startBlockNumber),
                            new DefaultBlockParameterNumber(latestBlockNumber),
                            fullTransactionObjects),
                    Flowable.defer(
                            () ->
                                    replayPastBlocksFlowableSync(
                                            new DefaultBlockParameterNumber(
                                                    latestBlockNumber.add(BigInteger.ONE)),
                                            fullTransactionObjects,
                                            onCompleteFlowable)));
        }
    }

    public Flowable<Transaction> replayPastTransactionsFlowable(DefaultBlockParameter startBlock) {
        return replayPastBlocksFlowable(startBlock, true, Flowable.empty())
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    public Flowable<EthBlock> replayPastAndFutureBlocksFlowable(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            long pollingInterval) {

        return replayPastBlocksFlowable(
                startBlock,
                fullTransactionObjects,
                blockFlowable(fullTransactionObjects, pollingInterval));
    }

    public Flowable<Transaction> replayPastAndFutureTransactionsFlowable(
            DefaultBlockParameter startBlock, long pollingInterval) {
        return replayPastAndFutureBlocksFlowable(startBlock, true, pollingInterval)
                .flatMapIterable(JsonRpc2_0Rx::toTransactions);
    }

    private BigInteger getLatestBlockNumber() throws IOException {
        return getBlockNumber(DefaultBlockParameterName.LATEST);
    }

    private BigInteger getBlockNumber(DefaultBlockParameter defaultBlockParameter)
            throws IOException {
        if (defaultBlockParameter instanceof DefaultBlockParameterNumber) {
            return ((DefaultBlockParameterNumber) defaultBlockParameter).getBlockNumber();
        } else {
            EthBlock latestEthBlock =
                    web3j.ethGetBlockByNumber(defaultBlockParameter, false).send();
            return latestEthBlock.getBlock().getNumber();
        }
    }

    private static List<Transaction> toTransactions(EthBlock ethBlock) {
        // If you ever see an exception thrown here, it's probably due to an incomplete chain in
        // Geth/Parity. You should resync to solve.
        return ethBlock.getBlock().getTransactions().stream()
                .map(transactionResult -> (Transaction) transactionResult.get())
                .collect(Collectors.toList());
    }
}
