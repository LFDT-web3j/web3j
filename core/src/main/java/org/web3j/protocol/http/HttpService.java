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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.protocol.Service;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.protocol.exceptions.RateLimitException;

import static okhttp3.ConnectionSpec.CLEARTEXT;

/** HTTP implementation of our services API. */
public class HttpService extends Service {

    /** Copied from {@link ConnectionSpec#APPROVED_CIPHER_SUITES}. */
    @SuppressWarnings("JavadocReference")
    private static final CipherSuite[] INFURA_CIPHER_SUITES =
            new CipherSuite[] {
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,

                // Note that the following cipher suites are all on HTTP/2's bad cipher suites list.
                // We'll
                // continue to include them until better suites are commonly available. For example,
                // none
                // of the better cipher suites listed above shipped with Android 4.4 or Java 7.
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,

                // Additional INFURA CipherSuites
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256
            };

    private static final ConnectionSpec INFURA_CIPHER_SUITE_SPEC =
            new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .cipherSuites(INFURA_CIPHER_SUITES)
                    .build();

    /** The list of {@link ConnectionSpec} instances used by the connection. */
    private static final List<ConnectionSpec> CONNECTION_SPEC_LIST =
            Arrays.asList(INFURA_CIPHER_SUITE_SPEC, CLEARTEXT);

    public static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse("application/json; charset=utf-8");

    public static final String DEFAULT_URL = "http://localhost:8545/";

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private static final String RETRY_AFTER_HEADER = "Retry-After";

    /**
     * Upper bound applied to parsed {@code Retry-After} values to guard against
     * buggy or adversarial servers stalling callers indefinitely.
     */
    static final long MAX_RETRY_AFTER_SECONDS = 86_400L;

    private static final Logger log = LoggerFactory.getLogger(HttpService.class);

    private OkHttpClient httpClient;

    private final String url;

    private HashMap<String, String> headers = new HashMap<>();

    public HttpService(String url, OkHttpClient httpClient, boolean includeRawResponses) {
        super(includeRawResponses);
        this.url = url;
        this.httpClient = httpClient;
    }

    public HttpService(OkHttpClient httpClient, boolean includeRawResponses) {
        this(DEFAULT_URL, httpClient, includeRawResponses);
    }

    public HttpService(String url, OkHttpClient httpClient) {
        this(url, httpClient, false);
    }

    public HttpService(String url) {
        this(url, createOkHttpClient());
    }

    public HttpService(String url, boolean includeRawResponse) {
        this(url, createOkHttpClient(), includeRawResponse);
    }

    public HttpService(OkHttpClient httpClient) {
        this(DEFAULT_URL, httpClient);
    }

    public HttpService(boolean includeRawResponse) {
        this(DEFAULT_URL, includeRawResponse);
    }

    public HttpService() {
        this(DEFAULT_URL);
    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        final OkHttpClient.Builder builder =
                new OkHttpClient.Builder().connectionSpecs(CONNECTION_SPEC_LIST);
        configureLogging(builder);
        return builder;
    }

    private static OkHttpClient createOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

    private static void configureLogging(OkHttpClient.Builder builder) {
        if (log.isDebugEnabled()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::debug);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }

    @Override
    protected InputStream performIO(String request) throws IOException {

        RequestBody requestBody = RequestBody.create(request, JSON_MEDIA_TYPE);
        Headers headers = buildHeaders();

        okhttp3.Request httpRequest =
                new okhttp3.Request.Builder().url(url).headers(headers).post(requestBody).build();

        try (okhttp3.Response response = httpClient.newCall(httpRequest).execute()) {
            processHeaders(response.headers());
            ResponseBody responseBody = response.body();
            if (response.isSuccessful()) {
                if (responseBody != null) {
                    return buildInputStream(responseBody);
                } else {
                    return null;
                }
            } else {
                int code = response.code();
                String text = responseBody == null ? "N/A" : responseBody.string();

                if (code == HTTP_TOO_MANY_REQUESTS) {
                    long retryAfterSeconds =
                            parseRetryAfterSeconds(
                                    response.headers().get(RETRY_AFTER_HEADER));
                    throw new RateLimitException(
                            "Too many requests (429); " + text,
                            retryAfterSeconds,
                            text);
                }

                throw new ClientConnectionException(
                        code,
                        "Invalid response received: " + code + "; " + text);
            }
        }
    }

    protected void processHeaders(Headers headers) {
        // Default implementation is empty
    }

    /**
     * Parses the {@code Retry-After} header per
     * <a href="https://www.rfc-editor.org/rfc/rfc7231#section-7.1.3">RFC 7231 Section 7.1.3</a>.
     *
     * <p>The header value is interpreted as either:
     * <ul>
     *   <li>a non-negative number of seconds ({@code delay-seconds}), or</li>
     *   <li>an HTTP-date (RFC 1123 format), converted to a relative delta from now.</li>
     * </ul>
     *
     * <p>Returns {@code 0} when the header is {@code null}, blank, negative, or unparseable.
     * The result is capped at {@link #MAX_RETRY_AFTER_SECONDS} to prevent stalling on
     * adversarial or misconfigured servers.
     *
     * @param retryAfterValue the raw header value, may be {@code null}
     * @return seconds to wait before retrying, in the range {@code [0, MAX_RETRY_AFTER_SECONDS]}
     */
    static long parseRetryAfterSeconds(String retryAfterValue) {
        if (retryAfterValue == null) {
            return 0L;
        }
        String trimmed = retryAfterValue.trim();
        if (trimmed.isEmpty()) {
            return 0L;
        }

        try {
            double seconds = Double.parseDouble(trimmed);
            if (seconds >= 0 && !Double.isInfinite(seconds) && !Double.isNaN(seconds)) {
                return Math.min(MAX_RETRY_AFTER_SECONDS, (long) Math.ceil(seconds));
            }
            return 0L;
        } catch (NumberFormatException ignored) {
            // Not numeric — fall through to HTTP-date parsing
        }

        try {
            ZonedDateTime retryAt =
                    ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME);
            long seconds = ChronoUnit.SECONDS.between(Instant.now(), retryAt.toInstant());
            return Math.min(MAX_RETRY_AFTER_SECONDS, Math.max(0L, seconds));
        } catch (DateTimeParseException e) {
            log.debug("Unparseable Retry-After header value: {}", trimmed);
            return 0L;
        }
    }

    private InputStream buildInputStream(ResponseBody responseBody) throws IOException {
        return new ByteArrayInputStream(responseBody.bytes());
    }

    private Headers buildHeaders() {
        return Headers.of(headers);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addHeaders(Map<String, String> headersToAdd) {
        headers.putAll(headersToAdd);
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void close() throws IOException {}
}
