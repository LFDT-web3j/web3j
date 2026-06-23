# Web3j CLI

Use this file for prompts like:

- `create a new project`
- `audit this contract with web3j`
- `create a wallet`
- `send ether with the web3j cli`

This file is about the external Web3j CLI. Do not present these commands as Java library APIs.

## Core commands

Project scaffold:

```sh
web3j new
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

## Interpretation rules

- `web3j audit` is static Solidity analysis from the CLI toolchain.
- It is not a runtime Java API in this repo.
- `wallet create`, `wallet update`, and `wallet send` are shell workflows.
- If the user wants wallet creation inside Java code instead of the CLI, route them to `WalletUtils.generateBip39Wallet(...)`, `WalletUtils.generateNewWalletFile(...)`, or credential-loading APIs in the library.

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

## Prompt-oriented answers

### `create a new project`

If the user is asking from a terminal-user perspective, answer with:

```sh
web3j new
```

Then mention that if they instead want to add Web3j to an existing Java project, the library path lives in `references/getting-started.md`.

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
