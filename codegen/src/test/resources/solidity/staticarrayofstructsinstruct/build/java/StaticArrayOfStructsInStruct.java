package org.web3j.unittests.java;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.reflection.Parameterized;
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
public class StaticArrayOfStructsInStruct extends Contract {
    public static final String BINARY = "608060405234801561000f575f80fd5b5060c58061001c5f395ff3fe6080604052348015600e575f80fd5b50600436106026575f3560e01c80639acc3bf114602a575b5f80fd5b60406004803603810190603c91906069565b6042565b005b5f80fd5b5f80fd5b5f80fd5b5f60a08284031215606057605f604a565b5b81905092915050565b5f60a08284031215607b57607a6046565b5b5f608684828501604e565b9150509291505056fea2646970667358221220a67d48af17079065cb181167be61364e31cd9d588c89b4a1ca7ceb1a9bf640a864736f6c63430008140033";

    private static String librariesLinkedBinary;

    public static final String FUNC_TEST = "test";

    @Deprecated
    protected StaticArrayOfStructsInStruct(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected StaticArrayOfStructsInStruct(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected StaticArrayOfStructsInStruct(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected StaticArrayOfStructsInStruct(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> test(Config config) {
        final Function function = new Function(
                FUNC_TEST, 
                Arrays.<Type>asList(config), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static StaticArrayOfStructsInStruct load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new StaticArrayOfStructsInStruct(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static StaticArrayOfStructsInStruct load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new StaticArrayOfStructsInStruct(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static StaticArrayOfStructsInStruct load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new StaticArrayOfStructsInStruct(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static StaticArrayOfStructsInStruct load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new StaticArrayOfStructsInStruct(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<StaticArrayOfStructsInStruct> deploy(Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(StaticArrayOfStructsInStruct.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<StaticArrayOfStructsInStruct> deploy(Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(StaticArrayOfStructsInStruct.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<StaticArrayOfStructsInStruct> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(StaticArrayOfStructsInStruct.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<StaticArrayOfStructsInStruct> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(StaticArrayOfStructsInStruct.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
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

    public static class Player extends StaticStruct {
        public String addr;

        public BigInteger timeLeft;

        public Player(String addr, BigInteger timeLeft) {
            super(new org.web3j.abi.datatypes.Address(160, addr), 
                    new org.web3j.abi.datatypes.generated.Uint256(timeLeft));
            this.addr = addr;
            this.timeLeft = timeLeft;
        }

        public Player(Address addr, Uint256 timeLeft) {
            super(addr, timeLeft);
            this.addr = addr.getValue();
            this.timeLeft = timeLeft.getValue();
        }
    }

    public static class Config extends StaticStruct {
        public BigInteger index;

        public List<Player> players;

        public Config(BigInteger index, List<Player> players) {
            super(new org.web3j.abi.datatypes.generated.Uint64(index), 
                    new org.web3j.abi.datatypes.generated.StaticArray2<Player>(Player.class, players));
            this.index = index;
            this.players = players;
        }

        public Config(Uint64 index,
                @Parameterized(type = Player.class) StaticArray2<Player> players) {
            super(index, players);
            this.index = index.getValue();
            this.players = players.getValue();
        }
    }
}

