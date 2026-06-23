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

import java.util.List;

import io.reactivex.Flowable;

import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.websocket.events.LogNotification;
import org.web3j.protocol.websocket.events.NewHeadsNotification;
import org.web3j.protocol.websocket.events.PendingTransactionNotification;

/** The Flowables JSON-RPC client event API. */
public interface Web3jRx {

    /**
     * Create an flowable to filter for specific log events on the blockchain.
     *
     * @param ethFilter filter criteria
     * @return a {@link Flowable} instance that emits all Log events matching the filter
     */
    Flowable<Log> ethLogFlowable(EthFilter ethFilter);

    /**
     * Create an Flowable to emit block hashes.
     *
     * @return a {@link Flowable} instance that emits all new block hashes as new blocks are created
     *     on the blockchain
     */
    Flowable<String> ethBlockHashFlowable();

    /**
     * Create an Flowable to emit pending transactions, i.e. those transactions that have been
     * submitted by a node, but don't yet form part of a block (haven't been mined yet).
     *
     * @return a {@link Flowable} instance to emit pending transaction hashes.
     */
    Flowable<String> ethPendingTransactionHashFlowable();

    /**
     * Create an {@link Flowable} instance to emit all new transactions as they are confirmed on the
     * blockchain. i.e. they have been mined and are incorporated into a block.
     *
     * @return a {@link Flowable} instance to emit new transactions on the blockchain
     */
    Flowable<Transaction> transactionFlowable();

    /**
     * Create an {@link Flowable} instance to emit all pending transactions that have yet to be
     * placed into a block on the blockchain.
     *
     * @return a {@link Flowable} instance to emit pending transactions
     */
    Flowable<Transaction> pendingTransactionFlowable();

    /**
     * Create an {@link Flowable} instance that emits newly created blocks on the blockchain.
     *
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *     transaction hashes
     * @return a {@link Flowable} instance that emits all new blocks as they are added to the
     *     blockchain
     */
    Flowable<EthBlock> blockFlowable(boolean fullTransactionObjects);

    /**
     * Create an {@link Flowable} instance that emits all blocks from the blockchain contained
     * within the requested range.
     *
     * @param startBlock block number to commence with
     * @param endBlock block number to finish with
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *     transaction hashes
     * @return a {@link Flowable} instance to emit these blocks
     */
    Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects);

    /**
     * Create an {@link Flowable} instance that emits all blocks from the blockchain contained
     * within the requested range.
     *
     * @param startBlock block number to commence with
     * @param endBlock block number to finish with
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *     transaction hashes
     * @param ascending if true, emits blocks in ascending order between range, otherwise in
     *     descending order
     * @return a {@link Flowable} instance to emit these blocks
     */
    Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects,
            boolean ascending);

    /**
     * Create a {@link Flowable} instance that emits all transactions from the blockchain starting
     * with a provided block number. Once it has replayed up to the most current block, the provided
     * Flowable is invoked.
     *
     * <p>To automatically subscribe to new blocks, use {@link
     * #replayPastAndFutureBlocksFlowable(DefaultBlockParameter, boolean)}.
     *
     * @param startBlock the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *     in the {@link EthBlock} responses
     * @param onCompleteFlowable a subsequent Flowable that we wish to run once we are caught up
     *     with the latest block
     * @return a {@link Flowable} instance to emit all requested blocks
     */
    Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            Flowable<EthBlock> onCompleteFlowable);

    /**
     * Creates a {@link Flowable} instance that emits all blocks from the requested block number to
     * the most current. Once it has emitted the most current block, onComplete is called.
     *
     * @param startBlock the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *     in the {@link EthBlock} responses
     * @return a {@link Flowable} instance to emit all requested blocks
     */
    Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects);

    /**
     * Create a {@link Flowable} instance that emits all transactions from the blockchain contained
     * within the requested range.
     *
     * @param startBlock block number to commence with
     * @param endBlock block number to finish with
     * @return a {@link Flowable} instance to emit these transactions in the order they appear in
     *     the blocks
     */
    Flowable<Transaction> replayPastTransactionsFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock);

    /**
     * Creates a {@link Flowable} instance that emits all transactions from the requested block
     * number to the most current. Once it has emitted the most current block's transactions,
     * onComplete is called.
     *
     * @param startBlock the block number we wish to request from
     * @return a {@link Flowable} instance to emit all requested transactions
     */
    Flowable<Transaction> replayPastTransactionsFlowable(DefaultBlockParameter startBlock);

    /**
     * Creates a {@link Flowable} instance that emits all blocks from the requested block number to
     * the most current. Once it has emitted the most current block, it starts emitting new blocks
     * as they are created.
     *
     * @param startBlock the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *     in the {@link EthBlock} responses
     * @return a {@link Flowable} instance to emit all requested blocks and future
     */
    Flowable<EthBlock> replayPastAndFutureBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects);

    /**
     * As per {@link #replayPastAndFutureBlocksFlowable(DefaultBlockParameter, boolean)}, except
     * that all transactions contained within the blocks are emitted.
     *
     * @param startBlock the block number we wish to request from
     * @return a {@link Flowable} instance to emit all requested transactions and future
     */
    Flowable<Transaction> replayPastAndFutureTransactionsFlowable(DefaultBlockParameter startBlock);

    /**
     * Creates a {@link Flowable} instance that emits a notification when a new header is appended
     * to a chain, including chain reorganizations.
     *
     * @return a {@link Flowable} instance that emits a notification for every new header
     */
    Flowable<NewHeadsNotification> newHeadsNotifications();

    /**
     * Creates aa {@link Flowable} instance that emits notifications for logs included in new
     * imported blocks.
     *
     * @param addresses only return logs from this list of address. Return logs from all addresses
     *     if the list is empty
     * @param topics only return logs that match specified topics. Returns logs for all topics if
     *     the list is empty
     * @return a {@link Flowable} instance that emits logs included in new blocks
     */
    Flowable<LogNotification> logsNotifications(List<String> addresses, List<String> topics);

    /**
     * Creates a {@link Flowable} instance that emits notifications for new pending transactions.
     *
     * @return a {@link Flowable} instance that emits notifications for new pending transactions
     */
    Flowable<PendingTransactionNotification> newPendingTransactionsNotifications();
}
