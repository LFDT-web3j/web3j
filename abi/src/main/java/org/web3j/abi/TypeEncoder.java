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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import org.web3j.abi.datatypes.NumericType;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Ufixed;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.primitive.PrimitiveType;
import org.web3j.utils.Numeric;

import static org.web3j.abi.datatypes.Type.MAX_BIT_LENGTH;
import static org.web3j.abi.datatypes.Type.MAX_BYTE_LENGTH;

/**
 * Ethereum Contract Application Binary Interface (ABI) encoding for types. Further details are
 * available <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">here</a>.
 */
public class TypeEncoder {

    private TypeEncoder() {}

    static boolean isDynamic(Type parameter) {
        return parameter instanceof DynamicBytes
                || parameter instanceof Utf8String
                || parameter instanceof DynamicArray
                || (parameter instanceof StaticArray
                        && DynamicStruct.class.isAssignableFrom(
                                ((StaticArray) parameter).getComponentType()));
    }

    @SuppressWarnings("unchecked")
    public static String encode(Type parameter) {
        if (parameter instanceof NumericType) {
            return encodeNumeric(((NumericType) parameter));
        } else if (parameter instanceof Address) {
            return encodeAddress((Address) parameter);
        } else if (parameter instanceof Bool) {
            return encodeBool((Bool) parameter);
        } else if (parameter instanceof Bytes) {
            return encodeBytes((Bytes) parameter);
        } else if (parameter instanceof DynamicBytes) {
            return encodeDynamicBytes((DynamicBytes) parameter);
        } else if (parameter instanceof Utf8String) {
            return encodeString((Utf8String) parameter);
        } else if (parameter instanceof StaticArray) {
            if (DynamicStruct.class.isAssignableFrom(
                    ((StaticArray) parameter).getComponentType())) {
                return encodeStaticArrayWithDynamicStruct((StaticArray) parameter);
            } else {
                return encodeArrayValues((StaticArray) parameter);
            }
        } else if (parameter instanceof DynamicStruct) {
            return encodeDynamicStruct((DynamicStruct) parameter);
        } else if (parameter instanceof DynamicArray) {
            return encodeDynamicArray((DynamicArray) parameter);
        } else if (parameter instanceof PrimitiveType) {
            return encode(((PrimitiveType) parameter).toSolidityType());
        } else {
            throw new UnsupportedOperationException(
                    "Type cannot be encoded: " + parameter.getClass());
        }
    }

    /**
     * Returns abi.encodePacked hex value for the supported types. First the value is encoded and
     * after the padding or length, in arrays cases, is removed resulting the packed encode hex
     * value
     *
     * @param parameter Value to be encoded
     * @return
     */
    public static String encodePacked(Type parameter) {
        if (parameter instanceof Utf8String) {
            // removePadding can also be used, but is not necessary
            return Numeric.toHexStringNoPrefix(
                    ((Utf8String) parameter).getValue().getBytes(StandardCharsets.UTF_8));
        } else if (parameter instanceof DynamicBytes) {
            // removePadding can also be used, but is not necessary
            return Numeric.toHexStringNoPrefix(((DynamicBytes) parameter).getValue());
        } else if (parameter instanceof DynamicArray) {
            return arrayEncodePacked((DynamicArray) parameter);
        } else if (parameter instanceof StaticArray) {
            return arrayEncodePacked((StaticArray) parameter);
        } else if (parameter instanceof PrimitiveType) {
            return encodePacked(((PrimitiveType) parameter).toSolidityType());
        } else {
            return removePadding(encode(parameter), parameter);
        }
    }

