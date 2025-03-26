package org.web3j.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import org.web3j.utils.Numeric;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StructuredDataEncoderTest {

    @Test
    public void testCustomEncodeData() throws Exception {

        // json

        String jsonMessage = """
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


        byte[] dataHash = encoder.encodeData(encoder.jsonMessageObject.getPrimaryType(),
                (HashMap<String, Object>) encoder.jsonMessageObject.getMessage());
        System.out.println("################ hash is " +  Numeric.toHexString(dataHash));
        baos.write(dataHash, 0, dataHash.length);
        

    }
} 