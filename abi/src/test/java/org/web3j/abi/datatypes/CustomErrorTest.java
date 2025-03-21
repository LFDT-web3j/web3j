/*
 * Copyright 2024 Web3 Labs Ltd.
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

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.AbiTypes;
import org.web3j.abi.datatypes.generated.Uint256;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CustomErrorTest {

    @Test
    public void testEmptyCustomError() {
        CustomError error = new CustomError("SimpleError");
        assertEquals(
            "SimpleError()", 
            error.getSignature(),
            "Empty error should have signature 'SimpleError()'"
        );
        assertEquals(
            "0xc2bb947c", 
            error.getSelector(),
            "Selector should match keccak256('SimpleError()')"
        );
    }

    @Test
    public void testCustomErrorWithParameters() {
        List<TypeReference<?>> parameters = Arrays.asList(
            TypeReference.create(Address.class),
            TypeReference.create(Uint256.class),
            TypeReference.create(Utf8String.class)
        );
        
        CustomError error = new CustomError("ComplexError", parameters);
        
        String expectedSignature = "ComplexError(address,uint256,string)";
        assertEquals(
            expectedSignature,
            error.getSignature(),
            "Error signature should match '" + expectedSignature + "'"
        );
        assertEquals(
            parameters,
            error.getParameters(),
            "Parameters list should match the input parameters"
        );
        assertEquals(
            "0xcca85a17",
            error.getSelector(),
            "Selector should match keccak256('" + expectedSignature + "')"
        );
    }

    @Test
    public void testCustomErrorWithArrayParameter() {
        List<TypeReference<?>> parameters = Collections.singletonList(
            new TypeReference<DynamicArray<Uint256>>() {}
        );
        
        CustomError error = new CustomError("ArrayError", parameters);
        
        String expectedSignature = "ArrayError(uint256[])";
        assertEquals(
            expectedSignature,
            error.getSignature(),
            "Error signature should match '" + expectedSignature + "'"
        );
        assertEquals(
            parameters,
            error.getParameters(),
            "Parameters list should match the input parameters"
        );
        assertEquals(
            "0x6300af57",
            error.getSelector(),
            "Selector should match keccak256('" + expectedSignature + "')"
        );
    }

    @Test
    public void testEquality() {
        List<TypeReference<?>> parameters1 = Arrays.asList(
            TypeReference.create(Address.class),
            TypeReference.create(Uint256.class)
        );
        
        List<TypeReference<?>> parameters2 = Arrays.asList(
            TypeReference.create(Address.class),
            TypeReference.create(Uint256.class)
        );
        
        CustomError error1 = new CustomError("TestError", parameters1);
        CustomError error2 = new CustomError("TestError", parameters2);
        CustomError error3 = new CustomError("DifferentError", parameters1);
        
        assertEquals(
            error1,
            error2,
            "Errors with same name and parameters should be equal"
        );
        assertNotEquals(
            error1,
            error3,
            "Errors with different names should not be equal"
        );
    }

    @Test
    public void testToString() {
        List<TypeReference<?>> parameters = Arrays.asList(
            TypeReference.create(Address.class),
            TypeReference.create(Uint256.class)
        );
        
        CustomError error = new CustomError("TestError", parameters);
        String expectedString = "TestError(address,uint256)";
        assertEquals(
            expectedString,
            error.toString(),
            "toString() should return the error signature '" + expectedString + "'"
        );
    }
} 