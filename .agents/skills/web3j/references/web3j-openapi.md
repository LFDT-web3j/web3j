# Web3j OpenAPI

Use this file for prompts like:

- `what is web3j openapi`
- `how do i use web3j openapi`
- `generate an openapi service with web3j`
- `how do i run a web3j openapi server`
- `how do i call a web3j openapi server`

## Positioning

`web3j_openapi` is a separately documented Web3j ecosystem surface in the `v5.0.3` docs. It is not one of the main runtime modules in this repo like `core`, `abi`, or `crypto`.

Treat it as generated-project and companion-tooling guidance, not as a core `org.web3j:core` package.

## What the docs support

The `v5.0.3` docs support these broad workflows:

- generate an OpenAPI-oriented project from the CLI
- package or generate server artifacts
- configure runtime parameters through CLI args, config files, or environment variables
- run the generated server with Gradle, an executable distribution, or a JAR
- call the generated server through the Web3j OpenAPI Kotlin/Java client
- handle contract events through Server-Sent Events

Core CLI entry points called out in the docs:

```sh
web3j openapi new
web3j openapi import
web3j openapi jar
web3j openapi generate
```

## Runtime configuration

The docs describe three configuration paths for generated OpenAPI servers:

1. CLI arguments
2. a config file
3. environment variables

Important environment variables from the docs:

- `WEB3J_ENDPOINT`
- `WEB3J_PRIVATE_KEY`
- `WEB3J_WALLET_PATH`
- `WEB3J_WALLET_PASSWORD`
- `WEB3J_OPENAPI_NAME`
- `WEB3J_OPENAPI_CONTRACT_ADDRESSES`
- `WEB3J_OPENAPI_HOST`
- `WEB3J_OPENAPI_PORT`
- `WEB3J_OPENAPI_CONFIG_FILE`

## Build and run paths

The docs show these generated-project run and packaging patterns:

- `./gradlew run`
- `./gradlew shadowJar`
- `./gradlew installShadowDist`

For CLI-generated projects, the command-line docs also show generated tasks such as:

- `./gradlew generateContractWrappers`
- `./gradlew generateWeb3jOpenApi`
- `./gradlew generateWeb3jSwaggerUi`

## Client usage

The `v5.0.3` docs show the OpenAPI client as a separate dependency:

```groovy
implementation "org.web3j.openapi:web3j-openapi-client:5.0.3"
```

The documented client shape is:

- create a `ClientService`
- create the app client with `ClientFactory.create(...)`
- call generated contract resources from the typed API
- use SSE helpers for event subscriptions

## Repo boundary

This repo does not expose a top-level `openapi` module in the same way it exposes `core`, `abi`, or `crypto`.

So:

- do not invent local classes or configuration blocks
- do not pretend `org.web3j:core` directly provides the OpenAPI server/client toolchain
- do treat it as official Web3j companion tooling documented in `v5.0.3`

## Recommended answer pattern

If a user asks how to use Web3j OpenAPI:

1. clarify whether they want server generation, runtime configuration, packaging, or client usage
2. answer with the CLI-generated-project path first
3. separate server-generation guidance from client-consumption guidance
4. only connect back to `org.web3j:core` when the question is about the contract logic behind the generated service

## Safe default response

Describe Web3j OpenAPI as a separately documented Web3j toolchain for generating and running blockchain-backed OpenAPI services, then offer one of these directions:

- scaffold a service
- configure and run the generated server
- use the Kotlin/Java client
- subscribe to contract events via SSE
