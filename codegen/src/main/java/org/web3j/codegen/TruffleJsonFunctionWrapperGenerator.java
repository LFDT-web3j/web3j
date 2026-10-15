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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import picocli.CommandLine;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.tx.ChainId;
import org.web3j.utils.Strings;

import static org.web3j.codegen.Console.exitError;
import static org.web3j.utils.Collection.tail;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.ExecutionException;
import static picocli.CommandLine.Help;
import static picocli.CommandLine.Help.Visibility.ALWAYS;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.ParseResult;
import static picocli.CommandLine.RunLast;

/**
 * Java wrapper source code generator for Truffle JSON format. Truffle embeds the Solidity ABI
 * formatted JSON in its own format. That format also gives access to the binary code. It also
 * contains information about deployment addresses. This should make integration with Truffle
 * easier.
 */
@SuppressWarnings("deprecation")
public class TruffleJsonFunctionWrapperGenerator extends FunctionWrapperGenerator {

    public static final String COMMAND_TRUFFLE = "truffle";
    public static final String COMMAND_GENERATE = "generate";
    public static final String COMMAND_PREFIX = COMMAND_TRUFFLE + " " + COMMAND_GENERATE;

    private final String jsonFileLocation;
    private final boolean generateBothCallAndSend;

    public TruffleJsonFunctionWrapperGenerator(
            String jsonFileLocation,
            String destinationDirLocation,
            String basePackageName,
            boolean useJavaNativeTypes,
            boolean generateBothCallAndSend) {

        super(new File(destinationDirLocation), basePackageName, useJavaNativeTypes);
        this.jsonFileLocation = jsonFileLocation;
        this.generateBothCallAndSend = generateBothCallAndSend;
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals(COMMAND_TRUFFLE)) {
            args = tail(args);
        }
        if (args.length > 0 && args[0].equals(COMMAND_GENERATE)) {
            args = tail(args);
        }
        CommandLine cmd = new CommandLine(new PicocliRunner());
        cmd.parseWithHandlers(
                new RunLast().useOut(System.out).useAnsi(Help.Ansi.AUTO),
                RethrowExceptionHandler.HANDLER,
                args);
    }

    @Command(
            name = COMMAND_PREFIX,
            mixinStandardHelpOptions = true,
            version = "4.0",
            sortOptions = false)
    private static class PicocliRunner implements Callable<Void> {

        @Option(
                names = {"-jt", JAVA_TYPES_ARG},
                description = "use native Java types.",
                showDefaultValue = ALWAYS)
        boolean useJavaNativeTypes;

        @Option(
                names = {"-st", SOLIDITY_TYPES_ARG},
                description = "use solidity types.")
        boolean useSolidityTypes;

        @Parameters(index = "0")
        String jsonFileLocation;

        @Option(
                names = {"-o", "--outputDir"},
                description = "destination base directory.",
                required = true)
        private String destinationDirLocation;

        @Option(
                names = {"-B", "--generateBoth"},
                description = "generate both call and send functions.",
                required = false)
        private boolean generateBothCallAndSend;

        @Option(
                names = {"-p", "--package"},
                description = "base package name.",
                required = true)
        private String basePackageName;

        @Override
        public Void call() throws Exception {
            useJavaNativeTypes = !(useSolidityTypes);
            new TruffleJsonFunctionWrapperGenerator(
                            jsonFileLocation,
                            destinationDirLocation,
                            basePackageName,
                            useJavaNativeTypes,
                            generateBothCallAndSend)
                    .generate();
            return null;
        }
    }

    public static class RethrowExceptionHandler
            implements CommandLine.IExceptionHandler2<List<Object>> {

        public static final RethrowExceptionHandler HANDLER = new RethrowExceptionHandler();

        @Override
        public List<Object> handleParseException(ParameterException ex, String[] args) {
            throw ex;
        }

        @Override
        public List<Object> handleExecutionException(
                ExecutionException ex, ParseResult parseResult) {
            throw ex;
        }
    }

    static Contract loadContractDefinition(File jsonFile) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        return objectMapper.readValue(jsonFile, Contract.class);
    }

    @SuppressWarnings("unchecked")
    public void generate() throws IOException, ClassNotFoundException {

        File truffleJsonFile = new File(jsonFileLocation);
        if (!truffleJsonFile.exists() || !truffleJsonFile.canRead()) {
            exitError("Invalid input json file specified: " + jsonFileLocation);
        }

        String fileName = truffleJsonFile.getName();
        String contractName = getFileNameNoExtension(fileName);

        Contract c = loadContractDefinition(truffleJsonFile);
        if (c == null) {
            exitError("Unable to parse input json file");
        } else {
            String className = Strings.capitaliseFirstLetter(contractName);
            System.out.printf("Generating " + basePackageName + "." + className + " ... ");
            Map<String, String> addresses;
            if (c.networks != null && !c.networks.isEmpty()) {
                addresses =
                        c.networks.entrySet().stream()
                                .filter(
                                        e ->
                                                (e.getValue() != null
                                                        && e.getValue().getAddress() != null))
                                .collect(
                                        Collectors.toMap(
                                                Map.Entry::getKey, e -> e.getValue().getAddress()));
            } else {
                addresses = Collections.EMPTY_MAP;
            }
            new SolidityFunctionWrapper(
                            useJavaNativeTypes, Address.DEFAULT_LENGTH, generateBothCallAndSend)
                    .generateJavaFiles(
                            contractName,
                            c.getBytecode(),
                            c.getAbi(),
                            destinationDirLocation.toString(),
                            basePackageName,
                            addresses);
            System.out.println("File written to " + destinationDirLocation.toString() + "\n");
        }
    }

    /**
     * Truffle Contract
     *
     * <p>Describes a contract exported by and consumable by Truffle, which may include information
     * about deployed instances on networks.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "contractName",
        "abi",
        "bytecode",
        "deployedBytecode",
        "sourceMap",
        "deployedSourceMap",
        "source",
        "sourcePath",
        "ast",
        "compiler",
        "networks",
        "schemaVersion",
        "updatedAt"
    })
    public static class Contract {

        @JsonProperty("contractName")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "^[a-zA-Z_][a-zA-Z0-9_]*$")
        public String contractName;

        @JsonProperty(value = "abi", required = true)
        public List<AbiDefinition> abi;

        @JsonProperty("bytecode")
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "^0x0$|^0x([a-fA-F0-9]{2}|__.{38})+$")
        public String bytecode;

        @JsonProperty("deployedBytecode")
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "^0x0$|^0x([a-fA-F0-9]{2}|__.{38})+$")
        public String deployedBytecode;

        @JsonProperty("sourceMap")
        public String sourceMap;

        @JsonProperty("deployedSourceMap")
        public String deployedSourceMap;

        @JsonProperty("source")
        public String source;

        @JsonProperty("sourcePath")
        public String sourcePath;

        @JsonProperty("ast")
        public JsonNode ast;

        @JsonProperty("compiler")
        public Compiler compiler;

        @JsonProperty("networks")
        public Map<String, NetworkInfo> networks;

        @JsonProperty("schemaVersion")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "[0-9]+\\.[0-9]+\\.[0-9]+")
        public String schemaVersion;

        @JsonProperty("updatedAt")
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                timezone = "GMT")
        public Date updatedAt;

        public Contract() {}

        public Contract(
                String contractName,
                List<AbiDefinition> abi,
                String bytecode,
                String deployedBytecode,
                String sourceMap,
                String deployedSourceMap,
                String source,
                String sourcePath,
                JsonNode ast,
                Compiler compiler,
                Map<String, NetworkInfo> networks,
                String schemaVersion,
                Date updatedAt) {
            super();
            this.contractName = contractName;
            this.abi = abi;
            this.bytecode = bytecode;
            this.deployedBytecode = deployedBytecode;
            this.sourceMap = sourceMap;
            this.deployedSourceMap = deployedSourceMap;
            this.source = source;
            this.sourcePath = sourcePath;
            this.ast = ast;
            this.compiler = compiler;
            this.networks = networks;
            this.schemaVersion = schemaVersion;
            this.updatedAt = updatedAt;
        }

        public String getContractName() {
            return contractName;
        }

        public List<AbiDefinition> getAbi() {
            return abi;
        }

        public String getBytecode() {
            return bytecode;
        }

        public NetworkInfo getNetwork(String networkId) {
            return networks == null ? null : networks.get(networkId);
        }

        public String getAddress(String networkId) {
            NetworkInfo network = getNetwork(networkId);
            return network == null ? null : network.getAddress();
        }

        /**
         * Convenience method to get the deployed address of the contract.
         *
         * @param network the contract's address on this Ethereum network
         * @return the contract's address or <code>null</code> if there isn't one known.
         */
        public String getAddress(Network network) {
            return getAddress(Long.toString(network.id));
        }

        /*
         * c.f., org.web3j.tx.ChainId
         *
         * This should be updated with https://github.com/hyperledger/web3j/issues/234
         */
        enum Network {
            olympic(0),
            mainnet(ChainId.MAINNET),
            morden(ChainId.EXPANSE_MAINNET),
            ropsten(ChainId.ROPSTEN),
            rinkeby(ChainId.RINKEBY),
            kovan(ChainId.KOVAN);

            public final long id;

            Network(long id) {
                this.id = id;
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"name", "version"})
    public static class Compiler {

        @JsonProperty("name")
        public String name;

        @JsonProperty("version")
        public String version;

        @JsonIgnore
        private Map<String, JsonNode> additionalProperties = new HashMap<String, JsonNode>();

        public Compiler() {}

        public Compiler(String name, String version) {
            super();
            this.name = name;
            this.version = version;
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, JsonNode value) {
            this.additionalProperties.put(name, value);
        }

        public Compiler withAdditionalProperty(String name, JsonNode value) {
            this.additionalProperties.put(name, value);
            return this;
        }
    }

    // For now we just ignore "events"
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"events", "links", "address"})
    public static class NetworkInfo {

        @JsonProperty("events")
        public Map<String, JsonNode> events;

        @JsonProperty("links")
        public Map<String, JsonNode> links;

        @JsonProperty("address")
        public String address;

        public NetworkInfo() {}

        public NetworkInfo(
                Map<String, JsonNode> events, Map<String, JsonNode> links, String address) {
            super();
            this.events = events;
            this.links = links;
            this.address = address;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
