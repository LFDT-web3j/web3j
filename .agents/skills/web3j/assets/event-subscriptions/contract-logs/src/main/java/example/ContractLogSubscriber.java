package example;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.reactivex.disposables.Disposable;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;

public class ContractLogSubscriber {
    public static void main(String[] args) throws Exception {
        String httpUrl = envOrDefault("WEB3J_HTTP_URL", "http://localhost:8545");
        String contractAddress = requireEnv("WEB3J_CONTRACT_ADDRESS");
        String eventTopic0 = System.getenv("WEB3J_EVENT_TOPIC0");

        Web3j web3j = Web3j.build(new HttpService(httpUrl));

        EthFilter filter =
                new EthFilter(
                        DefaultBlockParameterName.EARLIEST,
                        DefaultBlockParameterName.LATEST,
                        contractAddress);

        if (eventTopic0 != null && !eventTopic0.isBlank()) {
            filter.addSingleTopic(eventTopic0);
        }

        CountDownLatch keepAlive = new CountDownLatch(1);
        Disposable subscription =
                web3j.ethLogFlowable(filter).subscribe(
                        log -> {
                            List<String> topics = log.getTopics();
                            System.out.printf(
                                    "log block=%s tx=%s address=%s topics=%s data=%s%n",
                                    log.getBlockNumber(),
                                    log.getTransactionHash(),
                                    log.getAddress(),
                                    topics,
                                    log.getData());
                        },
                        error -> {
                            error.printStackTrace();
                            keepAlive.countDown();
                        });

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    subscription.dispose();
                                    web3j.shutdown();
                                }));

        System.out.println("Listening for logs on " + contractAddress);
        System.out.println("HTTP endpoint: " + httpUrl);
        if (eventTopic0 != null && !eventTopic0.isBlank()) {
            System.out.println("Topic0 filter: " + eventTopic0);
        } else {
            System.out.println("Topic0 filter: <all logs for contract>");
        }
        System.out.println("Press Ctrl+C to stop.");
        keepAlive.await();
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required environment variable: " + name);
        }
        return value;
    }
}
