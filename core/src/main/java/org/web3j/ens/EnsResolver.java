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
package org.web3j.ens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.abi.DefaultFunctionEncoder;
import org.web3j.abi.DefaultFunctionReturnDecoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.ens.OffchainLookup;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.dto.EnsGatewayRequestDTO;
import org.web3j.dto.EnsGatewayResponseDTO;
import org.web3j.ens.contracts.generated.ENS;
import org.web3j.ens.contracts.generated.OffchainResolverContract;
import org.web3j.ens.contracts.generated.PublicResolver;
import org.web3j.ens.contracts.generated.ReverseRegistrar;
import org.web3j.ens.contracts.generated.UniversalResolver;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.NetVersion;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.EnsUtils;
import org.web3j.utils.Numeric;
import org.web3j.utils.Strings;

import static org.web3j.service.HSMHTTPRequestProcessor.JSON;

/** Resolution logic for contract addresses. According to https://eips.ethereum.org/EIPS/eip-2544 */
public class EnsResolver {

    private static final Logger log = LoggerFactory.getLogger(EnsResolver.class);

    public static final long DEFAULT_SYNC_THRESHOLD = 1000 * 60 * 3;

    // Permit number offchain calls  for a single contract call.
    public static final int LOOKUP_LIMIT = 4;

    public static final String REVERSE_NAME_SUFFIX = ".addr.reverse";

    // Sentinel gateway URL viem / the ENSv2 Universal Resolver accept when the caller
    // wants onchain-only resolution (batch handled client-side for CCIP-Read cases).
    public static final String UNIVERSAL_RESOLVER_BATCH_GATEWAY = "x-batch-gateway:true";

    private final Web3j web3j;
    private final int addressLength;
    private final TransactionManager transactionManager;

    private OkHttpClient client = new OkHttpClient();
    private long syncThreshold; // non-final in case this value needs to be tweaked

    public EnsResolver(Web3j web3j, long syncThreshold, int addressLength) {
        this.web3j = web3j;
        transactionManager = new ClientTransactionManager(web3j, null); // don't use empty string
        this.syncThreshold = syncThreshold;
        this.addressLength = addressLength;
    }

    public EnsResolver(Web3j web3j, long syncThreshold) {
        this(web3j, syncThreshold, Keys.ADDRESS_LENGTH_IN_HEX);
    }

    public EnsResolver(Web3j web3j) {
        this(web3j, DEFAULT_SYNC_THRESHOLD);
    }

    public void setSyncThreshold(long syncThreshold) {
        this.syncThreshold = syncThreshold;
    }

    public long getSyncThreshold() {
        return syncThreshold;
    }

    /**
     * Provides an access to a valid public resolver in order to access other API methods.
     *
     * @deprecated
     *     <p>Use {@link EnsResolver#obtainOffchainResolver(String)} instead.
     * @param ensName our user input ENS name
     * @return PublicResolver
     */
    @Deprecated
    protected PublicResolver obtainPublicResolver(String ensName) {
        if (isValidEnsName(ensName, addressLength)) {
            try {
                if (!isSynced()) {
                    throw new EnsResolutionException("Node is not currently synced");
                } else {
                    return lookupResolver(ensName);
                }
            } catch (Exception e) {
                throw new EnsResolutionException("Unable to determine sync status of node", e);
            }
        } else {
            throw new EnsResolutionException("EnsName is invalid: " + ensName);
        }
    }

    /**
     * Provides an access to a valid offchain resolver in order to access other API methods.
     *
     * @param ensName our user input ENS name
     * @return OffchainResolver
     */
    protected OffchainResolverContract obtainOffchainResolver(String ensName) {
        if (isValidEnsName(ensName, addressLength)) {
            boolean isSynced;

            try {
                isSynced = isSynced();
            } catch (Exception e) {
                throw new EnsResolutionException("Unable to determine sync status of node", e);
            }

            if (!isSynced) {
                throw new EnsResolutionException("Node is not currently synced");
            }

            try {
                return lookupOffchainResolver(ensName);
            } catch (Exception e) {
                throw new EnsResolutionException("Unable to get resolver", e);
            }
        } else {
            throw new EnsResolutionException("EnsName is invalid: " + ensName);
        }
    }

