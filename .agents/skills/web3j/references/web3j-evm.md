# Web3j EVM

Use this file for prompts like:

- `what is web3j evm`
- `how do i use web3j evm`
- `run contracts against a local evm with web3j`

## Positioning

`web3j_evm` appears in the official Web3j docs sitemap as a separate documentation area.

Treat it as companion tooling in the broader Web3j ecosystem, not as a core module implemented in this repo.

## Safe boundaries

This repo does not have a clearly named `web3j-evm` module. Do not invent:

- local module coordinates
- APIs
- config blocks
- test harness calls

unless the user provides them from their own project or the dedicated docs are consulted.

## How to help anyway

If the user asks about Web3j EVM, clarify whether they want:

- a lightweight local execution environment
- testing support for contracts and transactions
- integration with existing Web3j Java code

Then connect it back to the main repo concepts they will still use:

- `Web3j` clients
- credentials
- transaction flows
- generated wrappers

## Safe default response

Describe Web3j EVM as a companion ecosystem topic around local or controlled EVM execution for Java-based workflows, then ask which concrete task they want:

- deploy a contract
- run tests
- simulate transactions
