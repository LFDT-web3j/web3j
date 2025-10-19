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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.tx.Contract;
import org.web3j.utils.Files;
import org.web3j.utils.Strings;

import static org.web3j.codegen.Console.exitError;
import static org.web3j.utils.Collection.tail;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

/** Java wrapper source code generator for Solidity ABI format. */
public class SolidityFunctionWrapperGenerator extends FunctionWrapperGenerator {
    public static final String COMMAND_SOLIDITY = "solidity";
    public static final String COMMAND_GENERATE = "generate";
    public static final String COMMAND_PREFIX = COMMAND_SOLIDITY + " " + COMMAND_GENERATE;

    /*
     * Usage: solidity generate [-hV] [-jt] [-st] [-B] [-r] -a=<abiFile> [-b=<binFile>]
     * -o=<destinationFileDir> -p=<packageName>
     * -h, --help                 Show this help message and exit.
     * -V, --version              Print version information and exit.
     * -a, --abiFile=<abiFile>    abi file with contract definition.
     * -b, --binFile=<binFile>    bin file with contract compiled code in order to
     * generate deploy methods.
     * -o, --outputDir=<destinationFileDir>
     * destination base directory.
     * -p, --package=<packageName>
     * base package name.
     * -jt, --javaTypes       use native java types.
     * Default: true
     * -st, --solidityTypes   use solidity types.
     * -B, --generateBoth     generate both call and send functions.
     * -r, --abiFuncs             ABI encoded function call getters.
     */

    private final File binFile;
    private final File abiFile;

    private final String contractName;

    private final int addressLength;

    private final boolean generateBothCallAndSend;

    public SolidityFunctionWrapperGenerator(
            File binFile,
            File abiFile,
            File destinationDir,
            String contractName,
            String basePackageName,
            boolean useJavaNativeTypes,
            boolean useJavaPrimitiveTypes,
            int addressLength) {

        this(
                binFile,
                abiFile,
                destinationDir,
                contractName,
                basePackageName,
                useJavaNativeTypes,
                useJavaPrimitiveTypes,
                false,
                Contract.class,
                addressLength,
                false);
    }

    public SolidityFunctionWrapperGenerator(
            File binFile,
            File abiFile,
            File destinationDir,
            String contractName,
            String basePackageName,
            boolean useJavaNativeTypes,
            boolean useJavaPrimitiveTypes,
            int addressLength,
            boolean abiFuncs) {

        this(
                binFile,
                abiFile,
                destinationDir,
                contractName,
                basePackageName,
                useJavaNativeTypes,
                useJavaPrimitiveTypes,
                false,
                Contract.class,
                addressLength,
                abiFuncs);
    }

    public SolidityFunctionWrapperGenerator(
            File binFile,
            File abiFile,
            File destinationDir,
            String contractName,
            String basePackageName,
            boolean useJavaNativeTypes,
            boolean useJavaPrimitiveTypes,
            boolean generateBothCallAndSend,
            Class<? extends Contract> contractClass,
            int addressLength,
            boolean abiFuncs) {

        super(
                contractClass,
                destinationDir,
                basePackageName,
                useJavaNativeTypes,
                useJavaPrimitiveTypes,
                abiFuncs);

        this.binFile = binFile;
        this.abiFile = abiFile;
        this.contractName = contractName;
        this.addressLength = addressLength;
        this.generateBothCallAndSend = generateBothCallAndSend;
    }

