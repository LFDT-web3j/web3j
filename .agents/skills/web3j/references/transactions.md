# Transactions

Use this file for prompts like:

- `how do transactions work in web3j`
- `how do i sign and send a transaction`
- `how do i use eip-1559`
- `how do i transfer ether`
- `how do i handle nonces and gas`

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
2. generated contract wrappers for contract calls and writes
3. `RawTransactionManager` for locally signed general-purpose transactions
4. manual `RawTransaction` plus encoder flow for low-level control

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

### `how do i transfer ether`

Default to `Transfer.sendFunds(...)` unless the user explicitly wants raw transaction assembly.
