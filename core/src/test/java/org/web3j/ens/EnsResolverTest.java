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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.dto.EnsGatewayResponseDTO;
import org.web3j.ens.contracts.generated.OffchainResolverContract;
import org.web3j.ens.contracts.generated.ReverseRegistrar;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.NetVersion;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ChainIdLong;
import org.web3j.utils.EnsUtils;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.web3j.ens.EnsResolver.DEFAULT_SYNC_THRESHOLD;
import static org.web3j.ens.EnsResolver.isValidEnsName;
import static org.web3j.protocol.http.HttpService.JSON_MEDIA_TYPE;

class EnsResolverTest {

    private Web3j web3j;
    private Web3jService web3jService;
    private EnsResolver ensResolver;

    private ObjectMapper om = ObjectMapperFactory.getObjectMapper();

    private static List<String> urls = new ArrayList<>();

    private String sender;

    private String data;

    public static String LOOKUP_HEX =
            "0x556f1830000000000000000000000000c1735677a60884abbcf72295e88d47764beda28200000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000000000000000000000160f4d4d2f800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000028000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000004768747470733a2f2f6f6666636861696e2d7265736f6c7665722d6578616d706c652e75632e722e61707073706f742e636f6d2f7b73656e6465727d2f7b646174617d2e6a736f6e0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e49061b92300000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000001701310f6f6666636861696e6578616d706c65036574680000000000000000000000000000000000000000000000000000000000000000000000000000000000243b3b57de1c9fb8c1fe76f464ccec6d2c003169598fdfcbcb6bbddf6af9c097a39fa0048c000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e49061b92300000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000001701310f6f6666636861696e6578616d706c65036574680000000000000000000000000000000000000000000000000000000000000000000000000000000000243b3b57de1c9fb8c1fe76f464ccec6d2c003169598fdfcbcb6bbddf6af9c097a39fa0048c0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

    public static String RESOLVED_NAME_HEX =
            "0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000002000000000000000000000000041563129cdbbd0c5d3e1c86cf9563926b243834d";

    @BeforeAll
    static void beforeAll() {
        urls.add("https://example-1.com/gateway/{sender}/{data}.json");
        urls.add("https://example-2.com/gateway/{sender}/{data}.json");
    }

    @BeforeEach
    public void setUp() {
        web3jService = mock(Web3jService.class);
        web3j = Web3j.build(web3jService);
        ensResolver = new EnsResolver(web3j);
        data = "0x00112233";
        sender = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
    }

