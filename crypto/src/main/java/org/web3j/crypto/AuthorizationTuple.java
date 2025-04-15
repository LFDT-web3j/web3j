package org.web3j.crypto;

import java.math.BigInteger;

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

}
