/*
 * Copyright 2020 Web3 Labs Ltd.
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
package org.web3j.protocol.besu.response;

import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.web3j.utils.Numeric;

public class BesuSignerMetric {

    private final String address;
    private final String proposedBlockCount;
    private final String lastProposedBlockNumber;

    @JsonCreator
    public BesuSignerMetric(
            @JsonProperty(value = "address") final String address,
            @JsonProperty(value = "proposedBlockCount") final String proposedBlockCount,
            @JsonProperty(value = "lastProposedBlockNumber") final String lastProposedBlockNumber) {
        this.address = address;
        this.proposedBlockCount = proposedBlockCount;
        this.lastProposedBlockNumber = lastProposedBlockNumber;
    }

    public String getAddress() {
        return address;
    }

    public BigInteger getProposedBlockCount() {
        return Numeric.decodeQuantity(proposedBlockCount);
    }

    public BigInteger getLastProposedBlockNumber() {
        return Numeric.decodeQuantity(lastProposedBlockNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BesuSignerMetric that = (BesuSignerMetric) o;
        return getAddress().equals(that.getAddress())
                && Objects.equals(getProposedBlockCount(), that.getProposedBlockCount())
                && Objects.equals(getLastProposedBlockNumber(), that.getLastProposedBlockNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getProposedBlockCount(), getLastProposedBlockNumber());
    }
}
