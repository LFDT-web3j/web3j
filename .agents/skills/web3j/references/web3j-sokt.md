# Web3j Sokt

Use this file for prompts like:

- `what is web3j sokt`
- `how do i use web3j sokt`
- `set up sokt with web3j`

## Positioning

`web3j_sokt` appears in the official `v5.0.3` docs as a separate documentation area.

Treat it as a companion Web3j ecosystem tool, not as something implemented inside this repo's main Java modules.

## What the `v5.0.3` docs show

The published docs describe Sokt as:

- a Kotlin wrapper around `solc`
- a tool that can resolve the ideal compiler version from the Solidity `pragma`
- a native compiler runner for Mac, Windows, and Linux x86/64

The docs show this dependency coordinate:

- `org.web3j:web3j-sokt:0.2.1`

And they show this concrete usage shape:

1. create a `SolidityFile`
2. call `getCompilerInstance()`
3. execute the compiler with `SolcArguments`
4. inspect `SolcOutput`

## Safe boundaries

This repo still does not expose a clearly named `sokt` module. Because of that:

- do not infer local classes or imports
- do not claim it is part of `core`, `abi`, `crypto`, or `codegen`
- do not invent extra build-tool wiring beyond what the dedicated docs explicitly provide

## How to answer

If the user explicitly names Web3j Sokt:

1. acknowledge it as the dedicated compiler-resolution and `solc` helper
2. ask whether they want direct compiler usage or wrapper generation in a build
3. if they want a runnable wrapper-first project, route them to `references/erc20-example.md`
4. if they want the direct compiler API, answer with the docs-backed `SolidityFile -> getCompilerInstance() -> execute(...)` flow

## Safe default response

Say that Web3j Sokt is the companion compiler wrapper for resolving and running `solc`, and distinguish it from the Gradle-plugin wrapper-generation path used in `assets/erc20-example/basic-erc20-app/`.
