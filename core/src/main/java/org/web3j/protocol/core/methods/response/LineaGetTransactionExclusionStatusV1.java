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

public class LineaGetTransactionExclusionStatusV1
        extends Response<LineaGetTransactionExclusionStatusV1.LineaExclusionStatus> {

    @Override
    @JsonDeserialize(using = LineaGetTransactionExclusionStatusV1.ResponseDeserialiser.class)
    public void setResult(LineaExclusionStatus result) {
        super.setResult(result);
    }

    public LineaExclusionStatus getLineaTransactionExclusionStatus() {
        return getResult();
    }

    public static class LineaExclusionStatus {

        private String txHash;
        private String from;
        private String nonce;
        private String txRejectionStage;
        private String reasonMessage;
        private String blockNumber;
        private String timestamp;

        public LineaExclusionStatus() {}

        public LineaExclusionStatus(
                String txHash,
                String from,
                String nonce,
                String txRejectionStage,
                String reasonMessage,
                String blockNumber,
                String timestamp) {
            this.txHash = txHash;
            this.from = from;
            this.nonce = nonce;
            this.txRejectionStage = txRejectionStage;
            this.reasonMessage = reasonMessage;
            this.blockNumber = blockNumber;
            this.timestamp = timestamp;
        }

        public String getTxHash() {
            return txHash;
        }

        public void setTxHash(String txHash) {
            this.txHash = txHash;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getTxRejectionStage() {
            return txRejectionStage;
        }

        public void setTxRejectionStage(String txRejectionStage) {
            this.txRejectionStage = txRejectionStage;
        }

        public String getReasonMessage() {
            return reasonMessage;
        }

        public void setReasonMessage(String reasonMessage) {
            this.reasonMessage = reasonMessage;
        }

        public String getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LineaExclusionStatus)) {
                return false;
            }
            LineaExclusionStatus res = (LineaExclusionStatus) o;

            if (getTxHash() != null
                    ? !getTxHash().equals(res.getTxHash())
                    : res.getTxHash() != null) {
                return false;
            }

            if (getNonce() != null ? !getNonce().equals(res.getNonce()) : res.getNonce() != null) {
                return false;
            }

            if (getFrom() != null ? !getFrom().equals(res.getFrom()) : res.getFrom() != null) {
                return false;
            }

            if (getReasonMessage() != null
                    ? !getReasonMessage().equals(res.getReasonMessage())
                    : res.getReasonMessage() != null) {
                return false;
            }

            if (getTxRejectionStage() != null
                    ? !getTxRejectionStage().equals(res.getTxRejectionStage())
                    : res.getTxRejectionStage() != null) {
                return false;
            }

            if (getBlockNumber() != null
                    ? !getBlockNumber().equals(res.getBlockNumber())
                    : res.getBlockNumber() != null) {
                return false;
            }

            return getTimestamp() != null
                    ? getTimestamp().equals(res.getTimestamp())
                    : res.getTimestamp() == null;
        }
    }

    public static class ResponseDeserialiser extends ValueDeserializer<LineaExclusionStatus> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public LineaExclusionStatus deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext) {
            if (jsonParser.currentToken() != JsonToken.VALUE_NULL) {
                return objectMapper.convertValue(
                        jsonParser.readValueAsTree(), LineaExclusionStatus.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
