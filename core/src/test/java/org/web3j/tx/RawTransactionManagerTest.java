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
import java.math.BigDecimal;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

import org.web3j.crypto.HSMHTTPPass;
import org.web3j.crypto.SampleKeys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.service.HSMHTTPRequestProcessor;
import org.web3j.service.TxHSMSignService;
import org.web3j.service.TxSignService;
import org.web3j.tx.exceptions.TxHashMismatchException;
import org.web3j.utils.Convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RawTransactionManagerTest extends ManagedTransactionTester {

    // Get From org.web3j.crypto.TransactionDecoderTest.testDecodingSigned
    private static final String TX_SIGN_FORMAT_DER_HEX =
            "3044022046360b50498ddf5566551ce1ce69c46c565f1f478bb0ee680caf31fbc08ab72702201b2f1432de16d110407d544f519fc91b84c8e16d3b6ec899592d486a94974cd0";
    private static final String TX_SIGN_RESULT_HEX =
            "0xf85580010a840add5355887fffffffffffffff801ca046360b50498ddf5566551ce1ce69c46c565f1f478bb0ee680caf31fbc08ab727a01b2f1432de16d110407d544f519fc91b84c8e16d3b6ec899592d486a94974cd0";

    @Test
    void testTxHashMismatch() throws IOException {
        TransactionReceipt transactionReceipt = prepareTransfer();
        prepareTransaction(transactionReceipt);

        TransactionManager transactionManager =
                new RawTransactionManager(web3j, SampleKeys.CREDENTIALS);
        Transfer transfer = new Transfer(web3j, transactionManager);
        assertThrows(
                TxHashMismatchException.class,
                () -> transfer.sendFunds(ADDRESS, BigDecimal.ONE, Convert.Unit.ETHER).send());
    }

    @Test
    void testSignRawTxWithHSM() throws IOException {
        TransactionReceipt transactionReceipt = prepareTransfer();
        prepareTransaction(transactionReceipt);

        OkHttpClient okHttpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response hmsResponse =
                new Response.Builder()
                        .code(200)
                        .request(new Request.Builder().url("http://test-call.com").build())
                        .protocol(Protocol.HTTP_1_1)
                        .message("response message")
                        .body(
                                ResponseBody.create(
                                        TX_SIGN_FORMAT_DER_HEX, MediaType.parse("text/plain")))
                        .build();

        when(call.execute()).thenReturn(hmsResponse);
        when(okHttpClient.newCall(any())).thenReturn(call);

        HSMHTTPRequestProcessor<HSMHTTPPass> hsmRequestProcessor =
                new HSMHTTPRequestProcessorTestImpl<>(okHttpClient);
        HSMHTTPPass hsmhttpPass =
                new HSMHTTPPass(
                        SampleKeys.CREDENTIALS.getAddress(),
                        SampleKeys.CREDENTIALS.getEcKeyPair().getPublicKey(),
                        "http://mock_request_url.com");

        TxSignService txHSMSignService = new TxHSMSignService<>(hsmRequestProcessor, hsmhttpPass);
        RawTransactionManager transactionManager =
                new RawTransactionManager(web3j, txHSMSignService, ChainIdLong.NONE);

        String sign = transactionManager.sign(createRawTx());

        assertEquals(TX_SIGN_RESULT_HEX, sign);
    }
}
