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
package org.web3j.protocol.core.methods.response;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.ResponseTester;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TxPoolInspectTest extends ResponseTester {

    @Test
    public void testTxPoolInspect() {
        buildResponse(
                "{\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"id\": 1,\n"
                        + "  \"result\": {\n"
                        + "    \"pending\": {\n"
                        + "      \"0x0032D05F320fa74C871E892F48F0e6387c0Dfe95\": {\n"
                        + "        \"0\": \"0.1 ETH + 21000 gas\"\n"
                        + "      }\n"
                        + "    },\n"
                        + "    \"queued\": {\n"
                        + "      \"0x00Bf700CeB382877F8bFa38b05fcC81126f4f228\": {\n"
                        + "        \"49\": \"0.05 ETH + 21000 gas\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}");

        TxPoolInspect content = deserialiseResponse(TxPoolInspect.class);

        assertEquals(
                "0.1 ETH + 21000 gas",
                content.getResult().getPending().get("0x0032D05F320fa74C871E892F48F0e6387c0Dfe95").get(BigInteger.ZERO));
        assertEquals(
                "0.05 ETH + 21000 gas",
                content.getResult().getQueued().get("0x00Bf700CeB382877F8bFa38b05fcC81126f4f228").get(new BigInteger("49")));
    }
}
