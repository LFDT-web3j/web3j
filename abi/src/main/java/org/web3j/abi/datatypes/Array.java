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
package org.web3j.abi.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Fixed size array. */
public abstract class Array<T extends Type> implements Type<List<T>> {

    private final Class<T> type;
    protected final List<T> value;

    @Deprecated
    @SafeVarargs
    Array(String type, T... values) {
        this(type, Arrays.asList(values));
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    Array(String type, List<T> values) {
        this((Class<T>) AbiTypes.getType(type), values);
    }

    @Deprecated
    Array(String type) {
        this(type, new ArrayList<>());
    }

    @SafeVarargs
    Array(Class<T> type, T... values) {
        this(type, Arrays.asList(values));
    }

    Array(Class<T> type, List<T> values) {
        checkValid(type, values);

        this.type = type;
        this.value = values;
    }

    @Override
    public int bytes32PaddedLength() {
        int length = 0;
        for (T t : value) {
            int valueLength = t.bytes32PaddedLength();
            length += valueLength;
        }
        return length;
    }

    @Override
    public List<T> getValue() {
        return value;
    }

    public List<Object> getNativeValueCopy() {
        List<Object> copy = new ArrayList<>(value.size());
        for (T t : value) {
            // nested arrays, e.g. uint256[][], are converted at every level
            copy.add(t instanceof Array ? ((Array<?>) t).getNativeValueCopy() : t.getValue());
        }
        return Collections.unmodifiableList(copy);
    }

    public Class<T> getComponentType() {
        return type;
    }

    @Override
    public abstract String getTypeAsString();

    private void checkValid(Class<T> type, List<T> values) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Array<?> array = (Array<?>) o;

        if (!type.equals(array.type)) {
            return false;
        }
        return Objects.equals(value, array.value);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
