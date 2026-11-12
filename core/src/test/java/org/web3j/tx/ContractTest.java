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
package org.web3j.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.SampleKeys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.*;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.JsonRpcError;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.exceptions.ContractCallException;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticEIP1559GasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.EmptyTransactionReceipt;
import org.web3j.utils.Async;
import org.web3j.utils.Numeric;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class ContractTest extends ManagedTransactionTester {

    private static final String TEST_CONTRACT_BINARY = "12345";

    private static final String TXN_SUCCESS_STATUS = "0x1";

    private static final String TXN_FAIL_STATUS = "0x0";

    private static final String TXN_GAS_USED = "0x1";

    private static final String TXN_BLOCK_NUM = "0x2";

    private static final String OWNER_REVERT_MSG_STR =
            "Only the contract owner can perform this action";

    private static final String OWNER_REVERT_MSG_HASH =
            "0x08c379a"
                    + "00000000000000000000000000000000000000000000000000000000000000020"
                    + "000000000000000000000000000000000000000000000000000000000000002f4"
                    + "f6e6c792074686520636f6e7472616374206f776e65722063616e20706572666f"
                    + "726d207468697320616374696f6e0000000000000000000000000000000000";

    private static final String ERROR_ENCODED_DATA =
            "0x82b42900" + "0000000000000000000000001111111111111111111111111111111111111111";

    private TestContract contract;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        contract =
                new TestContract(
                        ADDRESS,
                        web3j,
                        getVerifiedTransactionManager(SampleKeys.CREDENTIALS),
                        new DefaultGasProvider());
    }

    @Test
    void testConvertToNativeConvertsNestedArrays() {
        List<Type> values =
                Arrays.asList(
                        new DynamicArray<>(Uint256.class, new Uint256(1), new Uint256(2)),
                        new DynamicArray<>(Uint256.class, new Uint256(3)));

        // uint256[][] is returned as List<List<BigInteger>> by generated wrappers
        List<List<BigInteger>> nativeValues = Contract.convertToNative(values);

        assertEquals(
                Arrays.asList(
                        Arrays.asList(BigInteger.ONE, BigInteger.TWO),
                        singletonList(BigInteger.valueOf(3))),
                nativeValues);
        assertEquals(BigInteger.valueOf(3), nativeValues.get(1).get(0));
    }

    @Test
    void testGetContractAddress() {
        assertEquals(ADDRESS, contract.getContractAddress());
    }

    @Test
    void testGetContractTransactionReceipt() {
        assertFalse(contract.getTransactionReceipt().isPresent());
    }

    @Test
    void testDeploy() throws Exception {
        TransactionReceipt transactionReceipt = createTransactionReceipt();
        Contract deployedContract = deployContract(transactionReceipt);

        assertEquals(ADDRESS, deployedContract.getContractAddress());
        assertTrue(deployedContract.getTransactionReceipt().isPresent());
        assertEquals(transactionReceipt, deployedContract.getTransactionReceipt().get());
    }

    @Test
    void testContractDeployFails() throws IOException {
        TransactionReceipt transactionReceipt = createFailedTransactionReceipt();
        prepareCall(null);
        assertThrows(TransactionException.class, () -> deployContract(transactionReceipt));
    }

    @Test
    void testContractDeployWithNullStatusSucceeds() throws Exception {
        TransactionReceipt transactionReceipt = createTransactionReceiptWithStatus(null);
        Contract deployedContract = deployContract(transactionReceipt);

        assertEquals(ADDRESS, deployedContract.getContractAddress());
        assertTrue(deployedContract.getTransactionReceipt().isPresent());
        assertEquals(transactionReceipt, deployedContract.getTransactionReceipt().get());
    }

    @Test
    void testIsValid() throws Exception {
        prepareEthGetCode(TEST_CONTRACT_BINARY);

        Contract contract = deployContract(createTransactionReceipt());
        assertTrue(contract.isValid());
    }

    @Test
    void testIsValidSkipMetadataBzzr0() throws Exception {
        prepareEthGetCode(
                TEST_CONTRACT_BINARY
                        + "a165627a7a72305820"
                        + "a9bc86938894dc250f6ea25dd823d4472fad6087edcda429a3504e3713a9fc880029");

        Contract contract = deployContract(createTransactionReceipt());
        assertTrue(contract.isValid());
    }

    @Test
    void testIsValidSkipMetadataBzzr1() throws Exception {
        prepareEthGetCode(
                TEST_CONTRACT_BINARY
                        + "a265627a7a72315820"
                        + "a9bc86938894dc250f6ea25dd823d4472fad6087edcda429a3504e3713a9fc880029");

        Contract contract = deployContract(createTransactionReceipt());
        assertTrue(contract.isValid());
    }

    @Test
    void testIsValidSkipMetadataIpfs() throws Exception {
        prepareEthGetCode(
                TEST_CONTRACT_BINARY
                        + "a2646970667358221220"
                        + "a9bc86938894dc250f6ea25dd823d4472fad6087edcda429a3504e3713a9fc880029");

        Contract contract = deployContract(createTransactionReceipt());
        assertTrue(contract.isValid());
    }

    @Test
    void testIsValidSkipMetadataNone() throws Exception {
        prepareEthGetCode(
                TEST_CONTRACT_BINARY
                        + "a164736f6c634300080a000a"
                        + "a9bc86938894dc250f6ea25dd823d4472fad6087edcda429a3504e3713a9fc880029");

        Contract contract = deployContract(createTransactionReceipt());
        assertTrue(contract.isValid());
    }

    @Test
    void testIsValidDifferentCode() throws Exception {
        prepareEthGetCode(TEST_CONTRACT_BINARY + "0");

        Contract contract = deployContract(createTransactionReceipt());
        assertFalse(contract.isValid());
    }

    @Test
    void testIsValidEmptyCode() throws Exception {
        prepareEthGetCode("");

        Contract contract = deployContract(createTransactionReceipt());
        assertFalse(contract.isValid());
    }

    @Test
    void testIsValidNoBinThrows() {
        TransactionManager txManager = mock(TransactionManager.class);
        TestContract contract =
                new TestContract(
                        Contract.BIN_NOT_PROVIDED,
                        ADDRESS,
                        web3j,
                        txManager,
                        new DefaultGasProvider());
        assertThrows(UnsupportedOperationException.class, contract::isValid);
    }

    @Test
    void testDeployInvalidContractAddress() throws Throwable {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);

        prepareTransaction(transactionReceipt);
        ContractGasProvider contractGasProvider = new DefaultGasProvider();
        String encodedConstructor =
                FunctionEncoder.encodeConstructor(
                        Collections.<Type>singletonList(new Uint256(BigInteger.TEN)));
        assertThrows(
                RuntimeException.class,
                () ->
                        TestContract.deployRemoteCall(
                                        TestContract.class,
                                        web3j,
                                        SampleKeys.CREDENTIALS,
                                        contractGasProvider,
                                        "0xcafed00d",
                                        encodedConstructor,
                                        BigInteger.ZERO)
                                .send());
    }

    @Test
    void testCallSingleValue() throws Exception {
        // Example taken from FunctionReturnDecoderTest

        prepareCall(
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000000");

        assertEquals(new Utf8String(""), contract.callSingleValue().send());
    }

    @Test
    void testCallSingleValueEmpty() throws Exception {
        // Example taken from FunctionReturnDecoderTest

        prepareCall("0x");

        assertNull(contract.callSingleValue().send());
    }

    @Test
    void testCallSingleValueReverted() throws Exception {
        prepareCall(OWNER_REVERT_MSG_HASH);
        ContractCallException thrown =
                assertThrows(ContractCallException.class, () -> contract.callSingleValue().send());

        assertEquals(
                String.format(TransactionManager.REVERT_ERR_STR, OWNER_REVERT_MSG_STR),
                thrown.getMessage());
    }

    @Test
    void testCallMultipleValue() throws Exception {
        prepareCall(
                "0x0000000000000000000000000000000000000000000000000000000000000037"
                        + "0000000000000000000000000000000000000000000000000000000000000007");

        assertEquals(
                Arrays.asList(
                        new Uint256(BigInteger.valueOf(55)), new Uint256(BigInteger.valueOf(7))),
                contract.callMultipleValue().send());
    }

    @Test
    void testCallMultipleValueEmpty() throws Exception {
        prepareCall("0x");

        assertEquals(emptyList(), contract.callMultipleValue().send());
    }

    @SuppressWarnings("unchecked")
    private void prepareCall(String result) throws IOException {
        EthCall ethCall = new EthCall();
        ethCall.setResult(result);

        mockRequest(ethCall);
    }

    @SuppressWarnings("unchecked")
    private void prepareCallWithError(String result, String data) throws IOException {
        EthCall ethCall = new EthCall();
        ethCall.setResult(result);

        var error = new Response.Error();
        error.setData(data);

        ethCall.setError(error);

        mockRequest(ethCall);
    }

    private void mockRequest(EthCall ethCall) throws IOException {
        Request<?, EthCall> request = mock(Request.class);
        when(request.send()).thenReturn(ethCall);

        when(web3j.ethCall(any(Transaction.class), any(DefaultBlockParameter.class)))
                .thenReturn((Request) request);
    }

    @Test
    void testTransaction() throws Exception {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);
        transactionReceipt.setStatus(TXN_SUCCESS_STATUS);

        prepareTransaction(transactionReceipt);

        assertEquals(
                transactionReceipt,
                contract.performTransaction(
                                new Address(BigInteger.TEN), new Uint256(BigInteger.ONE))
                        .send());
    }

    @Test
    void testTransactionFailed() throws IOException {
        TransactionReceipt transactionReceipt = createFailedTransactionReceipt();
        prepareCall(null);

        assertThrows(
                TransactionException.class,
                () -> {
                    prepareTransaction(transactionReceipt);

                    contract.performTransaction(
                                    new Address(BigInteger.TEN), new Uint256(BigInteger.ONE))
                            .send();
                });
    }

    @Test
    void testTransactionFailedWithRevertReason() throws Exception {
        TransactionReceipt transactionReceipt = createFailedTransactionReceipt();
        prepareCall(OWNER_REVERT_MSG_HASH);

        TransactionException thrown =
                assertThrows(
                        TransactionException.class,
                        () -> {
                            prepareTransaction(transactionReceipt);

                            contract.performTransaction(
                                            new Address(BigInteger.TEN),
                                            new Uint256(BigInteger.ONE))
                                    .send();
                        });

        assertEquals(
                String.format(
                        "Transaction %s has failed with status: %s. Gas used: 1. Revert reason: '%s'.",
                        TRANSACTION_HASH, TXN_FAIL_STATUS, OWNER_REVERT_MSG_STR),
                thrown.getMessage());
        assertEquals(transactionReceipt, thrown.getTransactionReceipt().get());
    }

    @Test
    void testTransactionFailedWithEncodedData() throws Exception {
        TransactionReceipt transactionReceipt = createFailedTransactionReceipt();
        prepareCallWithError(OWNER_REVERT_MSG_HASH, ERROR_ENCODED_DATA);

        TransactionException thrown =
                assertThrows(
                        TransactionException.class,
                        () -> {
                            prepareTransaction(transactionReceipt);

                            contract.performTransaction(
                                            new Address(BigInteger.TEN),
                                            new Uint256(BigInteger.ONE))
                                    .send();
                        });

        assertEquals(
                String.format(
                        "Transaction %s has failed with status: %s. Gas used: 1. Revert reason: '%s'.",
                        TRANSACTION_HASH, TXN_FAIL_STATUS, OWNER_REVERT_MSG_STR),
                thrown.getMessage());
        assertEquals(transactionReceipt, thrown.getTransactionReceipt().get());
        assertEquals(ERROR_ENCODED_DATA, thrown.getEncodedDataResponse().get());
    }

    @Test
    void testProcessEvent() {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        Log log = new Log();
        log.setTopics(
                Arrays.asList(
                        // encoded function
                        "0xfceb437c298f40d64702ac26411b2316e79f3c28ffa60edfc891ad4fc8ab82ca",
                        // indexed value
                        "0000000000000000000000003d6cb163f7c72d20b0fcd6baae5889329d138a4a"));
        // non-indexed value
        log.setData("0000000000000000000000000000000000000000000000000000000000000001");

        transactionReceipt.setLogs(singletonList(log));

        EventValues eventValues = contract.processEvent(transactionReceipt).get(0);

        assertEquals(
                singletonList(new Address("0x3d6cb163f7c72d20b0fcd6baae5889329d138a4a")),
                eventValues.getIndexedValues());
        assertEquals(singletonList(new Uint256(BigInteger.ONE)), eventValues.getNonIndexedValues());
    }

    @Test
    void testProcessEventForLogWithoutTopics() {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        final Log log = new Log();
        log.setTopics(Collections.emptyList());
        // non-indexed value
        log.setData("0000000000000000000000000000000000000000000000000000000000000001");
        transactionReceipt.setLogs(singletonList(log));

        final List<EventValues> eventValues = contract.processEvent(transactionReceipt);
        assertTrue(eventValues.isEmpty(), "No events expected");
    }

    @Test
    void testTimeout() throws IOException {
        prepareTransaction(null);

        TransactionManager transactionManager =
                getVerifiedTransactionManager(SampleKeys.CREDENTIALS, 1, 1);

        contract = new TestContract(ADDRESS, web3j, transactionManager, new DefaultGasProvider());
        assertThrows(TransactionException.class, this::testErrorScenario);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInvalidTransactionResponse() throws IOException {
        prepareNonceRequest();

        EthSendTransaction ethSendTransaction = new EthSendTransaction();
        ethSendTransaction.setError(new Response.Error(1, "Invalid transaction"));

        Request<?, EthSendTransaction> rawTransactionRequest = mock(Request.class);
        when(rawTransactionRequest.sendAsync()).thenReturn(Async.run(() -> ethSendTransaction));
        when(web3j.ethSendRawTransaction(any(String.class)))
                .thenReturn((Request) rawTransactionRequest);
        assertThrows(RuntimeException.class, this::testErrorScenario);
    }

    @Test
    void testJsonRpcError() throws IOException {
        EthSendTransaction ethSendTransaction = new EthSendTransaction();
        Response.Error error = new Response.Error(1, "Invalid Transaction");
        error.setData("Additional data");
        ethSendTransaction.setError(error);

        TransactionManager txManager =
                spy(new RawTransactionManager(web3j, SampleKeys.CREDENTIALS));
        doReturn(ethSendTransaction)
                .when(txManager)
                .sendTransaction(
                        any(BigInteger.class),
                        any(BigInteger.class),
                        anyString(),
                        anyString(),
                        any(BigInteger.class),
                        anyBoolean());

        JsonRpcError exception =
                assertThrows(
                        JsonRpcError.class,
                        () ->
                                txManager.executeTransaction(
                                        BigInteger.ZERO,
                                        BigInteger.ZERO,
                                        "",
                                        "",
                                        BigInteger.ZERO,
                                        false));

        assertEquals(error.getCode(), exception.getCode());
        assertEquals(error.getMessage(), exception.getMessage());
        assertEquals(error.getData(), exception.getData());
    }

    @Test
    void testSetGetAddresses() {
        assertNull(contract.getDeployedAddress("1"));
        contract.setDeployedAddress("1", "0x000000000000add0e00000000000");
        assertNotNull(contract.getDeployedAddress("1"));
        contract.setDeployedAddress("2", "0x000000000000add0e00000000000");
        assertNotNull(contract.getDeployedAddress("2"));
    }

    @Test
    void testSetGetGasPrice() {
        assertEquals(DefaultGasProvider.GAS_PRICE, contract.getGasPrice());
        BigInteger newPrice = ManagedTransaction.GAS_PRICE.multiply(BigInteger.valueOf(2));
        contract.setGasPrice(newPrice);
        assertEquals(newPrice, contract.getGasPrice());
    }

    @Test
    void testStaticGasProvider() throws IOException, TransactionException {
        StaticGasProvider gasProvider = new StaticGasProvider(BigInteger.TEN, BigInteger.ONE);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.executeTransaction(
                        any(BigInteger.class),
                        any(BigInteger.class),
                        anyString(),
                        anyString(),
                        any(BigInteger.class),
                        anyBoolean()))
                .thenReturn(new TransactionReceipt());

        contract = new TestContract(ADDRESS, web3j, txManager, gasProvider);

        Function func =
                new Function(
                        "test",
                        Collections.<Type>emptyList(),
                        Collections.<TypeReference<?>>emptyList());
        contract.executeTransaction(func);

        verify(txManager)
                .executeTransaction(
                        eq(BigInteger.TEN),
                        eq(BigInteger.ONE),
                        anyString(),
                        anyString(),
                        any(BigInteger.class),
                        anyBoolean());
    }

    @Test
    void testStaticEIP1559GasProvider() throws IOException, TransactionException {
        StaticEIP1559GasProvider gasProvider =
                new StaticEIP1559GasProvider(1L, BigInteger.TEN, BigInteger.ZERO, BigInteger.ONE);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.executeTransaction(
                        any(BigInteger.class),
                        any(BigInteger.class),
                        anyString(),
                        anyString(),
                        any(BigInteger.class),
                        anyBoolean()))
                .thenReturn(new TransactionReceipt());

        contract = new TestContract(ADDRESS, web3j, txManager, gasProvider);

        Function func =
                new Function(
                        "test",
                        Collections.<Type>emptyList(),
                        Collections.<TypeReference<?>>emptyList());
        contract.executeTransaction(func);

        verify(txManager)
                .executeTransactionEIP1559(
                        eq(1L),
                        eq(BigInteger.ZERO),
                        eq(BigInteger.TEN),
                        eq(BigInteger.ONE),
                        anyString(),
                        anyString(),
                        any(BigInteger.class),
                        anyBoolean());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInvalidTransactionReceipt() throws Throwable {
        prepareNonceRequest();
        prepareTransactionRequest();

        EthGetTransactionReceipt ethGetTransactionReceipt = new EthGetTransactionReceipt();
        ethGetTransactionReceipt.setError(new Response.Error(1, "Invalid transaction receipt"));

        Request<?, EthGetTransactionReceipt> getTransactionReceiptRequest = mock(Request.class);
        when(getTransactionReceiptRequest.sendAsync())
                .thenReturn(Async.run(() -> ethGetTransactionReceipt));
        when(web3j.ethGetTransactionReceipt(TRANSACTION_HASH))
                .thenReturn((Request) getTransactionReceiptRequest);
        assertThrows(RuntimeException.class, this::testErrorScenario);
    }

    @Test
    void testExtractEventParametersWithLogGivenATransactionReceipt() {

        final java.util.function.Function<String, Event> eventFactory =
                name -> new Event(name, emptyList());

        final BiFunction<Integer, Event, Log> logFactory =
                (logIndex, event) ->
                        new Log(
                                false,
                                "" + logIndex,
                                "0",
                                "0x0",
                                "0x0",
                                "0",
                                "0x" + logIndex,
                                "",
                                "",
                                singletonList(EventEncoder.encode(event)));

        final Event testEvent1 = eventFactory.apply("TestEvent1");
        final Event testEvent2 = eventFactory.apply("TestEvent2");

        final List<Log> logs =
                Arrays.asList(logFactory.apply(0, testEvent1), logFactory.apply(1, testEvent2));

        final TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setLogs(logs);

        final List<Contract.EventValuesWithLog> eventValuesWithLogs1 =
                contract.extractEventParametersWithLog(testEvent1, transactionReceipt);

        assertEquals(eventValuesWithLogs1.size(), 1);
        assertEquals(eventValuesWithLogs1.get(0).getLog(), logs.get(0));

        final List<Contract.EventValuesWithLog> eventValuesWithLogs2 =
                contract.extractEventParametersWithLog(testEvent2, transactionReceipt);

        assertEquals(eventValuesWithLogs2.size(), 1);
        assertEquals(eventValuesWithLogs2.get(0).getLog(), logs.get(1));
    }

    @Test
    void testEmptyTransactionReceipt() throws Exception {
        TransactionReceipt transactionReceipt = new EmptyTransactionReceipt(TRANSACTION_HASH);

        prepareTransaction(transactionReceipt);

        assertEquals(
                transactionReceipt,
                contract.performTransaction(
                                new Address(BigInteger.TEN), new Uint256(BigInteger.ONE))
                        .send());
    }

    void testErrorScenario() throws Throwable {
        try {
            contract.performTransaction(new Address(BigInteger.TEN), new Uint256(BigInteger.ONE))
                    .send();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    private TransactionReceipt createTransactionReceipt() {
        return createTransactionReceiptWithStatus(TXN_SUCCESS_STATUS);
    }

    private TransactionReceipt createFailedTransactionReceipt() {
        return createTransactionReceiptWithStatus(TXN_FAIL_STATUS);
    }

    private TransactionReceipt createTransactionReceiptWithStatus(String status) {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        transactionReceipt.setTransactionHash(TRANSACTION_HASH);
        transactionReceipt.setContractAddress(ADDRESS);
        transactionReceipt.setStatus(status);
        transactionReceipt.setGasUsed(TXN_GAS_USED);
        transactionReceipt.setBlockNumber(TXN_BLOCK_NUM);
        return transactionReceipt;
    }

    private Contract deployContract(TransactionReceipt transactionReceipt) throws Exception {

        prepareTransaction(transactionReceipt);

        String encodedConstructor =
                FunctionEncoder.encodeConstructor(
                        Collections.<Type>singletonList(new Uint256(BigInteger.TEN)));
        ContractGasProvider contractGasProvider = new DefaultGasProvider();
        return TestContract.deployRemoteCall(
                        TestContract.class,
                        web3j,
                        getVerifiedTransactionManager(SampleKeys.CREDENTIALS),
                        contractGasProvider,
                        "0xcafed00d",
                        encodedConstructor,
                        BigInteger.ZERO)
                .send();
    }

    @SuppressWarnings("unchecked")
    private void prepareEthGetCode(String binary) throws IOException {
        EthGetCode ethGetCode = new EthGetCode();
        ethGetCode.setResult(Numeric.prependHexPrefix(binary));

        Request<?, EthGetCode> ethGetCodeRequest = mock(Request.class);
        when(ethGetCodeRequest.send()).thenReturn(ethGetCode);
        when(web3j.ethGetCode(ADDRESS, DefaultBlockParameterName.LATEST))
                .thenReturn((Request) ethGetCodeRequest);
    }

    private static class TestContract extends Contract {
        public TestContract(
                String contractAddress,
                Web3j web3j,
                Credentials credentials,
                ContractGasProvider contractGasProvider) {
            super(TEST_CONTRACT_BINARY, contractAddress, web3j, credentials, contractGasProvider);
        }

        public TestContract(
                String contractAddress,
                Web3j web3j,
                TransactionManager transactionManager,
                ContractGasProvider gasProvider) {
            this(TEST_CONTRACT_BINARY, contractAddress, web3j, transactionManager, gasProvider);
        }

        public TestContract(
                String binary,
                String contractAddress,
                Web3j web3j,
                TransactionManager transactionManager,
                ContractGasProvider gasProvider) {
            super(binary, contractAddress, web3j, transactionManager, gasProvider);
        }

        public RemoteCall<Utf8String> callSingleValue() {
            Function function =
                    new Function(
                            "call",
                            Collections.<Type>emptyList(),
                            Collections.<TypeReference<?>>singletonList(
                                    new TypeReference<Utf8String>() {}));
            return executeRemoteCallSingleValueReturn(function);
        }

        public RemoteCall<List<Type>> callMultipleValue() {
            Function function =
                    new Function(
                            "call",
                            Collections.emptyList(),
                            Arrays.asList(
                                    new TypeReference<Uint256>() {},
                                    new TypeReference<Uint256>() {}));
            return executeRemoteCallMultipleValueReturn(function);
        }

        public RemoteCall<TransactionReceipt> performTransaction(Address address, Uint256 amount) {
            Function function =
                    new Function(
                            "approve", Arrays.asList(address, amount), Collections.emptyList());
            return executeRemoteCallTransaction(function);
        }

        public List<EventValues> processEvent(TransactionReceipt transactionReceipt) {
            Event event =
                    new Event(
                            "Event",
                            Arrays.asList(
                                    new TypeReference<Address>(true) {},
                                    new TypeReference<Uint256>() {}));
            return extractEventParameters(event, transactionReceipt);
        }
    }
}
