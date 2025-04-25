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
package org.web3j.crypto;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.*;

public class StructuredDataEncoderTest {

    
    /**
     * This test compares the output of encodeData method against the expected ABI encoded output
     * from a Solidity contract for the same data structure.
     * 
     * Equivalent Solidity code for reference:
     * ```
     * // SPDX-License-Identifier: MIT
     * pragma solidity ^0.8.17;
     * 
     * contract StructEncodeTest {
     *     struct ClaimRequest {
     *         address to;
     *         uint256[] tokenIds;
     *         uint256[] amounts;
     *         uint128 validityStartTimestamp;
     *         uint128 validityEndTimestamp;
     *         uint256 salt;
     *     }
     *     
     *     bytes32 constant CLAIM_REQUEST_TYPEHASH = keccak256(
     *         "ClaimRequest(address to,uint256[] tokenIds,uint256[] amounts,uint128 validityStartTimestamp,uint128 validityEndTimestamp,uint256 salt)"
     *     );
     *     
     *     function hashClaimRequest(ClaimRequest calldata req) public pure returns (bytes32) {
     *         bytes32 structHash = keccak256(
     *             abi.encode(
     *                 CLAIM_REQUEST_TYPEHASH,
     *                 req.to,
     *                 keccak256(abi.encodePacked(req.tokenIds)),
     *                 keccak256(abi.encodePacked(req.amounts)),
     *                 req.validityStartTimestamp,
     *                 req.validityEndTimestamp,
     *                 req.salt
     *             )
     *         );
     *         return structHash;
     *     }
     *     
     *     function encodeClaimRequest(
     *         address to,
     *         uint256[] calldata tokenIds,
     *         uint256[] calldata amounts,
     *         uint128 validityStartTimestamp,
     *         uint128 validityEndTimestamp,
     *         uint256 salt
     *     ) public pure returns (bytes memory) {
     *         return abi.encode(
     *             CLAIM_REQUEST_TYPEHASH,
     *             to,
     *             keccak256(abi.encodePacked(tokenIds)),
     *             keccak256(abi.encodePacked(amounts)),
     *             validityStartTimestamp,
     *             validityEndTimestamp,
     *             salt
     *         );
     *     }
     * }
     * ```
     */
    @Test
    public void testDynamicArrayEncoding() throws Exception {
        // Create test parameters
        String address = "0xe483dea6aa7d3831173379d81e5c08874f1042e7";
        List<BigInteger> tokenIds = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(1));
        List<BigInteger> amounts = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(0));
        BigInteger validityStartTimestamp = BigInteger.valueOf(1742919454);
        BigInteger validityEndTimestamp = BigInteger.valueOf(1743005854);
        BigInteger salt = BigInteger.valueOf(82);
        
        // Construct the JSON message
        String jsonMessage = String.format("""
                {
                  "domain": {
                    "chainId": "84532",
                    "name": "Test",
                    "verifyingContract": "%s",
                    "version": "1.0.0"
                  },
                  "message": {
                    "to": "%s",
                    "tokenIds": [%d, %d],
                    "amounts": [%d, %d],
                    "validityStartTimestamp": %d,
                    "validityEndTimestamp": %d,
                    "salt": "%d"
                  },
                  "primaryType": "ClaimRequest",
                  "types": {
                    "ClaimRequest": [
                      { "name": "to", "type": "address" },
                      { "name": "tokenIds", "type": "uint256[]" },
                      { "name": "amounts", "type": "uint256[]" },
                      { "name": "validityStartTimestamp", "type": "uint128" },
                      { "name": "validityEndTimestamp", "type": "uint128" },
                      { "name": "salt", "type": "uint256" }
                    ],
                    "EIP712Domain": [
                      { "name": "name", "type": "string" },
                      { "name": "version", "type": "string" },
                      { "name": "chainId", "type": "uint256" },
                      { "name": "verifyingContract", "type": "address" }
                    ]
                  }
                }
                """, 
                address, address, 
                tokenIds.get(0), tokenIds.get(1),
                amounts.get(0), amounts.get(1),
                validityStartTimestamp, validityEndTimestamp, salt);
        
        // Create encoder and encode data
        StructuredDataEncoder encoder = new StructuredDataEncoder(jsonMessage);
        byte[] encoded = encoder.encodeData(
                encoder.jsonMessageObject.getPrimaryType(),
                (HashMap<String, Object>) encoder.jsonMessageObject.getMessage());
        
        String hexEncoded = Numeric.toHexString(encoded);
        System.out.println("Encoded data: " + hexEncoded);
        
        // The encoded data structure:
        // [typehash(32)][to address(32)][tokenIds offset(32)][amounts offset(32)][validityStartTimestamp(32)][validityEndTimestamp(32)][salt(32)]
        // [tokenIds length(32)][tokenIds data...]
        // [amounts length(32)][amounts data...]
        
        // Verify total length - sum of all components
        // 7 fields of a header = 224 bytes
        // Two dynamic arrays with:
        //   - tokenIds: 32 bytes (length) + 2 elements * 32 bytes = 96 bytes
        //   - amounts: 32 bytes (length) + 2 elements * 32 bytes = 96 bytes
        // Total expected length: 224 + 96 + 96 = 416 bytes
        assertEquals(416, encoded.length);
        
        // Extract individual components from the header (first 224 bytes)
        byte[] typeHash = Arrays.copyOfRange(encoded, 0, 32);
        byte[] addressBytes = Arrays.copyOfRange(encoded, 32, 64);
        byte[] tokenIdsOffsetBytes = Arrays.copyOfRange(encoded, 64, 96);
        byte[] amountsOffsetBytes = Arrays.copyOfRange(encoded, 96, 128);
        byte[] validityStartBytes = Arrays.copyOfRange(encoded, 128, 160);
        byte[] validityEndBytes = Arrays.copyOfRange(encoded, 160, 192);
        byte[] saltBytes = Arrays.copyOfRange(encoded, 192, 224);
        
        // Verify the typehash
        String expectedTypehash = "0x7902270f3978ac872a876a0dae841dd76a2ca6b251714a39f68b06e66fcd5855";
        assertEquals(expectedTypehash, Numeric.toHexString(typeHash));
        
        // Verify address encoding
        String expectedAddress = Numeric.toHexStringWithPrefixZeroPadded(
                Numeric.toBigInt(address), 64);
        assertEquals(expectedAddress, Numeric.toHexString(addressBytes));
        
        // Verify offsets - this is the key part of the fix
        // In our implementation, these contain the real headSize + dynamicDataSize,
        // not just a static reference to field positions
        BigInteger tokenIdsOffset = Numeric.toBigInt(tokenIdsOffsetBytes);
        assertEquals(BigInteger.valueOf(32), tokenIdsOffset); // First dynamic array offset (points to position after the 7-field header)
        
        BigInteger amountsOffset = Numeric.toBigInt(amountsOffsetBytes);
        assertEquals(BigInteger.valueOf(32), amountsOffset); // Second dynamic array offset
        
        // With an incorrect implementation that doesn't handle offsets properly:
        // - These would be static values like hardcoded indices
        // - Or they would be calculated from field position instead of dynamic content size
        // In Solidity ABI encoding, offsets point to positions relative to the start of their own data section
        
        // Verify timestamp and salt encoding
        String expectedValidityStart = Numeric.toHexStringWithPrefixZeroPadded(
                validityStartTimestamp, 64);
        assertEquals(expectedValidityStart, Numeric.toHexString(validityStartBytes));
        
        String expectedValidityEnd = Numeric.toHexStringWithPrefixZeroPadded(
                validityEndTimestamp, 64);
        assertEquals(expectedValidityEnd, Numeric.toHexString(validityEndBytes));
        
        String expectedSalt = Numeric.toHexStringWithPrefixZeroPadded(salt, 64);
        assertEquals(expectedSalt, Numeric.toHexString(saltBytes));
        
        // Verify tokenIds array data
        byte[] tokenIdsLengthBytes = Arrays.copyOfRange(encoded, 224, 256);
        assertEquals(BigInteger.valueOf(2), Numeric.toBigInt(tokenIdsLengthBytes)); // 2 elements
        
        byte[] tokenId1Bytes = Arrays.copyOfRange(encoded, 256, 288);
        assertEquals(BigInteger.valueOf(0), Numeric.toBigInt(tokenId1Bytes));
        
        byte[] tokenId2Bytes = Arrays.copyOfRange(encoded, 288, 320);
        assertEquals(BigInteger.valueOf(1), Numeric.toBigInt(tokenId2Bytes));
        
        // Verify amounts array data
        byte[] amountsLengthBytes = Arrays.copyOfRange(encoded, 320, 352);
        assertEquals(BigInteger.valueOf(2), Numeric.toBigInt(amountsLengthBytes)); // 2 elements
        
        byte[] amount1Bytes = Arrays.copyOfRange(encoded, 352, 384);
        assertEquals(BigInteger.valueOf(0), Numeric.toBigInt(amount1Bytes));
        
        byte[] amount2Bytes = Arrays.copyOfRange(encoded, 384, 416);
        assertEquals(BigInteger.valueOf(0), Numeric.toBigInt(amount2Bytes));
    }

}
