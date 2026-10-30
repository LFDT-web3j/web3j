/*
 * Copyright 2025 Web3 Labs Ltd.
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
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Struct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeReferenceTest {
    @Test
    public void testStructTypeReferenceToString() {
        List<String> fieldNames = Arrays.asList("name", "balance");
        List<TypeReference<?>> fieldTypes =
                Arrays.asList(
                        TypeReference.create(Utf8String.class),
                        TypeReference.create(Uint256.class));

        TypeReference.StructTypeReference<Struct> ref =
                new TypeReference.StructTypeReference<>("User", fieldNames, fieldTypes);
        // String expected = "StructType(User) {name: class org.web3j.abi.datatypes.Utf8String,
        // balance: class org.web3j.abi.datatypes.Uint256}";
        String expected =
                "StructType(User) {name: class org.web3j.abi.datatypes.Utf8String, balance: class org.web3j.abi.datatypes.generated.Uint256}";
        // System.out.println("Actual:   " + ref.toString());
        // System.out.println("Expected: " + expected);
        assertEquals(expected, ref.toString());
    }
}
