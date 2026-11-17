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
package org.web3j.protocol.core;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JsonRpc2_0Web3jTest {

    private ScheduledExecutorService scheduledExecutorService =
            mock(ScheduledExecutorService.class);
    private Web3jService service = mock(Web3jService.class);

    private Web3j web3j = Web3j.build(service, 10, scheduledExecutorService);

    @Test
    void testStopExecutorOnShutdown() throws Exception {
        web3j.shutdown();

        verifyServicesClosed();
    }

    @Test
    void testExceptionOnServiceClosure() {

        assertThrows(
                RuntimeException.class,
                () -> {
                    doThrow(new IOException("Failed to close")).when(service).close();

                    web3j.shutdown();
                });
    }

    @Test
    void shouldShutdownOnAutoClose() throws IOException {
        try (Web3j web3j = Web3j.build(service, 10, scheduledExecutorService)) {
            // Web3j.close() delegates to shutdown() and requires no checked Exception handling
        }
        verifyServicesClosed();
    }

    @Test
    void shouldCloseWithoutCheckedExceptionHandling() {
        try (Web3j web3j = Web3j.build(service, 10, scheduledExecutorService)) {
            // Verifies Web3j can be used in try-with-resources without catching or declaring
            // Exception
        }
        verify(scheduledExecutorService).shutdown();
    }

    @Test
    void shouldCloseWeb3jServiceOnAutoClose() throws IOException {
        try (Web3jService web3jService = service) {
            // Verifies Web3jService can be used in try-with-resources directly
        }
        verify(service).close();
    }

    private void verifyServicesClosed() throws IOException {
        verify(scheduledExecutorService).shutdown();
        verify(service).close();
    }
}
