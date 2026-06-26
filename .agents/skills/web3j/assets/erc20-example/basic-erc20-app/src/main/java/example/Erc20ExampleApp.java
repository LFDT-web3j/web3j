package example;

import example.contracts.ExampleToken;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

public final class Erc20ExampleApp {

    private Erc20ExampleApp() {}

    public static void main(String[] args) throws Exception {
        String endpoint = envOrDefault("WEB3J_HTTP_URL", "http://localhost:8545");
        String privateKey = requiredEnv("WEB3J_PRIVATE_KEY");
        String tokenName = envOrDefault("WEB3J_TOKEN_NAME", "Skill Token");
        String tokenSymbol = envOrDefault("WEB3J_TOKEN_SYMBOL", "SKILL");
        BigInteger initialSupply = new BigInteger(envOrDefault("WEB3J_INITIAL_SUPPLY", "1000000"));
        BigInteger transferAmount = new BigInteger(envOrDefault("WEB3J_TRANSFER_AMOUNT", "1"));

        Web3j web3j = Web3j.build(new HttpService(endpoint));
        Credentials credentials = Credentials.create(privateKey);
        ContractGasProvider gasProvider = new DefaultGasProvider();
        Disposable transferSubscription = null;

        try {
            ExampleToken token =
                    ExampleToken.deploy(
                                    web3j,
                                    credentials,
                                    gasProvider,
                                    initialSupply,
                                    tokenName,
                                    tokenSymbol)
                            .send();

            System.out.println("Contract address: " + token.getContractAddress());
            System.out.println("Token name: " + token.name().send());
            System.out.println("Token symbol: " + token.symbol().send());
            System.out.println("Total supply: " + token.totalSupply().send());
            System.out.println("Deployer balance: " + token.balanceOf(credentials.getAddress()).send());

            CountDownLatch eventSeen = new CountDownLatch(1);
            transferSubscription =
                    token.transferEventFlowable(
                                    DefaultBlockParameterName.EARLIEST,
                                    DefaultBlockParameterName.LATEST)
                            .subscribe(
                                    event -> {
                                        System.out.printf(
                                                "Transfer event: from=%s to=%s value=%s%n",
                                                event._from, event._to, event._value);
                                        eventSeen.countDown();
                                    },
                                    error -> {
                                        error.printStackTrace(System.err);
                                        eventSeen.countDown();
                                    });

            String recipient = envOrDefault("WEB3J_TRANSFER_TO", credentials.getAddress());
            TransactionReceipt receipt = token.transfer(recipient, transferAmount).send();
            System.out.println("Transfer tx hash: " + receipt.getTransactionHash());

            List<ExampleToken.TransferEventResponse> receiptEvents = token.getTransferEvents(receipt);
            if (!receiptEvents.isEmpty()) {
                ExampleToken.TransferEventResponse event = receiptEvents.get(0);
                System.out.printf(
                        "Receipt event: from=%s to=%s value=%s%n",
                        event._from, event._to, event._value);
            }

            System.out.println("Recipient balance: " + token.balanceOf(recipient).send());
            boolean observed = eventSeen.await(10, TimeUnit.SECONDS);
            System.out.println("Flowable observed at least one Transfer event: " + observed);
        } finally {
            if (transferSubscription != null && !transferSubscription.isDisposed()) {
                transferSubscription.dispose();
            }
            web3j.shutdown();
        }
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }
}