    protected OffchainResolverContract obtainOffchainResolver(
            String ensName, Credentials credentials) {
        if (isValidEnsName(ensName, addressLength)) {
            boolean isSynced;

            try {
                isSynced = isSynced();
            } catch (Exception e) {
                throw new EnsResolutionException("Unable to determine sync status of node", e);
            }

            if (!isSynced) {
                throw new EnsResolutionException("Node is not currently synced");
            }

            try {
                return lookupOffchainResolver(ensName, credentials);
            } catch (Exception e) {
                throw new EnsResolutionException("Unable to get resolver", e);
            }
        } else {
            throw new EnsResolutionException("EnsName is invalid: " + ensName);
        }
    }

    /**
     * Resolves an ENS name to an address via the Universal Resolver. Tries the ENSv2 {@code
     * resolveWithGateways} signature first and falls back to legacy {@code resolve} for older
     * deployments. Plain addresses are returned unchanged.
     *
     * @param ensName ENS name or 0x-prefixed address.
     * @return resolved address; {@code null} for blank / "." inputs.
     * @throws EnsResolutionException if the name does not resolve, including the zero-address case
     *     — callers must never silently receive a burn address.
     */
    public String resolve(String ensName) {
        if (Strings.isBlank(ensName) || (ensName.trim().length() == 1 && ensName.contains("."))) {
            return null;
        }
        if (!isValidEnsName(ensName, addressLength)) {
            return ensName;
        }
        try {
            UniversalResolver universalResolver = getUniversalResolverContract();
            byte[] dnsEncoded = Numeric.hexStringToByteArray(NameHash.dnsEncode(ensName));
            byte[] addrCalldata = encodeAddrCall(ensName);

            byte[] innerResult;
            try {
                innerResult =
                        universalResolver
                                .resolveWithGateways(
                                        dnsEncoded,
                                        addrCalldata,
                                        Collections.singletonList(UNIVERSAL_RESOLVER_BATCH_GATEWAY))
                                .send()
                                .component1();
            } catch (Exception ignored) {
                innerResult =
                        universalResolver.resolve(dnsEncoded, addrCalldata).send().component1();
            }

            String resolvedAddress = decodeAddrResult(innerResult);
            if (resolvedAddress == null
                    || EnsUtils.isAddressEmpty(resolvedAddress)
                    || !WalletUtils.isValidAddress(resolvedAddress, addressLength)) {
                throw new EnsResolutionException("Unable to resolve address for name: " + ensName);
            }
            return resolvedAddress;
        } catch (EnsResolutionException e) {
            throw e;
        } catch (Exception e) {
            throw new EnsResolutionException(e);
        }
    }

    private static byte[] encodeAddrCall(String ensName) {
        Function addrFn =
                new Function(
                        "addr",
                        Collections.singletonList(new Bytes32(NameHash.nameHashAsBytes(ensName))),
                        Collections.singletonList(new TypeReference<Address>() {}));
        return Numeric.hexStringToByteArray(FunctionEncoder.encode(addrFn));
    }

    private static String decodeAddrResult(byte[] resultBytes) {
        if (resultBytes == null || resultBytes.length == 0) {
            return null;
        }
        return DefaultFunctionReturnDecoder.decodeAddress(Numeric.toHexString(resultBytes));
    }

    private UniversalResolver getUniversalResolverContract() throws IOException {
        NetVersion netVersion = web3j.netVersion().send();
        String urAddress =
                UniversalResolverContracts.resolveUniversalResolverContract(
                        netVersion.getNetVersion());
        return UniversalResolver.load(
                urAddress, web3j, transactionManager, new DefaultGasProvider());
    }

    protected String resolveOffchain(
            String lookupData, OffchainResolverContract resolver, int lookupCounter)
            throws Exception {
        if (EnsUtils.isEIP3668(lookupData)) {

            OffchainLookup offchainLookup =
                    OffchainLookup.build(Numeric.hexStringToByteArray(lookupData.substring(10)));

            if (!resolver.getContractAddress().equals(offchainLookup.getSender())) {
                throw new EnsResolutionException(
                        "Cannot handle OffchainLookup raised inside nested call");
            }

            String gatewayResult =
                    ccipReadFetch(
                            offchainLookup.getUrls(),
                            offchainLookup.getSender(),
                            Numeric.toHexString(offchainLookup.getCallData()));

            if (gatewayResult == null) {
                throw new EnsResolutionException("CCIP Read disabled or provided no URLs.");
            }

            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            EnsGatewayResponseDTO gatewayResponseDTO =
                    objectMapper.readValue(gatewayResult, EnsGatewayResponseDTO.class);
            String callbackSelector = Numeric.toHexString(offchainLookup.getCallbackFunction());
            List<Type> parameters =
                    Arrays.asList(
                            new DynamicBytes(
                                    Numeric.hexStringToByteArray(gatewayResponseDTO.getData())),
                            new DynamicBytes(offchainLookup.getExtraData()));

            String encodedParams = new DefaultFunctionEncoder().encodeParameters(parameters);
            String encodedFunction = callbackSelector + encodedParams;
            String resolvedNameHex = resolver.executeCallWithoutDecoding(encodedFunction);

            // This protocol can result in multiple lookups being requested by the same contract.
            if (EnsUtils.isEIP3668(resolvedNameHex)) {
                if (lookupCounter <= 0) {
                    throw new EnsResolutionException("Lookup calls is out of limit.");
                }

                return resolveOffchain(lookupData, resolver, --lookupCounter);
            } else {
                byte[] resolvedNameBytes =
                        DefaultFunctionReturnDecoder.decodeDynamicBytes(resolvedNameHex);

                return DefaultFunctionReturnDecoder.decodeAddress(
                        Numeric.toHexString(resolvedNameBytes));
            }
        }

        return lookupData;
    }

