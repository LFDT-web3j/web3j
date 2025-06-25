/*
 * Copyright 2021 Web3 Labs Ltd.
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.ResponseTester;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.AccessListObject;
import org.web3j.protocol.core.methods.response.BooleanResponse;
import org.web3j.protocol.core.methods.response.DbGetHex;
import org.web3j.protocol.core.methods.response.DbGetString;
import org.web3j.protocol.core.methods.response.DbPutHex;
import org.web3j.protocol.core.methods.response.DbPutString;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthBaseFee;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthCompileLLL;
import org.web3j.protocol.core.methods.response.EthCompileSerpent;
import org.web3j.protocol.core.methods.response.EthCompileSolidity;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthEstimateUserOperationGas;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetBlockReceipts;
import org.web3j.protocol.core.methods.response.EthGetBlockTransactionCountByHash;
import org.web3j.protocol.core.methods.response.EthGetBlockTransactionCountByNumber;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthGetCompilers;
import org.web3j.protocol.core.methods.response.EthGetProof;
import org.web3j.protocol.core.methods.response.EthGetStorageAt;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthGetUncleCountByBlockHash;
import org.web3j.protocol.core.methods.response.EthGetUncleCountByBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetUserOperationByHash;
import org.web3j.protocol.core.methods.response.EthGetUserOperationReceipt;
import org.web3j.protocol.core.methods.response.EthGetWork;
import org.web3j.protocol.core.methods.response.EthHashrate;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthMining;
import org.web3j.protocol.core.methods.response.EthProtocolVersion;
import org.web3j.protocol.core.methods.response.EthSendRawTransaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthSendUserOperation;
import org.web3j.protocol.core.methods.response.EthSign;
import org.web3j.protocol.core.methods.response.EthSubmitHashrate;
import org.web3j.protocol.core.methods.response.EthSubmitWork;
import org.web3j.protocol.core.methods.response.EthSupportedEntryPoints;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.EthUninstallFilter;
import org.web3j.protocol.core.methods.response.LineaEstimateGas;
import org.web3j.protocol.core.methods.response.LineaGetProof;
import org.web3j.protocol.core.methods.response.LineaGetTransactionExclusionStatusV1;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.NetListening;
import org.web3j.protocol.core.methods.response.NetPeerCount;
import org.web3j.protocol.core.methods.response.NetVersion;
import org.web3j.protocol.core.methods.response.ShhAddToGroup;
import org.web3j.protocol.core.methods.response.ShhHasIdentity;
import org.web3j.protocol.core.methods.response.ShhMessages;
import org.web3j.protocol.core.methods.response.ShhNewFilter;
import org.web3j.protocol.core.methods.response.ShhNewGroup;
import org.web3j.protocol.core.methods.response.ShhNewIdentity;
import org.web3j.protocol.core.methods.response.ShhPost;
import org.web3j.protocol.core.methods.response.ShhUninstallFilter;
import org.web3j.protocol.core.methods.response.ShhVersion;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.TxPoolStatus;
import org.web3j.protocol.core.methods.response.UserOperation;
import org.web3j.protocol.core.methods.response.UserOperationReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.core.methods.response.Web3Sha3;
import org.web3j.protocol.core.methods.response.admin.AdminDataDir;
import org.web3j.protocol.core.methods.response.admin.AdminNodeInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Core Protocol Response tests. */
class ResponseTest extends ResponseTester {

    @Test
    void testErrorResponse() {
        buildResponse(
                "{"
                        + "  \"jsonrpc\":\"2.0\","
                        + "  \"id\":1,"
                        + "  \"error\":{"
                        + "    \"code\":-32602,"
                        + "    \"message\":\"Invalid address length, expected 40 got 64 bytes\","
                        + "    \"data\":null"
                        + "  }"
                        + "}");

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        assertTrue(ethBlock.hasError());
        assertEquals(
                ethBlock.getError(),
                (new Response.Error(-32602, "Invalid address length, expected 40 got 64 bytes")));
    }

    @Test
    void testErrorResponseComplexData() {
        buildResponse(
                "{"
                        + "  \"jsonrpc\":\"2.0\","
                        + "  \"id\":1,"
                        + "  \"error\":{"
                        + "    \"code\":-32602,"
                        + "    \"message\":\"Invalid address length, expected 40 got 64 bytes\","
                        + "    \"data\":{\"foo\":\"bar\"}"
                        + "  }"
                        + "}");

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        assertTrue(ethBlock.hasError());
        assertEquals(ethBlock.getError().getData(), ("{\"foo\":\"bar\"}"));
    }

