# Web3j OpenAPI

Use this file for prompts like:

- `what is web3j openapi`
- `how do i use web3j openapi`
- `generate web3 clients from openapi`

## Positioning

`web3j_openapi` appears in the official Web3j docs sitemap as a separate documentation area, which suggests it is a companion Web3j ecosystem surface rather than part of the main `web3j` Java library modules in this repo.

Treat it as adjacent tooling, not as a core package implemented here.

## How to answer

If a user explicitly asks for Web3j OpenAPI:

1. say it is a companion Web3j ecosystem topic documented separately from the core SDK repo
2. clarify whether they want:
   - OpenAPI generation from a blockchain-facing service
   - a generated client workflow
   - integration with an existing Java build
3. keep the answer high level unless the user provides the exact OpenAPI task

## Repo boundary

This repo does not expose a clearly named `openapi` module or implementation surface for that tool.

So:

- do not invent local classes or configuration blocks
- do not pretend the main `web3j` modules here implement OpenAPI support directly
- if exact configuration is needed, consult the dedicated official docs for `web3j_openapi`

## Safe default response

Say that Web3j OpenAPI is a companion tooling area in the wider Web3j ecosystem and ask whether the user wants:

- an overview
- setup help from the dedicated docs
- help integrating generated code into a Java project that already uses Web3j
