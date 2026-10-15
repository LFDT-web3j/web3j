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
package org.web3j.crypto;

import java.math.BigInteger;
import java.util.Objects;

public class AuthorizationTuple {
    private final BigInteger chainId;
    private final String address;
    private final BigInteger nonce;
    private final BigInteger yParity;
    private final BigInteger r;
    private final BigInteger s;

    public AuthorizationTuple(
            BigInteger chainId,
            String address,
            BigInteger nonce,
            BigInteger yParity,
            BigInteger r,
            BigInteger s) {
        this.chainId = chainId;
        this.address = address;
        this.nonce = nonce;
        this.yParity = yParity;
        this.r = r;
        this.s = s;
    }

    /**
     * Signs an EIP-7702 authorization with the given key pair and returns the populated (signed)
     * tuple. Convenience wrapper around {@link Sign#signAuthorization(BigInteger, String,
     * BigInteger, ECKeyPair)}.
     *
     * @param chainId chain id the authorization is bound to ({@code 0} = valid on any chain)
     * @param address 20-byte delegation target address (0x-prefixed)
     * @param nonce authority account nonce the authorization is valid for
     * @param keyPair the authority's key pair
     * @return a signed authorization tuple
     */
    public static AuthorizationTuple from(
            BigInteger chainId, String address, BigInteger nonce, ECKeyPair keyPair) {
        return Sign.signAuthorization(chainId, address, nonce, keyPair);
    }

    /** Convenience overload taking {@link Credentials}. */
    public static AuthorizationTuple from(
            BigInteger chainId, String address, BigInteger nonce, Credentials credentials) {
        return Sign.signAuthorization(chainId, address, nonce, credentials);
    }

    /** Convenience overload taking a {@code long} chainId. */
    public static AuthorizationTuple from(
            long chainId, String address, BigInteger nonce, ECKeyPair keyPair) {
        return Sign.signAuthorization(chainId, address, nonce, keyPair);
    }

    /** Convenience overload taking a {@code long} chainId and {@link Credentials}. */
    public static AuthorizationTuple from(
            long chainId, String address, BigInteger nonce, Credentials credentials) {
        return Sign.signAuthorization(chainId, address, nonce, credentials);
    }

    public BigInteger getChainId() {
        return chainId;
    }

    public String getAddress() {
        return address;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getYParity() {
        return yParity;
    }

    public BigInteger getR() {
        return r;
    }

    public BigInteger getS() {
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorizationTuple)) return false;
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