    protected String ccipReadFetch(List<String> urls, String sender, String data) {
        List<String> errorMessages = new ArrayList<>();

        for (String url : urls) {
            Request request;
            try {
                request = buildRequest(url, sender, data);
            } catch (JsonProcessingException | EnsResolutionException e) {
                log.error(e.getMessage(), e);
                break;
            }

            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        log.warn("Response body is null, url: {}", url);
                        break;
                    }

                    return new BufferedReader(new InputStreamReader(responseBody.byteStream()))
                            .lines()
                            .collect(Collectors.joining("\n"));
                } else {
                    int statusCode = response.code();
                    // 4xx indicates the result is not present; stop
                    if (statusCode >= 400 && statusCode < 500) {
                        log.error(
                                "Response error during CCIP fetch: url {}, error: {}",
                                url,
                                response.message());
                        throw new EnsResolutionException(response.message());
                    }

                    // 5xx indicates server issue; try the next url
                    errorMessages.add(response.message());

                    log.warn(
                            "Response error 500 during CCIP fetch: url {}, error: {}",
                            url,
                            response.message());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        log.warn(Arrays.toString(errorMessages.toArray()));
        return null;
    }

    protected Request buildRequest(String url, String sender, String data)
            throws JsonProcessingException {
        if (sender == null || !WalletUtils.isValidAddress(sender)) {
            throw new EnsResolutionException("Sender address is null or not valid");
        }
        if (data == null) {
            throw new EnsResolutionException("Data is null");
        }

        // URL expansion
        String href = url;

        if (url.contains("{sender}")) {
            href = href.replace("{sender}", sender);
        }

        if (url.contains("{data}")) {
            href = href.replace("{data}", data);
        }

        Request.Builder builder = new Request.Builder().url(href);

        // According to ERC-3668:
        // - If URL contains {data}, use GET
        // - Otherwise, use POST with JSON payload containing data and sender
        if (url.contains("{data}")) {
            return builder.get().build();
        } else {
            EnsGatewayRequestDTO requestDTO = new EnsGatewayRequestDTO(data, sender);
            ObjectMapper om = ObjectMapperFactory.getObjectMapper();

            return builder.post(RequestBody.create(om.writeValueAsString(requestDTO), JSON))
                    .addHeader("Content-Type", "application/json")
                    .build();
        }
    }

    public TransactionReceipt setReverseName(String name, Credentials credentials)
            throws Exception {
        ReverseRegistrar reverseRegistrar = getReverseRegistrarContract(credentials);
        return reverseRegistrar.setName(name).send();
    }

    public TransactionReceipt setReverseName(
            String addr, String owner, String resolver, String name, Credentials credentials)
            throws Exception {
        ReverseRegistrar reverseRegistrar = getReverseRegistrarContract(credentials);
        return reverseRegistrar.setNameForAddr(addr, owner, resolver, name).send();
    }

    /**
     * Reverse name resolution as documented in the <a
     * href="https://docs.ens.domains/contract-api-reference/reverseregistrar">specification</a>.
     *
     * @param address an ethereum address, example: "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"
     * @return a EnsName registered for provided address
     */
    public String reverseResolve(String address) {
        if (WalletUtils.isValidAddress(address, addressLength)) {
            String reverseName = Numeric.cleanHexPrefix(address) + REVERSE_NAME_SUFFIX;
            PublicResolver resolver = obtainOffchainResolver(reverseName);

            byte[] nameHash = NameHash.nameHashAsBytes(reverseName);
            String name;
            try {
                name = resolver.name(nameHash).send();
            } catch (Exception e) {
                throw new RuntimeException("Unable to execute Ethereum request", e);
            }

            if (!isValidEnsName(name, addressLength)) {
                throw new RuntimeException("Unable to resolve name for address: " + address);
            } else {
                return name;
            }
        } else {
            throw new EnsResolutionException("Address is invalid: " + address);
        }
    }

