# Basic ERC20 App

This starter is the shortest repo-grounded example that ties together:

- Solidity source in `src/main/solidity`
- wrapper generation via the Web3j Gradle plugin
- wrapper-based deploy, call, and transfer flows
- typed `Transfer` event subscriptions
- Web3j Unit `@EVMTest` coverage

It is intentionally a JVM-side starter, not a browser UI. If the user wants an HTTP layer, keep this contract/test flow and add `web3j_openapi` on top.

## Files

- `src/main/solidity/ExampleToken.sol`
  - small ERC20-like contract with `Transfer` and `Approval` events
- `src/main/java/example/Erc20ExampleApp.java`
  - deploys the wrapper, reads token state, performs a transfer, and listens for `Transfer` events
- `src/test/java/example/Erc20EvmTest.java`
  - runs the same pattern under `@EVMTest`
- `build.gradle`
  - applies the Web3j Gradle plugin and fixes generated wrappers to `example.contracts`

## Commands

Run the test path:

```sh
./gradlew test
```

Run the deploy/interact/event path:

```sh
WEB3J_PRIVATE_KEY=0xyourprivatekey ./gradlew run
```

## Environment variables

- `WEB3J_HTTP_URL`
  - defaults to `http://localhost:8545`
- `WEB3J_PRIVATE_KEY`
  - required for `run`
- `WEB3J_TOKEN_NAME`
  - defaults to `Skill Token`
- `WEB3J_TOKEN_SYMBOL`
  - defaults to `SKILL`
- `WEB3J_INITIAL_SUPPLY`
  - defaults to `1000000`
- `WEB3J_TRANSFER_TO`
  - defaults to the deployer address so the transfer path can run without a second account
- `WEB3J_TRANSFER_AMOUNT`
  - defaults to `1`

## How companion tools fit

CLI scaffolding:

- the published CLI docs already expose `web3j new`, and `erc20` is one of the documented templates

Web3j Sokt:

- the published `v5.0.3` docs position Sokt as the direct compiler-resolution and native `solc` helper
- the docs-backed shape is `SolidityFile -> getCompilerInstance() -> execute(...)`
- this starter keeps the shortest runnable path on the Gradle-plugin side for wrapper generation, and leaves direct Sokt wiring as a follow-on step when the user asks for compiler-management code specifically

Web3j Unit and Web3j EVM:

- `Erc20EvmTest` shows the concrete `@EVMTest` flow documented for Web3j Unit
- when a user asks about `web3j-unit` or `web3j-evm`, point them to the test first because it is the most directly runnable path in this starter
