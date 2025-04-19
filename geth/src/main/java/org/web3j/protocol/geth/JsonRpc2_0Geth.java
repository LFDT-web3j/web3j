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
package org.web3j.protocol.geth;

import java.util.Arrays;
import java.util.Collections;

import io.reactivex.Flowable;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.JsonRpc2_0Admin;
import org.web3j.protocol.admin.methods.response.BooleanResponse;
import org.web3j.protocol.admin.methods.response.PersonalSign;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthSubscribe;
import org.web3j.protocol.core.methods.response.MinerStartResponse;
import org.web3j.protocol.geth.response.PersonalEcRecover;
import org.web3j.protocol.geth.response.PersonalImportRawKey;
import org.web3j.protocol.websocket.events.PendingTransactionNotification;
import org.web3j.protocol.websocket.events.SyncingNotification;

/** JSON-RPC 2.0 factory implementation for Geth. */
public class JsonRpc2_0Geth extends JsonRpc2_0Admin implements Geth {

    public JsonRpc2_0Geth(Web3jService web3jService) {
        super(web3jService);
    }

    @Override
    public Request<?, PersonalImportRawKey> personalImportRawKey(String keydata, String password) {
        return new Request<>(
                "personal_importRawKey",
                Arrays.asList(keydata, password),
                web3jService,
                PersonalImportRawKey.class);
    }

    @Override
    public Request<?, BooleanResponse> personalLockAccount(String accountId) {
        return new Request<>(
                "personal_lockAccount",
                Arrays.asList(accountId),
                web3jService,
                BooleanResponse.class);
    }

    @Override
    public Request<?, PersonalSign> personalSign(
            String message, String accountId, String password) {
        return new Request<>(
                "personal_sign",
                Arrays.asList(message, accountId, password),
                web3jService,
                PersonalSign.class);
    }

    @Override
    public Request<?, PersonalEcRecover> personalEcRecover(
            String hexMessage, String signedMessage) {
        return new Request<>(
                "personal_ecRecover",
                Arrays.asList(hexMessage, signedMessage),
                web3jService,
                PersonalEcRecover.class);
    }

    @Override
    public Request<?, MinerStartResponse> minerStart(int threadCount) {
        return new Request<>(
                "miner_start", Arrays.asList(threadCount), web3jService, MinerStartResponse.class);
    }

    @Override
    public Request<?, BooleanResponse> minerStop() {
        return new Request<>(
                "miner_stop", Collections.<String>emptyList(), web3jService, BooleanResponse.class);
    }

    public Flowable<PendingTransactionNotification> newPendingTransactionsNotifications() {
        return web3jService.subscribe(
                new Request<>(
                        "eth_subscribe",
                        Arrays.asList("newPendingTransactions"),
                        web3jService,
                        EthSubscribe.class),
                "eth_unsubscribe",
                PendingTransactionNotification.class);
    }

    @Override
    public Flowable<SyncingNotification> syncingStatusNotifications() {
        return web3jService.subscribe(
                new Request<>(
                        "eth_subscribe",
                        Arrays.asList("syncing"),
                        web3jService,
                        EthSubscribe.class),
                "eth_unsubscribe",
                SyncingNotification.class);
    }
}
