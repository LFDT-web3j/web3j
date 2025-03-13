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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.web3j.TempFileProvider;
import org.web3j.utils.Strings;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.web3j.codegen.FunctionWrapperGenerator.JAVA_TYPES_ARG;
import static org.web3j.codegen.FunctionWrapperGenerator.PRIMITIVE_TYPES_ARG;
import static org.web3j.codegen.FunctionWrapperGenerator.SOLIDITY_TYPES_ARG;
import static org.web3j.codegen.SolidityFunctionWrapperGenerator.COMMAND_GENERATE;
import static org.web3j.codegen.SolidityFunctionWrapperGenerator.COMMAND_SOLIDITY;
import static org.web3j.codegen.SolidityFunctionWrapperGenerator.getFileNameNoExtension;

public class SolidityFunctionWrapperGeneratorTest extends TempFileProvider {

    private String solidityBaseDir;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        URL url = SolidityFunctionWrapperGeneratorTest.class.getResource("/solidity");
        solidityBaseDir = url.getPath();
    }

    @Test
    public void testGetFileNoExtension() {
        assertEquals("", getFileNameNoExtension(""));
        assertEquals("file", getFileNameNoExtension("file"));
        assertEquals("file", getFileNameNoExtension("file."));
        assertEquals("file", getFileNameNoExtension("file.txt"));
    }

    @Test
    public void testAbiFuncsGeneration() throws Exception {
        testCodeGeneration(emptyList(), "abifuncs", "AbiFuncs", JAVA_TYPES_ARG, true, false, true);
        testCodeGeneration(
                emptyList(), "abifuncs", "AbiFuncs", SOLIDITY_TYPES_ARG, true, false, true);
    }

    @Test
    public void testAbiFuncsCompareJavaFileTest() throws Exception {
        compareJavaFile("AbiFuncs", false, true);
    }

    @Test
    public void testGreeterGeneration() throws Exception {
        testCodeGenerationJvmTypes("greeter", "Greeter");
        testCodeGenerationSolidityTypes("greeter", "Greeter");
    }

    @Test
    public void testHumanStandardTokenGeneration() throws Exception {
        testCodeGenerationJvmTypes("humanstandardtoken", "HumanStandardToken");
        testCodeGenerationSolidityTypes("humanstandardtoken", "HumanStandardToken");
    }

    @Test
    public void testSimpleStorageGeneration() throws Exception {
        testCodeGenerationJvmTypes("simplestorage", "SimpleStorage");
        testCodeGenerationSolidityTypes("simplestorage", "SimpleStorage");
    }

    @Test
    public void testFibonacciGeneration() throws Exception {
        testCodeGenerationJvmTypes("fibonacci", "Fibonacci");
        testCodeGenerationSolidityTypes("fibonacci", "Fibonacci");
    }

    @Test
    public void testArrays() throws Exception {
        testCodeGenerationJvmTypes("arrays", "Arrays");
        testCodeGenerationSolidityTypes("arrays", "Arrays");
    }

    @Test
    public void testContractWithLargeBinary() throws Exception {
        testCodeGenerationJvmTypes("large", "Large");
    }

    @Test
    public void testShipIt() throws Exception {
        testCodeGenerationJvmTypes("shipit", "ShipIt");
        testCodeGenerationSolidityTypes("shipit", "ShipIt");
    }

    @Test
    public void testStructArray() throws Exception {
        testCodeGenerationJvmTypes("structarray", "StructArray");
    }

    @Test
    public void testStructArray2() throws Exception {
        testCodeGenerationJvmTypes("structarray2", "StructArray2");
        testCodeGenerationSolidityTypes("structarray2", "StructArray2");
    }

    @Test
    public void testMisc() throws Exception {
        testCodeGenerationJvmTypes("misc", "Misc");
        testCodeGenerationSolidityTypes("misc", "Misc");
    }

    @Test
    public void testContractsNoBin() throws Exception {
        testCodeGeneration("humanstandardtoken", "HumanStandardToken", JAVA_TYPES_ARG, false);
        testCodeGeneration("humanstandardtoken", "HumanStandardToken", SOLIDITY_TYPES_ARG, false);
    }

    @Test
    public void testComplexStorage0425() throws Exception {
        testCodeGenerationJvmTypes("complexstoragenew", "ComplexStorageNew");
        testCodeGenerationSolidityTypes("complexstoragenew", "ComplexStorageNew");
    }

    @Test
    public void testComplexStorage() throws Exception {
        testCodeGenerationJvmTypes("complexstorage", "ComplexStorage");
        testCodeGenerationSolidityTypes("complexstorage", "ComplexStorage");
    }

    @Test
    public void testStructOnlyInArray() throws Exception {
        testCodeGeneration("onlyinarraystruct", "OnlyInArrayStruct", JAVA_TYPES_ARG, false);
    }

    @Test
    public void testStructOnlyInArrayCompareJavaFile() throws Exception {
        compareJavaFile("OnlyInArrayStruct", false, false);
    }

    @Test
    public void testArraysInStructCompareJavaFileTest() throws Exception {
        compareJavaFile("ArraysInStruct", false, false);
    }

    @Test
    public void testDuplicateField() throws Exception {
        PrintStream console = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        testCodeGeneration("duplicate", "DuplicateField", JAVA_TYPES_ARG, false);
        testCodeGeneration("duplicate", "DuplicateField", SOLIDITY_TYPES_ARG, false);

        System.setOut(console);
        System.out.println(out.toString());
        assertTrue(out.toString().contains("Duplicate field(s) found"));
    }

    @Test
    public void testGenerationCommandPrefixes() throws Exception {
        testCodeGeneration(
                Arrays.asList(COMMAND_SOLIDITY, COMMAND_GENERATE),
                "humanstandardtoken",
                "HumanStandardToken",
                JAVA_TYPES_ARG,
                true);
        testCodeGeneration(
                Arrays.asList(COMMAND_GENERATE),
                "humanstandardtoken",
                "HumanStandardToken",
                SOLIDITY_TYPES_ARG,
                true);
    }

    @Test
    public void testPrimitiveTypes() throws Exception {
        testCodeGenerationJvmTypes("primitive", "Primitive", true);
    }

    @Test
    public void testABIFlag() throws Exception {
        testCodeGeneration(emptyList(), "primitive", "Primitive", JAVA_TYPES_ARG, true, true, true);
    }

    @Test
    public void testEventParametersNoNamed() throws Exception {
        testCodeGeneration("eventparameters", "EventParameters", JAVA_TYPES_ARG, false);
        testCodeGeneration("eventparameters", "EventParameters", SOLIDITY_TYPES_ARG, false);
    }

    @Test
    public void testEventParametersNoNamedCompareJavaFile() throws Exception {
        compareJavaFile("EventParameters", false, false);
    }

    @Test
    public void testDeployMethodGenerated() throws Exception {
        compareJavaFile("MetaCoin", true, false);
    }

    @Test
    public void testSameInnerStructName() throws Exception {
        testCodeGeneration("sameinnerstructname", "SameInnerStructName", JAVA_TYPES_ARG, false);
        testCodeGeneration("sameinnerstructname", "SameInnerStructName", SOLIDITY_TYPES_ARG, false);
    }

    @Test
    public void testSameInnerStructNameCompareJavaFile() throws Exception {
        compareJavaFile("SameInnerStructName", true, false);
    }

    @Test
    public void testArrayOfStructClassGeneration() throws Exception {
        testCodeGeneration(
                "arrayofstructclassgeneration",
                "ArrayOfStructClassGeneration",
                JAVA_TYPES_ARG,
                false);
    }

    @Test
    public void testArrayOfStructClassGenerationCompareJavaFile() throws Exception {
        compareJavaFile("ArrayOfStructClassGeneration", true, false);
    }

    @Test
    public void testArrayOfStructAndStructGeneration() throws Exception {
        testCodeGeneration(
                "arrayofstructandstruct", "ArrayOfStructAndStruct", JAVA_TYPES_ARG, false);
    }

    @Test
    public void testArrayOfStructAndStructCompareJavaFile() throws Exception {
        compareJavaFile("ArrayOfStructAndStruct", true, false);
    }

    @Test
    public void testStaticArrayOfStructsInStructGeneration() throws Exception {
        testCodeGeneration(
                "staticarrayofstructsinstruct",
                "StaticArrayOfStructsInStruct",
                JAVA_TYPES_ARG,
                false);
    }

    @Test
    public void testStaticArrayOfStructsInStructGenerationCompareJavaFile() throws Exception {
        compareJavaFile("StaticArrayOfStructsInStruct", true, false);
    }

    @Test
    public void testCustomErrorGeneration() throws Exception {
        testCodeGeneration(
                "customerror",
                "CustomError",
                JAVA_TYPES_ARG,
                false);
        testCodeGeneration(
                "customerror",
                "CustomError",
                SOLIDITY_TYPES_ARG,
                false);
    }

    private void compareJavaFile(String inputFileName, boolean useBin, boolean abiFuncs)
            throws Exception {
        String contract = inputFileName.toLowerCase();
        String packagePath =
                generateCode(
                        emptyList(),
                        contract,
                        inputFileName,
                        JAVA_TYPES_ARG,
                        useBin,
                        false,
                        abiFuncs);
        File fileActual = new File(tempDirPath, packagePath + "/" + inputFileName + ".java");
        File fileExpected =
                new File(
                        Strings.join(
                                Arrays.asList(
                                        solidityBaseDir,
                                        contract,
                                        "build",
                                        "java",
                                        inputFileName + ".java"),
                                File.separator));
        assertEquals(
                new String(Files.readAllBytes(fileExpected.toPath())).replaceAll("(\r\n|\n)", ""),
                new String(Files.readAllBytes(fileActual.toPath())).replaceAll("(\r\n|\n)", ""));

        verifyGeneratedCode(fileActual.getAbsolutePath());
    }

    private void testCodeGenerationJvmTypes(String contractName, String inputFileName)
            throws Exception {
        testCodeGeneration(contractName, inputFileName, JAVA_TYPES_ARG, true);
    }

    private void testCodeGenerationJvmTypes(
            String contractName, String inputFileName, boolean primitive) throws Exception {
        testCodeGeneration(
                emptyList(), contractName, inputFileName, JAVA_TYPES_ARG, true, primitive, false);
    }

    private void testCodeGenerationSolidityTypes(String contractName, String inputFileName)
            throws Exception {
        testCodeGeneration(contractName, inputFileName, SOLIDITY_TYPES_ARG, true);
    }

    private void testCodeGeneration(
            String contractName, String inputFileName, String types, boolean useBin)
            throws Exception {
        testCodeGeneration(emptyList(), contractName, inputFileName, types, useBin);
    }

    private void testCodeGeneration(
            List<String> prefixes,
            String contractName,
            String inputFileName,
            String types,
            boolean useBin)
            throws Exception {
        testCodeGeneration(prefixes, contractName, inputFileName, types, useBin, false, false);
    }

    private void testCodeGeneration(
            List<String> prefixes,
            String contractName,
            String inputFileName,
            String types,
            boolean useBin,
            boolean primitives,
            boolean abiFuncs)
            throws Exception {

        String packagePath =
                generateCode(
                        prefixes, contractName, inputFileName, types, useBin, primitives, abiFuncs);
        verifyGeneratedCode(
                tempDirPath
                        + File.separator
                        + packagePath
                        + File.separator
                        + Strings.capitaliseFirstLetter(inputFileName)
                        + ".java");
    }

    private String generateCode(
            List<String> prefixes,
            String contractName,
            String inputFileName,
            String types,
            boolean useBin,
            boolean primitives,
            boolean abiFuncs) {
        String packageName = null;
        if (types.equals(JAVA_TYPES_ARG)) {
            packageName = "org.web3j.unittests.java";
        } else if (types.equals(SOLIDITY_TYPES_ARG)) {
            packageName = "org.web3j.unittests.solidity";
        }

        List<String> options = new ArrayList<>();
        options.addAll(prefixes);
        options.add(types);
        if (useBin) {
            options.add("-b");
            options.add(
                    solidityBaseDir
                            + File.separator
                            + contractName
                            + File.separator
                            + "build"
                            + File.separator
                            + inputFileName
                            + ".bin");
        }
        options.add("-a");
        options.add(
                solidityBaseDir
                        + File.separator
                        + contractName
                        + File.separator
                        + "build"
                        + File.separator
                        + inputFileName
                        + ".abi");
        options.add("-p");
        options.add(packageName);
        options.add("-o");
        options.add(tempDirPath);

        if (primitives) {
            options.add(PRIMITIVE_TYPES_ARG);
        }
        if (abiFuncs) {
            options.add("-r");
        }

        SolidityFunctionWrapperGenerator.main(options.toArray(new String[options.size()]));
        return packageName.replace('.', File.separatorChar);
    }

    private void verifyGeneratedCode(String sourceFile) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        try (StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(diagnostics, null, null)) {
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromStrings(Arrays.asList(sourceFile));
            JavaCompiler.CompilationTask task =
                    compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
            boolean result = task.call();

            System.out.println(diagnostics.getDiagnostics());
            assertTrue(result, "Generated contract contains compile time error");
        }
    }
}
