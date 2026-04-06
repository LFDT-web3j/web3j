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
package org.web3j.protocol.core.filters;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.Response.Error;
import org.web3j.protocol.core.RpcErrors;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthUninstallFilter;

/** Class for creating managed filter requests with callbacks. */
public abstract class Filter<T> {

    private static final Logger log = LoggerFactory.getLogger(Filter.class);

    protected final Web3j web3j;
    protected Callback<T> callback;

    private volatile BigInteger filterId;

    protected ScheduledFuture<?> schedule;

    private ScheduledExecutorService scheduledExecutorService;

    private long blockTime;

    private static final String FILTER_NOT_FOUND_PATTERN = "(?i)\\bfilter\\s+not\\s+found\\b";

    public Filter(Web3j web3j, Callback<T> callback) {
        this.web3j = web3j;
        this.callback = callback;
    }

    public void run(ScheduledExecutorService scheduledExecutorService, long blockTime) {
        try {
            EthFilter ethFilter = sendRequest();
            if (ethFilter.hasError()) {
                throwException(ethFilter.getError());
            }

            filterId = ethFilter.getFilterId();
            this.scheduledExecutorService = scheduledExecutorService;
            this.blockTime = blockTime;
            // this runs in the caller thread as if any exceptions are encountered, we shouldn't
            // proceed with creating the scheduled task below
            getInitialFilterLogs();

            /*
            We want the filter to be resilient against client issues. On numerous occasions
            users have reported socket timeout exceptions when connected over HTTP to Geth and
            Parity clients. For examples, refer to
            https://github.com/web3j/web3j/issues/144 and
            https://github.com/ethereum/go-ethereum/issues/15243.

            Hence we consume errors and log them as errors, allowing our polling for changes to
            resume. The downside of this approach is that users will not be notified of
            downstream connection issues. But given the intermittent nature of the connection
            issues, this seems like a reasonable compromise.

            The alternative approach would be to have another thread that blocks waiting on
            schedule.get(), catching any Exceptions thrown, and passing them back up to the
            caller. However, the user would then be required to recreate subscriptions manually
            which isn't ideal given the aforementioned issues.
            */
            schedule =
                    scheduledExecutorService.scheduleAtFixedRate(
                            () -> {
                                try {
                                    this.pollFilter(ethFilter);
                                } catch (Throwable e) {
                                    // All exceptions must be caught, otherwise our job terminates
                                    // without
                                    // any notification
                                    log.warn("Error sending request", e);
                                }
                            },
                            0,
                            blockTime,
                            TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            throwException(e);
        }
    }

    private void getInitialFilterLogs() {
        try {
            Optional<Request<?, EthLog>> maybeRequest = this.getFilterLogs(this.filterId);
            EthLog ethLog = null;
            if (maybeRequest.isPresent()) {
                ethLog = maybeRequest.get().send();
            } else {
                ethLog = new EthLog();
                ethLog.setResult(Collections.emptyList());
            }

            if (ethLog.hasError()) {
                throwException(ethLog.getError());
            }

            process(ethLog.getLogs());

        } catch (IOException e) {
            throwException(e);
        }
    }

    private void pollFilter(EthFilter ethFilter) {
        EthLog ethLog = null;
        try {
            ethLog = web3j.ethGetFilterChanges(filterId).send();
        } catch (IOException e) {
            throwException(e);
        }
        if (ethLog.hasError()) {
            Error error = ethLog.getError();
            String message = error.getMessage();
            switch (error.getCode()) {
                case RpcErrors.FILTER_NOT_FOUND:
                    reinstallFilter();
                    break;
                default:
                    if (Pattern.compile(FILTER_NOT_FOUND_PATTERN).matcher(message).find())
                        reinstallFilter();
                    else throwException(error);
                    break;
            }
        } else {
            process(ethLog.getLogs());
        }
    }

    protected abstract EthFilter sendRequest() throws IOException;

    protected abstract void process(List<EthLog.LogResult<?>> logResults);

    private void reinstallFilter() {
        log.warn(
                "Previously installed filter has not been found, trying to re-install. Filter id: {}",
                filterId);
        schedule.cancel(false);
        this.run(scheduledExecutorService, blockTime);
    }

    public void cancel() {
        schedule.cancel(false);

        try {
            EthUninstallFilter ethUninstallFilter = uninstallFilter(filterId);
            if (ethUninstallFilter.hasError()) {
                throwException(ethUninstallFilter.getError());
            }

            if (!ethUninstallFilter.isUninstalled()) {
                throw new FilterException("Filter with id '" + filterId + "' failed to uninstall");
            }
        } catch (IOException e) {
            throwException(e);
        }
    }

    protected EthUninstallFilter uninstallFilter(BigInteger filterId) throws IOException {
        return web3j.ethUninstallFilter(filterId).send();
    }

    /**
     * Retrieves historic filters for the filter with the given id. Getting historic logs is not
     * supported by all filters. If not the method should return an empty EthLog object
     *
     * @param filterId Id of the filter for which the historic log should be retrieved
     * @return Historic logs, or an empty optional if the filter cannot retrieve historic logs
     */
    protected abstract Optional<Request<?, EthLog>> getFilterLogs(BigInteger filterId);

    void throwException(Response.Error error) {
        throw new FilterException(
                "Invalid request: " + (error == null ? "Unknown Error" : error.getMessage()));
    }

    void throwException(Throwable cause) {
        throw new FilterException("Error sending request", cause);
    }
}
