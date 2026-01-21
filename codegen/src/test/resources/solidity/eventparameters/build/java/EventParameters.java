package org.web3j.unittests.java;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version none.
 */
@SuppressWarnings("rawtypes")
@Generated("org.web3j.codegen.SolidityFunctionWrapperGenerator")
public class EventParameters extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC__CONTRACTNUMBER = "_contractNumber";

    public static final String FUNC__TESTADDRESS = "_testAddress";

    public static final String FUNC_TESTEVENT = "testEvent";

    public static final Event TESTEVENT_EVENT = new Event("TestEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>(true) {}, new TypeReference<Utf8String>(true) {}, new TypeReference<Address>() {}));
    ;

    @Deprecated
    protected EventParameters(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected EventParameters(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected EventParameters(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected EventParameters(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<TestEventEventResponse> getTestEventEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TESTEVENT_EVENT, transactionReceipt);
        ArrayList<TestEventEventResponse> responses = new ArrayList<TestEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TestEventEventResponse typedResponse = new TestEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._contractNumber = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.param1 = (byte[]) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.param2 = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.param3 = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TestEventEventResponse getTestEventEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TESTEVENT_EVENT, log);
        TestEventEventResponse typedResponse = new TestEventEventResponse();
        typedResponse.log = log;
        typedResponse._contractNumber = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.param1 = (byte[]) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.param2 = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.param3 = (String) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<TestEventEventResponse> testEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTestEventEventFromLog(log));
    }

    public Flowable<TestEventEventResponse> testEventEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TESTEVENT_EVENT));
        return testEventEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> _contractNumber() {
        final Function function = new Function(FUNC__CONTRACTNUMBER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> _testAddress() {
        final Function function = new Function(FUNC__TESTADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> testEvent() {
        final Function function = new Function(
                FUNC_TESTEVENT, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static EventParameters load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new EventParameters(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static EventParameters load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new EventParameters(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static EventParameters load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new EventParameters(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static EventParameters load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new EventParameters(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class TestEventEventResponse extends BaseEventResponse {
        public BigInteger _contractNumber;

        public byte[] param1;

        public String param2;

        public String param3;
    }
}
