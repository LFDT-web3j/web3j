# Smart Contracts

Use this file for prompts like:

- `how do i deploy & interact with smart contracts`
- `using web3j, deploy a contract & call a function`
- `generate wrappers for this solidity contract`
- `call this contract method with web3j`

Prefer generated wrappers first. Use raw transaction and ABI flows only when the user explicitly wants lower-level control or does not have generated wrappers.

## Wrapper generation

The standard flow is:

1. Compile Solidity to `.bin` and `.abi`
2. Generate Java wrappers
3. Use the generated class for deploy, load, call, and transact

Compile with `solc`:

```sh
solc MyContract.sol --bin --abi --optimize -o build/
```

Generate wrappers with the Web3j CLI:

```sh
web3j generate solidity \
  -b build/MyContract.bin \
  -a build/MyContract.abi \
  -o src/main/java \
  -p com.example.contracts
```

Repo grounding:

- `contracts/scripts/generateWrappers.sh` uses the same general flow
- `codegen/src/main/java/org/web3j/codegen/SolidityFunctionWrapperGenerator.java` confirms the supported arguments, including `-a`, `-b`, `-o`, `-p`, `-c`, `-B`, and `-r`

## Recommended deploy/load pattern

Use a `Web3j` client, load `Credentials`, and supply a `ContractGasProvider`.

```java
Web3j web3j = Web3j.build(new HttpService("http://localhost:8545"));
Credentials credentials = WalletUtils.loadCredentials(
        "password",
        "/path/to/keystore.json");
ContractGasProvider gasProvider =
        new StaticGasProvider(
                BigInteger.valueOf(20_000_000_000L),
                BigInteger.valueOf(6_721_975L));
```

Deploy with a generated wrapper:

```java
MyContract contract = MyContract.deploy(
        web3j,
        credentials,
        gasProvider
).send();
```

Load an existing deployment:

```java
MyContract contract = MyContract.load(
        "0xYourContractAddress",
        web3j,
        credentials,
        gasProvider
);
```

## Call vs transact

Read-only call:

```java
BigInteger value = contract.get().send();
```

State-changing transaction:

```java
TransactionReceipt receipt = contract.set(BigInteger.valueOf(1000)).send();
```

Repo grounding:

- `integration-tests/.../SimpleStorageContractIT.java` shows deploy, `set(...)`, and `get()`
- `integration-tests/.../FunctionWrappersIT.java` shows deploy, load, return-value calls, and event extraction

## Lower-level raw deployment and ABI calls

Use this when the user wants raw JSON-RPC style control or no wrapper exists.

Key pieces:

- `RawTransaction.createContractTransaction(...)` for deployment
- `TransactionEncoder.signMessage(...)` for signing
- `FunctionEncoder.encode(...)` for ABI-encoding a function call
- `ethCall(...)` for read-only execution
- `ethSendRawTransaction(...)` for signed writes

Repo grounding:

- `integration-tests/.../DeployContractIT.java` shows raw contract deployment and a follow-up ABI-encoded function call

## ETH transfer alongside contract work

If the user also needs to fund an account or send ETH, prefer:

```java
Transfer.sendFunds(
        web3j,
        credentials,
        recipientAddress,
        BigDecimal.valueOf(0.2),
        Convert.Unit.ETHER
).send();
```

Repo grounding:

- `integration-tests/.../SendEtherIT.java`

## Prompt-oriented answers

### `how do i deploy & interact with smart contracts`

Answer with:

1. Compile Solidity to ABI and BIN
2. Generate wrappers with `web3j generate solidity`
3. Load `Web3j`, `Credentials`, and a gas provider
4. `MyContract.deploy(...).send()`
5. `MyContract.load(...)`
6. Call read methods with `.send()`
7. Send write methods with `.send()` and inspect `TransactionReceipt`

### `using web3j, deploy a contract & call a function`

Default example shape:

```java
Web3j web3j = Web3j.build(new HttpService("http://localhost:8545"));
Credentials credentials = WalletUtils.loadCredentials("password", "/path/to/keystore.json");
ContractGasProvider gasProvider =
        new StaticGasProvider(
                BigInteger.valueOf(20_000_000_000L),
                BigInteger.valueOf(6_721_975L));

MyContract contract = MyContract.deploy(web3j, credentials, gasProvider).send();
BigInteger result = contract.someViewMethod(BigInteger.TEN).send();
```

If the user explicitly says they do not want wrapper generation, switch to the raw deployment and ABI-call pattern from `DeployContractIT`.
