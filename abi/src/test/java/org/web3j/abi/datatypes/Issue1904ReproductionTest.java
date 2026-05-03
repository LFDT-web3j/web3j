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
package org.web3j.abi.datatypes;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1904ReproductionTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testString2DArrayTypeAsString() {
        DynamicArray<Utf8String> innerArray =
                new DynamicArray<>(Utf8String.class, new Utf8String("a"), new Utf8String("b"));
        DynamicArray<DynamicArray<Utf8String>> outerArray =
                new DynamicArray<>(
                        (Class<DynamicArray<Utf8String>>) (Class<?>) DynamicArray.class,
                        Collections.singletonList(innerArray));

        assertEquals("string[][]", outerArray.getTypeAsString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEmptyString2DArrayTypeAsString() {
        DynamicArray<Utf8String> innerArray =
                new DynamicArray<>(Utf8String.class, Collections.emptyList());
        DynamicArray<DynamicArray<Utf8String>> outerArray =
                new DynamicArray<>(
                        (Class<DynamicArray<Utf8String>>) (Class<?>) DynamicArray.class,
                        Collections.singletonList(innerArray));

        assertEquals("string[][]", outerArray.getTypeAsString());
    }
}
