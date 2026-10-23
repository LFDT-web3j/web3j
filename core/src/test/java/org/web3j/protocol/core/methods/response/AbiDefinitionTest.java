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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbiDefinitionTest {

    @Test
    void testNamedTypeIsDynamic() {
        assertTrue(namedType("bytes").isDynamic());
        assertTrue(namedType("string").isDynamic());
        assertTrue(namedType("uint256[]").isDynamic());
        // T[k] is dynamic when T is dynamic
        assertTrue(namedType("bytes[2]").isDynamic());
        assertTrue(namedType("string[3]").isDynamic());
        assertTrue(namedType("uint256[][2]").isDynamic());
        assertTrue(namedType("bytes[2][2]").isDynamic());

        assertFalse(namedType("uint256").isDynamic());
        assertFalse(namedType("bytes32").isDynamic());
        assertFalse(namedType("bytes32[2]").isDynamic());
        assertFalse(namedType("uint256[2][2]").isDynamic());
    }

    private static AbiDefinition.NamedType namedType(String type) {
        return new AbiDefinition.NamedType("value", type);
    }
}
