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
package org.web3j.protocol.exceptions;

/**
 * Thrown when the RPC endpoint responds with HTTP 429 Too Many Requests.
 *
 * <p>Callers can inspect {@link #getRetryAfterSeconds()} to honour the server's
 * back-off hint and {@link #getResponseBody()} for any machine-readable error payload.
 */
public class RateLimitException extends ClientConnectionException {

    private final long retryAfterSeconds;
    private final String responseBody;

    /**
     * @param message human-readable description (typically includes the status code and body)
     * @param retryAfterSeconds parsed {@code Retry-After} delay, {@code 0} when absent
     * @param responseBody raw HTTP response body, may be {@code null}
     */
    public RateLimitException(String message, long retryAfterSeconds, String responseBody) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.responseBody = responseBody;
    }

    /** Convenience constructor when the response body is not available separately. */
    public RateLimitException(String message, long retryAfterSeconds) {
        this(message, retryAfterSeconds, null);
    }

    /** Seconds the server asked the client to wait, or {@code 0} if the header was absent. */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /** Raw HTTP response body returned with the 429 status, or {@code null} if unavailable. */
    public String getResponseBody() {
        return responseBody;
    }
}
