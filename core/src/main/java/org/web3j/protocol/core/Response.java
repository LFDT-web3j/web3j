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
package org.web3j.protocol.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.web3j.protocol.deserializer.KeepAsJsonDeserialzier;

/**
 * Our common JSON-RPC response type.
 *
 * @param <T> the object type contained within the response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response<T> {
    private long id;
    private String jsonrpc;
    private T result;
    private Error error;
    private String rawResponse;

    public Response() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public static class Error {
        private int code;
        private String message;

        @JsonDeserialize(using = KeepAsJsonDeserialzier.class)
        private String data;

        public Error() {}

        public Error(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Error)) {
                return false;
            }

            Error error = (Error) o;

            if (getCode() != error.getCode()) {
                return false;
            }
            if (getMessage() != null
                    ? !getMessage().equals(error.getMessage())
                    : error.getMessage() != null) {
                return false;
            }
            return getData() != null ? getData().equals(error.getData()) : error.getData() == null;
        }

        @Override
        public int hashCode() {
            int result = getCode();
            result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
            result = 31 * result + (getData() != null ? getData().hashCode() : 0);
            return result;
        }
    }
}
