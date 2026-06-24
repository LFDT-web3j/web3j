# Web3j CLI

Use this file for prompts like:

- `create a new project`
- `import this project with web3j`
- `audit this contract with web3j`
- `create a wallet`
- `run this generated project`
- `generate an openapi service with web3j`
- `send ether with the web3j cli`

This file is about the external Web3j CLI. Do not present these commands as Java library APIs.

## Core commands

Project scaffold:

```sh
web3j new
```

Import a project into a CLI-managed Web3j layout:

```sh
web3j import
```

OpenAPI project generation and packaging:

```sh
web3j openapi new
web3j openapi import
web3j openapi jar
web3j openapi generate
```

Run a generated project locally:

```sh
web3j run
```

Run a generated project in Docker:

```sh
web3j docker run [-l] <network> <wallet_path> <wallet_password>
```

Create a wallet:

```sh
web3j wallet create
```

Update a wallet password:

```sh
web3j wallet update <walletfile>
```

Send ETH from a wallet:

```sh
web3j wallet send <walletfile> 0x<address>|<ensName>
```

Audit a Solidity contract:

```sh
web3j audit Campaign.sol
```

Generate unit tests from generated wrappers:

```sh
web3j generate tests
```

## Interpretation rules

- `web3j audit` is static Solidity analysis from the CLI toolchain.
- It is not a runtime Java API in this repo.
- `web3j new`, `web3j import`, `web3j run`, and `web3j docker run` are shell workflows around generated projects.
- `web3j openapi ...` commands are CLI workflows for generating or packaging OpenAPI-oriented projects, not core `org.web3j:core` APIs.
- `wallet create`, `wallet update`, and `wallet send` are shell workflows.
- If the user wants wallet creation inside Java code instead of the CLI, route them to `WalletUtils.generateBip39Wallet(...)`, `WalletUtils.generateNewWalletFile(...)`, or credential-loading APIs in the library.

The `v5.0.3` docs also show that `web3j new` supports named templates such as `helloworld`, `erc20`, and `erc777`, and a Kotlin option. Prefer mentioning those capabilities at a high level unless the user asks for the exact interactive flow.

## Library counterpart for wallet tasks

If the user asks for Java code instead of CLI:

- create encrypted wallet file:
  - `WalletUtils.generateNewWalletFile(...)`
  - `WalletUtils.generateLightNewWalletFile(...)`
  - `WalletUtils.generateFullNewWalletFile(...)`
- create BIP-39 wallet:
  - `WalletUtils.generateBip39Wallet(...)`
- load credentials:
  - `WalletUtils.loadCredentials(...)`
  - `WalletUtils.loadBip39Credentials(...)`

Repo grounding:

- `core/src/main/java/org/web3j/crypto/WalletUtils.java`

## Generated-project runtime configuration

The `v5.0.3` CLI docs expose these environment variables for generated projects:

- `WEB3J_ENDPOINT`
- `WEB3J_WALLET_PATH`
- `WEB3J_WALLET_PASSWORD`
- `WEB3J_PRIVATE_KEY`

For generated OpenAPI projects, the docs also show:

- `WEB3J_OPENAPI_NAME`
- `WEB3J_OPENAPI_CONTRACT_ADDRESSES`
- `WEB3J_OPENAPI_HOST`
- `WEB3J_OPENAPI_PORT`

When the user asks how to run the generated project, prefer these generated-project Gradle tasks from the CLI docs:

- `./gradlew build`
- `./gradlew generateContractWrappers`
- `./gradlew generateWeb3jOpenApi`
- `./gradlew generateWeb3jSwaggerUi`
- `./gradlew clean`

## Prompt-oriented answers

### `create a new project`

If the user is asking from a terminal-user perspective, answer with:

```sh
web3j new
```

Then mention:

- `web3j new` is for a CLI scaffold
- `web3j import` is for bringing an existing project into the generated-project workflow
- adding `org.web3j:core` to an existing Java project is covered in `references/getting-started.md`

### `audit this contract with web3j`

Default answer:

```sh
web3j audit MyContract.sol
```

Clarify:

- this performs static analysis on Solidity source
- output includes issue location, severity, description, and rule identifier
- it is useful before deployment, not after a contract is already on-chain

### `create a wallet`

Default terminal answer:

```sh
web3j wallet create
```

If the user wants code instead, switch to the Java wallet APIs from `WalletUtils`.

### `run this generated project`

Default terminal answer:

```sh
web3j run
```

Then mention the generated-project environment variables or the generated Gradle tasks if they need wrapper regeneration or OpenAPI output first.

### `generate an openapi service with web3j`

Default CLI answer:

```sh
web3j openapi new
```

If the user wants runtime configuration, generated JAR packaging, or the Kotlin client surface, route them to `references/web3j-openapi.md`.
