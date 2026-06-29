# ERC20 Example Starters

Use these starter assets when a user wants one example that shows how Web3j modules and companion tools fit together around an ERC20-like contract.

## Templates

- `basic-erc20-app/`
  - minimal JVM-side app and test suite
  - covers Solidity source, wrapper generation, deploy/load/call flow, typed events, and `@EVMTest`
  - best answer for `show me a basic erc20 example`, `how do the web3j pieces fit together`, or `create a basic erc20 app`

## Agent workflow

When the user asks for a quick scaffold:

1. copy `basic-erc20-app/` into the target directory
2. keep the package names and wrapper package unless the user asks to rename them
3. patch token metadata, endpoint defaults, or transfer env vars only if the user provided concrete values
4. mention `web3j new` if they asked for a CLI-generated template
5. mention `web3j_openapi` only if they want an HTTP layer on top of the same contract flow
