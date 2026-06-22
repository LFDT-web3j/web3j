/*
 * Copyright 2019 Web3 Labs Ltd.
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
package org.web3j.protocol.geth;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketListener;
import org.web3j.protocol.websocket.WebSocketService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonRpc2_0GethTest {

    private WebSocketClient webSocketClient = mock(WebSocketClient.class);

    private WebSocketService webSocketService = new WebSocketService(webSocketClient, true);

    private Geth geth = Geth.build(webSocketService);

    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private WebSocketListener listener;

    @BeforeEach
    public void before() throws Exception {
        when(webSocketClient.connectBlocking()).thenReturn(true);

        doAnswer(
                        invocation -> {
                            listener = invocation.getArgument(0, WebSocketListener.class);
                            return null;
                        })
                .when(webSocketClient)
                .setListener(any());

        doAnswer(
                        invocation -> {
                            String message = invocation.getArgument(0, String.class);
                            int requestId = getRequestId(message);

                            sendSubscriptionConfirmation(requestId);
                            return null;
                        })
                .when(webSocketClient)
                .send(anyString());

        webSocketService.connect();
    }

    @Test
    public void testPendingTransactionsNotifications() {
        geth.newPendingTransactionsNotifications();

        verify(webSocketClient)
                .send(
                        matches(
                                "\\{\"jsonrpc\":\"2.0\",\"method\":\"eth_subscribe\",\"params\":"
                                        + "\\[\"newPendingTransactions\"],\"id\":[0-9]{1,}}"));
    }

    @Test
    public void testSyncingStatusNotifications() {
        geth.syncingStatusNotifications();

        verify(webSocketClient)
                .send(
                        matches(
                                "\\{\"jsonrpc\":\"2.0\",\"method\":\"eth_subscribe\","
                                        + "\"params\":\\[\"syncing\"],\"id\":[0-9]{1,}}"));
    }

    private int getRequestId(String message) throws IOException {
        JsonNode messageJson = objectMapper.readTree(message);
        return messageJson.get("id").asInt();
    }

    private void sendSubscriptionConfirmation(int requestId) throws IOException {
        listener.onMessage(
                String.format(
                        "{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":%d,"
                                + "\"result\":\"0xcd0c3e8af590364c09d0fa6a1210faf5\""
                                + "}",
                        requestId));
    }
}
