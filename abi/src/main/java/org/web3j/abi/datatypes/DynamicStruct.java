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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DynamicStruct extends DynamicArray<Type> implements StructType {

    public final List<Class<Type>> itemTypes = new ArrayList<>();

    public DynamicStruct(List<Type> values) {
        this(Type.class, values);
    }

    @SuppressWarnings("unchecked")
    private DynamicStruct(Class<Type> type, List<Type> values) {
        super(type, values);
        for (Type value : values) {
            itemTypes.add((Class<Type>) value.getClass());
        }
    }

    @Override
    public int bytes32PaddedLength() {
        return super.bytes32PaddedLength() + 32;
    }

    public DynamicStruct(Type... values) {
        this(Arrays.asList(values));
    }

    @SafeVarargs
    public DynamicStruct(Class<Type> type, Type... values) {
        this(type, Arrays.asList(values));
    }

    @Override
    public String getTypeAsString() {
        final StringBuilder type = new StringBuilder("(");
        for (int i = 0; i < itemTypes.size(); ++i) {
            final Class<Type> cls = itemTypes.get(i);
            if (StructType.class.isAssignableFrom(cls)
                    || DynamicArray.class.isAssignableFrom(cls)) {
                type.append(getValue().get(i).getTypeAsString());
            } else if (Array.class.isAssignableFrom(cls)) {
                type.append(getValue().get(i).getTypeAsString());
            } else {
                type.append(AbiTypes.getTypeAString(cls));
            }
            if (i < itemTypes.size() - 1) {
                type.append(",");
            }
        }
        type.append(")");
        return type.toString();
    }

    // overriding toString method in SFWJ file
    @Override
    public String toString() {
        // Try to use reflection
        try {
            Field[] declared = getClass().getDeclaredFields();
            List<Field> publicFields = new ArrayList<>();
            for (Field f : declared) {
                int mod = f.getModifiers();
                if (!Modifier.isStatic(mod) && Modifier.isPublic(mod) && !f.isSynthetic()) {
                    publicFields.add(f);
                }
            }

            if (!publicFields.isEmpty()) {
                StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("(");
                for (int i = 0; i < publicFields.size(); i++) {
                    Field f = publicFields.get(i);
                    Object value = f.get(this);
                    sb.append(f.getName()).append(": ").append(unwrap(value));
                    if (i < publicFields.size() - 1) sb.append(", ");
                }
                sb.append(")");
                return sb.toString();
            }
        } catch (Exception e) {
            // If reflection fails, fallback below
        }

        // original behaviour with field0, field1...
        StringBuilder sb = new StringBuilder("DynamicStruct(");
        List<Type> values = getValue();
        for (int i = 0; i < values.size(); i++) {
            sb.append("field").append(i).append(": ").append(values.get(i));
            if (i < values.size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    private static Object unwrap(Object o) {
        if (o == null) return null;

        return switch (o) {
            case DynamicStruct ds -> ds.toString();

            case Type<?> t -> unwrap(t.getValue());

            case Collection<?> col -> {
                List<Object> out = new ArrayList<>();
                for (Object e : col) out.add(unwrap(e));
                yield out;
            }

            case Object[] arr -> {
                List<Object> out = new ArrayList<>(arr.length);
                for (Object e : arr) out.add(unwrap(e));
                yield out;
            }

            default -> {
                if (o.getClass().isArray()) {
                    int n = Array.getLength(o);
                    List<Object> out = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) out.add(unwrap(Array.get(o, i)));
                    yield out;
                } else {
                    yield o;
                }
            }
        };
    }
}