    /**
     * Remove padding from the static types and {@link Utf8String} after the encode was applied
     *
     * @param encodedValue Encoded value of the parameter
     * @param parameter Value which was encoded
     * @return The encoded value without padding
     */
    static String removePadding(String encodedValue, Type parameter) {
        if (parameter instanceof NumericType) {
            if (parameter instanceof Ufixed || parameter instanceof Fixed) {
                return encodedValue;
            }
            return encodedValue.substring(64 - ((NumericType) parameter).getBitSize() / 4, 64);
        } else if (parameter instanceof Address) {
            return encodedValue.substring(64 - ((Address) parameter).toUint().getBitSize() / 4, 64);
        } else if (parameter instanceof Bool) {
            return encodedValue.substring(62, 64);
        }
        if (parameter instanceof Bytes) {
            return encodedValue.substring(0, ((BytesType) parameter).getValue().length * 2);
        }
        if (parameter instanceof Utf8String) {
            int length =
                    ((Utf8String) parameter).getValue().getBytes(StandardCharsets.UTF_8).length;
            return encodedValue.substring(64, 64 + length * 2);
        }
        if (parameter instanceof DynamicBytes) {
            return encodedValue.substring(
                    64, 64 + ((DynamicBytes) parameter).getValue().length * 2);
        } else {
            throw new UnsupportedOperationException(
                    "Type cannot be encoded: " + parameter.getClass());
        }
    }

    /**
     * Encodes a static array containing a dynamic struct type. In this case, the array items are
     * decoded as dynamic values and have their offsets at the beginning of the encoding. Example:
     * For the following static array containing three elements: <code>StaticArray3</code>
     * enc([struct1, struct2, struct2]) = offset(enc(struct1)) offset(enc(struct2))
     * offset(enc(struct3)) enc(struct1) enc(struct2) enc(struct3)
     *
     * @param
     * @return
     */
    private static <T extends Type> String encodeStaticArrayWithDynamicStruct(Array<T> value) {
        String valuesOffsets = encodeDynamicsTypesArraysOffsets(value);
        String encodedValues = encodeArrayValues(value);

        StringBuilder result = new StringBuilder();
        result.append(valuesOffsets);
        result.append(encodedValues);
        return result.toString();
    }

    static String encodeAddress(Address address) {
        return encodeNumeric(address.toUint());
    }

    static String encodeNumeric(NumericType numericType) {
        byte[] rawValue = toByteArray(numericType);
        byte paddingValue = getPaddingValue(numericType);
        byte[] paddedRawValue = new byte[MAX_BYTE_LENGTH];
        if (paddingValue != 0) {
            for (int i = 0; i < paddedRawValue.length; i++) {
                paddedRawValue[i] = paddingValue;
            }
        }

        System.arraycopy(
                rawValue, 0, paddedRawValue, MAX_BYTE_LENGTH - rawValue.length, rawValue.length);
        return Numeric.toHexStringNoPrefix(paddedRawValue);
    }

    private static byte getPaddingValue(NumericType numericType) {
        if (numericType.getValue().signum() == -1) {
            return (byte) 0xff;
        } else {
            return 0;
        }
    }

    private static byte[] toByteArray(NumericType numericType) {
        BigInteger value = numericType.getValue();
        if (numericType instanceof Ufixed || numericType instanceof Uint) {
            if (value.bitLength() == MAX_BIT_LENGTH) {
                // As BigInteger is signed, if we have a 256 bit value, the resultant byte array
                // will contain a sign byte in it's MSB, which we should ignore for this unsigned
                // integer type.
                byte[] byteArray = new byte[MAX_BYTE_LENGTH];
                System.arraycopy(value.toByteArray(), 1, byteArray, 0, MAX_BYTE_LENGTH);
                return byteArray;
            }
        }
        return value.toByteArray();
    }

    static String encodeBool(Bool value) {
        byte[] rawValue = new byte[MAX_BYTE_LENGTH];
        if (value.getValue()) {
            rawValue[rawValue.length - 1] = 1;
        }
        return Numeric.toHexStringNoPrefix(rawValue);
    }

    static String encodeBytes(BytesType bytesType) {
        byte[] value = bytesType.getValue();
        int length = value.length;
        int mod = length % MAX_BYTE_LENGTH;

        byte[] dest;
        if (mod != 0) {
            int padding = MAX_BYTE_LENGTH - mod;
            dest = new byte[length + padding];
            System.arraycopy(value, 0, dest, 0, length);
        } else {
            dest = value;
        }
        return Numeric.toHexStringNoPrefix(dest);
    }

