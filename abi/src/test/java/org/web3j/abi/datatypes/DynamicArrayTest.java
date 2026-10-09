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
package org.web3j.abi.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicArrayTest {

    @Test
    public void testEmptyDynamicArray() {
        final DynamicArray<Address> array =
                new DynamicArray<>(Address.class, Collections.emptyList());

        assertEquals(Address.TYPE_NAME + "[]", array.getTypeAsString());
    }

    @Test
    public void testDynamicArrayWithDynamicStruct() {
        final List<DynamicStruct> list = Collections.singletonList(new DynamicStruct());
        final DynamicArray<DynamicStruct> array = new DynamicArray<>(DynamicStruct.class, list);

        assertEquals("()[]", array.getTypeAsString());
    }

    @Test
    public void testDynamicArrayWithAbiType() {
        final DynamicArray<Uint> array = new DynamicArray<>(Uint.class, arrayOfUints(1));

        assertEquals(Uint.TYPE_NAME + "[]", array.getTypeAsString());
    }

    @Test
    public void testMultidimensionalDynamicArray() {
        DynamicArray<DynamicArray> array =
                new DynamicArray<>(
                        DynamicArray.class,
                        List.of(
                                new DynamicArray<>(
                                        DynamicArray.class,
                                        List.of(
                                                new DynamicArray<>(
                                                        Uint256.class, new ArrayList<>())))));
        assertEquals("uint256[][][]", array.getTypeAsString());
    }

    @Test
    public void testString2DArrayTypeAsString() {
        DynamicArray<Utf8String> innerArray =
                new DynamicArray<>(Utf8String.class, new Utf8String("a"), new Utf8String("b"));
        DynamicArray<DynamicArray> outerArray =
                new DynamicArray<>(DynamicArray.class, Collections.singletonList(innerArray));

        assertEquals("string[][]", outerArray.getTypeAsString());
    }

    @Test
    public void testEmptyString2DArrayTypeAsString() {
        DynamicArray<Utf8String> innerArray =
                new DynamicArray<>(Utf8String.class, Collections.emptyList());
        DynamicArray<DynamicArray> outerArray =
                new DynamicArray<>(DynamicArray.class, Collections.singletonList(innerArray));

        assertEquals("string[][]", outerArray.getTypeAsString());
    }

    @Test
    public void testBytes2DArrayTypeAsString() {
        DynamicArray<DynamicBytes> inner =
                new DynamicArray<>(DynamicBytes.class, new DynamicBytes(new byte[] {0x01}));
        DynamicArray<DynamicArray> outer =
                new DynamicArray<>(DynamicArray.class, Collections.singletonList(inner));

        assertEquals("bytes[][]", outer.getTypeAsString());
    }

    @Test
    public void testTripleDimensionArrayTypeAsString() {
        DynamicArray<Utf8String> level1 = new DynamicArray<>(Utf8String.class, new Utf8String("x"));
        DynamicArray<DynamicArray> level2 =
                new DynamicArray<>(DynamicArray.class, Collections.singletonList(level1));
        DynamicArray<DynamicArray> level3 =
                new DynamicArray<>(DynamicArray.class, Collections.singletonList(level2));

        assertEquals("string[][][]", level3.getTypeAsString());
    }

    @Test
    public void testNestedStaticArrayTypeAsString() {
        StaticArray2<Uint256> inner =
                new StaticArray2<>(Uint256.class, new Uint256(1), new Uint256(2));
        DynamicArray<StaticArray2> outer =
                new DynamicArray<>(StaticArray2.class, Collections.singletonList(inner));

        assertEquals("uint256[2][]", outer.getTypeAsString());
    }

    @Test
    public void testStaticArrayContainingDynamicArrayTypeAsString() {
        DynamicArray<Uint256> inner =
                new DynamicArray<>(Uint256.class, new Uint256(1), new Uint256(2));
        StaticArray2<DynamicArray> outer = new StaticArray2<>(DynamicArray.class, inner, inner);

        assertEquals("uint256[][2]", outer.getTypeAsString());
    }

    @Test
    public void testUint256DynamicArrayNested() {
        List<Uint256> innerValues = Arrays.asList(new Uint256(1), new Uint256(2));
        DynamicArray<Uint256> inner = new DynamicArray<>(Uint256.class, innerValues);
        DynamicArray<DynamicArray> outer =
                new DynamicArray<>(DynamicArray.class, Collections.singletonList(inner));

        assertEquals("uint256[][]", outer.getTypeAsString());
    }

    @Test
    public void test2DArrayEncodingDecoding() {
        DynamicArray<Uint256> inner =
                new DynamicArray<>(Uint256.class, new Uint256(1), new Uint256(2));
        DynamicArray<DynamicArray> outer = new DynamicArray<>(DynamicArray.class, inner);

        assertEquals("uint256[][]", outer.getTypeAsString());

        String encoded = TypeEncoder.encode(outer);
        TypeReference<DynamicArray<DynamicArray<Uint256>>> typeRef = new TypeReference<>() {};
        DynamicArray decoded = TypeDecoder.decodeDynamicArray(encoded, 0, typeRef);

        assertEquals(outer.getTypeAsString(), decoded.getTypeAsString());
        assertEquals(1, decoded.getValue().size());
        DynamicArray decodedInner = (DynamicArray) decoded.getValue().get(0);
        assertEquals(2, decodedInner.getValue().size());
        assertEquals(new Uint256(1), decodedInner.getValue().get(0));
        assertEquals(new Uint256(2), decodedInner.getValue().get(1));
    }

    @Test
    public void test3DArrayEncodingDecoding() {
        DynamicArray<Uint256> inner1 =
                new DynamicArray<>(Uint256.class, new Uint256(1), new Uint256(2));
        DynamicArray<Uint256> inner2 = new DynamicArray<>(Uint256.class, new Uint256(3));
        DynamicArray<DynamicArray> middle1 = new DynamicArray<>(DynamicArray.class, inner1, inner2);
        DynamicArray<DynamicArray> middle2 = new DynamicArray<>(DynamicArray.class, inner2);
        DynamicArray<DynamicArray> outer3D =
                new DynamicArray<>(DynamicArray.class, middle1, middle2);

        assertEquals("uint256[][][]", outer3D.getTypeAsString());

        String encoded3D = TypeEncoder.encode(outer3D);
        TypeReference<DynamicArray<DynamicArray<DynamicArray<Uint256>>>> typeRef3D =
                new TypeReference<>() {};
        DynamicArray decoded3D = TypeDecoder.decodeDynamicArray(encoded3D, 0, typeRef3D);

        assertEquals(outer3D.getTypeAsString(), decoded3D.getTypeAsString());
        assertEquals(2, decoded3D.getValue().size());

        DynamicArray decodedMiddle1 = (DynamicArray) decoded3D.getValue().get(0);
        assertEquals(2, decodedMiddle1.getValue().size());
        DynamicArray decodedInner1 = (DynamicArray) decodedMiddle1.getValue().get(0);
        assertEquals(2, decodedInner1.getValue().size());
        assertEquals(new Uint256(1), decodedInner1.getValue().get(0));
        assertEquals(new Uint256(2), decodedInner1.getValue().get(1));
        DynamicArray decodedInner2 = (DynamicArray) decodedMiddle1.getValue().get(1);
        assertEquals(1, decodedInner2.getValue().size());
        assertEquals(new Uint256(3), decodedInner2.getValue().get(0));

        DynamicArray decodedMiddle2 = (DynamicArray) decoded3D.getValue().get(1);
        assertEquals(1, decodedMiddle2.getValue().size());
        DynamicArray decodedInner3 = (DynamicArray) decodedMiddle2.getValue().get(0);
        assertEquals(1, decodedInner3.getValue().size());
        assertEquals(new Uint256(3), decodedInner3.getValue().get(0));
    }

    @Test
    public void test4DArrayEncodingDecoding() {
        DynamicArray<Uint256> inner1 =
                new DynamicArray<>(Uint256.class, new Uint256(1), new Uint256(2));
        DynamicArray<Uint256> inner2 = new DynamicArray<>(Uint256.class, new Uint256(3));
        DynamicArray<DynamicArray> middle1 = new DynamicArray<>(DynamicArray.class, inner1, inner2);
        DynamicArray<DynamicArray> middle2 = new DynamicArray<>(DynamicArray.class, inner2);
        DynamicArray<DynamicArray> outer3D =
                new DynamicArray<>(DynamicArray.class, middle1, middle2);
        DynamicArray<DynamicArray> outer4D = new DynamicArray<>(DynamicArray.class, outer3D);

        assertEquals("uint256[][][][]", outer4D.getTypeAsString());

        String encoded4D = TypeEncoder.encode(outer4D);
        TypeReference<DynamicArray<DynamicArray<DynamicArray<DynamicArray<Uint256>>>>> typeRef4D =
                new TypeReference<>() {};
        DynamicArray decoded4D = TypeDecoder.decodeDynamicArray(encoded4D, 0, typeRef4D);

        assertEquals(outer4D.getTypeAsString(), decoded4D.getTypeAsString());
        assertEquals(1, decoded4D.getValue().size());

        DynamicArray decoded3D = (DynamicArray) decoded4D.getValue().get(0);
        assertEquals(2, decoded3D.getValue().size());

        DynamicArray decodedMiddle1 = (DynamicArray) decoded3D.getValue().get(0);
        assertEquals(2, decodedMiddle1.getValue().size());
        DynamicArray decodedInner1 = (DynamicArray) decodedMiddle1.getValue().get(0);
        assertEquals(2, decodedInner1.getValue().size());
        assertEquals(new Uint256(1), decodedInner1.getValue().get(0));
        assertEquals(new Uint256(2), decodedInner1.getValue().get(1));
        DynamicArray decodedInner2 = (DynamicArray) decodedMiddle1.getValue().get(1);
        assertEquals(1, decodedInner2.getValue().size());
        assertEquals(new Uint256(3), decodedInner2.getValue().get(0));

        DynamicArray decodedMiddle2 = (DynamicArray) decoded3D.getValue().get(1);
        assertEquals(1, decodedMiddle2.getValue().size());
        DynamicArray decodedInner3 = (DynamicArray) decodedMiddle2.getValue().get(0);
        assertEquals(1, decodedInner3.getValue().size());
        assertEquals(new Uint256(3), decodedInner3.getValue().get(0));
    }

    @Test
    public void testString2DArrayEncodingDecodingAndFunctionCall() {
        List<String> u0 = Arrays.asList("7300", "7400");
        List<String> u1 = Arrays.asList("7200", "mark");
        List<List<String>> uAll = Arrays.asList(u0, u1);

        DynamicArray<DynamicArray> array =
                new DynamicArray<>(
                        DynamicArray.class,
                        Utils.typeMap(uAll, DynamicArray.class, Utf8String.class));

        assertEquals("string[][]", array.getTypeAsString());

        Function function =
                new Function(
                        "testParas",
                        Collections.singletonList(array),
                        Collections.singletonList(new TypeReference<Utf8String>() {}));

        String encodedFunction = FunctionEncoder.encode(function);
        assertTrue(encodedFunction.startsWith("0xb7f3e465"));

        String encoded2D = TypeEncoder.encode(array);
        TypeReference<DynamicArray<DynamicArray<Utf8String>>> typeRef = new TypeReference<>() {};
        DynamicArray decoded = TypeDecoder.decodeDynamicArray(encoded2D, 0, typeRef);

        assertEquals("string[][]", decoded.getTypeAsString());
        assertEquals(2, decoded.getValue().size());
        DynamicArray decodedInner1 = (DynamicArray) decoded.getValue().get(0);
        assertEquals(new Utf8String("7300"), decodedInner1.getValue().get(0));
        assertEquals(new Utf8String("7400"), decodedInner1.getValue().get(1));
        DynamicArray decodedInner2 = (DynamicArray) decoded.getValue().get(1);
        assertEquals(new Utf8String("7200"), decodedInner2.getValue().get(0));
        assertEquals(new Utf8String("mark"), decodedInner2.getValue().get(1));
    }

    private Uint[] arrayOfUints(int length) {
        return IntStream.rangeClosed(1, length).mapToObj(Uint8::new).toArray(Uint[]::new);
    }
}
