/*
 * Copyright 2026 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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
                web3j.newHeadsNotifications()
                        .subscribe(
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
