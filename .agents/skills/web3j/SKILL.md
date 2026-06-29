---
name: web3j
description: Use this skill when the task involves creating a Java project with Web3j, adding Web3j dependencies, generating or using smart contract wrappers, deploying or interacting with Ethereum smart contracts from Java, creating wallets, using the Web3j CLI for project scaffolding or auditing, or working with separately documented Web3j ecosystem tools such as OpenAPI, Unit, EVM, Sokt, or the Eth2 client.
---

# Web3j

This skill is for AI agents helping users consume the Web3j library and CLI.

Treat Web3j as two separate interfaces:

- Java library usage inside application code
- Web3j CLI usage from the shell

Do not blur these together. If the user asks for code inside a Java project, answer with the library APIs. If the user asks to scaffold a project, audit Solidity, or manage wallets from the terminal, answer with the CLI.

## Routing

- For contributing to the Web3j library itself, including bug reports, fixes, feature proposals, formatting, PR expectations, and community discussion paths, read [references/contributing.md](references/contributing.md).
- For project setup, dependency selection, module guidance, and "how do I use this library", read [references/getting-started.md](references/getting-started.md).
- For end-to-end ERC20 examples that show how CLI scaffolding, wrapper generation, interaction, tests, and events fit together, read [references/erc20-example.md](references/erc20-example.md).
- For event subscriptions, filters, flowables, WebSocket pub/sub, and runnable starter examples, read [references/events.md](references/events.md).
- For smart contract wrapper generation, deployment, loading, calling, sending transactions, contract event subscriptions, and raw transaction fallbacks, read [references/smart-contracts.md](references/smart-contracts.md).
- For `web3j new`, `web3j import`, `web3j run`, `web3j docker run`, `web3j wallet create`, `web3j wallet update`, `web3j wallet send`, `web3j audit`, and OpenAPI-oriented CLI scaffolding, read [references/cli.md](references/cli.md).
- For Gradle and Maven plugin workflows, read [references/build-plugins.md](references/build-plugins.md).
- For private transactions, Besu/EEA privacy APIs, and permissioned-network configuration context, read [references/privacy-permissioning.md](references/privacy-permissioning.md).
- For transaction managers, signing, gas, nonces, EIP transaction types, wallet files, ETH transfers, generic log filters, and block/transaction flowables, read [references/transactions.md](references/transactions.md).
- For the broader Web3j ecosystem tools named directly by the user, read:
  - [references/web3j-eth2-client.md](references/web3j-eth2-client.md)
  - [references/web3j-openapi.md](references/web3j-openapi.md)
  - [references/web3j-unit.md](references/web3j-unit.md)
  - [references/web3j-evm.md](references/web3j-evm.md)
  - [references/web3j-sokt.md](references/web3j-sokt.md)

## Repo-grounded examples

When you need canonical implementation patterns, prefer these local sources:

- `README.md` for published dependency coordinates, Java version expectations, and high-level positioning
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/FunctionWrappersIT.java` for generated wrapper deploy/load/call patterns
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/DeployContractIT.java` for lower-level raw contract deployment and ABI calls
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/SendEtherIT.java` for ETH transfers and `Transfer.sendFunds`
- `integration-tests/src/test/java/org/web3j/protocol/core/FlowableIT.java` for block, transaction, pending-transaction, log, and replay flowables
- `integration-tests/src/test/java/org/web3j/protocol/scenarios/EventFilterIT.java` for manual `EthFilter`-based event filtering and log decoding
- `core/src/main/java/org/web3j/crypto/WalletUtils.java` for wallet creation and credential loading APIs
- `core/src/main/java/org/web3j/tx/RawTransactionManager.java` for local signing and raw transaction broadcast patterns
- `integration-tests/build.gradle` for `web3j-unit` usage in this repo's test setup
- `integration-tests/src/test/java/org/web3j/protocol/besu/BesuPrivacyQuickstartIntegrationTest.java` for private transaction and privacy group flows
- `besu/src/main/java/org/web3j/tx/PrivateTransactionManager.java` and `eea/src/main/java/org/web3j/protocol/eea/crypto/RawPrivateTransaction.java` for privacy APIs
- `contracts/scripts/generateWrappers.sh` and `codegen/src/main/java/org/web3j/codegen/SolidityFunctionWrapperGenerator.java` for wrapper-generation CLI shape

## Working rules

- Prefer concise, copyable answers.
- Separate "library code" from "CLI commands" with explicit headings when both are relevant.
- If the user asks to create a new project, first decide whether they mean:
  - a CLI-scaffolded Web3j project
  - a CLI import or generated-project workflow
  - adding Web3j to an existing Maven or Gradle Java project
- If the user asks how to contribute to Web3j itself, first decide whether they mean:
  - report a bug
  - submit a fix
  - propose a feature
  - ask a source-code question
- For dependency coordinates and install steps, prefer official published usage from the docs and `README.md`.
- For implementation details, prefer this repo's source and integration tests.
- When showing smart contract usage, default to generated wrappers first and raw ABI / raw transaction flows second.
- When the user asks for a basic ERC20 example or how Web3j pieces fit together across wrappers, tests, and events, prefer the starter assets under `assets/erc20-example/`.
- When the user asks for a runnable event-listening example, prefer the starter assets under `assets/event-subscriptions/` and adapt the closest template instead of writing one from scratch.
- When answering audit questions, make clear that `web3j audit` is static analysis from the CLI toolchain, not a Java runtime API.
- When answering OpenAPI, Unit, EVM, Sokt, or Eth2 questions, be explicit when the docs describe a companion library or generated project rather than a package implemented in this repo's main modules.
- When helping with repo contributions, follow `CONTRIBUTING.md`: bug issues should include a real reproducer, fixes should be formatted with Spotless, and feature ideas should go through Discord discussion before GitHub issue creation.
