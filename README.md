Web3j: Web3 Java Ethereum Dapp API
==================================

[![Documentation Status](https://readthedocs.org/projects/web3j-docs/badge/?version=latest)](https://docs.web3j.io)
[![build status](https://github.com/web3j/web3j/actions/workflows/build.yml/badge.svg)](https://github.com/web3j/web3j/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/web3j/web3j/branch/main/graph/badge.svg?token=a4G9ITI6CU)](https://codecov.io/gh/web3j/web3j)
[![Discord](https://img.shields.io/discord/779382027614158919?label=discord)](https://discord.gg/A9UXfPF2tS)
[![Gurubase](https://img.shields.io/badge/Gurubase-Ask%20Web3j%20Guru-006BFF)](https://gurubase.io/g/web3j)


Web3j is a lightweight, highly modular, reactive, type safe Java and
Android library for working with Smart Contracts and integrating with
clients (nodes) on the Ethereum network:

![image](https://github.com/LFDT-web3j/web3j-docs/blob/main/docs/img/web3j_network.png)

This allows you to work with the [Ethereum](https://www.ethereum.org/)
blockchain, without the additional overhead of having to write your own
integration code for the platform.

The [Java and the Blockchain](https://www.youtube.com/watch?v=ea3miXs_P6Y) talk provides
an overview of blockchain, Ethereum and Web3j.

NEW! Get involved!
--------
Since Web3J moved under Hyperledger we started to do Web3J Contributors calls every 2 weeks!
Subscribe to our community page and to see check our call schedule.
Your contribution matters!
- [Community Link](https://lists.hyperledger.org/g/web3j) - Check our last updates! 
- [Calendar Invite](https://lists.hyperledger.org/g/web3j/ics/invite.ics?repeatid=57401) - Add the contributor call to your calendar!

Features
--------

-   Complete implementation of Ethereum's
    [JSON-RPC](https://ethereum.org/en/developers/docs/apis/json-rpc/) client
    API over HTTP and IPC
-   Ethereum wallet support
-   Auto-generation of Java smart contract wrappers to create, deploy,
    transact with and call smart contracts from native Java code
    ([Solidity](http://solidity.readthedocs.io/en/latest/using-the-compiler.html#using-the-commandline-compiler)
    and
    [Truffle](https://github.com/trufflesuite/truffle-contract-schema)
    definition formats supported)
-   Reactive-functional API for working with filters
-   [Ethereum Name Service (ENS)](https://ens.domains/) support
-   Support for Parity's
    [Personal](https://github.com/paritytech/parity/wiki/JSONRPC-personal-module),
    and Geth's
    [Personal](https://github.com/ethereum/go-ethereum/wiki/Management-APIs#personal)
    client APIs
-   Support for [Alchemy](https://docs.alchemyapi.io/alchemy/guides/getting-started#web-3-j) and [Infura](https://infura.io/), so you don't have to run
    an Ethereum client yourself
-   Comprehensive integration tests demonstrating a number of the above
    scenarios
-   Command line tools
-   Android compatible
-   Support for JP Morgan's Quorum via
    [web3j-quorum](https://github.com/web3j/quorum)
-   Support for [EEA Privacy features as described in EEA
    documentation](https://entethalliance.org/technical-documents/) and
    implemented in [Hyperledger
    Besu](https://besu.hyperledger.org/private-networks/reference/api#eea-methods).

It has five runtime dependencies:

-   [RxJava](https://github.com/ReactiveX/RxJava) for its
    reactive-functional API
-   [OKHttp](https://square.github.io/okhttp/)
    for HTTP connections
-   [Jackson Core](https://github.com/FasterXML/jackson-core) for fast
    JSON serialisation/deserialization
-   [Bouncy Castle](https://www.bouncycastle.org/) for
    crypto
-   [Jnr-unixsocket](https://github.com/jnr/jnr-unixsocket) for \*nix
    IPC (not available on Android)
-   [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket)

It also uses [JavaPoet](https://github.com/square/javapoet) for
generating smart contract wrappers.

QuickStart
---------
The simplest way to start your journey with Web3j is to create a project.
We provide this functionality using the [Web3j CLI](http://docs.web3j.io/latest/command_line_tools/). This latter can be installed as follows:

For Unix:

```shell script
curl -L get.web3j.io | sh && source ~/.web3j/source.sh
```

For Windows, in Powershell:

```shell script
Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://raw.githubusercontent.com/hyperledger/web3j-installer/main/installer.ps1'))
```

Create a new project by running:

```shell script
$ web3j new 
```

Or use our [Maven](https://github.com/web3j/web3j-maven-plugin) or 
[Gradle](https://github.com/web3j/web3j-gradle-plugin) plugins to 
generate java files from Solidity contracts.


#### Please head to the [Web3j Documentation](https://docs.web3j.io) for further instructions on using Web3j.

Maven
-----

Java:

```xml
<dependency>
  <groupId>org.web3j</groupId>
  <artifactId>core</artifactId>
  <version>4.14.0</version>
</dependency>
```

**Note:** The Web3j Java binaries are compiled using Java 21. Java 21 or a more recent version is required to use Web3j
 as a dependency.

Android:

```xml
<dependency>
  <groupId>org.web3j</groupId>
  <artifactId>core</artifactId>
  <version>4.12.3-android</version>
</dependency>
```

Gradle
------

Java:

```groovy
implementation ('org.web3j:core:4.14.0')
```

Android:

```groovy
implementation ('org.web3j:core:4.12.3-android')
```

Build instructions
------------------

Web3j includes integration tests for running against a live Ethereum
client. If you do not have a client running, you can exclude their
execution as per the below instructions.

To run a full build (excluding integration tests):

``` {.sourceCode .bash}
$ ./gradlew check
```

To run the integration tests, you will need to set up these variables in order to pull the Docker 
images from the Docker Hub registry:

- `registry.username`
- `registry.password`

Then run the following command:

``` {.sourceCode .bash}
$ ./gradlew -Pintegration-tests=true :integration-tests:test
```

If you do not want the integration test to run:

``` {.sourceCode .bash}
$ ./gradlew -Pintegration-tests=false :test
```

Check the [Docker client API](https://github.com/docker-java/docker-java/blob/master/docs/getting_started.md#instantiating-a-dockerclientconfig)
for more information on configuration options.


Projects using Web3j
------------------

<a href="https://alphawallet.com/">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/alphawallet-dark.svg">
    <img alt="Alphawallet logo" src="static-imgs/alphawallet-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://linea.build">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/linea-dark.svg">
    <img alt="Linea logo" src="static-imgs/linea-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://consensys.io/">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/consensys-dark.svg">
    <img alt="ConsenSys logo" src="static-imgs/consensys-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://ens.domains">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/ens-dark.svg">
    <img alt="ENS logo" src="static-imgs/ens-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://github.com/hyperledger/besu">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/hyperledger-besu-dark.svg">
    <img alt="Hyperledger Besu logo" src="static-imgs/hyperledger-besu-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://web3auth.io">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/web3auth_dark.svg">
    <img alt="Web3Auth logo" src="static-imgs/web3auth_light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://hedera.com">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/hedera-dark.svg">
    <img alt="Hedera logo" src="static-imgs/hedera-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://celo.org">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/celo-dark.svg">
    <img alt="Celo logo" src="static-imgs/celo-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://explorer.walletconnect.com/">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/walletconnect-dark.svg">
    <img alt="WalletConnect logo" src="static-imgs/walletconnect-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://argent.xyz">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/argent-dark.svg">
    <img alt="Argent logo" src="static-imgs/argent-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://unstoppabledomains.com">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/unstoppable_domains-dark.svg">
    <img alt="Unstoppable Domains logo" src="static-imgs/unstoppable_domains-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://www.fnality.org/home">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/fnality-dark.svg">
    <img alt="Fnality logo" src="static-imgs/fnality-light.svg" width="auto" height="50">
  </picture>
</a>

<a href="https://zksync.io/">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="static-imgs/zksync-dark.svg">
    <img alt="zksync logo" src="static-imgs/zksync-light.svg" width="auto" height="50">
  </picture>
</a>

Commercial support and training
-------------------------------

Commercial support and training is available from
[web3labs.com](https://www.web3labs.com/web3j-sdk).

License
------
Apache 2.0
