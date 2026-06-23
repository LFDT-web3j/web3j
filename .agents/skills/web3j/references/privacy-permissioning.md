# Privacy And Permissioning

Use this file for prompts like:

- `how do i use privacy with web3j`
- `how do i send a private transaction with besu`
- `how do privacy groups work`
- `what about permissioning in a besu network`

## Scope

In this repo, privacy support is strongly represented by:

- the `besu` module
- the `eea` module
- privacy-focused integration tests under `integration-tests`

Permissioning is not exposed as a first-class Java helper surface in the same way privacy is, but the integration-test network configuration shows how permissioned Besu nodes are configured in practice.

## Primary Java surfaces

For Besu privacy:

- `org.web3j.tx.PrivateTransactionManager`
- `org.web3j.tx.BesuPrivateTransactionManager`
- `org.web3j.tx.LegacyPrivateTransactionManager`
- `org.web3j.tx.gas.BesuPrivacyGasProvider`
- `org.web3j.protocol.besu.Besu`
- `org.web3j.protocol.besu.response.privacy.*`

For EEA/private transaction encoding:

- `org.web3j.protocol.eea.Eea`
- `org.web3j.protocol.eea.crypto.RawPrivateTransaction`
- `org.web3j.protocol.eea.crypto.PrivateTransactionEncoder`
- `org.web3j.utils.Base64String`
- `org.web3j.utils.Restriction`

## Recommended starting point

If the user wants application-level private transactions on Besu, start from:

- a `Besu` client
- enclave public keys / privacy participants
- a `PrivateTransactionManager`
- either a generated wrapper or a manually built `RawPrivateTransaction`

## Repo-grounded examples

Strongest examples:

- `integration-tests/src/test/java/org/web3j/protocol/besu/BesuPrivacyQuickstartIntegrationTest.java`
- `besu/src/main/java/org/web3j/tx/PrivateTransactionManager.java`
- `eea/src/main/java/org/web3j/protocol/eea/crypto/RawPrivateTransaction.java`
- `eea/src/main/java/org/web3j/protocol/eea/crypto/PrivateTransactionEncoder.java`
- `besu/src/main/java/org/web3j/protocol/besu/Besu.java`

The privacy quickstart test demonstrates:

1. creating or retrieving a privacy group
2. building a private contract deployment transaction
3. signing it with `PrivateTransactionEncoder`
4. sending it with `eeaSendRawTransaction`
5. retrieving a private receipt
6. using `PrivateTransactionManager` with generated wrappers

## Permissioning context

For permissioned Besu network configuration, inspect:

- `integration-tests/src/test/resources/quorum-test-network/config/besu/config.toml`

This test setup shows node-permissioning configuration such as:

- `permissions-nodes-config-file-enabled=true`
- a configured permissions file path

Use this as environment context, not as proof of a higher-level Web3j Java abstraction for permissioning.

## Prompt-oriented answers

### `how do i send a private transaction with besu`

Default answer shape:

1. connect with a `Besu` client
2. construct a `PrivateTransactionManager` or `RawPrivateTransaction`
3. supply enclave keys / privacy group details
4. sign using privacy-specific encoding
5. send and wait for a private receipt

If the user wants the exact code pattern, mirror the flow from `BesuPrivacyQuickstartIntegrationTest`.

### `what about permissioning`

Clarify:

- privacy and permissioning are related but different concerns
- this repo provides substantial privacy APIs
- permissioning is more visible here as node/network configuration context than as a dedicated convenience layer
