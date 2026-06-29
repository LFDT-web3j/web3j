# Transactions

Use this file for prompts like:

- `how do transactions work in web3j`
- `how do i sign and send a transaction`
- `how do i use eip-1559`
- `how do i transfer ether`
- `how do i handle nonces and gas`
- `how do logs fit into transactions`
- `how do i subscribe to events with web3j`
- `what about wallet files`
- `what about eip-7702`

## Core transaction surfaces

The main transaction abstractions in this repo are:

- `TransactionManager`
- `RawTransactionManager`
- `ClientTransactionManager`
- `ReadonlyTransactionManager`
- `Transfer`
- `RawTransaction`
- `TransactionEncoder`

The strongest repo-grounded file for local signing and send flow is:

- `core/src/main/java/org/web3j/tx/RawTransactionManager.java`

## Default recommendation

Choose the simplest abstraction that fits:

1. `Transfer.sendFunds(...)` for basic ETH transfers
2. generated contract wrappers for contract creation, calls, and writes
3. `RawTransactionManager` for locally signed general-purpose transactions
4. manual `RawTransaction` plus encoder flow for low-level control

The `v5.0.3` docs explicitly separate transaction topics into:

- Ether transfer
- Smart contract creation and contract interaction
- Wallet files and credentials
- Logs and events
- EIP transaction types, including `EIP-1559`, `EIP-2930`, `EIP-4844`, and `EIP-7702`

## Common patterns

### ETH transfer

```java
Transfer.sendFunds(
        web3j,
        credentials,
        recipientAddress,
        BigDecimal.valueOf(0.2),
        Convert.Unit.ETHER
).send();
```

Repo grounding:

- `integration-tests/src/test/java/org/web3j/protocol/scenarios/SendEtherIT.java`

### Local signing and broadcast

`RawTransactionManager`:

- fetches the pending nonce
- signs locally
- sends via `ethSendRawTransaction`
- verifies local and remote tx hashes

Use it when the user wants explicit signing control without manually assembling every JSON-RPC call.

### Manual raw transaction flow

For the most explicit control:

1. build a `RawTransaction`
2. sign with `TransactionEncoder`
3. hex-encode it
4. call `ethSendRawTransaction`

This pattern is visible in:

- `integration-tests/src/test/java/org/web3j/protocol/scenarios/DeployContractIT.java`
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/SendEtherIT.java`

## Transaction topics to cover when relevant

- credentials and wallet files
- gas price vs max fee / priority fee
- nonces
- transaction receipts
- chain IDs and EIP-155 signing
- EIP-1559 transactions
- EIP-2930 and newer transaction types if the user asks specifically
- contract creation vs contract calls vs read-only `ethCall`
- logs and event decoding after transactions are mined

## Wallet files

If the user asks about wallet files in a transaction context, connect them to:

- `WalletUtils` for wallet generation and credential loading
- `Credentials` for signer material
- CLI wallet workflows only if they explicitly want a shell command

The `v5.0.3` docs expose wallet files as a dedicated transaction topic, so answer wallet-file questions as part of normal transaction setup rather than treating them as a side topic.

## Transactions and smart contracts

The `v5.0.3` docs separate these flows explicitly:

1. transfer Ether
2. create a smart contract
3. transact with a smart contract
4. query contract state with a read-only call

In this repo, the best examples remain:

- `FunctionWrappersIT`
- `SimpleStorageContractIT`
- `DeployContractIT`

Prefer wrappers first, then raw ABI / raw transaction examples.

## Logs and events

The `v5.0.3` docs expose Ethereum logs as a first-class transaction topic.

When the user asks about "what happened after my transaction":

- explain that state-changing transactions often emit logs
- prefer wrapper-generated event response types when wrappers exist
- otherwise decode logs from the receipt with ABI/event utilities

Repo grounding:

- `integration-tests/src/test/java/org/web3j/protocol/scenarios/FunctionWrappersIT.java` shows event extraction patterns from generated wrappers

## Generic event subscriptions and flowables

The `v5.0.3` docs cover two related event-listening paths:

1. polling/filter-based flowables
2. WebSocket pub/sub subscriptions

For generic log subscriptions, the docs show `EthFilter` plus `web3j.ethLogFlowable(filter)`.

Typical pattern:

```java
EthFilter filter =
        new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                contractAddress);

filter.addSingleTopic(EventEncoder.encode(MY_EVENT));

Disposable subscription =
        web3j.ethLogFlowable(filter)
                .subscribe(log -> {
                    // decode log or hand it to wrapper helpers
                });
```

Important notes from the docs:

- topic filters only work on indexed event parameters
- if you omit topics, you can capture all matching logs for the address/range
- filters should be unsubscribed or disposed when no longer needed
- the published docs note that filters are not supported on Infura

For generic chain activity, the docs and local tests also expose:

- `web3j.blockFlowable(...)`
- `web3j.transactionFlowable()`
- `web3j.pendingTransactionFlowable()`
- replay flowables for past blocks and transactions

Repo grounding:

- `integration-tests/src/test/java/org/web3j/protocol/core/FlowableIT.java`
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/EventFilterIT.java`

## WebSocket pub/sub

If the user wants push-style subscriptions instead of polling, the docs route through `WebSocketService`.

High-level shape:

1. connect with `WebSocketService`
2. build `Web3j` from that service
3. subscribe via the WebSocket-backed flowable or notification helper
4. dispose the subscription when done

Use this path for prompts about:

- subscribing to new heads
- pending transactions
- pub/sub over WebSocket
- avoiding polling

## Prompt-oriented answers

### `how do i sign and send a transaction`

Default answer:

- use `RawTransactionManager` if they want a reusable manager abstraction
- use manual `RawTransaction` plus `TransactionEncoder` if they want to see every step

### `how do i use eip-1559`

Point them to:

- `RawTransactionManager.sendEIP1559Transaction(...)`

and explain the required fields:

- chain ID
- max priority fee per gas
- max fee per gas
- gas limit
- target address
- data
- value

If they ask about other transaction types, explain that the official `v5.0.3` docs also cover `EIP-2930`, `EIP-4844`, and `EIP-7702`, and then verify the exact helper or construction path in local source before naming a convenience API that is not already in active use here.

### `how do i transfer ether`

Default to `Transfer.sendFunds(...)` unless the user explicitly wants raw transaction assembly.

### `how do i subscribe to events with web3j`

Default answer:

- for contract events, prefer generated wrapper `...EventFlowable(...)` helpers
- for generic logs, use `EthFilter` plus `web3j.ethLogFlowable(filter)`
- for node-level push subscriptions such as new heads, use a `WebSocketService` path and the Web3j flowables
- always dispose the subscription when finished

### `what about eip-7702`

Default answer shape:

- the `v5.0.3` docs include `EIP-7702` as a dedicated transaction-type topic
- treat it as a specialized transaction flow, not the default transfer or wrapper path
- verify the exact local Web3j helper or transaction-construction API before giving a code sample beyond the high-level flow
