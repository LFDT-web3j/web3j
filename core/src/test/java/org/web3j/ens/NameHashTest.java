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
package org.web3j.ens;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.web3j.ens.NameHash.labelHash;
import static org.web3j.ens.NameHash.nameHash;
import static org.web3j.ens.NameHash.normalise;

class NameHashTest {

    @Test
    void testNameHash() {
        assertEquals(
                nameHash(""),
                ("0x0000000000000000000000000000000000000000000000000000000000000000"));

        assertEquals(
                nameHash("eth"),
                ("0x93cdeb708b7545dc668eb9280176169d1c33cfd8ed6f04690a0bcc88a93fc4ae"));

        assertEquals(
                nameHash("foo.eth"),
                ("0xde9b09fd7c5f901e23a3f19fecc54828e9c848539801e86591bd9801b019f84f"));

        assertEquals(
                nameHash("\uD83D\uDC8E.gmcafe.art"),
                ("0xf7de5954cda078ee481b14cff677e8066fe805a89b5c87b4a9b866338049b04a"));
    }

    @Test
    void testLabelHash() {
        assertEquals(
                labelHash(""),
                ("0x0000000000000000000000000000000000000000000000000000000000000000"));

        assertEquals(
                labelHash("label"),
                ("0x1b036544434cea9770a413fd03e0fb240e1ccbd10a452f7dba85c8eca9ca3eda"));

        assertEquals(
                labelHash("label.eth"),
                ("0x1b036544434cea9770a413fd03e0fb240e1ccbd10a452f7dba85c8eca9ca3eda"));

        assertEquals(
                labelHash("\uD83D\uDC8E.gmcafe.art"),
                ("0xed2cc33b0587afe42a04579d79c23b72b25c5416efcb50c674f3f59e2db9cce6"));
    }

    @Test
    void testNormalise() {
        assertEquals(normalise("foo"), ("foo"));
        assertEquals(normalise("foo.bar.baz.eth"), ("foo.bar.baz.eth"));
        assertEquals(normalise("fOo.eth"), ("foo.eth"));
        assertEquals(normalise("foo-bar.eth"), ("foo-bar.eth"));

        assertEquals(normalise("Obb.at"), ("obb.at"));
        assertEquals(normalise("TESTER.eth"), ("tester.eth"));
        assertEquals(normalise("test\u200btest.com"), ("testtest.com"));
        assertEquals(normalise("hyph-‐‑‒–—―⁃−⎯⏤﹘e⸺n⸻s.eth"), ("hyph------------e--n---s.eth"));
    }

    @Test
    void testNormaliseInvalid() {
        testInvalidName("foo..bar");
        testInvalidName("ba\\u007Fr.eth");
        testInvalidName("foo_bar.eth");
        testInvalidName("..a..eth");
        testInvalidName("aα.ɑ");
        testInvalidName("0x.0χ.0х");
    }

    @Test
    void testDnsEncode() throws IOException {
        String dnsEncoded = NameHash.dnsEncode("1.offchainexample.eth");
        assertEquals("0x01310f6f6666636861696e6578616d706c650365746800", dnsEncoded);
    }

    private void testInvalidName(String ensName) {
        try {
            normalise(ensName);
            fail("Name should not be valid");
        } catch (EnsResolutionException e) {
            // expected
        }
    }
}