    @Test
    void testResolve() throws Exception {
        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));
        when(web3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);

        // UniversalResolver.resolveWithGateways(...) returns (bytes resultBytes, address resolver)
        // where resultBytes is the ABI-encoded addr(bytes32) return: a 32-byte zero-padded address.
        String urResponse =
                "0x"
                        // offset to resultBytes
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        // resolver address (value is irrelevant for this test)
                        + "0000000000000000000000004c641fb9bad9b60ef180c31f56051ce826d21a9a"
                        // resultBytes length = 32
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        // resolved address, zero-padded to 32 bytes
                        + "00000000000000000000000019e03255f667bdfd50a32722df860b1eeaf4d635";
        EthCall ethCall = new EthCall();
        ethCall.setResult(urResponse);
        when(web3jService.send(any(Request.class), eq(EthCall.class))).thenReturn(ethCall);

        assertEquals(
                "0x19e03255f667bdfd50a32722df860b1eeaf4d635", ensResolver.resolve("web3j.eth"));
    }

    @Test
    void testResolveEnsNameEmptyOrDot() {
        assertNull(ensResolver.resolve(" "));
        assertNull(ensResolver.resolve(""));
        assertNull(ensResolver.resolve("."));
        assertNull(ensResolver.resolve(" . "));
    }

    @Test
    void testResolveThrowsWhenZeroAddressReturned() throws Exception {
        // Names with no addr record: resolvers return 0x0000...0000. resolve() must
        // raise EnsResolutionException so callers never silently get a burn address.
        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));
        when(web3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);

        String urResponse =
                "0x"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000004c641fb9bad9b60ef180c31f56051ce826d21a9a"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000000";
        EthCall ethCall = new EthCall();
        ethCall.setResult(urResponse);
        when(web3jService.send(any(Request.class), eq(EthCall.class))).thenReturn(ethCall);

        assertThrows(EnsResolutionException.class, () -> ensResolver.resolve("nope.eth"));
    }

    @Test
    void testResolveFallsBackToLegacyResolveSignature() throws Exception {
        // resolveWithGateways(bytes,bytes,string[]) is unavailable on this UR deployment
        // (call returns 0x simulating method-not-found). resolve() must fall back to the
        // older resolve(bytes,bytes) signature, which returns just (bytes resultBytes).
        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));
        when(web3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);

        EthCall emptyResponse = new EthCall();
        emptyResponse.setResult("0x");

        String legacyResponse =
                "0x"
                        // offset to resultBytes
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        // resultBytes length = 32
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        // ABI-encoded addr() result
                        + "00000000000000000000000019e03255f667bdfd50a32722df860b1eeaf4d635";
        EthCall legacyCall = new EthCall();
        legacyCall.setResult(legacyResponse);

        when(web3jService.send(any(Request.class), eq(EthCall.class)))
                .thenReturn(emptyResponse, legacyCall);

        assertEquals(
                "0x19e03255f667bdfd50a32722df860b1eeaf4d635", ensResolver.resolve("web3j.eth"));
    }

    @Test
    void testResolveThrowsWhenBothSignaturesUnavailable() throws Exception {
        // Both resolveWithGateways and the legacy resolve(bytes,bytes) revert.
        // resolve() must surface EnsResolutionException rather than returning null
        // or a junk address.
        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));
        when(web3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);

        EthCall reverted = new EthCall();
        reverted.setError(new org.web3j.protocol.core.Response.Error(3, "execution reverted"));
        when(web3jService.send(any(Request.class), eq(EthCall.class))).thenReturn(reverted);

        assertThrows(EnsResolutionException.class, () -> ensResolver.resolve("nope.eth"));
    }

    @Test
    void testReverseResolve() throws Exception {
        configureSyncing(false);
        configureLatestBlock(System.currentTimeMillis() / 1000); // block timestamp is in seconds

        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));

        String resolverAddress =
                "0x0000000000000000000000004c641fb9bad9b60ef180c31f56051ce826d21a9a";
        String contractName =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + TypeEncoder.encode(new Utf8String("web3j.eth"));

        EthCall resolverAddressResponse = new EthCall();
        resolverAddressResponse.setResult(resolverAddress);

        EthCall contractNameResponse = new EthCall();
        contractNameResponse.setResult(contractName);

        when(web3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);
        when(web3jService.send(any(Request.class), eq(EthCall.class)))
                .thenReturn(resolverAddressResponse);
        when(web3jService.send(any(Request.class), eq(EthCall.class)))
                .thenReturn(contractNameResponse);

        assertEquals(
                ensResolver.reverseResolve("0x19e03255f667bdfd50a32722df860b1eeaf4d635"),
                ("web3j.eth"));
    }

    @Test
    void testIsSyncedSyncing() throws Exception {
        configureSyncing(true);

        assertFalse(ensResolver.isSynced());
    }

    @Test
    void testIsSyncedFullySynced() throws Exception {
        configureSyncing(false);
        configureLatestBlock(System.currentTimeMillis() / 1000); // block timestamp is in seconds

        assertTrue(ensResolver.isSynced());
    }

    @Test
    void testIsSyncedBelowThreshold() throws Exception {
        configureSyncing(false);
        configureLatestBlock((System.currentTimeMillis() / 1000) - DEFAULT_SYNC_THRESHOLD);

        assertFalse(ensResolver.isSynced());
    }

    private void configureSyncing(boolean isSyncing) throws IOException {
        EthSyncing ethSyncing = new EthSyncing();
        EthSyncing.Result result = new EthSyncing.Result();
        result.setSyncing(isSyncing);
        ethSyncing.setResult(result);

        when(web3jService.send(any(Request.class), eq(EthSyncing.class))).thenReturn(ethSyncing);
    }

    private void configureLatestBlock(long timestamp) throws IOException {
        EthBlock.Block block = new EthBlock.Block();
        block.setTimestamp(Numeric.encodeQuantity(BigInteger.valueOf(timestamp)));
        EthBlock ethBlock = new EthBlock();
        ethBlock.setResult(block);

        when(web3jService.send(any(Request.class), eq(EthBlock.class))).thenReturn(ethBlock);
    }

    @Test
    void testIsEnsName() {
        assertTrue(isValidEnsName("eth"));
        assertTrue(isValidEnsName("web3.eth"));
        assertTrue(isValidEnsName("0x19e03255f667bdfd50a32722df860b1eeaf4d635.eth"));

        assertFalse(isValidEnsName("0x19e03255f667bdfd50a32722df860b1eeaf4d635"));
        assertFalse(isValidEnsName("19e03255f667bdfd50a32722df860b1eeaf4d635"));

        assertTrue(isValidEnsName(""));
        assertTrue(isValidEnsName("."));
    }

    @Test
    void buildRequestWhenGetSuccessTest() throws IOException {
        String url = "https://example.com/gateway/{sender}/{data}.json";
        String sender = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
        String data = "0xd5fa2b00";

        okhttp3.Request request = ensResolver.buildRequest(url, sender, data);

        assertNotNull(request);
        assertNotNull(request.url());
        assertEquals("GET", request.method());
        assertEquals(
                "https://example.com/gateway/0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8/0xd5fa2b00.json",
                request.url().url().toString());
    }

    @Test
    void buildRequestWhenPostSuccessTest() throws IOException {
        String url = "https://example.com/gateway/{sender}.json";
        sender = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
        data = "0xd5fa2b00";

        okhttp3.Request request = ensResolver.buildRequest(url, sender, data);

        assertNotNull(request);
        assertNotNull(request.url());
        assertNotNull(request.body());
        assertEquals("POST", request.method());
        assertEquals(
                "https://example.com/gateway/0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8.json",
                request.url().url().toString());
    }

    @Test
    void buildRequestWhenWithoutDataTest() {
        String url = "https://example.com/gateway/{sender}.json";
        sender = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";

        assertThrows(
                EnsResolutionException.class, () -> ensResolver.buildRequest(url, sender, null));
    }

    @Test
    void buildRequestWhenWithoutSenderTest() {
        String url = "https://example.com/gateway/{sender}.json";
        data = "0xd5fa2b00";

        assertThrows(EnsResolutionException.class, () -> ensResolver.buildRequest(url, null, data));
    }

    @Test
    void buildRequestWhenNotValidSenderTest() {
        String url = "https://example.com/gateway/{sender}.json";
        data = "0xd5fa2b00";

        assertThrows(
                EnsResolutionException.class,
                () -> ensResolver.buildRequest(url, "not valid address", data));
    }

    @Test
    void buildRequestWithDataParameterOnly() throws Exception {
        String url = "https://example.com/gateway/{data}.json";
        sender = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
        data = "0xd5fa2b00";

        okhttp3.Request request = ensResolver.buildRequest(url, sender, data);

        assertEquals("https://example.com/gateway/0xd5fa2b00.json", request.url().toString());
        assertEquals("GET", request.method());
        assertNull(request.body());
    }

    @Test
    void buildRequestWithSenderParameterOnly() throws Exception {
        String url = "https://example.com/gateway/{sender}/lookup";
        sender = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
        data = "0xd5fa2b00";

        okhttp3.Request request = ensResolver.buildRequest(url, sender, data);

        assertEquals(
                "https://example.com/gateway/0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8/lookup",
                request.url().toString());
        assertEquals("POST", request.method());
        assertNotNull(request.body());
        assertEquals("application/json", request.header("Content-Type"));
    }

    @Test
    void verifyPostRequestBodyContainsSenderAndData() throws Exception {
        String url = "https://example.com/gateway/lookup";
        sender = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
        data = "0xd5fa2b00";

        okhttp3.Request request = ensResolver.buildRequest(url, sender, data);
        assertNotNull(request.body());
        okhttp3.MediaType contentType = request.body().contentType();
        assertNotNull(contentType);
        assertTrue(contentType.toString().startsWith("application/json"));
    }

    @Test
    void buildRequestWithBothParameters() throws Exception {
        String url = "https://example.com/gateway/{sender}/{data}";
        sender = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
        data = "0xd5fa2b00";

        okhttp3.Request request = ensResolver.buildRequest(url, sender, data);

        assertEquals(
                "https://example.com/gateway/0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8/0xd5fa2b00",
                request.url().toString());
        assertEquals("GET", request.method());
        assertNull(request.body());
    }

    @Test
    void ccipReadFetchWhenSuccess() throws IOException {
        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);

        okhttp3.Response responseObj = buildResponse(200, urls.get(0), sender, data);

        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(responseObj);

        String responseStr = ensResolver.ccipReadFetch(urls, sender, data);

        assertNotNull(responseStr);
        EnsGatewayResponseDTO response = om.readValue(responseStr, EnsGatewayResponseDTO.class);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(data, response.getData());
    }

    @Test
    void ccipReadFetchWhenFirst400_Second200() throws IOException {
        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);

        okhttp3.Response responseObj_1 = buildResponse(400, urls.get(0), sender, data);
        okhttp3.Response responseObj_2 = buildResponse(200, urls.get(1), sender, data);

        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(responseObj_1, responseObj_2);

        assertThrows(
                EnsResolutionException.class, () -> ensResolver.ccipReadFetch(urls, sender, data));
    }

    @Test
    void ccipReadFetchWhenFirst200_Second400() throws IOException {
        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);

        okhttp3.Response responseObj_1 = buildResponse(200, urls.get(0), sender, data);
        okhttp3.Response responseObj_2 = buildResponse(400, urls.get(1), sender, data);

        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(responseObj_1, responseObj_2);

        String responseStr = ensResolver.ccipReadFetch(urls, sender, data);

        assertNotNull(responseStr);
        EnsGatewayResponseDTO response = om.readValue(responseStr, EnsGatewayResponseDTO.class);
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(data, response.getData());
    }

    @Test
    void ccipReadFetchWhenFirst500_Second200() throws IOException {
        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);

        okhttp3.Response responseObj_1 = buildResponse(500, urls.get(0), sender, data);
        okhttp3.Response responseObj_2 = buildResponse(200, urls.get(1), sender, data);

        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(responseObj_1, responseObj_2);

        String responseStr = ensResolver.ccipReadFetch(urls, sender, data);

        assertNotNull(responseStr);
        EnsGatewayResponseDTO response = om.readValue(responseStr, EnsGatewayResponseDTO.class);
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(data, response.getData());
    }

    @Test
    void ccipReadFetchWhenFirst500_Second500() throws IOException {
        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);

        okhttp3.Response responseObj_1 = buildResponse(500, urls.get(0), sender, data);
        okhttp3.Response responseObj_2 = buildResponse(500, urls.get(1), sender, data);

        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(responseObj_1, responseObj_2);

        String responseStr = ensResolver.ccipReadFetch(urls, sender, data);

        assertNull(responseStr);
    }

    @Test
    void resolveOffchainNotEIP() throws Exception {
        String lookupData = "some data";

        String resolveResponse = ensResolver.resolveOffchain(lookupData, null, 4);

        assertEquals(lookupData, resolveResponse);
    }

    @Test
    void resolveOffchainWhenContractAddressNotEq() {
        OffchainResolverContract resolver = mock(OffchainResolverContract.class);

        when(resolver.getContractAddress()).thenReturn("0x123456");

        assertThrows(
                EnsResolutionException.class,
                () -> ensResolver.resolveOffchain(LOOKUP_HEX, resolver, 4));
    }

    @Test
    void resolveOffchainSuccess() throws Exception {
        OffchainResolverContract resolver = mock(OffchainResolverContract.class);
        when(resolver.getContractAddress())
                .thenReturn("0xc1735677a60884abbcf72295e88d47764beda282");

        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        okhttp3.Response responseObj = buildResponse(200, urls.get(0), sender, data);

        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(responseObj);

        when(resolver.executeCallWithoutDecoding(any())).thenReturn(RESOLVED_NAME_HEX);

        String result = ensResolver.resolveOffchain(LOOKUP_HEX, resolver, 4);

        assertEquals("0x41563129cdbbd0c5d3e1c86cf9563926b243834d", result);
    }

    @Test
    void resolveOffchainWhenLookUpCallsOutOfLimit() throws Exception {
        OffchainResolverContract resolver = mock(OffchainResolverContract.class);
        String contractAddress = "0xc1735677a60884abbcf72295e88d47764beda282";
        when(resolver.getContractAddress())
                .thenReturn(contractAddress, contractAddress, contractAddress);

        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);

        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call, call, call);
        when(call.execute())
                .thenReturn(
                        buildResponse(200, urls.get(0), sender, data),
                        buildResponse(200, urls.get(0), sender, data),
                        buildResponse(200, urls.get(0), sender, data));

        String eip3668Data = EnsUtils.EIP_3668_CCIP_INTERFACE_ID + "data";
        when(resolver.executeCallWithoutDecoding(any()))
                .thenReturn(eip3668Data, eip3668Data, eip3668Data);

        assertThrows(
                EnsResolutionException.class,
                () -> ensResolver.resolveOffchain(LOOKUP_HEX, resolver, 2));
    }

    @Test
    void resolveOffchainWithDynamicCallback() throws Exception {
        OffchainResolverContract resolver = mock(OffchainResolverContract.class);
        when(resolver.getContractAddress())
                .thenReturn("0xc1735677a60884abbcf72295e88d47764beda282");

        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        okhttp3.Response responseObj = buildResponse(200, urls.get(0), sender, data);
        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(responseObj);

        // Create a custom LOOKUP_HEX with a different callback function
        String customCallbackSelector = "aabbccdd";
        String customLookupHex =
                LOOKUP_HEX.substring(0, 202) + customCallbackSelector + LOOKUP_HEX.substring(210);

        // Capture the function call to verify the correct callback selector is used
        ArgumentCaptor<String> functionCallCaptor = ArgumentCaptor.forClass(String.class);
        when(resolver.executeCallWithoutDecoding(functionCallCaptor.capture()))
                .thenReturn(RESOLVED_NAME_HEX);

        String result = ensResolver.resolveOffchain(customLookupHex, resolver, 4);
        assertEquals("0x41563129cdbbd0c5d3e1c86cf9563926b243834d", result);

        // Verify that the captured function call starts with our custom callback selector
        String capturedFunctionCall = functionCallCaptor.getValue();
        assertTrue(
                capturedFunctionCall.startsWith("0x" + customCallbackSelector),
                "Function call should start with the custom callback selector");
    }

    @Test
    void resolveOffchainParameterEncoding() throws Exception {
        OffchainResolverContract resolver = mock(OffchainResolverContract.class);
        when(resolver.getContractAddress())
                .thenReturn("0xc1735677a60884abbcf72295e88d47764beda282");

        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        String testData = "0xabcdef";

        EnsGatewayResponseDTO responseDTO = new EnsGatewayResponseDTO(testData);
        String responseJson = om.writeValueAsString(responseDTO);

        okhttp3.Response responseObj =
                new okhttp3.Response.Builder()
                        .request(new okhttp3.Request.Builder().url(urls.get(0)).build())
                        .protocol(Protocol.HTTP_2)
                        .code(200)
                        .body(ResponseBody.create(responseJson, JSON_MEDIA_TYPE))
                        .message("OK")
                        .build();

        ensResolver.setHttpClient(httpClientMock);
        when(httpClientMock.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(responseObj);

        ArgumentCaptor<String> functionCallCaptor = ArgumentCaptor.forClass(String.class);
        when(resolver.executeCallWithoutDecoding(functionCallCaptor.capture()))
                .thenReturn(RESOLVED_NAME_HEX);

        String result = ensResolver.resolveOffchain(LOOKUP_HEX, resolver, 4);
        assertEquals("0x41563129cdbbd0c5d3e1c86cf9563926b243834d", result);

        String capturedFunctionCall = functionCallCaptor.getValue();
        assertTrue(
                capturedFunctionCall.contains(testData.substring(2)),
                "Function call should contain the encoded test data");
    }

    class EnsResolverForTest extends EnsResolver {
        private OffchainResolverContract resolverMock;

        private ReverseRegistrar reverseRegistrarMock;

        public EnsResolverForTest(Web3j web3j) {
            super(web3j);
        }

        @Override
        protected OffchainResolverContract obtainOffchainResolver(String ensName) {
            return resolverMock;
        }

        @Override
        protected OffchainResolverContract obtainOffchainResolver(
                String ensName, Credentials credentials) {
            return resolverMock;
        }

        @Override
        protected ReverseRegistrar getReverseRegistrarContract(Credentials credentials) {
            return reverseRegistrarMock;
        }

        public OffchainResolverContract getResolverMock() {
            return resolverMock;
        }

        public void setResolverMock(OffchainResolverContract resolverMock) {
            this.resolverMock = resolverMock;
        }

        public void setReverseRegistrarMock(ReverseRegistrar reverseRegistrarMock) {
            this.reverseRegistrarMock = reverseRegistrarMock;
        }
    }

    // ENSIP-10 wildcard traversal and CCIP-Read trampoline now happen inside the UR
    // contract itself, so client-side branching on supportsInterface no longer exists
    // to test. The UR happy path is covered above by testResolve and live in
    // UniversalResolverIT; failure cases (zero address, both signatures unavailable,
    // legacy fallback) are covered by the testResolve* tests added above.

    @Test
    void testGetEnsTextSuccess() throws Exception {
        String expectedText = "value";
        String name = "example.eth";
        String key = "key";

        OffchainResolverContract resolverMock = mock(OffchainResolverContract.class);

        RemoteFunctionCall<String> remoteFunctionCallMock = mock(RemoteFunctionCall.class);
        when(resolverMock.text(any(), eq(key))).thenReturn(remoteFunctionCallMock);
        when(remoteFunctionCallMock.send()).thenReturn(expectedText);

        EnsResolverForTest ensResolverForTest = new EnsResolverForTest(web3j);
        ensResolverForTest.setResolverMock(resolverMock);

        String result = ensResolverForTest.getEnsText(name, key);

        assertNotNull(result);
        assertEquals(expectedText, result);
    }

    @Test
    void testSetEnsTextSuccess() throws Exception {
        String name = "example.eth";
        String key = "key";
        String value = "value";

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0x123");

        Credentials credentials = mock(Credentials.class);

        OffchainResolverContract resolverMock = mock(OffchainResolverContract.class);
        RemoteFunctionCall<TransactionReceipt> remoteFunctionCallMock =
                mock(RemoteFunctionCall.class);

        when(resolverMock.setText(any(), eq(key), eq(value))).thenReturn(remoteFunctionCallMock);
        when(remoteFunctionCallMock.send()).thenReturn(receipt);

        EnsResolverForTest ensResolverForTest = new EnsResolverForTest(web3j);
        ensResolverForTest.setResolverMock(resolverMock);

        TransactionReceipt receiptResult =
                ensResolverForTest.setEnsText(name, key, value, credentials);

        assertNotNull(receiptResult);
        assertEquals(receipt.getTransactionHash(), receiptResult.getTransactionHash());
    }

    @Test
    void testSetReverseName() throws Exception {
        String name = "example.eth";
        Credentials credentials = mock(Credentials.class);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0x123");
        ReverseRegistrar reverseRegistrarMock = mock(ReverseRegistrar.class);
        RemoteFunctionCall<TransactionReceipt> remoteFunctionCallMock =
                mock(RemoteFunctionCall.class);

        when(remoteFunctionCallMock.send()).thenReturn(receipt);
        when(reverseRegistrarMock.setName(name)).thenReturn(remoteFunctionCallMock);

        EnsResolverForTest resolver = new EnsResolverForTest(web3j);
        resolver.setReverseRegistrarMock(reverseRegistrarMock);

        TransactionReceipt receiptResult = resolver.setReverseName(name, credentials);

        assertNotNull(receiptResult);
        assertEquals(receipt.getTransactionHash(), receiptResult.getTransactionHash());
    }

    @Test
    void testSetNameForAddr() throws Exception {
        String addr = "0x41563129cdbbd0c5d3e1c86cf9563926b243834d";
        String owner = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
        String resolver = "0x226159d592E2b063810a10Ebf6dcbADA94Ed68b8";
        String name = "example.eth";
        Credentials credentials = mock(Credentials.class);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xabc123");

        ReverseRegistrar reverseRegistrarMock = mock(ReverseRegistrar.class);
        RemoteFunctionCall<TransactionReceipt> remoteFunctionCallMock =
                mock(RemoteFunctionCall.class);

        when(remoteFunctionCallMock.send()).thenReturn(receipt);
        when(reverseRegistrarMock.setNameForAddr(addr, owner, resolver, name))
                .thenReturn(remoteFunctionCallMock);

        EnsResolverForTest resolverObj = new EnsResolverForTest(web3j);
        resolverObj.setReverseRegistrarMock(reverseRegistrarMock);

        TransactionReceipt receiptResult =
                resolverObj.setReverseName(addr, owner, resolver, name, credentials);

        assertNotNull(receiptResult);
        assertEquals(receipt.getTransactionHash(), receiptResult.getTransactionHash());
    }

    @Test
    void testGetEnsMetadataSuccess() throws Exception {
        OkHttpClient httpClientMock = mock(OkHttpClient.class);
        String name = "example.eth";
        String apiUrl = "https://ens-api.example.com/";

        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));
        when(web3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);

        EnsMetadataResponse expectedResponse = new EnsMetadataResponse();
        expectedResponse.setName("example.eth");
        expectedResponse.setDescription("example.eth, an ENS name.");
        String jsonResponse = new ObjectMapper().writeValueAsString(expectedResponse);

        okhttp3.Response mockResponse = buildResponse(200, apiUrl, jsonResponse);
        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(httpClientMock.newCall(any())).thenReturn(mockCall);

        EnsMetadataResponse actualResponse = ensResolver.getEnsMetadata(name);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getName(), actualResponse.getName());
        assertEquals(expectedResponse.getDescription(), actualResponse.getDescription());
    }

    private okhttp3.Response buildResponse(int code, String url, String sender, String data)
            throws JsonProcessingException {
        EnsGatewayResponseDTO responseDTO = new EnsGatewayResponseDTO(data);

        return new okhttp3.Response.Builder()
                .request(ensResolver.buildRequest(url, sender, data))
                .protocol(Protocol.HTTP_2)
                .code(code)
                .body(ResponseBody.create(om.writeValueAsString(responseDTO), JSON_MEDIA_TYPE))
                .message("Some error message Code: " + code)
                .build();
    }

    private okhttp3.Response buildResponse(int code, String url, String jsonBody) {
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(code)
                .message("Response Message")
                .body(ResponseBody.create(jsonBody, MediaType.parse("application/json")))
                .build();
    }
}
