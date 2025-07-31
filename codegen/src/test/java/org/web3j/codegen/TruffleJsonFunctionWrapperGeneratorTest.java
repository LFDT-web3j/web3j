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
package org.web3j.codegen;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.web3j.android_test_utils.TempFileProvider;
import org.web3j.utils.Strings;

import static org.web3j.codegen.FunctionWrapperGenerator.JAVA_TYPES_ARG;
import static org.web3j.codegen.FunctionWrapperGenerator.SOLIDITY_TYPES_ARG;

public class TruffleJsonFunctionWrapperGeneratorTest extends TempFileProvider {

    private static final String PackageName = "org.web3j.unittests.truffle.java";

    private String contractBaseDir;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        URL url = SolidityFunctionWrapperGeneratorTest.class.getResource("/truffle");
        contractBaseDir = url.getPath();
    }

    @Test
    public void testLibGeneration() throws Exception {
        testCodeGenerationJvmTypes("MetaCoin", "ConvertLib");
        testCodeGenerationSolidtyTypes("MetaCoin", "ConvertLib");
    }

    @Test
    public void testContractGeneration() throws Exception {
        testCodeGenerationJvmTypes("MetaCoin", "MetaCoin");
        testCodeGenerationSolidtyTypes("MetaCoin", "MetaCoin");
    }

    @SuppressWarnings("SameParameterValue")
    private void testCodeGenerationJvmTypes(String contractName, String inputFileName)
            throws Exception {

        testCodeGeneration(contractName, inputFileName, PackageName, JAVA_TYPES_ARG, false);
        testCodeGeneration(contractName, inputFileName, PackageName, JAVA_TYPES_ARG, true);
    }

    @SuppressWarnings("SameParameterValue")
    private void testCodeGenerationSolidtyTypes(String contractName, String inputFileName)
            throws Exception {

        testCodeGeneration(contractName, inputFileName, PackageName, SOLIDITY_TYPES_ARG, false);
        testCodeGeneration(contractName, inputFileName, PackageName, SOLIDITY_TYPES_ARG, true);
    }

    private void testCodeGeneration(
            String contractName,
            String inputFileName,
            String packageName,
            String types,
            boolean generateBothCallAndSend)
            throws Exception {

        List<String> argList =
                new ArrayList<>(
                        Arrays.asList(
                                types,
                                ContractJsonParseTest.jsonFileLocation(
                                        contractBaseDir, contractName, inputFileName),
                                "-p",
                                packageName,
                                "-o",
                                tempDirPath));

        if (generateBothCallAndSend) {
            argList.add("-B");
        }
        TruffleJsonFunctionWrapperGenerator.main(argList.toArray(new String[0]));

        GeneraterTestUtils.verifyGeneratedCode(
                tempDirPath
                        + File.separator
                        + packageName.replace('.', File.separatorChar)
                        + File.separator
                        + Strings.capitaliseFirstLetter(inputFileName)
                        + ".java");
    }
}
