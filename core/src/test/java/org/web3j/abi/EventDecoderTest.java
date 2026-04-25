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
package org.web3j.abi;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventDecoderTest {

    @Test
    public void testDecodeEvent() {
        // ABI for Transfer(address indexed from, address indexed to, uint256 value)
        String abiJson =
                "[{"
                        + "\"anonymous\":false,"
                        + "\"inputs\":["
                        + "  {\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},"
                        + "  {\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},"
                        + "  {\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}"
                        + "],"
                        + "\"name\":\"Transfer\","
                        + "\"type\":\"event\""
                        + "}]";

        // Synthetic Log
        Log log = new Log();

        // Setup signature and indexed parameters for Transfer event
        Event transferEvent =
                new Event(
                        "Transfer",
                        Arrays.asList(
                                new TypeReference<Address>(true) {},
                                new TypeReference<Address>(true) {},
                                new TypeReference<Uint256>(false) {}));

        String encodedEventSignature = EventEncoder.encode(transferEvent);

        // Topics: [signature, from, to]
        log.setTopics(
                Arrays.asList(
                        encodedEventSignature,
                        "0x0000000000000000000000001234567890123456789012345678901234567890",
                        "0x0000000000000000000000000987654321098765432109876543210987654321"));

        // Data: value (100)
        log.setData("0x0000000000000000000000000000000000000000000000000000000000000064");

        Map<String, Object> result = EventDecoder.decodeEvent(abiJson, log);

        assertEquals(3, result.size());
        assertEquals("0x1234567890123456789012345678901234567890", result.get("from"));
        assertEquals("0x0987654321098765432109876543210987654321", result.get("to"));
        assertEquals(new BigInteger("100"), result.get("value"));
    }
}
