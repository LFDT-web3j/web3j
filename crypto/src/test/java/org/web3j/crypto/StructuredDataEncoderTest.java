/*
 * Copyright 2025 Web3 Labs Ltd.
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
package org.web3j.crypto;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.*;

public class StructuredDataEncoderTest {

    @Test
    public void testCustomEncodeData() throws Exception {

        // json

        String jsonMessage =
                """
                {
                  "domain" : {
                    "chainId" : "84532",
                    "name" : "Test",
                    "verifyingContract" : "0xe483dea6aa7d3831173379d81e5c08874f1042e7",
                    "version" : "1.0.0"
                  },
                  "message" : {
                    "amounts" : [ 0, 0 ],
                    "to" : "0xe483dea6aa7d3831173379d81e5c08874f1042e7",
                    "tokenIds" : [ 0, 1 ],
                    "validityEndTimestamp" : 1743005854,
                    "validityStartTimestamp" : 1742919454,
                    "salt" : "82"
                  },
                  "primaryType" : "ClaimRequest",
                  "types" : {
                    "ClaimRequest" : [ {
                      "name" : "to",
                      "type" : "address"
                    }, {
                      "name" : "tokenIds",
                      "type" : "uint256[]"
                    }, {
                      "name" : "amounts",
                      "type" : "uint256[]"
                    }, {
                      "name" : "validityStartTimestamp",
                      "type" : "uint128"
                    }, {
                      "name" : "validityEndTimestamp",
                      "type" : "uint128"
                    }, {
                      "name" : "salt",
                      "type" : "uint256"
                    } ],
                    "EIP712Domain" : [ {
                      "name" : "name",
                      "type" : "string"
                    }, {
                      "name" : "version",
                      "type" : "string"
                    }, {
                      "name" : "chainId",
                      "type" : "uint256"
                    }, {
                      "name" : "verifyingContract",
                      "type" : "address"
                    } ]
                  }
                }
                """;

        System.out.println("JSON Message: " + jsonMessage);

        // struct instance
        StructuredDataEncoder encoder = new StructuredDataEncoder(jsonMessage);
        encoder.hashStructuredData();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] dataHash =
                encoder.encodeData(
                        encoder.jsonMessageObject.getPrimaryType(),
                        (HashMap<String, Object>) encoder.jsonMessageObject.getMessage());
        System.out.println("################ hash is " + Numeric.toHexString(dataHash));
        baos.write(dataHash, 0, dataHash.length);
    }
}
