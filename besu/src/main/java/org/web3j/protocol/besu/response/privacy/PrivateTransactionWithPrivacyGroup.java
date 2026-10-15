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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.web3j.utils.Base64String;

@JsonDeserialize(using = ValueDeserializer.None.class)
public class PrivateTransactionWithPrivacyGroup extends PrivateTransaction {

    private Base64String privacyGroupId;

    @JsonCreator
    public PrivateTransactionWithPrivacyGroup(
            @JsonProperty(value = "hash") final String hash,
            @JsonProperty(value = "nonce") final String nonce,
            @JsonProperty(value = "from") final String from,
            @JsonProperty(value = "to") final String to,
            @JsonProperty(value = "value") final String value,
            @JsonProperty(value = "gas") final String gas,
            @JsonProperty(value = "gasPrice") final String gasPrice,
            @JsonProperty(value = "input") final String input,
            @JsonProperty(value = "r") final String r,
            @JsonProperty(value = "s") final String s,
            @JsonProperty(value = "v") final String v,
            @JsonProperty(value = "privateFrom") final Base64String privateFrom,
            // FIXME: This label should be changed once
            // https://pegasys1.atlassian.net/browse/PAN-3009 goes into production
            @JsonProperty(value = "privateFor") final Base64String privacyGroupId,
            @JsonProperty(value = "restriction") final String restriction) {
        super(
                hash,
                nonce,
                from,
                to,
                value,
                gas,
                gasPrice,
                input,
                r,
                s,
                v,
                privateFrom,
                restriction);
        this.privacyGroupId = privacyGroupId;
    }

    public Base64String getPrivacyGroupId() {
        return privacyGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PrivateTransactionWithPrivacyGroup that = (PrivateTransactionWithPrivacyGroup) o;
        return getPrivacyGroupId().equals(that.getPrivacyGroupId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPrivacyGroupId());
    }
}
