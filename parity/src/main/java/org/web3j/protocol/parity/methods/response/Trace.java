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

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.node.ObjectNode;

import org.web3j.utils.Numeric;

/**
 * Trace used in following methods.
 *
 * <ol>
 *   <li>trace_call
 *   <li>trace_rawTransaction
 *   <li>trace_replayTransaction
 *   <li>trace_block
 *   <li>trace_filter
 *   <li>trace_get
 * </ol>
 */
public class Trace {

    private Action action;
    private String error;
    private Result result;
    private BigInteger subtraces;
    private List<BigInteger> traceAddress;
    private String type;
    private String blockHash;
    private BigInteger blockNumber;
    private String transactionHash;
    private BigInteger transactionPosition;

    @JsonDeserialize(using = ActionDeserializer.class)
    public interface Action {}

    @JsonDeserialize()
    public static class SuicideAction implements Action {

        private String address;
        private String balance;
        private String refundAddress;

        public SuicideAction() {}

        public SuicideAction(String address, String balance, String refundAddress) {
            this.address = address;
            this.balance = balance;
            this.refundAddress = refundAddress;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public BigInteger getBalance() {
            return Numeric.decodeQuantity(balance);
        }

        public String getBalanceRaw() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getRefundAddress() {
            return refundAddress;
        }

        public void setRefundAddress(String refundAddress) {
            this.refundAddress = refundAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof SuicideAction)) {
                return false;
            }

            SuicideAction that = (SuicideAction) o;

            if (getAddress() != null
                    ? !getAddress().equals(that.getAddress())
                    : that.getAddress() != null) {
                return false;
            }
            if (getBalanceRaw() != null
                    ? !getBalanceRaw().equals(that.getBalanceRaw())
                    : that.getBalanceRaw() != null) {
                return false;
            }
            return getRefundAddress() != null
                    ? getRefundAddress().equals(that.getRefundAddress())
                    : that.getRefundAddress() == null;
        }

        @Override
        public int hashCode() {
            int result = getAddress() != null ? getAddress().hashCode() : 0;
            result = 31 * result + (getBalanceRaw() != null ? getBalanceRaw().hashCode() : 0);
            result = 31 * result + (getRefundAddress() != null ? getRefundAddress().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "SuicideAction{"
                    + "address='"
                    + getAddress()
                    + '\''
                    + ", balance='"
                    + getBalanceRaw()
                    + '\''
                    + ", refundAddress='"
                    + getRefundAddress()
                    + '\''
                    + '}';
        }
    }

    @JsonDeserialize()
    public static class CallAction implements Action {

        private String callType;
        private String from;
        private String to;
        private String gas;
        private String input;
        private String value;

        public CallAction() {}

        public CallAction(
                String callType, String from, String to, String gas, String input, String value) {
            this.callType = callType;
            this.from = from;
            this.to = to;
            this.gas = gas;
            this.input = input;
            this.value = value;
        }

        public String getCallType() {
            return callType;
        }

        public void setCallType(String callType) {
            this.callType = callType;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public BigInteger getGas() {
            return Numeric.decodeQuantity(gas);
        }

        public String getGasRaw() {
            return gas;
        }

        public void setGas(String gas) {
            this.gas = gas;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public BigInteger getValue() {
            return Numeric.decodeQuantity(value);
        }

        public String getValueRaw() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CallAction)) {
                return false;
            }

            CallAction that = (CallAction) o;

