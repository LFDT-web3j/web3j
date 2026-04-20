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
package org.web3j.protocol.scenarios;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.web3j.EVMTest;
import org.web3j.NodeType;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.test.contract.Fibonacci;
import org.web3j.tx.gas.ContractGasProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Filter scenario integration tests. */
@EVMTest(type = NodeType.BESU)
public class EventFilterIT extends Scenario {

    private static Fibonacci fib;

    @BeforeAll
    public static void setUP(Web3j web3j, ContractGasProvider gasProvider) throws Exception {

        EventFilterIT.fib = Fibonacci.deploy(web3j, ALICE, gasProvider).send();
        Scenario.web3j = web3j;
    }

    @Test
    public void testEventFilter() throws Exception {
        TransactionReceipt transactionReceipt = fib.fibonacciNotify(BigInteger.ONE).send();
        List<Log> logs = transactionReceipt.getLogs();
        assertFalse(logs.isEmpty());
        Log log = logs.get(0);
        List<String> topics = log.getTopics();
        assertEquals(topics.size(), (1));
        Event event =
                new Event(
                        "Notify",
                        Arrays.asList(
                                new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));

        // check function signature - we only have a single topic our event signature,
        // there are no indexed parameters in this example
        String encodedEventSignature = EventEncoder.encode(event);
        assertEquals(topics.get(0), (encodedEventSignature));

        // verify our two event parameters
        List<Type> results =
                FunctionReturnDecoder.decode(log.getData(), event.getNonIndexedParameters());
        results.forEach(it -> assertEquals(it.getValue(), BigInteger.valueOf(1)));
        assertEquals(
                results,
                (Arrays.asList(
                        new Uint256(BigInteger.valueOf(1)), new Uint256(BigInteger.valueOf(1)))));

        // finally check it shows up in the event filter
        List<EthLog.LogResult<?>> filterLogs = createFilterForEvent(encodedEventSignature);
        assertFalse(filterLogs.isEmpty());
    }

    private List<EthLog.LogResult<?>> createFilterForEvent(String encodedEventSignature)
            throws Exception {
        EthFilter ethFilter =
                new EthFilter(
                        DefaultBlockParameterName.EARLIEST,
                        DefaultBlockParameterName.LATEST,
                        EventFilterIT.fib.getContractAddress());

        ethFilter.addSingleTopic(encodedEventSignature);
        EthLog ethLog = web3j.ethGetLogs(ethFilter).send();
        return ethLog.getLogs();
    }
}
