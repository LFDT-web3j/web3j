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

import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                new DynamicArray<>(
                        DynamicArray.class,
                        Collections.singletonList(innerArray));

        assertEquals("string[][]", outerArray.getTypeAsString());
    }

    @Test
    public void testEmptyString2DArrayTypeAsString() {
        DynamicArray<Utf8String> innerArray =
                new DynamicArray<>(Utf8String.class, Collections.emptyList());
        DynamicArray<DynamicArray> outerArray =
                new DynamicArray<>(
                        DynamicArray.class,
                        Collections.singletonList(innerArray));

        assertEquals("string[][]", outerArray.getTypeAsString());
    }

    @Test
    public void testBytes2DArrayTypeAsString() {
        DynamicArray<DynamicBytes> inner =
                new DynamicArray<>(DynamicBytes.class, new DynamicBytes(new byte[] {0x01}));
        DynamicArray<DynamicArray> outer =
                new DynamicArray<>(
                        DynamicArray.class,
                        Collections.singletonList(inner));

        assertEquals("bytes[][]", outer.getTypeAsString());
    }

    @Test
    public void testTripleDimensionArrayTypeAsString() {
        DynamicArray<Utf8String> level1 = new DynamicArray<>(Utf8String.class, new Utf8String("x"));
        DynamicArray<DynamicArray> level2 =
                new DynamicArray<>(
                        DynamicArray.class,
                        Collections.singletonList(level1));
        DynamicArray<DynamicArray> level3 =
                new DynamicArray<>(
                        DynamicArray.class,
                        Collections.singletonList(level2));

        assertEquals("string[][][]", level3.getTypeAsString());
    }

    @Test
    public void testNestedStaticArrayTypeAsString() {
        StaticArray2<Uint256> inner =
                new StaticArray2<>(Uint256.class, new Uint256(1), new Uint256(2));
        DynamicArray<StaticArray2> outer =
                new DynamicArray<>(
                        StaticArray2.class,
                        Collections.singletonList(inner));

        assertEquals("uint256[2][]", outer.getTypeAsString());
    }

    @Test
    public void testUint256DynamicArrayNested() {
        List<Uint256> innerValues = Arrays.asList(new Uint256(1), new Uint256(2));
        DynamicArray<Uint256> inner = new DynamicArray<>(Uint256.class, innerValues);
        DynamicArray<DynamicArray> outer =
                new DynamicArray<>(
                        DynamicArray.class,
                        Collections.singletonList(inner));

        assertEquals("uint256[][]", outer.getTypeAsString());
    }

    private Uint[] arrayOfUints(int length) {
        return IntStream.rangeClosed(1, length).mapToObj(Uint8::new).toArray(Uint[]::new);
    }
}