    static String encodeDynamicBytes(DynamicBytes dynamicBytes) {
        int size = dynamicBytes.getValue().length;
        String encodedLength = encode(new Uint(BigInteger.valueOf(size)));
        String encodedValue = encodeBytes(dynamicBytes);

        StringBuilder result = new StringBuilder();
        result.append(encodedLength);
        result.append(encodedValue);
        return result.toString();
    }

    static String encodeString(Utf8String string) {
        byte[] utfEncoded = string.getValue().getBytes(StandardCharsets.UTF_8);
        return encodeDynamicBytes(new DynamicBytes(utfEncoded));
    }

    static <T extends Type> String encodeArrayValues(Array<T> value) {
        StringBuilder result = new StringBuilder();
        for (Type type : value.getValue()) {
            result.append(encode(type));
        }
        return result.toString();
    }

    static String encodeDynamicStruct(final DynamicStruct value) {
        String encodedValues = encodeDynamicStructValues(value);

        StringBuilder result = new StringBuilder();
        result.append(encodedValues);
        return result.toString();
    }

    private static String encodeDynamicStructValues(final DynamicStruct value) {
        int staticSize = 0;
        for (int i = 0; i < value.getValue().size(); ++i) {
            final Type type = value.getValue().get(i);
            if (isDynamic(type)) {
                staticSize += 32;
            } else {
                staticSize += type.bytes32PaddedLength();
            }
        }
        int dynamicOffset = staticSize;
        final List<String> offsetsAndStaticValues = new ArrayList<>();
        final List<String> dynamicValues = new ArrayList<>();
        for (int i = 0; i < value.getValue().size(); ++i) {
            final Type type = value.getValue().get(i);
            if (isDynamic(type)) {
                offsetsAndStaticValues.add(
                        Numeric.toHexStringNoPrefix(
                                Numeric.toBytesPadded(
                                        new BigInteger(Long.toString(dynamicOffset)),
                                        MAX_BYTE_LENGTH)));
                String encodedValue = encode(type);
                dynamicValues.add(encodedValue);
                dynamicOffset += encodedValue.length() >> 1;
            } else {
                offsetsAndStaticValues.add(encode(value.getValue().get(i)));
            }
        }
        final List<String> data = new ArrayList<>();
        data.addAll(offsetsAndStaticValues);
        data.addAll(dynamicValues);
        return String.join("", data);
    }

    static <T extends Type> String encodeDynamicArray(DynamicArray<T> value) {
        int size = value.getValue().size();
        String encodedLength = encode(new Uint(BigInteger.valueOf(size)));
        String valuesOffsets = encodeArrayValuesOffsets(value);
        String encodedValues = encodeArrayValues(value);

        StringBuilder result = new StringBuilder();
        result.append(encodedLength);
        result.append(valuesOffsets);
        result.append(encodedValues);
        return result.toString();
    }

