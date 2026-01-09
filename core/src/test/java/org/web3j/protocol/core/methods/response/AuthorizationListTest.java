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

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AuthorizationListTest {
    @Test
    void testAuthorizationTupleEquality() {
        AuthorizationTuple auth1 =
                new AuthorizationTuple(
                        "0x1",
                        "0x7b73644935b8e68019ac6356c40661e1bc315860",
                        "0x0",
                        "0x1",
                        "0x1234",
                        "0x5678");

        AuthorizationTuple auth2 =
                new AuthorizationTuple(
                        "0x1",
                        "0x7b73644935b8e68019ac6356c40661e1bc315860",
                        "0x0",
                        "0x1",
                        "0x1234",
                        "0x5678");

        AuthorizationTuple auth3 =
                new AuthorizationTuple(
                        "0x1",
                        "0x1234567890123456789012345678901234567890",
                        "0x0",
                        "0x1",
                        "0x1234",
                        "0x5678");

        assertEquals(auth1, auth2);
        assertEquals(auth1.hashCode(), auth2.hashCode());
        assertNotEquals(auth1, auth3);
    }

    @Test
    void testAuthorizationTupleGetters() {
        AuthorizationTuple auth =
                new AuthorizationTuple(
                        "0xa",
                        "0x7b73644935b8e68019ac6356c40661e1bc315860",
                        "0x5",
                        "0x1",
                        "0x1234",
                        "0x5678");

        assertEquals(BigInteger.TEN, auth.getChainId());
        assertEquals("0xa", auth.getChainIdRaw());
        assertEquals("0x7b73644935b8e68019ac6356c40661e1bc315860", auth.getAddress());
        assertEquals(BigInteger.valueOf(5), auth.getNonce());
        assertEquals("0x5", auth.getNonceRaw());
        assertEquals(BigInteger.ONE, auth.getYParity());
        assertEquals("0x1", auth.getYParityRaw());
    }
}
