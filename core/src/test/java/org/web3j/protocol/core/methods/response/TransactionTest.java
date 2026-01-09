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
package org.web3j.protocol.core.methods.response;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TransactionTest {

    @Test
    void testTransactionWithAuthorizationListEquality() {
        List<AuthorizationTuple> authList =
                Arrays.asList(
                        new AuthorizationTuple(
                                "0x1",
                                "0x7b73644935b8e68019ac6356c40661e1bc315860",
                                "0x0",
                                "0x1",
                                "0x1234",
                                "0x5678"));

        Transaction tx1 =
                new Transaction(
                        "0xhash",
                        "0x1",
                        "0xblockhash",
                        "0x1",
                        "0x1",
                        "0x1",
                        "0xfrom",
                        "0xto",
                        "0x1",
                        "0x1",
                        "0x1",
                        "0x",
                        null,
                        null,
                        null,
                        "0x1",
                        "0x1",
                        0,
                        "0x0",
                        "0x4",
                        "0x1",
                        "0x1",
                        null,
                        null,
                        null,
                        authList);

        Transaction tx2 =
                new Transaction(
                        "0xhash",
                        "0x1",
                        "0xblockhash",
                        "0x1",
                        "0x1",
                        "0x1",
                        "0xfrom",
                        "0xto",
                        "0x1",
                        "0x1",
                        "0x1",
                        "0x",
                        null,
                        null,
                        null,
                        "0x1",
                        "0x1",
                        0,
                        "0x0",
                        "0x4",
                        "0x1",
                        "0x1",
                        null,
                        null,
                        null,
                        authList);

        Transaction tx3 =
                new Transaction(
                        "0xhash",
                        "0x1",
                        "0xblockhash",
                        "0x1",
                        "0x1",
                        "0x1",
                        "0xfrom",
                        "0xto",
                        "0x1",
                        "0x1",
                        "0x1",
                        "0x",
                        null,
                        null,
                        null,
                        "0x1",
                        "0x1",
                        0,
                        "0x0",
                        "0x4",
                        "0x1",
                        "0x1",
                        null,
                        null,
                        null,
                        null);

        assertEquals(tx1, tx2);
        assertNotEquals(tx1, tx3);
    }
}
