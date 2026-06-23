# Web3j Sokt

Use this file for prompts like:

- `what is web3j sokt`
- `how do i use web3j sokt`
- `set up sokt with web3j`

## Positioning

`web3j_sokt` appears in the official Web3j docs sitemap as a separate documentation area.

Treat it as a companion Web3j ecosystem tool, not as something implemented inside this repo's main Java modules.

## Safe boundaries

This repo does not expose a clearly named `sokt` package or module. Because of that:

- do not infer local classes or imports
- do not claim it is part of `core`, `abi`, `crypto`, or `codegen`
- do not invent setup commands beyond what the dedicated docs explicitly provide

## How to answer

If the user explicitly names Web3j Sokt:

1. acknowledge it as a separately documented Web3j ecosystem component
2. ask what they want to do with it if the task is unclear
3. keep the explanation high level unless they provide the exact workflow

## Safe default response

Say that Web3j Sokt is a separately documented companion tool in the Web3j ecosystem, and that exact setup details should come from the dedicated docs for `web3j_sokt` rather than being guessed from this repo.
