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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

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

                // Note that the following cipher suites are all on HTTP/2's bad cipher suites
                // list.
                // We'll
                // continue to include them until better suites are commonly available. For
                // example,
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
     * Upper bound applied to parsed {@code Retry-After} values to guard against buggy or
     * adversarial servers stalling callers indefinitely.
     */
    static final long MAX_RETRY_AFTER_SECONDS = 86_400L;

    private static final BigDecimal MAX_RETRY_AFTER_BIG_DECIMAL =
            BigDecimal.valueOf(MAX_RETRY_AFTER_SECONDS);

    private static final Pattern DECIMAL_DELAY_PATTERN = Pattern.compile("^-?\\d+(?:\\.\\d+)?$");

    private static final DateTimeFormatter RFC_1123_ZONE_NAME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    private static final DateTimeFormatter RFC_850_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH);

    private static final DateTimeFormatter ASCTIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM ppd HH:mm:ss yyyy", Locale.ENGLISH);

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
                String rawBody = responseBody == null ? null : responseBody.string();
                if (rawBody != null && rawBody.isEmpty()) {
                    // OkHttp 5 returns an empty ResponseBody when the server omits a payload
                    rawBody = null;
                }
                String text = rawBody == null ? "N/A" : rawBody;

                if (code == HTTP_TOO_MANY_REQUESTS) {
                    long retryAfterSeconds =
                            parseRetryAfterSeconds(response.header(RETRY_AFTER_HEADER));
                    throw new RateLimitException(
                            "Too many requests (429); " + text, retryAfterSeconds, rawBody);
                }

                throw new ClientConnectionException(
                        code, "Invalid response received: " + code + "; " + text);
            }
        }
    }

    protected void processHeaders(Headers headers) {
        // Default implementation is empty
    }

    /**
     * Parses the {@code Retry-After} header per <a
     * href="https://www.rfc-editor.org/rfc/rfc9110#section-10.2.3">RFC 9110 Section 10.2.3</a>.
     *
     * @param retryAfterValue the raw header value, may be {@code null}
     * @return seconds to wait before retrying, in the range {@code [0, MAX_RETRY_AFTER_SECONDS]}
     */
    static long parseRetryAfterSeconds(String retryAfterValue) {
        return parseRetryAfterSeconds(retryAfterValue, Instant.now());
    }

    static long parseRetryAfterSeconds(String retryAfterValue, Instant now) {
        if (retryAfterValue == null || now == null) {
            return 0L;
        }
        String trimmed = retryAfterValue.trim();
        if (trimmed.isEmpty()) {
            return 0L;
        }

        if (DECIMAL_DELAY_PATTERN.matcher(trimmed).matches()) {
            if (trimmed.startsWith("-")) {
                return 0L;
            }
            BigDecimal seconds = new BigDecimal(trimmed);
            if (seconds.compareTo(MAX_RETRY_AFTER_BIG_DECIMAL) >= 0) {
                return MAX_RETRY_AFTER_SECONDS;
            }
            return Math.min(
                    MAX_RETRY_AFTER_SECONDS,
                    seconds.setScale(0, RoundingMode.CEILING).longValueExact());
        }

        Instant retryAt = parseHttpDate(trimmed);
        if (retryAt == null) {
            log.debug("Unparseable Retry-After header value: {}", trimmed);
            return 0L;
        }

        Duration wait = Duration.between(now, retryAt);
        if (wait.isNegative() || wait.isZero()) {
            return 0L;
        }
        long waitSeconds = wait.getSeconds() + (wait.getNano() > 0 ? 1L : 0L);
        return Math.min(MAX_RETRY_AFTER_SECONDS, waitSeconds);
    }

    private static Instant parseHttpDate(String value) {
        for (DateTimeFormatter formatter :
                Arrays.asList(
                        DateTimeFormatter.RFC_1123_DATE_TIME,
                        RFC_1123_ZONE_NAME_FORMATTER,
                        RFC_850_FORMATTER)) {
            try {
                return ZonedDateTime.parse(value, formatter).toInstant();
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return LocalDateTime.parse(value, ASCTIME_FORMATTER).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
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
