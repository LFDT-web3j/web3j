# ERC20 Example

Use this file for prompts like:

- `show me a basic erc20 example with web3j`
- `create a basic erc20 web app`
- `how do the web3j pieces fit together`
- `show me wrappers tests and events in one example`
- `scaffold an erc20 project with web3j`

## Decision rule

Decide which of these the user actually needs:

1. a CLI-generated starter such as `web3j new` with the `erc20` template
2. a repo-grounded end-to-end example that an agent can copy and adapt quickly
3. an actual HTTP or OpenAPI surface on top of contract logic

If they want the shortest runnable example that also shows tests and events, prefer the local starter asset.

## Default starter

Use:

- `assets/erc20-example/basic-erc20-app/`

This is the default answer for an AI agent when the user wants one example that ties together:

- Solidity source under `src/main/solidity`
- wrapper generation via the Web3j Gradle plugin
- wrapper-based deploy, call, and transfer flows
- typed `Transfer` event subscriptions
- `@EVMTest`-based contract testing

## What this starter covers

The starter is intentionally wrapper-first and repo-grounded.

It shows:

1. an ERC20-like contract with `Transfer` and `Approval` events
2. generated wrappers placed in a fixed package for stable imports
3. a small JVM app that deploys the contract, reads state, sends a transfer, and listens for `Transfer` events
4. a JUnit test using `@EVMTest` to exercise the same flow

Repo grounding for the pattern:

- `integration-tests/src/test/java/org/web3j/protocol/scenarios/HumanStandardTokenGeneratedIT.java`
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/HumanStandardTokenIT.java`
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/FunctionWrappersIT.java`
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/EventFilterIT.java`

## CLI and web-surface routing

If the user asks from a terminal-user perspective, mention that the official CLI docs already expose:

```sh
web3j new
```

and that the published templates include `erc20`.

If the user truly wants an HTTP or browser-facing layer, this starter is still the right contract and test baseline, but route them onward to:

- `references/web3j-openapi.md`

for the generated web surface.

## Sokt boundary

The published `v5.0.3` docs show `web3j-sokt` as a companion compiler-wrapper library that works around:

- `SolidityFile`
- `getCompilerInstance()`
- `SolcArguments`
- `execute(...)`

Use `references/web3j-sokt.md` when the user explicitly wants direct compiler-resolution or native `solc` invocation from code.

The local ERC20 starter keeps the shortest runnable path on the Gradle-plugin side for wrapper generation, and treats Sokt as the companion compilation surface rather than guessing extra local integration code.

## Prompt-oriented answers

### `show me a basic erc20 example with web3j`

Default answer:

1. copy `assets/erc20-example/basic-erc20-app/`
2. run `./gradlew test` for the `@EVMTest` flow
3. run `WEB3J_PRIVATE_KEY=... ./gradlew run` for the deploy/call/event flow
4. adapt token name, symbol, endpoint, and transfer defaults as needed

### `create a basic erc20 web app`

Default answer:

1. start from `assets/erc20-example/basic-erc20-app/` for the contract, wrappers, tests, and events
2. if they mean CLI scaffolding, mention `web3j new` with the `erc20` template
3. if they mean an HTTP layer, extend the same contract flow with `references/web3j-openapi.md`

### `how do the web3j pieces fit together`

Default answer:

1. Solidity contract source defines the ERC20 behavior
2. the Web3j Gradle plugin generates wrappers
3. the Java app deploys and calls the wrapper
4. event flowables provide typed subscriptions
5. Web3j Unit drives the `@EVMTest` contract test
