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
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StructType;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.JsonRpcError;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.exceptions.ContractCallException;
import org.web3j.tx.gas.ContractEIP1559GasProvider;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.EmptyTransactionReceipt;

import static org.web3j.crypto.Hash.sha3String;
import static org.web3j.utils.Numeric.cleanHexPrefix;
import static org.web3j.utils.RevertReasonExtractor.extractRevertReason;
import static org.web3j.utils.RevertReasonExtractor.extractRevertReasonEncodedData;

/**
 * Solidity contract type abstraction for interacting with smart contracts via native Java types.
 */
@SuppressWarnings({"WeakerAccess", "deprecation"})
public abstract class Contract extends ManagedTransaction {

    // https://www.reddit.com/r/ethereum/comments/5g8ia6/attention_miners_we_recommend_raising_gas_limit/
    /**
     * @deprecated ...
     * @see org.web3j.tx.gas.DefaultGasProvider
     */
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    public static final String BIN_NOT_PROVIDED = "Bin file was not provided";
    public static final String FUNC_DEPLOY = "deploy";
    protected final String contractBinary;
    protected String contractAddress;
    protected ContractGasProvider gasProvider;
    protected TransactionReceipt transactionReceipt;
    protected Map<String, String> deployedAddresses;
    protected DefaultBlockParameter defaultBlockParameter = DefaultBlockParameterName.LATEST;
    private static final List<String> METADATA_HASH_INDICATORS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "a165627a7a72305820" /*Swarm legacy (bzzr0)*/,
                            "a265627a7a72315820" /*Swarm (bzzr1)*/,
                            "a2646970667358221220" /*IPFS*/,
                            "a164736f6c634300080a000a" /*solc (None)*/));

    protected Contract(
            String contractBinary,
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider gasProvider) {

        this(
                new EnsResolver(web3j),
                contractBinary,
                contractAddress,
                web3j,
                transactionManager,
                gasProvider);
    }

    protected Contract(
            EnsResolver ensResolver,
            String contractBinary,
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider gasProvider) {

        super(ensResolver, web3j, transactionManager);
        this.contractAddress = resolveContractAddress(contractAddress);
        this.contractBinary = contractBinary;
        this.gasProvider = gasProvider;
    }

    protected Contract(
            String contractBinary,
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider gasProvider) {
        this(
                new EnsResolver(web3j),
                contractBinary,
                contractAddress,
                web3j,
                new RawTransactionManager(web3j, credentials),
                gasProvider);
    }

    @Deprecated
    protected Contract(
            String contractBinary,
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        this(
                new EnsResolver(web3j),
                contractBinary,
                contractAddress,
                web3j,
                transactionManager,
                new StaticGasProvider(gasPrice, gasLimit));
    }

    @Deprecated
    protected Contract(
            String contractBinary,
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        this(
                contractBinary,
                contractAddress,
                web3j,
                new RawTransactionManager(web3j, credentials),
                gasPrice,
                gasLimit);
    }

    @Deprecated
    protected Contract(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        this("", contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    @Deprecated
    protected Contract(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        this(
                "",
                contractAddress,
                web3j,
                new RawTransactionManager(web3j, credentials),
                gasPrice,
                gasLimit);
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setTransactionReceipt(TransactionReceipt transactionReceipt) {
        this.transactionReceipt = transactionReceipt;
    }

    public String getContractBinary() {
        return contractBinary;
    }

    public void setGasProvider(ContractGasProvider gasProvider) {
        this.gasProvider = gasProvider;
    }

    /**
     * Allow {@code gasPrice} to be set.
     *
     * @param newPrice gas price to use for subsequent transactions
     * @deprecated use ContractGasProvider
     */
    public void setGasPrice(BigInteger newPrice) {
        this.gasProvider = new StaticGasProvider(newPrice, gasProvider.getGasLimit());
    }

    /**
     * Get the current {@code gasPrice} value this contract uses when executing transactions.
     *
     * @return the gas price set on this contract
     * @deprecated use ContractGasProvider
     */
    public BigInteger getGasPrice() {
        return gasProvider.getGasPrice();
    }

    /**
     * Check that the contract deployed at the address associated with this smart contract wrapper
     * is in fact the contract you believe it is.
     *
     * <p>This method uses the <a
     * href="https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getcode">eth_getCode</a> method to
     * get the contract byte code and validates it against the byte code stored in this smart
     * contract wrapper.
     *
     * @return true if the contract is valid
     * @throws IOException if unable to connect to web3j node
     */
    public boolean isValid() throws IOException {
        if (contractBinary.equals(BIN_NOT_PROVIDED)) {
            throw new UnsupportedOperationException(
                    "Contract binary not present in contract wrapper, "
                            + "please generate your wrapper using -abiFile=<file>");
        }

        if (contractAddress.equals("")) {
            throw new UnsupportedOperationException(
                    "Contract binary not present, you will need to regenerate your smart "
                            + "contract wrapper with web3j v2.2.0+");
        }

        EthGetCode ethGetCode =
                transactionManager.getCode(contractAddress, DefaultBlockParameterName.LATEST);
        if (ethGetCode.hasError()) {
            return false;
        }

        String code = cleanHexPrefix(ethGetCode.getCode());

        int metadataIndex = -1;
        for (String metadataIndicator : METADATA_HASH_INDICATORS) {
            metadataIndex = code.indexOf(metadataIndicator);

            if (metadataIndex != -1) {
                code = code.substring(0, metadataIndex);
                break;
            }
        }
        // There may be multiple contracts in the Solidity bytecode, hence we only check for a
        // match with a subset
        return !code.isEmpty() && contractBinary.contains(code);
    }

    /**
     * If this Contract instance was created at deployment, the TransactionReceipt associated with
     * the initial creation will be provided, e.g. via a <em>deploy</em> method. This will not
     * persist for Contracts instances constructed via a <em>load</em> method.
     *
     * @return the TransactionReceipt generated at contract deployment
     */
    public Optional<TransactionReceipt> getTransactionReceipt() {
        return Optional.ofNullable(transactionReceipt);
    }

    /**
     * Sets the default block parameter. This use useful if one wants to query historical state of a
     * contract.
     *
     * @param defaultBlockParameter the default block parameter
     */
    public void setDefaultBlockParameter(DefaultBlockParameter defaultBlockParameter) {
        this.defaultBlockParameter = defaultBlockParameter;
    }

    /**
     * Execute constant function call - i.e. a call that does not change state of the contract
     *
     * @param function to call
     * @return {@link List} of values returned by function call
     */
    private List<Type> executeCall(Function function) throws IOException {
        String encodedFunction = FunctionEncoder.encode(function);

        String value = call(contractAddress, encodedFunction, defaultBlockParameter);

        return FunctionReturnDecoder.decode(value, function.getOutputParameters());
    }

    protected String executeCallWithoutDecoding(Function function) throws IOException {
        String encodedFunction = FunctionEncoder.encode(function);
        return call(contractAddress, encodedFunction, defaultBlockParameter);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Type> T executeCallSingleValueReturn(Function function)
            throws IOException {
        List<Type> values = executeCall(function);
        if (!values.isEmpty()) {
            return (T) values.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends Type, R> R executeCallSingleValueReturn(
            Function function, Class<R> returnType) throws IOException {
        T result = executeCallSingleValueReturn(function);
        if (result == null) {
            throw new ContractCallException("Empty value (0x) returned from contract");
        }

        Object value = result.getValue();
        if (returnType.isAssignableFrom(result.getClass())) {
            return (R) result;
        } else if (returnType.isAssignableFrom(value.getClass())) {
            return (R) value;
        } else if (result.getClass().equals(Address.class) && returnType.equals(String.class)) {
            return (R) result.toString(); // cast isn't necessary
        } else {
            throw new ContractCallException(
                    "Unable to convert response: "
                            + value
                            + " to expected type: "
                            + returnType.getSimpleName());
        }
    }

    protected List<Type> executeCallMultipleValueReturn(Function function) throws IOException {
        return executeCall(function);
    }

    protected TransactionReceipt executeTransaction(Function function)
            throws IOException, TransactionException {
        return executeTransaction(function, BigInteger.ZERO);
    }

    private TransactionReceipt executeTransaction(Function function, BigInteger weiValue)
            throws IOException, TransactionException {
        return executeTransaction(FunctionEncoder.encode(function), weiValue, function.getName());
    }

    TransactionReceipt executeTransaction(String data, BigInteger weiValue, String funcName)
            throws TransactionException, IOException {

        return executeTransaction(data, weiValue, funcName, false);
    }

    /**
     * Given the duration required to execute a transaction.
     *
     * @param data to send in transaction
     * @param weiValue in Wei to send in transaction
     * @return {@link Optional} containing our transaction receipt
     * @throws IOException if the call to the node fails
     * @throws TransactionException if the transaction was not mined while waiting
     */
    TransactionReceipt executeTransaction(
            String data, BigInteger weiValue, String funcName, boolean constructor)
            throws TransactionException, IOException {

        TransactionReceipt receipt = null;
        try {
            if (gasProvider instanceof ContractEIP1559GasProvider) {
                ContractEIP1559GasProvider eip1559GasProvider =
                        (ContractEIP1559GasProvider) gasProvider;

                receipt =
                        sendEIP1559(
                                eip1559GasProvider.getChainId(),
                                contractAddress,
                                data,
                                weiValue,
                                eip1559GasProvider.getGasLimit(
                                        getGenericTransaction(data, constructor, weiValue)),
                                eip1559GasProvider.getMaxPriorityFeePerGas(),
                                eip1559GasProvider.getMaxFeePerGas(),
                                constructor);
            }

            if (receipt == null) {
                receipt =
                        send(
                                contractAddress,
                                data,
                                weiValue,
                                gasProvider.getGasPrice(),
                                gasProvider.getGasLimit(
                                        getGenericTransaction(data, constructor, weiValue)),
                                constructor);
            }
        } catch (JsonRpcError error) {

            if (error.getData() != null) {
                throw new TransactionException(error.getData().toString());
            } else {
                throw new TransactionException(
                        String.format(
                                "JsonRpcError thrown with code %d. Message: %s",
                                error.getCode(), error.getMessage()));
            }
        }

        if (!(receipt instanceof EmptyTransactionReceipt)
                && receipt != null
                && !receipt.isStatusOK()) {
            throw new TransactionException(
                    String.format(
                            "Transaction %s has failed with status: %s. "
                                    + "Gas used: %s. "
                                    + "Revert reason: '%s'.",
                            receipt.getTransactionHash(),
                            receipt.getStatus(),
                            receipt.getGasUsedRaw() != null
                                    ? receipt.getGasUsed().toString()
                                    : "unknown",
                            extractRevertReason(receipt, data, web3j, true, weiValue)),
                    receipt,
                    extractRevertReasonEncodedData(receipt, data, web3j, weiValue));
        }
        return receipt;
    }

    protected Transaction getGenericTransaction(
            String data, boolean constructor, BigInteger weiValue) {
        if (constructor) {
            return Transaction.createContractTransaction(
                    this.transactionManager.getFromAddress(),
                    BigInteger.ONE,
                    gasProvider.getGasPrice(),
                    gasProvider.getGasLimit(),
                    weiValue,
                    data);
        } else {
            return Transaction.createFunctionCallTransaction(
                    this.transactionManager.getFromAddress(),
                    BigInteger.ONE,
                    gasProvider.getGasPrice(),
                    gasProvider.getGasLimit(),
                    contractAddress,
                    weiValue,
                    data);
        }
    }

    protected <T extends Type> RemoteFunctionCall<T> executeRemoteCallSingleValueReturn(
            Function function) {
        return new RemoteFunctionCall<>(function, () -> executeCallSingleValueReturn(function));
    }

    protected <T> RemoteFunctionCall<T> executeRemoteCallSingleValueReturn(
            Function function, Class<T> returnType) {
        return new RemoteFunctionCall<>(
                function, () -> executeCallSingleValueReturn(function, returnType));
    }

    protected RemoteFunctionCall<List<Type>> executeRemoteCallMultipleValueReturn(
            Function function) {
        return new RemoteFunctionCall<>(function, () -> executeCallMultipleValueReturn(function));
    }

    protected RemoteFunctionCall<TransactionReceipt> executeRemoteCallTransaction(
            Function function) {
        return new RemoteFunctionCall<>(function, () -> executeTransaction(function));
    }

    protected RemoteFunctionCall<TransactionReceipt> executeRemoteCallTransaction(
            Function function, BigInteger weiValue) {
        return new RemoteFunctionCall<>(function, () -> executeTransaction(function, weiValue));
    }

    private static <T extends Contract> T create(
            T contract, String binary, String encodedConstructor, BigInteger value)
            throws IOException, TransactionException {
        TransactionReceipt transactionReceipt =
                contract.executeTransaction(binary + encodedConstructor, value, FUNC_DEPLOY, true);

        String contractAddress = transactionReceipt.getContractAddress();
        if (contractAddress == null) {
            throw new RuntimeException("Empty contract address returned");
        }
        contract.setContractAddress(contractAddress);
        contract.setTransactionReceipt(transactionReceipt);

        return contract;
    }

    public static class LinkReference {
        final String source;
        final String libraryName;
        final Address address;

        public LinkReference(String source, String libraryName, Address address) {
            this.source = source;
            this.libraryName = libraryName;
            this.address = address;
        }
    }

    public static String linkBinaryWithReferences(String binary, List<LinkReference> links) {
        String replacingBinary = binary;
        for (LinkReference link : links) {
            // solc / hardhat convention
            String libSourceName = link.source + ":" + link.libraryName;
            String placeHolder = "__$" + sha3String(libSourceName).substring(2, 36) + "$__";
            String addressReplacement = cleanHexPrefix(link.address.toString());
            replacingBinary = replacingBinary.replace(placeHolder, addressReplacement);

            // old version solc
            String linkString = link.source + ":" + link.libraryName;
            String oldSolcPlaceHolder =
                    "__" + linkString + "_".repeat(40 - linkString.length() - 2);
            replacingBinary = replacingBinary.replace(oldSolcPlaceHolder, addressReplacement);

            // truffle old version
            String trufflePlaceHolder =
                    "__" + link.libraryName + "_".repeat(40 - link.libraryName.length() - 2);
            replacingBinary = replacingBinary.replace(trufflePlaceHolder, addressReplacement);
        }
        return replacingBinary;
    }

    protected static <T extends Contract> T deploy(
            Class<T> type,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger value)
            throws RuntimeException, TransactionException {

        try {
            Constructor<T> constructor =
                    type.getDeclaredConstructor(
                            String.class,
                            Web3j.class,
                            Credentials.class,
                            ContractGasProvider.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            T contract = constructor.newInstance(null, web3j, credentials, contractGasProvider);

            return create(contract, binary, encodedConstructor, value);
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends Contract> T deploy(
            Class<T> type,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger value)
            throws RuntimeException, TransactionException {

        try {
            Constructor<T> constructor =
                    type.getDeclaredConstructor(
                            String.class,
                            Web3j.class,
                            TransactionManager.class,
                            ContractGasProvider.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            T contract =
                    constructor.newInstance(null, web3j, transactionManager, contractGasProvider);
            return create(contract, binary, encodedConstructor, value);
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    protected static <T extends Contract> T deploy(
            Class<T> type,
            Web3j web3j,
            Credentials credentials,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            String encodedConstructor,
            BigInteger value)
            throws RuntimeException, TransactionException {

        return deploy(
                type,
                web3j,
                credentials,
                new StaticGasProvider(gasPrice, gasLimit),
                binary,
                encodedConstructor,
                value);
    }

    @Deprecated
    protected static <T extends Contract> T deploy(
            Class<T> type,
            Web3j web3j,
            TransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            String encodedConstructor,
            BigInteger value)
            throws RuntimeException, TransactionException {

        return deploy(
                type,
                web3j,
                transactionManager,
                new StaticGasProvider(gasPrice, gasLimit),
                binary,
                encodedConstructor,
                value);
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Web3j web3j,
            Credentials credentials,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            String encodedConstructor,
            BigInteger value) {
        return new RemoteCall<>(
                () ->
                        deploy(
                                type,
                                web3j,
                                credentials,
                                gasPrice,
                                gasLimit,
                                binary,
                                encodedConstructor,
                                value));
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Web3j web3j,
            Credentials credentials,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            String encodedConstructor) {
        return deployRemoteCall(
                type,
                web3j,
                credentials,
                gasPrice,
                gasLimit,
                binary,
                encodedConstructor,
                BigInteger.ZERO);
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger value) {
        return new RemoteCall<>(
                () ->
                        deploy(
                                type,
                                web3j,
                                credentials,
                                contractGasProvider,
                                binary,
                                encodedConstructor,
                                value));
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor) {
        return new RemoteCall<>(
                () ->
                        deploy(
                                type,
                                web3j,
                                credentials,
                                contractGasProvider,
                                binary,
                                encodedConstructor,
                                BigInteger.ZERO));
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Web3j web3j,
            TransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            String encodedConstructor,
            BigInteger value) {
        return new RemoteCall<>(
                () ->
                        deploy(
                                type,
                                web3j,
                                transactionManager,
                                gasPrice,
                                gasLimit,
                                binary,
                                encodedConstructor,
                                value));
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Web3j web3j,
            TransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            String encodedConstructor) {
        return deployRemoteCall(
                type,
                web3j,
                transactionManager,
                gasPrice,
                gasLimit,
                binary,
                encodedConstructor,
                BigInteger.ZERO);
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger value) {
        return new RemoteCall<>(
                () ->
                        deploy(
                                type,
                                web3j,
                                transactionManager,
                                contractGasProvider,
                                binary,
                                encodedConstructor,
                                value));
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor) {
        return new RemoteCall<>(
                () ->
                        deploy(
                                type,
                                web3j,
                                transactionManager,
                                contractGasProvider,
                                binary,
                                encodedConstructor,
                                BigInteger.ZERO));
    }

    public static EventValues staticExtractEventParameters(Event event, Log log) {
        final List<String> topics = log.getTopics();
        String encodedEventSignature = EventEncoder.encode(event);
        if (topics == null || topics.isEmpty() || !topics.get(0).equals(encodedEventSignature)) {
            return null;
        }

        List<Type> indexedValues = new ArrayList<>();
        List<Type> nonIndexedValues =
                FunctionReturnDecoder.decode(log.getData(), event.getNonIndexedParameters());

        List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
        for (int i = 0; i < indexedParameters.size(); i++) {
            Type value =
                    FunctionReturnDecoder.decodeIndexedValue(
                            topics.get(i + 1), indexedParameters.get(i));
            indexedValues.add(value);
        }
        return new EventValues(indexedValues, nonIndexedValues);
    }

    protected String resolveContractAddress(String contractAddress) {
        return ensResolver.resolve(contractAddress);
    }

    protected EventValues extractEventParameters(Event event, Log log) {
        return staticExtractEventParameters(event, log);
    }

    protected List<EventValues> extractEventParameters(
            Event event, TransactionReceipt transactionReceipt) {
        return transactionReceipt.getLogs().stream()
                .map(log -> extractEventParameters(event, log))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected EventValuesWithLog extractEventParametersWithLog(Event event, Log log) {
        return staticExtractEventParametersWithLog(event, log);
    }

    protected static EventValuesWithLog staticExtractEventParametersWithLog(Event event, Log log) {
        final EventValues eventValues = staticExtractEventParameters(event, log);
        return (eventValues == null) ? null : new EventValuesWithLog(eventValues, log);
    }

    protected List<EventValuesWithLog> extractEventParametersWithLog(
            Event event, TransactionReceipt transactionReceipt) {
        return transactionReceipt.getLogs().stream()
                .map(log -> extractEventParametersWithLog(event, log))
                .filter(Objects::nonNull)
                .toList();
    }

    protected static List<EventValuesWithLog> staticExtractEventParametersWithLog(
            Event event, TransactionReceipt transactionReceipt) {
        return transactionReceipt.getLogs().stream()
                .map(log -> staticExtractEventParametersWithLog(event, log))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Subclasses should implement this method to return pre-existing addresses for deployed
     * contracts.
     *
     * @param networkId the network id, for example "1" for the main-net, "3" for ropsten, etc.
     * @return the deployed address of the contract, if known, and null otherwise.
     */
    protected String getStaticDeployedAddress(String networkId) {
        return null;
    }

    public final void setDeployedAddress(String networkId, String address) {
        if (deployedAddresses == null) {
            deployedAddresses = new HashMap<>();
        }
        deployedAddresses.put(networkId, address);
    }

    public final String getDeployedAddress(String networkId) {
        String addr = null;
        if (deployedAddresses != null) {
            addr = deployedAddresses.get(networkId);
        }
        return addr == null ? getStaticDeployedAddress(networkId) : addr;
    }

    /** Adds a log field to {@link EventValues}. */
    public static class EventValuesWithLog {
        private final EventValues eventValues;
        private final Log log;

        private EventValuesWithLog(EventValues eventValues, Log log) {
            this.eventValues = eventValues;
            this.log = log;
        }

        public List<Type> getIndexedValues() {
            return eventValues.getIndexedValues();
        }

        public List<Type> getNonIndexedValues() {
            return eventValues.getNonIndexedValues();
        }

        public Log getLog() {
            return log;
        }
    }

    @SuppressWarnings("unchecked")
    protected static <S extends Type, T> List<T> convertToNative(List<S> arr) {
        List<T> out = new ArrayList<>();
        for (final S s : arr) {
            if (StructType.class.isAssignableFrom(s.getClass())) {
                out.add((T) s);
            } else {
                out.add((T) s.getValue());
            }
        }
        return out;
    }
}
