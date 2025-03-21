package org.web3j.protocol.core.methods.response;

/**
 * @Author DylanYang
 * @Date 2025/3/8
 */
public class Authorization {
    private String chainId;
    private String nonce;
    private String address;
    private String yParity;
    private String s;
    private String r;

    public Authorization(String chainId, String r, String nonce, String address, String yParity, String s) {
        this.chainId = chainId;
        this.r = r;
        this.nonce = nonce;
        this.address = address;
        this.yParity = yParity;
        this.s = s;
    }

    public Authorization() {
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getyParity() {
        return yParity;
    }

    public void setyParity(String yParity) {
        this.yParity = yParity;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }
}
