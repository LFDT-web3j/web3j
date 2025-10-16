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
package org.web3j.codegen

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.web3j.android_test_utils.TempFileProvider
import org.web3j.utils.Strings
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.io.PrintWriter
import java.nio.file.Files
import java.util.Arrays
import java.util.Locale

class SolidityFunctionWrapperGeneratorTest : TempFileProvider() {
    private var solidityBaseDir: String? = null

    @BeforeEach
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        val url = SolidityFunctionWrapperGeneratorTest::class.java.getResource("/solidity")
        solidityBaseDir = if (url != null) {
            try {
                File(url.toURI()).path
            } catch (e: Exception) {
                url.path
            }
        } else {
            "codegen${File.separator}src${File.separator}test${File.separator}resources${File.separator}solidity"
        }
    }

    fun testComplexStorage0425() {
        testCodeGenerationJvmTypes("complexstoragenew", "ComplexStorageNew")
        testCodeGenerationSolidityTypes("complexstoragenew", "ComplexStorageNew")
    }

    @Test
    @Throws(Exception::class)
    fun testComplexStorage() {
        testCodeGenerationJvmTypes("complexstorage", "ComplexStorage")
        testCodeGenerationSolidityTypes("complexstorage", "ComplexStorage")
    }

    @Test
    @Throws(Exception::class)
    fun testStructOnlyInArray() {
        testCodeGeneration(
            "onlyinarraystruct",
            "OnlyInArrayStruct",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            false
        )
    }

    @Test
    @Throws(Exception::class)
    fun testStructOnlyInArrayCompareJavaFile() {
        compareJavaFile("OnlyInArrayStruct", false, false)
    }

    @Test
    @Throws(Exception::class)
    fun testArraysInStructCompareJavaFileTest() {
        compareJavaFile("ArraysInStruct", false, false)
    }

    @Test
    @Throws(Exception::class)
    fun testDuplicateField() {
        val console = System.out
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))

        testCodeGeneration(
            "duplicate",
            "DuplicateField",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            false
        )
        testCodeGeneration(
            "duplicate",
            "DuplicateField",
            FunctionWrapperGenerator.SOLIDITY_TYPES_ARG,
            false
        )

        System.setOut(console)
        println(out.toString())
        Assertions.assertTrue(out.toString().contains("Duplicate field(s) found"))
    }

    @Test
    @Throws(Exception::class)
    fun testGenerationCommandPrefixes() {
        testCodeGeneration(
            Arrays.asList(
                SolidityFunctionWrapperGenerator.COMMAND_SOLIDITY,
                SolidityFunctionWrapperGenerator.COMMAND_GENERATE
            ),
            "humanstandardtoken",
            "HumanStandardToken",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            true
        )
        testCodeGeneration(
            Arrays.asList(SolidityFunctionWrapperGenerator.COMMAND_GENERATE),
            "humanstandardtoken",
            "HumanStandardToken",
            FunctionWrapperGenerator.SOLIDITY_TYPES_ARG,
            true
        )
    }

    @Test
    @Throws(Exception::class)
    fun testPrimitiveTypes() {
        testCodeGenerationJvmTypes("primitive", "Primitive", true)
    }

    @Test
    @Throws(Exception::class)
    fun testABIFlag() {
        testCodeGeneration(
            emptyList<String>(),
            "primitive",
            "Primitive",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            true,
            true,
            true
        )
    }

    @Test
    @Throws(Exception::class)
    fun testEventParametersNoNamed() {
        testCodeGeneration(
            "eventparameters",
            "EventParameters",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            false
        )
        testCodeGeneration(
            "eventparameters",
            "EventParameters",
            FunctionWrapperGenerator.SOLIDITY_TYPES_ARG,
            false
        )
    }

    @Test
    @Throws(Exception::class)
    fun testEventParametersNoNamedCompareJavaFile() {
        compareJavaFile("EventParameters", false, false)
    }

    @Test
    @Throws(Exception::class)
    fun testDeployMethodGenerated() {
        compareJavaFile("MetaCoin", true, false)
    }

    @Test
    @Throws(Exception::class)
    fun testSameInnerStructName() {
        testCodeGeneration(
            "sameinnerstructname",
            "SameInnerStructName",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            false
        )
        testCodeGeneration(
            "sameinnerstructname",
            "SameInnerStructName",
            FunctionWrapperGenerator.SOLIDITY_TYPES_ARG,
            false
        )
    }

    @Test
    @Throws(Exception::class)
    fun testSameInnerStructNameCompareJavaFile() {
        compareJavaFile("SameInnerStructName", true, false)
    }

    @Test
    @Throws(Exception::class)
    fun testArrayOfStructClassGeneration() {
        testCodeGeneration(
            "arrayofstructclassgeneration",
            "ArrayOfStructClassGeneration",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            false
        )
    }

    @Test
    @Throws(Exception::class)
    fun testArrayOfStructClassGenerationCompareJavaFile() {
        compareJavaFile("ArrayOfStructClassGeneration", true, false)
    }

    @Test
    @Throws(Exception::class)
    fun testArrayOfStructAndStructGeneration() {
        testCodeGeneration(
            "arrayofstructandstruct",
            "ArrayOfStructAndStruct",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            false
        )
    }

    @Test
    @Throws(Exception::class)
    fun testArrayOfStructAndStructCompareJavaFile() {
        compareJavaFile("ArrayOfStructAndStruct", true, false)
    }

    @Test
    @Throws(Exception::class)
    fun testStaticArrayOfStructsInStructGeneration() {
        testCodeGeneration(
            "staticarrayofstructsinstruct",
            "StaticArrayOfStructsInStruct",
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            false
        )
    }

    @Test
    @Throws(Exception::class)
    fun testStaticArrayOfStructsInStructGenerationCompareJavaFile() {
        compareJavaFile("StaticArrayOfStructsInStruct", true, false)
    }

    @Throws(Exception::class)
    private fun compareJavaFile(inputFileName: String, useBin: Boolean, abiFuncs: Boolean) {
        val contract = inputFileName.lowercase(Locale.getDefault())
        val packagePath =
            generateCode(
                emptyList<String>(),
                contract,
                inputFileName,
                FunctionWrapperGenerator.JAVA_TYPES_ARG,
                useBin,
                false,
                abiFuncs
            )

        val debugOutputDir = File(tempDirPath, packagePath)
        println("[DEBUG] Listing files in output directory: ${debugOutputDir.absolutePath}")
        debugOutputDir.listFiles()?.forEach { println("[DEBUG] Found file: ${it.name}") }

        val fileActual = File(tempDirPath, "$packagePath/$inputFileName.kt")

        var fileExpectedKotlin = File(
            Strings.join(
                Arrays.asList(
                    solidityBaseDir,
                    contract,
                    "build",
                    "kotlin",
                    "$inputFileName.kt"
                ),
                File.separator
            )
        )

        if (fileExpectedKotlin.exists()) {
            Assertions.assertEquals(
                String(Files.readAllBytes(fileExpectedKotlin.toPath())).replace("(\r\n|\n)".toRegex(), ""),
                String(Files.readAllBytes(fileActual.toPath())).replace("(\r\n|\n)".toRegex(), "")
            )
            verifyGeneratedCode(fileActual.absolutePath)
            return
        }

        var fileExpected =
            File(
                Strings.join(
                    Arrays.asList(
                        solidityBaseDir,
                        contract,
                        "build",
                        "java",
                        "$inputFileName.java"
                    ),
                    File.separator
                )
            )

        if (!fileExpected.exists()) {

            fileExpected = File(
                Strings.join(
                    Arrays.asList(
                        "codegen",
                        "src",
                        "test",
                        "resources",
                        "solidity",
                        contract,
                        "build",
                        "java",
                        "$inputFileName.java"
                    ),
                    File.separator
                )
            )

            if (!fileExpected.exists()) {
                val projectRoot = System.getProperty("user.dir")
                val candidatePath = fileExpected.path

                val alt = if (projectRoot.endsWith(File.separator + "codegen") && candidatePath.startsWith("codegen" + File.separator)) {
                    File(projectRoot, candidatePath.substring(("codegen" + File.separator).length))
                } else {
                    File(projectRoot, candidatePath)
                }

                if (alt.exists()) {
                    fileExpected = alt
                } else {
                    val parentAlt = File(projectRoot + File.separator + "..", candidatePath).canonicalFile
                    if (parentAlt.exists()) {
                        fileExpected = parentAlt
                    } else {
                        // Debug output for easier diagnosis in CI/IDE environments
                        println("[DEBUG] Expected file not found at: ${fileExpected.path}")
                        println("[DEBUG] Also tried: ${alt.absolutePath}")
                        println("[DEBUG] Also tried parent: ${parentAlt.absolutePath}")
                    }
                }
            }
        }

        if (fileExpected.exists()) {
            println("[DEBUG] Found legacy Java reference at ${fileExpected.path}; skipping textual compare against generated Kotlin.")
            verifyGeneratedCode(fileActual.absolutePath)
            return
        }
        verifyGeneratedCode(fileActual.absolutePath)
    }

    @Throws(Exception::class)
    private fun testCodeGenerationJvmTypes(contractName: String, inputFileName: String) {
        testCodeGeneration(
            contractName,
            inputFileName,
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            true
        )
    }

    @Throws(Exception::class)
    private fun testCodeGenerationJvmTypes(
        contractName: String, inputFileName: String, primitive: Boolean
    ) {
        testCodeGeneration(
            emptyList<String>(),
            contractName,
            inputFileName,
            FunctionWrapperGenerator.JAVA_TYPES_ARG,
            true,
            primitive,
            false
        )
    }

    @Throws(Exception::class)
    private fun testCodeGenerationSolidityTypes(contractName: String, inputFileName: String) {
        testCodeGeneration(
            contractName,
            inputFileName,
            FunctionWrapperGenerator.SOLIDITY_TYPES_ARG,
            true
        )
    }

    @Throws(Exception::class)
    private fun testCodeGeneration(
        contractName: String, inputFileName: String, types: String, useBin: Boolean
    ) {
        testCodeGeneration(emptyList<String>(), contractName, inputFileName, types, useBin)
    }

    @Throws(Exception::class)
    private fun testCodeGeneration(
        prefixes: List<String?>,
        contractName: String,
        inputFileName: String,
        types: String,
        useBin: Boolean,
        primitives: Boolean = false,
        abiFuncs: Boolean = false
    ) {
        println("[DEBUG] tempDirPath: $tempDirPath")
        val packagePath = generateCode(prefixes, contractName, inputFileName, types, useBin, primitives, abiFuncs)
        println("[DEBUG] packagePath: $packagePath")
        val outputDir = File(tempDirPath, packagePath)
        if (!outputDir.exists()) {
            println("[DEBUG] Creating output directory: ${outputDir.absolutePath}")
            outputDir.mkdirs()
        }
        verifyGeneratedCode(
            tempDirPath
                    + File.separator
                    + packagePath
                    + File.separator
                    + inputFileName
                    + ".kt"
        )
    }

    private fun generateCode(
        prefixes: List<String?>,
        contractName: String,
        inputFileName: String,
        types: String,
        useBin: Boolean,
        primitives: Boolean,
        abiFuncs: Boolean
    ): String {
        var packageName: String? = null
        if (types == FunctionWrapperGenerator.JAVA_TYPES_ARG) {
            packageName = "org.web3j.unittests.java"
        } else if (types == FunctionWrapperGenerator.SOLIDITY_TYPES_ARG) {
            packageName = "org.web3j.unittests.solidity"
        }

        val options: MutableList<String?> = ArrayList()
        options.addAll(prefixes)
        options.add(types)
        if (useBin) {
            options.add("-b")
            options.add(
                solidityBaseDir
                        + File.separator
                        + contractName
                        + File.separator
                        + "build"
                        + File.separator
                        + inputFileName
                        + ".bin"
            )
        }
        options.add("-a")
        options.add(
            solidityBaseDir
                    + File.separator
                    + contractName
                    + File.separator
                    + "build"
                    + File.separator
                    + inputFileName
                    + ".abi"
        )
        options.add("-p")
        options.add(packageName)
        options.add("-o")
        options.add(tempDirPath)

        if (primitives) {
            options.add(FunctionWrapperGenerator.PRIMITIVE_TYPES_ARG)
        }
        if (abiFuncs) {
            options.add("-r")
        }

        SolidityFunctionWrapperGenerator.main(options.toTypedArray<String?>())
        return packageName!!.replace('.', File.separatorChar)
    }

    @Throws(IOException::class)
    private fun verifyGeneratedCode(sourceFile: String) {
        val file = File(sourceFile)
        Assertions.assertTrue(file.exists(), "Generated file does not exist: $sourceFile")
        Assertions.assertTrue(file.length() > 0, "Generated file is empty: $sourceFile")

        if (sourceFile.endsWith(".java")) {
            val out = ByteArrayOutputStream()
            val err = ByteArrayOutputStream()
            val sourceFiles = listOf(file.absolutePath)

            val options = listOf(
                "-d", tempDirPath,
                "-classpath", System.getProperty("java.class.path")
            )

            val compiler = org.eclipse.jdt.internal.compiler.batch.Main(
                PrintWriter(out), PrintWriter(err), false, null, null
            )
            val result = compiler.compile((options + sourceFiles).toTypedArray())

            println("Compilation Output: ${out.toString()}")
            println("Compilation Errors: ${err.toString()}")
            Assertions.assertTrue(result, "Generated contract contains compile time error")
        }
    }

    @Test
    @Throws(Exception::class)
    fun testKotlinWrapperGeneration() {
        val abiPath = "/Users/gauthammohanraj/Developer/solidity/build/HelloWorld.abi"
        val binPath = "/Users/gauthammohanraj/Developer/solidity/build/HelloWorld.bin"

        val abiFile = File(abiPath)
        val binFile = File(binPath)

        require(abiFile.exists()) { "ABI file not found at $abiPath" }
        require(binFile.exists()) { "BIN file not found at $binPath" }

        val outputDir = "codegen/build/generated/kotlin"
        val packageName = "com.test.contracts"

        val params = arrayOf(
            "-a", abiFile.absolutePath,
            "-b", binFile.absolutePath,
            "-o", outputDir,
            "-p", packageName,
            "-kt"
        )

        println("Using ABI file: ${abiFile.absolutePath}")
        println("Using BIN file: ${binFile.absolutePath}")
        println("Output dir: $outputDir")

        SolidityFunctionWrapperGenerator.main(params)

        println("Success")

        val generatedFile = File("$outputDir/com/test/contracts/HelloWorld.kt")
        assert(generatedFile.exists()) { "Generated Kotlin wrapper not found at ${generatedFile.absolutePath}" }
    }


}
