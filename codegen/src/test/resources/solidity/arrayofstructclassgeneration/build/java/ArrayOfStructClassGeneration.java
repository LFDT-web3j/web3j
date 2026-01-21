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
public class ArrayOfStructClassGeneration extends Contract {
    public static final String BINARY = "608060405234801561000f575f80fd5b5060c88061001c5f395ff3fe6080604052348015600e575f80fd5b50600436106026575f3560e01c80631ae18a6414602a575b5f80fd5b60406004803603810190603c9190606c565b6042565b005b5f80fd5b5f80fd5b5f80fd5b5f819050826020600202820111156066576065604a565b5b92915050565b5f60408284031215607e57607d6046565b5b5f608984828501604e565b9150509291505056fea26469706673582212207623489a002fb52ee39ece715271c1ed1515fd033f97ca89ccbfbab8a2f493ab64736f6c63430008140033";

    private static String librariesLinkedBinary;

    public static final String FUNC_TEST = "test";

    @Deprecated
    protected ArrayOfStructClassGeneration(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ArrayOfStructClassGeneration(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected ArrayOfStructClassGeneration(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected ArrayOfStructClassGeneration(String contractAddress, Web3j web3j,
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

    @Deprecated
    public static ArrayOfStructClassGeneration load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ArrayOfStructClassGeneration(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static ArrayOfStructClassGeneration load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ArrayOfStructClassGeneration(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static ArrayOfStructClassGeneration load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new ArrayOfStructClassGeneration(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static ArrayOfStructClassGeneration load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new ArrayOfStructClassGeneration(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<ArrayOfStructClassGeneration> deploy(Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(ArrayOfStructClassGeneration.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<ArrayOfStructClassGeneration> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(ArrayOfStructClassGeneration.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<ArrayOfStructClassGeneration> deploy(Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ArrayOfStructClassGeneration.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<ArrayOfStructClassGeneration> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ArrayOfStructClassGeneration.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
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

