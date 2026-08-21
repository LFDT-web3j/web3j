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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.StaticArray3;
import org.web3j.abi.datatypes.generated.StaticArray4;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionReturnDecoderTest {

    @Test
    public void testSimpleFunctionDecode() {
        Function function =
                new Function(
                        "test",
                        Collections.<Type>emptyList(),
                        Collections.singletonList(new TypeReference<Uint>() {}));

        assertEquals(
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000037",
                        function.getOutputParameters()),
                (Collections.singletonList(new Uint(BigInteger.valueOf(55)))));
    }

    @Test
    public void testSimpleFunctionStringResultDecode() {
        Function function =
                new Function(
                        "simple",
                        Arrays.asList(),
                        Collections.singletonList(new TypeReference<Utf8String>() {}));

        List<Type> utf8Strings =
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000020"
                                + "000000000000000000000000000000000000000000000000000000000000000d"
                                + "6f6e65206d6f72652074696d6500000000000000000000000000000000000000",
                        function.getOutputParameters());

        assertEquals(utf8Strings.get(0).getValue(), ("one more time"));
    }

    @Test
    public void testFunctionEmptyStringResultDecode() {
        Function function =
                new Function(
                        "test",
                        Collections.emptyList(),
                        Collections.singletonList(new TypeReference<Utf8String>() {}));

        List<Type> utf8Strings =
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000020"
                                + "0000000000000000000000000000000000000000000000000000000000000000",
                        function.getOutputParameters());

        assertEquals(utf8Strings.get(0).getValue(), (""));
    }

    @Test
    public void testMultipleResultFunctionDecode() {
        Function function =
                new Function(
                        "test",
                        Collections.<Type>emptyList(),
                        Arrays.asList(new TypeReference<Uint>() {}, new TypeReference<Uint>() {}));

        assertEquals(
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000037"
                                + "0000000000000000000000000000000000000000000000000000000000000007",
                        function.getOutputParameters()),
                (Arrays.asList(new Uint(BigInteger.valueOf(55)), new Uint(BigInteger.valueOf(7)))));
    }

    @Test
    public void testDecodeMultipleStringValues() {
        Function function =
                new Function(
                        "function",
                        Collections.<Type>emptyList(),
                        Arrays.asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));

        assertEquals(
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000080"
                                + "00000000000000000000000000000000000000000000000000000000000000c0"
                                + "0000000000000000000000000000000000000000000000000000000000000100"
                                + "0000000000000000000000000000000000000000000000000000000000000140"
                                + "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6465663100000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6768693100000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6a6b6c3100000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6d6e6f3200000000000000000000000000000000000000000000000000000000",
                        function.getOutputParameters()),
                (Arrays.asList(
                        new Utf8String("def1"), new Utf8String("ghi1"),
                        new Utf8String("jkl1"), new Utf8String("mno2"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeStaticArrayValue() {
        List<TypeReference<Type>> outputParameters = new ArrayList<>(1);
        outputParameters.add(
                (TypeReference)
                        new TypeReference.StaticArrayTypeReference<StaticArray<Uint256>>(2) {});
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {});

        List<Type> decoded =
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000037"
                                + "0000000000000000000000000000000000000000000000000000000000000001"
                                + "000000000000000000000000000000000000000000000000000000000000000a",
                        outputParameters);

        StaticArray2<Uint256> uint256StaticArray2 =
                new StaticArray2<>(
                        new Uint256(BigInteger.valueOf(55)), new Uint256(BigInteger.ONE));

        List<Type> expected = Arrays.asList(uint256StaticArray2, new Uint256(BigInteger.TEN));
        assertEquals(decoded, (expected));
    }

    @Test
    public void testVoidResultFunctionDecode() {
        Function function = new Function("test", Collections.emptyList(), Collections.emptyList());

        assertEquals(
                FunctionReturnDecoder.decode("0x", function.getOutputParameters()),
                (Collections.emptyList()));
    }

    @Test
    public void testEmptyResultFunctionDecode() {
        Function function =
                new Function(
                        "test",
                        Collections.emptyList(),
                        Collections.singletonList(new TypeReference<Uint>() {}));

        assertEquals(
                FunctionReturnDecoder.decode("0x", function.getOutputParameters()),
                (Collections.emptyList()));
    }

    @Test
    public void testDecodeIndexedUint256Value() {
        Uint256 value = new Uint256(BigInteger.TEN);
        String encoded = TypeEncoder.encodeNumeric(value);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(encoded, new TypeReference<Uint256>() {}),
                (value));
    }

    @Test
    public void testDecodeIndexedStringValue() {
        Utf8String string = new Utf8String("some text");
        String encoded = TypeEncoder.encodeString(string);
        String hash = Hash.sha3(encoded);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(hash, new TypeReference<Utf8String>() {}),
                (new Bytes32(Numeric.hexStringToByteArray(hash))));
    }

    @Test
    public void testDecodeIndexedBytes32Value() {
        String rawInput = "0x1234567890123456789012345678901234567890123456789012345678901234";
        byte[] rawInputBytes = Numeric.hexStringToByteArray(rawInput);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(rawInput, new TypeReference<Bytes32>() {}),
                (new Bytes32(rawInputBytes)));
    }

    @Test
    public void testDecodeIndexedBytes16Value() {
        String rawInput = "0x1234567890123456789012345678901200000000000000000000000000000000";
        byte[] rawInputBytes = Numeric.hexStringToByteArray(rawInput.substring(0, 34));

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(rawInput, new TypeReference<Bytes16>() {}),
                (new Bytes16(rawInputBytes)));
    }

    @Test
    public void testDecodeIndexedDynamicBytesValue() {
        DynamicBytes bytes = new DynamicBytes(new byte[] {1, 2, 3, 4, 5});
        String encoded = TypeEncoder.encodeDynamicBytes(bytes);
        String hash = Hash.sha3(encoded);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(
                        hash, new TypeReference<DynamicBytes>() {}),
                (new Bytes32(Numeric.hexStringToByteArray(hash))));
    }

    @Test
    public void testDecodeIndexedDynamicArrayValue() {
        DynamicArray<Uint256> array =
                new DynamicArray<>(Uint256.class, new Uint256(BigInteger.TEN));

        String encoded = TypeEncoder.encodeDynamicArray(array);
        String hash = Hash.sha3(encoded);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(
                        hash, new TypeReference<DynamicArray>() {}),
                (new Bytes32(Numeric.hexStringToByteArray(hash))));
    }

    @Test
    public void testDecodeStaticStruct() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000064";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getBarFunction.getOutputParameters()),
                Collections.singletonList(
                        new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.valueOf(100))));
    }

    @Test
    public void testDecodeDynamicStruct() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getFooFunction.getOutputParameters()),
                Collections.singletonList(new AbiV2TestFixture.Foo("id", "name")));
    }

    @Test
    public void testDecodeDynamicStruct3() {
        AbiV2TestFixture.Nazz nazz =
                new AbiV2TestFixture.Nazz(
                        Collections.singletonList(
                                new AbiV2TestFixture.Nazzy(
                                        Arrays.asList(
                                                new AbiV2TestFixture.Foo("a", "b"),
                                                new AbiV2TestFixture.Foo("c", "d")))),
                        new BigInteger("100"));
        String rawInput = FunctionEncoder.encodeConstructor(Collections.singletonList(nazz));

        List<Type> decoded =
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getNazzFunction.getOutputParameters());
        assertEquals(Collections.singletonList(nazz).get(0).toString(), decoded.get(0).toString());
    }

    @Test
    public void testDecodeDynamicStruct2() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getBozFunction.getOutputParameters()),
                Collections.singletonList(new AbiV2TestFixture.Boz(BigInteger.ONE, "id")));
    }

    @Test
    public void testDecodeStaticStructNested() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000001";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getFuzzFunction.getOutputParameters()),
                Collections.singletonList(
                        new AbiV2TestFixture.Fuzz(
                                new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN),
                                BigInteger.ONE)));
    }

    @Test
    public void testDecodeMultipleStaticStructNested() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000001";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getFuzzFuzzFunction.getOutputParameters()),
                Arrays.asList(
                        new AbiV2TestFixture.Fuzz(
                                new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN),
                                BigInteger.ONE),
                        new AbiV2TestFixture.Fuzz(
                                new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN),
                                BigInteger.ONE)));
    }

    @Test
    public void testDynamicStructNestedEncode() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getNuuFunction.getOutputParameters()),
                Collections.singletonList(
                        new AbiV2TestFixture.Nuu(new AbiV2TestFixture.Foo("id", "name"))));
    }

    @Test
    public void testDecodeTupleDynamicStructNested() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getFooBarFunction.getOutputParameters()),
                Arrays.asList(
                        new AbiV2TestFixture.Foo("id", "name"),
                        new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN)));
    }

    @Test
    public void testDecodeMultipleDynamicStruct() {
        String rawInput =
                "0x00000000000000000000000000000000000000000000000000000000000000a0"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "000000000000000000000000000000000000000000000000000000000000000b"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getFooBarBarFunction.getOutputParameters()),
                Arrays.asList(
                        new AbiV2TestFixture.Foo("id", "name"),
                        new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN),
                        new AbiV2TestFixture.Bar(BigInteger.valueOf(2), BigInteger.valueOf(11))));
    }

    @Test
    public void testDecodeMultipleDynamicStruct2() {
        String rawInput =
                "0x00000000000000000000000000000000000000000000000000000000000000c0"
                        + "0000000000000000000000000000000000000000000000000000000000000180"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "000000000000000000000000000000000000000000000000000000000000000b"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getFooFooBarBarFunction.getOutputParameters()),
                Arrays.asList(
                        new AbiV2TestFixture.Foo("id", "name"),
                        new AbiV2TestFixture.Foo("id", "name"),
                        new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN),
                        new AbiV2TestFixture.Bar(BigInteger.valueOf(2), BigInteger.valueOf(11))));
    }

    @Test
    public void testDecodeDynamicNested3() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getNarFunction.getOutputParameters()),
                Arrays.asList(
                        new AbiV2TestFixture.Nar(
                                new AbiV2TestFixture.Nuu(
                                        new AbiV2TestFixture.Foo("4", "nestedFoo")))));
    }

    @Test
    public void testDecodeMultipleDynamicStaticNestedStructs() {
        String rawInput =
                "0000000000000000000000000000000000000000000000000000000000000240"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "00000000000000000000000000000000000000000000000000000000000004d1"
                        + "0000000000000000000000000000000000000000000000000000000000000079"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000340"
                        + "0000000000000000000000000000000000000000000000000000000000000400"
                        + "00000000000000000000000000000000000000000000000000000000000004d1"
                        + "0000000000000000000000000000000000000000000000000000000000000079"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000500"
                        + "00000000000000000000000000000000000000000000000000000000000005a0"
                        + "00000000000000000000000000000000000000000000000000000000000004d1"
                        + "0000000000000000000000000000000000000000000000000000000000000079"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6861686100000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "686f686f00000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6861686100000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "686f686f00000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput,
                        AbiV2TestFixture.getNarBarBarFuzzFooNarFuzzNuuFooFuzzFunction
                                .getOutputParameters()),
                Arrays.asList(
                        new AbiV2TestFixture.Nar(
                                new AbiV2TestFixture.Nuu(
                                        new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                        new AbiV2TestFixture.Bar(BigInteger.valueOf(123), BigInteger.valueOf(123)),
                        new AbiV2TestFixture.Bar(BigInteger.valueOf(123), BigInteger.valueOf(123)),
                        new AbiV2TestFixture.Fuzz(
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(1233), BigInteger.valueOf(121)),
                                BigInteger.valueOf(2)),
                        new AbiV2TestFixture.Foo("haha", "hoho"),
                        new AbiV2TestFixture.Nar(
                                new AbiV2TestFixture.Nuu(
                                        new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                        new AbiV2TestFixture.Fuzz(
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(1233), BigInteger.valueOf(121)),
                                BigInteger.valueOf(2)),
                        new AbiV2TestFixture.Nuu(new AbiV2TestFixture.Foo("", "")),
                        new AbiV2TestFixture.Foo("haha", "hoho"),
                        new AbiV2TestFixture.Fuzz(
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(1233), BigInteger.valueOf(121)),
                                BigInteger.valueOf(2))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeDynamicStructDynamicArray() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput,
                        AbiV2TestFixture.getFooDynamicArrayFunction.getOutputParameters()),
                Arrays.asList(
                        new DynamicArray(
                                AbiV2TestFixture.Foo.class,
                                new AbiV2TestFixture.Foo("id", "name"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeStaticStructStaticArray() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getBarStaticArrayFunction.getOutputParameters()),
                Arrays.asList(
                        new StaticArray3(
                                AbiV2TestFixture.Bar.class,
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(0), BigInteger.valueOf(0)),
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(123), BigInteger.valueOf(123)),
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(0), BigInteger.valueOf(0)))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeDynamicStructStaticArray() {
        String rawInput =
                "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000220"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getNarStaticArrayFunction.getOutputParameters()),
                Arrays.asList(
                        new StaticArray3(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(new AbiV2TestFixture.Foo("", ""))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeDynamicStructDynamicArray2() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000260"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput,
                        AbiV2TestFixture.getNarDynamicArrayFunction.getOutputParameters()),
                Arrays.asList(
                        new DynamicArray(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("", ""))))));
    }

    @Test
    public void testDecodeMultipleDynamicStructStaticDynamicArrays() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000140"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000460"
                        + "0000000000000000000000000000000000000000000000000000000000000560"
                        + "00000000000000000000000000000000000000000000000000000000000008a0"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000220"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000260"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000120"
                        + "00000000000000000000000000000000000000000000000000000000000001e0"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput,
                        AbiV2TestFixture.getNarBarFooNarFooDynamicArrayFunction
                                .getOutputParameters()),
                Arrays.asList(
                        new StaticArray3<>(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(new AbiV2TestFixture.Foo("", ""))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo")))),
                        new StaticArray3<>(
                                AbiV2TestFixture.Bar.class,
                                new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO),
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(123), BigInteger.valueOf(123)),
                                new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO)),
                        new DynamicArray<>(
                                AbiV2TestFixture.Foo.class, new AbiV2TestFixture.Foo("id", "name")),
                        new DynamicArray<>(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("", "")))),
                        new StaticArray3<>(
                                AbiV2TestFixture.Foo.class,
                                new AbiV2TestFixture.Foo("id", "name"),
                                new AbiV2TestFixture.Foo("id", "name"),
                                new AbiV2TestFixture.Foo("id", "name"))));
    }

    @Test
    public void testDecodeStructMultipleDynamicStaticArray() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000140"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000480"
                        + "0000000000000000000000000000000000000000000000000000000000000580"
                        + "00000000000000000000000000000000000000000000000000000000000008c0"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000260"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000260"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput,
                        AbiV2TestFixture.idNarBarFooNarFooArraysFunction.getOutputParameters()),
                Arrays.asList(
                        new DynamicArray<>(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("", "")))),
                        new StaticArray3<>(
                                AbiV2TestFixture.Bar.class,
                                new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO),
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(123), BigInteger.valueOf(123)),
                                new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO)),
                        new DynamicArray<>(
                                AbiV2TestFixture.Foo.class, new AbiV2TestFixture.Foo("id", "name")),
                        new DynamicArray<>(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("", "")))),
                        new DynamicArray<>(
                                AbiV2TestFixture.Foo.class,
                                new AbiV2TestFixture.Foo("id", "name"))));
    }

    @Test
    public void testDecodeStructMultipleDynamicStaticArray2() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000140"
                        + "0000000000000000000000000000000000000000000000000000000000000460"
                        + "0000000000000000000000000000000000000000000000000000000000000560"
                        + "00000000000000000000000000000000000000000000000000000000000008a0"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000220"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000260"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000120"
                        + "00000000000000000000000000000000000000000000000000000000000001e0"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput,
                        AbiV2TestFixture.idBarNarFooNarFooArraysFunction.getOutputParameters()),
                Arrays.asList(
                        new StaticArray3<>(
                                AbiV2TestFixture.Bar.class,
                                new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO),
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(123), BigInteger.valueOf(123)),
                                new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO)),
                        new StaticArray3<>(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(new AbiV2TestFixture.Foo("", ""))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo")))),
                        new DynamicArray<>(
                                AbiV2TestFixture.Foo.class, new AbiV2TestFixture.Foo("id", "name")),
                        new DynamicArray<>(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("", "")))),
                        new StaticArray3<>(
                                AbiV2TestFixture.Foo.class,
                                new AbiV2TestFixture.Foo("id", "name"),
                                new AbiV2TestFixture.Foo("id", "name"),
                                new AbiV2TestFixture.Foo("id", "name"))));
    }

    @Test
    public void testDecodeStructMultipleDynamicStaticArray3() {
        String rawInput =
                "0x00000000000000000000000000000000000000000000000000000000000000a0"
                        + "00000000000000000000000000000000000000000000000000000000000003c0"
                        + "00000000000000000000000000000000000000000000000000000000000004a0"
                        + "00000000000000000000000000000000000000000000000000000000000005a0"
                        + "00000000000000000000000000000000000000000000000000000000000008e0"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000220"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000000c"
                        + "0000000000000000000000000000000000000000000000000000000000000021"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000003"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000160"
                        + "0000000000000000000000000000000000000000000000000000000000000260"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "3400000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000009"
                        + "6e6573746564466f6f0000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000120"
                        + "00000000000000000000000000000000000000000000000000000000000001e0"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput,
                        AbiV2TestFixture.idNarBarFooNarFooArraysFunction2.getOutputParameters()),
                Arrays.asList(
                        new StaticArray3<>(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(new AbiV2TestFixture.Foo("", ""))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo")))),
                        new DynamicArray<>(
                                AbiV2TestFixture.Bar.class,
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(123), BigInteger.valueOf(123)),
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(12), BigInteger.valueOf(33)),
                                new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO)),
                        new DynamicArray<>(
                                AbiV2TestFixture.Foo.class, new AbiV2TestFixture.Foo("id", "name")),
                        new DynamicArray<>(
                                AbiV2TestFixture.Nar.class,
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                                new AbiV2TestFixture.Nar(
                                        new AbiV2TestFixture.Nuu(
                                                new AbiV2TestFixture.Foo("", "")))),
                        new StaticArray3<>(
                                AbiV2TestFixture.Foo.class,
                                new AbiV2TestFixture.Foo("id", "name"),
                                new AbiV2TestFixture.Foo("id", "name"),
                                new AbiV2TestFixture.Foo("id", "name"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeStaticStructDynamicArray() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput,
                        AbiV2TestFixture.getBarDynamicArrayFunction.getOutputParameters()),
                Arrays.asList(
                        new DynamicArray(
                                AbiV2TestFixture.Bar.class,
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(123), BigInteger.valueOf(123)),
                                new AbiV2TestFixture.Bar(
                                        BigInteger.valueOf(123), BigInteger.valueOf(123)))));
    }

    @Test
    public void testBuildDynamicArrayOfStaticStruct() throws ClassNotFoundException {
        // This is a version of testDecodeStaticStructDynamicArray() that builds
        // the decoding TypeReferences using inner types.
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b"
                        + "000000000000000000000000000000000000000000000000000000000000007b";

        // (uint256, uint256) static struct.
        TypeReference<StaticStruct> staticStructTr =
                new TypeReference<StaticStruct>(
                        false,
                        Arrays.asList(
                                TypeReference.makeTypeReference("uint256"),
                                TypeReference.makeTypeReference("uint256"))) {};

        // (uint256, uint256)[] dynamic array of static struct.
        TypeReference<DynamicArray> dynamicArray =
                new TypeReference<DynamicArray>(false) {
                    @Override
                    public TypeReference getSubTypeReference() {
                        return staticStructTr;
                    }

                    @Override
                    public java.lang.reflect.Type getType() {
                        return new java.lang.reflect.ParameterizedType() {
                            @Override
                            public java.lang.reflect.Type[] getActualTypeArguments() {
                                return new java.lang.reflect.Type[] {staticStructTr.getType()};
                            }

                            @Override
                            public java.lang.reflect.Type getRawType() {
                                return DynamicArray.class;
                            }

                            @Override
                            public java.lang.reflect.Type getOwnerType() {
                                return Class.class;
                            }
                        };
                    }
                };

        List<Type> decodedData =
                FunctionReturnDecoder.decode(rawInput, Utils.convert(Arrays.asList(dynamicArray)));

        List<Type> decodedArray = ((DynamicArray) decodedData.get(0)).getValue();

        List<Type> decodedStaticStruct0 = ((StaticStruct) decodedArray.get(0)).getValue();
        assertEquals(decodedStaticStruct0.get(0).getValue(), BigInteger.valueOf(123));
        assertEquals(decodedStaticStruct0.get(1).getValue(), BigInteger.valueOf(123));

        List<Type> decodedStaticStruct1 = ((StaticStruct) decodedArray.get(1)).getValue();
        assertEquals(decodedStaticStruct1.get(0).getValue(), BigInteger.valueOf(123));
        assertEquals(decodedStaticStruct1.get(1).getValue(), BigInteger.valueOf(123));
    }

    @Test
    public void testDecodeTupleOfStaticArrays() {
        List outputParameters = new ArrayList<TypeReference<Type>>();
        outputParameters.addAll(
                Arrays.asList(
                        new TypeReference<StaticArray4<Utf8String>>() {},
                        new TypeReference<StaticArray4<Uint256>>() {}));

        // tuple of (strings string[4]{"", "", "", ""}, ints int[4]{0, 0, 0, 0})
        String rawInput =
                "0x"
                        + "00000000000000000000000000000000000000000000000000000000000000a0" // strings array offset
                        + "0000000000000000000000000000000000000000000000000000000000000000" // ints[0]
                        + "0000000000000000000000000000000000000000000000000000000000000000" // ints[1]
                        + "0000000000000000000000000000000000000000000000000000000000000000" // ints[2]
                        + "0000000000000000000000000000000000000000000000000000000000000000" // ints[3]
                        + "0000000000000000000000000000000000000000000000000000000000000080" // offset strings[0]
                        + "00000000000000000000000000000000000000000000000000000000000000a0" // offset strings[1]
                        + "00000000000000000000000000000000000000000000000000000000000000c0" // offset strings[2]
                        + "00000000000000000000000000000000000000000000000000000000000000e0" // offset strings[3]
                        + "0000000000000000000000000000000000000000000000000000000000000000" // strings[0]
                        + "0000000000000000000000000000000000000000000000000000000000000000" // strings[1]
                        + "0000000000000000000000000000000000000000000000000000000000000000" // strings[2]
                        + "0000000000000000000000000000000000000000000000000000000000000000"; // strings[3]

        assertEquals(
                FunctionReturnDecoder.decode(rawInput, outputParameters),
                Arrays.asList(
                        new StaticArray4(
                                Utf8String.class,
                                new Utf8String(""),
                                new Utf8String(""),
                                new Utf8String(""),
                                new Utf8String("")),
                        new StaticArray4(
                                Uint256.class,
                                new Uint256(0),
                                new Uint256(0),
                                new Uint256(0),
                                new Uint256(0))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeDynamicStructWithStaticStruct() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6461746100000000000000000000000000000000000000000000000000000000";
        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getQuxFunction.getOutputParameters()),
                Arrays.asList(
                        new AbiV2TestFixture.Qux(
                                new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN), "data")));
    }

    @Test
    public void testBuildDynamicStructWithStaticStruct() throws ClassNotFoundException {
        // This is a version of testDecodeDynamicStructWithStaticStruct() that builds
        // the decoding TypeReferences using inner types.
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6461746100000000000000000000000000000000000000000000000000000000";
        // (uint256, uint256) static struct.
        TypeReference<StaticStruct> staticStruct =
                new TypeReference<StaticStruct>(
                        false,
                        Arrays.asList(
                                TypeReference.makeTypeReference("uint256"),
                                TypeReference.makeTypeReference("uint256"))) {};

        // ((uint256, uint256), string) dynamic struct containing static struct.
        TypeReference<DynamicStruct> dynamicStruct =
                new TypeReference<DynamicStruct>(
                        false,
                        Arrays.asList(staticStruct, TypeReference.makeTypeReference("string"))) {};

        List<Type> decodedData =
                FunctionReturnDecoder.decode(rawInput, Utils.convert(Arrays.asList(dynamicStruct)));
        List<Type> decodedDynamicStruct = ((DynamicStruct) decodedData.get(0)).getValue();

        List<Type> decodedStaticStruct = ((StaticStruct) decodedDynamicStruct.get(0)).getValue();
        assertEquals(decodedStaticStruct.get(0).getValue(), BigInteger.ONE);
        assertEquals(decodedStaticStruct.get(1).getValue(), BigInteger.TEN);

        assertEquals(decodedDynamicStruct.get(1).getValue(), "data");
    }

    @Test
    public void testDynamicStructOfDynamicStructWithAdditionalParametersReturn()
            throws ClassNotFoundException {
        // Return data from 'testInputAndOutput' function of this contract
        // https://sepolia.etherscan.io/address/0x009C10396226ECFE3E39b3f1AEFa072E37578e30#readContract
        //        struct MyStruct {
        //            uint256 value1;
        //            string value2;
        //            string value3;
        //            MyStruct2 value4;
        //        }
        //
        //        struct MyStruct2 {
        //            string value1;
        //            string value2;
        //        }
        //        function testInputAndOutput(MyStruct memory struc) external pure
        //        returns(string memory valueBefore, MyStruct memory, string memory valueAfter) {
        //
        //            return ("valuebefore", mystruc, "valueafter");
        //
        //        }
        String returnedData =
                "0x000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000000000000000000000260000000000000000000000000000000000000000000000000000000000000000b76616c75656265666f72650000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000c000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000001320000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000013300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000800000000000000000000000000000000000000000000000000000000000000004313233340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000063078313233340000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a76616c7565616674657200000000000000000000000000000000000000000000";

        List<TypeReference<?>> myStruct2Types = new ArrayList<>();
        List<TypeReference<?>> myStructTypes = new ArrayList<>();
        List<TypeReference<?>> myParameters = new ArrayList<>();

        myStruct2Types.add(TypeReference.makeTypeReference("string"));
        myStruct2Types.add(TypeReference.makeTypeReference("string"));

        myStructTypes.add(TypeReference.makeTypeReference("uint256"));
        myStructTypes.add(TypeReference.makeTypeReference("string"));
        myStructTypes.add(TypeReference.makeTypeReference("string"));
        myStructTypes.add(new TypeReference<DynamicStruct>(false, myStruct2Types) {});

        myParameters.add(TypeReference.makeTypeReference("string"));
        myParameters.add(new TypeReference<DynamicStruct>(false, myStructTypes) {});

        myParameters.add(TypeReference.makeTypeReference("string"));

        List<Type> decodedData =
                FunctionReturnDecoder.decode(returnedData, Utils.convert(myParameters));

        assertEquals(decodedData.get(0).getValue(), "valuebefore");

        List<Type> structData = ((DynamicStruct) decodedData.get(1)).getValue();

        assertEquals(structData.get(0).getValue(), BigInteger.valueOf(1));
        assertEquals(structData.get(1).getValue(), "2");
        assertEquals(structData.get(2).getValue(), "3");

        List<Type> innerStructData = ((DynamicStruct) structData.get(3)).getValue();

        assertEquals(innerStructData.get(0).getValue(), "1234");
        assertEquals(innerStructData.get(1).getValue(), "0x1234");

        assertEquals(decodedData.get(2).getValue(), "valueafter");
    }

    @Test
    public void testDynamicStructOfStaticStructReturn() throws ClassNotFoundException {
        String returnedData =
                "0x00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000c000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000004746573740000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047465737400000000000000000000000000000000000000000000000000000000";

        List<TypeReference<?>> myStruct2Types = new ArrayList<>();
        List<TypeReference<?>> myStructTypes = new ArrayList<>();

        myStruct2Types.add(TypeReference.makeTypeReference("uint256"));

        myStructTypes.add(TypeReference.makeTypeReference("uint256"));
        myStructTypes.add(TypeReference.makeTypeReference("string"));
        myStructTypes.add(TypeReference.makeTypeReference("string"));
        myStructTypes.add(new TypeReference<StaticStruct>(false, myStruct2Types) {});

        TypeReference<DynamicStruct> dynamicStruct =
                new TypeReference<DynamicStruct>(false, myStructTypes) {};

        List<Type> decodedData =
                FunctionReturnDecoder.decode(
                        returnedData, Utils.convert(Arrays.asList(dynamicStruct)));

        List<Type> decodedStruct = ((DynamicStruct) decodedData.get(0)).getValue();

        assertEquals(decodedStruct.get(0).getValue(), BigInteger.valueOf(1));
        assertEquals(decodedStruct.get(1).getValue(), "test");
        assertEquals(decodedStruct.get(2).getValue(), "test");

        List<Type> innerStructData = ((StaticStruct) decodedStruct.get(3)).getValue();

        assertEquals(innerStructData.get(0).getValue(), BigInteger.valueOf(1));
    }

    @Test
    public void testBuildEventOfArrayOfDynamicStruct() throws ClassNotFoundException {
        // The full event signature is
        //
        //     Stamp3(uint256 indexed stampId, address indexed caller, bool odd,
        // (uint256,bool,string) topMessage, (uint256,bool,string)[] messages),
        //
        // but we are only decoding the non-indexed data portion of it represented by
        // 'bool odd, (uint256,bool,string) topMessage, (uint256,bool,string)[] messages'.
        //
        // Transaction:
        // https://testnet.treasurescan.io/tx/0x041e53e7571283d462df99a95b2c21324279657f26a3adef907095d2d9c5ed85?tab=logs
        // Contract:
        // https://testnet.treasurescan.io/address/0x5167E9A422aCEd95C2D0b62bF05a7847a9a942B2
        String data =
                "0x000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000001400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000568656c6c6f00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000001a00000000000000000000000000000000000000000000000000000000000000015000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000002676d0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000002676d0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000002676d000000000000000000000000000000000000000000000000000000000000";

        TypeReference<DynamicStruct> tupleTr =
                new TypeReference<DynamicStruct>(
                        false,
                        Arrays.asList(
                                TypeReference.makeTypeReference("uint256"),
                                TypeReference.makeTypeReference("bool"),
                                TypeReference.makeTypeReference("string"))) {};

        // Decode data according to the above signature for the non-indexed parameters.
        List<Type> decodedData =
                FunctionReturnDecoder.decode(
                        data,
                        Utils.convert(
                                Arrays.asList(
                                        // bool odd
                                        TypeReference.makeTypeReference("bool"),

                                        // (uint256,bool,string)
                                        tupleTr,

                                        // (uint256,bool,string)[]
                                        new TypeReference<DynamicArray>(false) {
                                            @Override
                                            public TypeReference getSubTypeReference() {
                                                return tupleTr;
                                            }

                                            @Override
                                            public java.lang.reflect.Type getType() {
                                                return new java.lang.reflect.ParameterizedType() {
                                                    @Override
                                                    public java.lang.reflect.Type[]
                                                            getActualTypeArguments() {
                                                        return new java.lang.reflect.Type[] {
                                                            tupleTr.getType()
                                                        };
                                                    }

                                                    @Override
                                                    public java.lang.reflect.Type getRawType() {
                                                        return DynamicArray.class;
                                                    }

                                                    @Override
                                                    public java.lang.reflect.Type getOwnerType() {
                                                        return Class.class;
                                                    }
                                                };
                                            }
                                        })));

        assertEquals(decodedData.get(0).getValue(), false);

        List<Type> tupleData = ((DynamicStruct) decodedData.get(1)).getValue();

        assertEquals(tupleData.get(0).getValue(), BigInteger.valueOf(20));
        assertEquals(tupleData.get(1).getValue(), false);
        assertEquals(tupleData.get(2).getValue(), "hello");

        List<DynamicStruct> tupleArrayData =
                ((DynamicArray<DynamicStruct>) decodedData.get(2)).getValue();

        List<Type> tupleArrayEntry0 = tupleArrayData.get(0).getValue();
        assertEquals(tupleArrayEntry0.get(0).getValue(), BigInteger.valueOf(21));
        assertEquals(tupleArrayEntry0.get(1).getValue(), true);
        assertEquals(tupleArrayEntry0.get(2).getValue(), "gm");

        List<Type> tupleArrayEntry1 = tupleArrayData.get(1).getValue();
        assertEquals(tupleArrayEntry1.get(0).getValue(), BigInteger.valueOf(22));
        assertEquals(tupleArrayEntry1.get(1).getValue(), false);
        assertEquals(tupleArrayEntry1.get(2).getValue(), "gm");

        List<Type> tupleArrayEntry2 = tupleArrayData.get(2).getValue();
        assertEquals(tupleArrayEntry2.get(0).getValue(), BigInteger.valueOf(23));
        assertEquals(tupleArrayEntry2.get(1).getValue(), true);
        assertEquals(tupleArrayEntry2.get(2).getValue(), "gm");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeStaticArrayOfDynamicBytesFollowedByUint() {
        // Tuple (bytes[2], uint256). The StaticArray<DynamicBytes> at the top
        // level is encoded as a single pointer slot, so after decoding it the
        // top-level offset must advance by exactly one slot (not length * slot).
        // Otherwise the trailing uint256 reads from the wrong position.
        String rawInput =
                "0x"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "00000000000000000000000000000000000000000000000000000000000007ad"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6c6f6c3100000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6f6d673200000000000000000000000000000000000000000000000000000000";

        List<TypeReference<Type>> outputParameters = new ArrayList<>();
        outputParameters.add(
                (TypeReference) new TypeReference<StaticArray2<DynamicBytes>>() {});
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {});

        List<Type> result = FunctionReturnDecoder.decode(rawInput, outputParameters);

        StaticArray2<DynamicBytes> bytesArray = (StaticArray2<DynamicBytes>) result.get(0);
        Uint256 trailing = (Uint256) result.get(1);

        assertArrayEquals(
                new byte[] {'l', 'o', 'l', '1'}, bytesArray.getValue().get(0).getValue());
        assertArrayEquals(
                new byte[] {'o', 'm', 'g', '2'}, bytesArray.getValue().get(1).getValue());
        assertEquals(BigInteger.valueOf(0x7ad), trailing.getValue());
    }

    @Test
    @Disabled(
            "Nested StaticArray of dynamic-element StaticArrays (e.g. bytes[2][2]) "
                    + "throws StringIndexOutOfBoundsException in TypeDecoder.decodeArrayElements. "
                    + "This is a pre-existing bug independent of the top-level offset fix in this PR.")
    @SuppressWarnings("unchecked")
    public void testDecodeNestedStaticArrayOfDynamicBytesFollowedByUint() {
        // Tuple (bytes[2][2], uint256). bytes[2][2] is dynamic (inner bytes is
        // dynamic), so at the top level the whole nested array occupies a
        // single pointer slot and the trailing uint256 must read from slot 1.
        // Encoded values:
        //   bytes[2][2] = [["lol1", "omg2"], ["wat3", "yes4"]]
        //   uint256 = 0x7ad
        String rawInput =
                "0x"
                        // top-level head
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "00000000000000000000000000000000000000000000000000000000000007ad"
                        // bytes[2][2] head at 0x40
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000100"
                        // first bytes[2] at 0x80
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6c6f6c3100000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6f6d673200000000000000000000000000000000000000000000000000000000"
                        // second bytes[2] at 0x140
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "7761743300000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "7965733400000000000000000000000000000000000000000000000000000000";

        List<TypeReference<Type>> outputParameters = new ArrayList<>();
        outputParameters.add(
                (TypeReference)
                        new TypeReference<StaticArray2<StaticArray2<DynamicBytes>>>() {});
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {});

        List<Type> result = FunctionReturnDecoder.decode(rawInput, outputParameters);

        StaticArray2<StaticArray2<DynamicBytes>> nested =
                (StaticArray2<StaticArray2<DynamicBytes>>) result.get(0);
        Uint256 trailing = (Uint256) result.get(1);

        assertArrayEquals(
                new byte[] {'l', 'o', 'l', '1'},
                nested.getValue().get(0).getValue().get(0).getValue());
        assertArrayEquals(
                new byte[] {'o', 'm', 'g', '2'},
                nested.getValue().get(0).getValue().get(1).getValue());
        assertArrayEquals(
                new byte[] {'w', 'a', 't', '3'},
                nested.getValue().get(1).getValue().get(0).getValue());
        assertArrayEquals(
                new byte[] {'y', 'e', 's', '4'},
                nested.getValue().get(1).getValue().get(1).getValue());
        assertEquals(BigInteger.valueOf(0x7ad), trailing.getValue());
    }
}
