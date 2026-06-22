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
package org.web3j.protocol.parity.methods.response;

import java.util.Map;
import java.util.stream.Collectors;

import tools.jackson.databind.JsonNode;

/**
 * StateDiff used in following methods.
 *
 * <ol>
 *   <li>trace_call
 *   <li>trace_rawTransaction
 *   <li>trace_replayTransaction
 * </ol>
 */
public class StateDiff {

    private State balance;
    private State code;
    private State nonce;
    private Map<String, State> storage;

    public interface State {

        boolean isChanged();

        String getFrom();

        String getTo();
    }

    public static class ChangedState implements State {

        private String from;
        private String to;

        public ChangedState() {}

        public ChangedState(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public void setTo(String to) {
            this.to = to;
        }

        @Override
        public boolean isChanged() {
            return true;
        }

        @Override
        public String getFrom() {
            return from;
        }

        @Override
        public String getTo() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof ChangedState)) {
                return false;
            }

            ChangedState that = (ChangedState) o;

            if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) {
                return false;
            }
            return getTo() != null ? getTo().equals(that.getTo()) : that.getTo() == null;
        }

        @Override
        public int hashCode() {
            int result = getFrom() != null ? getFrom().hashCode() : 0;
            result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ChangedState{" + "from='" + from + '\'' + ", to='" + to + '\'' + '}';
        }
    }

    public static class UnchangedState implements State {

        public UnchangedState(String jsonString) {}

        public UnchangedState() {}

        @Override
        public boolean isChanged() {
            return false;
        }

        @Override
        public String getFrom() {
            return null;
        }

        @Override
        public String getTo() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return o != null && (this == o || (o instanceof UnchangedState));
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    public static class AddedState implements State {

        private String value;

        public AddedState() {}

        public AddedState(String value) {
            this.value = value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String getFrom() {
            return null;
        }

        @Override
        public String getTo() {
            return value;
        }

        @Override
        public boolean isChanged() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof AddedState)) {
                return false;
            }

            AddedState that = (AddedState) o;

            return value != null ? value.equals(that.value) : that.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "AddedState{" + "value='" + value + '\'' + '}';
        }
    }

    public StateDiff() {}

    public StateDiff(State balance, State code, State nonce, Map<String, State> storage) {
        this.balance = balance;
        this.code = code;
        this.nonce = nonce;
        this.storage = storage;
    }

    public State getBalance() {
        return balance;
    }

    public void setBalance(JsonNode balance) {
        this.balance = deserializeState(balance);
    }

    public State getCode() {
        return code;
    }

    public void setCode(JsonNode code) {
        this.code = deserializeState(code);
    }

    public State getNonce() {
        return nonce;
    }

    public void setNonce(JsonNode nonce) {
        this.nonce = deserializeState(nonce);
    }

    public Map<String, State> getStorage() {
        return storage;
    }

    public void setStorage(Map<String, JsonNode> storage) {
        this.storage =
                storage.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> deserializeState(entry.getValue())));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof StateDiff)) {
            return false;
        }

        StateDiff that = (StateDiff) o;

        if (getBalance() != null
                ? !getBalance().equals(that.getBalance())
                : that.getBalance() != null) {
            return false;
        }
        if (getCode() != null ? !getCode().equals(that.getCode()) : that.getCode() != null) {
            return false;
        }
        if (getNonce() != null ? !getNonce().equals(that.getNonce()) : that.getNonce() != null) {
            return false;
        }
        return getStorage() != null
                ? getStorage().equals(that.getStorage())
                : that.getStorage() == null;
    }

    @Override
    public int hashCode() {
        int result = getBalance() != null ? getBalance().hashCode() : 0;
        result = 31 * result + (getCode() != null ? getCode().hashCode() : 0);
        result = 31 * result + (getNonce() != null ? getNonce().hashCode() : 0);
        result = 31 * result + (getStorage() != null ? getStorage().hashCode() : 0);
        return result;
    }

    private State deserializeState(JsonNode node) {
        State state = null;
        if (node.isTextual() && node.asText().equals("=")) {
            state = new UnchangedState();
        } else if (node.isObject() && node.has("*")) {
            JsonNode subNode = node.get("*");
            if (subNode.isObject() && subNode.has("from") && subNode.has("to")) {
                state = new ChangedState(subNode.get("from").asText(), subNode.get("to").asText());
            }
        } else if (node.isObject() && node.has("+")) {
            JsonNode subNode = node.get("+");
            if (subNode.isTextual()) {
                state = new AddedState(subNode.asText());
            }
        }
        return state;
    }

    @Override
    public String toString() {
        return "StateDiff{"
                + "balance="
                + getBalance()
                + ", code="
                + getCode()
                + ", nonce="
                + getNonce()
                + ", storage="
                + getStorage()
                + '}';
    }
}
