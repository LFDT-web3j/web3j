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
package org.web3j.protocol.besu.response.privacy;

import java.math.BigInteger;
import java.util.Objects;

import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.web3j.utils.Base64String;
import org.web3j.utils.Numeric;
import org.web3j.utils.Restriction;

@JsonDeserialize(using = PrivateTransaction.ResponseDeserialiser.class)
public abstract class PrivateTransaction {

    public static class ResponseDeserialiser extends ValueDeserializer<PrivateTransaction> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        protected ResponseDeserialiser() {
            super();
        }

        @Override
        public PrivateTransaction deserialize(JsonParser p, DeserializationContext ctxt) {
            final TreeNode node = p.readValueAsTree();

            // Select the concrete class based on the existence of a property
            if (node.get("privateFor") != null && node.get("privateFor").isArray()) {
                return objectMapper.convertValue(node, PrivateTransactionLegacy.class);
            } else if ((node.get("privateFor") != null && node.get("privateFor").isValueNode())
                    || (node.get("privacyGroupId") != null
                            && node.get("privacyGroupId").isValueNode())) {
                return objectMapper.convertValue(node, PrivateTransactionWithPrivacyGroup.class);
            }

            return null;
        }
    }

    private String hash;
    private BigInteger nonce;
    private String from;
    private String to;
    private BigInteger value;
    private BigInteger gasPrice;
    private BigInteger gas;
    private String input;
    private String r;
    private String s;
    private long v;
    private Base64String privateFrom;
    private Restriction restriction;

    public PrivateTransaction(
            final String hash,
            final String nonce,
            final String from,
            final String to,
            final String value,
            final String gas,
            final String gasPrice,
            final String input,
            final String r,
            final String s,
            final String v,
            final Base64String privateFrom,
            final String restriction) {

        this.hash = hash;
        this.nonce = Numeric.decodeQuantity(nonce);
        this.from = from;
        this.to = to;
        this.value = Numeric.decodeQuantity(value);
        this.gasPrice = Numeric.decodeQuantity(gasPrice);
        this.gas = Numeric.decodeQuantity(gas);
        this.input = input;
        this.r = r;
        this.s = s;
        this.v = Numeric.decodeQuantity(v).longValue();
        this.privateFrom = privateFrom;
        this.restriction = Restriction.fromString(restriction);
    }

    public String getHash() {
        return hash;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getGas() {
        return gas;
    }

    public String getInput() {
        return input;
    }

    public String getR() {
        return r;
    }

    public String getS() {
        return s;
    }

    public long getV() {
        return v;
    }

    public Base64String getPrivateFrom() {
        return privateFrom;
    }

    public Restriction getRestriction() {
        return restriction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrivateTransaction that = (PrivateTransaction) o;
        return getV() == that.getV()
                && getHash().equals(that.getHash())
                && getNonce().equals(that.getNonce())
                && getFrom().equals(that.getFrom())
                && getTo().equals(that.getTo())
                && getValue().equals(that.getValue())
                && getGasPrice().equals(that.getGasPrice())
                && getGas().equals(that.getGas())
                && getInput().equals(that.getInput())
                && getR().equals(that.getR())
                && getS().equals(that.getS())
                && getPrivateFrom().equals(that.getPrivateFrom())
                && getRestriction().equals(that.getRestriction());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getHash(),
                getNonce(),
                getFrom(),
                getTo(),
                getValue(),
                getGasPrice(),
                getGas(),
                getInput(),
                getR(),
                getS(),
                getV(),
                getPrivateFrom(),
                getRestriction());
    }
}
