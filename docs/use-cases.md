# Practical Web3j Use Cases

This guide provides simple, practical examples for developers using Web3j to interact with the Ethereum blockchain. These examples are designed to be copy-paste friendly with minimal setup.

## Prerequisites

Before using these examples, ensure you have the Web3j core dependency in your project:

### Maven
```xml
<dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>5.0.2</version>
</dependency>
```

### Gradle
```groovy
implementation 'org.web3j:core:5.0.2'
```

---

## 1. Connect to an Ethereum Node

To interact with Ethereum, you need to connect to a node (either local or via services like Infura or Alchemy).

```java
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class ConnectExample {
    public static void main(String[] args) {
        // Connect to an Ethereum node (e.g., Infura)
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/YOUR_PROJECT_ID"));

        System.out.println("Connected to Ethereum node.");
        web3j.shutdown();
    }
}
```

---

## 2. Get ETH Balance

Retrieve the balance of an Ethereum address in Wei.

```java
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import java.math.BigInteger;

public class GetBalanceExample {
    public static void main(String[] args) throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/YOUR_PROJECT_ID"));

        String address = "0x00000000219ab540356cBB839Cbe05303d7705Fa"; // Example address

        EthGetBalance balanceResponse = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        BigInteger balance = balanceResponse.getBalance();

        System.out.println("Balance for " + address + ": " + balance + " Wei");
        web3j.shutdown();
    }
}
```

---

## 3. Send ETH Transaction

The simplest way to send ETH is using the high-level `Transfer` class.

```java
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import java.math.BigDecimal;

public class SendEthExample {
    public static void main(String[] args) throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/YOUR_PROJECT_ID"));

        // Load your credentials from a private key or wallet file
        Credentials credentials = Credentials.create("YOUR_PRIVATE_KEY");

        // Send 0.01 ETHER to a specific address
        TransactionReceipt transactionReceipt = Transfer.sendFunds(
                web3j, credentials, "0xRECEIVER_ADDRESS",
                BigDecimal.valueOf(0.01), Convert.Unit.ETHER).send();

        System.out.println("Transaction complete: " + transactionReceipt.getTransactionHash());
        web3j.shutdown();
    }
}
```

---

## 4. Read Smart Contract (Static Call)

Interact with a smart contract without sending a transaction (read-only).

```java
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReadContractExample {
    public static void main(String[] args) throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/YOUR_PROJECT_ID"));

        String contractAddress = "0xdAC17F958D2ee523a2206206994597C13D831ec7"; // USDT Address

        // Define the function we want to call (e.g., name())
        Function function = new Function(
                "name",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Utf8String>() {}));

        String encodedFunction = FunctionEncoder.encode(function);

        // Execute the call
        EthCall ethCall = web3j.ethCall(
                Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST).send();

        // Decode the return value
        List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
        if (!results.isEmpty()) {
            System.out.println("Contract Name: " + results.get(0).getValue());
        } else {
            System.out.println("Fail to get contract name.");
        }
        web3j.shutdown();
    }
}
```

---

## Summary of Examples

| Feature | Class / Method | Description |
| :--- | :--- | :--- |
| **Connect** | `Web3j.build()` | Establish a connection to the Ethereum network. |
| **Balance** | `web3j.ethGetBalance()` | Get account balance in Wei. |
| **Transfer**| `Transfer.sendFunds()` | High-level abstraction for simple ETH transfers. |
| **Call**    | `web3j.ethCall()` | Read data from a smart contract (No Gas cost). |
