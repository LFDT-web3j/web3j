/*
 * Copyright 2020 Web3 Labs Ltd.
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
package org.web3j.protocol.geth;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.web3j.EVMTest;
import org.web3j.NodeType;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TxPoolContentFrom;
import org.web3j.protocol.core.methods.response.TxPoolInspect;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EVMTest(type = NodeType.GETH)
public class TxPoolIT {

    private static Web3j web3j;

    public TxPoolIT() {}

    @BeforeAll
    public static void setUp(
            Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        TxPoolIT.web3j = web3j;
    }

    @Test
    public void testTxPoolContentFrom() throws Exception {
        String coinbase = web3j.ethCoinbase().send().getAddress();
        TxPoolContentFrom contentFrom = web3j.txPoolContentFrom(coinbase).send();
        assertNotNull(contentFrom.getResult());
    }

    @Test
    public void testTxPoolInspect() throws Exception {
        TxPoolInspect inspect = web3j.txPoolInspect().send();
        assertNotNull(inspect.getResult());
        assertNotNull(inspect.getResult().getPending());
        assertNotNull(inspect.getResult().getQueued());
    }

    @Test
    public void testTxPoolInspectWithQueuedTransaction() throws Exception {
        String coinbase = web3j.ethCoinbase().send().getAddress();

        BigInteger nonce =
                web3j.ethGetTransactionCount(coinbase, DefaultBlockParameterName.PENDING)
                        .send()
                        .getTransactionCount();

        Transaction tx =
                Transaction.createEtherTransaction(
                        coinbase,
                        nonce.add(BigInteger.valueOf(100)),
                        BigInteger.valueOf(1_000_000_000L),
                        BigInteger.valueOf(21_000L),
                        "0x0000000000000000000000000000000000000000",
                        BigInteger.ONE);

        web3j.ethSendTransaction(tx).send();

        TxPoolInspect inspect = web3j.txPoolInspect().send();
        assertNotNull(inspect.getResult());
        assertNotNull(inspect.getResult().getQueued());
    }
}
