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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;

/** Logs filter handler. */
public class LogsFilter extends Filter<List<Log>> {

    private final org.web3j.protocol.core.methods.request.EthFilter ethFilter;

    public LogsFilter(
            Web3j web3j,
            Callback<List<Log>> callback,
            org.web3j.protocol.core.methods.request.EthFilter ethFilter) {
        super(web3j, callback);
        this.ethFilter = ethFilter;
    }

    @Override
    protected EthFilter sendRequest() throws IOException {
        return web3j.ethNewFilter(ethFilter).send();
    }

    @Override
    protected void process(List<LogResult<?>> logResults) {
        List<Log> logs = new ArrayList<>(logResults.size());

        for (EthLog.LogResult<?> logResult : logResults) {
            if (!(logResult instanceof EthLog.LogObject)) {
                throw new FilterException(
                        "Unexpected result type: " + logResult.get() + " required LogObject");
            }

            logs.add(((EthLog.LogObject) logResult).get());
        }

        callback.onEvent(logs);
    }

    @Override
    protected Optional<Request<?, EthLog>> getFilterLogs(BigInteger filterId) {
        return Optional.of(web3j.ethGetFilterLogs(filterId));
    }
}
