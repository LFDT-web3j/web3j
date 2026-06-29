/*
 * Copyright 2021 Web3 Labs Ltd.
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

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"address", "storageKeys"})
public class AccessListObject {
    private String address;
    private List<String> storageKeys;

    public AccessListObject() {}

    public AccessListObject(String address, List<String> storageKeys) {
        this.address = address;
        this.storageKeys = storageKeys;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getStorageKeys() {
        return storageKeys;
    }

    public void setStorageKeys(List<String> storageKeys) {
        this.storageKeys = storageKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessListObject that = (AccessListObject) o;
        return Objects.equals(getAddress(), that.getAddress())
                && Objects.equals(getStorageKeys(), that.getStorageKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getStorageKeys());
    }
}
