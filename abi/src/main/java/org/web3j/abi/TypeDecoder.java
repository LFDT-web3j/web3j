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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.web3j.abi.datatypes.AbiTypes;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.BytesType;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Fixed;
import org.web3j.abi.datatypes.FixedPointType;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.IntType;
import org.web3j.abi.datatypes.NumericType;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.StructType;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Ufixed;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint160;
import org.web3j.abi.datatypes.primitive.Double;
import org.web3j.abi.datatypes.primitive.Float;
import org.web3j.utils.Numeric;

import static org.web3j.abi.DefaultFunctionReturnDecoder.getDataOffset;
import static org.web3j.abi.TypeReference.makeTypeReference;
import static org.web3j.abi.Utils.findStructConstructor;
import static org.web3j.abi.Utils.getSimpleTypeName;
import static org.web3j.abi.Utils.staticStructNestedPublicFieldsFlatList;

/**
 * Ethereum Contract Application Binary Interface (ABI) decoding for types. Decoding is not
 * documented, but is the reverse of the encoding details located <a
 * href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">here</a>.
 *
 * <p>The public API is composed of "decode*" methods and provides backward-compatibility. See
 * https://github.com/hyperledger/web3j/issues/1591 for a discussion about decoding and possible
 * improvements.
 */
public class TypeDecoder {

    static final int MAX_BYTE_LENGTH_FOR_HEX_STRING = Type.MAX_BYTE_LENGTH << 1;

    private static final ConcurrentHashMap<Class<?>, Integer> TYPE_LENGTH_CACHE =
            new ConcurrentHashMap<>();

    public static Type instantiateType(String solidityType, Object value)
            throws InvocationTargetException,
                    NoSuchMethodException,
                    InstantiationException,
                    IllegalAccessException,
                    ClassNotFoundException {
        return instantiateType(makeTypeReference(solidityType), value);
    }

    public static Type instantiateType(TypeReference ref, Object value)
            throws NoSuchMethodException,
                    IllegalAccessException,
                    InvocationTargetException,
                    InstantiationException,
                    ClassNotFoundException {
        Class rc = ref.getClassType();
        if (Array.class.isAssignableFrom(rc)) {
            return instantiateArrayType(ref, value);
        }
        return instantiateAtomicType(rc, value);
    }

