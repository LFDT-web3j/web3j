/*
 * Copyright 2024 Web3 Labs Ltd.
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
package org.web3j.abi.datatypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;

/** 
 * Represents a Solidity custom error definition.
 * This class defines the structure of a custom error, not its values.
 * The parameters list contains TypeReferences that define what types of values
 * the error can contain, not the actual values themselves.
 */
public class CustomError {
    private final String name;
    private final List<TypeReference<?>> parameters;

    public CustomError(String name, List<TypeReference<?>> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public CustomError(String name) {
        this(name, new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public List<TypeReference<?>> getParameters() {
        return parameters;
    }

    /**
     * Returns the error signature in the format "ErrorName(type1,type2,...)"
     */
    public String getSignature() {
        StringBuilder signature = new StringBuilder();
        signature.append(name).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            signature.append(Utils.getSolidityTypeName(parameters.get(i)));
            if (i < parameters.size() - 1) {
                signature.append(",");
            }
        }
        signature.append(")");
        return signature.toString();
    }

    /**
     * Returns the first 4 bytes of the Keccak-256 hash of the error signature.
     * This is used to identify the error in transaction receipts.
     */
    public String getSelector() {
        return org.web3j.crypto.Hash.sha3String(getSignature()).substring(0, 10);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomError that = (CustomError) o;
        return this.getSignature().equals(that.getSignature());
    }

    @Override
    public int hashCode() {
        return getSignature().hashCode();
    }

    @Override
    public String toString() {
        return getSignature();
    }
} 