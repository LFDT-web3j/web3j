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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Filter implementation as per <a
 * href="https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_newfilter">docs</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Filter<T extends Filter> {

    private List<FilterTopic> topics;

    Filter() {
        topics = new ArrayList<>();
    }

    public T addSingleTopic(String topic) {
        topics.add(new SingleTopic(topic));
        return getThis();
    }

    public T addNullTopic() {
        topics.add(new SingleTopic());
        return getThis();
    }

    // how to pass in null topic?
    public T addOptionalTopics(String... optionalTopics) {
        topics.add(new ListTopic(optionalTopics));
        return getThis();
    }

    public List<FilterTopic> getTopics() {
        return topics;
    }

    abstract T getThis();

    public interface FilterTopic<T> {
        @JsonValue
        T getValue();
    }

    public static class SingleTopic implements FilterTopic<String> {

        private String topic;

        public SingleTopic() {
            this.topic = null; // null topic
        }

        public SingleTopic(String topic) {
            this.topic = topic;
        }

        @Override
        public String getValue() {
            return topic;
        }
    }

    public static class ListTopic implements FilterTopic<List<SingleTopic>> {
        private List<SingleTopic> topics;

        public ListTopic(String... optionalTopics) {
            topics = new ArrayList<>();
            for (String topic : optionalTopics) {
                if (topic != null) {
                    topics.add(new SingleTopic(topic));
                } else {
                    topics.add(new SingleTopic());
                }
            }
        }

        @Override
        public List<SingleTopic> getValue() {
            return topics;
        }
    }
}
