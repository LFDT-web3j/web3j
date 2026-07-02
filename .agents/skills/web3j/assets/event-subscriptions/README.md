# Event Subscription Starters

Use these starter projects when a user asks for a runnable Web3j event-listening example.

## Templates

- `websocket-newheads/`
  - subscribes to new block headers over WebSocket
  - best default runnable example
- `contract-logs/`
  - subscribes to contract logs with `EthFilter`
  - best when the user already has a contract address and event topic

## Agent workflow

When the user asks for a quick scaffold:

1. copy the closest starter into the target directory
2. rename the package or project only if the user cares
3. patch the env-var defaults if the user gave a concrete endpoint or contract
4. explain the exact env vars required to run it

## Runtime expectations

These templates are minimal Gradle Java apps.

Typical run commands:

```sh
gradle run
```

or, if the target project has a wrapper:

```sh
./gradlew run
```

## Environment variables

### `websocket-newheads`

- `WEB3J_WS_URL`

### `contract-logs`

- `WEB3J_HTTP_URL`
- `WEB3J_CONTRACT_ADDRESS`
- `WEB3J_EVENT_TOPIC0` optional
