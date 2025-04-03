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
package org.web3j.protocol.core.methods.response;

import java.math.BigInteger;
import java.util.List;

import org.web3j.crypto.TransactionUtils;
import org.web3j.utils.Numeric;

/** Transaction object used by both {@link EthTransaction} and {@link EthBlock}. */
public class Transaction {
    private String hash;
    private String nonce;
    private String blockHash;
    private String blockNumber;
    private String chainId;
    private String transactionIndex;
    private String from;
    private String to;
    private String value;
    private String gasPrice;
    private String gas;
    private String input;
    private String creates;
    private String publicKey;
    private String raw;
    private String r;
    private String s;
    private long v; // see https://github.com/web3j/web3j/issues/44
    private String yParity;
    private String type;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private List<AccessListObject> accessList;
    private String maxFeePerBlobGas;
    private List<String> blobVersionedHashes;
    private List<Authorization> authorizationList;

    public Transaction() {}

    /** Use constructor with ChainId */
    @Deprecated
    public Transaction(
            String hash,
            String nonce,
            String blockHash,
            String blockNumber,
            String transactionIndex,
            String from,
            String to,
            String value,
            String gas,
            String gasPrice,
            String input,
            String creates,
            String publicKey,
            String raw,
            String r,
            String s,
            long v,
            String type,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            List accessList) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.transactionIndex = transactionIndex;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.input = input;
        this.creates = creates;
        this.publicKey = publicKey;
        this.raw = raw;
        this.r = r;
        this.s = s;
        this.v = v;
        this.type = type;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.accessList = accessList;
    }

    public Transaction(
            String hash,
            String nonce,
            String blockHash,
            String blockNumber,
            String chainId,
            String transactionIndex,
            String from,
            String to,
            String value,
            String gas,
            String gasPrice,
            String input,
            String creates,
            String publicKey,
            String raw,
            String r,
            String s,
            long v,
            String yParity,
            String type,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            List accessList,
            List<Authorization> authorizationList
            ) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.chainId = chainId;
        this.transactionIndex = transactionIndex;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.input = input;
        this.creates = creates;
        this.publicKey = publicKey;
        this.raw = raw;
        this.r = r;
        this.s = s;
        this.v = v;
        this.yParity = yParity;
        this.type = type;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.accessList = accessList;
        this.authorizationList = authorizationList;
    }

    public Transaction(
            String hash,
            String nonce,
            String blockHash,
            String blockNumber,
            String chainId,
            String transactionIndex,
            String from,
            String to,
            String value,
            String gas,
            String gasPrice,
            String input,
            String creates,
            String publicKey,
            String raw,
            String r,
            String s,
            long v,
            String yParity,
            String type,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            List accessList,
            String maxFeePerBlobGas,
            List versionedHashes,
            List<Authorization> authorizationList
            ) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.chainId = chainId;
        this.transactionIndex = transactionIndex;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.input = input;
        this.creates = creates;
        this.publicKey = publicKey;
        this.raw = raw;
        this.r = r;
        this.s = s;
        this.v = v;
        this.yParity = yParity;
        this.type = type;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.accessList = accessList;
        this.maxFeePerBlobGas = maxFeePerBlobGas;
        this.blobVersionedHashes = versionedHashes;
        this.authorizationList = authorizationList;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BigInteger getNonce() {
        return Numeric.decodeQuantity(nonce);
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getNonceRaw() {
        return nonce;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public BigInteger getBlockNumber() {
        return Numeric.decodeQuantity(blockNumber);
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlockNumberRaw() {
        return blockNumber;
    }

    public BigInteger getTransactionIndex() {
        return Numeric.decodeQuantity(transactionIndex);
    }

    public void setTransactionIndex(String transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getTransactionIndexRaw() {
        return transactionIndex;
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

    public BigInteger getValue() {
        return Numeric.decodeQuantity(value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueRaw() {
        return value;
    }

    public BigInteger getGasPrice() {
        return Numeric.decodeQuantity(gasPrice);
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getGasPriceRaw() {
        return gasPrice;
    }

    public BigInteger getGas() {
        return Numeric.decodeQuantity(gas);
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getGasRaw() {
        return gas;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getCreates() {
        return creates;
    }

    public void setCreates(String creates) {
        this.creates = creates;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public long getV() {
        return v;
    }

    public List<Authorization> getAuthorizationList() {
        return authorizationList;
    }

    public void setAuthorizationList(List<Authorization> authorizationList) {
        this.authorizationList = authorizationList;
    }

    // Workaround until Geth & Parity return consistent values. At present
    // Parity returns a byte value, Geth returns a hex-encoded string
    // https://github.com/ethereum/go-ethereum/issues/3339
    public void setV(Object v) {
        if (v instanceof String) {
            // longValueExact() is not implemented on android 11 or later only on 12 so it was
            // replaced with longValue.
            this.v = Numeric.toBigInt((String) v).longValue();
        } else if (v instanceof Integer) {
            this.v = ((Integer) v).longValue();
        } else {
            this.v = (Long) v;
        }
    }

    //    public void setV(byte v) {
    //        this.v = v;
    //    }

    public String getyParity() {
        return yParity;
    }

    public void setyParity(String yParity) {
        this.yParity = yParity;
    }

    public Long getChainId() {
        if (chainId != null) {
            return Numeric.decodeQuantity(chainId).longValue();
        }

        return TransactionUtils.deriveChainId(v);
    }

    public String getChainIdRaw() {
        return this.chainId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigInteger getMaxFeePerGas() {
        if (maxFeePerGas == null) return null;
        return Numeric.decodeQuantity(maxFeePerGas);
    }

    public String getMaxFeePerGasRaw() {
        return maxFeePerGas;
    }

    public void setMaxFeePerGas(String maxFeePerGas) {
        this.maxFeePerGas = maxFeePerGas;
    }

    public String getMaxPriorityFeePerGasRaw() {
        return maxPriorityFeePerGas;
    }

    public BigInteger getMaxPriorityFeePerGas() {
        return Numeric.decodeQuantity(maxPriorityFeePerGas);
    }

    public void setMaxPriorityFeePerGas(String maxPriorityFeePerGas) {
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    public List<AccessListObject> getAccessList() {
        return accessList;
    }

    public void setAccessList(List<AccessListObject> accessList) {
        this.accessList = accessList;
    }

    public String getMaxFeePerBlobGasRaw() {
        return maxFeePerBlobGas;
    }

    public BigInteger getMaxFeePerBlobGas() {
        return Numeric.decodeQuantity(maxFeePerBlobGas);
    }

    public void setMaxFeePerBlobGas(String maxFeePerBlobGas) {
        this.maxFeePerBlobGas = maxFeePerBlobGas;
    }

    public List<String> getBlobVersionedHashes() {
        return blobVersionedHashes;
    }

    public void setBlobVersionedHashes(List<String> blobVersionedHashes) {
        this.blobVersionedHashes = blobVersionedHashes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction)) {
            return false;
        }

        Transaction that = (Transaction) o;

        if (getV() != that.getV()) {
            return false;
        }
        if (getHash() != null ? !getHash().equals(that.getHash()) : that.getHash() != null) {
            return false;
        }
        if (getNonceRaw() != null
                ? !getNonceRaw().equals(that.getNonceRaw())
                : that.getNonceRaw() != null) {
            return false;
        }
        if (getBlockHash() != null
                ? !getBlockHash().equals(that.getBlockHash())
                : that.getBlockHash() != null) {
            return false;
        }

        if (getChainIdRaw() != null
                ? !getChainIdRaw().equals(that.getChainIdRaw())
                : that.getChainIdRaw() != null) {
            return false;
        }

        if (getBlockNumberRaw() != null
                ? !getBlockNumberRaw().equals(that.getBlockNumberRaw())
                : that.getBlockNumberRaw() != null) {
            return false;
        }
        if (getTransactionIndexRaw() != null
                ? !getTransactionIndexRaw().equals(that.getTransactionIndexRaw())
                : that.getTransactionIndexRaw() != null) {
            return false;
        }
        if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) {
            return false;
        }
        if (getTo() != null ? !getTo().equals(that.getTo()) : that.getTo() != null) {
            return false;
        }
        if (getValueRaw() != null
                ? !getValueRaw().equals(that.getValueRaw())
                : that.getValueRaw() != null) {
            return false;
        }
        if (getGasPriceRaw() != null
                ? !getGasPriceRaw().equals(that.getGasPriceRaw())
                : that.getGasPriceRaw() != null) {
            return false;
        }
        if (getGasRaw() != null
                ? !getGasRaw().equals(that.getGasRaw())
                : that.getGasRaw() != null) {
            return false;
        }
        if (getInput() != null ? !getInput().equals(that.getInput()) : that.getInput() != null) {
            return false;
        }
        if (getCreates() != null
                ? !getCreates().equals(that.getCreates())
                : that.getCreates() != null) {
            return false;
        }
        if (getPublicKey() != null
                ? !getPublicKey().equals(that.getPublicKey())
                : that.getPublicKey() != null) {
            return false;
        }
        if (getRaw() != null ? !getRaw().equals(that.getRaw()) : that.getRaw() != null) {
            return false;
        }
        if (getR() != null ? !getR().equals(that.getR()) : that.getR() != null) {
            return false;
        }
        if (getyParity() != null
                ? !getyParity().equals(that.getyParity())
                : that.getyParity() != null) {
            return false;
        }
        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) {
            return false;
        }
        if (getMaxFeePerGasRaw() != null
                ? !getMaxFeePerGasRaw().equals(that.getMaxFeePerGasRaw())
                : that.getMaxFeePerGasRaw() != null) {
            return false;
        }
        if (getMaxPriorityFeePerGasRaw() != null
                ? !getMaxPriorityFeePerGasRaw().equals(that.getMaxPriorityFeePerGasRaw())
                : that.getMaxPriorityFeePerGasRaw() != null) {
            return false;
        }

        if (getMaxFeePerBlobGasRaw() != null
                ? !getMaxFeePerBlobGasRaw().equals(that.getMaxFeePerBlobGasRaw())
                : that.getMaxFeePerBlobGasRaw() != null) {
            return false;
        }
        if (getBlobVersionedHashes() != null
                ? !getBlobVersionedHashes().equals(that.getBlobVersionedHashes())
                : that.getBlobVersionedHashes() != null) {
            return false;
        }
        if (getAccessList() != null
                ? !getAccessList().equals(that.getAccessList())
                : that.getAccessList() != null) {
            return false;
        }
        if (getAuthorizationList() !=null ? !getAuthorizationList().equals(that.getAuthorizationList()) :
                that.getAuthorizationList() != null) {
            return false;
        }
        return getS() != null ? getS().equals(that.getS()) : that.getS() == null;
    }

    @Override
    public int hashCode() {
        int result = getHash() != null ? getHash().hashCode() : 0;
        result = 31 * result + (getNonceRaw() != null ? getNonceRaw().hashCode() : 0);
        result = 31 * result + (getBlockHash() != null ? getBlockHash().hashCode() : 0);
        result = 31 * result + (getBlockNumberRaw() != null ? getBlockNumberRaw().hashCode() : 0);
        result = 31 * result + (getChainIdRaw() != null ? getChainIdRaw().hashCode() : 0);
        result =
                31 * result
                        + (getTransactionIndexRaw() != null
                                ? getTransactionIndexRaw().hashCode()
                                : 0);
        result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
        result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
        result = 31 * result + (getValueRaw() != null ? getValueRaw().hashCode() : 0);
        result = 31 * result + (getGasPriceRaw() != null ? getGasPriceRaw().hashCode() : 0);
        result = 31 * result + (getGasRaw() != null ? getGasRaw().hashCode() : 0);
        result = 31 * result + (getInput() != null ? getInput().hashCode() : 0);
        result = 31 * result + (getCreates() != null ? getCreates().hashCode() : 0);
        result = 31 * result + (getPublicKey() != null ? getPublicKey().hashCode() : 0);
        result = 31 * result + (getRaw() != null ? getRaw().hashCode() : 0);
        result = 31 * result + (getR() != null ? getR().hashCode() : 0);
        result = 31 * result + (getS() != null ? getS().hashCode() : 0);
        result = 31 * result + BigInteger.valueOf(getV()).hashCode();
        result = 31 * result + (getyParity() != null ? getyParity().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getMaxFeePerGasRaw() != null ? getMaxFeePerGasRaw().hashCode() : 0);
        result =
                31 * result
                        + (getMaxPriorityFeePerGasRaw() != null
                                ? getMaxPriorityFeePerGasRaw().hashCode()
                                : 0);
        result =
                31 * result
                        + (getMaxFeePerBlobGasRaw() != null
                                ? getMaxFeePerBlobGasRaw().hashCode()
                                : 0);
        result =
                31 * result
                        + (getBlobVersionedHashes() != null
                                ? getBlobVersionedHashes().hashCode()
                                : 0);
        result = 31 * result + (getAccessList() != null ? getAccessList().hashCode() : 0);
        result = 31 * result + (getAuthorizationList() != null ? getAuthorizationList().hashCode() : 0);
        return result;
    }
}
