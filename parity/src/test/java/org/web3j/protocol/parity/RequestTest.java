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
package org.web3j.protocol.parity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.web3j.crypto.WalletFile;
import org.web3j.protocol.RequestTester;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.methods.request.Derivation;
import org.web3j.protocol.parity.methods.request.TraceFilter;
import org.web3j.utils.Numeric;

public class RequestTest extends RequestTester {

    private Parity web3j;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        web3j = Parity.build(httpService);
    }

    @Test
    public void testParityAllAccountsInfo() throws Exception {
        web3j.parityAllAccountsInfo().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_allAccountsInfo\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testParityChangePassword() throws Exception {

        web3j.parityChangePassword(
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1", "hunter2", "bazqux5")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_changePassword\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"hunter2\",\"bazqux5\"],\"id\":1}");
    }

    @Test
    public void testParityDeriveAddressHash() throws Exception {
        Derivation hashType =
                Derivation.createDerivationHash(
                        "0x2547ea3382099c7c76d33dd468063b32d41016aacb02cbd51ebc14ff5d2b6a43",
                        "hard");

        web3j.parityDeriveAddressHash(
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1", "hunter2", hashType, false)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_deriveAddressHash\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"hunter2\","
                        + "{\"hash\":\"0x2547ea3382099c7c76d33dd468063b32d41016aacb02cbd51ebc14ff5d2b6a43\",\"type\":\"hard\"},false],\"id\":1}");
    }

    @Test
    public void testParityDeriveAddressIndex() throws Exception {
        List<Derivation> indexType = new ArrayList<>();
        indexType.add(Derivation.createDerivationIndex(1, "hard"));
        indexType.add(Derivation.createDerivationIndex(2, "soft"));

        web3j.parityDeriveAddressIndex(
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1", "hunter2", indexType, false)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_deriveAddressIndex\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"hunter2\","
                        + "[{\"index\":1,\"type\":\"hard\"},{\"index\":2,\"type\":\"soft\"}],false],\"id\":1}");
    }

    @Test
    public void testParityExportAccount() throws Exception {
        web3j.parityExportAccount("0x407d73d8a49eeb85d32cf465507dd71d507100c1", "hunter2").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_exportAccount\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"hunter2\"],\"id\":1}");
    }

    @Test
    public void testParityGetDappAddresses() throws Exception {
        web3j.parityGetDappAddresses("web").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_getDappAddresses\","
                        + "\"params\":[\"web\"],\"id\":1}");
    }

    @Test
    public void testParityGetDefaultDappAddress() throws Exception {
        web3j.parityGetDappDefaultAddress("web").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_getDappDefaultAddress\","
                        + "\"params\":[\"web\"],\"id\":1}");
    }

    public void testParityGetNewDappsAddresses() throws Exception {
        web3j.parityAllAccountsInfo().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_getNewDappsAddresses\","
                        + "\"params\":[],\"id\":1}");
    }

    public void testParityGetNewDappsDefaultAddress() throws Exception {
        web3j.parityGetNewDappsDefaultAddress().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_getNewDappsDefaultAddress\","
                        + "\"params\":[],\"id\":1}");
    }

    public void testParityImportGethAccounts() throws Exception {
        ArrayList<String> gethAccounts = new ArrayList<>();
        gethAccounts.add("0x407d73d8a49eeb85d32cf465507dd71d507100c1");
        web3j.parityImportGethAccounts(gethAccounts).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_importGethAccounts\","
                        + "\"params\":[[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"]],\"id\":1}");
    }

    public void testParityKillAccount() throws Exception {
        web3j.parityKillAccount("0x407d73d8a49eeb85d32cf465507dd71d507100c1", "hunter2").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_killAccount\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"hunter2\"],\"id\":1}");
    }

    @Test
    public void testParityListAccountsNoAccountOffsetNoBlockTag() throws Exception {
        BigInteger maxQuantityReturned = BigInteger.valueOf(100);
        web3j.parityListAccounts(maxQuantityReturned, null, null).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_listAccounts\","
                        + "\"params\":[100,null],\"id\":1}");
    }

    @Test
    public void testParityListAccountsNoAccountOffsetWithBlockTag() throws Exception {
        BigInteger maxQuantityReturned = BigInteger.valueOf(100);
        DefaultBlockParameter blockParameter = new DefaultBlockParameterNumber(BigInteger.ONE);
        web3j.parityListAccounts(maxQuantityReturned, null, blockParameter).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_listAccounts\","
                        + "\"params\":[100,null,\"0x1\"],\"id\":1}");
    }

    @Test
    public void testParityListAccountsWithAccountOffsetWithBlockTag() throws Exception {
        BigInteger maxQuantityReturned = BigInteger.valueOf(100);
        DefaultBlockParameter blockParameter = DefaultBlockParameterName.LATEST;
        web3j.parityListAccounts(
                        maxQuantityReturned,
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        blockParameter)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_listAccounts\","
                        + "\"params\":[100,\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"latest\"],\"id\":1}");
    }

    @Test
    public void testParityListAccountsWithAccountOffsetNoBlockTag() throws Exception {
        BigInteger maxQuantityReturned = BigInteger.valueOf(100);
        web3j.parityListAccounts(
                        maxQuantityReturned, "0x407d73d8a49eeb85d32cf465507dd71d507100c1", null)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_listAccounts\","
                        + "\"params\":[100,\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"],\"id\":1}");
    }

    public void testParityListGethAccounts() throws Exception {
        web3j.parityListGethAccounts().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_listGethAccounts\","
                        + "\"params\":[],\"id\":1}");
    }

    public void testParityListRecentDapps() throws Exception {
        web3j.parityListRecentDapps().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_listRecentDapps\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    public void testParityNewAccountFromPhrase() throws Exception {
        web3j.parityNewAccountFromPhrase("phrase", "password").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_newAccountFromPhrase\","
                        + "\"params\":[\"phrase\",\"password\"],\"id\":1}");
    }

    @Test
    public void testParityNewAccountFromSecret() throws Exception {

        web3j.parityNewAccountFromSecret(
                        "0x1db2c0cf57505d0f4a3d589414f0a0025ca97421d2cd596a9486bc7e2cd2bf8b",
                        "password")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_newAccountFromSecret\","
                        + "\"params\":[\"0x1db2c0cf57505d0f4a3d589414f0a0025ca97421d2cd596a9486bc7e2cd2bf8b\",\"password\"],\"id\":1}");
    }

    @Test
    public void testParityNewAccountFromWallet() throws Exception {
        WalletFile walletFile = new WalletFile();
        walletFile.setAddress("0x...");

        WalletFile.Crypto crypto = new WalletFile.Crypto();
        crypto.setCipher("CIPHER");
        crypto.setCiphertext("CIPHERTEXT");
        walletFile.setCrypto(crypto);

        WalletFile.CipherParams cipherParams = new WalletFile.CipherParams();
        cipherParams.setIv("IV");
        crypto.setCipherparams(cipherParams);

        crypto.setKdf("KDF");
        WalletFile.ScryptKdfParams kdfParams = new WalletFile.ScryptKdfParams();
        kdfParams.setDklen(32);
        kdfParams.setN(1);
        kdfParams.setP(10);
        kdfParams.setR(100);
        kdfParams.setSalt("SALT");
        crypto.setKdfparams(kdfParams);

        crypto.setMac("MAC");
        walletFile.setCrypto(crypto);
        walletFile.setId("cab06c9e-79a9-48ea-afc7-d3bdb3a59526");
        walletFile.setVersion(1);

        web3j.parityNewAccountFromWallet(walletFile, "password").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_newAccountFromWallet\",\"params\":[{"
                        + "\"address\":\"0x...\","
                        + "\"crypto\":{"
                        + "\"cipher\":\"CIPHER\","
                        + "\"cipherparams\":{\"iv\":\"IV\"},"
                        + "\"ciphertext\":\"CIPHERTEXT\","
                        + "\"kdf\":\"KDF\","
                        + "\"kdfparams\":{\"dklen\":32,\"n\":1,\"p\":10,\"r\":100,\"salt\":\"SALT\"},"
                        + "\"mac\":\"MAC\""
                        + "},"
                        + "\"id\":\"cab06c9e-79a9-48ea-afc7-d3bdb3a59526\","
                        + "\"version\":1"
                        + "},"
                        + "\"password\"],"
                        + "\"id\":1}");
    }

    public void testParityRemoveAddress() throws Exception {
        web3j.parityRemoveAddress("0x407d73d8a49eeb85d32cf465507dd71d507100c1").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_removeAddress\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"],\"id\":1}");
    }

    @Test
    public void testParitySetAccountMeta() throws Exception {
        Map<String, Object> meta = new HashMap<>(1);
        meta.put("foo", "bar");
        web3j.paritySetAccountMeta("0xfc390d8a8ddb591b010fda52f4db4945742c3809", meta).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_setAccountMeta\","
                        + "\"params\":[\"0xfc390d8a8ddb591b010fda52f4db4945742c3809\",{\"foo\":\"bar\"}],"
                        + "\"id\":1}");
    }

    @Test
    public void testParitySetAccountName() throws Exception {
        web3j.paritySetAccountName("0xfc390d8a8ddb591b010fda52f4db4945742c3809", "Savings").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_setAccountName\","
                        + "\"params\":[\"0xfc390d8a8ddb591b010fda52f4db4945742c3809\",\"Savings\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testParitySetDappAddresses() throws Exception {
        ArrayList<String> dAppAddresses = new ArrayList<>();
        dAppAddresses.add("0x407d73d8a49eeb85d32cf465507dd71d507100c1");
        web3j.paritySetDappAddresses("web", dAppAddresses).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_setDappAddresses\","
                        + "\"params\":[\"web\",[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"]],"
                        + "\"id\":1}");
    }

    @Test
    public void testParitySetDappDefaultAddress() throws Exception {
        web3j.paritySetDappDefaultAddress("web", "0x407d73d8a49eeb85d32cf465507dd71d507100c1")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_setDappDefaultAddress\","
                        + "\"params\":[\"web\",\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testParitySetNewDappsAddresses() throws Exception {
        ArrayList<String> dAppAddresses = new ArrayList<>();
        dAppAddresses.add("0x407d73d8a49eeb85d32cf465507dd71d507100c1");
        web3j.paritySetNewDappsAddresses(dAppAddresses).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_setNewDappsAddresses\","
                        + "\"params\":[[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"]],"
                        + "\"id\":1}");
    }

    @Test
    public void testParitySetNewDappsDefaultAddress() throws Exception {
        web3j.paritySetNewDappsDefaultAddress("0x407d73d8a49eeb85d32cf465507dd71d507100c1").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_setNewDappsDefaultAddress\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testParityTestPassword() throws Exception {
        web3j.parityTestPassword("0x407d73d8a49eeb85d32cf465507dd71d507100c1", "hunter2").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_testPassword\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"hunter2\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testParitySignMessage() throws Exception {

        web3j.paritySignMessage(
                        "0xc171033d5cbff7175f29dfd3a63dda3d6f8f385e",
                        "password1",
                        "0xbc36789e7a1e281436464229828f817d6612f7b477d66591ff96a9e064bcc98a")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"parity_signMessage\","
                        + "\"params\":[\"0xc171033d5cbff7175f29dfd3a63dda3d6f8f385e\",\"password1\","
                        + "\"0xbc36789e7a1e281436464229828f817d6612f7b477d66591ff96a9e064bcc98a\"],\"id\":1}");
    }

    @Test
    public void testTraceCall() throws Exception {
        Transaction transaction =
                Transaction.createFunctionCallTransaction(
                        "0xc171033d5cbff7175f29dfd3a63dda3d6f8f385e",
                        BigInteger.ONE,
                        Numeric.toBigInt("0x9184e72a000"),
                        Numeric.toBigInt("0x76c0"),
                        "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                        Numeric.toBigInt("0x9184e72a"),
                        "0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb"
                                + "970870f072445675058bb8eb970870f072445675");
        web3j.traceCall(
                        transaction,
                        Arrays.asList("trace", "vmTrace", "stateDiff"),
                        DefaultBlockParameterName.LATEST)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"trace_call\","
                        + "\"params\":["
                        + "{\"from\":\"0xc171033d5cbff7175f29dfd3a63dda3d6f8f385e\","
                        + "\"to\":\"0xb60e8dd61c5d32be8058bb8eb970870f07233155\","
                        + "\"gas\":\"0x76c0\","
                        + "\"gasPrice\":\"0x9184e72a000\","
                        + "\"value\":\"0x9184e72a\","
                        + "\"data\":\"0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675\","
                        + "\"nonce\":\"0x1\"},"
                        + "[\"trace\",\"vmTrace\",\"stateDiff\"],"
                        + "\"latest\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testTraceRawTransaction() throws Exception {

        web3j.traceRawTransaction(
                        "0xf869808504e3b292008305499d94781ab1a38837e351bfe1e318c6587766848abffa8084b46300ec26a0b1ffd8f843e08a9dbf0a42b3c7dd5288a48885cd6e3bcdb2609e943d0b0053d4a07bfdb3c12a7cec896bfc2cfc7c346a2cb411e1aca62ad085e8d7abbb6532e128",
                        Arrays.asList("trace", "vmTrace", "stateDiff"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"trace_rawTransaction\","
                        + "\"params\":["
                        + "\"0xf869808504e3b292008305499d94781ab1a38837e351bfe1e318c6587766848abffa8084b46300ec26a0b1ffd8f843e08a9dbf0a42b3c7"
                        + "dd5288a48885cd6e3bcdb2609e943d0b0053d4a07bfdb3c12a7cec896bfc2cfc7c346a2cb411e1aca62ad085e8d7abbb6532e128\","
                        + "[\"trace\",\"vmTrace\",\"stateDiff\"]],"
                        + "\"id\":1}");
    }

    @Test
    public void testTraceReplayTransaction() throws Exception {
        web3j.traceReplayTransaction(
                        "0x090a4bbdeb57f15fe252cccc924255855eda45a2d8f65b12ec81f03e2cc33249",
                        Arrays.asList("trace", "vmTrace", "stateDiff"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"trace_replayTransaction\","
                        + "\"params\":["
                        + "\"0x090a4bbdeb57f15fe252cccc924255855eda45a2d8f65b12ec81f03e2cc33249\","
                        + "[\"trace\",\"vmTrace\",\"stateDiff\"]],"
                        + "\"id\":1}");
    }

    @Test
    public void testTraceBlock() throws Exception {
        web3j.traceBlock(DefaultBlockParameterName.LATEST).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"trace_block\","
                        + "\"params\":[\"latest\"],"
                        + "\"id\":1}");
    }

    @Test
    public void testTraceFilter() throws Exception {
        web3j.traceFilter(
                        new TraceFilter(
                                DefaultBlockParameterName.EARLIEST,
                                DefaultBlockParameterName.LATEST,
                                Collections.singletonList(
                                        "0xa9bebd4853ce06c3dc1b711bbafa1514ed5b5130"),
                                Collections.singletonList(
                                        "0xB4d9b203d8D16f41916a62DEab83389cF2b7eeCb")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"trace_filter\","
                        + "\"params\":[{"
                        + "\"fromAddress\":[\"0xa9bebd4853ce06c3dc1b711bbafa1514ed5b5130\"],"
                        + "\"fromBlock\":\"earliest\","
                        + "\"toAddress\":[\"0xB4d9b203d8D16f41916a62DEab83389cF2b7eeCb\"],"
                        + "\"toBlock\":\"latest\""
                        + "}],"
                        + "\"id\":1}");
    }

    @Test
    public void testTraceGet() throws Exception {
        web3j.traceGet(
                        "0x090a4bbdeb57f15fe252cccc924255855eda45a2d8f65b12ec81f03e2cc33249",
                        Arrays.asList(BigInteger.valueOf(2), BigInteger.ZERO, BigInteger.ZERO))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"trace_get\","
                        + "\"params\":["
                        + "\"0x090a4bbdeb57f15fe252cccc924255855eda45a2d8f65b12ec81f03e2cc33249\","
                        + "[\"0x2\",\"0x0\",\"0x0\"]],"
                        + "\"id\":1}");
    }
}
