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
public class SameInnerStructName extends Contract {
    public static final String BINARY = "608060405234801561000f575f80fd5b5060f18061001c5f395ff3fe6080604052348015600e575f80fd5b50600436106026575f3560e01c8063b1a29dbb14602a575b5f80fd5b60406004803603810190603c91906084565b6042565b005b5f80fd5b5f80fd5b5f80fd5b5f60208284031215606057605f604a565b5b81905092915050565b5f60208284031215607b57607a604a565b5b81905092915050565b5f806040838503121560975760966046565b5b5f60a285828601604e565b925050602060b1858286016069565b915050925092905056fea2646970667358221220024a769b6d57f11f6f5df0145ce9d31d8dd2ec08b9a923116f016e14a0912bb464736f6c63430008140033";

    private static String librariesLinkedBinary;

    public static final String FUNC_TEST = "test";

    @Deprecated
    protected SameInnerStructName(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SameInnerStructName(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SameInnerStructName(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SameInnerStructName(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> test(Foo_Info foo, Bar_Info bar) {
        final Function function = new Function(
                FUNC_TEST, 
                Arrays.<Type>asList(foo, 
                bar), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static SameInnerStructName load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SameInnerStructName(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SameInnerStructName load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SameInnerStructName(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SameInnerStructName load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SameInnerStructName(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SameInnerStructName load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SameInnerStructName(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<SameInnerStructName> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SameInnerStructName.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<SameInnerStructName> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SameInnerStructName.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<SameInnerStructName> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SameInnerStructName.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<SameInnerStructName> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SameInnerStructName.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
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

    public static class Foo_Info extends StaticStruct {
        public Boolean dummy;

        public Foo_Info(Boolean dummy) {
            super(new org.web3j.abi.datatypes.Bool(dummy));
            this.dummy = dummy;
        }

        public Foo_Info(Bool dummy) {
            super(dummy);
            this.dummy = dummy.getValue();
        }
    }

    public static class Bar_Info extends StaticStruct {
        public Boolean dummy;

        public Bar_Info(Boolean dummy) {
            super(new org.web3j.abi.datatypes.Bool(dummy));
            this.dummy = dummy;
        }

        public Bar_Info(Bool dummy) {
            super(dummy);
            this.dummy = dummy.getValue();
        }
    }
}

