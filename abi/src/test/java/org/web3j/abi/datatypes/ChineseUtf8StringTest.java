package org.web3j.abi.datatypes;

import org.junit.jupiter.api.Test;
import org.web3j.abi.TypeEncoder;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChineseUtf8StringTest {

    @Test
    public void testChineseStringEncoding() {
        // Chinese text: Zhong Guo Liu Li Fa Lang Cai Hua Niao Zi Ming Zhong
        String chineseText = "\u4e2d\u56fd\u7409\u7483\u73d0\u7405\u5f69\u82b1\u9e1f\u81ea\u9e23\u949f";
        Utf8String utf8String = new Utf8String(chineseText);
        
        String encoded = TypeEncoder.encode(utf8String);
        System.out.println("Encoded: " + encoded);
        
        byte[] bytes = chineseText.getBytes(StandardCharsets.UTF_8);
        System.out.println("Byte length: " + bytes.length);
        
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
        System.out.println("Array Encoded: " + encoded);
    }
}
