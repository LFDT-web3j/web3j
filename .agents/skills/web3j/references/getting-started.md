# Getting Started

Use this file for prompts like:

- `create a new project`
- `how do i use this library`
- `add web3j to my java project`
- `what module do i need`

## Decision rule

Decide which of these the user means:

1. A brand new Web3j project scaffold from the CLI
2. Importing or generating a CLI-managed Web3j project
3. Adding Web3j to an existing Java project
4. Understanding which modules and APIs to use

If the prompt is ambiguous, prefer the narrowest useful answer and mention the alternate path briefly.

## New project via CLI

For a brand new project scaffold, use the Web3j CLI:

```sh
web3j new
```

The `v5.0.3` CLI docs show this as the project-generation entry point for Java or Kotlin scaffolds, including templates such as `helloworld`, `erc20`, and `erc777`.

This is a CLI workflow, not an in-repo Java API.

If the user is clearly asking about importing an existing contract-driven project into a generated Web3j layout, route them to the CLI workflow instead:

```sh
web3j import
```

If the user asks how to install the CLI first, point them to the official Web3j command-line tools docs and the project `README.md`.

## Add Web3j to an existing Java project

The main dependency for standard JVM usage is `org.web3j:core`.

Example Maven dependency from the repo `README.md`:

```xml
<dependency>
  <groupId>org.web3j</groupId>
  <artifactId>core</artifactId>
  <version>5.0.3</version>
</dependency>
```

Example Gradle dependency from the repo `README.md`:

```groovy
implementation('org.web3j:core:5.0.3')
```

Important:

- The repo currently documents Java 21 for current Java binaries.
- This repo itself is at `5.0.3-SNAPSHOT`, so if the user needs a released version, prefer the published coordinates from the docs or `README.md` rather than guessing from local snapshot state.
- If the user is asking for OpenAPI server generation rather than a normal JVM app dependency, route them to `references/web3j-openapi.md`.

## Module guidance

Default module choices:

- `core`: primary JVM entry point for JSON-RPC, credentials, transactions, contract wrappers, ENS, and common Ethereum workflows
- `abi`: low-level ABI encoding and decoding
- `codegen`: wrapper generation utilities
- `crypto`: key, wallet, and signing primitives
- `contracts`: reusable EIP contract support
- `besu`, `geth`, `parity`, `eea`: client-specific or protocol-specific surfaces

Recommend `core` unless the user has a specific low-level need.

## Minimal library usage pattern

For a basic Java integration, the common flow is:

1. Create a `Web3j` client
2. Load `Credentials`
3. Use JSON-RPC APIs directly or use generated contract wrappers

Example:

```java
Web3j web3j = Web3j.build(new HttpService("http://localhost:8545"));
Credentials credentials = WalletUtils.loadCredentials(
        "password",
        "/path/to/keystore.json");
```

From there:

- for smart contracts, route to `references/smart-contracts.md`
- for wallet CLI tasks, route to `references/cli.md`
- for OpenAPI-generated services, route to `references/web3j-openapi.md`

## Prompt-oriented answers

### `create a new project`

If the user wants a Web3j scaffold:

```sh
web3j new
```

If they mean "import or generate a CLI-managed project from existing artifacts", route them to:

```sh
web3j import
```

If they mean "add Web3j to my existing app", give the `core` dependency and a minimal `Web3j.build(...)` example instead.

### `how do i use this library`

Give this structure:

1. Add `org.web3j:core`
2. Create a `Web3j` client
3. Load credentials or connect read-only
4. Use JSON-RPC directly or generate contract wrappers

Point contract questions to `references/smart-contracts.md`.
