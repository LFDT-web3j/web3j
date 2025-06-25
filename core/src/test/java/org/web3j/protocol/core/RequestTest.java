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

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.RequestTester;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.methods.request.EIP4337Transaction;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.ShhFilter;
import org.web3j.protocol.core.methods.request.ShhPost;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestTest extends RequestTester {

    private Web3j web3j;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        web3j = Web3j.build(httpService);
    }

    @Test
    void testWeb3ClientVersion() throws Exception {
        web3j.web3ClientVersion().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"web3_clientVersion\",\"params\":[],\"id\":1}");
    }

    @Test
    void testWeb3Sha3() throws Exception {
        web3j.web3Sha3("0x68656c6c6f20776f726c64").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"web3_sha3\","
                        + "\"params\":[\"0x68656c6c6f20776f726c64\"],\"id\":1}");
    }

    @Test
    void testNetVersion() throws Exception {
        web3j.netVersion().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"net_version\",\"params\":[],\"id\":1}");
    }

    @Test
    void testNetListening() throws Exception {
        web3j.netListening().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"net_listening\",\"params\":[],\"id\":1}");
    }

    @Test
    void testNetPeerCount() throws Exception {
        web3j.netPeerCount().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"net_peerCount\",\"params\":[],\"id\":1}");
    }

    @Test
    void testAdminNodeInfo() throws Exception {
        web3j.adminNodeInfo().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"admin_nodeInfo\",\"params\":[],\"id\":1}");
    }

    @Test
    void testAdminAddPeer() throws Exception {
        web3j.adminAddPeer("url").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"admin_addPeer\",\"params\":[\"url\"],\"id\":1}");
    }

    @Test
    void testAdminDataDir() throws Exception {
        web3j.adminDataDir().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"admin_datadir\",\"params\":[],\"id\":1}");
    }

    @Test
    void testAdminRemovePeer() throws Exception {
        web3j.adminRemovePeer("url").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"admin_removePeer\",\"params\":[\"url\"],\"id\":1}");
    }

    @Test
    void testEthProtocolVersion() throws Exception {
        web3j.ethProtocolVersion().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_protocolVersion\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthSyncing() throws Exception {
        web3j.ethSyncing().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_syncing\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthCoinbase() throws Exception {
        web3j.ethCoinbase().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_coinbase\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthMining() throws Exception {
        web3j.ethMining().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_mining\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthHashrate() throws Exception {
        web3j.ethHashrate().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_hashrate\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthGasPrice() throws Exception {
        web3j.ethGasPrice().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_gasPrice\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthMaxPriorityFeePerGas() throws Exception {
        web3j.ethMaxPriorityFeePerGas().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_maxPriorityFeePerGas\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthBaseFee() throws Exception {
        web3j.ethBaseFee().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_baseFee\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthFeeHistory() throws Exception {
        web3j.ethFeeHistory(1, DefaultBlockParameterName.LATEST, null).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_feeHistory\",\"params\":[\"0x1\",\"latest\",null],\"id\":1}");
    }

    @Test
    void testEthAccounts() throws Exception {
        web3j.ethAccounts().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_accounts\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthBlockNumber() throws Exception {
        web3j.ethBlockNumber().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_blockNumber\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthGetBalance() throws Exception {
        web3j.ethGetBalance(
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        DefaultBlockParameterName.LATEST)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBalance\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"latest\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetStorageAt() throws Exception {
        web3j.ethGetStorageAt(
                        "0x295a70b2de5e3953354a6a8344e616ed314d7251",
                        BigInteger.ZERO,
                        DefaultBlockParameterName.LATEST)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getStorageAt\","
                        + "\"params\":[\"0x295a70b2de5e3953354a6a8344e616ed314d7251\",\"0x0\",\"latest\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetTransactionCount() throws Exception {
        web3j.ethGetTransactionCount(
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        DefaultBlockParameterName.LATEST)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionCount\","
                        + "\"params\":[\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\"latest\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetBlockTransactionCountByHash() throws Exception {
        web3j.ethGetBlockTransactionCountByHash(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockTransactionCountByHash\",\"params\":[\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],\"id\":1}");
    }

    @Test
    void testEthGetBlockTransactionCountByNumber() throws Exception {
        web3j.ethGetBlockTransactionCountByNumber(
                        DefaultBlockParameter.valueOf(Numeric.toBigInt("0xe8")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockTransactionCountByNumber\","
                        + "\"params\":[\"0xe8\"],\"id\":1}");
    }

    @Test
    void testEthGetUncleCountByBlockHash() throws Exception {
        web3j.ethGetUncleCountByBlockHash(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUncleCountByBlockHash\",\"params\":[\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],\"id\":1}");
    }

    @Test
    void testEthGetUncleCountByBlockNumber() throws Exception {
        web3j.ethGetUncleCountByBlockNumber(DefaultBlockParameter.valueOf(Numeric.toBigInt("0xe8")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUncleCountByBlockNumber\","
                        + "\"params\":[\"0xe8\"],\"id\":1}");
    }

    @Test
    void testEthGetCode() throws Exception {
        web3j.ethGetCode(
                        "0xa94f5374fce5edbc8e2a8697c15331677e6ebf0b",
                        DefaultBlockParameter.valueOf(Numeric.toBigInt("0x2")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getCode\","
                        + "\"params\":[\"0xa94f5374fce5edbc8e2a8697c15331677e6ebf0b\",\"0x2\"],\"id\":1}");
    }

    @Test
    void testEthSign() throws Exception {
        web3j.ethSign(
                        "0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab",
                        "0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sign\","
                        + "\"params\":[\"0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab\","
                        + "\"0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthSendTransaction() throws Exception {
        web3j.ethSendTransaction(
                        new Transaction(
                                "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                                BigInteger.ONE,
                                Numeric.toBigInt("0x9184e72a000"),
                                Numeric.toBigInt("0x76c0"),
                                "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                                Numeric.toBigInt("0x9184e72a"),
                                "0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb"
                                        + "970870f072445675058bb8eb970870f072445675"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\":\"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\"to\":\"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\"gas\":\"0x76c0\",\"gasPrice\":\"0x9184e72a000\",\"value\":\"0x9184e72a\",\"data\":\"0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675\",\"nonce\":\"0x1\"}],\"id\":1}");
    }

    @Test
    void testEthSendRawTransaction() throws Exception {
        web3j.ethSendRawTransaction(
                        "0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f"
                                + "072445675058bb8eb970870f072445675")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendRawTransaction\",\"params\":[\"0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675\"],\"id\":1}");
    }

    @Test
    void testEthCall() throws Exception {
        web3j.ethCall(
                        Transaction.createEthCallTransaction(
                                "0xa70e8dd61c5d32be8058bb8eb970870f07233155",
                                "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                                "0x0"),
                        DefaultBlockParameter.valueOf("latest"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_call\","
                        + "\"params\":[{\"from\":\"0xa70e8dd61c5d32be8058bb8eb970870f07233155\","
                        + "\"to\":\"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\"data\":\"0x0\"},"
                        + "\"latest\"],\"id\":1}");
    }

    @Test
    void testEthEstimateGas() throws Exception {
        web3j.ethEstimateGas(
                        Transaction.createEthCallTransaction(
                                "0xa70e8dd61c5d32be8058bb8eb970870f07233155",
                                "0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f",
                                "0x0"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_estimateGas\","
                        + "\"params\":[{\"from\":\"0xa70e8dd61c5d32be8058bb8eb970870f07233155\","
                        + "\"to\":\"0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f\",\"data\":\"0x0\"}],"
                        + "\"id\":1}");
    }

    @Test
    void testEthSendUserOperation() throws Exception {
        web3j.ethSendUserOperation(
                        EIP4337Transaction.createEthSendUserOperationTransaction(
                                "0xa70e8dd61c5d32be8058bb8eb970870f07233155",
                                BigInteger.ONE,
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0"),
                        "0xa70e8dd61c5d32be8058bb8eb970870f07233156")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendUserOperation\","
                        + "\"params\":[{\"sender\":\"0xa70e8dd61c5d32be8058bb8eb970870f07233155\",\"nonce\":\"0x1\",\"initCode\":\"0x0\",\"callData\":\"0x0\",\"callGasLimit\":\"0x0\",\"verificationGasLimit\":\"0x0\",\"preVerificationGas\":\"0x0\",\"maxFeePerGas\":\"0x0\",\"maxPriorityFeePerGas\":\"0x0\",\"signature\":\"0x0\",\"paymasterAndData\":\"0x0\"},\"0xa70e8dd61c5d32be8058bb8eb970870f07233156\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthEstimateUserOperationGas() throws Exception {
        web3j.ethEstimateUserOperationGas(
                        EIP4337Transaction.createEthEstimateUserOperationGasTransaction(
                                "0xa70e8dd61c5d32be8058bb8eb970870f07233155",
                                BigInteger.ONE,
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0",
                                "0x0"),
                        "0xa70e8dd61c5d32be8058bb8eb970870f07233156")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_estimateUserOperationGas\","
                        + "\"params\":[{\"sender\":\"0xa70e8dd61c5d32be8058bb8eb970870f07233155\",\"nonce\":\"0x1\",\"initCode\":\"0x0\",\"callData\":\"0x0\",\"callGasLimit\":\"0x0\",\"verificationGasLimit\":\"0x0\",\"preVerificationGas\":\"0x0\",\"maxFeePerGas\":\"0x0\",\"maxPriorityFeePerGas\":\"0x0\",\"signature\":\"0x0\",\"paymasterAndData\":\"0x0\"},\"0xa70e8dd61c5d32be8058bb8eb970870f07233156\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetUserOperationByHash() throws Exception {
        web3j.ethGetUserOperationByHash("0xa70e8dd61c5d32be8058bb8eb970870f07233156").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUserOperationByHash\","
                        + "\"params\":[\"0xa70e8dd61c5d32be8058bb8eb970870f07233156\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetUserOperationReceipt() throws Exception {
        web3j.ethGetUserOperationReceipt("0xa70e8dd61c5d32be8058bb8eb970870f07233156").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUserOperationReceipt\","
                        + "\"params\":[\"0xa70e8dd61c5d32be8058bb8eb970870f07233156\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthSupportedEntryPoints() throws Exception {
        web3j.ethSupportedEntryPoints().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_supportedEntryPoints\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthEstimateGasContractCreation() throws Exception {
        web3j.ethEstimateGas(
                        Transaction.createContractTransaction(
                                "0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f",
                                BigInteger.ONE,
                                BigInteger.TEN,
                                ""))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_estimateGas\","
                        + "\"params\":[{\"from\":\"0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f\","
                        + "\"gasPrice\":\"0xa\",\"data\":\"0x\",\"nonce\":\"0x1\"}],\"id\":1}");
    }

    @Test
    void testEthGetBlockByHash() throws Exception {
        web3j.ethGetBlockByHash(
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331", true)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockByHash\",\"params\":["
                        + "\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\""
                        + ",true],\"id\":1}");
    }

    @Test
    void testEthGetBlockByNumber() throws Exception {
        web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(Numeric.toBigInt("0x1b4")), true)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockByNumber\","
                        + "\"params\":[\"0x1b4\",true],\"id\":1}");
    }

    @Test
    void testEthGetTransactionByHash() throws Exception {
        web3j.ethGetTransactionByHash(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionByHash\",\"params\":["
                        + "\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetTransactionByBlockHashAndIndex() throws Exception {
        web3j.ethGetTransactionByBlockHashAndIndex(
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                        BigInteger.ZERO)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionByBlockHashAndIndex\",\"params\":[\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\"0x0\"],\"id\":1}");
    }

    @Test
    void testEthGetTransactionByBlockNumberAndIndex() throws Exception {
        web3j.ethGetTransactionByBlockNumberAndIndex(
                        DefaultBlockParameter.valueOf(Numeric.toBigInt("0x29c")), BigInteger.ZERO)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionByBlockNumberAndIndex\","
                        + "\"params\":[\"0x29c\",\"0x0\"],\"id\":1}");
    }

    @Test
    void testEthGetTransactionReceipt() throws Exception {
        web3j.ethGetTransactionReceipt(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionReceipt\",\"params\":["
                        + "\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetBlockReceipts() throws Exception {
        web3j.ethGetBlockReceipts(DefaultBlockParameter.valueOf(BigInteger.valueOf(15455945)))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockReceipts\",\"params\":["
                        + "\"0xebd6c9\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetUncleByBlockHashAndIndex() throws Exception {
        web3j.ethGetUncleByBlockHashAndIndex(
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        BigInteger.ZERO)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUncleByBlockHashAndIndex\","
                        + "\"params\":["
                        + "\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\"0x0\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetUncleByBlockNumberAndIndex() throws Exception {
        web3j.ethGetUncleByBlockNumberAndIndex(
                        DefaultBlockParameter.valueOf(Numeric.toBigInt("0x29c")), BigInteger.ZERO)
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getUncleByBlockNumberAndIndex\","
                        + "\"params\":[\"0x29c\",\"0x0\"],\"id\":1}");
    }

    @Test
    void testEthGetCompilers() throws Exception {
        web3j.ethGetCompilers().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getCompilers\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    void testEthCompileSolidity() throws Exception {
        web3j.ethCompileSolidity(
                        "contract test { function multiply(uint a) returns(uint d) {   return a * 7;   } }")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_compileSolidity\","
                        + "\"params\":[\"contract test { function multiply(uint a) returns(uint d) {"
                        + "   return a * 7;   } }\"],\"id\":1}");
    }

    @Test
    void testEthCompileLLL() throws Exception {
        web3j.ethCompileLLL("(returnlll (suicide (caller)))").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_compileLLL\","
                        + "\"params\":[\"(returnlll (suicide (caller)))\"],\"id\":1}");
    }

    @Test
    void testEthCompileSerpent() throws Exception {
        web3j.ethCompileSerpent("/* some serpent */").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_compileSerpent\","
                        + "\"params\":[\"/* some serpent */\"],\"id\":1}");
    }

    @Test
    void testEthNewFilter() throws Exception {
        EthFilter ethFilter = new EthFilter().addSingleTopic("0x12341234");

        web3j.ethNewFilter(ethFilter).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_newFilter\","
                        + "\"params\":[{\"topics\":[\"0x12341234\"]}],\"id\":1}");
    }

    @Test
    void testEthNewBlockFilter() throws Exception {
        web3j.ethNewBlockFilter().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_newBlockFilter\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    void testEthNewPendingTransactionFilter() throws Exception {
        web3j.ethNewPendingTransactionFilter().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_newPendingTransactionFilter\","
                        + "\"params\":[],\"id\":1}");
    }

    @Test
    void testEthUninstallFilter() throws Exception {
        web3j.ethUninstallFilter(Numeric.toBigInt("0xb")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_uninstallFilter\","
                        + "\"params\":[\"0xb\"],\"id\":1}");
    }

    @Test
    void testEthGetFilterChanges() throws Exception {
        web3j.ethGetFilterChanges(Numeric.toBigInt("0x16")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getFilterChanges\","
                        + "\"params\":[\"0x16\"],\"id\":1}");
    }

    @Test
    void testEthGetFilterLogs() throws Exception {
        web3j.ethGetFilterLogs(Numeric.toBigInt("0x16")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getFilterLogs\","
                        + "\"params\":[\"0x16\"],\"id\":1}");
    }

    @Test
    void testEthGetLogs() throws Exception {
        web3j.ethGetLogs(
                        new EthFilter()
                                .addSingleTopic(
                                        "0x000000000000000000000000a94f5374fce5edbc8e2a8697c15331677e6ebf0b"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getLogs\","
                        + "\"params\":[{\"topics\":["
                        + "\"0x000000000000000000000000a94f5374fce5edbc8e2a8697c15331677e6ebf0b\"]}],"
                        + "\"id\":1}");
    }

    @Test
    void testEthGetLogsWithNumericBlockRange() throws Exception {
        web3j.ethGetLogs(
                        new EthFilter(
                                DefaultBlockParameter.valueOf(Numeric.toBigInt("0xe8")),
                                DefaultBlockParameter.valueOf("latest"),
                                ""))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getLogs\","
                        + "\"params\":[{\"topics\":[],\"fromBlock\":\"0xe8\","
                        + "\"toBlock\":\"latest\",\"address\":[\"\"]}],\"id\":1}");
    }

    @Test
    void testEthGetLogsWithBlockHash() throws Exception {
        web3j.ethGetLogs(
                        new EthFilter(
                                "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                                ""))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getLogs\","
                        + "\"params\":[{\"topics\":[],"
                        + "\"blockHash\":\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\","
                        + "\"address\":[\"\"]}],\"id\":<generatedValue>}");
    }

    @Test
    void testEthGetProof() throws Exception {
        web3j.ethGetProof(
                        "0x7F0d15C7FAae65896648C8273B6d7E43f58Fa842",
                        Arrays.asList(
                                "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"),
                        "latest")
                .send();
        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getProof\","
                        + "\"params\":[\"0x7F0d15C7FAae65896648C8273B6d7E43f58Fa842\","
                        + "[\"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\"],"
                        + "\"latest\"],"
                        + "\"id\":0}");
    }

    @Test
    void testEthGetWork() throws Exception {
        web3j.ethGetWork().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"eth_getWork\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthSubmitWork() throws Exception {
        web3j.ethSubmitWork(
                        "0x0000000000000001",
                        "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
                        "0xD1FE5700000000000000000000000000D1FE5700000000000000000000000000")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_submitWork\","
                        + "\"params\":[\"0x0000000000000001\","
                        + "\"0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef\","
                        + "\"0xD1FE5700000000000000000000000000D1FE5700000000000000000000000000\"],"
                        + "\"id\":1}");
    }

    @Test
    void testEthSubmitHashRate() throws Exception {
        web3j.ethSubmitHashrate(
                        "0x0000000000000000000000000000000000000000000000000000000000500000",
                        "0x59daa26581d0acd1fce254fb7e85952f4c09d0915afd33d3886cd914bc7d283c")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_submitHashrate\","
                        + "\"params\":["
                        + "\"0x0000000000000000000000000000000000000000000000000000000000500000\","
                        + "\"0x59daa26581d0acd1fce254fb7e85952f4c09d0915afd33d3886cd914bc7d283c\"],"
                        + "\"id\":1}");
    }

    @Test
    void testDbPutString() throws Exception {
        web3j.dbPutString("testDB", "myKey", "myString").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"db_putString\","
                        + "\"params\":[\"testDB\",\"myKey\",\"myString\"],\"id\":1}");
    }

    @Test
    void testDbGetString() throws Exception {
        web3j.dbGetString("testDB", "myKey").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"db_getString\","
                        + "\"params\":[\"testDB\",\"myKey\"],\"id\":1}");
    }

    @Test
    void testDbPutHex() throws Exception {
        web3j.dbPutHex("testDB", "myKey", "0x68656c6c6f20776f726c64").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"db_putHex\","
                        + "\"params\":[\"testDB\",\"myKey\",\"0x68656c6c6f20776f726c64\"],\"id\":1}");
    }

    @Test
    void testDbGetHex() throws Exception {
        web3j.dbGetHex("testDB", "myKey").send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"db_getHex\","
                        + "\"params\":[\"testDB\",\"myKey\"],\"id\":1}");
    }

    @Test
    void testShhVersion() throws Exception {
        web3j.shhVersion().send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_version\"," + "\"params\":[],\"id\":1}");
    }

    @Test
    void testShhPost() throws Exception {

        web3j.shhPost(
                        new ShhPost(
                                "0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1",
                                "0x3e245533f97284d442460f2998cd41858798ddf04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a0d4d661997d3940272b717b1",
                                Arrays.asList(
                                        "0x776869737065722d636861742d636c69656e74",
                                        "0x4d5a695276454c39425154466b61693532"),
                                "0x7b2274797065223a226d6",
                                Numeric.toBigInt("0x64"),
                                Numeric.toBigInt("0x64")))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_post\",\"params\":[{\"from\":\"0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1\",\"to\":\"0x3e245533f97284d442460f2998cd41858798ddf04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a0d4d661997d3940272b717b1\",\"topics\":[\"0x776869737065722d636861742d636c69656e74\",\"0x4d5a695276454c39425154466b61693532\"],\"payload\":\"0x7b2274797065223a226d6\",\"priority\":\"0x64\",\"ttl\":\"0x64\"}],\"id\":1}");
    }

    @Test
    void testShhNewIdentity() throws Exception {
        web3j.shhNewIdentity().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"shh_newIdentity\",\"params\":[],\"id\":1}");
    }

    @Test
    void testShhHasIdentity() throws Exception {

        web3j.shhHasIdentity(
                        "0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_hasIdentity\",\"params\":[\"0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1\"],\"id\":1}");
    }

    @Test
    void testShhNewGroup() throws Exception {
        web3j.shhNewGroup().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"shh_newGroup\",\"params\":[],\"id\":1}");
    }

    @Test
    void testShhAddToGroup() throws Exception {

        web3j.shhAddToGroup(
                        "0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_addToGroup\",\"params\":[\"0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1\"],\"id\":1}");
    }

    @Test
    void testShhNewFilter() throws Exception {

        web3j.shhNewFilter(
                        new ShhFilter(
                                        "0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1")
                                .addSingleTopic("0x12341234bf4b564f"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_newFilter\",\"params\":[{\"topics\":[\"0x12341234bf4b564f\"],\"to\":\"0x04f96a5e25610293e42a73908e93ccc8c4d4dc0edcfa9fa872f50cb214e08ebf61a03e245533f97284d442460f2998cd41858798ddfd4d661997d3940272b717b1\"}],\"id\":1}");
    }

    @Test
    void testShhUninstallFilter() throws Exception {
        web3j.shhUninstallFilter(Numeric.toBigInt("0x7")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_uninstallFilter\","
                        + "\"params\":[\"0x7\"],\"id\":1}");
    }

    @Test
    void testShhGetFilterChanges() throws Exception {
        web3j.shhGetFilterChanges(Numeric.toBigInt("0x7")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_getFilterChanges\","
                        + "\"params\":[\"0x7\"],\"id\":1}");
    }

    @Test
    void testShhGetMessages() throws Exception {
        web3j.shhGetMessages(Numeric.toBigInt("0x7")).send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"shh_getMessages\","
                        + "\"params\":[\"0x7\"],\"id\":1}");
    }

    @Test
    void testTxPoolStatus() throws Exception {
        web3j.txPoolStatus().send();

        verifyResult("{\"jsonrpc\":\"2.0\",\"method\":\"txpool_status\",\"params\":[],\"id\":1}");
    }

    @Test
    void testEthGetBaseFeePerBlobGas() throws Exception {
        // mock Web3jService
        Web3jService web3jService = mock(Web3jService.class);
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        when(web3jService.send(any(), eq(EthBlock.class))).thenReturn(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        when(block.getExcessBlobGas()).thenReturn(BigInteger.ZERO);

        JsonRpc2_0Web3j jsonRpc20Web3j = new JsonRpc2_0Web3j(web3jService);

        BigInteger resultWhenExcessBlobGasIsZero = jsonRpc20Web3j.ethGetBaseFeePerBlobGas();
        assertEquals(
                BigInteger.ONE,
                resultWhenExcessBlobGasIsZero); // Expected result based on your fakeExponential
        // logic and input

        when(block.getExcessBlobGas()).thenReturn(BigInteger.valueOf(79429632L));
        BigInteger resultWhenExcessBlobGasIsNotZero = jsonRpc20Web3j.ethGetBaseFeePerBlobGas();
        assertEquals(BigInteger.valueOf(21518435987L), resultWhenExcessBlobGasIsNotZero);
    }

    @Test
    void testLineaEstimateGas() throws Exception {
        web3j.lineaEstimateGas(
                        Transaction.createEthCallTransaction(
                                "0xa70e8dd61c5d32be8058bb8eb970870f07233155",
                                "0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f",
                                "0x0"))
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"linea_estimateGas\","
                        + "\"params\":[{\"from\":\"0xa70e8dd61c5d32be8058bb8eb970870f07233155\","
                        + "\"to\":\"0x52b93c80364dc2dd4444c146d73b9836bbbb2b3f\",\"data\":\"0x0\"}],"
                        + "\"id\":1}");
    }

    @Test
    void testLineaGetProof() throws Exception {
        web3j.lineaGetProof(
                        "0x7F0d15C7FAae65896648C8273B6d7E43f58Fa842",
                        Arrays.asList(
                                "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"),
                        "latest")
                .send();
        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"linea_getProof\","
                        + "\"params\":[\"0x7F0d15C7FAae65896648C8273B6d7E43f58Fa842\","
                        + "[\"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\"],"
                        + "\"latest\"],"
                        + "\"id\":0}");
    }

    @Test
    void testLineaGetTransactionExclusionStatusV1() throws Exception {
        web3j.lineaGetTransactionExclusionStatusV1(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238")
                .send();

        verifyResult(
                "{\"jsonrpc\":\"2.0\",\"method\":\"linea_getTransactionExclusionStatusV1\",\"params\":["
                        + "\"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\"],"
                        + "\"id\":1}");
    }
}