    /**
     * Encodes the array values offsets of the to be encrypted dynamic array, which are in our case
     * the heads of the encryption. Refer to
     *
     * @see <a
     *     href="https://docs.soliditylang.org/en/v0.5.3/abi-spec.html#formal-specification-of-the-encoding">encoding
     *     formal specification</a>
     *     <h2>Dynamic structs array encryption</h2>
     *     <p>An array of dynamic structs (ie, structs containing dynamic datatypes) is encoded in
     *     the following way: Considering X = [struct1, struct2] for example enc(X) = head(struct1)
     *     head(struct2) tail(struct1) tail(struct2) with: - tail(struct1) = enc(struct1) -
     *     tail(struct2) = enc(struct2) - head(struct1) = enc(len( head(struct1) head(struct2))) =
     *     enc(64), because the heads are 256bits - head(struct2) = enc(len( head(struct1)
     *     head(struct2) tail(struct1)))
     */
    private static <T extends Type> String encodeArrayValuesOffsets(DynamicArray<T> value) {
        StringBuilder result = new StringBuilder();
        boolean arrayOfBytes =
                !value.getValue().isEmpty() && value.getValue().get(0) instanceof DynamicBytes;
        boolean arrayOfString =
                !value.getValue().isEmpty() && value.getValue().get(0) instanceof Utf8String;
        boolean arrayOfDynamicStructs =
                !value.getValue().isEmpty() && value.getValue().get(0) instanceof DynamicStruct;
        boolean arrayOfDynamicArrays =
                !value.getValue().isEmpty() && value.getValue().get(0) instanceof DynamicArray;
        if (arrayOfBytes || arrayOfString) {
            long offset = 0;
            for (int i = 0; i < value.getValue().size(); i++) {
                if (i == 0) {
                    offset = value.getValue().size() * MAX_BYTE_LENGTH;
                } else {
                    int bytesLength =
                            arrayOfBytes
                                    ? ((byte[]) value.getValue().get(i - 1).getValue()).length
                                    : ((String) value.getValue().get(i - 1).getValue())
                                            .getBytes(StandardCharsets.UTF_8)
                                            .length;
                    int numberOfWords = (bytesLength + MAX_BYTE_LENGTH - 1) / MAX_BYTE_LENGTH;
                    int totalBytesLength = numberOfWords * MAX_BYTE_LENGTH;
                    offset += totalBytesLength + MAX_BYTE_LENGTH;
                }
                result.append(
                        Numeric.toHexStringNoPrefix(
                                Numeric.toBytesPadded(
                                        new BigInteger(Long.toString(offset)), MAX_BYTE_LENGTH)));
            }
        } else if (arrayOfDynamicArrays || arrayOfDynamicStructs) {
            result.append(encodeDynamicsTypesArraysOffsets(value));
        }
        return result.toString();
    }

    /**
     * Encodes arrays of structs or dynamic arrays elements offsets. To be used when encoding a
     * dynamic arrays or a static array containing dynamic structs,
     *
     * @param value DynamicArray or StaticArray containing dynamic structs
     * @return encoded array offset
     */
    private static <T extends Type> String encodeDynamicsTypesArraysOffsets(Array<T> value) {
        StringBuilder result = new StringBuilder();
        long offset = value.getValue().size();
        List<String> tailsEncoding =
                value.getValue().stream().map(TypeEncoder::encode).collect(Collectors.toList());
        for (int i = 0; i < value.getValue().size(); i++) {
            if (i == 0) {
                offset = offset * MAX_BYTE_LENGTH;
            } else {
                offset += tailsEncoding.get(i - 1).length() / 2;
            }
            result.append(
                    Numeric.toHexStringNoPrefix(
                            Numeric.toBytesPadded(
                                    new BigInteger(Long.toString(offset)), MAX_BYTE_LENGTH)));
        }
        return result.toString();
    }

    /**
     * Checks if the received array doesn't contain any element that can make the array unsupported
     * for abi.encodePacked
     *
     * @param value Array to which the abi.encodePacked should be applied
     * @param <T> Types of elements from the array
     * @return if the encodePacked is supported for the given array
     */
    private static <T extends Type> boolean isSupportingEncodedPacked(Array<T> value) {
        if (Utf8String.class.isAssignableFrom(value.getComponentType())
                || DynamicStruct.class.isAssignableFrom(value.getComponentType())
                || DynamicArray.class.isAssignableFrom(value.getComponentType())
                || StaticStruct.class.isAssignableFrom(value.getComponentType())
                || FixedPointType.class.isAssignableFrom(value.getComponentType())
                || DynamicBytes.class.isAssignableFrom(value.getComponentType())) {
            return false;
        }
        return true;
    }

    private static <T extends Type> String arrayEncodePacked(Array<T> values) {
        if (isSupportingEncodedPacked(values)) {
            if (values.getValue().isEmpty()) {
                return "";
            }
            if (values instanceof DynamicArray) {
                return encode(values).substring(64);
            } else if (values instanceof StaticArray) {
                return encode(values);
            }
        }
        throw new UnsupportedOperationException(
                "Type cannot be packed encoded: " + values.getClass());
    }
}
