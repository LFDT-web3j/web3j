/*
 * Copyright 2026 Web3 Labs Ltd.
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.Uint256;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Encoding and decoding of static arrays whose elements are dynamic ({@code bytes[2]}, {@code
 * uint256[][2]}, {@code bytes[2][2]}, ...) and of nested static arrays ({@code uint256[2][2]}),
 * each followed by a {@code uint256} so that a wrong head size shows up as a wrong trailing value.
 *
 * <p>Expected encodings are produced by Foundry's {@code cast abi-encode}, which is used as an
 * independent reference implementation of the ABI spec.
 */
public class StaticArrayDynamicElementsTest {

    // cast abi-encode "f(bytes[2],uint256)" '[0x6c6f6c31,0x6f6d6732]' 1965
    private static final String BYTES2_UINT =
            ""
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "00000000000000000000000000000000000000000000000000000000000007ad"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6c6f6c3100000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6f6d673200000000000000000000000000000000000000000000000000000000";

    // cast abi-encode "f(string[2],uint256)" '["lol1","omg2"]' 1965
    private static final String STRING2_UINT =
            ""
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "00000000000000000000000000000000000000000000000000000000000007ad"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6c6f6c3100000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6f6d673200000000000000000000000000000000000000000000000000000000";

    // cast abi-encode "f(bytes[2],bytes,uint256)" '[0x6c6f6c31,0x6f6d6732]' 0x77617433 1965
    private static final String BYTES2_BYTES_UINT =
            ""
                    + "0000000000000000000000000000000000000000000000000000000000000060"
                    + "0000000000000000000000000000000000000000000000000000000000000120"
                    + "00000000000000000000000000000000000000000000000000000000000007ad"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6c6f6c3100000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6f6d673200000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "7761743300000000000000000000000000000000000000000000000000000000";

    // cast abi-encode "f(uint256[][2],uint256)" '[[1,2],[]]' 1965
    private static final String UINT_DYN2_UINT =
            ""
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "00000000000000000000000000000000000000000000000000000000000007ad"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "00000000000000000000000000000000000000000000000000000000000000a0"
                    + "0000000000000000000000000000000000000000000000000000000000000002"
                    + "0000000000000000000000000000000000000000000000000000000000000001"
                    + "0000000000000000000000000000000000000000000000000000000000000002"
                    + "0000000000000000000000000000000000000000000000000000000000000000";

    // cast abi-encode "f(bytes[2][2],uint256)" '[[0x6c6f6c31,0x6f6d6732],[0x77617433,0x79657334]]'
    // 1965
    private static final String BYTES2X2_UINT =
            ""
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "00000000000000000000000000000000000000000000000000000000000007ad"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000100"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6c6f6c3100000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6f6d673200000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "7761743300000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "7965733400000000000000000000000000000000000000000000000000000000";

    // cast abi-encode "f(uint256[2][2],uint256)" '[[1,2],[3,4]]' 1965
    private static final String UINT2X2_UINT =
            ""
                    + "0000000000000000000000000000000000000000000000000000000000000001"
                    + "0000000000000000000000000000000000000000000000000000000000000002"
                    + "0000000000000000000000000000000000000000000000000000000000000003"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "00000000000000000000000000000000000000000000000000000000000007ad";

    // cast abi-encode "f(uint256[][2][2],uint256)" '[[[1],[2,3]],[[],[4]]]' 1965
    private static final String UINT_DYN2X2_UINT =
            ""
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "00000000000000000000000000000000000000000000000000000000000007ad"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000120"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + "0000000000000000000000000000000000000000000000000000000000000001"
                    + "0000000000000000000000000000000000000000000000000000000000000001"
                    + "0000000000000000000000000000000000000000000000000000000000000002"
                    + "0000000000000000000000000000000000000000000000000000000000000002"
                    + "0000000000000000000000000000000000000000000000000000000000000003"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000060"
                    + "0000000000000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000001"
                    + "0000000000000000000000000000000000000000000000000000000000000004";

    // cast abi-encode "f(bytes[2][],uint256)" '[[0x6c6f6c31,0x6f6d6732],[0x77617433,0x79657334]]'
    // 1965
    private static final String BYTES2_DYN_UINT =
            ""
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "00000000000000000000000000000000000000000000000000000000000007ad"
                    + "0000000000000000000000000000000000000000000000000000000000000002"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000100"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6c6f6c3100000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "6f6d673200000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "7761743300000000000000000000000000000000000000000000000000000000"
                    + "0000000000000000000000000000000000000000000000000000000000000004"
                    + "7965733400000000000000000000000000000000000000000000000000000000";

