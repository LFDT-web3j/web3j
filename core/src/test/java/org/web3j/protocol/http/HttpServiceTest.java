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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthSubscribe;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.protocol.exceptions.RateLimitException;
import org.web3j.protocol.websocket.events.NewHeadsNotification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
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

        HttpService mockedHttpService = mockHttpServiceWithResponse(response);

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
            assertEquals(response.code(), e.getCode());
            return;
        }

        fail("No exception");
    }

    @Test
    void rateLimitIncludesRetryAfterSecondsAndStatusCode() throws IOException {
        Response response =
                buildResponse(429, "rate limited").newBuilder().header("Retry-After", "30").build();

        HttpService rateLimitedService = mockHttpServiceWithResponse(response);

        Request<String, EthBlockNumber> request =
                new Request<>(
                        "eth_blockNumber",
                        Collections.emptyList(),
                        rateLimitedService,
                        EthBlockNumber.class);

        RateLimitException thrown =
                assertThrows(
                        RateLimitException.class,
                        () -> rateLimitedService.send(request, EthBlockNumber.class));
        assertEquals(Integer.valueOf(429), thrown.getCode());
        assertEquals(30L, thrown.getRetryAfterSeconds());
        assertEquals("rate limited", thrown.getResponseBody());
        assertTrue(thrown.getMessage().contains("429"));
    }

    @Test
    void rateLimitCatchableAsClientConnectionExceptionWithCode429() throws IOException {
        Response response =
                buildResponse(429, "too many requests")
                        .newBuilder()
                        .header("retry-after", "12")
                        .build();

        HttpService rateLimitedService = mockHttpServiceWithResponse(response);
        Request<String, EthBlockNumber> request =
                new Request<>(
                        "eth_blockNumber",
                        Collections.emptyList(),
                        rateLimitedService,
                        EthBlockNumber.class);

        ClientConnectionException caught =
                assertThrows(
                        ClientConnectionException.class,
                        () -> rateLimitedService.send(request, EthBlockNumber.class));
        assertInstanceOf(RateLimitException.class, caught);
        assertEquals(Integer.valueOf(429), caught.getCode());
        assertEquals(12L, ((RateLimitException) caught).getRetryAfterSeconds());
    }

    @Test
    void rateLimitWithNullBodyPreservesNullResponseBody() throws IOException {
        Response response =
                new Response.Builder()
                        .code(429)
                        .message("Too Many Requests")
                        .header("Retry-After", "15")
                        .request(new okhttp3.Request.Builder().url(HttpService.DEFAULT_URL).build())
                        .protocol(Protocol.HTTP_1_1)
                        .build();

        HttpService rateLimitedService = mockHttpServiceWithResponse(response);
        Request<String, EthBlockNumber> request =
                new Request<>(
                        "eth_blockNumber",
                        Collections.emptyList(),
                        rateLimitedService,
                        EthBlockNumber.class);

        RateLimitException thrown =
                assertThrows(
                        RateLimitException.class,
                        () -> rateLimitedService.send(request, EthBlockNumber.class));
        assertEquals(Integer.valueOf(429), thrown.getCode());
        assertEquals(15L, thrown.getRetryAfterSeconds());
        assertNull(thrown.getResponseBody());
        assertEquals("Too many requests (429); N/A", thrown.getMessage());
    }

    @Test
    void rateLimitWithMissingRetryAfterHeader() throws IOException {
        HttpService rateLimitedService =
                mockHttpServiceWithResponse(buildResponse(429, "rate limited"));

        Request<String, EthBlockNumber> request =
                new Request<>(
                        "eth_blockNumber",
                        Collections.emptyList(),
                        rateLimitedService,
                        EthBlockNumber.class);

        RateLimitException thrown =
                assertThrows(
                        RateLimitException.class,
                        () -> rateLimitedService.send(request, EthBlockNumber.class));
        assertEquals(Integer.valueOf(429), thrown.getCode());
        assertEquals(0L, thrown.getRetryAfterSeconds());
        assertEquals("rate limited", thrown.getResponseBody());
    }

    @Test
    void rateLimitWithMalformedRetryAfterHeader() throws IOException {
        Response response =
                buildResponse(429, "rate limited")
                        .newBuilder()
                        .header("Retry-After", "not-a-number-or-date")
                        .build();

        HttpService rateLimitedService = mockHttpServiceWithResponse(response);
        Request<String, EthBlockNumber> request =
                new Request<>(
                        "eth_blockNumber",
                        Collections.emptyList(),
                        rateLimitedService,
                        EthBlockNumber.class);

        RateLimitException thrown =
                assertThrows(
                        RateLimitException.class,
                        () -> rateLimitedService.send(request, EthBlockNumber.class));
        assertEquals(0L, thrown.getRetryAfterSeconds());
    }

    @Test
    void rateLimitWithDateBasedRetryAfterHeader() throws IOException {
        ZonedDateTime futureDate = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(120);
        String httpDate = DateTimeFormatter.RFC_1123_DATE_TIME.format(futureDate);
        Response response =
                buildResponse(429, "rate limited")
                        .newBuilder()
                        .header("Retry-After", httpDate)
                        .build();

        HttpService rateLimitedService = mockHttpServiceWithResponse(response);
        Request<String, EthBlockNumber> request =
                new Request<>(
                        "eth_blockNumber",
                        Collections.emptyList(),
                        rateLimitedService,
                        EthBlockNumber.class);

        RateLimitException thrown =
                assertThrows(
                        RateLimitException.class,
                        () -> rateLimitedService.send(request, EthBlockNumber.class));
        assertTrue(
                thrown.getRetryAfterSeconds() >= 115 && thrown.getRetryAfterSeconds() <= 125,
                "Expected ~120s, got " + thrown.getRetryAfterSeconds());
    }

    @Test
    void rateLimitPropagatesThroughBatchAndAsyncAndProcessesHeaders() throws IOException {
        Response response =
                buildResponse(429, "quota exceeded")
                        .newBuilder()
                        .header("Retry-After", "45")
                        .header("X-RateLimit-Remaining", "0")
                        .build();

        AtomicReference<Headers> capturedHeaders = new AtomicReference<>();
        OkHttpClient client = mockClientForResponse(response);
        HttpService service =
                new HttpService(client) {
                    @Override
                    protected void processHeaders(Headers headers) {
                        capturedHeaders.set(headers);
                    }
                };

        BatchRequest batch = new BatchRequest(service);
        batch.add(
                new Request<>(
                        "eth_blockNumber", Collections.emptyList(), service, EthBlockNumber.class));

        RateLimitException batchThrown = assertThrows(RateLimitException.class, batch::send);
        assertEquals(45L, batchThrown.getRetryAfterSeconds());
        assertEquals("0", capturedHeaders.get().get("X-RateLimit-Remaining"));

        Request<String, EthBlockNumber> asyncReq =
                new Request<>(
                        "eth_blockNumber", Collections.emptyList(), service, EthBlockNumber.class);
        CompletionException asyncThrown =
                assertThrows(CompletionException.class, () -> asyncReq.sendAsync().join());
        assertInstanceOf(RateLimitException.class, asyncThrown.getCause());
        assertEquals(45L, ((RateLimitException) asyncThrown.getCause()).getRetryAfterSeconds());
    }

    @Test
    void rateLimitExceptionClampsNegativeDelayAndSetsCode429() {
        RateLimitException ex = new RateLimitException("msg", -5L);
        assertEquals(Integer.valueOf(429), ex.getCode());
        assertEquals(0L, ex.getRetryAfterSeconds());
        assertNull(ex.getResponseBody());
    }

    @Test
    void parseRetryAfterSeconds_null() {
        assertEquals(0L, HttpService.parseRetryAfterSeconds(null));
    }

    @Test
    void parseRetryAfterSeconds_empty() {
        assertEquals(0L, HttpService.parseRetryAfterSeconds(""));
    }

    @Test
    void parseRetryAfterSeconds_whitespaceOnly() {
        assertEquals(0L, HttpService.parseRetryAfterSeconds("   "));
    }

    @Test
    void parseRetryAfterSeconds_validInteger() {
        assertEquals(60L, HttpService.parseRetryAfterSeconds("60"));
    }

    @Test
    void parseRetryAfterSeconds_validIntegerWithWhitespace() {
        assertEquals(45L, HttpService.parseRetryAfterSeconds("  45  "));
    }

    @Test
    void parseRetryAfterSeconds_decimalRoundedUp() {
        assertEquals(3L, HttpService.parseRetryAfterSeconds("2.7"));
        assertEquals(1L, HttpService.parseRetryAfterSeconds("0.001"));
        assertEquals(0L, HttpService.parseRetryAfterSeconds("0.0"));
    }

    @Test
    void parseRetryAfterSeconds_negativeReturnsZero() {
        assertEquals(0L, HttpService.parseRetryAfterSeconds("-10"));
        assertEquals(0L, HttpService.parseRetryAfterSeconds("-0.5"));
    }

    @Test
    void parseRetryAfterSeconds_zero() {
        assertEquals(0L, HttpService.parseRetryAfterSeconds("0"));
    }

    @Test
    void parseRetryAfterSeconds_rejectsJavaFloatAndHexLiterals() {
        assertEquals(0L, HttpService.parseRetryAfterSeconds("NaN"));
        assertEquals(0L, HttpService.parseRetryAfterSeconds("Infinity"));
        assertEquals(0L, HttpService.parseRetryAfterSeconds("-Infinity"));
        assertEquals(0L, HttpService.parseRetryAfterSeconds("10d"));
        assertEquals(0L, HttpService.parseRetryAfterSeconds("10f"));
        assertEquals(0L, HttpService.parseRetryAfterSeconds("0x10p0"));
    }

    @Test
    void parseRetryAfterSeconds_veryLargeValueCappedToMax() {
        assertEquals(
                HttpService.MAX_RETRY_AFTER_SECONDS,
                HttpService.parseRetryAfterSeconds("999999999"));
        assertEquals(
                HttpService.MAX_RETRY_AFTER_SECONDS,
                HttpService.parseRetryAfterSeconds("999999999999999999999999999"));
    }

    @Test
    void parseRetryAfterSeconds_httpDateFormatsAndSubsecondCeiling() {
        Instant now = Instant.parse("2026-11-28T00:00:00.800Z");

        assertEquals(
                300L,
                HttpService.parseRetryAfterSeconds(
                        "Sat, 28 Nov 2026 00:05:00 GMT", Instant.parse("2026-11-28T00:00:00Z")));
        // Round partial seconds up so retries never fire before the HTTP-date boundary
        assertEquals(1L, HttpService.parseRetryAfterSeconds("Sat, 28 Nov 2026 00:00:01 GMT", now));
        assertEquals(
                10L,
                HttpService.parseRetryAfterSeconds(
                        "Sat, 28 Nov 2026 00:00:10 UTC", Instant.parse("2026-11-28T00:00:00Z")));
        assertEquals(
                10L,
                HttpService.parseRetryAfterSeconds(
                        "Saturday, 28-Nov-26 00:00:10 GMT", Instant.parse("2026-11-28T00:00:00Z")));
        assertEquals(
                10L,
                HttpService.parseRetryAfterSeconds(
                        "Sat Nov 28 00:00:10 2026", Instant.parse("2026-11-28T00:00:00Z")));
        assertEquals(
                10L,
                HttpService.parseRetryAfterSeconds(
                        "Tue Dec  1 00:00:10 2026", Instant.parse("2026-12-01T00:00:00Z")));
    }

    @Test
    void parseRetryAfterSeconds_httpDateInPastReturnsZero() {
        Instant now = Instant.parse("2026-11-28T01:00:00Z");
        assertEquals(0L, HttpService.parseRetryAfterSeconds("Sat, 28 Nov 2026 00:00:00 GMT", now));
    }

    @Test
    void parseRetryAfterSeconds_httpDateFarFutureCappedToMax() {
        Instant now = Instant.parse("2026-11-28T00:00:00Z");
        assertEquals(
                HttpService.MAX_RETRY_AFTER_SECONDS,
                HttpService.parseRetryAfterSeconds("Sat, 05 Dec 2026 00:00:00 GMT", now));
    }

    @Test
    void parseRetryAfterSeconds_malformedDateReturnsZero() {
        assertEquals(0L, HttpService.parseRetryAfterSeconds("not-a-date"));
        assertEquals(0L, HttpService.parseRetryAfterSeconds("abc123!@#"));
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

    private static Response buildResponse(int code, String body) {
        return new Response.Builder()
                .code(code)
                .message("")
                .body(ResponseBody.create(body, null))
                .request(new okhttp3.Request.Builder().url(HttpService.DEFAULT_URL).build())
                .protocol(Protocol.HTTP_1_1)
                .build();
    }

    private static HttpService mockHttpServiceWithResponse(Response response) throws IOException {
        return new HttpService(mockClientForResponse(response));
    }

    private static OkHttpClient mockClientForResponse(Response response) throws IOException {
        OkHttpClient httpClient = Mockito.mock(OkHttpClient.class);
        Mockito.when(httpClient.newCall(Mockito.any()))
                .thenAnswer(
                        invocation -> {
                            Call call = Mockito.mock(Call.class);
                            Mockito.when(call.execute()).thenReturn(response);
                            return call;
                        });
        return httpClient;
    }
}
