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

/** eth_estimateUserOperationGas. */
public class EthEstimateUserOperationGas
        extends Response<EthEstimateUserOperationGas.UserOperationGas> {

    @Override
    @JsonDeserialize(using = EthEstimateUserOperationGas.ResponseDeserialiser.class)
    public void setResult(UserOperationGas result) {
        super.setResult(result);
    }

    public UserOperationGas getUserOperationGas() {
        return getResult();
    }

    public static class UserOperationGas {
        private String preVerificationGas;
        private String verificationGasLimit;
        private String callGasLimit;
        private String paymasterVerificationGasLimit;

        public UserOperationGas() {}

        public UserOperationGas(
                String preVerificationGas,
                String verificationGasLimit,
                String callGasLimit,
                String paymasterVerificationGasLimit) {
            this.preVerificationGas = preVerificationGas;
            this.verificationGasLimit = verificationGasLimit;
            this.callGasLimit = callGasLimit;
            this.paymasterVerificationGasLimit = paymasterVerificationGasLimit;
        }

        public String getPreVerificationGas() {
            return preVerificationGas;
        }

        public void setPreVerificationGas(String preVerificationGas) {
            this.preVerificationGas = preVerificationGas;
        }

        public String getVerificationGasLimit() {
            return verificationGasLimit;
        }

        public void setVerificationGasLimit(String verificationGasLimit) {
            this.verificationGasLimit = verificationGasLimit;
        }

        public String getCallGasLimit() {
            return callGasLimit;
        }

        public void setCallGasLimit(String callGasLimit) {
            this.callGasLimit = callGasLimit;
        }

        public String getPaymasterVerificationGasLimit() {
            return paymasterVerificationGasLimit;
        }

        public void setPaymasterVerificationGasLimit(String paymasterVerificationGasLimit) {
            this.paymasterVerificationGasLimit = paymasterVerificationGasLimit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UserOperationGas userOperationGas)) {
                return false;
            }

            if (getPreVerificationGas() != null) {
                return getCallGasLimit().equals(userOperationGas.getCallGasLimit())
                        && getPreVerificationGas().equals(userOperationGas.getPreVerificationGas())
                        && getVerificationGasLimit()
                                .equals(userOperationGas.getVerificationGasLimit());
            }
            return getCallGasLimit().equals(userOperationGas.getCallGasLimit())
                    && getPreVerificationGas().equals(userOperationGas.getPreVerificationGas())
                    && getVerificationGasLimit().equals(userOperationGas.getVerificationGasLimit())
                    && getPaymasterVerificationGasLimit()
                            .equals(userOperationGas.getVerificationGasLimit());
        }

        @Override
        public int hashCode() {
            int result = getCallGasLimit() != null ? getCallGasLimit().hashCode() : 0;
            result =
                    31 * result
                            + (getVerificationGasLimit() != null
                                    ? getVerificationGasLimit().hashCode()
                                    : 0);
            result =
                    31 * result
                            + (getPreVerificationGas() != null
                                    ? getPreVerificationGas().hashCode()
                                    : 0);
            result =
                    31 * result
                            + (getPaymasterVerificationGasLimit() != null
                                    ? getPaymasterVerificationGasLimit().hashCode()
                                    : 0);
            return result;
        }
    }

    public static class ResponseDeserialiser
            extends JsonDeserializer<EthEstimateUserOperationGas.UserOperationGas> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        @Override
        public EthEstimateUserOperationGas.UserOperationGas deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                return objectReader.readValue(
                        jsonParser, EthEstimateUserOperationGas.UserOperationGas.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
