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
package org.web3j.tx;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import org.web3j.crypto.HSMHTTPPass;
import org.web3j.dto.HSMHTTPRequestDTO;
import org.web3j.service.HSMHTTPRequestProcessor;
import org.web3j.utils.Numeric;

public class HSMHTTPRequestProcessorTestImpl<T extends HSMHTTPPass>
        extends HSMHTTPRequestProcessor<HSMHTTPPass> {
    private static final Logger log =
            LoggerFactory.getLogger(HSMHTTPRequestProcessorTestImpl.class);

    public HSMHTTPRequestProcessorTestImpl(OkHttpClient okHttpClient) {
        super(okHttpClient);
    }

    protected Request createRequest(byte[] dataToSign, HSMHTTPPass pass) {
        HSMHTTPRequestDTO requestDto =
                new HSMHTTPRequestDTO(Numeric.toHexStringNoPrefix(dataToSign));
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String json;
        try {
            json = ow.writeValueAsString(requestDto);
        } catch (JacksonException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return new Request.Builder()
                .url(pass.getUrl())
                .post(RequestBody.create(json, JSON))
                .build();
    }

    protected String readResponse(InputStream responseData) {
        return new BufferedReader(new InputStreamReader(responseData))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
