/*
 * Copyright 2025 Web3 Labs Ltd.
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
package org.web3j.protocol.core.methods.response;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.web3j.protocol.core.Response;

/** eth_getUserOperationByHash. */
public class EthGetUserOperationByHash extends Response<UserOperationResult> {

    @Override
    @JsonDeserialize(using = EthGetUserOperationByHash.ResponseDeserialiser.class)
    public void setResult(UserOperationResult result) {
        super.setResult(result);
    }

    public UserOperationResult getUserOperation() {
        return getResult();
    }

    public static class ResponseDeserialiser extends ValueDeserializer<UserOperationResult> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public UserOperationResult deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext) {
            if (jsonParser.currentToken() != JsonToken.VALUE_NULL) {
                return objectMapper.convertValue(
                        jsonParser.readValueAsTree(), UserOperationResult.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