    protected List<AbiDefinition> loadContractDefinition(File absFile) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(absFile, AbiDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    public final void generate() throws IOException, ClassNotFoundException {
        generate(false);
    }

    public final void generate(boolean generateKotlin) throws IOException, ClassNotFoundException {
        String binary = Contract.BIN_NOT_PROVIDED;
        if (binFile != null) {
            byte[] bytes = Files.readBytes(binFile);
            binary = new String(bytes);
        }
        List<AbiDefinition> functionDefinitions = loadContractDefinition(abiFile);

        if (!functionDefinitions.isEmpty()) {

            String className = Strings.capitaliseFirstLetter(contractName);
            System.out.print("Generating " + basePackageName + "." + className + " ... ");

            SolidityFunctionWrapper wrapper = new SolidityFunctionWrapper(
                    useJavaNativeTypes,
                    useJavaPrimitiveTypes,
                    generateBothCallAndSend,
                    abiFuncs,
                    addressLength);

            if (generateKotlin) {
                wrapper.generateKotlinFiles(
                        contractClass,
                        contractName,
                        binary,
                        functionDefinitions,
                        destinationDirLocation.toString(),
                        basePackageName,
                        null);
            } else {
                wrapper.generateJavaFiles(
                        contractClass,
                        contractName,
                        binary,
                        functionDefinitions,
                        destinationDirLocation.toString(),
                        basePackageName,
                        null);
            }

            System.out.println("File written to " + destinationDirLocation.toString() + "\n");
        } else {
            System.out.println("Ignoring empty ABI file: " + abiFile.getName() + ".abi" + "\n");
        }
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals(COMMAND_SOLIDITY)) {
            args = tail(args);
        }

        if (args.length > 0 && args[0].equals(COMMAND_GENERATE)) {
            args = tail(args);
        }

        CommandLine.run(new PicocliRunner(), args);
    }

    @Command(
            name = COMMAND_PREFIX,
            mixinStandardHelpOptions = true,
            version = "4.0",
            sortOptions = false)
    private static class PicocliRunner implements Runnable {
        @Option(
                names = {"-a", "--abiFile"},
                description = "abi file with contract definition.",
                required = true)
        private File abiFile;

        @Option(
                names = {"-b", "--binFile"},
                description =
                        "bin file with contract compiled code "
                                + "in order to generate deploy methods.",
                required = false)
        private File binFile;

        @Option(
                names = {"-c", "--contractName"},
                description = "contract name (defaults to ABI file name).",
                required = false)
        private String contractName;

        @Option(
                names = {"-o", "--outputDir"},
                description = "destination base directory.",
                required = true)
        private File destinationFileDir;

        @Option(
                names = {"-p", "--package"},
                description = "base package name.",
                required = true)
        private String packageName;

        @Option(
                names = {"-al", "--addressLength"},
                description = "address length in bytes (defaults to 20).",
                required = false)
        private int addressLength = Address.DEFAULT_LENGTH / Byte.SIZE;

        @Option(
                names = {"-jt", JAVA_TYPES_ARG},
                description = "use native Java types.",
                required = false,
                showDefaultValue = ALWAYS)
        private boolean javaTypes = true;

        @Option(
                names = {"-st", SOLIDITY_TYPES_ARG},
                description = "use solidity types.",
                required = false)
        private boolean solidityTypes;

        @Option(
                names = {"-pt", PRIMITIVE_TYPES_ARG},
                description = "use Java primitive types.",
                required = false)
        private boolean primitiveTypes = false;

        @Option(
                names = {"-B", "--generateBoth"},
                description = "generate both call and send functions.",
                required = false)
        private boolean generateBothCallAndSend;

        @Option(
                names = {"-r", ABI_FUNCS},
                description = "ABI encoded function call getters.",
                required = false)
        private boolean abiFuncs = false;

        @Option(
                names = {"-kt", "--kotlin"},
                description = "generate Kotlin code instead of Java.",
                required = false)
        private boolean generateKotlin = false;

        @Override
        public void run() {
            try {
                // grouping is not implemented in picocli yet(planned for 3.1), therefore
                // simply check if solidityTypes were requested
                boolean useJavaTypes = !(solidityTypes);

                if (contractName == null || contractName.isEmpty()) {
                    contractName = getFileNameNoExtension(abiFile.getName());
                }

                new SolidityFunctionWrapperGenerator(
                                binFile,
                                abiFile,
                                destinationFileDir,
                                contractName,
                                packageName,
                                useJavaTypes,
                                primitiveTypes,
                                generateBothCallAndSend,
                                Contract.class,
                                addressLength,
                                abiFuncs)
                        .generate(generateKotlin);
            } catch (Exception e) {
                exitError(e);
            }
        }
    }
}
