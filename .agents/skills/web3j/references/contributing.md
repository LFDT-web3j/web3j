# Contributing

Use this file for prompts like:

- `how do i contribute to web3j`
- `i found a bug in web3j`
- `how should i submit a fix`
- `i want to add a feature to web3j`
- `where should i ask questions about the source code`

## Scope

This file is for contributing to the Web3j library and repo itself, not for using Web3j as a dependency in an application.

Primary repo grounding:

- `CONTRIBUTING.md`
- `AGENTS.md`
- `README.md`

## Community entry points

The repo's contribution guidance highlights these community channels:

- the Hyperledger Web3j community page
- the recurring Web3j contributor call
- the Web3j Discord channel under the Hyperledger server

If the user cannot access the Web3j Discord channel directly, the repo guidance says to join the Hyperledger Discord server first and then use the Web3j channel.

## Bug reports

If the user found a bug:

1. search existing GitHub issues first
2. if an issue already exists, add useful details or signal support there
3. if not, open a new issue with:
   - a clear title
   - a clear description
   - relevant context
   - a code sample, or preferably an executable test case

Best contributor guidance:

- prefer a reproducer over a vague narrative
- if the user is already in the repo, suggest placing the reproducer near the relevant module or scenario-style tests

## Bug fixes and patches

If the user already wrote a fix:

1. open a pull request
2. clearly describe the problem and the solution
3. link the related issue when applicable
4. run Spotless formatting before pushing

Required repo-specific reminder:

```sh
./gradlew spotlessApply
```

The contribution guide explicitly warns that the build can fail if formatting is not applied.

Pair that with the repo's normal validation guidance:

- run the narrowest relevant Gradle test task first
- expand to broader validation only when needed

## New features or behavior changes

The contribution guide treats feature work differently from bug fixes.

If the user wants to add a feature or make a behavior change:

1. discuss the idea on Discord first
2. wait for positive feedback
3. only then move toward implementation and PR work

Important boundary from the repo guide:

- do not open a GitHub issue first for speculative feature proposals
- GitHub issues are mainly for bug reports and fixes

## Source-code questions

If the user is asking how the source works or where to discuss implementation ideas:

- direct them to the Web3j Discord channel
- keep GitHub issues focused on actionable bugs/fixes unless maintainers ask otherwise

## Contribution-ready workflow

When an agent is helping with a contribution, the safest default flow is:

1. identify whether it is a bug, patch, feature proposal, or source-code question
2. if bug:
   - search issues
   - build a reproducer or executable test
3. if fix:
   - make the minimal patch
   - run `./gradlew spotlessApply`
   - run narrow tests
   - open a PR with issue context
4. if feature:
   - prepare a short proposal
   - direct the user to Discord first

## Prompt-oriented answers

### `i found a bug in web3j`

Default answer:

1. search GitHub issues first
2. if nothing matches, open a new issue
3. include a minimal code sample or, ideally, an executable test case
4. if they want help reproducing it, build that test in the relevant module

### `how should i submit a fix`

Default answer:

1. make the patch
2. run:

```sh
./gradlew spotlessApply
```

3. run the narrowest relevant tests
4. open a PR describing the problem and solution
5. include the issue number if one exists

### `i want to add a feature to web3j`

Default answer:

1. discuss it on Discord first
2. wait for positive feedback
3. then start implementation

Do not default to "open a GitHub issue first" for feature ideas, because that conflicts with the repo guidance.
