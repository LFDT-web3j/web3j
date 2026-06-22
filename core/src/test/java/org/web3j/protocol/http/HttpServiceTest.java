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
package org.web3j.protocol.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthSubscribe;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.protocol.websocket.events.NewHeadsNotification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class HttpServiceTest {

    private HttpService httpService = new HttpService();

    @Test
    void testAddHeader() {
        String headerName = "customized_header0";
        String headerValue = "customized_value0";
        httpService.addHeader(headerName, headerValue);
        assertTrue(httpService.getHeaders().get(headerName).equals(headerValue));
    }

    @Test
    void testAddHeaders() {
        String headerName1 = "customized_header1";
        String headerValue1 = "customized_value1";

        String headerName2 = "customized_header2";
        String headerValue2 = "customized_value2";

        HashMap<String, String> headersToAdd = new HashMap<>();
        headersToAdd.put(headerName1, headerValue1);
        headersToAdd.put(headerName2, headerValue2);

        httpService.addHeaders(headersToAdd);

        assertTrue(httpService.getHeaders().get(headerName1).equals(headerValue1));
        assertTrue(httpService.getHeaders().get(headerName2).equals(headerValue2));
    }

    @Test
    void httpWebException() throws IOException {
        String content = "400 error";
        Response response =
                new Response.Builder()
                        .code(400)
                        .message("")
                        .body(ResponseBody.create(content, null))
                        .request(new okhttp3.Request.Builder().url(HttpService.DEFAULT_URL).build())
                        .protocol(Protocol.HTTP_1_1)
                        .build();

        OkHttpClient httpClient = Mockito.mock(OkHttpClient.class);
        Mockito.when(httpClient.newCall(Mockito.any()))
                .thenAnswer(
                        invocation -> {
                            Call call = Mockito.mock(Call.class);
                            Mockito.when(call.execute()).thenReturn(response);

                            return call;
                        });
        HttpService mockedHttpService = new HttpService(httpClient);

        Request<String, EthBlockNumber> request =
                new Request<>(
                        "eth_blockNumber1",
                        Collections.emptyList(),
                        mockedHttpService,
                        EthBlockNumber.class);
        try {
            mockedHttpService.send(request, EthBlockNumber.class);
        } catch (ClientConnectionException e) {
            assertEquals(
                    e.getMessage(),
                    "Invalid response received: " + response.code() + "; " + content);
            assertEquals(
                    response.code(),
                    e.getCode());
            return;
        }

        fail("No exception");
    }

    @Test
    void subscriptionNotSupported() {
        Request<Object, EthSubscribe> subscribeRequest =
                new Request<>(
                        "eth_subscribe",
                        Arrays.asList("newHeads", Collections.emptyMap()),
                        httpService,
                        EthSubscribe.class);
        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        httpService.subscribe(
                                subscribeRequest, "eth_unsubscribe", NewHeadsNotification.class));
    }
}
