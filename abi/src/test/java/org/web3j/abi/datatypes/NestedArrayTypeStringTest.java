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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.Uint256;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedArrayTypeStringTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testString2DArrayTypeAsString() {
        DynamicArray<Utf8String> inner =
                new DynamicArray<>(Utf8String.class, new Utf8String("a"), new Utf8String("b"));
        DynamicArray<DynamicArray<Utf8String>> outer =
                new DynamicArray<>(
                        (Class<DynamicArray<Utf8String>>) (Class<?>) DynamicArray.class,
                        Collections.singletonList(inner));

        assertEquals("string[][]", outer.getTypeAsString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBytes2DArrayTypeAsString() {
        DynamicArray<DynamicBytes> inner =
                new DynamicArray<>(DynamicBytes.class, new DynamicBytes(new byte[] {0x01}));
        DynamicArray<DynamicArray<DynamicBytes>> outer =
                new DynamicArray<>(
                        (Class<DynamicArray<DynamicBytes>>) (Class<?>) DynamicArray.class,
                        Collections.singletonList(inner));

        assertEquals("bytes[][]", outer.getTypeAsString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTripleDimensionArrayTypeAsString() {
        DynamicArray<Utf8String> level1 = new DynamicArray<>(Utf8String.class, new Utf8String("x"));
        DynamicArray<DynamicArray<Utf8String>> level2 =
                new DynamicArray<>(
                        (Class<DynamicArray<Utf8String>>) (Class<?>) DynamicArray.class,
                        Collections.singletonList(level1));
        DynamicArray<DynamicArray<DynamicArray<Utf8String>>> level3 =
                new DynamicArray<>(
                        (Class<DynamicArray<DynamicArray<Utf8String>>>)
                                (Class<?>) DynamicArray.class,
                        Collections.singletonList(level2));

        assertEquals("string[][][]", level3.getTypeAsString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNestedStaticArrayTypeAsString() {
        StaticArray2<Uint256> inner =
                new StaticArray2<>(Uint256.class, new Uint256(1), new Uint256(2));
        DynamicArray<StaticArray2<Uint256>> outer =
                new DynamicArray<>(
                        (Class<StaticArray2<Uint256>>) (Class<?>) StaticArray2.class,
                        Collections.singletonList(inner));

        assertEquals("uint256[2][]", outer.getTypeAsString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUint256DynamicArrayNested() {
        List<Uint256> innerValues = Arrays.asList(new Uint256(1), new Uint256(2));
        DynamicArray<Uint256> inner = new DynamicArray<>(Uint256.class, innerValues);
        DynamicArray<DynamicArray<Uint256>> outer =
                new DynamicArray<>(
                        (Class<DynamicArray<Uint256>>) (Class<?>) DynamicArray.class,
                        Collections.singletonList(inner));

        assertEquals("uint256[][]", outer.getTypeAsString());
    }
}
