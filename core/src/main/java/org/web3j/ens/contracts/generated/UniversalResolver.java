package org.web3j.ens.contracts.generated;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
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
 * <p>Generated with web3j version 5.0.3-SNAPSHOT.
 */
@SuppressWarnings("rawtypes")
@Generated("org.web3j.codegen.SolidityFunctionWrapperGenerator")
public class UniversalResolver extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_RESOLVE = "resolve";

    public static final String FUNC_RESOLVEWITHGATEWAYS = "resolveWithGateways";

    public static final String FUNC_REVERSE = "reverse";

    public static final String FUNC_REVERSEWITHGATEWAYS = "reverseWithGateways";

    @Deprecated
    protected UniversalResolver(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected UniversalResolver(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected UniversalResolver(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected UniversalResolver(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<Tuple2<byte[], String>> resolve(byte[] name, byte[] data) {
        final Function function = new Function(FUNC_RESOLVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(name), 
                new org.web3j.abi.datatypes.DynamicBytes(data)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}, new TypeReference<Address>() {}));
        return new RemoteFunctionCall<Tuple2<byte[], String>>(function,
                new Callable<Tuple2<byte[], String>>() {
                    @Override
                    public Tuple2<byte[], String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<byte[], String>(
                                (byte[]) results.get(0).getValue(), 
                                (String) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<byte[], String>> resolveWithGateways(byte[] name, byte[] data,
            List<String> gateways) {
        final Function function = new Function(FUNC_RESOLVEWITHGATEWAYS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(name), 
                new org.web3j.abi.datatypes.DynamicBytes(data), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Utf8String>(
                        org.web3j.abi.datatypes.Utf8String.class,
                        org.web3j.abi.Utils.typeMap(gateways, org.web3j.abi.datatypes.Utf8String.class))), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}, new TypeReference<Address>() {}));
        return new RemoteFunctionCall<Tuple2<byte[], String>>(function,
                new Callable<Tuple2<byte[], String>>() {
                    @Override
                    public Tuple2<byte[], String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<byte[], String>(
                                (byte[]) results.get(0).getValue(), 
                                (String) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple3<String, String, String>> reverse(byte[] lookupAddress,
            BigInteger coinType) {
        final Function function = new Function(FUNC_REVERSE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(lookupAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(coinType)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}));
        return new RemoteFunctionCall<Tuple3<String, String, String>>(function,
                new Callable<Tuple3<String, String, String>>() {
                    @Override
                    public Tuple3<String, String, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, String>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple3<String, String, String>> reverseWithGateways(
            byte[] lookupAddress, BigInteger coinType, List<String> gateways) {
        final Function function = new Function(FUNC_REVERSEWITHGATEWAYS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(lookupAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(coinType), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Utf8String>(
                        org.web3j.abi.datatypes.Utf8String.class,
                        org.web3j.abi.Utils.typeMap(gateways, org.web3j.abi.datatypes.Utf8String.class))), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}));
        return new RemoteFunctionCall<Tuple3<String, String, String>>(function,
                new Callable<Tuple3<String, String, String>>() {
                    @Override
                    public Tuple3<String, String, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, String>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue());
                    }
                });
    }

    @Deprecated
    public static UniversalResolver load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new UniversalResolver(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static UniversalResolver load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new UniversalResolver(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static UniversalResolver load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new UniversalResolver(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static UniversalResolver load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new UniversalResolver(contractAddress, web3j, transactionManager, contractGasProvider);
    }
}