    @Test
    void testWeb3ClientVersion() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"Mist/v0.9.3/darwin/go1.4.1\"\n"
                        + "}");

        Web3ClientVersion web3ClientVersion = deserialiseResponse(Web3ClientVersion.class);
        assertEquals(web3ClientVersion.getWeb3ClientVersion(), ("Mist/v0.9.3/darwin/go1.4.1"));
    }

    @Test
    void testWeb3Sha3() throws IOException {
        buildResponse(
                "{\n"
                        + "  \"id\":64,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad\"\n"
                        + "}");

        Web3Sha3 web3Sha3 = deserialiseResponse(Web3Sha3.class);
        assertEquals(
                web3Sha3.getResult(),
                ("0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad"));
    }

    @Test
    void testNetVersion() throws IOException {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"59\"\n"
                        + "}");

        NetVersion netVersion = deserialiseResponse(NetVersion.class);
        assertEquals(netVersion.getNetVersion(), ("59"));
    }

    @Test
    void testNetListening() throws IOException {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\":true\n"
                        + "}");

        NetListening netListening = deserialiseResponse(NetListening.class);
        assertEquals(netListening.isListening(), (true));
    }

    @Test
    void testNetPeerCount() throws IOException {
        buildResponse(
                "{\n"
                        + "  \"id\":74,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x2\"\n"
                        + "}");

        NetPeerCount netPeerCount = deserialiseResponse(NetPeerCount.class);
        assertEquals(netPeerCount.getQuantity(), (BigInteger.valueOf(2L)));
    }

    @Test
    void testAdminNodeInfo() throws Exception {
        buildResponse(
                "{\n"
                        + "    \"jsonrpc\": \"2.0\",\n"
                        + "    \"id\": 1,\n"
                        + "    \"result\": {\n"
                        + "        \"id\": \"8ae75d6795f3541f897bcbfd3b4551aaf78b932cd0e91bf75a273940375c12a3\",\n"
                        + "        \"name\": \"Geth/v1.9.6-stable-bd059680/linux-amd64/go1.13.1\",\n"
                        + "        \"enode\": \"enode://1672a190f8c67669590db4b094c87573cbbc9b12f63d7137f505cfaa2cd2d35bea61abe1f8c898db4eab01d6c901270d7fff601b97a78f79ccefd83016b315cc@127.0.0.1:30303\",\n"
                        + "        \"enr\": \"enr:-Jq4QCKylmBZEJ1xizokiKyEST7FUrrOESva-sFWTkbBY6J0Xco6eUOkoc7lGOHy6yyCnjWhBEd35dr-c1FRxE3ozUEEg2V0aMrJhMs6ZLuDD8wlgmlkgnY0gmlwhH8AAAGJc2VjcDI1NmsxoQIWcqGQ-MZ2aVkNtLCUyHVzy7ybEvY9cTf1Bc-qLNLTW4N0Y3CCdl-DdWRwgnZf\",\n"
                        + "        \"ip\": \"127.0.0.1\",\n"
                        + "        \"ports\": {\n"
                        + "            \"discovery\": 30303,\n"
                        + "            \"listener\": 30303\n"
                        + "        },\n"
                        + "        \"listenAddr\": \"[::]:30303\",\n"
                        + "        \"protocols\": {\n"
                        + "            \"eth\": {\n"
                        + "                \"network\": 4,\n"
                        + "                \"difficulty\": 1,\n"
                        + "                \"genesis\": \"0x6341fd3daf94b748c72ced5a5b26028f2474f5f00d824504e4fa37a75767e177\",\n"
                        + "				   \"consensus\": \"clique\",\n"
                        + "                \"config\": {\n"
                        + "                    \"chainId\": 4,\n"
                        + "                    \"homesteadBlock\": 1,\n"
                        + "                    \"daoForkSupport\": true,\n"
                        + "                    \"eip150Block\": 2,\n"
                        + "                    \"eip150Hash\": \"0x9b095b36c15eaf13044373aef8ee0bd3a382a5abb92e402afa44b8249c3a90e9\",\n"
                        + "                    \"eip155Block\": 3,\n"
                        + "                    \"eip158Block\": 3,\n"
                        + "                    \"byzantiumBlock\": 1035301,\n"
                        + "                    \"constantinopleBlock\": 3660663,\n"
                        + "                    \"petersburgBlock\": 4321234,\n"
                        + "                    \"istanbulBlock\": 5435345,\n"
                        + "                    \"clique\": {\n"
                        + "                        \"period\": 15,\n"
                        + "                        \"epoch\": 30000\n"
                        + "                    }\n"
                        + "                },\n"
                        + "                \"head\": \"0x6341fd3daf94b748c72ced5a5b26028f2474f5f00d824504e4fa37a75767e177\"\n"
                        + "            }\n"
                        + "        }\n"
                        + "    }\n"
                        + "}");

        AdminNodeInfo adminNodeInfo = deserialiseResponse(AdminNodeInfo.class);
        assertEquals(
                adminNodeInfo.getResult().getName(),
                ("Geth/v1.9.6-stable-bd059680/linux-amd64/go1.13.1"));
    }

    @Test
    void testEthProtocolVersion() throws IOException {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"54\"\n"
                        + "}");

        EthProtocolVersion ethProtocolVersion = deserialiseResponse(EthProtocolVersion.class);
        assertEquals(ethProtocolVersion.getProtocolVersion(), ("54"));
    }

    @Test
    void testEthSyncingInProgress() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": {\n"
                        + "  \"startingBlock\": \"0x384\",\n"
                        + "  \"currentBlock\": \"0x386\",\n"
                        + "  \"highestBlock\": \"0x454\"\n"
                        + "  }\n"
                        + "}");

        // Response received from Geth node
        // "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"currentBlock\":\"0x117a\",
        // \"highestBlock\":\"0x21dab4\",\"knownStates\":\"0x0\",\"pulledStates\":\"0x0\",
        // \"startingBlock\":\"0xa51\"}}"

        EthSyncing ethSyncing = deserialiseResponse(EthSyncing.class);

        assertEquals(
                ethSyncing.getResult(),
                (new EthSyncing.Syncing("0x384", "0x386", "0x454", null, null)));
    }

    @Test
    void testEthSyncing() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": false\n"
                        + "}");

        EthSyncing ethSyncing = deserialiseResponse(EthSyncing.class);
        assertEquals(ethSyncing.isSyncing(), (false));
    }

    @Test
    void testEthMining() {
        buildResponse(
                "{\n"
                        + "  \"id\":71,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        EthMining ethMining = deserialiseResponse(EthMining.class);
        assertEquals(ethMining.isMining(), (true));
    }

    @Test
    void testEthHashrate() {
        buildResponse(
                "{\n"
                        + "  \"id\":71,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x38a\"\n"
                        + "}");

        EthHashrate ethHashrate = deserialiseResponse(EthHashrate.class);
        assertEquals(ethHashrate.getHashrate(), (BigInteger.valueOf(906L)));
    }

    @Test
    void testEthGasPrice() {
        buildResponse(
                "{\n"
                        + "  \"id\":73,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x9184e72a000\"\n"
                        + "}");

        EthGasPrice ethGasPrice = deserialiseResponse(EthGasPrice.class);
        assertEquals(ethGasPrice.getGasPrice(), (BigInteger.valueOf(10000000000000L)));
    }

    @Test
    void testEthAccounts() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": [\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"]\n"
                        + "}");

        EthAccounts ethAccounts = deserialiseResponse(EthAccounts.class);
        assertEquals(
                ethAccounts.getAccounts(),
                (Arrays.asList("0x407d73d8a49eeb85d32cf465507dd71d507100c1")));
    }

    @Test
    void testEthBlockNumber() {
        buildResponse(
                "{\n"
                        + "  \"id\":83,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x4b7\"\n"
                        + "}");

        EthBlockNumber ethBlockNumber = deserialiseResponse(EthBlockNumber.class);
        assertEquals(ethBlockNumber.getBlockNumber(), (BigInteger.valueOf(1207L)));
    }

    @Test
    void testEthGetBalance() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x234c8a3397aab58\"\n"
                        + "}");

        EthGetBalance ethGetBalance = deserialiseResponse(EthGetBalance.class);
        assertEquals(ethGetBalance.getBalance(), (BigInteger.valueOf(158972490234375000L)));
    }

    @Test
    void testEthStorageAt() {
        buildResponse(
                "{\n"
                        + "    \"jsonrpc\":\"2.0\","
                        + "    \"id\":1,"
                        + "    \"result\":"
                        + "\"0x000000000000000000000000000000000000000000000000000000000000162e\""
                        + "}");

        EthGetStorageAt ethGetStorageAt = deserialiseResponse(EthGetStorageAt.class);
        assertEquals(
                ethGetStorageAt.getResult(),
                ("0x000000000000000000000000000000000000000000000000000000000000162e"));
    }

    @Test
    void testEthGetTransactionCount() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x1\"\n"
                        + "}");

        EthGetTransactionCount ethGetTransactionCount =
                deserialiseResponse((EthGetTransactionCount.class));
        assertEquals(ethGetTransactionCount.getTransactionCount(), (BigInteger.valueOf(1L)));
    }

    @Test
    void testEthGetBlockTransactionCountByHash() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0xb\"\n"
                        + "}");

        EthGetBlockTransactionCountByHash ethGetBlockTransactionCountByHash =
                deserialiseResponse(EthGetBlockTransactionCountByHash.class);
        assertEquals(
                ethGetBlockTransactionCountByHash.getTransactionCount(), (BigInteger.valueOf(11)));
    }

    @Test
    void testEthGetBlockTransactionCountByNumber() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0xa\"\n"
                        + "}");

        EthGetBlockTransactionCountByNumber ethGetBlockTransactionCountByNumber =
                deserialiseResponse(EthGetBlockTransactionCountByNumber.class);
        assertEquals(
                ethGetBlockTransactionCountByNumber.getTransactionCount(),
                (BigInteger.valueOf(10)));
    }

    @Test
    void testEthGetUncleCountByBlockHash() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x1\"\n"
                        + "}");

        EthGetUncleCountByBlockHash ethGetUncleCountByBlockHash =
                deserialiseResponse(EthGetUncleCountByBlockHash.class);
        assertEquals(ethGetUncleCountByBlockHash.getUncleCount(), (BigInteger.valueOf(1)));
    }

    @Test
    void testEthGetUncleCountByBlockNumber() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x1\"\n"
                        + "}");

        EthGetUncleCountByBlockNumber ethGetUncleCountByBlockNumber =
                deserialiseResponse(EthGetUncleCountByBlockNumber.class);
        assertEquals(ethGetUncleCountByBlockNumber.getUncleCount(), (BigInteger.valueOf(1)));
    }

    @Test
    void testGetCode() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x600160008035811a818181146012578301005b601b60013560255"
                        + "65b8060005260206000f25b600060078202905091905056\"\n"
                        + "}");

        EthGetCode ethGetCode = deserialiseResponse(EthGetCode.class);
        assertEquals(
                ethGetCode.getCode(),
                ("0x600160008035811a818181146012578301005b601b60013560255"
                        + "65b8060005260206000f25b600060078202905091905056"));
    }

    @Test
    void testEthSign() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b712"
                        + "7ea4871e996cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001\"\n"
                        + "}");

        EthSign ethSign = deserialiseResponse(EthSign.class);
        assertEquals(
                ethSign.getSignature(),
                ("0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e9"
                        + "96cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001"));
    }

    @Test
    void testEthSendTransaction() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\"\n"
                        + "}");

        EthSendTransaction ethSendTransaction = deserialiseResponse(EthSendTransaction.class);
        assertEquals(
                ethSendTransaction.getTransactionHash(),
                ("0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331"));
    }

    @Test
    void testEthSendRawTransaction() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\"\n"
                        + "}");

        EthSendRawTransaction ethSendRawTransaction =
                deserialiseResponse(EthSendRawTransaction.class);
        assertEquals(
                ethSendRawTransaction.getTransactionHash(),
                ("0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331"));
    }

    @Test
    void testEthCall() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x\"\n"
                        + "}");

        EthCall ethCall = deserialiseResponse(EthCall.class);
        assertEquals(ethCall.getValue(), ("0x"));
        assertFalse(ethCall.isReverted());
        assertNull(ethCall.getRevertReason());
    }

    @Test
    void testEthCallReverted() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x08c379a0"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "00000000000000000000000000000000000000000000000000000000000000ee"
                        + "536f6c696469747920757365732073746174652d726576657274696e67206578"
                        + "63657074696f6e7320746f2068616e646c65206572726f72732e205468652072"
                        + "6571756972652066756e6374696f6e2073686f756c6420626520757365642074"
                        + "6f20656e737572652076616c696420636f6e646974696f6e732c207375636820"
                        + "617320696e707574732c206f7220636f6e747261637420737461746520766172"
                        + "6961626c657320617265206d65742c206f7220746f2076616c69646174652072"
                        + "657475726e2076616c7565732066726f6d2063616c6c7320746f206578746572"
                        + "6e616c20636f6e7472616374732e000000000000000000000000000000000000\"\n"
                        + "}");

        EthCall ethCall = deserialiseResponse(EthCall.class);
        assertTrue(ethCall.isReverted());
        assertEquals(
                ethCall.getRevertReason(),
                ("Solidity uses state-reverting exceptions to "
                        + "handle errors. The require function should be "
                        + "used to ensure valid conditions, such as inputs, "
                        + "or contract state variables are met, or to "
                        + "validate return values from calls to "
                        + "external contracts."));
    }

    @Test
    void testEthEstimateGas() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x5208\"\n"
                        + "}");

        EthEstimateGas ethEstimateGas = deserialiseResponse(EthEstimateGas.class);
        assertEquals(ethEstimateGas.getAmountUsed(), (BigInteger.valueOf(21000)));
    }

    @Test
    void testEthSendUserOperation() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x5208\"\n"
                        + "}");

        EthSendUserOperation ethEstimateGas = deserialiseResponse(EthSendUserOperation.class);
        assertEquals("0x5208", ethEstimateGas.getUserOperationHash());
    }

    @Test
    void testEthEstimateUserOperationGas() {
        buildResponse(
                "{\n"
                        + "\"id\":1,\n"
                        + "\"jsonrpc\":\"2.0\",\n"
                        + "\"result\": {\n"
                        + "    \"preVerificationGas\": \"0x9f759\",\n"
                        + "    \"verificationGasLimit\": \"0x9f759\",\n"
                        + "    \"callGasLimit\": \"0x9f759\",\n"
                        + "    \"paymasterVerificationGasLimit\": \"0x9f759\"\n"
                        + "  }\n"
                        + "}");

        EthEstimateUserOperationGas userGas =
                deserialiseResponse(EthEstimateUserOperationGas.class);
        EthEstimateUserOperationGas.UserOperationGas gas =
                new EthEstimateUserOperationGas.UserOperationGas(
                        "0x9f759", "0x9f759", "0x9f759", "0x9f759");

        assertEquals(gas, userGas.getUserOperationGas());
    }

    @Test
    void testEthGetUserOperationByHash() {
        buildResponse(
                "{\n"
                        + "\"id\":1,\n"
                        + "\"jsonrpc\":\"2.0\",\n"
                        + "\"result\": {\n"
                        + "    \"sender\": \"0x600160008035811a818181146012578301005b601b60013560255\",\n"
                        + "    \"nonce\": \"0x1\",\n"
                        + "    \"initCode\": \"0x9f759\",\n"
                        + "    \"callData\": \"0x9f759\",\n"
                        + "    \"callGasLimit\": \"0x9f759\",\n"
                        + "    \"verificationGasLimit\": \"0x9f759\",\n"
                        + "    \"preVerificationGas\": \"0x9f759\",\n"
                        + "    \"maxFeePerGas\": \"0x9f759\",\n"
                        + "    \"maxPriorityFeePerGas\": \"0x9f759\",\n"
                        + "    \"paymasterAndData\": \"0x9f759\",\n"
                        + "    \"signature\": \"0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea487110\",\n"
                        + "    \"entryPoint\": \"0xa70e8dd61c5d32be8058bb8eb970870f07233156\",\n"
                        + "    \"blockNumber\": \"0x1\",\n"
                        + "    \"blockHash\": \"0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e9\",\n"
                        + "    \"transactionHash\": \"0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e1\"\n"
                        + "  }\n"
                        + "}");

        EthGetUserOperationByHash userOperationByHash =
                deserialiseResponse(EthGetUserOperationByHash.class);

        UserOperation userOperation =
                new UserOperation(
                        "0x600160008035811a818181146012578301005b601b60013560255",
                        "0x1",
                        "0x9f759",
                        "0x9f759",
                        "0x9f759",
                        "0x9f759",
                        "0x9f759",
                        "0x9f759",
                        "0x9f759",
                        "0x9f759",
                        "0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea487110",
                        "0xa70e8dd61c5d32be8058bb8eb970870f07233156",
                        "0x1",
                        "0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e9",
                        "0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e1");

        assertEquals(userOperation, userOperationByHash.getUserOperation());
    }

    @Test
    void testEthGetUserOperationReceipt() {
        buildResponse(
                "{\n"
                        + "\"id\":1,\n"
                        + "\"jsonrpc\":\"2.0\",\n"
                        + "\"result\": {\n"
                        + "    \"userOpHash\": \"0x13574b2256b73bdc33fb121052f64b3803161e5ec602a6dc9e56177ba387e700\",\n"
                        + "    \"entryPoint\": \"0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789\",\n"
                        + "    \"sender\": \"0x023fEF87894773DF227587d9B29af8D17b4dBB5A\",\n"
                        + "    \"nonce\": \"0x1\",\n"
                        + "    \"paymaster\": null,\n"
                        + "    \"actualGasCost\": \"0x6f75ef8d\",\n"
                        + "    \"actualGasUsed\": \"0x329af\",\n"
                        + "    \"success\": true,\n"
                        + "    \"reason\": \"\",\n"
                        + "    \"logs\": [\n"
                        + "      {\n"
                        + "        \"address\": \"0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789\",\n"
                        + "        \"topics\": [\n"
                        + "          \"0xbb47ee3e183a558b1a2ff0874b079f3fc5478b7454eacf2bfc5af2ff5878f972\"\n"
                        + "        ],\n"
                        + "        \"data\": \"0x\",\n"
                        + "        \"blockNumber\": \"0x27fb22e\",\n"
                        + "        \"transactionHash\": \"0x0f9b0e5868beaf345d8d55895c8037ae85adb91c422c00badcdcae8a0bf247a1\",\n"
                        + "        \"transactionIndex\": \"0x4\",\n"
                        + "        \"blockHash\": \"0x965e08190b1093c078bde81f67362203834784e34cf499d516f1a7b9c7a7b29e\",\n"
                        + "        \"logIndex\": \"0x13\",\n"
                        + "        \"removed\": false\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"receipt\": {\n"
                        + "      \"blockHash\": \"0x965e08190b1093c078bde81f67362203834784e34cf499d516f1a7b9c7a7b29e\",\n"
                        + "      \"blockNumber\": \"0x27fb22e\",\n"
                        + "      \"from\": \"0x425d190ef5F561aFc8728593cA13EAf2FD9E3380\",\n"
                        + "      \"to\": \"0x25aD59adbe00C2d80c86d01e2E05e1294DA84823\",\n"
                        + "      \"cumulativeGasUsed\": \"0xe13e1\",\n"
                        + "      \"gasUsed\": \"0x329af\",\n"
                        + "      \"contractAddress\": null,\n"
                        + "      \"logs\": [null],\n"
                        + "      \"logsBloom\": \"0x000000010000000000000000800000000000000000000008000000000200000000080000020000020002080100010000001080000000000000100210000000000000000000000008000000000000808010000000000000000001000000000000000000000e000000000000000000080000002200000000408880000000000040000020000000000001000000080000002040000000040000000000000008000020000000000100000040000000000000000000000000000000000220000000400000000000000000000100000010000044000000800020000a100000010020000000000040000081000000000000000000000000000000400000000000100000\",\n"
                        + "      \"status\": 1,\n"
                        + "      \"type\": \"0x2\",\n"
                        + "      \"transactionHash\": \"0x0f9b0e5868beaf345d8d55895c8037ae85adb91c422c00badcdcae8a0bf247a1\",\n"
                        + "      \"transactionIndex\": \"0x4\",\n"
                        + "      \"effectiveGasPrice\": \"0x6f75ef8d\"\n"
                        + "    }\n"
                        + "  }\n"
                        + "}");

        EthGetUserOperationReceipt userOperationReceipt =
                deserialiseResponse(EthGetUserOperationReceipt.class);

        Log log = new Log();
        log.setAddress("0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789");
        log.setTopics(
                Collections.singletonList(
                        "0xbb47ee3e183a558b1a2ff0874b079f3fc5478b7454eacf2bfc5af2ff5878f972"));
        log.setData("0x");
        log.setBlockNumber("0x27fb22e");
        log.setTransactionHash(
                "0x0f9b0e5868beaf345d8d55895c8037ae85adb91c422c00badcdcae8a0bf247a1");
        log.setTransactionIndex("0x4");
        log.setBlockHash("0x965e08190b1093c078bde81f67362203834784e34cf499d516f1a7b9c7a7b29e");
        log.setLogIndex("0x13");
        log.setRemoved(false);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setBlockHash("0x965e08190b1093c078bde81f67362203834784e34cf499d516f1a7b9c7a7b29e");
        receipt.setBlockNumber("0x27fb22e");
        receipt.setFrom("0x425d190ef5F561aFc8728593cA13EAf2FD9E3380");
        receipt.setTo("0x25aD59adbe00C2d80c86d01e2E05e1294DA84823");
        receipt.setCumulativeGasUsed("0xe13e1");
        receipt.setGasUsed("0x329af");
        receipt.setContractAddress(null);
        receipt.setLogs(Collections.singletonList(null)); // JSON contains a single null log
        receipt.setLogsBloom(
                "0x000000010000000000000000800000000000000000000008000000000200000000080000020000020002080100010000001080000000000000100210000000000000000000000008000000000000808010000000000000000001000000000000000000000e000000000000000000080000002200000000408880000000000040000020000000000001000000080000002040000000040000000000000008000020000000000100000040000000000000000000000000000000000220000000400000000000000000000100000010000044000000800020000a100000010020000000000040000081000000000000000000000000000000400000000000100000");
        receipt.setStatus("1");
        receipt.setType("0x2");
        receipt.setTransactionHash(
                "0x0f9b0e5868beaf345d8d55895c8037ae85adb91c422c00badcdcae8a0bf247a1");
        receipt.setTransactionIndex("0x4");
        receipt.setEffectiveGasPrice("0x6f75ef8d");

        UserOperationReceipt userOpReceipt = new UserOperationReceipt();
        userOpReceipt.setUserOpHash(
                "0x13574b2256b73bdc33fb121052f64b3803161e5ec602a6dc9e56177ba387e700");
        userOpReceipt.setEntryPoint("0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789");
        userOpReceipt.setSender("0x023fEF87894773DF227587d9B29af8D17b4dBB5A");
        userOpReceipt.setNonce("0x1");
        userOpReceipt.setPaymaster(null);
        userOpReceipt.setActualGasCost("0x6f75ef8d");
        userOpReceipt.setActualGasUsed("0x329af");
        userOpReceipt.setSuccess(true);
        userOpReceipt.setReason("");
        userOpReceipt.setLogs(Collections.singletonList(log));
        userOpReceipt.setReceipt(receipt);

        assertEquals(userOpReceipt, userOperationReceipt.getOperationReceipt());
    }

    @Test
    void testEthSupportedEntryPoints() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": [\"0x600160008035811a818181146012578301005b601b60013560255\"]\n"
                        + "}");

        EthSupportedEntryPoints entryPoints = deserialiseResponse(EthSupportedEntryPoints.class);
        assertEquals(
                List.of("0x600160008035811a818181146012578301005b601b60013560255"),
                entryPoints.getSupportedEntryPoints());
    }

    @Test
    void testEthBaseFee() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x5d21dba00\"\n"
                        + "}");

        EthBaseFee ethBaseFee = deserialiseResponse(EthBaseFee.class);
        assertEquals(ethBaseFee.getBaseFee(), (BigInteger.valueOf(25000000000L)));
    }

    @Test
    void testEthBlockTransactionHashes() {

        buildResponse(
                "{\n"
                        + "\"id\":1,\n"
                        + "\"jsonrpc\":\"2.0\",\n"
                        + "\"result\": {\n"
                        + "    \"number\": \"0x1b4\",\n"
                        + "    \"hash\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"parentHash\": \"0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5\",\n"
                        + "    \"parentBeaconBlockRoot\": \"0x87996eeac031318cd38ee5de92b630676da7263b697a93ff55d0ad88c142e169\",\n"
                        + "    \"nonce\": \"0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2\",\n"
                        + "    \"sha3Uncles\": \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "    \"logsBloom\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"transactionsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"stateRoot\": \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\",\n"
                        + "    \"receiptsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"author\": \"0x1a95ad5ccdb0677af951810c6ddf4935afe4e5a6\",\n"
                        + "    \"miner\": \"0x4e65fda2159562a496f9f3522f89122a3088497a\",\n"
                        + "    \"mixHash\": \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "    \"difficulty\": \"0x027f07\",\n"
                        + "    \"totalDifficulty\":  \"0x027f07\",\n"
                        + "    \"extraData\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "    \"size\":  \"0x027f07\",\n"
                        + "    \"gasLimit\": \"0x9f759\",\n"
                        + "    \"gasUsed\": \"0x9f759\",\n"
                        + "    \"timestamp\": \"0x54e34e8e\",\n"
                        + "    \"transactions\": ["
                        + "        \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "        \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1df\"\n"
                        + "    ], \n"
                        + "    \"uncles\": [\n"
                        + "       \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "       \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\"\n"
                        + "    ],\n"
                        + "    \"sealFields\": [\n"
                        + "       \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "       \"0x39a3eb432fbef1fc\"\n"
                        + "    ],\n"
                        + "    \"baseFeePerGas\": \"0x7\",\n"
                        + "    \"withdrawalsRoot\": \"0x1b1f845cd61c375a89ef101fd1bd86355f372a6a3dfe1960f2355e70bd5cd8a2\",\n"
                        + "    \"withdrawals\": [\n"
                        + "      {\n"
                        + "        \"index\": \"0x68ba80\",\n"
                        + "        \"validatorIndex\": \"0x65285\",\n"
                        + "        \"address\": \"0x1e09b4199780a45792f4ff195ef68410a091b047\",\n"
                        + "        \"amount\": \"0xd1f129\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"blobGasUsed\": \"0xa0000\",\n"
                        + "    \"excessBlobGas\": \"0x4bc0000\"\n"
                        + "  }\n"
                        + "}");

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        EthBlock.Block block =
                new EthBlock.Block(
                        "0x1b4",
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                        "0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5",
                        "0x87996eeac031318cd38ee5de92b630676da7263b697a93ff55d0ad88c142e169",
                        "0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2",
                        "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                        "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff",
                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                        "0x1a95ad5ccdb0677af951810c6ddf4935afe4e5a6",
                        "0x4e65fda2159562a496f9f3522f89122a3088497a",
                        "0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b",
                        "0x027f07",
                        "0x027f07",
                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                        "0x027f07",
                        "0x9f759",
                        "0x9f759",
                        "0x54e34e8e",
                        Arrays.asList(
                                new EthBlock.TransactionHash(
                                        "0xe670ec64341771606e55d6b4ca35a1a6b"
                                                + "75ee3d5145a99d05921026d1527331"),
                                new EthBlock.TransactionHash(
                                        "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1df")),
                        Arrays.asList(
                                "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                                "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff"),
                        Arrays.asList(
                                "0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b",
                                "0x39a3eb432fbef1fc"),
                        "0x7",
                        "0x1b1f845cd61c375a89ef101fd1bd86355f372a6a3dfe1960f2355e70bd5cd8a2",
                        Arrays.asList(
                                new EthBlock.Withdrawal(
                                        "0x68ba80",
                                        "0x65285",
                                        "0x1e09b4199780a45792f4ff195ef68410a091b047",
                                        "0xd1f129")),
                        "0xa0000",
                        "0x4bc0000");
        assertEquals(ethBlock.getBlock(), (block));
    }

    @Test
    void testEthBlockFullTransactionsWithoutBlob() {

        buildResponse(
                "{\n"
                        + "\"id\":1,\n"
                        + "\"jsonrpc\":\"2.0\",\n"
                        + "\"result\": {\n"
                        + "    \"number\": \"0x1b4\",\n"
                        + "    \"hash\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"parentHash\": \"0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5\",\n"
                        + "    \"nonce\": \"0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2\",\n"
                        + "    \"sha3Uncles\": \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "    \"logsBloom\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"transactionsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"stateRoot\": \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\",\n"
                        + "    \"receiptsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"author\": \"0x1a95ad5ccdb0677af951810c6ddf4935afe4e5a6\",\n"
                        + "    \"miner\": \"0x4e65fda2159562a496f9f3522f89122a3088497a\",\n"
                        + "    \"mixHash\": \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "    \"difficulty\": \"0x027f07\",\n"
                        + "    \"totalDifficulty\":  \"0x027f07\",\n"
                        + "    \"extraData\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "    \"size\":  \"0x027f07\",\n"
                        + "    \"gasLimit\": \"0x9f759\",\n"
                        + "    \"gasUsed\": \"0x9f759\",\n"
                        + "    \"timestamp\": \"0x54e34e8e\",\n"
                        + "    \"transactions\": [{"
                        + "        \"hash\":\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"nonce\":\"0x\",\n"
                        + "        \"blockHash\": \"0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0x15df\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"value\":\"0x7f110\",\n"
                        + "        \"gas\": \"0x7f110\",\n"
                        + "        \"gasPrice\":\"0x09184e72a000\",\n"
                        + "        \"input\":\"0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360\","
                        + "        \"creates\":null,\n"
                        + "        \"publicKey\":\"0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b\",\n"
                        + "        \"raw\":\"0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"r\":\"0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc\",\n"
                        + "        \"s\":\"0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"v\":\"0\",\n"
                        + "    \"accessList\": [{"
                        + "        \"address\":\"0x408e41876cccdc0f92210600ef50372656052a38\",\n"
                        + "    \"storageKeys\": ["
                        + "        \"0x18919546fd5421b0ef1b1b8dfce80500e69f2e28ae34c4d6298172949fa77dcc\",\n"
                        + "        \"0x4869ff95a61ee1ded0b22e2d0e3f54f3199886a9f361e634132c95164bfc5129\"\n"
                        + "    ] \n"
                        + "    }], \n"
                        + "        \"type\":\"0x0\",\n"
                        + "        \"maxFeePerGas\": \"0x7f110\",\n"
                        + "        \"maxPriorityFeePerGas\": \"0x7f110\"\n"
                        + "    }], \n"
                        + "    \"uncles\": [\n"
                        + "       \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "       \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\"\n"
                        + "    ],\n"
                        + "    \"sealFields\": [\n"
                        + "       \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "       \"0x39a3eb432fbef1fc\"\n"
                        + "    ],\n"
                        + "    \"baseFeePerGas\": \"0x7\",\n"
                        + "    \"withdrawalsRoot\": \"0x1b1f845cd61c375a89ef101fd1bd86355f372a6a3dfe1960f2355e70bd5cd8a2\",\n"
                        + "    \"withdrawals\": [\n"
                        + "      {\n"
                        + "        \"index\": \"0x68ba80\",\n"
                        + "        \"validatorIndex\": \"0x65285\",\n"
                        + "        \"address\": \"0x1e09b4199780a45792f4ff195ef68410a091b047\",\n"
                        + "        \"amount\": \"0xd1f129\"\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}");

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        EthBlock.Block block =
                new EthBlock.Block(
                        "0x1b4",
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                        "0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5",
                        "0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2",
                        "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                        "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff",
                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                        "0x1a95ad5ccdb0677af951810c6ddf4935afe4e5a6",
                        "0x4e65fda2159562a496f9f3522f89122a3088497a",
                        "0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b",
                        "0x027f07",
                        "0x027f07",
                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                        "0x027f07",
                        "0x9f759",
                        "0x9f759",
                        "0x54e34e8e",
                        Arrays.asList(
                                new EthBlock.TransactionObject(
                                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                                        "0x",
                                        "0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b",
                                        "0x15df",
                                        "0x1",
                                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                                        "0x7f110",
                                        "0x09184e72a000",
                                        "0x7f110",
                                        "0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360",
                                        null,
                                        "0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b",
                                        "0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62",
                                        "0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc",
                                        "0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62",
                                        (byte) 0,
                                        "0x0",
                                        "0x7f110",
                                        "0x7f110",
                                        Arrays.asList(
                                                new AccessListObject(
                                                        "0x408e41876cccdc0f92210600ef50372656052a38",
                                                        Arrays.asList(
                                                                "0x18919546fd5421b0ef1b1b8dfce80500e69f2e28ae34c4d6298172949fa77dcc",
                                                                "0x4869ff95a61ee1ded0b22e2d0e3f54f3199886a9f361e634132c95164bfc5129"))))),
                        Arrays.asList(
                                "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                                "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff"),
                        Arrays.asList(
                                "0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b",
                                "0x39a3eb432fbef1fc"),
                        "0x7",
                        "0x1b1f845cd61c375a89ef101fd1bd86355f372a6a3dfe1960f2355e70bd5cd8a2",
                        Arrays.asList(
                                new EthBlock.Withdrawal(
                                        "0x68ba80",
                                        "0x65285",
                                        "0x1e09b4199780a45792f4ff195ef68410a091b047",
                                        "0xd1f129")));

        assertEquals(ethBlock.getBlock(), (block));
    }

    @Test
    void testEthBlockFullTransactionsWithBlob() {

        buildResponse(
                "{\n"
                        + "\"id\":1,\n"
                        + "\"jsonrpc\":\"2.0\",\n"
                        + "\"result\": {\n"
                        + "    \"number\": \"0x1b4\",\n"
                        + "    \"hash\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"parentHash\": \"0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5\",\n"
                        + "    \"parentBeaconBlockRoot\": \"0x87996eeac031318cd38ee5de92b630676da7263b697a93ff55d0ad88c142e169\",\n"
                        + "    \"nonce\": \"0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2\",\n"
                        + "    \"sha3Uncles\": \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "    \"logsBloom\": \"0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331\",\n"
                        + "    \"transactionsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"stateRoot\": \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\",\n"
                        + "    \"receiptsRoot\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"author\": \"0x1a95ad5ccdb0677af951810c6ddf4935afe4e5a6\",\n"
                        + "    \"miner\": \"0x4e65fda2159562a496f9f3522f89122a3088497a\",\n"
                        + "    \"mixHash\": \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "    \"difficulty\": \"0x027f07\",\n"
                        + "    \"totalDifficulty\":  \"0x027f07\",\n"
                        + "    \"extraData\": \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "    \"size\":  \"0x027f07\",\n"
                        + "    \"gasLimit\": \"0x9f759\",\n"
                        + "    \"gasUsed\": \"0x9f759\",\n"
                        + "    \"timestamp\": \"0x54e34e8e\",\n"
                        + "    \"transactions\": [{"
                        + "        \"hash\":\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"nonce\":\"0x\",\n"
                        + "        \"blockHash\": \"0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0x15df\",\n"
                        + "        \"chainId\": \"0x7f110\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"value\":\"0x7f110\",\n"
                        + "        \"gas\": \"0x7f110\",\n"
                        + "        \"gasPrice\":\"0x09184e72a000\",\n"
                        + "        \"input\":\"0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360\","
                        + "        \"creates\":null,\n"
                        + "        \"publicKey\":\"0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b\",\n"
                        + "        \"raw\":\"0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"r\":\"0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc\",\n"
                        + "        \"s\":\"0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"v\":\"0\",\n"
                        + "        \"yParity\": \"0x0\",\n"
                        + "    \"accessList\": [{"
                        + "        \"address\":\"0x408e41876cccdc0f92210600ef50372656052a38\",\n"
                        + "    \"storageKeys\": ["
                        + "        \"0x18919546fd5421b0ef1b1b8dfce80500e69f2e28ae34c4d6298172949fa77dcc\",\n"
                        + "        \"0x4869ff95a61ee1ded0b22e2d0e3f54f3199886a9f361e634132c95164bfc5129\"\n"
                        + "    ] \n"
                        + "    }], \n"
                        + "        \"type\":\"0x0\",\n"
                        + "        \"maxFeePerGas\": \"0x7f110\",\n"
                        + "        \"maxPriorityFeePerGas\": \"0x7f110\",\n"
                        + "        \"maxFeePerBlobGas\": \"0x7f110\",\n"
                        + "        \"blobVersionedHashes\": [\"0x013343644e9aaa7e8673ba3be38b56bb3dfaa57db923797247e5f2e504b721c3\", \"0x01cad19a7fe88d9e14575394847a4a0026fccf292c4ca30ef047e6d03d3a74bb\"]"
                        + "    }], \n"
                        + "    \"uncles\": [\n"
                        + "       \"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\n"
                        + "       \"0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff\"\n"
                        + "    ],\n"
                        + "    \"sealFields\": [\n"
                        + "       \"0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b\",\n"
                        + "       \"0x39a3eb432fbef1fc\"\n"
                        + "    ],\n"
                        + "    \"baseFeePerGas\": \"0x7\",\n"
                        + "    \"withdrawalsRoot\": \"0x1b1f845cd61c375a89ef101fd1bd86355f372a6a3dfe1960f2355e70bd5cd8a2\",\n"
                        + "    \"withdrawals\": [\n"
                        + "      {\n"
                        + "        \"index\": \"0x68ba80\",\n"
                        + "        \"validatorIndex\": \"0x65285\",\n"
                        + "        \"address\": \"0x1e09b4199780a45792f4ff195ef68410a091b047\",\n"
                        + "        \"amount\": \"0xd1f129\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"blobGasUsed\": \"0xa0000\",\n"
                        + "    \"excessBlobGas\": \"0x4bc0000\"\n"
                        + "  }\n"
                        + "}");

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        EthBlock.Block block =
                new EthBlock.Block(
                        "0x1b4",
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                        "0x9646252be9520f6e71339a8df9c55e4d7619deeb018d2a3f2d21fc165dde5eb5",
                        "0x87996eeac031318cd38ee5de92b630676da7263b697a93ff55d0ad88c142e169",
                        "0xe04d296d2460cfb8472af2c5fd05b5a214109c25688d3704aed5484f9a7792f2",
                        "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                        "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331",
                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                        "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff",
                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                        "0x1a95ad5ccdb0677af951810c6ddf4935afe4e5a6",
                        "0x4e65fda2159562a496f9f3522f89122a3088497a",
                        "0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b",
                        "0x027f07",
                        "0x027f07",
                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                        "0x027f07",
                        "0x9f759",
                        "0x9f759",
                        "0x54e34e8e",
                        Arrays.asList(
                                new EthBlock.TransactionObject(
                                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                                        "0x",
                                        "0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b",
                                        "0x15df",
                                        "0x7f110",
                                        "0x1",
                                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                                        "0x7f110",
                                        "0x09184e72a000",
                                        "0x7f110",
                                        "0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360",
                                        null,
                                        "0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b",
                                        "0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62",
                                        "0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc",
                                        "0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62",
                                        0,
                                        "0x0",
                                        "0x0",
                                        "0x7f110",
                                        "0x7f110",
                                        Arrays.asList(
                                                new AccessListObject(
                                                        "0x408e41876cccdc0f92210600ef50372656052a38",
                                                        Arrays.asList(
                                                                "0x18919546fd5421b0ef1b1b8dfce80500e69f2e28ae34c4d6298172949fa77dcc",
                                                                "0x4869ff95a61ee1ded0b22e2d0e3f54f3199886a9f361e634132c95164bfc5129"))),
                                        "0x7f110",
                                        List.of(
                                                "0x013343644e9aaa7e8673ba3be38b56bb3dfaa57db923797247e5f2e504b721c3",
                                                "0x01cad19a7fe88d9e14575394847a4a0026fccf292c4ca30ef047e6d03d3a74bb"))),
                        Arrays.asList(
                                "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                                "0xd5855eb08b3387c0af375e9cdb6acfc05eb8f519e419b874b6ff2ffda7ed1dff"),
                        Arrays.asList(
                                "0x57919c4e72e79ad7705a26e7ecd5a08ff546ac4fa37882e9cc57be87a3dab26b",
                                "0x39a3eb432fbef1fc"),
                        "0x7",
                        "0x1b1f845cd61c375a89ef101fd1bd86355f372a6a3dfe1960f2355e70bd5cd8a2",
                        Arrays.asList(
                                new EthBlock.Withdrawal(
                                        "0x68ba80",
                                        "0x65285",
                                        "0x1e09b4199780a45792f4ff195ef68410a091b047",
                                        "0xd1f129")),
                        "0xa0000",
                        "0x4bc0000");

        assertEquals(ethBlock.getBlock(), (block));
    }

    @Test
    void testEthBlockNull() {
        buildResponse("{\n" + "  \"result\": null\n" + "}");

        EthBlock ethBlock = deserialiseResponse(EthBlock.class);
        assertNull(ethBlock.getBlock());
    }

    //
    @Test
    void testEthTransaction() {

        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": {\n"
                        + "        \"hash\":\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"nonce\":\"0x\",\n"
                        + "        \"blockHash\": \"0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0x15df\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"value\":\"0x7f110\",\n"
                        + "        \"gas\": \"0x7f110\",\n"
                        + "        \"gasPrice\":\"0x09184e72a000\",\n"
                        + "        \"input\":\"0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360\",\n"
                        + "        \"creates\":null,\n"
                        + "        \"publicKey\":\"0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b\",\n"
                        + "        \"raw\":\"0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"r\":\"0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc\",\n"
                        + "        \"s\":\"0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"v\":\"0\",\n"
                        + "    \"accessList\": [{"
                        + "        \"address\":\"0x408e41876cccdc0f92210600ef50372656052a38\",\n"
                        + "    \"storageKeys\": ["
                        + "        \"0x18919546fd5421b0ef1b1b8dfce80500e69f2e28ae34c4d6298172949fa77dcc\",\n"
                        + "        \"0x4869ff95a61ee1ded0b22e2d0e3f54f3199886a9f361e634132c95164bfc5129\"\n"
                        + "    ] \n"
                        + "    }], \n"
                        + "        \"type\":\"0x0\",\n"
                        + "        \"maxFeePerGas\": \"0x7f110\",\n"
                        + "        \"maxPriorityFeePerGas\": \"0x7f110\"\n"
                        + "  }\n"
                        + "}");
        Transaction transaction =
                new Transaction(
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        "0x",
                        "0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b",
                        "0x15df",
                        "0x1",
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                        "0x7f110",
                        "0x7f110",
                        "0x09184e72a000",
                        "0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360",
                        null,
                        "0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b",
                        "0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62",
                        "0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc",
                        "0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62",
                        (byte) 0,
                        "0x0",
                        "0x7f110",
                        "0x7f110",
                        Arrays.asList(
                                new AccessListObject(
                                        "0x408e41876cccdc0f92210600ef50372656052a38",
                                        Arrays.asList(
                                                "0x18919546fd5421b0ef1b1b8dfce80500e69f2e28ae34c4d6298172949fa77dcc",
                                                "0x4869ff95a61ee1ded0b22e2d0e3f54f3199886a9f361e634132c95164bfc5129"))));

        EthTransaction ethTransaction = deserialiseResponse(EthTransaction.class);
        assertEquals(ethTransaction.getTransaction().get(), (transaction));
    }

    @Test
    void testEthTransactionResponseWithYParity() {
        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": {\n"
                        + "        \"hash\":\"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"nonce\":\"0x\",\n"
                        + "        \"blockHash\": \"0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0x15df\",\n"
                        + "        \"chainId\": \"4\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"value\":\"0x7f110\",\n"
                        + "        \"gas\": \"0x7f110\",\n"
                        + "        \"gasPrice\":\"0x09184e72a000\",\n"
                        + "        \"input\":\"0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360\",\n"
                        + "        \"creates\":null,\n"
                        + "        \"publicKey\":\"0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b\",\n"
                        + "        \"raw\":\"0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"r\":\"0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc\",\n"
                        + "        \"s\":\"0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62\",\n"
                        + "        \"v\":\"0\",\n"
                        + "        \"yParity\":\"1\",\n"
                        + "    \"accessList\": [{"
                        + "        \"address\":\"0x408e41876cccdc0f92210600ef50372656052a38\",\n"
                        + "    \"storageKeys\": ["
                        + "        \"0x18919546fd5421b0ef1b1b8dfce80500e69f2e28ae34c4d6298172949fa77dcc\",\n"
                        + "        \"0x4869ff95a61ee1ded0b22e2d0e3f54f3199886a9f361e634132c95164bfc5129\"\n"
                        + "    ] \n"
                        + "    }], \n"
                        + "        \"type\":\"0x0\",\n"
                        + "        \"maxFeePerGas\": \"0x7f110\",\n"
                        + "        \"maxPriorityFeePerGas\": \"0x7f110\"\n"
                        + "  }\n"
                        + "}");
        Transaction transaction =
                new Transaction(
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        "0x",
                        "0xbeab0aa2411b7ab17f30a99d3cb9c6ef2fc5426d6ad6fd9e2a26a6aed1d1055b",
                        "0x15df",
                        "4",
                        "0x1",
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                        "0x7f110",
                        "0x7f110",
                        "0x09184e72a000",
                        "0x603880600c6000396000f300603880600c6000396000f3603880600c6000396000f360",
                        null,
                        "0x6614d7d7bfe989295821985de0439e868b26ff05f98ae0da0ce5bccc24ea368a083b785323c9fcb405dd4c10a2c95d93312a1b2d68beb24ab4ea7c3c2f7c455b",
                        "0xf8cd83103a048504a817c800830e57e0945927c5cc723c4486f93bf90bad3be8831139499e80b864140f8dd300000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000c03905df347aa6490d5a98fbb8d8e49520000000000000000000000000000000000000000000000000000000057d56ee61ba0f115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dca04a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62",
                        "0xf115cc4d7516dd430046504e1c888198e0323e8ded016d755f89c226ba3481dc",
                        "0x4a2ae8ee49f1100b5c0202b37ed8bacf4caeddebde6b7f77e12e7a55893e9f62",
                        (byte) 0,
                        "1",
                        "0x0",
                        "0x7f110",
                        "0x7f110",
                        Arrays.asList(
                                new AccessListObject(
                                        "0x408e41876cccdc0f92210600ef50372656052a38",
                                        Arrays.asList(
                                                "0x18919546fd5421b0ef1b1b8dfce80500e69f2e28ae34c4d6298172949fa77dcc",
                                                "0x4869ff95a61ee1ded0b22e2d0e3f54f3199886a9f361e634132c95164bfc5129"))));

        EthTransaction ethTransaction = deserialiseResponse(EthTransaction.class);
        assertEquals(ethTransaction.getTransaction().get(), (transaction));
    }

    @Test
    void testTransactionChainId() {
        Transaction transaction = new Transaction();
        transaction.setV(0x25);
        assertEquals(transaction.getChainId(), (1L));

        Transaction transaction2 = new Transaction();
        transaction2.setV(0x24);
        assertEquals(transaction2.getChainId(), (0L));
    }

    @Test
    void testTransactionWithoutChainId() {
        Transaction transaction1 = new Transaction();
        transaction1.setV(0x1b); // 27
        assertEquals(transaction1.getChainId(), null);

        Transaction transaction2 = new Transaction();
        transaction2.setV(0x1c); // 28
        assertEquals(transaction2.getChainId(), null);
    }

    @Test
    void testTransactionLongChainId() {
        Transaction transaction = new Transaction();
        transaction.setV(0x4A817C823L);
        assertEquals(transaction.getChainId(), (10000000000L));
    }

    @Test
    void testEthTransactionNull() {
        buildResponse("{\n" + "  \"result\": null\n" + "}");

        EthTransaction ethTransaction = deserialiseResponse(EthTransaction.class);
        assertEquals(ethTransaction.getTransaction(), (Optional.empty()));
    }

    @Test
    void testeEthGetTransactionReceiptBeforeByzantium() {

        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": {\n"
                        + "        \"transactionHash\": \"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"blockHash\": \"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0xb\",\n"
                        + "        \"cumulativeGasUsed\": \"0x33bc\",\n"
                        + "        \"gasUsed\": \"0x4dc\",\n"
                        + "        \"contractAddress\": \"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\n"
                        + "        \"root\": \"9307ba10e41ecf3d40507fc908655fe72fc129d46f6d99baf7605d0e29184911\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"logs\": [{\n"
                        + "            \"removed\": false,\n"
                        + "            \"logIndex\": \"0x1\",\n"
                        + "            \"transactionIndex\": \"0x0\",\n"
                        + "            \"transactionHash\": \"0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf\",\n"
                        + "            \"blockHash\": \"0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"blockNumber\":\"0x1b4\",\n"
                        + "            \"address\": \"0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "            \"type\":\"mined\",\n"
                        + "            \"topics\": [\"0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5\"]"
                        + "        }],\n"
                        + "        \"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "        \"effectiveGasPrice\": null,\n"
                        + "        \"type\": null\n"
                        + "  }\n"
                        + "}");

        TransactionReceipt transactionReceipt =
                new TransactionReceipt(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238",
                        "0x1",
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        "0xb",
                        "0x33bc",
                        "0x4dc",
                        "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                        "9307ba10e41ecf3d40507fc908655fe72fc129d46f6d99baf7605d0e29184911",
                        null,
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                        Arrays.asList(
                                new Log(
                                        false,
                                        "0x1",
                                        "0x0",
                                        "0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf",
                                        "0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x1b4",
                                        "0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                                        "mined",
                                        Arrays.asList(
                                                "0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5"))),
                        "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                        null,
                        null,
                        null);

        EthGetTransactionReceipt ethGetTransactionReceipt =
                deserialiseResponse(EthGetTransactionReceipt.class);
        assertEquals(ethGetTransactionReceipt.getTransactionReceipt().get(), (transactionReceipt));
    }

    @Test
    void testEthGetBlockReceipts() {
        buildResponse(
                "{\n"
                        + "    \"jsonrpc\": \"2.0\",\n"
                        + "    \"id\": 1,\n"
                        + "    \"result\": [\n"
                        + "        {\n"
                        + "            \"blockHash\": \"0x8e38b4dbf6b11fcc3b9dee84fb7986e29ca0a02cecd8977c161ff7333329681e\",\n"
                        + "            \"blockNumber\": \"0xf4240\",\n"
                        + "            \"contractAddress\": null,\n"
                        + "            \"cumulativeGasUsed\": \"0x723c\",\n"
                        + "            \"effectiveGasPrice\": \"0x12bfb19e60\",\n"
                        + "            \"from\": \"0x39fa8c5f2793459d6622857e7d9fbb4bd91766d3\",\n"
                        + "            \"gasUsed\": \"0x723c\",\n"
                        + "            \"logs\": [\n"
                        + "                {\n"
                        + "                    \"address\": \"0xc083e9947cf02b8ffc7d3090ae9aea72df98fd47\",\n"
                        + "                    \"topics\": [\n"
                        + "                        \"0xe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c\"\n"
                        + "                    ],\n"
                        + "                    \"data\": \"0x00000000000000000000000039fa8c5f2793459d6622857e7d9fbb4bd91766d30000000000000000000000000000000000000000000000056bc75e2d63100000\",\n"
                        + "                    \"blockNumber\": \"0xf4240\",\n"
                        + "                    \"transactionHash\": \"0xea1093d492a1dcb1bef708f771a99a96ff05dcab81ca76c31940300177fcf49f\",\n"
                        + "                    \"transactionIndex\": \"0x0\",\n"
                        + "                    \"blockHash\": \"0x8e38b4dbf6b11fcc3b9dee84fb7986e29ca0a02cecd8977c161ff7333329681e\",\n"
                        + "                    \"logIndex\": \"0x0\",\n"
                        + "                    \"removed\": false\n"
                        + "                }\n"
                        + "            ],\n"
                        + "            \"logsBloom\": \"0x00000000000000000000000000000000000800000000000000000000000800000000000000000400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000\",\n"
                        + "            \"status\": \"0x1\",\n"
                        + "            \"to\": \"0xc083e9947cf02b8ffc7d3090ae9aea72df98fd47\",\n"
                        + "            \"transactionHash\": \"0xea1093d492a1dcb1bef708f771a99a96ff05dcab81ca76c31940300177fcf49f\",\n"
                        + "            \"transactionIndex\": \"0x0\",\n"
                        + "            \"type\": \"0x0\"\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"blockHash\": \"0x8e38b4dbf6b11fcc3b9dee84fb7986e29ca0a02cecd8977c161ff7333329681e\",\n"
                        + "            \"blockNumber\": \"0xf4240\",\n"
                        + "            \"contractAddress\": null,\n"
                        + "            \"cumulativeGasUsed\": \"0xc444\",\n"
                        + "            \"effectiveGasPrice\": \"0xdf8475800\",\n"
                        + "            \"from\": \"0x32be343b94f860124dc4fee278fdcbd38c102d88\",\n"
                        + "            \"gasUsed\": \"0x5208\",\n"
                        + "            \"logs\": [],\n"
                        + "            \"logsBloom\": \"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "            \"status\": \"0x1\",\n"
                        + "            \"to\": \"0xdf190dc7190dfba737d7777a163445b7fff16133\",\n"
                        + "            \"transactionHash\": \"0xe9e91f1ee4b56c0df2e9f06c2b8c27c6076195a88a7b8537ba8313d80e6f124e\",\n"
                        + "            \"transactionIndex\": \"0x1\",\n"
                        + "            \"type\": \"0x0\"\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}");
        List<TransactionReceipt> transactionReceipts =
                Arrays.asList(
                        new TransactionReceipt(
                                "0xea1093d492a1dcb1bef708f771a99a96ff05dcab81ca76c31940300177fcf49f",
                                "0x0",
                                "0x8e38b4dbf6b11fcc3b9dee84fb7986e29ca0a02cecd8977c161ff7333329681e",
                                "0xf4240",
                                "0x723c",
                                "0x723c",
                                null,
                                null,
                                "0x1",
                                "0x39fa8c5f2793459d6622857e7d9fbb4bd91766d3",
                                "0xc083e9947cf02b8ffc7d3090ae9aea72df98fd47",
                                Arrays.asList(
                                        new Log(
                                                false,
                                                "0x0",
                                                "0x0",
                                                "0xea1093d492a1dcb1bef708f771a99a96ff05dcab81ca76c31940300177fcf49f",
                                                "0x8e38b4dbf6b11fcc3b9dee84fb7986e29ca0a02cecd8977c161ff7333329681e",
                                                "0xf4240",
                                                "0xc083e9947cf02b8ffc7d3090ae9aea72df98fd47",
                                                "0x00000000000000000000000039fa8c5f2793459d6622857e7d9fbb4bd91766d30000000000000000000000000000000000000000000000056bc75e2d63100000",
                                                null,
                                                Arrays.asList(
                                                        "0xe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c"))),
                                "0x00000000000000000000000000000000000800000000000000000000000800000000000000000400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000",
                                null,
                                "0x0",
                                "0x12bfb19e60"),
                        new TransactionReceipt(
                                "0xe9e91f1ee4b56c0df2e9f06c2b8c27c6076195a88a7b8537ba8313d80e6f124e",
                                "0x1",
                                "0x8e38b4dbf6b11fcc3b9dee84fb7986e29ca0a02cecd8977c161ff7333329681e",
                                "0xf4240",
                                "0xc444",
                                "0x5208",
                                null,
                                null,
                                "0x1",
                                "0x32be343b94f860124dc4fee278fdcbd38c102d88",
                                "0xdf190dc7190dfba737d7777a163445b7fff16133",
                                Arrays.asList(),
                                "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                                null,
                                "0x0",
                                "0xdf8475800"));
        EthGetBlockReceipts ethGetBlockReceipts = deserialiseResponse(EthGetBlockReceipts.class);

        assertEquals(ethGetBlockReceipts.getBlockReceipts().get(), (transactionReceipts));
    }

    @Test
    void testeEthGetTransactionReceiptAfterByzantium() {

        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": {\n"
                        + "        \"transactionHash\": \"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"blockHash\": \"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0xb\",\n"
                        + "        \"cumulativeGasUsed\": \"0x33bc\",\n"
                        + "        \"effectiveGasPrice\": null,\n"
                        + "        \"type\": null,\n"
                        + "        \"gasUsed\": \"0x4dc\",\n"
                        + "        \"contractAddress\": \"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\n"
                        + "        \"status\": \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"logs\": [{\n"
                        + "            \"removed\": false,\n"
                        + "            \"logIndex\": \"0x1\",\n"
                        + "            \"transactionIndex\": \"0x0\",\n"
                        + "            \"transactionHash\": \"0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf\",\n"
                        + "            \"blockHash\": \"0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"blockNumber\":\"0x1b4\",\n"
                        + "            \"address\": \"0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "            \"type\":\"mined\",\n"
                        + "            \"topics\": [\"0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5\"]"
                        + "        }],\n"
                        + "        \"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\"\n"
                        + "  }\n"
                        + "}");

        TransactionReceipt transactionReceipt =
                new TransactionReceipt(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238",
                        "0x1",
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        "0xb",
                        "0x33bc",
                        "0x4dc",
                        "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                        null,
                        "0x1",
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                        Arrays.asList(
                                new Log(
                                        false,
                                        "0x1",
                                        "0x0",
                                        "0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf",
                                        "0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x1b4",
                                        "0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                                        "mined",
                                        Arrays.asList(
                                                "0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5"))),
                        "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                        null,
                        null,
                        null);

        EthGetTransactionReceipt ethGetTransactionReceipt =
                deserialiseResponse(EthGetTransactionReceipt.class);
        assertEquals(ethGetTransactionReceipt.getTransactionReceipt().get(), (transactionReceipt));
    }

    @Test
    void testeEthGetTransactionReceiptAfterEIP4844() {

        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": {\n"
                        + "        \"transactionHash\": \"0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238\",\n"
                        + "        \"transactionIndex\":  \"0x1\",\n"
                        + "        \"blockHash\": \"0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b\",\n"
                        + "        \"blockNumber\": \"0xb\",\n"
                        + "        \"cumulativeGasUsed\": \"0x33bc\",\n"
                        + "        \"effectiveGasPrice\": null,\n"
                        + "        \"type\": null,\n"
                        + "        \"gasUsed\": \"0x4dc\",\n"
                        + "        \"contractAddress\": \"0xb60e8dd61c5d32be8058bb8eb970870f07233155\",\n"
                        + "        \"status\": \"0x1\",\n"
                        + "        \"from\":\"0x407d73d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"to\":\"0x85h43d8a49eeb85d32cf465507dd71d507100c1\",\n"
                        + "        \"logs\": [{\n"
                        + "            \"removed\": false,\n"
                        + "            \"logIndex\": \"0x1\",\n"
                        + "            \"transactionIndex\": \"0x0\",\n"
                        + "            \"transactionHash\": \"0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf\",\n"
                        + "            \"blockHash\": \"0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"blockNumber\":\"0x1b4\",\n"
                        + "            \"address\": \"0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "            \"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "            \"type\":\"mined\",\n"
                        + "            \"topics\": [\"0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5\"]"
                        + "        }],\n"
                        + "        \"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "        \"blobGasPrice\": \"0x1abcd\",\n"
                        + "        \"blobGasUsed\": \"0x2dcba\"\n"
                        + "  }\n"
                        + "}");

        TransactionReceipt transactionReceipt =
                new TransactionReceipt(
                        "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238",
                        "0x1",
                        "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b",
                        "0xb",
                        "0x33bc",
                        "0x4dc",
                        "0xb60e8dd61c5d32be8058bb8eb970870f07233155",
                        null,
                        "0x1",
                        "0x407d73d8a49eeb85d32cf465507dd71d507100c1",
                        "0x85h43d8a49eeb85d32cf465507dd71d507100c1",
                        Arrays.asList(
                                new Log(
                                        false,
                                        "0x1",
                                        "0x0",
                                        "0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf",
                                        "0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x1b4",
                                        "0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                        "0x0000000000000000000000000000000000000000000000000000000000000000",
                                        "mined",
                                        Arrays.asList(
                                                "0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5"))),
                        "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                        null,
                        null,
                        null,
                        "0x1abcd",
                        "0x2dcba");

        EthGetTransactionReceipt ethGetTransactionReceipt =
                deserialiseResponse(EthGetTransactionReceipt.class);
        assertEquals(ethGetTransactionReceipt.getTransactionReceipt().get(), (transactionReceipt));
    }

    @Test
    void testTransactionReceiptIsStatusOK() {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setStatus("0x1");
        assertEquals(transactionReceipt.isStatusOK(), (true));

        TransactionReceipt transactionReceiptNoStatus = new TransactionReceipt();
        assertEquals(transactionReceiptNoStatus.isStatusOK(), (true));

        TransactionReceipt transactionReceiptZeroStatus = new TransactionReceipt();
        transactionReceiptZeroStatus.setStatus("0x0");
        assertEquals(transactionReceiptZeroStatus.isStatusOK(), (false));
    }

    @Test
    void testEthGetCompilers() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": [\"solidity\", \"lll\", \"serpent\"]\n"
                        + "}");

        EthGetCompilers ethGetCompilers = deserialiseResponse(EthGetCompilers.class);
        assertEquals(ethGetCompilers.getCompilers(), (Arrays.asList("solidity", "lll", "serpent")));
    }

    @Test
    void testEthCompileSolidity() {

        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": {\n"
                        + "    \"test\": {\n"
                        + "      \"code\": \"0x605280600c6000396000f3006000357c010000000000000000000000000000000000000000000000000000000090048063c6888fa114602e57005b60376004356041565b8060005260206000f35b6000600782029050604d565b91905056\",\n"
                        + "      \"info\": {\n"
                        + "        \"source\": \"contract test {\\n\\tfunction multiply(uint a) pure returns(uint d) {\\n\\t\\treturn a * 7;\\n\\t}\\n}\\n\",\n"
                        + "        \"language\": \"Solidity\",\n"
                        + "        \"languageVersion\": \"0\",\n"
                        + "        \"compilerVersion\": \"0.8.2\",\n"
                        + "        \"compilerOptions\": \"--bin --abi --userdoc --devdoc --add-std --optimize -o /var/folders/3m/_6gnl12n1tj_5kf7sc3d72dw0000gn/T/solc498936951\",\n"
                        + "        \"abiDefinition\": [\n"
                        + "          {\n"
                        + "            \"inputs\": [\n"
                        + "              {\n"
                        + "                \"name\": \"a\",\n"
                        + "                \"type\": \"uint256\"\n"
                        + "              }\n"
                        + "            ],\n"
                        + "            \"name\": \"multiply\",\n"
                        + "            \"outputs\": [\n"
                        + "              {\n"
                        + "                \"name\": \"d\",\n"
                        + "                \"type\": \"uint256\"\n"
                        + "              }\n"
                        + "            ],\n"
                        + "            \"type\": \"function\",\n"
                        + "            \"stateMutability\": \"pure\"\n"
                        + "          }\n"
                        + "        ],\n"
                        + "        \"userDoc\": {\n"
                        + "          \"methods\": {}\n"
                        + "        },\n"
                        + "        \"developerDoc\": {\n"
                        + "          \"methods\": {}\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "    }"
                        + "  }\n"
                        + "}");

        Map<String, EthCompileSolidity.Code> compiledSolidity = new HashMap<>(1);
        compiledSolidity.put(
                "test",
                new EthCompileSolidity.Code(
                        "0x605280600c6000396000f3006000357c010000000000000000000000000000000000000000000000000000000090048063c6888fa114602e57005b60376004356041565b8060005260206000f35b6000600782029050604d565b91905056",
                        new EthCompileSolidity.SolidityInfo(
                                "contract test {\n\tfunction multiply(uint a) pure returns(uint d) {\n"
                                        + "\t\treturn a * 7;\n\t}\n}\n",
                                "Solidity",
                                "0",
                                "0.8.2",
                                "--bin --abi --userdoc --devdoc --add-std --optimize -o "
                                        + "/var/folders/3m/_6gnl12n1tj_5kf7sc3d72dw0000gn/T/solc498936951",
                                Arrays.asList(
                                        new AbiDefinition(
                                                true,
                                                Arrays.asList(
                                                        new AbiDefinition.NamedType(
                                                                "a", "uint256")),
                                                "multiply",
                                                Arrays.asList(
                                                        new AbiDefinition.NamedType(
                                                                "d", "uint256")),
                                                "function",
                                                false,
                                                "pure")),
                                new EthCompileSolidity.Documentation(),
                                new EthCompileSolidity.Documentation())));

        EthCompileSolidity ethCompileSolidity = deserialiseResponse(EthCompileSolidity.class);
        assertEquals(ethCompileSolidity.getCompiledSolidity(), (compiledSolidity));
    }

    @Test
    void testEthCompileLLL() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x603880600c6000396000f3006001600060e060020a60003504806"
                        + "3c6888fa114601857005b6021600435602b565b8060005260206000f35b600081600702"
                        + "905091905056\"\n"
                        + "}");

        EthCompileLLL ethCompileLLL = deserialiseResponse(EthCompileLLL.class);
        assertEquals(
                ethCompileLLL.getCompiledSourceCode(),
                ("0x603880600c6000396000f3006001600060e060020a600035048063c6888fa114601857005b60"
                        + "21600435602b565b8060005260206000f35b600081600702905091905056"));
    }

    @Test
    void testEthCompileSerpent() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x603880600c6000396000f3006001600060e060020a60003504806"
                        + "3c6888fa114601857005b6021600435602b565b8060005260206000f35b600081600702"
                        + "905091905056\"\n"
                        + "}");

        EthCompileSerpent ethCompileSerpent = deserialiseResponse(EthCompileSerpent.class);
        assertEquals(
                ethCompileSerpent.getCompiledSourceCode(),
                ("0x603880600c6000396000f3006001600060e060020a600035048063c6888fa114601857005b60"
                        + "21600435602b565b8060005260206000f35b600081600702905091905056"));
    }

    @Test
    void testEthFilter() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"0x1\"\n"
                        + "}");

        EthFilter ethFilter = deserialiseResponse(EthFilter.class);
        assertEquals(ethFilter.getFilterId(), (BigInteger.valueOf(1)));
    }

    @Test
    void testEthUninstallFilter() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        EthUninstallFilter ethUninstallFilter = deserialiseResponse(EthUninstallFilter.class);
        assertEquals(ethUninstallFilter.isUninstalled(), (true));
    }

    @Test
    void testEthLog() {

        buildResponse(
                "{\n"
                        + "    \"id\":1,\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"result\": [{\n"
                        + "        \"removed\": false,\n"
                        + "        \"logIndex\": \"0x1\",\n"
                        + "        \"transactionIndex\": \"0x0\",\n"
                        + "        \"transactionHash\": \"0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf\",\n"
                        + "        \"blockHash\": \"0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "        \"blockNumber\":\"0x1b4\",\n"
                        + "        \"address\": \"0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d\",\n"
                        + "        \"data\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\n"
                        + "        \"type\":\"mined\",\n"
                        + "        \"topics\": [\"0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5\"]"
                        + "    }]"
                        + "}");

        List<Log> logs =
                Collections.singletonList(
                        new EthLog.LogObject(
                                false,
                                "0x1",
                                "0x0",
                                "0xdf829c5a142f1fccd7d8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcf",
                                "0x8216c5785ac562ff41e2dcfdf5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                "0x1b4",
                                "0x16c5785ac562ff41e2dcfdf829c5a142f1fccd7d",
                                "0x0000000000000000000000000000000000000000000000000000000000000000",
                                "mined",
                                Collections.singletonList(
                                        "0x59ebeb90bc63057b6515673c3ecf9438e5058bca0f92585014eced636878c9a5")));

        EthLog ethLog = deserialiseResponse(EthLog.class);
        assertEquals(ethLog.getLogs(), (logs));
    }

    @Test
    void testEthGetProof() {
        buildResponse(
                "{\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"id\": 1,\n"
                        + "  \"result\": {\n"
                        + "    \"address\": \"0x7f0d15c7faae65896648c8273b6d7e43f58fa842\",\n"
                        + "    \"accountProof\": [\n"
                        + "      \"0xf90211a08245f766623948f962b9f277955e4be3868d107042195746e5bb9b60e45c4a35a0f95891987a66390224caca18cf15d40288e85a58fee02b61b4e235a95844678ca04c783e1622a91a7cd4d06e82e654cc8bcd5b00a545889ff729f35ccd8387f5e3a08e28b463ee2ada55e1647c4c502976e5c09dcd94828e78d0ad09ab94db80a14aa0a41aaedbede9a14677796971bbe97ac152c4101471d62786208cf54bb7763818a02bc95a8f57339e1262460c02e885ab2fc03762a7a646693b659e64889522ca3aa075772fb16c66643216c7ab93eb2c96d28142b8c99573f4fcdce41b9064a303d1a0da840a0f8f62bf404e1241139fb6d6eedf56e5ea7fed9e79001bbc31806d7168a0550099050860af73557452539c5bd055324400969f76b4643b531a8fef7c4ad2a02de16af6ab3f4205295bdd828e81c97930413ee3dd8e0aa76268909492c0f646a0ab2f8ba58a07310e12c7140427278f5715cbf975b735bac5d2338c487b2b615ca004725301c50e47bfa03c8495b5f009ee5aac8b15581dc6e9f5870c63e8fcc118a0cbd9efe9133d519ca35327c90c3b102972967e35e4432e4d0ce8416311267b7ca04e8d4cd1b3d110571ed885ccd470458a86e2dcf1fd083f1c2b9118001d4fc89ea03c6135b75f89e3b5c24c1354638e1a10c2248e5d7bc87df2d8230910660d3d84a0186eea2111408bc02272677443c21e2533146a56785a6bc34f9180e2ba4b567880\",\n"
                        + "      \"0xf90211a0ebfa45ac468954fd9f1e4d5c8b5b0015500b7e9f083e1626e05a03069d2d7b67a077e18486d90836a1816056a6fa8ed0339e15cf937ea87841e035ab26fd39057ea06e8c3b073ed5d7e86a205f0011f726b0a87f011b4e6e0521a287d378eda627a5a0b1decdb542bf89924e1022d9ab05e3dd9e2aca2f32f43489ce5723ddbe8b7a4fa0223dd0aa6c255a7b5a1e4b9753eacf599db92476baf674136f39b808d084991da0cbd89f24d151b9b23b147bf1bb71006d8661b19a8171cf26bb20842944a25ff1a0a380efa4b380cf41b0af35bb7a4f68c1a3988f9aa62c1a4c3c0bfd72d6c588c2a0c76f535a14cd7d869f5f01054cc72b827bd1c7b11df30138b98421a7c331d5a8a0d5b25d3d8a6c82f60c6eab724afed222f575824789d2d9f0768d46be39b7cbd1a0e2b2326a7ee7c454bde59d682367af495756b3c11408b7d97e3e84936ef4c5a1a00bb7e8a57023a0926c6683cd2a9a530b29d26d75d38872aeec1a21100b77fecba0288d0834990caaf30d2c6dfff07e52615e16c463e7256fab04ede25899d032f9a0dbb8748605aeb933068490238594afa7d93e018d04712d3d6af29fac9c81620fa0bae1aa9abe2c40e45f3274edeaba95f81a7e91da98c746bf31b21c280f100682a0c726027419c64dba7e2aeb5d1423b1fad6e773cb604668dc044671d5d5b18430a0cacd3a2b6c933e9a2148c7314d294d2f7356bb17900ac3b4faccd7f561cd6f1d80\",\n"
                        + "      \"0xf90211a0d26a9a18bdeb7e2387f9d9375bbc1b987c45a58f3eaaa4d29df611f0618c17d4a0839e8f34f581129bf52277d6aab412bc9794866fa22e93938fffbf85896b067ba008d315b1fbb99070a424b7efdeb3a8a90d631e325dc48f5f8c36b67ca0244081a0c1436f0a1e74a33600455326571d9549e904724d2646c0c86bed0e226a687bd1a05699909cad5f4b507001ce88a887af4148fb7ee14ed0f93c7aa9635b66407f9da0cb5f6eb1cc89cef0e221aeef566c891fa46f456370d52a8b322e4917c2f9ad87a0da1425f08bbf9d0368d159cd810f33c2b19a6e5d0b15043dfac565cf03b57c77a0b26393ad5305e312a3e388deb50bec841bbcbec45317ffb7753114f3d6d37ff9a0c24f396b4ae01cb32a72880baff2db25843979103c3a4d1116698416e29439c4a0d6738cafb3d29dd438a2bf074d9b991636535b83b41f589189801ce75e94c5b7a02f2c355b91c78a6d5386a874a092b1d8e1c740408afb532721c1af0e905de5e3a01511013c45279c74536a5e538251c4dfd7594848ed570b48615b094ca83849dda05e12cb9b86dc6c2c1e0dd68f8c2a93c83d2eedb5dbba3969a2b6c7032e26fc7ca0cfa01a6943b11ca2be19dfa3b23d535395f17db6ac1fe1dd5172643e8ccd68f7a002ae61b52a9a0b09c4ca9866b69e4f10765c1f7778e2d4f2300183868f5fd151a039dc21a690fbeed9ea699959be8b2e76eed93b9da21d12ee4254d0a1c51dc87380\",\n"
                        + "      \"0xf90211a05b44d7409dc085529b33905d01eb00210fb1c787998b88df5619fb6e3b860181a03bb3e5e3e8d979fd3bc86c602d6e6a57b1fc4c7f5f6a774b7e91af001cdef664a0a8fd61cceed0f8a639beca30fcd32411c0fcca5a102ff4a2656cde8c7eb858b4a0052e56ef09a9139a58d80329870ddc961e8ed112c8edc1325f71903fba84275ba0729ff34831623850f26d3601d7db1af2da69f115e9be8f9fa8454cc006b51403a09e56b2a68595be5b215cbc173a6e810137be55a748587eb81d172439018ee7aaa0fa5b27319522283b38e2d1abec5941b2d78e78fc40aa5a4b4476e36a0443311fa05b5f6d0058c46ea701f4ba42aabedb7b12e664a91f4b35c5519797df4c623f55a0947a7cbc568c23382aacbe508353c7f8ad299f3ea3eb471506c6b4291868b3e5a0f2313976f5c85685f419fc3022cf28fb9696c05ca27c44437f717f02e0154fbda01a96e6375ed8e8dc0749b2d45f93207d9219168a9eea142053a5e23e7a47232aa06f9d9145b92865e970f60dd81aa55ced05698090e9e23f341729be2a90ce7b96a01b6c084aad02ab8cfc5bdc0800b956db46853cfa8b9b47ff35867f02718f8130a0d7493eefde332cd1de8898096e9e8916cc7e320a6eb0ede2dc165fc5ed8803a1a0f0c6e212824552eee4b2479dae9b570041441e0cb0244d8bf045a73268aefda6a01e9045c2bc23b1610f20c3501862a067bfc80af7908344527c62faf1129fb43d80\",\n"
                        + "      \"0xf90211a056e7736ac44b0a6d46f4a6e40abf6426866f7f29ab0637bc89c390b4f0dc7807a07aec46d6e64a9847c400021f5e0f0373592e676b11d15cdf22765addf1033f01a09c356f78e4889e626568761468ca4e44ce0cc78f08a40035fc9be23b1c1990a0a0d18cfe2360f9559b663f4a8864995420b54e0c4c6c7f33019d2467b67080fed5a08b3d08ef8de3638edabb423c901a6c1ca8b1ef93b41427b2d409be7198a89225a0df689118041c8f4b2751afbf3cb7be36540e52742362df3ded6f67e3a8024feea0a7e5e17854690d418ef89eb6753e1d36e96cadc4a186bcd8ce71f8665fbe6814a0367bc2399eee7dffeaf8aea140a3973cbced2ed904aa7e6c69443a4877bb7115a0b1f51cb2cb73acdce7ee790854c03d59940e415b6598bda7fa753c0ae0e129dda021f400618bbd2b1ac65d863146f3497feaca7e73df4a3cf211b0a95ec5f39b5ea0494ec6c1615466d244e80992aee4b2496b4c8adda7f7b989e776a715a7a72b4aa03d64c83151b47fdf8c16ce7845306575108c3b8c6b3f9ce8032b65f05fb5d017a0c68bc1b17e6a32dd438aec2ca6c8beea5dd53a3192971bbc484036c9431ead88a0580a8ef71765615a19afd214553866e2215f94eb72631335fa8ec0c5ad450e19a04eda5daea47af109418ad18cae8c124e790d66046ce9389376c4a965f919c725a0a0a6553d3fc52e639eac9c20dbb7cd8930eacaa8b35f8b8f3bb23afb53cc966580\",\n"
                        + "      \"0xf90211a0597493b2845e511c876b84fad3925bb78c3922b3734208f20aff5461c7078e7ca0eb3723d662049847561ea31922b2ea5ab7a641ce7c14b5fa0ec346975d16c187a00e5c88ea26113f0295d8c682f1526f2fa7906bc422bfe9cc8b8f869686e179d4a0b94a30e2218afad53a0eacc505ea552a8288457847fcae72555aac638b52c46ea0d6430dd23ef9889e1272e494530d6ec89f080c49bf09f8a6ac634b153fa8ce5ca0e8c4de149f9b385e51fb66516a4d91b62dbd80ddedd4522e14d370da47f63e5aa0fa40fc95a8bdd7126c4db03b0c1a20c654aa22cf6f72024c4a0925d6f29fc9baa005c16a67b6ee21b8e9371064570b2f30342add57d7c45bc43df25fc680132186a0c2ed9529b58b672abd733db30ccac4e5ebeeedeb38418a5925e57b0f822df814a0d18e42474e815b62da57cf67bcd8ed1c90d4f8ecf8f6d0b2e66b9656be2686ada0e4dc3683f731f21c0c34a10d7ffac39b20a64ac326269433a5cacaeb90a8a17da0e3f8b04c275387c6222b6536967f2f1e6fc76dc6ee0e705d3e505f3d07e8bdd4a0deaf89533b13c20ff50ff3f03ff3a2daa8d1329a6a6d17a58c422cbe5ee2f80aa022b542269e108a24dc0ed74e62fc571185dcf398acfbf15a7fb6ce3e8aba50d6a0f29369f8c3d70bc9f472c2175ab5495edcf35a65527f67d391068c0275284307a0c93bd89abbea5ae96496a12d236323d11d2da9cde7a3b9fdb15d504f4ea1eda680\",\n"
                        + "      \"0xf90151a000595129cf1c67ce97ed615df7184fb47a0d018d9147a2a7952a639e464cdfd380a052081f67d885b439bee62a1c4e09c02a1ec729aef4d92be6584a29141a79c5c7a0d99cae2bc3b6fc3e8e2767b4d7a5884b9b2d9d20da7890318beefcdb9c7346d58080a0f63571735d99e763dafadb03d1abe3a93a98740ecddc8514d13df46a35a8d94680a081870f4697e57436ccdd8bdc1d624d7bc07861748ff8b3a352334df68f2c03ada0f0252504ee8753335ecf0ca1f73cf13d4505783ed5110bf4e9b384a68ad88f69a0d38a18ef7b993c9ad2d848453e7722c26998ff3d63e243b99cdab0dabf84157ea06e5ad479dbda7f1647ed613e35d837f5d2c697ef34160f3665173186553ee9c280a0bf8371cac505b6ea0cb0f67ef744713f306a2455f3f8844cfa527c62db59c69980a0d6d689405b1cf73786c5fdabb55ec22f9b5712d1ba9c0bfcd46d4b93721fcac780\",\n"
                        + "      \"0xf8669d37f29e142d49f8824d4e7f1735ec3da219687387629b5fccd86812df84b846f8440180a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a01d93f60f105899172f7255c030301c3af4564edd4a48577dbdc448aec7ddb0ac\"],\n"
                        + "    \"balance\": \"0x0\",\n"
                        + "    \"codeHash\": \"0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470\",\n"
                        + "    \"nonce\": \"0x0\",\n"
                        + "    \"storageHash\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"storageProof\": [{\n"
                        + "      \"key\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "      \"value\": \"0x0\",\n"
                        + "      \"proof\": []\n"
                        + "    }]\n"
                        + "  }\n"
                        + "}");

        EthGetProof ethGetProof = deserialiseResponse(EthGetProof.class);

        EthGetProof.Proof proof =
                new EthGetProof.Proof(
                        "0x7f0d15c7faae65896648c8273b6d7e43f58fa842",
                        "0x0",
                        "0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470",
                        "0x0",
                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                        Arrays.asList(
                                "0xf90211a08245f766623948f962b9f277955e4be3868d107042195746e5bb9b60e45c4a35a0f95891987a66390224caca18cf15d40288e85a58fee02b61b4e235a95844678ca04c783e1622a91a7cd4d06e82e654cc8bcd5b00a545889ff729f35ccd8387f5e3a08e28b463ee2ada55e1647c4c502976e5c09dcd94828e78d0ad09ab94db80a14aa0a41aaedbede9a14677796971bbe97ac152c4101471d62786208cf54bb7763818a02bc95a8f57339e1262460c02e885ab2fc03762a7a646693b659e64889522ca3aa075772fb16c66643216c7ab93eb2c96d28142b8c99573f4fcdce41b9064a303d1a0da840a0f8f62bf404e1241139fb6d6eedf56e5ea7fed9e79001bbc31806d7168a0550099050860af73557452539c5bd055324400969f76b4643b531a8fef7c4ad2a02de16af6ab3f4205295bdd828e81c97930413ee3dd8e0aa76268909492c0f646a0ab2f8ba58a07310e12c7140427278f5715cbf975b735bac5d2338c487b2b615ca004725301c50e47bfa03c8495b5f009ee5aac8b15581dc6e9f5870c63e8fcc118a0cbd9efe9133d519ca35327c90c3b102972967e35e4432e4d0ce8416311267b7ca04e8d4cd1b3d110571ed885ccd470458a86e2dcf1fd083f1c2b9118001d4fc89ea03c6135b75f89e3b5c24c1354638e1a10c2248e5d7bc87df2d8230910660d3d84a0186eea2111408bc02272677443c21e2533146a56785a6bc34f9180e2ba4b567880",
                                "0xf90211a0ebfa45ac468954fd9f1e4d5c8b5b0015500b7e9f083e1626e05a03069d2d7b67a077e18486d90836a1816056a6fa8ed0339e15cf937ea87841e035ab26fd39057ea06e8c3b073ed5d7e86a205f0011f726b0a87f011b4e6e0521a287d378eda627a5a0b1decdb542bf89924e1022d9ab05e3dd9e2aca2f32f43489ce5723ddbe8b7a4fa0223dd0aa6c255a7b5a1e4b9753eacf599db92476baf674136f39b808d084991da0cbd89f24d151b9b23b147bf1bb71006d8661b19a8171cf26bb20842944a25ff1a0a380efa4b380cf41b0af35bb7a4f68c1a3988f9aa62c1a4c3c0bfd72d6c588c2a0c76f535a14cd7d869f5f01054cc72b827bd1c7b11df30138b98421a7c331d5a8a0d5b25d3d8a6c82f60c6eab724afed222f575824789d2d9f0768d46be39b7cbd1a0e2b2326a7ee7c454bde59d682367af495756b3c11408b7d97e3e84936ef4c5a1a00bb7e8a57023a0926c6683cd2a9a530b29d26d75d38872aeec1a21100b77fecba0288d0834990caaf30d2c6dfff07e52615e16c463e7256fab04ede25899d032f9a0dbb8748605aeb933068490238594afa7d93e018d04712d3d6af29fac9c81620fa0bae1aa9abe2c40e45f3274edeaba95f81a7e91da98c746bf31b21c280f100682a0c726027419c64dba7e2aeb5d1423b1fad6e773cb604668dc044671d5d5b18430a0cacd3a2b6c933e9a2148c7314d294d2f7356bb17900ac3b4faccd7f561cd6f1d80",
                                "0xf90211a0d26a9a18bdeb7e2387f9d9375bbc1b987c45a58f3eaaa4d29df611f0618c17d4a0839e8f34f581129bf52277d6aab412bc9794866fa22e93938fffbf85896b067ba008d315b1fbb99070a424b7efdeb3a8a90d631e325dc48f5f8c36b67ca0244081a0c1436f0a1e74a33600455326571d9549e904724d2646c0c86bed0e226a687bd1a05699909cad5f4b507001ce88a887af4148fb7ee14ed0f93c7aa9635b66407f9da0cb5f6eb1cc89cef0e221aeef566c891fa46f456370d52a8b322e4917c2f9ad87a0da1425f08bbf9d0368d159cd810f33c2b19a6e5d0b15043dfac565cf03b57c77a0b26393ad5305e312a3e388deb50bec841bbcbec45317ffb7753114f3d6d37ff9a0c24f396b4ae01cb32a72880baff2db25843979103c3a4d1116698416e29439c4a0d6738cafb3d29dd438a2bf074d9b991636535b83b41f589189801ce75e94c5b7a02f2c355b91c78a6d5386a874a092b1d8e1c740408afb532721c1af0e905de5e3a01511013c45279c74536a5e538251c4dfd7594848ed570b48615b094ca83849dda05e12cb9b86dc6c2c1e0dd68f8c2a93c83d2eedb5dbba3969a2b6c7032e26fc7ca0cfa01a6943b11ca2be19dfa3b23d535395f17db6ac1fe1dd5172643e8ccd68f7a002ae61b52a9a0b09c4ca9866b69e4f10765c1f7778e2d4f2300183868f5fd151a039dc21a690fbeed9ea699959be8b2e76eed93b9da21d12ee4254d0a1c51dc87380",
                                "0xf90211a05b44d7409dc085529b33905d01eb00210fb1c787998b88df5619fb6e3b860181a03bb3e5e3e8d979fd3bc86c602d6e6a57b1fc4c7f5f6a774b7e91af001cdef664a0a8fd61cceed0f8a639beca30fcd32411c0fcca5a102ff4a2656cde8c7eb858b4a0052e56ef09a9139a58d80329870ddc961e8ed112c8edc1325f71903fba84275ba0729ff34831623850f26d3601d7db1af2da69f115e9be8f9fa8454cc006b51403a09e56b2a68595be5b215cbc173a6e810137be55a748587eb81d172439018ee7aaa0fa5b27319522283b38e2d1abec5941b2d78e78fc40aa5a4b4476e36a0443311fa05b5f6d0058c46ea701f4ba42aabedb7b12e664a91f4b35c5519797df4c623f55a0947a7cbc568c23382aacbe508353c7f8ad299f3ea3eb471506c6b4291868b3e5a0f2313976f5c85685f419fc3022cf28fb9696c05ca27c44437f717f02e0154fbda01a96e6375ed8e8dc0749b2d45f93207d9219168a9eea142053a5e23e7a47232aa06f9d9145b92865e970f60dd81aa55ced05698090e9e23f341729be2a90ce7b96a01b6c084aad02ab8cfc5bdc0800b956db46853cfa8b9b47ff35867f02718f8130a0d7493eefde332cd1de8898096e9e8916cc7e320a6eb0ede2dc165fc5ed8803a1a0f0c6e212824552eee4b2479dae9b570041441e0cb0244d8bf045a73268aefda6a01e9045c2bc23b1610f20c3501862a067bfc80af7908344527c62faf1129fb43d80",
                                "0xf90211a056e7736ac44b0a6d46f4a6e40abf6426866f7f29ab0637bc89c390b4f0dc7807a07aec46d6e64a9847c400021f5e0f0373592e676b11d15cdf22765addf1033f01a09c356f78e4889e626568761468ca4e44ce0cc78f08a40035fc9be23b1c1990a0a0d18cfe2360f9559b663f4a8864995420b54e0c4c6c7f33019d2467b67080fed5a08b3d08ef8de3638edabb423c901a6c1ca8b1ef93b41427b2d409be7198a89225a0df689118041c8f4b2751afbf3cb7be36540e52742362df3ded6f67e3a8024feea0a7e5e17854690d418ef89eb6753e1d36e96cadc4a186bcd8ce71f8665fbe6814a0367bc2399eee7dffeaf8aea140a3973cbced2ed904aa7e6c69443a4877bb7115a0b1f51cb2cb73acdce7ee790854c03d59940e415b6598bda7fa753c0ae0e129dda021f400618bbd2b1ac65d863146f3497feaca7e73df4a3cf211b0a95ec5f39b5ea0494ec6c1615466d244e80992aee4b2496b4c8adda7f7b989e776a715a7a72b4aa03d64c83151b47fdf8c16ce7845306575108c3b8c6b3f9ce8032b65f05fb5d017a0c68bc1b17e6a32dd438aec2ca6c8beea5dd53a3192971bbc484036c9431ead88a0580a8ef71765615a19afd214553866e2215f94eb72631335fa8ec0c5ad450e19a04eda5daea47af109418ad18cae8c124e790d66046ce9389376c4a965f919c725a0a0a6553d3fc52e639eac9c20dbb7cd8930eacaa8b35f8b8f3bb23afb53cc966580",
                                "0xf90211a0597493b2845e511c876b84fad3925bb78c3922b3734208f20aff5461c7078e7ca0eb3723d662049847561ea31922b2ea5ab7a641ce7c14b5fa0ec346975d16c187a00e5c88ea26113f0295d8c682f1526f2fa7906bc422bfe9cc8b8f869686e179d4a0b94a30e2218afad53a0eacc505ea552a8288457847fcae72555aac638b52c46ea0d6430dd23ef9889e1272e494530d6ec89f080c49bf09f8a6ac634b153fa8ce5ca0e8c4de149f9b385e51fb66516a4d91b62dbd80ddedd4522e14d370da47f63e5aa0fa40fc95a8bdd7126c4db03b0c1a20c654aa22cf6f72024c4a0925d6f29fc9baa005c16a67b6ee21b8e9371064570b2f30342add57d7c45bc43df25fc680132186a0c2ed9529b58b672abd733db30ccac4e5ebeeedeb38418a5925e57b0f822df814a0d18e42474e815b62da57cf67bcd8ed1c90d4f8ecf8f6d0b2e66b9656be2686ada0e4dc3683f731f21c0c34a10d7ffac39b20a64ac326269433a5cacaeb90a8a17da0e3f8b04c275387c6222b6536967f2f1e6fc76dc6ee0e705d3e505f3d07e8bdd4a0deaf89533b13c20ff50ff3f03ff3a2daa8d1329a6a6d17a58c422cbe5ee2f80aa022b542269e108a24dc0ed74e62fc571185dcf398acfbf15a7fb6ce3e8aba50d6a0f29369f8c3d70bc9f472c2175ab5495edcf35a65527f67d391068c0275284307a0c93bd89abbea5ae96496a12d236323d11d2da9cde7a3b9fdb15d504f4ea1eda680",
                                "0xf90151a000595129cf1c67ce97ed615df7184fb47a0d018d9147a2a7952a639e464cdfd380a052081f67d885b439bee62a1c4e09c02a1ec729aef4d92be6584a29141a79c5c7a0d99cae2bc3b6fc3e8e2767b4d7a5884b9b2d9d20da7890318beefcdb9c7346d58080a0f63571735d99e763dafadb03d1abe3a93a98740ecddc8514d13df46a35a8d94680a081870f4697e57436ccdd8bdc1d624d7bc07861748ff8b3a352334df68f2c03ada0f0252504ee8753335ecf0ca1f73cf13d4505783ed5110bf4e9b384a68ad88f69a0d38a18ef7b993c9ad2d848453e7722c26998ff3d63e243b99cdab0dabf84157ea06e5ad479dbda7f1647ed613e35d837f5d2c697ef34160f3665173186553ee9c280a0bf8371cac505b6ea0cb0f67ef744713f306a2455f3f8844cfa527c62db59c69980a0d6d689405b1cf73786c5fdabb55ec22f9b5712d1ba9c0bfcd46d4b93721fcac780",
                                "0xf8669d37f29e142d49f8824d4e7f1735ec3da219687387629b5fccd86812df84b846f8440180a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a01d93f60f105899172f7255c030301c3af4564edd4a48577dbdc448aec7ddb0ac"),
                        // String key, String value, List<String> proof
                        Arrays.asList(
                                new EthGetProof.StorageProof(
                                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                                        "0x0",
                                        new ArrayList<>())));
        assertEquals(proof, ethGetProof.getProof());
    }

    @Test
    void testEthGetWork() {

        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": [\n"
                        + "      \"0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef\",\n"
                        + "      \"0x5EED00000000000000000000000000005EED0000000000000000000000000000\",\n"
                        + "      \"0xd1ff1c01710000000000000000000000d1ff1c01710000000000000000000000\"\n"
                        + "    ]\n"
                        + "}");

        EthGetWork ethGetWork = deserialiseResponse(EthGetWork.class);
        assertEquals(
                ethGetWork.getCurrentBlockHeaderPowHash(),
                ("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"));
        assertEquals(
                ethGetWork.getSeedHashForDag(),
                ("0x5EED00000000000000000000000000005EED0000000000000000000000000000"));
        assertEquals(
                ethGetWork.getBoundaryCondition(),
                ("0xd1ff1c01710000000000000000000000d1ff1c01710000000000000000000000"));
    }

    @Test
    void testEthSubmitWork() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        EthSubmitWork ethSubmitWork = deserialiseResponse(EthSubmitWork.class);
        assertEquals(ethSubmitWork.solutionValid(), (true));
    }

    @Test
    void testEthSubmitHashrate() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        EthSubmitHashrate ethSubmitHashrate = deserialiseResponse(EthSubmitHashrate.class);
        assertEquals(ethSubmitHashrate.submissionSuccessful(), (true));
    }

    @Test
    void testDbPutString() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        DbPutString dbPutString = deserialiseResponse(DbPutString.class);
        assertEquals(dbPutString.valueStored(), (true));
    }

    @Test
    void testDbGetString() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"myString\"\n"
                        + "}");

        DbGetString dbGetString = deserialiseResponse(DbGetString.class);
        assertEquals(dbGetString.getStoredValue(), ("myString"));
    }

    @Test
    void testDbPutHex() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        DbPutHex dbPutHex = deserialiseResponse(DbPutHex.class);
        assertEquals(dbPutHex.valueStored(), (true));
    }

    @Test
    void testDbGetHex() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"0x68656c6c6f20776f726c64\"\n"
                        + "}");

        DbGetHex dbGetHex = deserialiseResponse(DbGetHex.class);
        assertEquals(dbGetHex.getStoredValue(), ("0x68656c6c6f20776f726c64"));
    }

    @Test
    void testSshVersion() {
        buildResponse(
                "{\n"
                        + "  \"id\":67,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": \"2\"\n"
                        + "}");

        ShhVersion shhVersion = deserialiseResponse(ShhVersion.class);
        assertEquals(shhVersion.getVersion(), ("2"));
    }

    @Test
    void testSshPost() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        ShhPost shhPost = deserialiseResponse(ShhPost.class);
        assertEquals(shhPost.messageSent(), (true));
    }

    @Test
    void testSshNewIdentity() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0xc931d93e97ab07fe42d923478ba2465f283f440fd6cabea4dd7a2c807108f651b713"
                        + "5d1d6ca9007d5b68aa497e4619ac10aa3b27726e1863c1fd9b570d99bbaf\"\n"
                        + "}");

        ShhNewIdentity shhNewIdentity = deserialiseResponse(ShhNewIdentity.class);
        assertEquals(
                shhNewIdentity.getAddress(),
                ("0xc931d93e97ab07fe42d923478ba2465f283f440fd6cabea4dd7a2c807108f651b7135d1d6ca9"
                        + "007d5b68aa497e4619ac10aa3b27726e1863c1fd9b570d99bbaf"));
    }

    @Test
    void testSshHasIdentity() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        ShhHasIdentity shhHasIdentity = deserialiseResponse(ShhHasIdentity.class);
        assertEquals(shhHasIdentity.hasPrivateKeyForIdentity(), (true));
    }

    @Test
    void testSshNewGroup() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": "
                        + "\"0xc65f283f440fd6cabea4dd7a2c807108f651b7135d1d6ca90931d93e97ab07fe42d9"
                        + "23478ba2407d5b68aa497e4619ac10aa3b27726e1863c1fd9b570d99bbaf\"\n"
                        + "}");

        ShhNewGroup shhNewGroup = deserialiseResponse(ShhNewGroup.class);
        assertEquals(
                shhNewGroup.getAddress(),
                ("0xc65f283f440fd6cabea4dd7a2c807108f651b7135d1d6ca90931d93e97ab07fe42d923478ba24"
                        + "07d5b68aa497e4619ac10aa3b27726e1863c1fd9b570d99bbaf"));
    }

    @Test
    void testSshAddToGroup() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        ShhAddToGroup shhAddToGroup = deserialiseResponse(ShhAddToGroup.class);
        assertEquals(shhAddToGroup.addedToGroup(), (true));
    }

    @Test
    void testSshNewFilter() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": \"0x7\"\n"
                        + "}");

        ShhNewFilter shhNewFilter = deserialiseResponse(ShhNewFilter.class);
        assertEquals(shhNewFilter.getFilterId(), (BigInteger.valueOf(7)));
    }

    @Test
    void testSshUninstallFilter() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": true\n"
                        + "}");

        ShhUninstallFilter shhUninstallFilter = deserialiseResponse(ShhUninstallFilter.class);
        assertEquals(shhUninstallFilter.isUninstalled(), (true));
    }

    @Test
    void testSshMessages() {
        buildResponse(
                "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": [{\n"
                        + "    \"hash\": \"0x33eb2da77bf3527e28f8bf493650b1879b08c4f2a362beae4ba2f"
                        + "71bafcd91f9\",\n"
                        + "    \"from\": \"0x3ec052fc33...\",\n"
                        + "    \"to\": \"0x87gdf76g8d7fgdfg...\",\n"
                        + "    \"expiry\": \"0x54caa50a\",\n"
                        + "    \"ttl\": \"0x64\",\n"
                        + "    \"sent\": \"0x54ca9ea2\",\n"
                        + "    \"topics\": [\"0x6578616d\"],\n"
                        + "    \"payload\": \"0x7b2274797065223a226d657373616765222c2263686...\",\n"
                        + "    \"workProved\": \"0x0\"\n"
                        + "    }]\n"
                        + "}");

        List<ShhMessages.SshMessage> messages =
                Arrays.asList(
                        new ShhMessages.SshMessage(
                                "0x33eb2da77bf3527e28f8bf493650b1879b08c4f2a362beae4ba2f71bafcd91f9",
                                "0x3ec052fc33...",
                                "0x87gdf76g8d7fgdfg...",
                                "0x54caa50a",
                                "0x64",
                                "0x54ca9ea2",
                                Arrays.asList("0x6578616d"),
                                "0x7b2274797065223a226d657373616765222c2263686...",
                                "0x0"));

        ShhMessages shhMessages = deserialiseResponse(ShhMessages.class);
        assertEquals(shhMessages.getMessages(), (messages));
    }

    @Test
    void testBooleanResponse() {
        buildResponse(
                "{\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"id\":22,\n"
                        + "    \"result\":true\n"
                        + "}");

        BooleanResponse booleanResponse = deserialiseResponse(BooleanResponse.class);
        assertTrue(booleanResponse.success());
    }

    @Test
    void testAdminDataDir() {
        buildResponse(
                "{\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"id\":22,\n"
                        + "    \"result\":\"sampleDir\"\n"
                        + "}");

        AdminDataDir dataDir = deserialiseResponse(AdminDataDir.class);
        assertEquals(dataDir.getDataDir(), "sampleDir");
    }

    @Test
    void testTxPoolStatus() {
        buildResponse(
                "{\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"id\":22,\n"
                        + "    \"result\":{ \"pending\": \"10\",\n"
                        + "			\"queued\": \"7\"}\n"
                        + "}");

        TxPoolStatus status = deserialiseResponse(TxPoolStatus.class);
        assertEquals(10, status.getPending());
        assertEquals(7, status.getQueued());
    }

    @Test
    void testLineaEstimateGas() {
        buildResponse(
                "{\n"
                        + "    \"jsonrpc\":\"2.0\",\n"
                        + "    \"id\":22,\n"
                        + "    \"result\":{ \"baseFeePerGas\": \"0x7\",\n"
                        + "    \"gasLimit\": \"0xcf08\",\n"
                        + "			\"priorityFeePerGas\": \"0x43a82a4\"}\n"
                        + "}");

        LineaEstimateGas lineaEstimateGas = deserialiseResponse(LineaEstimateGas.class);
        assertEquals(
                lineaEstimateGas.getLineaEstimateGas().getGasLimit(), (BigInteger.valueOf(53000)));
        assertEquals(
                lineaEstimateGas.getLineaEstimateGas().getBaseFeePerGas(), (BigInteger.valueOf(7)));
        assertEquals(
                lineaEstimateGas.getLineaEstimateGas().getPriorityFeePerGas(),
                (BigInteger.valueOf(70943396)));
    }

    @Test
    void testLineaGetProof() {
        buildResponse(
                "{\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"id\": 1,\n"
                        + "  \"result\": {\n"
                        + "    \"address\": \"0x7f0d15c7faae65896648c8273b6d7e43f58fa842\",\n"
                        + "    \"accountProof\": [\n"
                        + "      \"0xf90211a08245f766623948f962b9f277955e4be3868d107042195746e5bb9b60e45c4a35a0f95891987a66390224caca18cf15d40288e85a58fee02b61b4e235a95844678ca04c783e1622a91a7cd4d06e82e654cc8bcd5b00a545889ff729f35ccd8387f5e3a08e28b463ee2ada55e1647c4c502976e5c09dcd94828e78d0ad09ab94db80a14aa0a41aaedbede9a14677796971bbe97ac152c4101471d62786208cf54bb7763818a02bc95a8f57339e1262460c02e885ab2fc03762a7a646693b659e64889522ca3aa075772fb16c66643216c7ab93eb2c96d28142b8c99573f4fcdce41b9064a303d1a0da840a0f8f62bf404e1241139fb6d6eedf56e5ea7fed9e79001bbc31806d7168a0550099050860af73557452539c5bd055324400969f76b4643b531a8fef7c4ad2a02de16af6ab3f4205295bdd828e81c97930413ee3dd8e0aa76268909492c0f646a0ab2f8ba58a07310e12c7140427278f5715cbf975b735bac5d2338c487b2b615ca004725301c50e47bfa03c8495b5f009ee5aac8b15581dc6e9f5870c63e8fcc118a0cbd9efe9133d519ca35327c90c3b102972967e35e4432e4d0ce8416311267b7ca04e8d4cd1b3d110571ed885ccd470458a86e2dcf1fd083f1c2b9118001d4fc89ea03c6135b75f89e3b5c24c1354638e1a10c2248e5d7bc87df2d8230910660d3d84a0186eea2111408bc02272677443c21e2533146a56785a6bc34f9180e2ba4b567880\",\n"
                        + "      \"0xf90211a0ebfa45ac468954fd9f1e4d5c8b5b0015500b7e9f083e1626e05a03069d2d7b67a077e18486d90836a1816056a6fa8ed0339e15cf937ea87841e035ab26fd39057ea06e8c3b073ed5d7e86a205f0011f726b0a87f011b4e6e0521a287d378eda627a5a0b1decdb542bf89924e1022d9ab05e3dd9e2aca2f32f43489ce5723ddbe8b7a4fa0223dd0aa6c255a7b5a1e4b9753eacf599db92476baf674136f39b808d084991da0cbd89f24d151b9b23b147bf1bb71006d8661b19a8171cf26bb20842944a25ff1a0a380efa4b380cf41b0af35bb7a4f68c1a3988f9aa62c1a4c3c0bfd72d6c588c2a0c76f535a14cd7d869f5f01054cc72b827bd1c7b11df30138b98421a7c331d5a8a0d5b25d3d8a6c82f60c6eab724afed222f575824789d2d9f0768d46be39b7cbd1a0e2b2326a7ee7c454bde59d682367af495756b3c11408b7d97e3e84936ef4c5a1a00bb7e8a57023a0926c6683cd2a9a530b29d26d75d38872aeec1a21100b77fecba0288d0834990caaf30d2c6dfff07e52615e16c463e7256fab04ede25899d032f9a0dbb8748605aeb933068490238594afa7d93e018d04712d3d6af29fac9c81620fa0bae1aa9abe2c40e45f3274edeaba95f81a7e91da98c746bf31b21c280f100682a0c726027419c64dba7e2aeb5d1423b1fad6e773cb604668dc044671d5d5b18430a0cacd3a2b6c933e9a2148c7314d294d2f7356bb17900ac3b4faccd7f561cd6f1d80\",\n"
                        + "      \"0xf90211a0d26a9a18bdeb7e2387f9d9375bbc1b987c45a58f3eaaa4d29df611f0618c17d4a0839e8f34f581129bf52277d6aab412bc9794866fa22e93938fffbf85896b067ba008d315b1fbb99070a424b7efdeb3a8a90d631e325dc48f5f8c36b67ca0244081a0c1436f0a1e74a33600455326571d9549e904724d2646c0c86bed0e226a687bd1a05699909cad5f4b507001ce88a887af4148fb7ee14ed0f93c7aa9635b66407f9da0cb5f6eb1cc89cef0e221aeef566c891fa46f456370d52a8b322e4917c2f9ad87a0da1425f08bbf9d0368d159cd810f33c2b19a6e5d0b15043dfac565cf03b57c77a0b26393ad5305e312a3e388deb50bec841bbcbec45317ffb7753114f3d6d37ff9a0c24f396b4ae01cb32a72880baff2db25843979103c3a4d1116698416e29439c4a0d6738cafb3d29dd438a2bf074d9b991636535b83b41f589189801ce75e94c5b7a02f2c355b91c78a6d5386a874a092b1d8e1c740408afb532721c1af0e905de5e3a01511013c45279c74536a5e538251c4dfd7594848ed570b48615b094ca83849dda05e12cb9b86dc6c2c1e0dd68f8c2a93c83d2eedb5dbba3969a2b6c7032e26fc7ca0cfa01a6943b11ca2be19dfa3b23d535395f17db6ac1fe1dd5172643e8ccd68f7a002ae61b52a9a0b09c4ca9866b69e4f10765c1f7778e2d4f2300183868f5fd151a039dc21a690fbeed9ea699959be8b2e76eed93b9da21d12ee4254d0a1c51dc87380\",\n"
                        + "      \"0xf90211a05b44d7409dc085529b33905d01eb00210fb1c787998b88df5619fb6e3b860181a03bb3e5e3e8d979fd3bc86c602d6e6a57b1fc4c7f5f6a774b7e91af001cdef664a0a8fd61cceed0f8a639beca30fcd32411c0fcca5a102ff4a2656cde8c7eb858b4a0052e56ef09a9139a58d80329870ddc961e8ed112c8edc1325f71903fba84275ba0729ff34831623850f26d3601d7db1af2da69f115e9be8f9fa8454cc006b51403a09e56b2a68595be5b215cbc173a6e810137be55a748587eb81d172439018ee7aaa0fa5b27319522283b38e2d1abec5941b2d78e78fc40aa5a4b4476e36a0443311fa05b5f6d0058c46ea701f4ba42aabedb7b12e664a91f4b35c5519797df4c623f55a0947a7cbc568c23382aacbe508353c7f8ad299f3ea3eb471506c6b4291868b3e5a0f2313976f5c85685f419fc3022cf28fb9696c05ca27c44437f717f02e0154fbda01a96e6375ed8e8dc0749b2d45f93207d9219168a9eea142053a5e23e7a47232aa06f9d9145b92865e970f60dd81aa55ced05698090e9e23f341729be2a90ce7b96a01b6c084aad02ab8cfc5bdc0800b956db46853cfa8b9b47ff35867f02718f8130a0d7493eefde332cd1de8898096e9e8916cc7e320a6eb0ede2dc165fc5ed8803a1a0f0c6e212824552eee4b2479dae9b570041441e0cb0244d8bf045a73268aefda6a01e9045c2bc23b1610f20c3501862a067bfc80af7908344527c62faf1129fb43d80\",\n"
                        + "      \"0xf90211a056e7736ac44b0a6d46f4a6e40abf6426866f7f29ab0637bc89c390b4f0dc7807a07aec46d6e64a9847c400021f5e0f0373592e676b11d15cdf22765addf1033f01a09c356f78e4889e626568761468ca4e44ce0cc78f08a40035fc9be23b1c1990a0a0d18cfe2360f9559b663f4a8864995420b54e0c4c6c7f33019d2467b67080fed5a08b3d08ef8de3638edabb423c901a6c1ca8b1ef93b41427b2d409be7198a89225a0df689118041c8f4b2751afbf3cb7be36540e52742362df3ded6f67e3a8024feea0a7e5e17854690d418ef89eb6753e1d36e96cadc4a186bcd8ce71f8665fbe6814a0367bc2399eee7dffeaf8aea140a3973cbced2ed904aa7e6c69443a4877bb7115a0b1f51cb2cb73acdce7ee790854c03d59940e415b6598bda7fa753c0ae0e129dda021f400618bbd2b1ac65d863146f3497feaca7e73df4a3cf211b0a95ec5f39b5ea0494ec6c1615466d244e80992aee4b2496b4c8adda7f7b989e776a715a7a72b4aa03d64c83151b47fdf8c16ce7845306575108c3b8c6b3f9ce8032b65f05fb5d017a0c68bc1b17e6a32dd438aec2ca6c8beea5dd53a3192971bbc484036c9431ead88a0580a8ef71765615a19afd214553866e2215f94eb72631335fa8ec0c5ad450e19a04eda5daea47af109418ad18cae8c124e790d66046ce9389376c4a965f919c725a0a0a6553d3fc52e639eac9c20dbb7cd8930eacaa8b35f8b8f3bb23afb53cc966580\",\n"
                        + "      \"0xf90211a0597493b2845e511c876b84fad3925bb78c3922b3734208f20aff5461c7078e7ca0eb3723d662049847561ea31922b2ea5ab7a641ce7c14b5fa0ec346975d16c187a00e5c88ea26113f0295d8c682f1526f2fa7906bc422bfe9cc8b8f869686e179d4a0b94a30e2218afad53a0eacc505ea552a8288457847fcae72555aac638b52c46ea0d6430dd23ef9889e1272e494530d6ec89f080c49bf09f8a6ac634b153fa8ce5ca0e8c4de149f9b385e51fb66516a4d91b62dbd80ddedd4522e14d370da47f63e5aa0fa40fc95a8bdd7126c4db03b0c1a20c654aa22cf6f72024c4a0925d6f29fc9baa005c16a67b6ee21b8e9371064570b2f30342add57d7c45bc43df25fc680132186a0c2ed9529b58b672abd733db30ccac4e5ebeeedeb38418a5925e57b0f822df814a0d18e42474e815b62da57cf67bcd8ed1c90d4f8ecf8f6d0b2e66b9656be2686ada0e4dc3683f731f21c0c34a10d7ffac39b20a64ac326269433a5cacaeb90a8a17da0e3f8b04c275387c6222b6536967f2f1e6fc76dc6ee0e705d3e505f3d07e8bdd4a0deaf89533b13c20ff50ff3f03ff3a2daa8d1329a6a6d17a58c422cbe5ee2f80aa022b542269e108a24dc0ed74e62fc571185dcf398acfbf15a7fb6ce3e8aba50d6a0f29369f8c3d70bc9f472c2175ab5495edcf35a65527f67d391068c0275284307a0c93bd89abbea5ae96496a12d236323d11d2da9cde7a3b9fdb15d504f4ea1eda680\",\n"
                        + "      \"0xf90151a000595129cf1c67ce97ed615df7184fb47a0d018d9147a2a7952a639e464cdfd380a052081f67d885b439bee62a1c4e09c02a1ec729aef4d92be6584a29141a79c5c7a0d99cae2bc3b6fc3e8e2767b4d7a5884b9b2d9d20da7890318beefcdb9c7346d58080a0f63571735d99e763dafadb03d1abe3a93a98740ecddc8514d13df46a35a8d94680a081870f4697e57436ccdd8bdc1d624d7bc07861748ff8b3a352334df68f2c03ada0f0252504ee8753335ecf0ca1f73cf13d4505783ed5110bf4e9b384a68ad88f69a0d38a18ef7b993c9ad2d848453e7722c26998ff3d63e243b99cdab0dabf84157ea06e5ad479dbda7f1647ed613e35d837f5d2c697ef34160f3665173186553ee9c280a0bf8371cac505b6ea0cb0f67ef744713f306a2455f3f8844cfa527c62db59c69980a0d6d689405b1cf73786c5fdabb55ec22f9b5712d1ba9c0bfcd46d4b93721fcac780\",\n"
                        + "      \"0xf8669d37f29e142d49f8824d4e7f1735ec3da219687387629b5fccd86812df84b846f8440180a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a01d93f60f105899172f7255c030301c3af4564edd4a48577dbdc448aec7ddb0ac\"],\n"
                        + "    \"balance\": \"0x0\",\n"
                        + "    \"codeHash\": \"0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470\",\n"
                        + "    \"nonce\": \"0x0\",\n"
                        + "    \"storageHash\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "    \"storageProof\": [{\n"
                        + "      \"key\": \"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421\",\n"
                        + "      \"value\": \"0x0\",\n"
                        + "      \"proof\": []\n"
                        + "    }]\n"
                        + "  }\n"
                        + "}");

        LineaGetProof lineaGetProof = deserialiseResponse(LineaGetProof.class);

        EthGetProof.Proof proof =
                new EthGetProof.Proof(
                        "0x7f0d15c7faae65896648c8273b6d7e43f58fa842",
                        "0x0",
                        "0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470",
                        "0x0",
                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                        Arrays.asList(
                                "0xf90211a08245f766623948f962b9f277955e4be3868d107042195746e5bb9b60e45c4a35a0f95891987a66390224caca18cf15d40288e85a58fee02b61b4e235a95844678ca04c783e1622a91a7cd4d06e82e654cc8bcd5b00a545889ff729f35ccd8387f5e3a08e28b463ee2ada55e1647c4c502976e5c09dcd94828e78d0ad09ab94db80a14aa0a41aaedbede9a14677796971bbe97ac152c4101471d62786208cf54bb7763818a02bc95a8f57339e1262460c02e885ab2fc03762a7a646693b659e64889522ca3aa075772fb16c66643216c7ab93eb2c96d28142b8c99573f4fcdce41b9064a303d1a0da840a0f8f62bf404e1241139fb6d6eedf56e5ea7fed9e79001bbc31806d7168a0550099050860af73557452539c5bd055324400969f76b4643b531a8fef7c4ad2a02de16af6ab3f4205295bdd828e81c97930413ee3dd8e0aa76268909492c0f646a0ab2f8ba58a07310e12c7140427278f5715cbf975b735bac5d2338c487b2b615ca004725301c50e47bfa03c8495b5f009ee5aac8b15581dc6e9f5870c63e8fcc118a0cbd9efe9133d519ca35327c90c3b102972967e35e4432e4d0ce8416311267b7ca04e8d4cd1b3d110571ed885ccd470458a86e2dcf1fd083f1c2b9118001d4fc89ea03c6135b75f89e3b5c24c1354638e1a10c2248e5d7bc87df2d8230910660d3d84a0186eea2111408bc02272677443c21e2533146a56785a6bc34f9180e2ba4b567880",
                                "0xf90211a0ebfa45ac468954fd9f1e4d5c8b5b0015500b7e9f083e1626e05a03069d2d7b67a077e18486d90836a1816056a6fa8ed0339e15cf937ea87841e035ab26fd39057ea06e8c3b073ed5d7e86a205f0011f726b0a87f011b4e6e0521a287d378eda627a5a0b1decdb542bf89924e1022d9ab05e3dd9e2aca2f32f43489ce5723ddbe8b7a4fa0223dd0aa6c255a7b5a1e4b9753eacf599db92476baf674136f39b808d084991da0cbd89f24d151b9b23b147bf1bb71006d8661b19a8171cf26bb20842944a25ff1a0a380efa4b380cf41b0af35bb7a4f68c1a3988f9aa62c1a4c3c0bfd72d6c588c2a0c76f535a14cd7d869f5f01054cc72b827bd1c7b11df30138b98421a7c331d5a8a0d5b25d3d8a6c82f60c6eab724afed222f575824789d2d9f0768d46be39b7cbd1a0e2b2326a7ee7c454bde59d682367af495756b3c11408b7d97e3e84936ef4c5a1a00bb7e8a57023a0926c6683cd2a9a530b29d26d75d38872aeec1a21100b77fecba0288d0834990caaf30d2c6dfff07e52615e16c463e7256fab04ede25899d032f9a0dbb8748605aeb933068490238594afa7d93e018d04712d3d6af29fac9c81620fa0bae1aa9abe2c40e45f3274edeaba95f81a7e91da98c746bf31b21c280f100682a0c726027419c64dba7e2aeb5d1423b1fad6e773cb604668dc044671d5d5b18430a0cacd3a2b6c933e9a2148c7314d294d2f7356bb17900ac3b4faccd7f561cd6f1d80",
                                "0xf90211a0d26a9a18bdeb7e2387f9d9375bbc1b987c45a58f3eaaa4d29df611f0618c17d4a0839e8f34f581129bf52277d6aab412bc9794866fa22e93938fffbf85896b067ba008d315b1fbb99070a424b7efdeb3a8a90d631e325dc48f5f8c36b67ca0244081a0c1436f0a1e74a33600455326571d9549e904724d2646c0c86bed0e226a687bd1a05699909cad5f4b507001ce88a887af4148fb7ee14ed0f93c7aa9635b66407f9da0cb5f6eb1cc89cef0e221aeef566c891fa46f456370d52a8b322e4917c2f9ad87a0da1425f08bbf9d0368d159cd810f33c2b19a6e5d0b15043dfac565cf03b57c77a0b26393ad5305e312a3e388deb50bec841bbcbec45317ffb7753114f3d6d37ff9a0c24f396b4ae01cb32a72880baff2db25843979103c3a4d1116698416e29439c4a0d6738cafb3d29dd438a2bf074d9b991636535b83b41f589189801ce75e94c5b7a02f2c355b91c78a6d5386a874a092b1d8e1c740408afb532721c1af0e905de5e3a01511013c45279c74536a5e538251c4dfd7594848ed570b48615b094ca83849dda05e12cb9b86dc6c2c1e0dd68f8c2a93c83d2eedb5dbba3969a2b6c7032e26fc7ca0cfa01a6943b11ca2be19dfa3b23d535395f17db6ac1fe1dd5172643e8ccd68f7a002ae61b52a9a0b09c4ca9866b69e4f10765c1f7778e2d4f2300183868f5fd151a039dc21a690fbeed9ea699959be8b2e76eed93b9da21d12ee4254d0a1c51dc87380",
                                "0xf90211a05b44d7409dc085529b33905d01eb00210fb1c787998b88df5619fb6e3b860181a03bb3e5e3e8d979fd3bc86c602d6e6a57b1fc4c7f5f6a774b7e91af001cdef664a0a8fd61cceed0f8a639beca30fcd32411c0fcca5a102ff4a2656cde8c7eb858b4a0052e56ef09a9139a58d80329870ddc961e8ed112c8edc1325f71903fba84275ba0729ff34831623850f26d3601d7db1af2da69f115e9be8f9fa8454cc006b51403a09e56b2a68595be5b215cbc173a6e810137be55a748587eb81d172439018ee7aaa0fa5b27319522283b38e2d1abec5941b2d78e78fc40aa5a4b4476e36a0443311fa05b5f6d0058c46ea701f4ba42aabedb7b12e664a91f4b35c5519797df4c623f55a0947a7cbc568c23382aacbe508353c7f8ad299f3ea3eb471506c6b4291868b3e5a0f2313976f5c85685f419fc3022cf28fb9696c05ca27c44437f717f02e0154fbda01a96e6375ed8e8dc0749b2d45f93207d9219168a9eea142053a5e23e7a47232aa06f9d9145b92865e970f60dd81aa55ced05698090e9e23f341729be2a90ce7b96a01b6c084aad02ab8cfc5bdc0800b956db46853cfa8b9b47ff35867f02718f8130a0d7493eefde332cd1de8898096e9e8916cc7e320a6eb0ede2dc165fc5ed8803a1a0f0c6e212824552eee4b2479dae9b570041441e0cb0244d8bf045a73268aefda6a01e9045c2bc23b1610f20c3501862a067bfc80af7908344527c62faf1129fb43d80",
                                "0xf90211a056e7736ac44b0a6d46f4a6e40abf6426866f7f29ab0637bc89c390b4f0dc7807a07aec46d6e64a9847c400021f5e0f0373592e676b11d15cdf22765addf1033f01a09c356f78e4889e626568761468ca4e44ce0cc78f08a40035fc9be23b1c1990a0a0d18cfe2360f9559b663f4a8864995420b54e0c4c6c7f33019d2467b67080fed5a08b3d08ef8de3638edabb423c901a6c1ca8b1ef93b41427b2d409be7198a89225a0df689118041c8f4b2751afbf3cb7be36540e52742362df3ded6f67e3a8024feea0a7e5e17854690d418ef89eb6753e1d36e96cadc4a186bcd8ce71f8665fbe6814a0367bc2399eee7dffeaf8aea140a3973cbced2ed904aa7e6c69443a4877bb7115a0b1f51cb2cb73acdce7ee790854c03d59940e415b6598bda7fa753c0ae0e129dda021f400618bbd2b1ac65d863146f3497feaca7e73df4a3cf211b0a95ec5f39b5ea0494ec6c1615466d244e80992aee4b2496b4c8adda7f7b989e776a715a7a72b4aa03d64c83151b47fdf8c16ce7845306575108c3b8c6b3f9ce8032b65f05fb5d017a0c68bc1b17e6a32dd438aec2ca6c8beea5dd53a3192971bbc484036c9431ead88a0580a8ef71765615a19afd214553866e2215f94eb72631335fa8ec0c5ad450e19a04eda5daea47af109418ad18cae8c124e790d66046ce9389376c4a965f919c725a0a0a6553d3fc52e639eac9c20dbb7cd8930eacaa8b35f8b8f3bb23afb53cc966580",
                                "0xf90211a0597493b2845e511c876b84fad3925bb78c3922b3734208f20aff5461c7078e7ca0eb3723d662049847561ea31922b2ea5ab7a641ce7c14b5fa0ec346975d16c187a00e5c88ea26113f0295d8c682f1526f2fa7906bc422bfe9cc8b8f869686e179d4a0b94a30e2218afad53a0eacc505ea552a8288457847fcae72555aac638b52c46ea0d6430dd23ef9889e1272e494530d6ec89f080c49bf09f8a6ac634b153fa8ce5ca0e8c4de149f9b385e51fb66516a4d91b62dbd80ddedd4522e14d370da47f63e5aa0fa40fc95a8bdd7126c4db03b0c1a20c654aa22cf6f72024c4a0925d6f29fc9baa005c16a67b6ee21b8e9371064570b2f30342add57d7c45bc43df25fc680132186a0c2ed9529b58b672abd733db30ccac4e5ebeeedeb38418a5925e57b0f822df814a0d18e42474e815b62da57cf67bcd8ed1c90d4f8ecf8f6d0b2e66b9656be2686ada0e4dc3683f731f21c0c34a10d7ffac39b20a64ac326269433a5cacaeb90a8a17da0e3f8b04c275387c6222b6536967f2f1e6fc76dc6ee0e705d3e505f3d07e8bdd4a0deaf89533b13c20ff50ff3f03ff3a2daa8d1329a6a6d17a58c422cbe5ee2f80aa022b542269e108a24dc0ed74e62fc571185dcf398acfbf15a7fb6ce3e8aba50d6a0f29369f8c3d70bc9f472c2175ab5495edcf35a65527f67d391068c0275284307a0c93bd89abbea5ae96496a12d236323d11d2da9cde7a3b9fdb15d504f4ea1eda680",
                                "0xf90151a000595129cf1c67ce97ed615df7184fb47a0d018d9147a2a7952a639e464cdfd380a052081f67d885b439bee62a1c4e09c02a1ec729aef4d92be6584a29141a79c5c7a0d99cae2bc3b6fc3e8e2767b4d7a5884b9b2d9d20da7890318beefcdb9c7346d58080a0f63571735d99e763dafadb03d1abe3a93a98740ecddc8514d13df46a35a8d94680a081870f4697e57436ccdd8bdc1d624d7bc07861748ff8b3a352334df68f2c03ada0f0252504ee8753335ecf0ca1f73cf13d4505783ed5110bf4e9b384a68ad88f69a0d38a18ef7b993c9ad2d848453e7722c26998ff3d63e243b99cdab0dabf84157ea06e5ad479dbda7f1647ed613e35d837f5d2c697ef34160f3665173186553ee9c280a0bf8371cac505b6ea0cb0f67ef744713f306a2455f3f8844cfa527c62db59c69980a0d6d689405b1cf73786c5fdabb55ec22f9b5712d1ba9c0bfcd46d4b93721fcac780",
                                "0xf8669d37f29e142d49f8824d4e7f1735ec3da219687387629b5fccd86812df84b846f8440180a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a01d93f60f105899172f7255c030301c3af4564edd4a48577dbdc448aec7ddb0ac"),
                        // String key, String value, List<String> proof
                        Arrays.asList(
                                new EthGetProof.StorageProof(
                                        "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
                                        "0x0",
                                        new ArrayList<>())));
        assertEquals(proof, lineaGetProof.getProof());
    }

    @Test
    void testLineaGetTransactionExclusionStatusV1() {
        buildResponse(
                "{\n"
                        + "  \"jsonrpc\": \"2.0\",\n"
                        + "  \"id\": 1,\n"
                        + "  \"result\": {\n"
                        + "    \"txHash\": \"0x526e56101cf39c1e717cef9cedf6fdddb42684711abda35bae51136dbb350ad7\",\n"
                        + "    \"from\": \"0x4d144d7b9c96b26361d6ac74dd1d8267edca4fc2\",\n"
                        + "    \"nonce\": \"0x64\",\n"
                        + "    \"txRejectionStage\": \"SEQUENCER\",\n"
                        + "    \"reasonMessage\": \"Transaction line count for module ADD=402 is above the limit 70\",\n"
                        + "    \"blockNumber\": \"0x3039\",\n"
                        + "	   \"timestamp\": \"2024-08-22T09:18:51Z\"}\n"
                        + "}");

        LineaGetTransactionExclusionStatusV1 lineaExclusionStatus =
                deserialiseResponse(LineaGetTransactionExclusionStatusV1.class);

        LineaGetTransactionExclusionStatusV1.LineaExclusionStatus exclusionStatus =
                new LineaGetTransactionExclusionStatusV1.LineaExclusionStatus(
                        "0x526e56101cf39c1e717cef9cedf6fdddb42684711abda35bae51136dbb350ad7",
                        "0x4d144d7b9c96b26361d6ac74dd1d8267edca4fc2",
                        "0x64",
                        "SEQUENCER",
                        "Transaction line count for module ADD=402 is above the limit 70",
                        "0x3039",
                        "2024-08-22T09:18:51Z");
        assertEquals(exclusionStatus, lineaExclusionStatus.getLineaTransactionExclusionStatus());
    }
}