    private static final Uint256 TRAILING = new Uint256(1965);

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(
                        "bytes[2],uint256",
                        BYTES2_UINT,
                        List.of(bytes2("lol1", "omg2"), TRAILING),
                        List.of(
                                new TypeReference<StaticArray2<DynamicBytes>>() {},
                                new TypeReference<Uint256>() {})),
                Arguments.of(
                        "string[2],uint256",
                        STRING2_UINT,
                        List.of(
                                new StaticArray2<>(
                                        Utf8String.class,
                                        new Utf8String("lol1"),
                                        new Utf8String("omg2")),
                                TRAILING),
                        List.of(
                                new TypeReference<StaticArray2<Utf8String>>() {},
                                new TypeReference<Uint256>() {})),
                Arguments.of(
                        "bytes[2],bytes,uint256",
                        BYTES2_BYTES_UINT,
                        List.of(bytes2("lol1", "omg2"), bytes("wat3"), TRAILING),
                        List.of(
                                new TypeReference<StaticArray2<DynamicBytes>>() {},
                                new TypeReference<DynamicBytes>() {},
                                new TypeReference<Uint256>() {})),
                Arguments.of(
                        "uint256[][2],uint256",
                        UINT_DYN2_UINT,
                        List.of(
                                new StaticArray2<>(DynamicArray.class, uints(1, 2), uints()),
                                TRAILING),
                        List.of(
                                new TypeReference<StaticArray2<DynamicArray<Uint256>>>() {},
                                new TypeReference<Uint256>() {})),
                Arguments.of(
                        "bytes[2][2],uint256",
                        BYTES2X2_UINT,
                        List.of(
                                new StaticArray2<>(
                                        StaticArray2.class,
                                        bytes2("lol1", "omg2"),
                                        bytes2("wat3", "yes4")),
                                TRAILING),
                        List.of(
                                new TypeReference<StaticArray2<StaticArray2<DynamicBytes>>>() {},
                                new TypeReference<Uint256>() {})),
                Arguments.of(
                        "uint256[2][2],uint256",
                        UINT2X2_UINT,
                        List.of(
                                new StaticArray2<>(StaticArray2.class, uints2(1, 2), uints2(3, 4)),
                                TRAILING),
                        List.of(
                                new TypeReference<StaticArray2<StaticArray2<Uint256>>>() {},
                                new TypeReference<Uint256>() {})),
                Arguments.of(
                        "uint256[][2][2],uint256",
                        UINT_DYN2X2_UINT,
                        List.of(
                                new StaticArray2<>(
                                        StaticArray2.class,
                                        new StaticArray2<>(
                                                DynamicArray.class, uints(1), uints(2, 3)),
                                        new StaticArray2<>(DynamicArray.class, uints(), uints(4))),
                                TRAILING),
                        List.of(
                                new TypeReference<
                                        StaticArray2<StaticArray2<DynamicArray<Uint256>>>>() {},
                                new TypeReference<Uint256>() {})),
                Arguments.of(
                        "bytes[2][],uint256",
                        BYTES2_DYN_UINT,
                        List.of(
                                new DynamicArray<>(
                                        StaticArray2.class,
                                        bytes2("lol1", "omg2"),
                                        bytes2("wat3", "yes4")),
                                TRAILING),
                        List.of(
                                new TypeReference<DynamicArray<StaticArray2<DynamicBytes>>>() {},
                                new TypeReference<Uint256>() {})));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    public void testEncodeMatchesReference(
            String signature,
            String expected,
            List<Type> values,
            List<TypeReference<?>> outputParameters) {
        assertEquals(expected, FunctionEncoder.encodeConstructor(values));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    public void testDecodeWithTypeReferenceClasses(
            String signature,
            String encoded,
            List<Type> expected,
            List<TypeReference<?>> outputParameters) {
        List<Type> decoded =
                FunctionReturnDecoder.decode("0x" + encoded, Utils.convert(outputParameters));

        assertEquals(expected, decoded);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    public void testDecodeWithSolidityTypeStrings(
            String signature,
            String encoded,
            List<Type> expected,
            List<TypeReference<?>> outputParameters)
            throws ClassNotFoundException {
        List<TypeReference<?>> references = new ArrayList<>();
        for (String solidityType : signature.split(",")) {
            references.add(TypeReference.makeTypeReference(solidityType));
        }

        List<Type> decoded =
                FunctionReturnDecoder.decode("0x" + encoded, Utils.convert(references));

        assertEquals(render(expected), render(decoded));
    }

    private static DynamicBytes bytes(String value) {
        return new DynamicBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private static StaticArray2<DynamicBytes> bytes2(String first, String second) {
        return new StaticArray2<>(DynamicBytes.class, bytes(first), bytes(second));
    }

    private static DynamicArray<Uint256> uints(long... values) {
        List<Uint256> result = new ArrayList<>();
        for (long value : values) {
            result.add(new Uint256(BigInteger.valueOf(value)));
        }
        return new DynamicArray<>(Uint256.class, result);
    }

    private static StaticArray2<Uint256> uints2(long first, long second) {
        return new StaticArray2<>(Uint256.class, new Uint256(first), new Uint256(second));
    }

    /**
     * Type references built from solidity type strings may resolve array component classes
     * differently from generic ones (e.g. a raw {@code StaticArray2} vs {@code
     * StaticArray2<Uint256>}), so compare the decoded values rather than the Type objects.
     */
    private static Object render(Object value) {
        if (value instanceof Type) {
            return render(((Type<?>) value).getValue());
        } else if (value instanceof List) {
            List<Object> result = new ArrayList<>();
            for (Object element : (List<?>) value) {
                result.add(render(element));
            }
            return result;
        } else if (value instanceof byte[]) {
            return Arrays.toString((byte[]) value);
        }
        return value;
    }
}
