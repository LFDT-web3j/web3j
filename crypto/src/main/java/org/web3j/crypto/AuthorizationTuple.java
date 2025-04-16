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
        BigInteger s
    ) {
        this.chainId = chainId;
        this.address = address;
        this.nonce = nonce;
        this.yParity = yParity;
        this.r = r;
        this.s = s;
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
        if (this == o) return true;          // same object in memory
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
