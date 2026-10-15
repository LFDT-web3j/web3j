# Events

Use this file for prompts like:

- `how do i subscribe to events with web3j`
- `how do i subscribe to contract events with web3j`
- `how do i watch new blocks with web3j`
- `how do i use flowables with web3j`
- `scaffold me a runnable event-listener example`
- `show me a full contract example with events`

## Decision rule

Decide which event-listening path the user actually needs:

1. contract events from a deployed contract with generated wrappers
2. generic contract logs without wrappers
3. node-level events such as new heads or pending transactions
4. replaying past blocks or transactions

If the user asks for a runnable starter, prefer the closest asset under `../assets/event-subscriptions/`.

## Default recommendations

Prefer these paths in order:

1. generated wrapper `...EventFlowable(...)` helpers for contract events
2. `EthFilter` plus `web3j.ethLogFlowable(filter)` for generic logs
3. `WebSocketService` plus `web3j.newHeadsNotifications()` or other Web3j flowables for push subscriptions

Always dispose the subscription when it is no longer needed.

## Runnable starter assets

Use these templates when the user wants a quick scaffold:

- `assets/event-subscriptions/websocket-newheads/`
  - immediate runnable example for `newHeads` subscriptions over WebSocket
  - best answer for "watch new blocks", "subscribe to new heads", or "show me a running example"
- `assets/event-subscriptions/contract-logs/`
  - runnable generic contract-log listener using `EthFilter`
  - best answer when the user has a contract address and event signature/topic but no generated wrapper yet
- `assets/erc20-example/basic-erc20-app/`
  - end-to-end contract example with wrapper generation, deploy/load/call flow, `@EVMTest`, and typed `Transfer` event subscriptions
  - best answer when the user wants event listening as part of a fuller smart-contract workflow instead of a bare listener

When adapting these assets, copy the nearest template and then only patch:

- project name
- package name if needed
- endpoint defaults
- contract address / topic env vars
- token metadata or transfer defaults for the ERC20 starter

## Wrapper-based contract events

If the user already has a generated wrapper, prefer this path:

1. load the contract wrapper
2. call `...EventFlowable(startBlock, endBlock)` or `...EventFlowable(filter)`
3. subscribe to the returned RxJava `Flowable`
4. dispose the subscription on shutdown

Route to `references/smart-contracts.md` for the wrapper-specific pattern.
Route to `references/erc20-example.md` when the user wants the event subscription embedded in a runnable deploy/interact example.

## Generic logs with EthFilter

If the user does not have wrappers, use:

1. `EthFilter` for block range and address
2. `addSingleTopic(...)` or optional topics when needed
3. `web3j.ethLogFlowable(filter)`
4. raw log printing or manual decoding

Important notes from the docs:

- filters work on indexed topics
- omitting topics broadens the subscription
- the docs note that filters are not supported on Infura

Route to `references/transactions.md` for lower-level filter details.

## WebSocket pub/sub

If the user wants push subscriptions instead of polling:

1. create `WebSocketService`
2. connect it
3. build `Web3j` from that service
4. subscribe via `newHeadsNotifications()` or other flowables

Use the `websocket-newheads` asset as the default runnable starter.

## Repo grounding

Best local sources:

- `integration-tests/src/test/java/org/web3j/protocol/core/FlowableIT.java`
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/EventFilterIT.java`
- generated wrapper event helpers in `contracts/src/main/java/org/web3j/contracts/eip20/generated/ERC20.java`
- generated wrapper event helpers in `contracts/src/main/java/org/web3j/contracts/eip721/generated/ERC721.java`
- wrapper-generation logic in `codegen/src/main/java/org/web3j/codegen/SolidityFunctionWrapper.java`

## Prompt-oriented answers

### `how do i subscribe to events with web3j`

Default answer:

1. if it is a contract event and wrappers exist, use the generated `...EventFlowable(...)` helper
2. otherwise use `EthFilter` plus `web3j.ethLogFlowable(filter)`
3. for node-level push subscriptions such as new heads, use `WebSocketService`
4. if they want a runnable starter, scaffold from `assets/event-subscriptions/`

### `scaffold me a runnable event-listener example`

Default choice:

- use `assets/event-subscriptions/websocket-newheads/` unless the user explicitly asks for contract logs
- use `assets/event-subscriptions/contract-logs/` when they already have a contract address and topic or event signature
- use `assets/erc20-example/basic-erc20-app/` when they want the event subscription to live inside a complete smart-contract example
