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
package org.web3j.protocol.core.methods.response;

import java.util.Optional;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;

import org.web3j.protocol.core.Response;

/**
 * Transaction object returned by:
 *
 * <ul>
 *   <li>eth_getTransactionByHash
 *   <li>eth_getTransactionByBlockHashAndIndex
 *   <li>eth_getTransactionByBlockNumberAndIndex
 * </ul>
 *
 * <p>This differs slightly from the request {@link EthSendTransaction} Transaction object.
 *
 * <p>See <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_gettransactionbyhash">docs</a>
 * for further details.
 */
public class EthTransaction extends Response<Transaction> {

    public Optional<Transaction> getTransaction() {
        return Optional.ofNullable(getResult());
    }

    public static class ResponseDeserialiser extends ValueDeserializer<Transaction> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Transaction deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext) {
            if (jsonParser.currentToken() != JsonToken.VALUE_NULL) {
                return objectMapper.convertValue(jsonParser.readValueAsTree(), Transaction.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
