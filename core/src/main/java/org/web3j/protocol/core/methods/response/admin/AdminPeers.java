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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.web3j.protocol.core.Response;

/** admin_peers. */
public class AdminPeers extends Response<List<AdminPeers.Peer>> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Override
    @JsonDeserialize(using = AdminPeers.ResponseDeserialiser.class)
    public void setResult(List<Peer> result) {
        super.setResult(result);
    }

    public static class Peer {
        public Peer() {}

        public Peer(String id, String name, String enode, PeerNetwork network) {
            this.id = id;
            this.name = name;
            this.network = network;
            this.enode = enode;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEnode() {
            return enode;
        }

        private String id;
        private String name;
        private PeerNetwork network;
        private String enode;

        public PeerNetwork getNetwork() {
            return network;
        }
    }

    public static class PeerNetwork {

        public PeerNetwork() {}

        private String localAddress;
        private String remoteAddress;

        public PeerNetwork(String localAddress, String remoteAddress) {
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
        }

        public String getLocalAddress() {
            return localAddress;
        }

        public String getRemoteAddress() {
            return remoteAddress;
        }
    }

    public static class ResponseDeserialiser extends ValueDeserializer<List<Peer>> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public List<Peer> deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext) {
            if (jsonParser.currentToken() != JsonToken.VALUE_NULL) {
                return objectMapper.convertValue(
                        jsonParser.readValueAsTree(), new TypeReference<List<Peer>>() {});
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
