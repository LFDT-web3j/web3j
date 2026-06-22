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
package org.web3j.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StructuredDataTest {

    private static String jsonMessageString;

    @BeforeEach
    public void validSetUp() throws IOException, RuntimeException {
        String validStructuredDataJSONFilePath =
                "build/resources/test/" + "structured_data_json_files/ValidStructuredData.json";
        jsonMessageString = getResource(validStructuredDataJSONFilePath);
    }

    private String getResource(String jsonFile) throws IOException {
        return new String(
                Files.readAllBytes(Paths.get(jsonFile).toAbsolutePath()), StandardCharsets.UTF_8);
    }

    private StructuredData.EIP712Message getResourceAsMessage(String jsonFile) throws IOException {
        return new ObjectMapper()
                .readValue(
                        new String(
                                Files.readAllBytes(Paths.get(jsonFile).toAbsolutePath()),
                                StandardCharsets.UTF_8),
                        StructuredData.EIP712Message.class);
    }

    @Test
    public void testInvalidIdentifierMessageCaughtByRegex() throws RuntimeException {
        String invalidStructuredDataJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/InvalidIdentifierStructuredData.json";

        assertThrows(
                RuntimeException.class,
                () -> new StructuredDataEncoder(getResource(invalidStructuredDataJSONFilePath)));
        assertThrows(
                RuntimeException.class,
                () ->
                        new StructuredDataEncoder(
                                getResourceAsMessage(invalidStructuredDataJSONFilePath)));
    }

    @Test
    public void testInvalidTypeMessageCaughtByRegex() throws RuntimeException {
        String invalidStructuredDataJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/InvalidTypeStructuredData.json";
        assertThrows(
                RuntimeException.class,
                () -> new StructuredDataEncoder(getResource(invalidStructuredDataJSONFilePath)));
        assertThrows(
                RuntimeException.class,
                () ->
                        new StructuredDataEncoder(
                                getResourceAsMessage(invalidStructuredDataJSONFilePath)));
    }

    @Test
    public void testGetDependencies() throws IOException, RuntimeException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        Set<String> deps =
                dataEncoder.getDependencies(dataEncoder.jsonMessageObject.getPrimaryType());

        Set<String> depsExpected = new HashSet<>();
        depsExpected.add("Mail");
        depsExpected.add("Person");

        assertEquals(deps, depsExpected);
    }

    @Test
    public void testEncodeType() throws IOException, RuntimeException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        String expectedTypeEncoding =
                "Mail(Person from,Person to,string contents)"
                        + "Person(string name,address wallet)";

        assertEquals(
                dataEncoder.encodeType(dataEncoder.jsonMessageObject.getPrimaryType()),
                expectedTypeEncoding);
    }

    @Test
    public void testMinimalEncodeType() throws IOException, RuntimeException {
        String validMinimalStructuredDataJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/ValidMinimalStructuredData.json";
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(getResource(validMinimalStructuredDataJSONFilePath));
        String expectedTypeEncoding = "EIP712Domain()";

        assertEquals(
                dataEncoder.encodeType(dataEncoder.jsonMessageObject.getPrimaryType()),
                expectedTypeEncoding);
    }

    @Test
    public void testTypeHash() throws IOException, RuntimeException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        String expectedTypeHashHex =
                "0xa0cedeb2dc280ba39b857546d74f5549c" + "3a1d7bdc2dd96bf881f76108e23dac2";

        assertEquals(
                Numeric.toHexString(
                        dataEncoder.typeHash(dataEncoder.jsonMessageObject.getPrimaryType())),
                expectedTypeHashHex);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEncodeData() throws RuntimeException, IOException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        byte[] encodedData =
                dataEncoder.encodeData(
                        dataEncoder.jsonMessageObject.getPrimaryType(),
                        (HashMap<String, Object>) dataEncoder.jsonMessageObject.getMessage());
        String expectedDataEncodingHex =
                "0xa0cedeb2dc280ba39b857546d74f5549c3a1d7bd"
                        + "c2dd96bf881f76108e23dac2fc71e5fa27ff56c350aa531bc129ebdf613b772b6"
                        + "604664f5d8dbe21b85eb0c8cd54f074a4af31b4411ff6a60c9719dbd559c221c8"
                        + "ac3492d9d872b041d703d1b5aadf3154a261abdd9086fc627b61efca26ae57027"
                        + "01d05cd2305f7c52a2fc8";

        assertEquals(Numeric.toHexString(encodedData), expectedDataEncodingHex);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHashData() throws RuntimeException, IOException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        byte[] dataHash =
                dataEncoder.hashMessage(
                        dataEncoder.jsonMessageObject.getPrimaryType(),
                        (HashMap<String, Object>) dataEncoder.jsonMessageObject.getMessage());
        String expectedMessageStructHash =
                "0xc52c0ee5d84264471806290a3f2c4cecf" + "c5490626bf912d01f240d7a274b371e";

        assertEquals(Numeric.toHexString(dataHash), expectedMessageStructHash);
    }

    @Test
    public void testHashDomain() throws RuntimeException, IOException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        byte[] structHashDomain = dataEncoder.hashDomain();
        String expectedDomainStructHash =
                "0xf2cee375fa42b42143804025fc449deafd" + "50cc031ca257e0b194a650a912090f";

        assertEquals(Numeric.toHexString(structHashDomain), expectedDomainStructHash);
    }

    @Test
    public void testHashStructuredMessage() throws RuntimeException, IOException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        byte[] hashStructuredMessage = dataEncoder.hashStructuredData();
        String expectedDomainStructHash =
                "0xbe609aee343fb3c4b28e1df9e632fca64fcfaede20" + "f02e86244efddf30957bd2";

        assertEquals(Numeric.toHexString(hashStructuredMessage), expectedDomainStructHash);
    }

    @Test
    public void testBytesAndBytes32Encoding() throws IOException {
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(
                        getResource(
                                "build/resources/test/"
                                        + "structured_data_json_files/ValidStructuredDataWithBytesTypes.json"));
        dataEncoder.hashStructuredData();
    }

    @Test
    public void test0xProtocolControlSample() throws IOException {
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(
                        getResource(
                                "build/resources/test/"
                                        + "structured_data_json_files/0xProtocolControlSample.json"));
        assertEquals(
                "0xccb29124860915763e8cd9257da1260abc7df668fde282272587d84b594f37f6",
                Numeric.toHexString(dataEncoder.hashStructuredData()));
    }

    @Test
    public void test0xProtocolControlSampleAsMessage() throws IOException {
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(
                        getResourceAsMessage(
                                "build/resources/test/"
                                        + "structured_data_json_files/0xProtocolControlSample.json"));
        assertEquals(
                "0xccb29124860915763e8cd9257da1260abc7df668fde282272587d84b594f37f6",
                Numeric.toHexString(dataEncoder.hashStructuredData()));
    }

    @Test
    public void testInvalidMessageValueTypeMismatch() throws RuntimeException, IOException {
        String invalidStructuredDataJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/InvalidMessageValueTypeMismatch.json";
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(getResource(invalidStructuredDataJSONFilePath));
        assertThrows(ClassCastException.class, dataEncoder::hashStructuredData);

        StructuredDataEncoder dataEncoderFromMessage =
                new StructuredDataEncoder(getResourceAsMessage(invalidStructuredDataJSONFilePath));
        assertThrows(ClassCastException.class, dataEncoderFromMessage::hashStructuredData);
    }

    @Test
    public void testInvalidMessageInvalidABIType() throws RuntimeException, IOException {
        String invalidStructuredDataJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/InvalidMessageInvalidABIType.json";
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(getResource(invalidStructuredDataJSONFilePath));
        assertThrows(UnsupportedOperationException.class, dataEncoder::hashStructuredData);
    }

    @Test
    public void testInvalidMessageValidABITypeInvalidValue() throws RuntimeException, IOException {
        String invalidStructuredDataJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/InvalidMessageValidABITypeInvalidValue.json";
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(getResource(invalidStructuredDataJSONFilePath));
        assertThrows(RuntimeException.class, dataEncoder::hashStructuredData);
    }

    // EIP712 v3
    @Test
    public void testValidStructureWithValues() throws IOException {
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(
                        getResource(
                                "build/resources/test/"
                                        + "structured_data_json_files/ValidStructuredDataWithValues.json"));

        assertEquals(
                "0x9a321bee2df12b3b43bc4caf71d19839f05d82264b780b48f1f529bf916b5d30",
                Numeric.toHexString(dataEncoder.hashStructuredData()));
    }

    @Test
    public void testValidStructureWithValuesAsMessage() throws IOException {
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(
                        getResourceAsMessage(
                                "build/resources/test/"
                                        + "structured_data_json_files/ValidStructuredDataWithValues.json"));

        assertEquals(
                "0x9a321bee2df12b3b43bc4caf71d19839f05d82264b780b48f1f529bf916b5d30",
                Numeric.toHexString(dataEncoder.hashStructuredData()));
    }

    // EIP712 v4
    @Test
    public void testEncodeTypeWithArrays() throws IOException {
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(
                        getResource(
                                "build/resources/test/"
                                        + "structured_data_json_files/ValidStructuredDataWithArrays.json")); // taken from https://danfinlay.github.io/js-eth-personal-sign-examples/ and updated to int,uint arrays
        String expectedMailType =
                "Mail(Person from,Group[] to,string contents)"
                        + "Group(string name,Person[] members)"
                        + "Person(string name,address[] wallets)";

        assertEquals(dataEncoder.encodeType("Mail"), expectedMailType);

        String expectedGroupType =
                "Group(string name,Person[] members)" + "Person(string name,address[] wallets)";

        assertEquals(dataEncoder.encodeType("Group"), expectedGroupType);
    }

    // EIP712 v4
    @Test
    public void testValidStructureWithArrays() throws IOException {
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(
                        getResource(
                                "build/resources/test/"
                                        + "structured_data_json_files/ValidStructuredArrayData.json")); // taken from https://danfinlay.github.io/js-eth-personal-sign-examples/ and updated to int,uint arrays

        byte[] dataHash =
                dataEncoder.hashMessage(
                        dataEncoder.jsonMessageObject.getPrimaryType(),
                        (HashMap<String, Object>) dataEncoder.jsonMessageObject.getMessage());

        String expectedMessageStructHash =
                "0x14f510c172a64b76f1105338b2e0b09dccfa4aef922131716d58d7fd94e097e8";

        assertEquals(expectedMessageStructHash, Numeric.toHexString(dataHash));

        assertEquals(
                "0xa31e6ec120743db6e47cf44cd6b116cc4383ff4875cf823308938f8ba4566cc0",
                Numeric.toHexString(dataEncoder.hashStructuredData()));
    }

    // EIP712 v3 Gnosis format; Multisig Structured message with addresses blanked
    @Test
    public void testValidGnosisStructure() throws IOException {
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(
                        getResource(
                                "build/resources/test/"
                                        + "structured_data_json_files/ValidStructuredGnosisData.json")); // typical Gnosis safe EIP712 data

        assertEquals(
                "0xb3ffe0073b0c3aecb00b19a636e4b3820cfc9692189f874312c906f70edf10bd",
                Numeric.toHexString(dataEncoder.hashStructuredData()));
    }

    @Test
    public void testGetArrayDimensionsFromData() throws RuntimeException, IOException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        // [[1, 2, 3], [4, 5, 6]]
        List<Object> testArrayData1 = new ArrayList<>();
        testArrayData1.add(new ArrayList<>(Arrays.asList("1", "2", "3")));
        testArrayData1.add(new ArrayList<>(Arrays.asList("4", "5", "6")));
        List<Integer> expectedDimensions1 =
                new ArrayList<Integer>() {
                    {
                        add(2);
                        add(3);
                    }
                };
        assertEquals(dataEncoder.getArrayDimensionsFromData(testArrayData1), expectedDimensions1);

        // [[1, 2, 3]]
        List<Object> testArrayData2 = new ArrayList<>();
        testArrayData2.add(new ArrayList<>(Arrays.asList("1", "2", "3")));
        List<Integer> expectedDimensions2 =
                new ArrayList<Integer>() {
                    {
                        add(1);
                        add(3);
                    }
                };
        assertEquals(dataEncoder.getArrayDimensionsFromData(testArrayData2), expectedDimensions2);

        // [1, 2, 3]
        List<Object> testArrayData3 =
                new ArrayList<Object>() {
                    {
                        add("1");
                        add("2");
                        add("3");
                    }
                };
        List<Integer> expectedDimensions3 =
                new ArrayList<Integer>() {
                    {
                        add(3);
                    }
                };
        assertEquals(dataEncoder.getArrayDimensionsFromData(testArrayData3), expectedDimensions3);

        // [[[1, 2], [3, 4], [5, 6]], [[7, 8], [9, 10], [11, 12]]]
        List<Object> testArrayData4 = new ArrayList<>();
        testArrayData4.add(
                new ArrayList<Object>() {
                    {
                        add(new ArrayList<>(Arrays.asList("1", "2")));
                        add(new ArrayList<>(Arrays.asList("3", "4")));
                        add(new ArrayList<>(Arrays.asList("5", "6")));
                    }
                });
        testArrayData4.add(
                new ArrayList<Object>() {
                    {
                        add(new ArrayList<>(Arrays.asList("7", "8")));
                        add(new ArrayList<>(Arrays.asList("9", "10")));
                        add(new ArrayList<>(Arrays.asList("11", "12")));
                    }
                });
        List<Integer> expectedDimensions4 =
                new ArrayList<Integer>() {
                    {
                        add(2);
                        add(3);
                        add(2);
                    }
                };
        assertEquals(dataEncoder.getArrayDimensionsFromData(testArrayData4), expectedDimensions4);
    }

    @Test
    public void testFlattenMultidimensionalArray() throws IOException, RuntimeException {
        StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonMessageString);
        // [[1, 2, 3], [4, 5, 6]]
        List<Object> testArrayData1 = new ArrayList<>();
        testArrayData1.add(new ArrayList<>(Arrays.asList(1, 2, 3)));
        testArrayData1.add(new ArrayList<>(Arrays.asList(4, 5, 6)));
        List<Integer> expectedFlatArray1 =
                new ArrayList<Integer>() {
                    {
                        add(1);
                        add(2);
                        add(3);
                        add(4);
                        add(5);
                        add(6);
                    }
                };
        assertEquals(dataEncoder.flattenMultidimensionalArray(testArrayData1), expectedFlatArray1);

        // [[1, 2, 3]]
        List<Object> testArrayData2 = new ArrayList<>();
        testArrayData2.add(new ArrayList<>(Arrays.asList(1, 2, 3)));
        List<Integer> expectedFlatArray2 =
                new ArrayList<Integer>() {
                    {
                        add(1);
                        add(2);
                        add(3);
                    }
                };
        assertEquals(dataEncoder.flattenMultidimensionalArray(testArrayData2), expectedFlatArray2);

        // [1, 2, 3]
        List<Object> testArrayData3 =
                new ArrayList<Object>() {
                    {
                        add(1);
                        add(2);
                        add(3);
                    }
                };
        List<Integer> expectedFlatArray3 =
                new ArrayList<Integer>() {
                    {
                        add(1);
                        add(2);
                        add(3);
                    }
                };
        assertEquals(dataEncoder.flattenMultidimensionalArray(testArrayData3), expectedFlatArray3);

        // [[[1, 2], [3, 4], [5, 6]], [[7, 8], [9, 10], [11, 12]]]
        List<Object> testArrayData4 = new ArrayList<>();
        testArrayData4.add(
                new ArrayList<Object>() {
                    {
                        add(new ArrayList<>(Arrays.asList(1, 2)));
                        add(new ArrayList<>(Arrays.asList(3, 4)));
                        add(new ArrayList<>(Arrays.asList(5, 6)));
                    }
                });
        testArrayData4.add(
                new ArrayList<Object>() {
                    {
                        add(new ArrayList<>(Arrays.asList(7, 8)));
                        add(new ArrayList<>(Arrays.asList(9, 10)));
                        add(new ArrayList<>(Arrays.asList(11, 12)));
                    }
                });
        List<Integer> expectedFlatArray4 =
                new ArrayList<Integer>() {
                    {
                        add(1);
                        add(2);
                        add(3);
                        add(4);
                        add(5);
                        add(6);
                        add(7);
                        add(8);
                        add(9);
                        add(10);
                        add(11);
                        add(12);
                    }
                };
        assertEquals(dataEncoder.flattenMultidimensionalArray(testArrayData4), expectedFlatArray4);
    }

    @Test
    public void testUnequalArrayLengthsBetweenSchemaAndData() throws RuntimeException, IOException {
        String invalidStructuredDataJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/"
                        + "InvalidMessageUnequalArrayLengthsBetweenSchemaAndData.json";
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(getResource(invalidStructuredDataJSONFilePath));
        assertThrows(RuntimeException.class, dataEncoder::hashStructuredData);
    }

    @Test
    public void testDataNotPerfectArrayButDeclaredArrayInSchema()
            throws RuntimeException, IOException {
        String invalidStructuredDataJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/"
                        + "InvalidMessageDataNotPerfectArrayButDeclaredArrayInSchema.json";
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(getResource(invalidStructuredDataJSONFilePath));
        assertThrows(RuntimeException.class, dataEncoder::hashStructuredData);
    }

    @Test
    public void testHashDomainWithSalt() throws RuntimeException, IOException {
        String validStructuredDataWithSaltJSONFilePath =
                "build/resources/test/"
                        + "structured_data_json_files/"
                        + "ValidStructuredDataWithSalt.json";
        StructuredDataEncoder dataEncoder =
                new StructuredDataEncoder(getResource(validStructuredDataWithSaltJSONFilePath));
        byte[] structHashDomain = dataEncoder.hashDomain();
        String expectedDomainStructHash =
                "0xbae4f6f7b9bfdfda060692099b0e1ccecd25d62b7c92cc9f3b907f33178b81e3";

        assertEquals(Numeric.toHexString(structHashDomain), expectedDomainStructHash);
    }
}