    private PublicResolver lookupResolver(String ensName) throws Exception {
        return PublicResolver.load(
                getResolverAddress(ensName), web3j, transactionManager, new DefaultGasProvider());
    }

    private OffchainResolverContract lookupOffchainResolver(String ensName) throws Exception {
        return OffchainResolverContract.load(
                getResolverAddress(ensName), web3j, transactionManager, new DefaultGasProvider());
    }

    private OffchainResolverContract lookupOffchainResolver(String ensName, Credentials credentials)
            throws Exception {
        return OffchainResolverContract.load(
                getResolverAddress(ensName), web3j, credentials, new DefaultGasProvider());
    }

    public String getResolverAddress(String ensName) throws Exception {
        ENS ensRegistry = getRegistryContract();
        byte[] nameHash = NameHash.nameHashAsBytes(ensName);
        String address = ensRegistry.resolver(nameHash).send();

        if (EnsUtils.isAddressEmpty(address)) {
            address = getResolverAddress(EnsUtils.getParent(ensName));
        }

        return address;
    }

    public String getOwnerAddress(String ensName) throws Exception {
        ENS ensRegistry = getRegistryContract();
        byte[] nameHash = NameHash.nameHashAsBytes(ensName);
        return ensRegistry.owner(nameHash).send();
    }

    private ENS getRegistryContract() throws IOException {
        NetVersion netVersion = web3j.netVersion().send();
        String registryContract = Contracts.resolveRegistryContract(netVersion.getNetVersion());

        return ENS.load(registryContract, web3j, transactionManager, new DefaultGasProvider());
    }

    protected ReverseRegistrar getReverseRegistrarContract(Credentials credentials)
            throws IOException {
        NetVersion netVersion = web3j.netVersion().send();
        String reverseRegistrarContract =
                ReverseRegistrarContracts.resolveReverseRegistrarContract(
                        netVersion.getNetVersion());

        return ReverseRegistrar.load(
                reverseRegistrarContract, web3j, credentials, new DefaultGasProvider());
    }

    public EnsMetadataResponse getEnsMetadata(String name) throws IOException {
        NetVersion netVersion = web3j.netVersion().send();
        byte[] nameHash = NameHash.nameHashAsBytes(name);
        String apiUrl =
                NameWrapperUrl.getEnsMetadataApi(netVersion.getNetVersion())
                        + Numeric.toHexString(nameHash);

        Request request = new Request.Builder().url(apiUrl).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(
                        "Failed to fetch ENS metadata. HTTP error code: " + response.code());
            }

            // Parse the JSON response
            assert response.body() != null;
            String responseBody = response.body().string();
            return new ObjectMapper().readValue(responseBody, EnsMetadataResponse.class);
        }
    }

    public String getEnsText(String name, String key) throws Exception {
        OffchainResolverContract offchainResolverContract = obtainOffchainResolver(name);
        byte[] nameHash = NameHash.nameHashAsBytes(name);
        return offchainResolverContract.text(nameHash, key).send();
    }

    public TransactionReceipt setEnsText(
            String name, String key, String value, Credentials credentials) throws Exception {
        OffchainResolverContract offchainResolverContract =
                obtainOffchainResolver(name, credentials);
        byte[] nameHash = NameHash.nameHashAsBytes(name);
        return offchainResolverContract.setText(nameHash, key, value).send();
    }

    boolean isSynced() throws Exception {
        EthSyncing ethSyncing = web3j.ethSyncing().send();
        if (ethSyncing.isSyncing()) {
            return false;
        } else {
            EthBlock ethBlock =
                    web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
            long timestamp = ethBlock.getBlock().getTimestamp().longValue() * 1000;

            return System.currentTimeMillis() - syncThreshold < timestamp;
        }
    }

    public static boolean isValidEnsName(String input) {
        return isValidEnsName(input, Keys.ADDRESS_LENGTH_IN_HEX);
    }

    public static boolean isValidEnsName(String input, int addressLength) {
        return input != null // will be set to null on new Contract creation
                && (input.contains(".") || !WalletUtils.isValidAddress(input, addressLength));
    }

    public void setHttpClient(OkHttpClient client) {
        this.client = client;
    }
}
