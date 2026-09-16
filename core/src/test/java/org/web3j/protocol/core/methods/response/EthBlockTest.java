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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.web3j.protocol.ObjectMapperFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testNewHeaderFieldsWithTransactions(boolean fullTransactions) {
        String transactionHash = "0x" + "11".repeat(32);
        String balHash = "0x" + "22".repeat(32);
        String requestsHash = "0x" + "33".repeat(32);
        String transaction =
                fullTransactions
                        ? "{\"hash\":\"" + transactionHash + "\"}"
                        : "\"" + transactionHash + "\"";
        EthBlock.Block block =
                readBlock(
                        """
                {"slotNumber":"0xffffffffffffffff",
                 "blockAccessListHash":"%s","requestsHash":"%s",
                 "transactions":[%s]}
                """
                                .formatted(balHash, requestsHash, transaction));

        assertEquals(new BigInteger("18446744073709551615"), block.getSlotNumber());
        assertEquals("0xffffffffffffffff", block.getSlotNumberRaw());
        assertEquals(balHash, block.getBlockAccessListHash());
        assertEquals(requestsHash, block.getRequestsHash());
        assertEquals(1, block.getTransactions().size());
        if (fullTransactions) {
            EthBlock.TransactionObject tx =
                    assertInstanceOf(
                            EthBlock.TransactionObject.class, block.getTransactions().get(0));
            assertEquals(transactionHash, tx.getHash());
        } else {
            assertEquals(transactionHash, block.getTransactions().get(0).get());
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"slotNumber\":null,\"blockAccessListHash\":null,\"requestsHash\":null}"
            })
    void testAbsentHeaderFields(String json) {
        EthBlock.Block block = readBlock(json);

        assertNull(block.getSlotNumber());
        assertNull(block.getSlotNumberRaw());
        assertNull(block.getBlockAccessListHash());
        assertNull(block.getRequestsHash());
    }

    @Test
    void testSlotZeroIsDistinctFromAbsentSlot() {
        EthBlock.Block block = readBlock("{\"slotNumber\":\"0x0\"}");

        assertEquals(BigInteger.ZERO, block.getSlotNumber());
        assertEquals("0x0", block.getSlotNumberRaw());
        assertNotEquals(readBlock("{}"), block);
    }

    private EthBlock.Block readBlock(String result) {
        return ObjectMapperFactory.getObjectMapper()
                .readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":" + result + "}", EthBlock.class)
                .getBlock();
    }
}
