# Build Plugins

Use this file for prompts like:

- `how do i use the web3j gradle plugin`
- `how do i use the web3j maven plugin`
- `generate wrappers during build`
- `what build plugins does web3j have`

## Scope

Web3j has companion build plugins outside this repo's main Java library modules.

The main plugin surfaces documented by the official docs sitemap are:

- Web3j Gradle plugin
- Web3j Maven plugin
- Solidity Gradle plugin

The root `README.md` also points users to:

- `web3j-maven-plugin`
- `web3j-gradle-plugin`

Use these when the user wants code generation integrated into a build rather than run manually from the CLI.

The `v5.0.3` docs also document generated-project build tasks around the CLI and OpenAPI flows. Treat those as generated-project tasks, not as proof that every task is a generic plugin feature.

## When to recommend plugins

Prefer a build plugin when the user wants:

- contract wrapper generation as part of `build`, `compile`, or codegen phases
- reproducible ABI/BIN-to-wrapper generation in CI
- less manual `solc` plus CLI orchestration

Prefer direct CLI commands when the user wants:

- a one-off wrapper generation
- exploration or debugging
- minimal setup

## Practical guidance

The functional goal of these plugins is the same as the CLI wrapper flow:

1. compile Solidity or consume ABI/BIN artifacts
2. generate Java wrappers
3. place generated sources under the project source tree or generated-sources area

If the user already has a Gradle or Maven project, recommend the plugin that matches their build tool first.

If the user is not tied to a build tool yet, the CLI path is simpler to explain and is covered in `references/smart-contracts.md`.

For generated OpenAPI projects, the documented Gradle tasks include:

- `generateContractWrappers`
- `generateWeb3jOpenApi`
- `generateWeb3jSwaggerUi`

Use those only in the context of the generated project layout shown by the CLI/OpenAPI docs.

## Repo-grounded context

This repo does not contain the plugin implementations themselves. It does contain the underlying code-generation logic and examples:

- `README.md` links the Maven and Gradle plugins
- `contracts/scripts/generateWrappers.sh` shows the CLI-based generation shape
- `codegen/src/main/java/org/web3j/codegen/SolidityFunctionWrapperGenerator.java` shows the supported wrapper-generation inputs

## Prompt-oriented answers

### `what build plugins does web3j have`

Answer with:

- Web3j Gradle plugin
- Web3j Maven plugin
- Solidity Gradle plugin

Then ask whether the user wants:

- wrappers generated during Gradle builds
- wrappers generated during Maven builds
- a one-off CLI alternative

### `generate wrappers during build`

Default answer shape:

1. identify the user's build tool
2. recommend the matching Web3j plugin
3. explain that it automates the same ABI/BIN-to-wrapper flow as `web3j generate solidity`
4. if exact plugin DSL is needed, consult the plugin docs or plugin repo because that configuration lives outside this repo

### `how do i generate openapi outputs during build`

Default answer shape:

1. clarify whether they mean a CLI-generated OpenAPI project or a normal Gradle/Maven build
2. if it is the generated project, point to the documented generated Gradle tasks
3. if they want generic build-plugin DSL, do not invent it from this repo alone
