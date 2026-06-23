---
name: web3j
description: Use this skill when the task involves creating a Java project with Web3j, adding Web3j dependencies, generating or using smart contract wrappers, deploying or interacting with Ethereum smart contracts from Java, creating wallets, or using the Web3j CLI for project scaffolding, wallet management, or Solidity contract auditing.
---

# Web3j

This skill is for AI agents helping users consume the Web3j library and CLI.

Treat Web3j as two separate interfaces:

- Java library usage inside application code
- Web3j CLI usage from the shell

Do not blur these together. If the user asks for code inside a Java project, answer with the library APIs. If the user asks to scaffold a project, audit Solidity, or manage wallets from the terminal, answer with the CLI.

## Routing

- For project setup, dependency selection, module guidance, and "how do I use this library", read [references/getting-started.md](references/getting-started.md).
- For smart contract wrapper generation, deployment, loading, calling, sending transactions, and raw transaction fallbacks, read [references/smart-contracts.md](references/smart-contracts.md).
- For `web3j new`, `web3j wallet create`, `web3j wallet update`, `web3j wallet send`, and `web3j audit`, read [references/cli.md](references/cli.md).
- For Gradle and Maven plugin workflows, read [references/build-plugins.md](references/build-plugins.md).
- For private transactions, Besu/EEA privacy APIs, and permissioned-network configuration context, read [references/privacy-permissioning.md](references/privacy-permissioning.md).
- For transaction managers, signing, gas, nonces, EIP transaction types, wallet files, and ETH transfers, read [references/transactions.md](references/transactions.md).
- For the broader Web3j ecosystem tools named directly by the user, read:
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
  - adding Web3j to an existing Maven or Gradle Java project
- For dependency coordinates and install steps, prefer official published usage from the docs and `README.md`.
- For implementation details, prefer this repo's source and integration tests.
- When showing smart contract usage, default to generated wrappers first and raw ABI / raw transaction flows second.
- When answering audit questions, make clear that `web3j audit` is static analysis from the CLI toolchain, not a Java runtime API.
