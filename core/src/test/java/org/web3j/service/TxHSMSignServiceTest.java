/*
 * Copyright 2026 Web3 Labs Ltd.
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
package org.web3j.service;

import java.io.IOException;
import java.math.BigInteger;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import org.web3j.crypto.HSMHTTPPass;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.SampleKeys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.tx.ChainIdLong;
import org.web3j.tx.HSMHTTPRequestProcessorTestImpl;
import org.web3j.tx.RawTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxHSMSignServiceTest {

    private HSMHTTPPass hsmHttpPass;
    private TxHSMSignService<HSMHTTPPass> signService;

    @BeforeEach
    void setUp() throws IOException {
        OkHttpClient okHttpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        when(call.execute()).thenThrow(new IOException("Connection refused"));
        when(okHttpClient.newCall(any())).thenReturn(call);

        hsmHttpPass =
                new HSMHTTPPass(
                        SampleKeys.CREDENTIALS.getAddress(),
                        SampleKeys.CREDENTIALS.getEcKeyPair().getPublicKey(),
                        "http://mock_request_url.com");
        signService =
                new TxHSMSignService<>(
                        new HSMHTTPRequestProcessorTestImpl<>(okHttpClient), hsmHttpPass);
    }

    @Test
    void testLegacySignFailsWhenHsmUnreachable() {
        assertHsmConnectionFailure(() -> signService.sign(createLegacyTx(), ChainIdLong.MAINNET));
    }

    @Test
    void testLegacySignWithoutChainIdFailsWhenHsmUnreachable() {
        assertHsmConnectionFailure(() -> signService.sign(createLegacyTx(), ChainIdLong.NONE));
    }

    @Test
    void test1559SignFailsWhenHsmUnreachable() {
        assertHsmConnectionFailure(() -> signService.sign(create1559Tx(), ChainIdLong.MAINNET));
    }

    @Test
    void testSignFailsWhenHsmReturnsEmptyResponse() throws IOException {
        OkHttpClient okHttpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response emptyResponse =
                new Response.Builder()
                        .code(200)
                        .request(new Request.Builder().url("http://test-call.com").build())
                        .protocol(Protocol.HTTP_1_1)
                        .message("response message")
                        .body(ResponseBody.create("", MediaType.parse("text/plain")))
                        .build();
        when(call.execute()).thenReturn(emptyResponse);
        when(okHttpClient.newCall(any())).thenReturn(call);
        TxHSMSignService<HSMHTTPPass> emptyResponseSignService =
                new TxHSMSignService<>(
                        new HSMHTTPRequestProcessorTestImpl<>(okHttpClient), hsmHttpPass);

        ClientConnectionException e =
                assertThrows(
                        ClientConnectionException.class,
                        () -> emptyResponseSignService.sign(create1559Tx(), ChainIdLong.MAINNET));
        assertEquals("Empty response received from HSM", e.getMessage());
    }

    @Test
    void testSignFailsWhenHsmReturnsIncompleteSignature() {
        Sign.SignatureData emptySignature =
                new Sign.SignatureData(new byte[0], new byte[0], new byte[0]);
        TxHSMSignService<HSMHTTPPass> nullSignatureService =
                new TxHSMSignService<>((dataToSign, pass) -> null, hsmHttpPass);
        TxHSMSignService<HSMHTTPPass> emptySignatureService =
                new TxHSMSignService<>((dataToSign, pass) -> emptySignature, hsmHttpPass);

        assertThrows(
                IllegalStateException.class,
                () -> nullSignatureService.sign(createLegacyTx(), ChainIdLong.NONE));
        assertThrows(
                IllegalStateException.class,
                () -> emptySignatureService.sign(createLegacyTx(), ChainIdLong.NONE));
    }

    @Test
    void testTransactionIsNotSentWhenHsmUnreachable() {
        Web3j web3j = mock(Web3j.class);
        RawTransactionManager transactionManager =
                new RawTransactionManager(web3j, signService, ChainIdLong.MAINNET);

        assertThrows(
                ClientConnectionException.class,
                () -> transactionManager.signAndSend(create1559Tx()));
        verify(web3j, never()).ethSendRawTransaction(anyString());
    }

    private static void assertHsmConnectionFailure(Executable sign) {
        ClientConnectionException e = assertThrows(ClientConnectionException.class, sign);
        assertEquals("Failed to call HSM", e.getMessage());
        assertInstanceOf(IOException.class, e.getCause());
    }

    private static RawTransaction createLegacyTx() {
        return RawTransaction.createEtherTransaction(
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.TEN,
                "0x0add5355",
                BigInteger.valueOf(Long.MAX_VALUE));
    }

    private static RawTransaction create1559Tx() {
        return RawTransaction.createEtherTransaction(
                ChainIdLong.MAINNET,
                BigInteger.ZERO,
                BigInteger.TEN,
                "0x0add5355",
                BigInteger.valueOf(Long.MAX_VALUE),
                BigInteger.ONE,
                BigInteger.TWO);
    }
}
