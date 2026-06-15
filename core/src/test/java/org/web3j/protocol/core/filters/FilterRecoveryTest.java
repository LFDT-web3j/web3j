package org.web3j.protocol.core.filters;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthUninstallFilter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilterRecoveryTest {

    private Web3jService web3jService;
    private Web3j web3j;
    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    @BeforeEach
    public void setUp() {
        web3jService = mock(Web3jService.class);
        web3j = Web3j.build(web3jService, 1000, scheduledExecutorService);
    }

    @Test
    public void testFilterNotFoundRecoveryInInitialLogs() throws Exception {
        org.web3j.protocol.core.methods.response.EthFilter ethFilterResponse =
                objectMapper.readValue(
                        "{\"id\":1,\"jsonrpc\":\"2.0\",\"result\":\"0x1\"}",
                        org.web3j.protocol.core.methods.response.EthFilter.class);

        EthLog notFoundError =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":{\"code\":-32000,\"message\":\"filter not found\"}}",
                        EthLog.class);

        EthLog successLog =
                objectMapper.readValue("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":[]}", EthLog.class);

        EthUninstallFilter ethUninstallFilter =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":true}", EthUninstallFilter.class);

        // Mock ethNewFilter
        when(web3jService.send(
                        any(Request.class),
                        eq(org.web3j.protocol.core.methods.response.EthFilter.class)))
                .thenReturn(ethFilterResponse);

        // First call to ethGetFilterLogs returns error, subsequent returns success
        when(web3jService.send(any(Request.class), eq(EthLog.class)))
                .thenReturn(notFoundError)
                .thenReturn(successLog);

        when(web3jService.send(any(Request.class), eq(EthUninstallFilter.class)))
                .thenReturn(ethUninstallFilter);

        LogFilter filter = new LogFilter(web3j, log -> {}, new EthFilter());

        // This should not throw FilterException anymore
        filter.run(scheduledExecutorService, 100);

        // Verification: Service should have received ethNewFilter request at least twice
        verify(web3jService, atLeast(2))
                .send(
                        any(Request.class),
                        eq(org.web3j.protocol.core.methods.response.EthFilter.class));
        filter.cancel();
    }

    @Test
    public void testFilterNotFoundExceedsRetryLimit() throws Exception {
        org.web3j.protocol.core.methods.response.EthFilter ethFilterResponse =
                objectMapper.readValue(
                        "{\"id\":1,\"jsonrpc\":\"2.0\",\"result\":\"0x1\"}",
                        org.web3j.protocol.core.methods.response.EthFilter.class);

        EthLog notFoundError =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":{\"code\":-32000,\"message\":\"filter not found\"}}",
                        EthLog.class);

        when(web3jService.send(any(Request.class), eq(EthLog.class))).thenReturn(notFoundError);

        when(web3jService.send(
                        any(Request.class),
                        eq(org.web3j.protocol.core.methods.response.EthFilter.class)))
                .thenReturn(ethFilterResponse);

        LogFilter filter = new LogFilter(web3j, log -> {}, new EthFilter());

        try {
            filter.run(scheduledExecutorService, 100);
            fail("Should have thrown FilterException due to retry limit");
        } catch (FilterException e) {
            assertTrue(
                    e.getMessage().contains("Exceeded maximum number of filter re-installations"));
        }

        // Verify it tried 4 times (1 initial + 3 retries)
        verify(web3jService, times(4))
                .send(
                        any(Request.class),
                        eq(org.web3j.protocol.core.methods.response.EthFilter.class));
    }
}
