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

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import org.web3j.abi.TypeEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChineseUtf8StringTest {

    @Test
    public void testChineseStringEncoding() {
        // Chinese text: Zhong Guo Liu Li Fa Lang Cai Hua Niao Zi Ming Zhong
        String chineseText =
                "\u4e2d\u56fd\u7409\u7483\u73d0\u7405\u5f69\u82b1\u9e1f\u81ea\u9e23\u949f";
        Utf8String utf8String = new Utf8String(chineseText);

        String encoded = TypeEncoder.encode(utf8String);
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000024"
                        + "e4b8ade59bbde79089e79283e78f90e79085e5bda9e88ab1e9b89fe887aae9b8a3e9929f"
                        + "00000000000000000000000000000000000000000000000000000000",
                encoded);

        byte[] bytes = chineseText.getBytes(StandardCharsets.UTF_8);

        // 12 characters * 3 bytes = 36 bytes.
        assertEquals(36, bytes.length);

        // Check bytes32PaddedLength
        // 32 (length) + 64 (padded data) = 96
        assertEquals(96, utf8String.bytes32PaddedLength());
    }

    @Test
    public void testChineseStringInArray() {
        // Chinese text: Zhong Guo
        String chineseText = "\u4e2d\u56fd";
        Utf8String utf8String = new Utf8String(chineseText);
        DynamicArray<Utf8String> array = new DynamicArray<>(Utf8String.class, utf8String);

        String encoded = TypeEncoder.encode(array);
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000006"
                        + "e4b8ade59bbd0000000000000000000000000000000000000000000000000000",
                encoded);
    }
}
