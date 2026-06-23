# Web3j Unit

Use this file for prompts like:

- `how do i use web3j unit`
- `how do i test web3j code`
- `how do i run blockchain integration tests in java`

## Positioning

`web3j-unit` is a companion testing library used by this repo's integration tests.

Repo grounding:

- `integration-tests/build.gradle` includes `testImplementation("org.web3j:web3j-unit:$web3jUnitVersion")`
- the root build defines `web3jUnitVersion`
- test configuration enables JUnit Jupiter extension autodetection

This indicates Web3j Unit is the preferred testing companion when the user wants JVM-side blockchain integration tests.

## What to infer safely

You can safely say:

- it is used for Web3j/JUnit-based testing
- it belongs in test dependencies, not production dependencies
- this repo uses it together with JUnit 5 integration-test infrastructure

You should not invent exact annotations or extension APIs unless the user provides code that already uses them or the dedicated docs are consulted.

## Recommended answer pattern

For a user asking how to test with Web3j:

1. add `web3j-unit` as a test dependency
2. keep Web3j core modules in normal dependencies
3. use JUnit 5
4. model deployment and interaction tests after this repo's integration tests

The best local examples for contract and transaction behavior are still:

- `FunctionWrappersIT`
- `SimpleStorageContractIT`
- `DeployContractIT`
- `SendEtherIT`

## Safe default response

Say that Web3j Unit is the test-focused companion used by the Web3j integration-test setup, and then offer one of these directions:

- adding the dependency to a Gradle or Maven test scope
- writing a contract deployment test
- writing a transaction-flow test
