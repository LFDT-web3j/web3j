/*
 * Copyright 2022 Web3 Labs Ltd.
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
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import org.web3j.crypto.CryptoUtils;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.HSMHTTPPass;
import org.web3j.crypto.Sign;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.utils.Numeric;

/**
 * Request processor to a HSM through the HTTP
 *
 * @param <T> Object with required parameters to perform request to a HSM
 */
public abstract class HSMHTTPRequestProcessor<T extends HSMHTTPPass>
        implements HSMRequestProcessor<HSMHTTPPass> {

    public static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;

    public HSMHTTPRequestProcessor(OkHttpClient okHttpClient) {
        this.client = okHttpClient;
    }

    @Override
    public Sign.SignatureData callHSM(byte[] dataToSign, HSMHTTPPass pass) {
        Request request = createRequest(dataToSign, pass);

        try (okhttp3.Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (response.isSuccessful()) {
                String signHex =
                        responseBody == null ? null : readResponse(responseBody.byteStream());
                if (signHex == null || signHex.isBlank()) {
                    throw new ClientConnectionException("Empty response received from HSM");
                }
                byte[] signBytes = Numeric.hexStringToByteArray(signHex);
                ECDSASignature signature = CryptoUtils.fromDerFormat(signBytes);

                return Sign.createSignatureData(signature, pass.getPublicKey(), dataToSign);
            } else {
                int code = response.code();
                String text = responseBody == null ? "N/A" : responseBody.string();
                throw new ClientConnectionException(
                        code, "Invalid response received: " + code + "; " + text);
            }
        } catch (IOException e) {
            ClientConnectionException exception =
                    new ClientConnectionException("Failed to call HSM");
            exception.initCause(e);
            throw exception;
        }
    }

    protected abstract Request createRequest(byte[] dataToSign, HSMHTTPPass pass);

    protected abstract String readResponse(InputStream responseData);
}
