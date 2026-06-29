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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.web3j.protocol.core.DefaultBlockParameter;

/**
 * Filter implementation as per <a
 * href="https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_newfilter">docs</a>.
 */
@JsonPropertyOrder({"address", "blockHash", "fromBlock", "toBlock", "topics"})
public class EthFilter extends Filter<EthFilter> {
    private DefaultBlockParameter fromBlock; // optional, params - defaults to latest for both
    private DefaultBlockParameter toBlock;
    private String blockHash; // optional, cannot be used together with fromBlock/toBlock
    private List<String> address; // spec. implies this can be single address as string or list

    public EthFilter() {
        super();
    }

    public EthFilter(
            DefaultBlockParameter fromBlock, DefaultBlockParameter toBlock, List<String> address) {
        super();
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
        this.address = address;
    }

    public EthFilter(
            DefaultBlockParameter fromBlock, DefaultBlockParameter toBlock, String address) {
        this(fromBlock, toBlock, Collections.singletonList(address));
    }

    public EthFilter(String blockHash) {
        super();
        this.blockHash = blockHash;
    }

    public EthFilter(String blockHash, String address) {
        this(null, null, Collections.singletonList(address));
        this.blockHash = blockHash;
    }

    public DefaultBlockParameter getFromBlock() {
        return fromBlock;
    }

    public DefaultBlockParameter getToBlock() {
        return toBlock;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public List<String> getAddress() {
        return address;
    }

    @Override
    @JsonIgnore
    EthFilter getThis() {
        return this;
    }
}
