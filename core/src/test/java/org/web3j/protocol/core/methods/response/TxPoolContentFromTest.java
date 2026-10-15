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

import org.junit.jupiter.api.Test;

import org.web3j.protocol.ResponseTester;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TxPoolContentFromTest extends ResponseTester {

    @Test
    public void testTxPoolContentFrom() {
        buildResponse(
                "{\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"id\": 1,\n"
                        + "  \"result\": {\n"
                        + "    \"pending\": {\n"
                        + "      \"0\": {\n"
                        + "        \"from\": \"0x0032d05f320fa74c871e892f48f0e6387c0dfe95\",\n"
                        + "        \"gas\": \"0x63cad\",\n"
                        + "        \"gasPrice\": \"0x1\",\n"
                        + "        \"hash\": \"0x56cf53cbd377535c14b28cd373fa43d129f501b1a20b36903fd14b747c3f6cf5\",\n"
                        + "        \"nonce\": \"0x0\",\n"
                        + "        \"value\": \"0x0\"\n"
                        + "      }\n"
                        + "    },\n"
                        + "    \"queued\": {\n"
                        + "      \"49\": {\n"
                        + "        \"from\": \"0x00bf700ceb382877f8bfa38b05fcc81126f4f228\",\n"
                        + "        \"gas\": \"0xfa00\",\n"
                        + "        \"gasPrice\": \"0x3b9aca00\",\n"
                        + "        \"hash\": \"0xa87ab980c4d277de6c4faf2670a2ec1b6e577482e582c8082d208b7e630cf395\",\n"
                        + "        \"nonce\": \"0x31\",\n"
                        + "        \"value\": \"0x0\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}");

        TxPoolContentFrom content = deserialiseResponse(TxPoolContentFrom.class);

        assertEquals(
                "0x0032d05f320fa74c871e892f48f0e6387c0dfe95",
                content.getResult().getPendingTransactions().get(0).getFrom());
        assertEquals(
                "0x00bf700ceb382877f8bfa38b05fcc81126f4f228",
                content.getResult().getQueuedTransactions().get(0).getFrom());
    }
}
