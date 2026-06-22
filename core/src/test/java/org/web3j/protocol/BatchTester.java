/*
 * Copyright 2020 Web3 Labs Ltd.
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
package org.web3j.protocol;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.jupiter.api.BeforeEach;
import tools.jackson.databind.node.ArrayNode;

import org.web3j.protocol.http.HttpService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class BatchTester {

    private OkHttpClient httpClient;
    private HttpService httpService;
    public static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse("application/json; charset=utf-8");
    private HttpInterceptor interceptor;

    @BeforeEach
    public void setUp() {
        interceptor = new HttpInterceptor();
        httpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        httpService = new HttpService(httpClient);
        initWeb3Client(httpService);
    }

    protected abstract void initWeb3Client(HttpService httpService);

    protected void buildResponse(String jsonResponse) {
        interceptor.setJsonResponse(jsonResponse);
    }

    protected void verifyResult(String[] expected) throws Exception {
        RequestBody requestBody = interceptor.getRequestBody();
        assertNotNull(requestBody);
        assertEquals(requestBody.contentType(), (HttpService.JSON_MEDIA_TYPE));

        String[] contents = interceptor.getContents();
        assertEquals(contents.length, expected.length);

        for (int i = 0; i < contents.length; i++) {
            assertEquals(replaceRequestId(contents[i]), (replaceRequestId(expected[i])));
        }
    }

    private String replaceRequestId(String json) {
        return json.replaceAll("\"id\":\\d*}$", "\"id\":<generatedValue>}");
    }

    private class HttpInterceptor implements Interceptor {

        private RequestBody requestBody;
        private String[] contents;
        private String jsonResponse;

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            this.requestBody = request.body();

            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            ArrayNode node =
                    (ArrayNode) ObjectMapperFactory.getObjectMapper().readTree(buffer.readUtf8());
            contents = new String[node.size()];

            // if not configured responses, fill with empty responses
            if (jsonResponse == null) {
                StringBuilder responseBody = new StringBuilder("[");

                for (int i = 0; i < node.size(); i++) {
                    contents[i] = node.get(i).toString();
                    responseBody.append(
                            "{\"jsonrpc\":\"2.0\",\"method\":\"\",\"params\":[],\"id\":1}");

                    if (i != node.size() - 1) {
                        responseBody.append(',');
                    }
                }

                jsonResponse = responseBody.append(']').toString();
            }

            return new okhttp3.Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_2)
                    .code(200)
                    .body(ResponseBody.create(jsonResponse, JSON_MEDIA_TYPE))
                    .message("")
                    .build();
        }

        public void setJsonResponse(String jsonResponse) {
            this.jsonResponse = jsonResponse;
        }

        public RequestBody getRequestBody() {
            return requestBody;
        }

        public String[] getContents() {
            return contents;
        }
    }
}
