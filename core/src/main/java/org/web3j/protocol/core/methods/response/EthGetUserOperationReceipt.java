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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.Response;

/** eth_getUserOperationReceipt. */
public class EthGetUserOperationReceipt extends Response<UserOperationReceipt> {

    @Override
    @JsonDeserialize(using = EthGetUserOperationReceipt.ResponseDeserialiser.class)
    public void setResult(UserOperationReceipt result) {
        super.setResult(result);
    }

    public UserOperationReceipt getOperationReceipt() {
        return getResult();
    }

    public static class ResponseDeserialiser extends JsonDeserializer<UserOperationReceipt> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        @Override
        public UserOperationReceipt deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                return objectReader.readValue(jsonParser, UserOperationReceipt.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
