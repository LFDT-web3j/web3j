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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.CustomError;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.web3j.abi.Utils.convert;

public class CustomErrorEncoderTest {
    @Test
    public void testCalculateSignatureHash() {
        assertEquals(
                CustomErrorEncoder.calculateSignatureHash("InvalidAccess(address,string,uint256)"),
                ("0xcb5157bf1b439b9573ea7a95f7c00cc33f832ed728345c2bd29146ce58bbab57"));

        assertEquals(
                CustomErrorEncoder.calculateSignatureHash("RandomError(address[],bytes)"),
                ("0xbf37b77ddf0fbbf29ee6a3ebda3d177c2d438123b10571806c57958230d9f905"));
    }

    @Test
    public void testEncode() {
        CustomError error =
                new CustomError(
                        "InvalidAccess",
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Address>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {}));

        assertEquals(
                CustomErrorEncoder.encode(error),
                "0xcb5157bf1b439b9573ea7a95f7c00cc33f832ed728345c2bd29146ce58bbab57");
    }

    @Test
    public void testBuildErrorSignature() {
        List<TypeReference<?>> parameters =
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Address>() {},
                        new TypeReference<Utf8String>() {},
                        new TypeReference<Uint256>() {});

        assertEquals(
                "InvalidAccess(address,string,uint256)",
                CustomErrorEncoder.buildErrorSignature("InvalidAccess", convert(parameters)));
    }

    @Test
    void testBuildErrorSignatureWithDynamicStructs() {
        List<TypeReference<?>> parameters =
                Arrays.asList(
                        new TypeReference<AbiV2TestFixture.Nazz>() {},
                        new TypeReference<AbiV2TestFixture.Foo>() {});

        assertEquals(
                "DynamicStructError((((string,string)[])[],uint256),(string,string))",
                CustomErrorEncoder.buildErrorSignature("DynamicStructError", convert(parameters)));
    }

    @Test
    void testBuildErrorSignatureWithDynamicArrays() {
        List<TypeReference<?>> parameters =
                Arrays.asList(new TypeReference<DynamicArray<AbiV2TestFixture.Nazz>>() {});

        assertEquals(
                "DynamicArrayError((((string,string)[])[],uint256)[])",
                EventEncoder.buildMethodSignature("DynamicArrayError", convert(parameters)));
    }
}
