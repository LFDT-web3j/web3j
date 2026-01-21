package org.web3j.unittests.java;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
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
public class ArrayOfStructAndStruct extends Contract {
    public static final String BINARY = "608060405234801561000f575f80fd5b5061014f8061001d5f395ff3fe608060405234801561000f575f80fd5b5060043610610034575f3560e01c80631ae18a6414610038578063f0cd1e3b14610054575b5f80fd5b610052600480360381019061004d91906100a1565b610070565b005b61006e600480360381019061006991906100ee565b610074565b005b5f80fd5b5f80fd5b5f80fd5b5f80fd5b5f8190508260206002028201111561009b5761009a61007c565b5b92915050565b5f604082840312156100b6576100b5610078565b5b5f6100c384828501610080565b91505092915050565b5f80fd5b5f602082840312156100e5576100e46100cc565b5b81905092915050565b5f6020828403121561010357610102610078565b5b5f610110848285016100d0565b9150509291505056fea2646970667358221220deb8c438006301a2bb5f5ed22ad6516a4f075c63a360dad46a6571057187273164736f6c63430008140033";

    private static String librariesLinkedBinary;

    public static final String FUNC_TEST = "test";

    public static final String FUNC_TESTSINGLE = "testSingle";

    @Deprecated
    protected ArrayOfStructAndStruct(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ArrayOfStructAndStruct(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected ArrayOfStructAndStruct(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected ArrayOfStructAndStruct(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> test(List<Foo> foos) {
        final Function function = new Function(
                FUNC_TEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.StaticArray2<Foo>(Foo.class, foos)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> testSingle(Foo foo) {
        final Function function = new Function(
                FUNC_TESTSINGLE, 
                Arrays.<Type>asList(foo), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static ArrayOfStructAndStruct load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ArrayOfStructAndStruct(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static ArrayOfStructAndStruct load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ArrayOfStructAndStruct(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static ArrayOfStructAndStruct load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new ArrayOfStructAndStruct(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static ArrayOfStructAndStruct load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new ArrayOfStructAndStruct(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<ArrayOfStructAndStruct> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(ArrayOfStructAndStruct.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<ArrayOfStructAndStruct> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(ArrayOfStructAndStruct.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<ArrayOfStructAndStruct> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ArrayOfStructAndStruct.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<ArrayOfStructAndStruct> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ArrayOfStructAndStruct.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class Foo extends StaticStruct {
        public Boolean dummy;

        public Foo(Boolean dummy) {
            super(new org.web3j.abi.datatypes.Bool(dummy));
            this.dummy = dummy;
        }

        public Foo(Bool dummy) {
            super(dummy);
            this.dummy = dummy.getValue();
        }
    }
}

