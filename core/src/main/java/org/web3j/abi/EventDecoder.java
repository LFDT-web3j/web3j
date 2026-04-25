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
package org.web3j.abi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.Log;

public class EventDecoder {

    /**
     * Decodes an event log into a structured map using ABI definitions.
     *
     * @param abiJson the ABI json string (can be a JSON array of definitions or a single definition
     *     object)
     * @param log the transaction log to decode
     * @return a map containing the decoded indexed and non-indexed parameter names and values
     */
    public static Map<String, Object> decodeEvent(String abiJson, Log log) {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        List<AbiDefinition> abiDefinitions = new ArrayList<>();

        try {
            JsonNode node = objectMapper.readTree(abiJson);
            if (node.isArray()) {
                abiDefinitions =
                        objectMapper.readValue(
                                abiJson, new TypeReference<List<AbiDefinition>>() {});
            } else {
                abiDefinitions.add(objectMapper.readValue(abiJson, AbiDefinition.class));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid ABI JSON", e);
        }

        // Find the matching event definition
        List<String> topics = log.getTopics();
        if (topics == null || topics.isEmpty()) {
            throw new IllegalArgumentException("Log does not contain any topics");
        }
        String eventSignature = topics.get(0);

        AbiDefinition targetEventDefinition = null;
        Event targetEvent = null;

        for (AbiDefinition abiDef : abiDefinitions) {
            if ("event".equals(abiDef.getType())) {
                List<org.web3j.abi.TypeReference<?>> parameters = new ArrayList<>();
                try {
                    for (AbiDefinition.NamedType namedType : abiDef.getInputs()) {
                        parameters.add(
                                org.web3j.abi.TypeReference.makeTypeReference(
                                        namedType.getType(), namedType.isIndexed(), false));
                    }
                } catch (Exception e) {
                    // Skip if the ABI definition cannot be parsed into an Event object
                    continue;
                }

                Event event = new Event(abiDef.getName(), parameters);
                String encodedSignature = EventEncoder.encode(event);

                if (encodedSignature.equals(eventSignature)) {
                    targetEventDefinition = abiDef;
                    targetEvent = event;
                    break;
                }
            }
        }

        if (targetEvent == null || targetEventDefinition == null) {
            throw new IllegalArgumentException(
                    "No matching event definition found in ABI for log signature: "
                            + eventSignature);
        }

        // Re-implement Contract's decoding since staticExtractEventParametersWithLog is protected
        List<Type> indexedValues = new ArrayList<>();
        List<org.web3j.abi.TypeReference<Type>> indexedParameters =
                targetEvent.getIndexedParameters();
        for (int i = 0; i < indexedParameters.size(); i++) {
            if (i + 1 < topics.size()) {
                Type value =
                        FunctionReturnDecoder.decodeIndexedValue(
                                topics.get(i + 1), indexedParameters.get(i));
                indexedValues.add(value);
            }
        }

        List<Type> nonIndexedValues =
                FunctionReturnDecoder.decode(log.getData(), targetEvent.getNonIndexedParameters());

        Map<String, Object> decodedResult = new HashMap<>();
        List<AbiDefinition.NamedType> inputs = targetEventDefinition.getInputs();

        int indexedIdx = 0;
        int nonIndexedIdx = 0;

        for (int i = 0; i < inputs.size(); i++) {
            AbiDefinition.NamedType input = inputs.get(i);
            Object value = null;
            if (input.isIndexed()) {
                if (indexedIdx < indexedValues.size()) {
                    Type<?> typeValue = indexedValues.get(indexedIdx++);
                    if (typeValue != null) {
                        value = typeValue.getValue();
                    }
                }
            } else {
                if (nonIndexedIdx < nonIndexedValues.size()) {
                    Type<?> typeValue = nonIndexedValues.get(nonIndexedIdx++);
                    if (typeValue != null) {
                        value = typeValue.getValue();
                    }
                }
            }

            String name = input.getName();
            // Provide a fallback name if name is empty
            if (name == null || name.trim().isEmpty()) {
                name = "param" + i;
            }

            decodedResult.put(name, value);
        }

        return decodedResult;
    }
}
