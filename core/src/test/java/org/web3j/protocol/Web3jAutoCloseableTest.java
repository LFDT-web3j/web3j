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
package org.web3j.protocol;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.core.JsonRpc2_0Web3j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Web3jAutoCloseableTest {

    @Test
    public void testTryWithResources() {
        Web3jService serviceMock = mock(Web3jService.class);
        Web3j web3j = new JsonRpc2_0Web3j(serviceMock);

        try (web3j) {
            // Use web3j
        }

        // shutdown should be called on the service (delegated from web3j.shutdown)
        verify(serviceMock).close();
    }

    @Test
    public void testWeb3jServiceAutoCloseable() {
        Web3jService serviceMock = mock(Web3jService.class);

        try (Web3jService service = serviceMock) {
            // Use service
        }

        verify(serviceMock).close();
    }
}
