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
package org.web3j.codegen;

import org.junit.jupiter.api.Test;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.generated.Uint256;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicStructTest {

    @Test
    public void testToStringForStruct() {
        Uint256 value1 = new Uint256(123);
        Address value2 = new Address("0x1234567890123456789012345678901234567890");
        DynamicStruct struct = new DynamicStruct(value1, value2);

        String output = struct.toString();
        System.out.println("toString output: " + output);

        // Optional: add assertion
        // assertTrue(output.contains("field0: 123"));
        // assertTrue(output.contains("field1: 0x1234567890123456789012345678901234567890"));

        // Assertions
        //       assertTrue(output.contains("123"));
        //       assertTrue(output.contains("0x1234567890123456789012345678901234567890"));
    }
}
