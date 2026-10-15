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
package org.web3j.protocol.core.methods.request;

import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.web3j.utils.Numeric;

/** https://github.com/ethereum/wiki/wiki/JSON-RPC#shh_post */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"from", "to", "topics", "payload", "priority", "ttl"})
public class ShhPost {
    private String from;
    private String to;
    private List<String> topics;
    private String payload;
    private BigInteger priority;
    private BigInteger ttl;

    public ShhPost(List<String> topics, String payload, BigInteger priority, BigInteger ttl) {
        this.topics = topics;
        this.payload = payload;
        this.priority = priority;
        this.ttl = ttl;
    }

    public ShhPost(
            String from,
            String to,
            List<String> topics,
            String payload,
            BigInteger priority,
            BigInteger ttl) {
        this.from = from;
        this.to = to;
        this.topics = topics;
        this.payload = payload;
        this.priority = priority;
        this.ttl = ttl;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getPayload() {
        return payload;
    }

    public String getPriority() {
        return convert(priority);
    }

    public String getTtl() {
        return convert(ttl);
    }

    private String convert(BigInteger value) {
        if (value != null) {
            return Numeric.encodeQuantity(value);
        } else {
            return null;
        }
    }
}
