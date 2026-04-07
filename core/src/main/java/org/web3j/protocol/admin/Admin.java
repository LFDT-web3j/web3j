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
package org.web3j.protocol.admin;

import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalListAccounts;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.admin.methods.response.TxPoolContent;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TxPoolContentFrom;

/**
 * JSON-RPC Request object building factory for common Parity and Geth. Personal namespace has now
 * been deprecated in favour of using Clef as an external signer and account manager. Read more -
 * https://geth.ethereum.org/docs/interacting-with-geth/rpc/ns-personal
 */
@Deprecated()
public interface Admin extends Web3j {

    static Admin build(Web3jService web3jService) {
        return new JsonRpc2_0Admin(web3jService);
    }

    static Admin build(
            Web3jService web3jService,
            long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Admin(web3jService, pollingInterval, scheduledExecutorService);
    }

    public Request<?, PersonalListAccounts> personalListAccounts();

    public Request<?, NewAccountIdentifier> personalNewAccount(String password);

    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String address, String passphrase, BigInteger duration);

    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String address, String passphrase);

    public Request<?, EthSendTransaction> personalSendTransaction(
            Transaction transaction, String password);

    public Request<?, TxPoolContent> txPoolContent();

    Request<?, TxPoolContentFrom> txPoolContentFrom(String address);
}
