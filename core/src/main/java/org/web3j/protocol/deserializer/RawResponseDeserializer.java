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
package org.web3j.protocol.deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import org.web3j.protocol.core.Response;

/** A jackson deserializer that sets the rawResponse variable of Response objects. */
@SuppressWarnings("rawtypes")
public class RawResponseDeserializer extends StdDeserializer<Response> {

    private final ValueDeserializer<?> defaultDeserializer;

    public RawResponseDeserializer(ValueDeserializer<?> defaultDeserializer) {
        super(Response.class);
        this.defaultDeserializer = defaultDeserializer;
    }

    @Override
    public Response deserialize(JsonParser jp, DeserializationContext ctxt) {
        Response deserializedResponse = (Response) defaultDeserializer.deserialize(jp, ctxt);
        deserializedResponse.setRawResponse(getRawResponse(jp));
        return deserializedResponse;
    }

    private String getRawResponse(JsonParser jp) {
        try {
            final InputStream inputSource =
                    jp.streamReadConstraints() != null ? (InputStream) jp.currentValue() : null;

            if (inputSource == null) {
                return "";
            }

            inputSource.reset();
            return streamToString(inputSource);
        } catch (IOException e) {
            return "";
        }
    }

    private String streamToString(InputStream input) throws IOException {
        try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8).useDelimiter("\\Z")) {
            return scanner.next();
        }
    }
}
