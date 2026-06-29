package example;

import java.net.ConnectException;
import java.util.concurrent.CountDownLatch;

import io.reactivex.disposables.Disposable;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.protocol.websocket.events.NewHead;

public class NewHeadsSubscriber {
    public static void main(String[] args) throws Exception {
        String websocketUrl = envOrDefault("WEB3J_WS_URL", "ws://localhost:8546");

        WebSocketService webSocketService = new WebSocketService(websocketUrl, false);
        connect(webSocketService, websocketUrl);

        Web3j web3j = Web3j.build(webSocketService);

        CountDownLatch keepAlive = new CountDownLatch(1);
        Disposable subscription =
                web3j.newHeadsNotifications().subscribe(
                        notification -> printHead(notification.getParams().getResult()),
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
                                    webSocketService.close();
                                }));

        System.out.println("Subscribed to new heads on " + websocketUrl);
        System.out.println("Press Ctrl+C to stop.");
        keepAlive.await();
    }

    private static void connect(WebSocketService webSocketService, String websocketUrl)
            throws ConnectException {
        webSocketService.connect();
        System.out.println("Connected to " + websocketUrl);
    }

    private static void printHead(NewHead head) {
        System.out.printf(
                "newHead number=%s hash=%s parent=%s%n",
                head.getNumber(), head.getHash(), head.getParentHash());
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
