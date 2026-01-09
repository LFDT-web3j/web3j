/*
 * Copyright 2026 Web3 Labs Ltd.
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

import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.web3j.utils.Numeric;

/** EIP-7702 authorization tuple returned in transaction responses. */
public class AuthorizationTuple {
    private String chainId;

    private String address;

    private String nonce;

    @JsonProperty("yParity")
    private String yParity;

    private String r;

    private String s;

    public AuthorizationTuple() {}

    public AuthorizationTuple(
            String chainId, String address, String nonce, String yParity, String r, String s) {
        this.chainId = chainId;
        this.address = address;
        this.nonce = nonce;
        this.yParity = yParity;
        this.r = r;
        this.s = s;
    }

    public BigInteger getChainId() {
        return chainId != null ? Numeric.decodeQuantity(chainId) : null;
    }

    public String getChainIdRaw() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigInteger getNonce() {
        return nonce != null ? Numeric.decodeQuantity(nonce) : null;
    }

    public String getNonceRaw() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public BigInteger getYParity() {
        return yParity != null ? Numeric.decodeQuantity(yParity) : null;
    }

    public String getYParityRaw() {
        return yParity;
    }

    public void setYParity(String yParity) {
        this.yParity = yParity;
    }

    public BigInteger getR() {
        return r != null ? Numeric.decodeQuantity(r) : null;
    }

    public String getRRaw() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public BigInteger getS() {
        return s != null ? Numeric.decodeQuantity(s) : null;
    }

    public String getSRaw() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationTuple that = (AuthorizationTuple) o;
        return Objects.equals(chainId, that.chainId)
                && Objects.equals(address, that.address)
                && Objects.equals(nonce, that.nonce)
                && Objects.equals(yParity, that.yParity)
                && Objects.equals(r, that.r)
                && Objects.equals(s, that.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chainId, address, nonce, yParity, r, s);
    }
}
