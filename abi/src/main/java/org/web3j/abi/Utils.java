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
package org.web3j.abi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Fixed;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.StructType;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Ufixed;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.reflection.Parameterized;

/** Utility functions. */
public class Utils {
    private Utils() {}

    static <T extends Type> String getTypeName(TypeReference<T> typeReference) {
        try {
            java.lang.reflect.Type reflectedType = typeReference.getType();

            Class<?> type;
            if (reflectedType instanceof ParameterizedType) {
                type = (Class<?>) ((ParameterizedType) reflectedType).getRawType();
                return getParameterizedTypeName(typeReference, type);
            } else if (typeReference.getSubTypeReference() != null) {
                return getParameterizedTypeName(typeReference, typeReference.getClassType());
            } else {
                type = Class.forName(getTypeName(reflectedType));
                if (StructType.class.isAssignableFrom(type)) {
                    return getStructType(type);
                }
                return getSimpleTypeName(type);
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Invalid class reference provided", e);
        }
    }

    public static String getStructType(Class type) {
        final StringBuilder sb = new StringBuilder("(");
        Constructor constructor = findStructConstructor(type);
        Class[] itemTypes = constructor.getParameterTypes();
        for (int i = 0; i < itemTypes.length; ++i) {
            final Class cls = itemTypes[i];
            if (StructType.class.isAssignableFrom(cls)) {
                sb.append(getStructType(cls));
            } else {
                Class parameterAnnotation =
                        extractParameterFromAnnotation(constructor.getParameterAnnotations()[i]);
                if (parameterAnnotation != null) {
                    sb.append(getTypeName(getDynamicArrayTypeReference(parameterAnnotation)));
                } else {
                    sb.append(getTypeName(TypeReference.create(cls)));
                }
            }
            if (i < itemTypes.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static TypeReference<DynamicArray> getDynamicArrayTypeReference(Class parameter) {
        return new TypeReference<DynamicArray>() {
            @Override
            public TypeReference getSubTypeReference() {
                return TypeReference.create(parameter);
            }
        };
    }

    public static <T extends Type> Class<T> extractParameterFromAnnotation(
            Annotation[] parameterAnnotation) {
        for (Annotation a : parameterAnnotation) {
            if (Parameterized.class.isInstance(a)) {
                return (Class<T>) ((Parameterized) a).type();
            }
        }
        return null;
    }

    public static Constructor findStructConstructor(Class classType) {
        return Arrays.stream(classType.getDeclaredConstructors())
                .filter(
                        declaredConstructor ->
                                Arrays.stream(declaredConstructor.getParameterTypes())
                                        .allMatch(Type.class::isAssignableFrom))
                .findAny()
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "TypeReferenced struct must contain a constructor with types that extend Type"));
    }

    static String getSimpleTypeName(Class<?> type) {
        String simpleName = type.getSimpleName().toLowerCase();

        if (type.equals(Uint.class)
                || type.equals(Int.class)
                || type.equals(Ufixed.class)
                || type.equals(Fixed.class)) {
            return simpleName + "256";
        } else if (type.equals(Utf8String.class)) {
            return "string";
        } else if (type.equals(DynamicBytes.class)) {
            return "bytes";
        } else if (StructType.class.isAssignableFrom(type)) {
            return type.getName();
        } else {
            return simpleName;
        }
    }

    static <T extends Type, U extends Type> String getParameterizedTypeName(
            TypeReference<T> typeReference, Class<?> type) {

        try {
            if (type.equals(DynamicArray.class)) {
                Class<U> parameterizedType = getParameterizedTypeFromArray(typeReference);
                String parameterizedTypeName = simpleNameOrStruct(parameterizedType);
                return parameterizedTypeName + "[]";
            } else if (StaticArray.class.isAssignableFrom(type)) {
                Class<U> parameterizedType = getParameterizedTypeFromArray(typeReference);
                String parameterizedTypeName = simpleNameOrStruct(parameterizedType);
                final int length;
                if (TypeReference.StaticArrayTypeReference.class.isAssignableFrom(
                        typeReference.getClass())) {
                    length = ((TypeReference.StaticArrayTypeReference) typeReference).getSize();
                } else {
                    length = Integer.parseInt(type.getSimpleName().replaceAll("\\D+", ""));
                }
                return parameterizedTypeName + "[" + length + "]";
            } else {
                throw new UnsupportedOperationException("Invalid type provided " + type.getName());
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Invalid class reference provided", e);
        }
    }

    private static <U extends Type> String simpleNameOrStruct(Class<U> parameterizedType) {
        if (StructType.class.isAssignableFrom(parameterizedType)) {
            return getStructType(parameterizedType);
        }
        return getSimpleTypeName(parameterizedType);
    }

    @SuppressWarnings("unchecked")
    static <T extends Type> Class<T> getParameterizedTypeFromArray(TypeReference typeReference)
            throws ClassNotFoundException {

        if (typeReference.getSubTypeReference() != null) {
            return typeReference.getSubTypeReference().getClassType();
        }

        java.lang.reflect.Type type = typeReference.getType();
        java.lang.reflect.Type[] typeArguments =
                ((ParameterizedType) type).getActualTypeArguments();

        if (typeArguments[0] instanceof ParameterizedType) {
            return (Class<T>)
                    Class.forName(getTypeName(((ParameterizedType) typeArguments[0]).getRawType()));
        }

        String parameterizedTypeName = getTypeName(typeArguments[0]);
        return (Class<T>) Class.forName(parameterizedTypeName);
    }

    static <T extends Type> Class<T> getFullParameterizedTypeFromArray(TypeReference typeReference)
            throws ClassNotFoundException {

        java.lang.reflect.Type type = typeReference.getType();

        java.lang.reflect.Type typeArgument =
                ((ParameterizedType) type).getActualTypeArguments()[0];

        return (Class<T>)
                Class.forName(
                        ((ParameterizedType) typeArgument)
                                .getActualTypeArguments()[0].getTypeName());
    }

    @SuppressWarnings("unchecked")
    public static List<TypeReference<Type>> convert(List<TypeReference<?>> input) {
        List<TypeReference<Type>> result = new ArrayList<>(input.size());
        result.addAll(
                input.stream()
                        .map(typeReference -> (TypeReference<Type>) typeReference)
                        .collect(Collectors.toList()));
        return result;
    }

    public static <T, R extends Type<T>, E extends Type<T>> List<E> typeMap(
            List<List<T>> input, Class<E> outerDestType, Class<R> innerType) {
        List<E> result = new ArrayList<>();
        try {
            Constructor<E> constructor =
                    outerDestType.getDeclaredConstructor(Class.class, List.class);
            for (List<T> ts : input) {
                E e = constructor.newInstance(innerType, typeMap(ts, innerType));
                result.add(e);
            }
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InstantiationException
                | InvocationTargetException e) {
            throw new TypeMappingException(e);
        }
        return result;
    }

    public static <T, R extends Type<T>> List<R> typeMap(List<T> input, Class<R> destType)
            throws TypeMappingException {

        List<R> result = new ArrayList<>(input.size());

        if (!input.isEmpty()) {
            try {
                Constructor<R> constructor =
                        destType.getDeclaredConstructor(input.get(0).getClass());
                for (T value : input) {
                    result.add(constructor.newInstance(value));
                }
            } catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException
                    | InstantiationException e) {
                throw new TypeMappingException(e);
            }
        }
        return result;
    }

    /**
     * Returns flat list of canonical fields in a static struct. Example: struct Baz { Struct Bar {
     * int a, int b }, int c } will return {a, b, c}.
     *
     * @param classType Static struct type
     * @return Flat list of canonical fields in a nested struct
     */
    public static List<Field> staticStructNestedPublicFieldsFlatList(Class<Type> classType) {
        return staticStructsNestedFieldsFlatList(classType).stream()
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .collect(Collectors.toList());
    }

    /**
     * Goes over a static structs and enumerates all of its fields and nested structs fields
     * recursively.
     *
     * @param classType Static struct type
     * @return Flat list of all the fields nested in the struct
     */
    @SuppressWarnings("unchecked")
    public static List<Field> staticStructsNestedFieldsFlatList(Class<Type> classType) {
        List<Field> canonicalFields =
                Arrays.stream(classType.getDeclaredFields())
                        .filter(field -> !StaticStruct.class.isAssignableFrom(field.getType()))
                        .collect(Collectors.toList());
        List<Field> nestedFields =
                Arrays.stream(classType.getDeclaredFields())
                        .filter(field -> StaticStruct.class.isAssignableFrom(field.getType()))
                        .map(
                                field ->
                                        staticStructsNestedFieldsFlatList(
                                                (Class<Type>) field.getType()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        return Stream.concat(canonicalFields.stream(), nestedFields.stream())
                .collect(Collectors.toList());
    }

    /** Ports {@link java.lang.reflect.Type#getTypeName()}. */
    public static String getTypeName(java.lang.reflect.Type type) {
        try {
            return type.getTypeName();
        } catch (NoSuchMethodError e) {
            return getClassName((Class) type);
        }
    }

    /** Support java version < 8 Copied from {@link Class#getTypeName()}. */
    private static String getClassName(Class type) {
        if (type.isArray()) {
            try {
                Class<?> cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) {
                /*FALLTHRU*/
            }
        }

        return type.getName();
    }

    /**
     * Gets the Solidity type name for a given TypeReference.
     * This method handles both simple types and complex types (arrays, structs).
     *
     * @param typeReference the TypeReference to get the Solidity type name for
     * @return the Solidity type name (e.g. "uint256", "address", "string[]")
     */
    public static String getSolidityTypeName(TypeReference<?> typeReference) {
        try {
            java.lang.reflect.Type reflectedType = typeReference.getType();
            Class<?> type;

            if (reflectedType instanceof ParameterizedType) {
                type = (Class<?>) ((ParameterizedType) reflectedType).getRawType();
                if (DynamicArray.class.isAssignableFrom(type)) {
                    Class<?> componentType = getParameterizedTypeFromArray(typeReference);
                    return getSoliditySimpleTypeName(componentType) + "[]";
                } else if (StaticArray.class.isAssignableFrom(type)) {
                    Class<?> componentType = getParameterizedTypeFromArray(typeReference);
                    int length = ((TypeReference.StaticArrayTypeReference) typeReference).getSize();
                    return getSoliditySimpleTypeName(componentType) + "[" + length + "]";
                }
            }

            type = typeReference.getClassType();
            return getSoliditySimpleTypeName(type);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Invalid class reference provided", e);
        }
    }

    private static String getSoliditySimpleTypeName(Class<?> type) {
        if (Uint.class.isAssignableFrom(type)) {
            return "uint256";
        } else if (Int.class.isAssignableFrom(type)) {
            return "int256";
        } else if (Ufixed.class.isAssignableFrom(type)) {
            return "ufixed256";
        } else if (Fixed.class.isAssignableFrom(type)) {
            return "fixed256";
        } else if (Utf8String.class.isAssignableFrom(type)) {
            return "string";
        } else if (DynamicBytes.class.isAssignableFrom(type)) {
            return "bytes";
        } else if (org.web3j.abi.datatypes.Address.class.isAssignableFrom(type)) {
            return "address";
        } else if (org.web3j.abi.datatypes.Bool.class.isAssignableFrom(type)) {
            return "bool";
        } else if (org.web3j.abi.datatypes.Bytes.class.isAssignableFrom(type)) {
            String typeName = type.getSimpleName();
            return typeName.toLowerCase();
        } else if (StructType.class.isAssignableFrom(type)) {
            return getStructType(type);
        } else {
            return type.getSimpleName().toLowerCase();
        }
    }
}