            if (getCallType() != null
                    ? !getCallType().equals(that.getCallType())
                    : that.getCallType() != null) {
                return false;
            }
            if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) {
                return false;
            }
            if (getTo() != null ? !getTo().equals(that.getTo()) : that.getTo() != null) {
                return false;
            }
            if (getGasRaw() != null
                    ? !getGasRaw().equals(that.getGasRaw())
                    : that.getGasRaw() != null) {
                return false;
            }
            if (getInput() != null
                    ? !getInput().equals(that.getInput())
                    : that.getInput() != null) {
                return false;
            }
            return getValueRaw() != null
                    ? getValueRaw().equals(that.getValueRaw())
                    : that.getValueRaw() == null;
        }

        @Override
        public int hashCode() {
            int result = getCallType() != null ? getCallType().hashCode() : 0;
            result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
            result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
            result = 31 * result + (getGasRaw() != null ? getGasRaw().hashCode() : 0);
            result = 31 * result + (getInput() != null ? getInput().hashCode() : 0);
            result = 31 * result + (getValueRaw() != null ? getValueRaw().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CallAction{"
                    + "callType='"
                    + getCallType()
                    + '\''
                    + ", from='"
                    + getFrom()
                    + '\''
                    + ", to='"
                    + getTo()
                    + '\''
                    + ", gas='"
                    + getGasRaw()
                    + '\''
                    + ", input='"
                    + getInput()
                    + '\''
                    + ", value='"
                    + getValueRaw()
                    + '\''
                    + '}';
        }
    }

    @JsonDeserialize()
    public static class CreateAction implements Action {

        private String from;
        private String gas;
        private String value;
        private String init;

        public CreateAction() {}

        public CreateAction(String from, String gas, String value, String init) {
            this.from = from;
            this.gas = gas;
            this.value = value;
            this.init = init;
        }

        public BigInteger getValue() {
            return Numeric.decodeQuantity(value);
        }

        public String getValueRaw() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public BigInteger getGas() {
            return Numeric.decodeQuantity(gas);
        }

        public String getGasRaw() {
            return gas;
        }

        public void setGas(String gas) {
            this.gas = gas;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getInit() {
            return init;
        }

        public void setInit(String init) {
            this.init = init;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CreateAction)) {
                return false;
            }

            CreateAction that = (CreateAction) o;

            if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) {
                return false;
            }
            if (getGasRaw() != null
                    ? !getGasRaw().equals(that.getGasRaw())
                    : that.getGasRaw() != null) {
                return false;
            }
            if (getValueRaw() != null
                    ? !getValueRaw().equals(that.getValueRaw())
                    : that.getValueRaw() != null) {
                return false;
            }
            return getInit() != null ? getInit().equals(that.getInit()) : that.getInit() == null;
        }

        @Override
        public int hashCode() {
            int result = getFrom() != null ? getFrom().hashCode() : 0;
            result = 31 * result + (getGasRaw() != null ? getGasRaw().hashCode() : 0);
            result = 31 * result + (getValueRaw() != null ? getValueRaw().hashCode() : 0);
            result = 31 * result + (getInit() != null ? getInit().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CreateAction{"
                    + "from='"
                    + getFrom()
                    + '\''
                    + ", gas='"
                    + getGasRaw()
                    + '\''
                    + ", value='"
                    + getValueRaw()
                    + '\''
                    + ", init='"
                    + getInit()
                    + '\''
                    + '}';
        }
    }

    @JsonDeserialize()
    public static class RewardAction implements Action {

        private String author;
        private String value;
        private String rewardType;

        public RewardAction() {}

        public RewardAction(String author, String value, String rewardType) {
            this.author = author;
            this.value = value;
            this.rewardType = rewardType;
        }

        public BigInteger getValue() {
            return Numeric.decodeQuantity(value);
        }

        public String getValueRaw() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getRewardType() {
            return rewardType;
        }

        public void setRewardType(String rewardType) {
            this.rewardType = rewardType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RewardAction)) {
                return false;
            }

            RewardAction that = (RewardAction) o;

            if (getAuthor() != null
                    ? !getAuthor().equals(that.getAuthor())
                    : that.getAuthor() != null) {
                return false;
            }
            if (getValueRaw() != null
                    ? !getValueRaw().equals(that.getValueRaw())
                    : that.getValueRaw() != null) {
                return false;
            }
            return getRewardType() != null
                    ? getRewardType().equals(that.getRewardType())
                    : that.getRewardType() == null;
        }

        @Override
        public int hashCode() {
            int result = getAuthor() != null ? getAuthor().hashCode() : 0;
            result = 31 * result + (getValueRaw() != null ? getValueRaw().hashCode() : 0);
            result = 31 * result + (getRewardType() != null ? getRewardType().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "RewardAction{"
                    + "author='"
                    + getAuthor()
                    + '\''
                    + ", value='"
                    + getValueRaw()
                    + '\''
                    + ", rewardType='"
                    + getRewardType()
                    + '\''
                    + '}';
        }
    }

    public static class Result {

        private String address;
        private String code;
        private String gasUsed;
        private String output;

        public Result() {}

        public Result(String address, String code, String gasUsed, String output) {
            this.address = address;
            this.code = code;
            this.gasUsed = gasUsed;
            this.output = output;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public BigInteger getGasUsed() {
            return Numeric.decodeQuantity(gasUsed);
        }

        public String getGasUsedRaw() {
            return gasUsed;
        }

        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof Result)) {
                return false;
            }

            Result result = (Result) o;

            if (getAddress() != null
                    ? !getAddress().equals(result.getAddress())
                    : result.getAddress() != null) {
                return false;
            }
            if (getCode() != null
                    ? !getCode().equals(result.getCode())
                    : result.getCode() != null) {
                return false;
            }
            if (getGasUsedRaw() != null
                    ? !getGasUsedRaw().equals(result.getGasUsedRaw())
                    : result.getGasUsedRaw() != null) {
                return false;
            }
            return getOutput() != null
                    ? getOutput().equals(result.getOutput())
                    : result.getOutput() == null;
        }

        @Override
        public int hashCode() {
            int result = getAddress() != null ? getAddress().hashCode() : 0;
            result = 31 * result + (getCode() != null ? getCode().hashCode() : 0);
            result = 31 * result + (getGasUsedRaw() != null ? getGasUsedRaw().hashCode() : 0);
            result = 31 * result + (getOutput() != null ? getOutput().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Result{"
                    + "address='"
                    + getAddress()
                    + '\''
                    + ", code='"
                    + getCode()
                    + '\''
                    + ", gasUsed='"
                    + getGasUsedRaw()
                    + '\''
                    + ", output='"
                    + getOutput()
                    + '\''
                    + '}';
        }
    }

    public Trace() {}

    public Trace(
            Action action,
            String error,
            Result result,
            BigInteger subtraces,
            List<BigInteger> traceAddress,
            String type,
            String blockHash,
            BigInteger blockNumber,
            String transactionHash,
            BigInteger transactionPosition) {
        this.action = action;
        this.error = error;
        this.result = result;
        this.subtraces = subtraces;
        this.traceAddress = traceAddress;
        this.type = type;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.transactionHash = transactionHash;
        this.transactionPosition = transactionPosition;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public BigInteger getSubtraces() {
        return subtraces;
    }

    public void setSubtraces(BigInteger subtraces) {
        this.subtraces = subtraces;
    }

    public List<BigInteger> getTraceAddress() {
        return traceAddress;
    }

    public void setTraceAddress(List<BigInteger> traceAddress) {
        this.traceAddress = traceAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public BigInteger getTransactionPosition() {
        return transactionPosition;
    }

    public void setTransactionPosition(BigInteger transactionPosition) {
        this.transactionPosition = transactionPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Trace)) {
            return false;
        }

        Trace trace = (Trace) o;

        if (getAction() != null
                ? !getAction().equals(trace.getAction())
                : trace.getAction() != null) {
            return false;
        }
        if (getError() != null ? !getError().equals(trace.getError()) : trace.getError() != null) {
            return false;
        }
        if (getResult() != null
                ? !getResult().equals(trace.getResult())
                : trace.getResult() != null) {
            return false;
        }
        if (getSubtraces() != null
                ? !getSubtraces().equals(trace.getSubtraces())
                : trace.getSubtraces() != null) {
            return false;
        }
        if (getTraceAddress() != null
                ? !getTraceAddress().equals(trace.getTraceAddress())
                : trace.getTraceAddress() != null) {
            return false;
        }
        if (getType() != null ? !getType().equals(trace.getType()) : trace.getType() != null) {
            return false;
        }
        if (getBlockHash() != null
                ? !getBlockHash().equals(trace.getBlockHash())
                : trace.getBlockHash() != null) {
            return false;
        }
        if (getBlockNumber() != null
                ? !getBlockNumber().equals(trace.getBlockNumber())
                : trace.getBlockNumber() != null) {
            return false;
        }
        if (getTransactionHash() != null
                ? !getTransactionHash().equals(trace.getTransactionHash())
                : trace.getTransactionHash() != null) {
            return false;
        }
        return getTransactionPosition() != null
                ? getTransactionPosition().equals(trace.getTransactionPosition())
                : trace.getTransactionPosition() == null;
    }

    @Override
    public int hashCode() {
        int result1 = getAction() != null ? getAction().hashCode() : 0;
        result1 = 31 * result1 + (getError() != null ? getError().hashCode() : 0);
        result1 = 31 * result1 + (getResult() != null ? getResult().hashCode() : 0);
        result1 = 31 * result1 + (getSubtraces() != null ? getSubtraces().hashCode() : 0);
        result1 = 31 * result1 + (getTraceAddress() != null ? getTraceAddress().hashCode() : 0);
        result1 = 31 * result1 + (getType() != null ? getType().hashCode() : 0);
        result1 = 31 * result1 + (getBlockHash() != null ? getBlockHash().hashCode() : 0);
        result1 = 31 * result1 + (getBlockNumber() != null ? getBlockNumber().hashCode() : 0);
        result1 =
                31 * result1 + (getTransactionHash() != null ? getTransactionHash().hashCode() : 0);
        result1 =
                31 * result1
                        + (getTransactionPosition() != null
                                ? getTransactionPosition().hashCode()
                                : 0);
        return result1;
    }

    @Override
    public String toString() {
        return "Trace{"
                + "action="
                + getAction()
                + ", error='"
                + getError()
                + '\''
                + ", result="
                + getResult()
                + ", subtraces="
                + getSubtraces()
                + ", traceAddress="
                + getTraceAddress()
                + ", type='"
                + getType()
                + '\''
                + ", blockHash='"
                + getBlockHash()
                + '\''
                + ", blockNumber="
                + getBlockNumber()
                + ", transactionHash='"
                + getTransactionHash()
                + '\''
                + ", transactionPosition="
                + getTransactionPosition()
                + '}';
    }

    public static class ActionDeserializer extends ValueDeserializer<Action> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Action deserialize(JsonParser jsonParser, DeserializationContext context) {
            ObjectNode root = jsonParser.readValueAsTree();

            if (root.has("callType")) {
                return objectMapper.convertValue(root, CallAction.class);
            } else if (root.has("init")) {
                return objectMapper.convertValue(root, CreateAction.class);
            } else if (root.has("refundAddress")) {
                return objectMapper.convertValue(root, SuicideAction.class);
            } else if (root.has("rewardType")) {
                return objectMapper.convertValue(root, RewardAction.class);
            }

            return null;
        }
    }
}
