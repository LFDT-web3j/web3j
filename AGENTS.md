# AGENTS.md

## Project overview

Web3j is a multi-module Java library for working with Ethereum and Ethereum-compatible clients from the JVM. This repository is primarily the SDK and code-generation surface, not a single end-user application.

Major modules in this repo:

- `core`: main JSON-RPC client, transactions, credentials, ENS, contract base classes
- `abi`: ABI encoding and decoding
- `codegen`: wrapper generation logic and CLI entrypoints for generating Java/Kotlin wrappers
- `crypto`: keys, wallets, signing, mnemonic helpers
- `contracts`: reusable EIP contract support
- `besu`: Besu-specific APIs, including privacy support
- `eea`: EEA/private transaction support
- `geth`, `parity`, `hosted-providers`, `rlp`, `tuples`, `utils`
- `integration-tests`: JVM integration tests and privacy/network test fixtures

## Setup commands

- Build and run the default verification suite: `./gradlew check`
- Run unit and module tests without integration tests: `./gradlew test`
- Run only integration tests: `./gradlew -Pintegration-tests=true :integration-tests:test`
- Explicitly disable integration tests: `./gradlew -Pintegration-tests=false :test`
- Run a single test class: `./gradlew :integration-tests:test --tests org.web3j.protocol.scenarios.FunctionWrappersIT`
- Run formatting checks: `./gradlew spotlessCheck`
- Apply formatting: `./gradlew spotlessApply`

## Environment and toolchain

- Use Java 21 for current JVM dependency and build compatibility.
- Gradle is the build system; prefer `./gradlew` over a system Gradle install.
- Integration tests may require Docker images and credentials:
  - `registry.username`
  - `registry.password`
- Some shared Gradle fragments are downloaded from `LFDT-web3j/web3j-build-tools` during build setup. Do not remove that behavior unless the task is specifically about build-tool bootstrapping.

## Repo-specific coding guidance

- Prefer modifying the smallest relevant module rather than spreading logic across modules.
- Keep public API changes deliberate. Web3j is consumed as a library, so signature changes and behavior changes should be treated as compatibility-sensitive.
- Follow existing Java style and license header conventions. Spotless is the source of truth for formatting.
- Preserve existing separation between:
  - generic Ethereum behavior in `core`
  - client-specific behavior in `besu`, `geth`, `parity`
  - privacy-specific behavior in `besu` and `eea`
  - code generation behavior in `codegen`

## Canonical code paths

Use these files first when you need working examples:

- `README.md`: published dependency coordinates, Java version note, high-level usage
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/FunctionWrappersIT.java`: generated wrapper deploy/load/call flow
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/SimpleStorageContractIT.java`: simple deploy/write/read wrapper usage
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/DeployContractIT.java`: lower-level raw contract deployment and ABI calls
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/SendEtherIT.java`: ETH transfers and `Transfer.sendFunds`
- `core/src/main/java/org/web3j/tx/RawTransactionManager.java`: local signing and raw transaction broadcast
- `core/src/main/java/org/web3j/crypto/WalletUtils.java`: wallet creation and credential loading
- `integration-tests/src/test/java/org/web3j/protocol/besu/BesuPrivacyQuickstartIntegrationTest.java`: privacy groups and private transaction flows
- `contracts/scripts/generateWrappers.sh`: wrapper-generation command shape
- `codegen/src/main/java/org/web3j/codegen/SolidityFunctionWrapperGenerator.java`: wrapper generation options and behavior

## Library vs CLI boundary

Treat Web3j as two separate surfaces:

1. Java library APIs implemented in this repo
2. Web3j CLI workflows documented externally

When working in this repo:

- Prefer library APIs for code examples inside Java applications.
- Prefer generated wrappers before raw ABI or raw transaction examples.
- Make it explicit when a user request refers to external CLI behavior such as:
  - `web3j new`
  - `web3j wallet create`
  - `web3j wallet update`
  - `web3j wallet send`
  - `web3j audit`

Do not claim those end-user CLI workflows are fully implemented in this repo unless you have verified the exact command path in local source.

## Testing guidance

- Default `test` does not include integration tests unless the `integration-tests` property enables them.
- Prefer targeted tests for the module you changed before running full builds.
- For transaction, wallet, wrapper, or privacy changes, add or update tests near existing scenario-style coverage instead of creating ad hoc examples elsewhere.
- If you change code generation or wrappers, check whether `codegen` tests or generated test resources need to move with the change.

## Security and correctness

- Be careful with signing, nonce, gas, chain ID, and transaction-type behavior. Small changes can create silent downstream breakage.
- Treat privacy-related changes in `besu` and `eea` as high-risk and validate against existing tests and flows.
- Do not introduce hardcoded secrets, RPC credentials, or private keys outside existing test fixtures.
- If modifying wallet or crypto code, prefer small, well-covered changes and preserve file format compatibility where possible.

## Documentation and agent context

- If you change user-facing usage patterns substantially, update `README.md` when appropriate.
- This repo also contains AI-facing skill docs under `.agents/skills/web3j/`. If you materially change supported workflows or recommended usage, keep those references aligned.

## Safe defaults for coding assistants

- Start with `README.md`, `settings.gradle`, and the relevant module `build.gradle` before editing.
- Search with `rg` instead of slower alternatives.
- Prefer precise, minimal patches.
- Run the narrowest relevant Gradle test task you can before finishing.
