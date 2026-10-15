# Web3j Eth2 Client

Use this file for prompts like:

- `what is web3j eth2 client`
- `how do i use the web3j beacon node client`
- `how do i talk to an eth2 beacon node from java`
- `how do i subscribe to beacon chain events`

## Positioning

The `v5.0.3` docs include `web3j_eth2_client` as a separately documented companion library.

Treat it as an adjacent Web3j ecosystem library, not as part of this repo's main `core` / `abi` / `crypto` module set.

## What the docs show

The docs describe it as a Beacon Node API client for interacting with Eth2 / Beacon Chain nodes through the Eth2 API specification, including event subscriptions.

The documented artifact is separate from `org.web3j:core`:

```xml
<dependency>
    <groupId>org.web3j.eth2</groupId>
    <artifactId>beacon-node-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

Important:

- this dependency is versioned separately from `org.web3j:core:5.0.3`
- do not replace it with core Web3j coordinates

## Basic client shape

The docs show this usage pattern:

1. create a `BeaconNodeService`
2. build the client with `BeaconNodeClientFactory.build(...)`
3. call typed Beacon API resources from the client

Example shape from the docs:

```java
var service = new BeaconNodeService("http://...");
var client = BeaconNodeClientFactory.build(service);
```

Then use the Beacon API resources, for example:

- `client.getBeacon().getBlocks().findById(...)`
- `client.getBeacon().getPool().getAttesterSlashings().findAll()`

## Event subscriptions

The docs explicitly describe event subscriptions as part of the client surface.

Documented high-level flow:

1. create the client
2. choose event topics
3. subscribe with the event API
4. wait for callbacks or latch completion in test/demo code

If the user asks about listening for events, explain that the docs show Beacon event subscriptions as a native capability of this client rather than something built on the main `Web3j` JSON-RPC client.

## Local test network context

The docs show a local Teku-based test-network example for trying the Beacon client against local nodes.

Use that as ecosystem guidance, not as a statement that this repo contains the Eth2 test-network implementation.

## Repo boundary

This repo does not contain a top-level `web3j-eth2` module implementing this client.

So:

- do not invent local imports or modules from this repo
- do not claim `org.web3j:core` directly exposes Beacon Node APIs
- do use the official `v5.0.3` docs guidance when the user explicitly asks about the Eth2 client

## Safe default response

Describe the Web3j Eth2 client as a separate Beacon Node API client library, then offer one of these directions:

- add the `beacon-node-api` dependency
- create a client and query Beacon data
- subscribe to Beacon events
- connect the client to a local test network
