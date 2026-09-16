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
package org.web3j.protocol.core.methods.response;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.ObjectMapperFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EthBlockTest {

    @Test
    void testEthBlockNullSize() {
        EthBlock.Block ethBlock =
                new EthBlock.Block(
                        null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null);

        assertEquals(BigInteger.ZERO, ethBlock.getSize());
    }

    @Test
    void testEthBlockNotNullSize() {
        EthBlock.Block ethBlock =
                new EthBlock.Block(
                        null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, "0x3e8", null, null, null, null, null, null, null,
                        null, null, null, null);

        assertEquals(BigInteger.valueOf(1000), ethBlock.getSize());
    }

    @Test
    void testNewHeaderFields() {
        String balHash = "0x" + "22".repeat(32);
        String requestsHash = "0x" + "33".repeat(32);
        EthBlock.Block block =
                readBlock(
                        """
                {"slotNumber":"0x0","blockAccessListHash":"%s","requestsHash":"%s"}
                """
                                .formatted(balHash, requestsHash));

        assertEquals(BigInteger.ZERO, block.getSlotNumber());
        assertEquals("0x0", block.getSlotNumberRaw());
        assertEquals(balHash, block.getBlockAccessListHash());
        assertEquals(requestsHash, block.getRequestsHash());
    }

    @Test
    void testAbsentHeaderFields() {
        EthBlock.Block block = readBlock("{}");

        assertNull(block.getSlotNumber());
        assertNull(block.getSlotNumberRaw());
        assertNull(block.getBlockAccessListHash());
        assertNull(block.getRequestsHash());
    }

    private EthBlock.Block readBlock(String result) {
        return ObjectMapperFactory.getObjectMapper()
                .readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":" + result + "}", EthBlock.class)
                .getBlock();
    }
}