    public static <T extends Array> T decode(
            String input, int offset, TypeReference<T> typeReference) {
        Class cls = ((ParameterizedType) typeReference.getType()).getRawType().getClass();
        if (StaticArray.class.isAssignableFrom(cls)) {
            return decodeStaticArray(input, offset, typeReference, 1);
        } else if (DynamicArray.class.isAssignableFrom(cls)) {
            return decodeDynamicArray(input, offset, typeReference);
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported TypeReference: "
                            + cls.getName()
                            + ", only Array types can be passed as TypeReferences");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Type> T decode(String input, int offset, Class<T> type) {
        if (NumericType.class.isAssignableFrom(type)) {
            return (T) decodeNumeric(input.substring(offset), (Class<NumericType>) type);
        } else if (Address.class.isAssignableFrom(type)) {
            return (T) decodeAddress(input.substring(offset));
        } else if (Bool.class.isAssignableFrom(type)) {
            return (T) decodeBool(input, offset);
        } else if (Bytes.class.isAssignableFrom(type)) {
            return (T) decodeBytes(input, offset, (Class<Bytes>) type);
        } else if (DynamicBytes.class.isAssignableFrom(type)) {
            return (T) decodeDynamicBytes(input, offset);
        } else if (Utf8String.class.isAssignableFrom(type)) {
            return (T) decodeUtf8String(input, offset);
        } else if (Array.class.isAssignableFrom(type)) {
            throw new UnsupportedOperationException(
                    "Array types must be wrapped in a TypeReference");
        } else {
            throw new UnsupportedOperationException("Type cannot be encoded: " + type.getClass());
        }
    }

    public static <T extends Type> T decode(String input, Class<T> type) {
        return decode(input, 0, type);
    }

    public static <T extends Type> T decode(String input, TypeReference<?> type)
            throws ClassNotFoundException {
        return decode(input, 0, ((TypeReference<T>) type).getClassType());
    }

    public static Address decodeAddress(String input) {
        return new Address(decodeNumeric(input, Uint160.class));
    }

    public static <T extends NumericType> T decodeNumeric(String input, Class<T> type) {
        try {
            byte[] inputByteArray = Numeric.hexStringToByteArray(input);
            int typeLengthAsBytes = getTypeLengthInBytes(type);
            int valueOffset = Type.MAX_BYTE_LENGTH - typeLengthAsBytes;

            BigInteger numericValue;
            if (Uint.class.isAssignableFrom(type) || Ufixed.class.isAssignableFrom(type)) {
                numericValue = new BigInteger(1, inputByteArray, valueOffset, typeLengthAsBytes);
            } else {
                numericValue = new BigInteger(inputByteArray, valueOffset, typeLengthAsBytes);
            }
            return type.getConstructor(BigInteger.class).newInstance(numericValue);

        } catch (NoSuchMethodException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            throw new UnsupportedOperationException(
                    "Unable to create instance of " + type.getName(), e);
        }
    }

    static <T extends NumericType> int getTypeLengthInBytes(Class<T> type) {
        return getTypeLength(type) >> 3; // divide by 8
    }

    static <T extends NumericType> int getTypeLength(Class<T> type) {
        return TYPE_LENGTH_CACHE.computeIfAbsent(type, TypeDecoder::computeTypeLength);
    }

    private static int computeTypeLength(Class<?> type) {
        if (IntType.class.isAssignableFrom(type)) {
            String regex = "(" + Uint.class.getSimpleName() + "|" + Int.class.getSimpleName() + ")";
            String[] splitName = type.getSimpleName().split(regex);
            if (splitName.length == 2) {
                return Integer.parseInt(splitName[1]);
            }
        } else if (FixedPointType.class.isAssignableFrom(type)) {
            String regex =
                    "(" + Ufixed.class.getSimpleName() + "|" + Fixed.class.getSimpleName() + ")";
            String[] splitName = type.getSimpleName().split(regex);
            if (splitName.length == 2) {
                String[] bitsCounts = splitName[1].split("x");
                return Integer.parseInt(bitsCounts[0]) + Integer.parseInt(bitsCounts[1]);
            }
        }
        return Type.MAX_BIT_LENGTH;
    }

    static Type instantiateArrayType(TypeReference ref, Object value)
            throws NoSuchMethodException,
                    IllegalAccessException,
                    InvocationTargetException,
                    InstantiationException,
                    ClassNotFoundException {
        List values;
        if (value instanceof List) {
            values = (List) value;
        } else if (value.getClass().isArray()) {
            values = arrayToList(value);
        } else {
            throw new ClassCastException(
                    "Arg of type "
                            + value.getClass()
                            + " should be a list to instantiate web3j Array");
        }
        Constructor listcons;
        int arraySize =
                ref instanceof TypeReference.StaticArrayTypeReference
                        ? ((TypeReference.StaticArrayTypeReference) ref).getSize()
                        : -1;
        if (arraySize <= 0) {
            listcons = DynamicArray.class.getConstructor(Class.class, List.class);
        } else {
            Class<?> arrayClass =
                    Class.forName("org.web3j.abi.datatypes.generated.StaticArray" + arraySize);
            listcons = arrayClass.getConstructor(Class.class, List.class);
        }
        // create a list of arguments coerced to the correct type of sub-TypeReference
        ArrayList<Type> transformedList = new ArrayList<Type>(values.size());
        TypeReference subTypeReference = ref.getSubTypeReference();
        for (Object o : values) {
            transformedList.add(instantiateType(subTypeReference, o));
        }
        return (Type) listcons.newInstance(subTypeReference.getClassType(), transformedList);
    }

    static Type instantiateAtomicType(Class<?> referenceClass, Object value)
            throws NoSuchMethodException,
                    IllegalAccessException,
                    InvocationTargetException,
                    InstantiationException,
                    ClassNotFoundException {
        Object constructorArg = null;
        if (NumericType.class.isAssignableFrom(referenceClass)) {
            constructorArg = asBigInteger(value);
        } else if (BytesType.class.isAssignableFrom(referenceClass)) {
            if (value instanceof byte[]) {
                constructorArg = value;
            } else if (value instanceof BigInteger) {
                constructorArg = ((BigInteger) value).toByteArray();
            } else if (value instanceof String) {
                constructorArg = Numeric.hexStringToByteArray((String) value);
            }
        } else if (Utf8String.class.isAssignableFrom(referenceClass)) {
            constructorArg = value.toString();
        } else if (Address.class.isAssignableFrom(referenceClass)) {
            if (value instanceof BigInteger || value instanceof Uint160) {
                constructorArg = value;
            } else {
                constructorArg = value.toString();
            }
        } else if (Bool.class.isAssignableFrom(referenceClass)) {
            if (value instanceof Boolean) {
                constructorArg = value;
            } else {
                BigInteger bival = asBigInteger(value);
                constructorArg = bival == null ? null : !bival.equals(BigInteger.ZERO);
            }
        }
        if (constructorArg == null) {
            throw new InstantiationException(
                    "Could not create type "
                            + referenceClass
                            + " from arg "
                            + value.toString()
                            + " of type "
                            + value.getClass());
        }
        Class<?>[] types = new Class[] {constructorArg.getClass()};
        Constructor cons = referenceClass.getConstructor(types);
        return (Type) cons.newInstance(constructorArg);
    }

    @SuppressWarnings("unchecked")
    static <T extends Type> int getSingleElementLength(String input, int offset, Class<T> type) {
        if (input.length() == offset) {
            return 0;
        } else if (DynamicBytes.class.isAssignableFrom(type)
                || Utf8String.class.isAssignableFrom(type)) {
            // length field + data value
            return (decodeUintAsInt(input, offset) / Type.MAX_BYTE_LENGTH) + 2;
        } else if (StaticStruct.class.isAssignableFrom(type)) {
            return staticStructNestedPublicFieldsFlatList((Class<Type>) type).size();
        } else {
            return 1;
        }
    }

    static int decodeUintAsInt(String rawInput, int offset) {
        String input = rawInput.substring(offset, offset + MAX_BYTE_LENGTH_FOR_HEX_STRING);
        return decode(input, 0, Uint.class).getValue().intValue();
    }

    public static Bool decodeBool(String rawInput, int offset) {
        String input = rawInput.substring(offset, offset + MAX_BYTE_LENGTH_FOR_HEX_STRING);
        BigInteger numericValue = Numeric.toBigInt(input);
        boolean value = numericValue.equals(BigInteger.ONE);
        return new Bool(value);
    }

    public static <T extends Bytes> T decodeBytes(String input, Class<T> type) {
        return decodeBytes(input, 0, type);
    }

    public static <T extends Bytes> T decodeBytes(String input, int offset, Class<T> type) {
        try {
            String simpleName = type.getSimpleName();
            String[] splitName = simpleName.split(Bytes.class.getSimpleName());
            int length = Integer.parseInt(splitName[1]);
            int hexStringLength = length << 1;

            byte[] bytes =
                    Numeric.hexStringToByteArray(input.substring(offset, offset + hexStringLength));
            return type.getConstructor(byte[].class).newInstance(bytes);
        } catch (NoSuchMethodException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            throw new UnsupportedOperationException(
                    "Unable to create instance of " + type.getName(), e);
        }
    }

    public static DynamicBytes decodeDynamicBytes(String input, int offset) {
        int encodedLength = decodeUintAsInt(input, offset);
        int hexStringEncodedLength = encodedLength << 1;

        int valueOffset = offset + MAX_BYTE_LENGTH_FOR_HEX_STRING;

        String data = input.substring(valueOffset, valueOffset + hexStringEncodedLength);
        byte[] bytes = Numeric.hexStringToByteArray(data);

        return new DynamicBytes(bytes);
    }

    public static Utf8String decodeUtf8String(String input, int offset) {
        DynamicBytes dynamicBytesResult = decodeDynamicBytes(input, offset);
        byte[] bytes = dynamicBytesResult.getValue();

        return new Utf8String(new String(bytes, StandardCharsets.UTF_8));
    }

    /** Static array length cannot be passed as a type. */
    @SuppressWarnings("unchecked")
    public static <T extends Type> T decodeStaticArray(
            String input, int offset, TypeReference<T> typeReference, int length) {

        BiFunction<List<T>, String, T> function =
                (elements, typeName) -> {
                    if (elements.isEmpty()) {
                        throw new UnsupportedOperationException(
                                "Zero length fixed array is invalid type");
                    } else {
                        return instantiateStaticArray(elements, length);
                    }
                };

        return decodeArrayElements(input, offset, typeReference, length, function);
    }

    public static <T extends Type> T decodeStaticStruct(
            final String input, final int offset, final TypeReference<T> typeReference) {
        BiFunction<List<T>, String, T> function =
                (elements, typeName) -> {
                    if (elements.isEmpty()) {
                        throw new UnsupportedOperationException(
                                "Zero length fixed array is invalid type");
                    } else {
                        return instantiateStruct(typeReference, elements);
                    }
                };

        if (typeReference.getInnerTypes() != null) {
            return decodeStaticStructElementFromInnerTypes(input, offset, typeReference, function);
        }

        return decodeStaticStructElement(input, offset, typeReference, function);
    }

    // Counts the number of nested fields in a StaticStruct with inner types.
    private static <T extends Type> int countNestedFields(final TypeReference<T> typeReference) {
        try {
            if (StaticStruct.class.isAssignableFrom(typeReference.getClassType())) {
                return typeReference.getInnerTypes().stream()
                        .map((tr) -> countNestedFields(tr))
                        .reduce(0, (a, b) -> a + b);
            }

            return 1;
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(
                    "countNestedFields failed for " + Utils.getTypeName(typeReference.getType()),
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Type> T decodeStaticStructElementFromInnerTypes(
            final String input,
            final int offset,
            final TypeReference<T> typeReference,
            final BiFunction<List<T>, String, T> consumer) {
        try {
            final List<TypeReference<?>> innerTypes = typeReference.getInnerTypes();
            List<T> elements = new ArrayList<>(innerTypes.size());

            for (int i = 0, currOffset = offset; i < innerTypes.size(); i++) {
                T value;
                final TypeReference<T> innerType = (TypeReference<T>) innerTypes.get(i);
                final Class<T> declaredField = innerType.getClassType();

                if (StaticStruct.class.isAssignableFrom(declaredField)) {
                    final int nestedStructLength = countNestedFields(innerType) * 64;
                    value =
                            decodeStaticStruct(
                                    input.substring(currOffset, currOffset + nestedStructLength),
                                    0,
                                    innerType);
                    currOffset += nestedStructLength;
                } else {
                    value = decode(input.substring(currOffset, currOffset + 64), 0, declaredField);
                    currOffset += 64;
                }
                elements.add(value);
            }

            return consumer.apply(elements, getSimpleTypeName(typeReference.getClassType()));
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(
                    "Unable to access parameterized type "
                            + Utils.getTypeName(typeReference.getType()),
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Type> T decodeStaticStructElement(
            final String input,
            final int offset,
            final TypeReference<T> typeReference,
            final BiFunction<List<T>, String, T> consumer) {
        try {
            Class<T> classType = typeReference.getClassType();
            Constructor<?> constructor = findStructConstructor(classType);
            final int length = constructor.getParameterCount();
            List<T> elements = new ArrayList<>(length);

            for (int i = 0, currOffset = offset; i < length; i++) {
                T value;
                final Class<T> declaredField = (Class<T>) constructor.getParameterTypes()[i];

                if (StaticStruct.class.isAssignableFrom(declaredField)) {
                    final int nestedStructLength =
                            classType
                                            .getDeclaredFields()[i]
                                            .getType()
                                            .getConstructors()[0]
                                            .getParameters()
                                            .length
                                    * 64;
                    value =
                            decodeStaticStruct(
                                    input.substring(currOffset, currOffset + nestedStructLength),
                                    0,
                                    TypeReference.create(declaredField));
                    currOffset += nestedStructLength;
                } else {
                    value = decode(input.substring(currOffset, currOffset + 64), 0, declaredField);
                    currOffset += 64;
                }
                elements.add(value);
            }

            String typeName = getSimpleTypeName(classType);

            return consumer.apply(elements, typeName);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(
                    "Unable to access parameterized type "
                            + Utils.getTypeName(typeReference.getType()),
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Type> T instantiateStruct(
            final TypeReference<T> typeReference, final List<T> parameters) {
        try {
            Class<T> classType = typeReference.getClassType();
            if (classType.isAssignableFrom(DynamicStruct.class)) {
                return (T) new DynamicStruct((List<Type>) parameters);
            } else if (classType.isAssignableFrom(StaticStruct.class)) {
                return (T) new StaticStruct((List<Type>) parameters);
            } else {
                Constructor ctor = findStructConstructor(classType);
                ctor.setAccessible(true);
                return (T) ctor.newInstance(parameters.toArray());
            }
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(
                    "Constructor cannot accept" + Arrays.toString(parameters.toArray()), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Type> T decodeDynamicArray(
            String input, int offset, TypeReference<T> typeReference) {

        int length = decodeUintAsInt(input, offset);

        BiFunction<List<T>, String, T> function =
                (elements, typeName) -> (T) new DynamicArray(AbiTypes.getType(typeName), elements);

        int valueOffset = offset + MAX_BYTE_LENGTH_FOR_HEX_STRING;

        return decodeArrayElements(input, valueOffset, typeReference, length, function);
    }

    public static <T extends Type> T decodeDynamicStruct(
            String input, int offset, TypeReference<T> typeReference)
            throws ClassNotFoundException {

        BiFunction<List<T>, String, T> function =
                (elements, typeName) -> {
                    if (elements.isEmpty()) {
                        throw new UnsupportedOperationException(
                                "Zero length fixed array is invalid type");
                    }

                    return instantiateStruct(typeReference, elements);
                };

        if (typeReference.getClassType().isAssignableFrom(DynamicStruct.class)
                && typeReference.getInnerTypes() != null) {
            return decodeDynamicStructElementsFromInnerTypes(
                    input, offset, typeReference, function);
        }

        return decodeDynamicStructElements(input, offset, typeReference, function);
    }

    private static class ParameterOffsetTracker<T extends Type> {
        public final Map<Integer, T> parameters;
        public final List<Integer> parameterOffsets;
        public int staticOffset;
        public int dynamicParametersToProcess;

        ParameterOffsetTracker(
                final Map<Integer, T> parametersIn,
                final List<Integer> parameterOffsetsIn,
                int staticOffsetIn,
                int dynamicParametersToProcessIn) {
            this.parameters = parametersIn;
            this.parameterOffsets = parameterOffsetsIn;
            this.staticOffset = staticOffsetIn;
            this.dynamicParametersToProcess = dynamicParametersToProcess;
        }
    }

    private static <T extends Type>
            ParameterOffsetTracker<T> getDynamicOffsetsAndNonDynamicParameters(
                    final String input, final int offset, final TypeReference<T> typeReference)
                    throws ClassNotFoundException {
        ParameterOffsetTracker<T> tracker =
                new ParameterOffsetTracker<T>(new HashMap<>(), new ArrayList<>(), 0, 0);

        final List<TypeReference<?>> innerTypes = typeReference.getInnerTypes();
        for (int i = 0; i < innerTypes.size(); ++i) {
            final TypeReference<T> innerType = (TypeReference<T>) innerTypes.get(i);
            final Class<T> declaredField = innerType.getClassType();
            final T value;
            final int beginIndex = offset + tracker.staticOffset;
            if (isDynamic(declaredField)) {
                final int parameterOffset =
                        decodeDynamicStructDynamicParameterOffset(
                                        input.substring(beginIndex, beginIndex + 64))
                                + offset;
                tracker.parameterOffsets.add(parameterOffset);
                tracker.staticOffset += 64;
                tracker.dynamicParametersToProcess += 1;
            } else {
                if (StaticStruct.class.isAssignableFrom(declaredField)) {
                    value = decodeStaticStruct(input.substring(beginIndex), 0, innerType);
                    tracker.staticOffset += countNestedFields(innerType) * 64;
                } else {
                    value = decode(input.substring(beginIndex), 0, declaredField);
                    tracker.staticOffset += value.bytes32PaddedLength() * 2;
                }
                tracker.parameters.put(i, value);
            }
        }

        return tracker;
    }

    private static <T extends Type> List<T> getDynamicParametersWithTracker(
            final String input,
            final TypeReference<T> typeReference,
            final ParameterOffsetTracker<T> tracker)
            throws ClassNotFoundException {

        final List<TypeReference<?>> innerTypes = typeReference.getInnerTypes();
        int dynamicParametersProcessed = 0;
        for (int i = 0; i < innerTypes.size(); ++i) {
            final TypeReference<T> parameterTypeReference = (TypeReference<T>) innerTypes.get(i);
            final Class<T> declaredField = parameterTypeReference.getClassType();
            if (isDynamic(declaredField)) {
                final boolean isLastParameterInStruct =
                        dynamicParametersProcessed == (tracker.dynamicParametersToProcess - 1);
                final int parameterLength =
                        isLastParameterInStruct
                                ? input.length()
                                        - tracker.parameterOffsets.get(dynamicParametersProcessed)
                                : tracker.parameterOffsets.get(dynamicParametersProcessed + 1)
                                        - tracker.parameterOffsets.get(dynamicParametersProcessed);

                tracker.parameters.put(
                        i,
                        decodeDynamicParameterFromStructWithTypeReference(
                                input,
                                tracker.parameterOffsets.get(dynamicParametersProcessed),
                                parameterLength,
                                parameterTypeReference));
                dynamicParametersProcessed++;
            }
        }

        final List<T> elements = new ArrayList<>();
        for (int i = 0; i < innerTypes.size(); ++i) {
            elements.add(tracker.parameters.get(i));
        }

        return elements;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Type> T decodeDynamicStructElementsFromInnerTypes(
            final String input,
            final int offset,
            final TypeReference<T> typeReference,
            final BiFunction<List<T>, String, T> consumer)
            throws ClassNotFoundException {
        ParameterOffsetTracker<T> tracker =
                getDynamicOffsetsAndNonDynamicParameters(input, offset, typeReference);
        final List<T> parameters = getDynamicParametersWithTracker(input, typeReference, tracker);
        String typeName = getSimpleTypeName(typeReference.getClassType());
        return consumer.apply(parameters, typeName);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Type> T decodeDynamicStructElements(
            final String input,
            final int offset,
            final TypeReference<T> typeReference,
            final BiFunction<List<T>, String, T> consumer) {
        try {
            final Class<T> classType = typeReference.getClassType();
            Constructor<?> constructor = findStructConstructor(classType);
            final int length = constructor.getParameterCount();
            final Map<Integer, T> parameters = new HashMap<>();
            int staticOffset = 0;
            final List<Integer> parameterOffsets = new ArrayList<>();
            for (int i = 0; i < length; ++i) {
                final Class<T> declaredField = (Class<T>) constructor.getParameterTypes()[i];
                final T value;
                final int beginIndex = offset + staticOffset;
                if (isDynamic(declaredField)) {
                    final int parameterOffset =
                            decodeDynamicStructDynamicParameterOffset(
                                            input.substring(beginIndex, beginIndex + 64))
                                    + offset;
                    parameterOffsets.add(parameterOffset);
                    staticOffset += 64;
                } else {
                    if (StaticStruct.class.isAssignableFrom(declaredField)) {
                        value =
                                decodeStaticStruct(
                                        input.substring(beginIndex),
                                        0,
                                        TypeReference.create(declaredField));
                        staticOffset +=
                                staticStructNestedPublicFieldsFlatList((Class<Type>) declaredField)
                                                .size()
                                        * MAX_BYTE_LENGTH_FOR_HEX_STRING;
                    } else {
                        value = decode(input.substring(beginIndex), 0, declaredField);
                        staticOffset += value.bytes32PaddedLength() * 2;
                    }
                    parameters.put(i, value);
                }
            }
            int dynamicParametersProcessed = 0;
            int dynamicParametersToProcess =
                    getDynamicStructDynamicParametersCount(constructor.getParameterTypes());
            for (int i = 0; i < length; ++i) {
                final Class<T> declaredField = (Class<T>) constructor.getParameterTypes()[i];
                if (isDynamic(declaredField)) {
                    final boolean isLastParameterInStruct =
                            dynamicParametersProcessed == (dynamicParametersToProcess - 1);
                    final int parameterLength =
                            isLastParameterInStruct
                                    ? input.length()
                                            - parameterOffsets.get(dynamicParametersProcessed)
                                    : parameterOffsets.get(dynamicParametersProcessed + 1)
                                            - parameterOffsets.get(dynamicParametersProcessed);
                    final Class<T> parameterFromAnnotation =
                            Utils.extractParameterFromAnnotation(
                                    constructor.getParameterAnnotations()[i]);
                    parameters.put(
                            i,
                            decodeDynamicParameterFromStruct(
                                    input,
                                    parameterOffsets.get(dynamicParametersProcessed),
                                    parameterLength,
                                    declaredField,
                                    parameterFromAnnotation));
                    dynamicParametersProcessed++;
                }
            }

            String typeName = getSimpleTypeName(classType);

            final List<T> elements = new ArrayList<>();
            for (int i = 0; i < length; ++i) {
                elements.add(parameters.get(i));
            }

            return consumer.apply(elements, typeName);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(
                    "Unable to access parameterized type "
                            + Utils.getTypeName(typeReference.getType()),
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Type> int getDynamicStructDynamicParametersCount(
            final Class<?>[] cls) {
        return (int) Arrays.stream(cls).filter(c -> isDynamic((Class<T>) c)).count();
    }

    private static <T extends Type> T decodeDynamicParameterFromStruct(
            final String input,
            final int parameterOffset,
            final int parameterLength,
            final Class<T> declaredField,
            final Class<T> parameter)
            throws ClassNotFoundException {
        final String dynamicElementData =
                input.substring(parameterOffset, parameterOffset + parameterLength);

        final T value;
        if (DynamicStruct.class.isAssignableFrom(declaredField)) {
            value = decodeDynamicStruct(dynamicElementData, 0, TypeReference.create(declaredField));
        } else if (DynamicArray.class.isAssignableFrom(declaredField)) {
            if (parameter == null) {
                throw new RuntimeException(
                        "parameter can not be null, try to use annotation @Parameterized to specify the parameter type");
            }
            value =
                    (T)
                            decodeDynamicArray(
                                    dynamicElementData,
                                    0,
                                    Utils.getDynamicArrayTypeReference(parameter));
        } else {
            value = decode(dynamicElementData, declaredField);
        }
        return value;
    }

    private static <T extends Type> T decodeDynamicParameterFromStructWithTypeReference(
            final String input,
            final int parameterOffset,
            final int parameterLength,
            final TypeReference<T> parameterTypeReference)
            throws ClassNotFoundException {
        final String dynamicElementData =
                input.substring(parameterOffset, parameterOffset + parameterLength);
        final Class<T> declaredField = parameterTypeReference.getClassType();

        final T value;
        if (DynamicStruct.class.isAssignableFrom(declaredField)) {
            value = decodeDynamicStruct(dynamicElementData, 0, parameterTypeReference);
        } else if (DynamicArray.class.isAssignableFrom(declaredField)) {
            value = (T) decodeDynamicArray(dynamicElementData, 0, parameterTypeReference);
        } else {
            value = decode(dynamicElementData, declaredField);
        }
        return value;
    }

    private static int decodeDynamicStructDynamicParameterOffset(final String input) {
        return (decodeUintAsInt(input, 0) * 2);
    }

    static <T extends Type> boolean isDynamic(Class<T> parameter) {
        return DynamicBytes.class.isAssignableFrom(parameter)
                || Utf8String.class.isAssignableFrom(parameter)
                || DynamicArray.class.isAssignableFrom(parameter)
                || DynamicStruct.class.isAssignableFrom(parameter);
    }

    static BigInteger asBigInteger(Object arg) {
        if (arg instanceof BigInteger) {
            return (BigInteger) arg;
        } else if (arg instanceof BigDecimal) {
            return ((BigDecimal) arg).toBigInteger();
        } else if (arg instanceof String) {
            return Numeric.toBigInt((String) arg);
        } else if (arg instanceof byte[]) {
            return Numeric.toBigInt((byte[]) arg);
        } else if (arg instanceof Double
                || arg instanceof Float
                || arg instanceof java.lang.Double
                || arg instanceof java.lang.Float) {
            return BigDecimal.valueOf(((Number) arg).doubleValue()).toBigInteger();
        } else if (arg instanceof Number) {
            return BigInteger.valueOf(((Number) arg).longValue());
        }
        return null;
    }

    static List arrayToList(Object array) {
        int len = java.lang.reflect.Array.getLength(array);
        ArrayList<Object> rslt = new ArrayList<Object>(len);
        for (int i = 0; i < len; i++) {
            rslt.add(java.lang.reflect.Array.get(array, i));
        }
        return rslt;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Type> T instantiateStaticArray(List<T> elements, int length) {
        try {
            Class<? extends StaticArray> arrayClass =
                    (Class<? extends StaticArray>)
                            Class.forName("org.web3j.abi.datatypes.generated.StaticArray" + length);
            return (T) arrayClass.getConstructor(List.class).newInstance(elements);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static <T extends Type> T decodeArrayElements(
            String input,
            int offset,
            TypeReference<T> typeReference,
            int length,
            BiFunction<List<T>, String, T> consumer) {

        try {
            Class<T> cls = Utils.getParameterizedTypeFromArray(typeReference);
            List<T> elements = new ArrayList<>(length);
            if (StructType.class.isAssignableFrom(cls)) {
                int currOffset = offset;
                for (int i = 0; i < length; i++) {
                    T value;
                    if (DynamicStruct.class.isAssignableFrom(cls)) {
                        if (Optional.ofNullable(typeReference)
                                .map(x -> x.getSubTypeReference())
                                .map(x -> x.getInnerTypes())
                                .isPresent()) {
                            value =
                                    TypeDecoder.decodeDynamicStruct(
                                            input,
                                            offset
                                                    + getDataOffset(
                                                            input, currOffset, typeReference),
                                            (TypeReference<T>)
                                                    new TypeReference<DynamicStruct>(
                                                            typeReference.isIndexed(),
                                                            typeReference
                                                                    .getSubTypeReference()
                                                                    .getInnerTypes()) {});
                            currOffset +=
                                    getSingleElementLength(input, currOffset, cls)
                                            * MAX_BYTE_LENGTH_FOR_HEX_STRING;
                        } else {
                            value =
                                    TypeDecoder.decodeDynamicStruct(
                                            input,
                                            offset
                                                    + getDataOffset(
                                                            input, currOffset, typeReference),
                                            TypeReference.create(cls));
                            currOffset +=
                                    getSingleElementLength(input, currOffset, cls)
                                            * MAX_BYTE_LENGTH_FOR_HEX_STRING;
                        }
                    } else {
                        if (Optional.ofNullable(typeReference)
                                .map(x -> x.getSubTypeReference())
                                .map(x -> x.getInnerTypes())
                                .isPresent()) {
                            value =
                                    TypeDecoder.decodeStaticStruct(
                                            input,
                                            currOffset,
                                            (TypeReference<T>) typeReference.getSubTypeReference());
                            currOffset +=
                                    countNestedFields(typeReference.getSubTypeReference())
                                            * MAX_BYTE_LENGTH_FOR_HEX_STRING;
                        } else {
                            value =
                                    TypeDecoder.decodeStaticStruct(
                                            input, currOffset, TypeReference.create(cls));
                            currOffset +=
                                    getSingleElementLength(input, currOffset, cls)
                                            * MAX_BYTE_LENGTH_FOR_HEX_STRING;
                        }
                    }
                    elements.add(value);
                }

                String typeName = getSimpleTypeName(cls);

                return consumer.apply(elements, typeName);
            } else if (Array.class.isAssignableFrom(cls)) {
                for (int i = 0, currOffset = offset; i < length; i++) {
                    T value;
                    if (DynamicArray.class.isAssignableFrom(cls)) {
                        value =
                                (T)
                                        TypeDecoder.decodeDynamicArray(
                                                input,
                                                offset
                                                        + getDataOffset(
                                                                input, currOffset, typeReference),
                                                Utils.getDynamicArrayTypeReference(
                                                        Utils.getFullParameterizedTypeFromArray(
                                                                typeReference)));
                        currOffset +=
                                getSingleElementLength(input, currOffset, cls)
                                        * MAX_BYTE_LENGTH_FOR_HEX_STRING;
                    } else {
                        String typeName = cls.getSimpleName();
                        String extractedLength =
                                typeName.substring(typeName.replaceAll("[0-9]+$", "").length());
                        int staticLength =
                                extractedLength.isEmpty() ? 0 : Integer.parseInt(extractedLength);
                        TypeReference innerType =
                                TypeReference.create(
                                        Utils.getFullParameterizedTypeFromArray(typeReference));

                        TypeReference.StaticArrayTypeReference staticReference =
                                new TypeReference.StaticArrayTypeReference<StaticArray>(
                                        staticLength) {

                                    @Override
                                    public TypeReference getSubTypeReference() {
                                        return innerType;
                                    }

                                    @Override
                                    public boolean isIndexed() {
                                        return false;
                                    }

                                    @Override
                                    public java.lang.reflect.Type getType() {
                                        return new ParameterizedType() {
                                            @Override
                                            public java.lang.reflect.Type[]
                                                    getActualTypeArguments() {
                                                return new java.lang.reflect.Type[] {
                                                    innerType.getType()
                                                };
                                            }

                                            @Override
                                            public java.lang.reflect.Type getRawType() {
                                                return cls;
                                            }

                                            @Override
                                            public java.lang.reflect.Type getOwnerType() {
                                                return Class.class;
                                            }
                                        };
                                    }
                                };
                        value =
                                (T)
                                        TypeDecoder.decodeStaticArray(
                                                input, currOffset, staticReference, staticLength);
                        currOffset +=
                                ((decodeUintAsInt(input, currOffset) / Type.MAX_BYTE_LENGTH) + 2)
                                        * MAX_BYTE_LENGTH_FOR_HEX_STRING;
                    }
                    elements.add(value);
                }
                return consumer.apply(elements, cls.getName());
            } else {
                int currOffset = offset;
                for (int i = 0; i < length; i++) {
                    T value;
                    if (isDynamic(cls)) {
                        int hexStringDataOffset = getDataOffset(input, currOffset, typeReference);
                        value = decode(input, offset + hexStringDataOffset, cls);
                        currOffset += MAX_BYTE_LENGTH_FOR_HEX_STRING;
                    } else {
                        value = decode(input, currOffset, cls);
                        currOffset +=
                                getSingleElementLength(input, currOffset, cls)
                                        * MAX_BYTE_LENGTH_FOR_HEX_STRING;
                    }
                    elements.add(value);
                }

                String typeName = getSimpleTypeName(cls);

                return consumer.apply(elements, typeName);
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(
                    "Unable to access parameterized type "
                            + Utils.getTypeName(typeReference.getType()),
                    e);
        }
    }
}
