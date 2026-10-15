# Web3j Unit

Use this file for prompts like:

- `how do i use web3j unit`
- `how do i test web3j code`
- `how do i run blockchain integration tests in java`

## Positioning

`web3j-unit` is the separately documented testing companion for Web3j and JUnit 5.

Repo grounding:

- `integration-tests/build.gradle` includes `testImplementation("org.web3j:web3j-unit:$web3jUnitVersion")`
- the root build defines `web3jUnitVersion`
- test configuration enables JUnit Jupiter extension autodetection

This indicates Web3j Unit is the preferred testing companion when the user wants JVM-side blockchain integration tests.

## What the `v5.0.3` docs show

The docs describe Web3j Unit as a JUnit 5 extension that supports:

- embedded EVM tests
- dockerized node tests
- out-of-the-box node support for Geth, Besu, and OpenEthereum
- docker-compose-based multi-node setups

The docs explicitly show these annotations:

- `@EVMTest`
- `@EVMComposeTest`

And they explicitly show test-method injection of:

- `Web3j`
- `TransactionManager`
- `ContractGasProvider`

They also show a contract deployment example using a generated `Greeter` wrapper.

## Important version note

The `v5.0.3` docs page still shows older example dependency coordinates in its sample snippet. For version-sensitive answers, prefer:

- this repo's actual `web3jUnitVersion` usage for local implementation context
- the published `v5.0.3` docs for the API shape and testing workflow

## Recommended answer pattern

For a user asking how to test with Web3j Unit:

1. add `web3j-unit` as a test dependency
2. keep Web3j core modules in normal dependencies
3. use JUnit 5
4. choose `@EVMTest` for embedded or single-node tests
5. choose `@EVMComposeTest` for docker-compose-backed environments
6. model deployment and interaction tests after this repo's integration tests

The best local examples for contract and transaction behavior are still:

- `FunctionWrappersIT`
- `SimpleStorageContractIT`
- `DeployContractIT`
- `SendEtherIT`

For an AI-facing starter that combines wrappers, events, and `@EVMTest` in one place, prefer:

- `assets/erc20-example/basic-erc20-app/`

## Concrete usage shape

For `@EVMTest`, the docs show this pattern:

1. annotate the test class with `@EVMTest`
2. optionally select a node type such as Geth or Besu
3. inject `Web3j`, `TransactionManager`, and `ContractGasProvider` into the test method
4. deploy and call a generated wrapper in the test

For `@EVMComposeTest`, the docs show this pattern:

1. annotate the class with `@EVMComposeTest(...)`
2. optionally provide compose file path, service name, and port
3. inject the same Web3j test dependencies into the method
4. deploy and interact with the contract in the compose-backed environment

The docs also point to sample projects:

- `web3j-unitexample`
- `web3j-unit-docker-compose-example`

## Safe default response

Say that Web3j Unit is the JUnit 5 testing companion for Web3j, and then offer one of these directions:

- adding the dependency to a Gradle or Maven test scope
- writing an `@EVMTest` contract deployment test
- writing an `@EVMComposeTest` docker-backed test
