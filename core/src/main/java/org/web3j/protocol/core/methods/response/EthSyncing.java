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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.web3j.protocol.core.Response;

/**
 * eth_syncing.
 *
 * <p>Returns an object with data about the sync status or false.
 */
public class EthSyncing extends Response<EthSyncing.Result> {

    @Override
    @JsonDeserialize(using = EthSyncing.ResponseDeserialiser.class)
    public void setResult(EthSyncing.Result result) {
        super.setResult(result);
    }

    public boolean isSyncing() {
        return getResult().isSyncing();
    }

    public static class Result {
        private boolean isSyncing = true;

        public Result() {}

        public boolean isSyncing() {
            return isSyncing;
        }

        public void setSyncing(boolean syncing) {
            isSyncing = syncing;
        }
    }

    @JsonIgnoreProperties({"knownStates", "pulledStates"})
    // these fields although not present in the RPC specification are returned by Geth 1.4.10
    public static class Syncing extends Result {

        private String startingBlock;
        private String currentBlock;
        private String highestBlock;
        private String knownStates;
        private String pulledStates;

        public Syncing() {}

        public Syncing(
                String startingBlock,
                String currentBlock,
                String highestBlock,
                String knownStates,
                String pulledStates) {
            this.startingBlock = startingBlock;
            this.currentBlock = currentBlock;
            this.highestBlock = highestBlock;
            this.knownStates = knownStates;
            this.pulledStates = pulledStates;
        }

        public String getStartingBlock() {
            return startingBlock;
        }

        public void setStartingBlock(String startingBlock) {
            this.startingBlock = startingBlock;
        }

        public String getCurrentBlock() {
            return currentBlock;
        }

        public void setCurrentBlock(String currentBlock) {
            this.currentBlock = currentBlock;
        }

        public String getHighestBlock() {
            return highestBlock;
        }

        public void setHighestBlock(String highestBlock) {
            this.highestBlock = highestBlock;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Syncing)) {
                return false;
            }

            Syncing syncing = (Syncing) o;

            if (isSyncing() != syncing.isSyncing()) {
                return false;
            }
            if (getStartingBlock() != null
                    ? !getStartingBlock().equals(syncing.getStartingBlock())
                    : syncing.getStartingBlock() != null) {
                return false;
            }
            if (getCurrentBlock() != null
                    ? !getCurrentBlock().equals(syncing.getCurrentBlock())
                    : syncing.getCurrentBlock() != null) {
                return false;
            }
            if (getHighestBlock() != null
                    ? !getHighestBlock().equals(syncing.getHighestBlock())
                    : syncing.getHighestBlock() != null) {
                return false;
            }
            if (knownStates != null
                    ? !knownStates.equals(syncing.knownStates)
                    : syncing.knownStates != null) {
                return false;
            }
            return pulledStates != null
                    ? pulledStates.equals(syncing.pulledStates)
                    : syncing.pulledStates == null;
        }

        @Override
        public int hashCode() {
            int result = getStartingBlock() != null ? getStartingBlock().hashCode() : 0;
            result = 31 * result + Boolean.hashCode(isSyncing());
            result = 31 * result + (getCurrentBlock() != null ? getCurrentBlock().hashCode() : 0);
            result = 31 * result + (getHighestBlock() != null ? getHighestBlock().hashCode() : 0);
            result = 31 * result + (knownStates != null ? knownStates.hashCode() : 0);
            result = 31 * result + (pulledStates != null ? pulledStates.hashCode() : 0);
            return result;
        }
    }

    public static class ResponseDeserialiser extends ValueDeserializer<Result> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Result deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext) {
            Result result;
            if (jsonParser.currentToken() == JsonToken.VALUE_FALSE) {
                result = new Result();
                result.setSyncing(jsonParser.getBooleanValue());
            } else {
                result = objectMapper.convertValue(jsonParser.readValueAsTree(), Syncing.class);
            }
            return result;
        }
    }
}
