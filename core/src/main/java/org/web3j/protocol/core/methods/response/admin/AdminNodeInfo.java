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
package org.web3j.protocol.core.methods.response.admin;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.web3j.protocol.core.Response;

/** admin_nodeInfo. */
public class AdminNodeInfo extends Response<AdminNodeInfo.NodeInfo> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Override
    @JsonDeserialize(using = AdminNodeInfo.ResponseDeserialiser.class)
    public void setResult(NodeInfo result) {
        super.setResult(result);
    }

    public static class NodeInfo {
        private String enode;
        private String id;
        private String ip;
        private String listenAddr;
        private String name;
        private String consensus;

        @JsonProperty("protocols")
        @SuppressWarnings("unchecked")
        private void consensusDeserializer(Map<String, Object> protocols) {
            if (protocols.containsKey("istanbul")) {
                consensus = "istanbul";
            } else if (protocols.containsKey("clique")) {
                consensus = "clique";
            } else if (protocols.containsKey("eth")) {
                Map<String, Object> eth = (Map<String, Object>) protocols.get("eth");
                consensus = (String) eth.get("consensus");
            } else {
                consensus = "unknown";
            }
        }

        public NodeInfo() {}

        public NodeInfo(
                String enode,
                String id,
                String ip,
                String listenAddr,
                String name,
                String consensus) {
            this.enode = enode;
            this.id = id;
            this.ip = ip;
            this.listenAddr = listenAddr;
            this.name = name;
            this.consensus = consensus;
        }

        public String getEnode() {
            return enode;
        }

        public String getId() {
            return id;
        }

        public String getIp() {
            return ip;
        }

        public String getListenAddr() {
            return listenAddr;
        }

        public String getName() {
            return name;
        }

        public String getConsensus() {
            return consensus;
        }
    }

    public static class ResponseDeserialiser extends ValueDeserializer<NodeInfo> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public NodeInfo deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext) {
            if (jsonParser.currentToken() != JsonToken.VALUE_NULL) {
                return objectMapper.convertValue(jsonParser.readValueAsTree(), NodeInfo.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
