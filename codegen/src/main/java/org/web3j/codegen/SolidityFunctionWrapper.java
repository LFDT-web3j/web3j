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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.CustomError;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.primitive.Byte;
import org.web3j.abi.datatypes.primitive.Char;
import org.web3j.abi.datatypes.primitive.Double;
import org.web3j.abi.datatypes.primitive.Float;
import org.web3j.abi.datatypes.primitive.Int;
import org.web3j.abi.datatypes.primitive.Long;
import org.web3j.abi.datatypes.primitive.Short;
import org.web3j.abi.datatypes.reflection.Parameterized;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.AbiDefinition.NamedType;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Collection;
import org.web3j.utils.Strings;
import org.web3j.utils.Version;

/** Generate Java Classes based on generated Solidity bin and abi files. */
public class SolidityFunctionWrapper extends Generator {

    private static final String BINARY = "BINARY";
    private static final String LIBRARIES_LINKED_BINARY = "librariesLinkedBinary";
    private static final String WEB3J = "web3j";
    private static final String CREDENTIALS = "credentials";
    private static final String CONTRACT_GAS_PROVIDER = "contractGasProvider";
    private static final String TRANSACTION_MANAGER = "transactionManager";
    private static final String INITIAL_VALUE = "initialWeiValue";
    private static final String CONTRACT_ADDRESS = "contractAddress";
    private static final String GAS_PRICE = "gasPrice";
    private static final String GAS_LIMIT = "gasLimit";
    private static final String FILTER = "filter";
    private static final String START_BLOCK = "startBlock";
    private static final String END_BLOCK = "endBlock";
    private static final String WEI_VALUE = "weiValue";
    private static final String FUNC_NAME_PREFIX = "FUNC_";
    private static final String TYPE_FUNCTION = "function";
    private static final String TYPE_EVENT = "event";
    private static final String TYPE_ERROR = "error";
    private static final String TYPE_CONSTRUCTOR = "constructor";

    private static final ClassName LOG = ClassName.get(Log.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(SolidityFunctionWrapper.class);
    private static final Pattern ARRAY_SUFFIX = Pattern.compile("\\[(\\d*)]");

    private static final String CODEGEN_WARNING =
            "<p>Auto generated code.\n"
                    + "<p><strong>Do not modify!</strong>\n"
                    + "<p>Please use the "
                    + "<a href=\"https://docs.web3j.io/command_line.html\">web3j command line tools</a>,\n"
                    + "or the "
                    + SolidityFunctionWrapperGenerator.class.getName()
                    + " in the \n"
                    + "<a href=\"https://github.com/LFDT-web3j/web3j/tree/main/codegen\">"
                    + "codegen module</a> to update.\n";

    private final boolean useNativeJavaTypes;
    private final boolean useJavaPrimitiveTypes;
    private final boolean generateBothCallAndSend;
    private final boolean abiFuncs;
    private final int addressLength;

    private final HashMap<String, ClassName> structClassNameMap = new HashMap<>();

    private final List<NamedType> structsNamedTypeList = new ArrayList<>();

    private static final String regex = "(\\w+)(?:\\[(.*?)\\])(?:\\[(.*?)\\])?";
    private static final Pattern pattern = Pattern.compile(regex);

    private final GenerationReporter reporter;

    public SolidityFunctionWrapper(boolean useNativeJavaTypes) {
        this(useNativeJavaTypes, Address.DEFAULT_LENGTH);
    }

    public SolidityFunctionWrapper(boolean useNativeJavaTypes, int addressLength) {
        this(useNativeJavaTypes, false, false, addressLength);
    }

    public SolidityFunctionWrapper(
            boolean useNativeJavaTypes, int addressLength, boolean generateBothCallAndSend) {
        this(useNativeJavaTypes, false, generateBothCallAndSend, addressLength);
    }

    public SolidityFunctionWrapper(
            boolean useNativeJavaTypes,
            boolean useJavaPrimitiveTypes,
            boolean generateBothCallAndSend,
            int addressLength) {
        this(
                useNativeJavaTypes,
                useJavaPrimitiveTypes,
                generateBothCallAndSend,
                false,
                addressLength,
                new LogGenerationReporter(LOGGER));
    }

    public SolidityFunctionWrapper(
            boolean useNativeJavaTypes,
            boolean useJavaPrimitiveTypes,
            boolean generateBothCallAndSend,
            boolean abiFuncs,
            int addressLength) {
        this(
                useNativeJavaTypes,
                useJavaPrimitiveTypes,
                generateBothCallAndSend,
                abiFuncs,
                addressLength,
                new LogGenerationReporter(LOGGER));
    }

    public SolidityFunctionWrapper(
            boolean useNativeJavaTypes,
            boolean useJavaPrimitiveTypes,
            boolean generateBothCallAndSend,
            int addressLength,
            GenerationReporter reporter) {
        this(
                useNativeJavaTypes,
                useJavaPrimitiveTypes,
                generateBothCallAndSend,
                false,
                addressLength,
                reporter);
    }

    public SolidityFunctionWrapper(
            boolean useNativeJavaTypes,
            boolean useJavaPrimitiveTypes,
            boolean generateBothCallAndSend,
            boolean abiFuncs,
            int addressLength,
            GenerationReporter reporter) {
        this.useNativeJavaTypes = useNativeJavaTypes;
        this.useJavaPrimitiveTypes = useJavaPrimitiveTypes;
        this.abiFuncs = abiFuncs;
        this.addressLength = addressLength;
        this.reporter = reporter;
        this.generateBothCallAndSend = generateBothCallAndSend;
    }

    public void generateJavaFiles(
            String contractName,
            String bin,
            List<AbiDefinition> abi,
            String destinationDir,
            String basePackageName,
            Map<String, String> addresses)
            throws IOException, ClassNotFoundException {

        generateJavaFiles(
                Contract.class, contractName, bin, abi, destinationDir, basePackageName, addresses);
    }

    public void generateJavaFiles(
            Class<? extends Contract> contractClass,
            String contractName,
            String bin,
            List<AbiDefinition> abi,
            String destinationDir,
            String basePackageName,
            Map<String, String> addresses)
            throws IOException, ClassNotFoundException {

        if (!java.lang.reflect.Modifier.isAbstract(contractClass.getModifiers())) {
            throw new IllegalArgumentException("Contract base class must be abstract");
        }

        String className = Strings.capitaliseFirstLetter(contractName);
        TypeSpec.Builder classBuilder = createClassBuilder(contractClass, className, bin);

        classBuilder.addAnnotation(
                AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"rawtypes\"")
                        .build());

        classBuilder.addAnnotation(
                AnnotationSpec.builder(ClassName.get("javax.annotation.processing", "Generated"))
                        .addMember(
                                "value", "$S", "org.web3j.codegen.SolidityFunctionWrapperGenerator")
                        .build());

        classBuilder.addMethod(buildConstructor(Credentials.class, CREDENTIALS, false));
        classBuilder.addMethod(buildConstructor(Credentials.class, CREDENTIALS, true));
        classBuilder.addMethod(
                buildConstructor(TransactionManager.class, TRANSACTION_MANAGER, false));
        classBuilder.addMethod(
                buildConstructor(TransactionManager.class, TRANSACTION_MANAGER, true));
        classBuilder.addFields(buildFuncNameConstants(abi));
        classBuilder.addTypes(buildStructTypes(abi));
        buildStructsNamedTypesList(abi);
        classBuilder.addMethods(buildFunctionDefinitions(className, classBuilder, abi));
        classBuilder.addMethod(buildLoad(className, Credentials.class, CREDENTIALS, false));
        classBuilder.addMethod(
                buildLoad(className, TransactionManager.class, TRANSACTION_MANAGER, false));
        classBuilder.addMethod(buildLoad(className, Credentials.class, CREDENTIALS, true));
        classBuilder.addMethod(
                buildLoad(className, TransactionManager.class, TRANSACTION_MANAGER, true));
        if (!bin.equals(Contract.BIN_NOT_PROVIDED)) {
            classBuilder.addMethods(buildDeployMethods(className, classBuilder, abi));
            classBuilder.addMethod(buildLinkLibraryMethod());
            classBuilder.addMethod(buildGetDeploymentBinaryMethod());
        }

        addAddressesSupport(classBuilder, addresses);

        write(basePackageName, classBuilder.build(), destinationDir);
    }

    private void buildStructsNamedTypesList(List<AbiDefinition> abi) {
        structsNamedTypeList.addAll(
                abi.stream()
                        .flatMap(
                                definition -> {
                                    List<AbiDefinition.NamedType> parameters = new ArrayList<>();
                                    parameters.addAll(definition.getInputs());
                                    parameters.addAll(definition.getOutputs());
                                    return parameters.stream()
                                            .filter(
                                                    namedType ->
                                                            namedType.getType().equals("tuple"));
                                })
                        .collect(Collectors.toList()));
    }

    /**
     * Verifies if the two structs are the same. Equal structs means: - They have the same field
     * names - They have the same field types The order of declaring the fields does not matter.
     *
     * @return True if they are the same fields
     */
    private boolean isSameStruct(NamedType base, NamedType target) {
        for (NamedType baseField : base.getComponents()) {
            if (!target.getComponents().stream()
                    .anyMatch(
                            targetField ->
                                    baseField.getType().equals(targetField.getType())
                                            && baseField.getName().equals(targetField.getName())))
                return false;
        }
        return true;
    }

    private void addAddressesSupport(TypeSpec.Builder classBuilder, Map<String, String> addresses) {
        if (addresses != null) {

            ClassName stringType = ClassName.get(String.class);
            ClassName mapType = ClassName.get(HashMap.class);
            TypeName mapStringString = ParameterizedTypeName.get(mapType, stringType, stringType);
            FieldSpec addressesStaticField =
                    FieldSpec.builder(
                                    mapStringString,
                                    "_addresses",
                                    Modifier.PROTECTED,
                                    Modifier.STATIC,
                                    Modifier.FINAL)
                            .build();
            classBuilder.addField(addressesStaticField);

            final CodeBlock.Builder staticInit = CodeBlock.builder();
            staticInit.addStatement("_addresses = new HashMap<String, String>()");
            addresses.forEach(
                    (k, v) ->
                            staticInit.addStatement(
                                    String.format("_addresses.put(\"%1s\", \"%2s\")", k, v)));
            classBuilder.addStaticBlock(staticInit.build());

            // See org.web3j.tx.Contract#getStaticDeployedAddress(String)
            MethodSpec getAddress =
                    MethodSpec.methodBuilder("getStaticDeployedAddress")
                            .addModifiers(Modifier.PROTECTED)
                            .returns(stringType)
                            .addParameter(stringType, "networkId")
                            .addCode(
                                    CodeBlock.builder()
                                            .addStatement("return _addresses.get(networkId)")
                                            .build())
                            .build();
            classBuilder.addMethod(getAddress);

            MethodSpec getPreviousAddress =
                    MethodSpec.methodBuilder("getPreviouslyDeployedAddress")
                            .addModifiers(Modifier.PUBLIC)
                            .addModifiers(Modifier.STATIC)
                            .returns(stringType)
                            .addParameter(stringType, "networkId")
                            .addCode(
                                    CodeBlock.builder()
                                            .addStatement("return _addresses.get(networkId)")
                                            .build())
                            .build();
            classBuilder.addMethod(getPreviousAddress);
        }
    }

    private TypeSpec.Builder createClassBuilder(
            Class<? extends Contract> contractClass, String className, String binary) {

        String javadoc = CODEGEN_WARNING + getWeb3jVersion();

        TypeSpec.Builder classBuilder =
                TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc(javadoc)
                        .superclass(contractClass)
                        .addField(createBinaryDefinition(binary));

        if (!binary.equals(Contract.BIN_NOT_PROVIDED)) {
            classBuilder.addField(createLibrariesLinkedBinaryField());
        }
        return classBuilder;
    }

    private String getWeb3jVersion() {
        String version;

        try {
            // This only works if run as part of the web3j command line tools which contains
            // a version.properties file
            version = Version.getVersion();
        } catch (IOException | NullPointerException e) {
            version = Version.DEFAULT;
        }
        return "\n<p>Generated with web3j version " + version + ".\n";
    }

    private FieldSpec createLibrariesLinkedBinaryField() {
        return FieldSpec.builder(String.class, LIBRARIES_LINKED_BINARY)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();
    }

    FieldSpec createBinaryDefinition(String binary) {
        if (binary.length() < 65534) {
            return FieldSpec.builder(String.class, BINARY)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                    .initializer("$S", binary)
                    .build();
        }

        String[] argsArray = binary.split("(?<=\\G.{65534})");
        StringBuilder stringBuilderString = new StringBuilder().append("new StringBuilder()");
        for (String s : argsArray) {
            stringBuilderString.append(".append(\"");
            stringBuilderString.append(
                    s.replaceAll("\\$", "\\$\\$")); // escape $ which bytecode may contain
            stringBuilderString.append("\")");
        }
        stringBuilderString.append(".toString()");
        return FieldSpec.builder(String.class, BINARY)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer(CodeBlock.of(stringBuilderString.toString()))
                .build();
    }

    private FieldSpec createEventDefinition(
            String name,
            List<NamedTypeName> parameters,
            Map<String, Integer> eventsCount,
            AbiDefinition event) {

        CodeBlock initializer = buildVariableLengthEventInitializer(name, parameters);

        Integer occurrences = eventsCount.get(name);
        if (occurrences > 1) {
            event.setName(name + (occurrences - 1));
            eventsCount.replace(name, occurrences - 1);
            name = event.getName();
        }

        return FieldSpec.builder(Event.class, buildEventDefinitionName(name))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(initializer)
                .build();
    }

    void buildCustomErrorDefinitions(
            @NotNull AbiDefinition abiDefinition,
            TypeSpec.Builder classBuilder,
            Map<String, Integer> customErrorsOccurrences)
            throws ClassNotFoundException {
        if (abiDefinition.getName().isEmpty()) {
            System.out.println(
                    "\nWarning: Blank name field found in custom error abi definition. "
                            + "No code will be generated for this abi definition.");
            return;
        }

        List<NamedTypeName> parameters = new ArrayList<>();
        for (AbiDefinition.NamedType namedType : abiDefinition.getInputs()) {
            final TypeName typeName;
            if (namedType.getType().equals("tuple")) {
                typeName = structClassNameMap.get(namedType.structIdentifier());
            } else if (namedType.getType().startsWith("tuple")
                    && namedType.getType().contains("[")) {
                typeName = buildStructArrayTypeName(namedType, false);
            } else {
                typeName = buildTypeName(namedType.getType(), useJavaPrimitiveTypes);
            }
            parameters.add(new NamedTypeName(namedType, typeName));
        }

        classBuilder.addField(
                createCustomErrorDefinition(
                        abiDefinition.getName(), parameters, customErrorsOccurrences));
    }

    /**
     * Generates code for CustomError instance definition. For example, with name of {@code
     * InvalidAddress}, generates:
     *
     * <pre>{@code
     * public static final Event INVALIDADDRESS_ERROR = new CustomError(...);
     * ;
     * }</pre>
     */
    @NotNull
    private FieldSpec createCustomErrorDefinition(
            String customErrorName,
            List<NamedTypeName> parameters,
            Map<String, Integer> customErrorsOccurrences) {

        return FieldSpec.builder(
                        CustomError.class,
                        buildCustomErrorDefinitionName(customErrorName, customErrorsOccurrences))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(buildVariableLengthCustomErrorInitializer(customErrorName, parameters))
                .build();
    }

    /**
     * Generates code for CustomError instance. For example, with abi definition of:
     *
     * <pre>{@code
     * {
     *   "inputs":[
     *     {"internalType":"address","type":"address"},
     *     {"internalType":"string","type":"string"}
     *   ],
     *   "name":"InvalidAddress",
     *   "type":"error"
     * }
     * }</pre>
     *
     * generates:
     *
     * <pre>{@code
     * new CustomError("InvalidAddress",
     *       Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}));
     * }</pre>
     */
    @NotNull
    private static CodeBlock buildVariableLengthCustomErrorInitializer(
            String errorName, @NotNull List<NamedTypeName> parameterTypes) {

        List<Object> objects = new ArrayList<>();
        objects.add(CustomError.class);
        objects.add(errorName);

        objects.add(Arrays.class);
        objects.add(TypeReference.class);
        for (NamedTypeName parameterType : parameterTypes) {
            objects.add(TypeReference.class);
            objects.add(parameterType.getTypeName());
        }

        String asListParams =
                parameterTypes.stream()
                        .map(type -> "new $T<$T>() {}")
                        .collect(Collectors.joining(", "));

        return CodeBlock.builder()
                .addStatement(
                        "new $T($S, \n" + "$T.<$T<?>>asList(" + asListParams + "))",
                        objects.toArray())
                .build();
    }

    /**
     * Creates variable names for CustomError definitions. Duplicate names will be suffixed with
     * number.
     *
     * <pre>{@code
     * Given error abi definitions;
     * [
     *   {"name": "aa", "type": "error", "inputs": []},
     *   {"name": "aA", "type": "error", "inputs": []},
     *   {"name": "bbb", "type": "error", "inputs": []},
     *   {"name": "bBB", "type": "error", "inputs": [{"type": "string"}]},
     *   {"name": "bbB", "type": "error", "inputs": []}
     * ]
     *
     * Then variables with names;
     * public static final CustomError AA_ERROR = ...
     * public static final CustomError AA1_ERROR = ...
     * public static final CustomError BBB_ERROR = ...
     * public static final CustomError BBB1_ERROR = ...
     * public static final CustomError BBB2_ERROR = ...
     * }</pre>
     */
    @NotNull
    private String buildCustomErrorDefinitionName(
            String customErrorName, @NotNull Map<String, Integer> customErrorsOccurrences) {

        customErrorName = customErrorName.toUpperCase();
        Integer occurrences = customErrorsOccurrences.get(customErrorName);
        if (occurrences > 1) {
            customErrorsOccurrences.replace(customErrorName, occurrences - 1);
            customErrorName = customErrorName + (occurrences - 1);
        }
        return customErrorName + "_ERROR";
    }

    private String buildEventDefinitionName(String eventName) {
        return eventName.toUpperCase() + "_EVENT";
    }

    List<MethodSpec> buildFunctionDefinitions(
            String className,
            TypeSpec.Builder classBuilder,
            List<AbiDefinition> functionDefinitions)
            throws ClassNotFoundException {

        Set<String> duplicateFunctionNames = getDuplicateFunctionNames(functionDefinitions);
        Map<String, Integer> eventsCount = getDuplicatedEventNames(functionDefinitions);
        Map<String, Integer> customErrorsOccurrences =
                getDuplicateCustomErrorNames(functionDefinitions);
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (AbiDefinition functionDefinition : functionDefinitions) {
            if (functionDefinition.getType().equals(TYPE_FUNCTION)) {
                String functionName = funcNameToConst(functionDefinition.getName(), true);
                boolean useUpperCase = !duplicateFunctionNames.contains(functionName);
                methodSpecs.addAll(buildFunctions(functionDefinition, useUpperCase));
            } else if (functionDefinition.getType().equals(TYPE_EVENT)) {
                methodSpecs.addAll(
                        buildEventFunctions(functionDefinition, classBuilder, eventsCount));
            } else if (functionDefinition.getType().equals(TYPE_ERROR)) {
                buildCustomErrorDefinitions(
                        functionDefinition, classBuilder, customErrorsOccurrences);
            }
        }
        return methodSpecs;
    }

    // TODO: Refactor to use for types of function, event and error
    Map<String, Integer> getDuplicateCustomErrorNames(@NotNull List<AbiDefinition> abiDefinitions) {
        Map<String, Integer> countMap = new HashMap<>();

        abiDefinitions.stream()
                .filter(abi -> TYPE_ERROR.equals(abi.getType()) && abi.getName() != null)
                .forEach(
                        abi -> {
                            String functionName = abi.getName().toUpperCase();
                            if (countMap.containsKey(functionName)) {
                                int count = countMap.get(functionName);
                                countMap.put(functionName, count + 1);
                            } else {
                                countMap.put(functionName, 1);
                            }
                        });

        return countMap;
    }

    Map<String, Integer> getDuplicatedEventNames(List<AbiDefinition> functionDefinitions) {
        Map<String, Integer> countMap = new HashMap<>();

        functionDefinitions.stream()
                .filter(
                        function ->
                                TYPE_EVENT.equals(function.getType()) && function.getName() != null)
                .forEach(
                        function -> {
                            String functionName = function.getName();
                            if (countMap.containsKey(functionName)) {
                                int count = countMap.get(functionName);
                                countMap.put(functionName, count + 1);
                            } else {
                                countMap.put(functionName, 1);
                            }
                        });

        return countMap;
    }

    private List<TypeSpec> buildStructTypes(final List<AbiDefinition> functionDefinitions)
            throws ClassNotFoundException {
        final List<AbiDefinition.NamedType> orderedKeys = extractStructs(functionDefinitions);
        int structCounter = 0;
        final List<TypeSpec> structs = new ArrayList<>();
        final Map<String, Integer> structNamesCountPreview = new HashMap<>();

        for (final AbiDefinition.NamedType namedType : orderedKeys) {
            final String internalType = namedType.getInternalType();
            if (internalType != null && !internalType.isEmpty()) {
                final String structName = getStructName(internalType);
                structNamesCountPreview.putIfAbsent(structName, 0);
                structNamesCountPreview.compute(structName, (s, count) -> count + 1);
            }
        }

        for (final AbiDefinition.NamedType namedType : orderedKeys) {
            final String internalType = namedType.getInternalType();
            final String structName;
            if (internalType == null || internalType.isEmpty()) {
                structName = "Struct" + structCounter;
            } else {
                String tempStructName = getStructName(internalType);
                if (structNamesCountPreview.getOrDefault(tempStructName, 0) > 1) {
                    structName = getStructName(internalType.replace(".", "_"));
                } else {
                    structName = tempStructName;
                }
            }

            final TypeSpec.Builder builder =
                    TypeSpec.classBuilder(structName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            final MethodSpec.Builder constructorBuilder =
                    MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement(
                                    "super("
                                            + addParameters(
                                                    MethodSpec.constructorBuilder(),
                                                    namedType.getComponents(),
                                                    useNativeJavaTypes)
                                            + ")");

            final MethodSpec.Builder nativeConstructorBuilder =
                    MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement(
                                    "super("
                                            + addParameters(
                                                    MethodSpec.constructorBuilder(),
                                                    namedType.getComponents(),
                                                    false)
                                            + ")");

            for (AbiDefinition.NamedType component : namedType.getComponents()) {
                String type = component.getType();
                TypeName typeName, nativeTypeName;
                AnnotationSpec annotationSpec = null;
                if (type.equals("tuple")) {
                    nativeTypeName =
                            typeName = structClassNameMap.get(component.structIdentifier());
                } else if (type.startsWith("tuple") && type.contains("[")) {
                    nativeTypeName = buildStructArrayTypeName(component, false);
                    typeName = buildStructArrayTypeName(component, useNativeJavaTypes);

                    // adding extra annotation for dynamic types
                    annotationSpec =
                            AnnotationSpec.builder(Parameterized.class)
                                    .addMember(
                                            "type",
                                            "$T.class",
                                            ClassName.get("", resolveStructName(component)))
                                    .build();
                } else {
                    nativeTypeName = buildTypeName(type, useJavaPrimitiveTypes);
                    typeName = getWrapperType(nativeTypeName);
                    if (type.contains("[")) {
                        annotationSpec =
                                AnnotationSpec.builder(Parameterized.class)
                                        .addMember(
                                                "type",
                                                "$T.class",
                                                TypeReference.makeTypeReference(
                                                                type.substring(
                                                                        0, type.indexOf('[')))
                                                        .getClassType())
                                        .build();
                    }
                }
                final String componentName =
                        !SourceVersion.isName(component.getName())
                                ? "_" + component.getName()
                                : component.getName();
                builder.addField(typeName, componentName, Modifier.PUBLIC);
                constructorBuilder.addParameter(typeName, componentName);
                ParameterSpec.Builder nativeParameterBuilder =
                        ParameterSpec.builder(nativeTypeName, componentName);
                if (annotationSpec != null) {
                    nativeParameterBuilder.addAnnotation(annotationSpec);
                }
                nativeConstructorBuilder.addParameter(nativeParameterBuilder.build());

                constructorBuilder.addStatement("this." + componentName + " = " + componentName);
                nativeConstructorBuilder.addStatement(
                        "this."
                                + componentName
                                + " = "
                                + componentName
                                + adjustToNativeTypeIfNecessary(component),
                        Collectors.class);
            }

            builder.superclass(namedType.isDynamic() ? DynamicStruct.class : StaticStruct.class);
            builder.addMethod(constructorBuilder.build());
            if (useNativeJavaTypes
                    && namedType.getComponents().stream()
                            .anyMatch(
                                    component ->
                                            structClassNameMap.get(component.structIdentifier())
                                                    == null)) {
                builder.addMethod(nativeConstructorBuilder.build());
            }
            structClassNameMap.put(namedType.structIdentifier(), ClassName.get("", structName));
            structs.add(builder.build());
            structCounter++;
        }
        return structs;
    }

    @NotNull
    private static String getStructName(String internalType) {
        final String fullStructName = internalType.substring(internalType.lastIndexOf(" ") + 1);
        String tempStructName = fullStructName.substring(fullStructName.lastIndexOf(".") + 1);
        final String structName =
                SourceVersion.isName(tempStructName) ? tempStructName : "_" + tempStructName;
        return structName;
    }

    private String adjustToNativeTypeIfNecessary(NamedType component) {
        if (useNativeJavaTypes && structClassNameMap.get(component.structIdentifier()) == null) {
            if (ARRAY_SUFFIX.matcher(component.getType()).find()
                    && structClassNameMap.get(normalizeNamedType(component).structIdentifier())
                            == null) {
                return ".getValue().stream().map(v -> v.getValue()).collect($T.toList())";
            } else {
                return ".getValue()";
            }
        } else {
            return "";
        }
    }

    private NamedType normalizeNamedType(NamedType namedType) {
        // dynamic array
        if (namedType.getType().endsWith("[]") && namedType.getInternalType().endsWith("[]")) {
            return new NamedType(
                    namedType.getName(),
                    namedType.getType().substring(0, namedType.getType().length() - 2),
                    namedType.getComponents(),
                    namedType
                            .getInternalType()
                            .substring(0, namedType.getInternalType().length() - 2),
                    namedType.isIndexed());
        } else if (namedType.getType().startsWith("tuple[")
                && namedType.getInternalType().contains("[")
                && namedType.getInternalType().endsWith("]")) { // static array

            return new NamedType(
                    namedType.getName(),
                    namedType.getType().substring(0, namedType.getType().indexOf("[")),
                    namedType.getComponents(),
                    namedType
                            .getInternalType()
                            .substring(0, namedType.getInternalType().indexOf("[")),
                    namedType.isIndexed());
        } else {
            return namedType;
        }
    }

    @NotNull
    private List<AbiDefinition.NamedType> extractStructs(
            final List<AbiDefinition> functionDefinitions) {
        final HashMap<String, AbiDefinition.NamedType> structMap = new LinkedHashMap<>();
        functionDefinitions.stream()
                .flatMap(
                        definition -> {
                            List<AbiDefinition.NamedType> parameters = new ArrayList<>();
                            parameters.addAll(definition.getInputs());
                            parameters.addAll(definition.getOutputs());
                            return parameters.stream()
                                    .map(this::normalizeNamedType)
                                    .filter(namedType -> namedType.getType().equals("tuple"));
                        })
                .forEach(
                        namedType -> {
                            structMap.put(namedType.structIdentifier(), namedType);
                            extractNested(namedType).stream()
                                    .map(this::normalizeNamedType)
                                    .filter(
                                            nestedNamedStruct ->
                                                    nestedNamedStruct.getType().equals("tuple"))
                                    .forEach(
                                            nestedNamedType ->
                                                    structMap.put(
                                                            nestedNamedType.structIdentifier(),
                                                            nestedNamedType));
                        });

        return structMap.values().stream()
                .sorted(Comparator.comparingInt(AbiDefinition.NamedType::nestedness))
                .collect(Collectors.toList());
    }

    private java.util.Collection<? extends AbiDefinition.NamedType> extractNested(
            final AbiDefinition.NamedType namedType) {
        if (namedType.getComponents().size() == 0) {
            return new ArrayList<>();
        } else {
            List<AbiDefinition.NamedType> nestedStructs = new ArrayList<>();
            namedType
                    .getComponents()
                    .forEach(
                            nestedNamedStruct -> {
                                nestedStructs.add(nestedNamedStruct);
                                nestedStructs.addAll(extractNested(nestedNamedStruct));
                            });
            return nestedStructs;
        }
    }

    private Set<String> getDuplicateFunctionNames(List<AbiDefinition> functionDefinitions) {
        Set<String> duplicateNames = new HashSet<>();
        Set<String> functionNames = new HashSet<>();
        for (AbiDefinition functionDefinition : functionDefinitions) {
            if (functionDefinition.getName() != null
                    && TYPE_FUNCTION.equals(functionDefinition.getType())) {
                String functionName = funcNameToConst(functionDefinition.getName(), true);
                if (!functionNames.add(functionName)) {
                    duplicateNames.add(functionName);
                }
            }
        }
        return duplicateNames;
    }

    private static MethodSpec buildGetDeploymentBinaryMethod() {
        MethodSpec.Builder toReturn =
                MethodSpec.methodBuilder("getDeploymentBinary")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .returns(ClassName.get(String.class));

        CodeBlock codeBlock =
                CodeBlock.builder()
                        .beginControlFlow("if ($L != null)", LIBRARIES_LINKED_BINARY)
                        .addStatement("return $L", LIBRARIES_LINKED_BINARY)
                        .nextControlFlow("else")
                        .addStatement("return $L", BINARY)
                        .endControlFlow()
                        .build();

        toReturn.addCode(codeBlock);

        return toReturn.build();
    }

    List<MethodSpec> buildDeployMethods(
            String className,
            TypeSpec.Builder classBuilder,
            List<AbiDefinition> functionDefinitions)
            throws ClassNotFoundException {
        boolean constructor = false;
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (AbiDefinition functionDefinition : functionDefinitions) {
            if (functionDefinition.getType().equals(TYPE_CONSTRUCTOR)) {
                constructor = true;
                methodSpecs.add(
                        buildDeploy(
                                className,
                                functionDefinition,
                                Credentials.class,
                                CREDENTIALS,
                                true));
                methodSpecs.add(
                        buildDeploy(
                                className,
                                functionDefinition,
                                TransactionManager.class,
                                TRANSACTION_MANAGER,
                                true));
                methodSpecs.add(
                        buildDeploy(
                                className,
                                functionDefinition,
                                Credentials.class,
                                CREDENTIALS,
                                false));
                methodSpecs.add(
                        buildDeploy(
                                className,
                                functionDefinition,
                                TransactionManager.class,
                                TRANSACTION_MANAGER,
                                false));
            }
        }

        // constructor will not be specified in ABI file if its empty
        if (!constructor) {
            MethodSpec.Builder credentialsMethodBuilder =
                    getDeployMethodSpec(className, Credentials.class, CREDENTIALS, false, true);
            methodSpecs.add(
                    buildDeployNoParams(
                            credentialsMethodBuilder, className, CREDENTIALS, false, true));

            MethodSpec.Builder credentialsMethodBuilderNoGasProvider =
                    getDeployMethodSpec(className, Credentials.class, CREDENTIALS, false, false);
            methodSpecs.add(
                    buildDeployNoParams(
                            credentialsMethodBuilderNoGasProvider,
                            className,
                            CREDENTIALS,
                            false,
                            false));

            MethodSpec.Builder transactionManagerMethodBuilder =
                    getDeployMethodSpec(
                            className, TransactionManager.class, TRANSACTION_MANAGER, false, true);
            methodSpecs.add(
                    buildDeployNoParams(
                            transactionManagerMethodBuilder,
                            className,
                            TRANSACTION_MANAGER,
                            false,
                            true));

            MethodSpec.Builder transactionManagerMethodBuilderNoGasProvider =
                    getDeployMethodSpec(
                            className, TransactionManager.class, TRANSACTION_MANAGER, false, false);
            methodSpecs.add(
                    buildDeployNoParams(
                            transactionManagerMethodBuilderNoGasProvider,
                            className,
                            TRANSACTION_MANAGER,
                            false,
                            false));
        }

        return methodSpecs;
    }

    Iterable<FieldSpec> buildFuncNameConstants(List<AbiDefinition> functionDefinitions) {
        List<FieldSpec> fields = new ArrayList<>();
        Set<String> fieldNames = new HashSet<>();
        fieldNames.add(Contract.FUNC_DEPLOY);
        Set<String> duplicateFunctionNames = getDuplicateFunctionNames(functionDefinitions);
        if (!duplicateFunctionNames.isEmpty()) {
            System.out.println(
                    "\nWarning: Duplicate field(s) found: "
                            + duplicateFunctionNames
                            + ". Please don't use names which will be the same in uppercase.");
        }
        for (AbiDefinition functionDefinition : functionDefinitions) {
            if (functionDefinition.getType().equals(TYPE_FUNCTION)) {
                String funcName = functionDefinition.getName();

                if (!fieldNames.contains(funcName)) {
                    boolean useUpperCase =
                            !duplicateFunctionNames.contains(funcNameToConst(funcName, true));
                    FieldSpec field =
                            FieldSpec.builder(
                                            String.class,
                                            funcNameToConst(funcName, useUpperCase),
                                            Modifier.PUBLIC,
                                            Modifier.STATIC,
                                            Modifier.FINAL)
                                    .initializer("$S", funcName)
                                    .build();
                    fields.add(field);
                    fieldNames.add(funcName);
                }
            }
        }
        return fields;
    }

    private static MethodSpec buildConstructor(
            Class authType, String authName, boolean withGasProvider) {
        MethodSpec.Builder toReturn =
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(String.class, CONTRACT_ADDRESS)
                        .addParameter(Web3j.class, WEB3J)
                        .addParameter(authType, authName);

        if (withGasProvider) {
            toReturn.addParameter(ContractGasProvider.class, CONTRACT_GAS_PROVIDER)
                    .addStatement(
                            "super($N, $N, $N, $N, $N)",
                            BINARY,
                            CONTRACT_ADDRESS,
                            WEB3J,
                            authName,
                            CONTRACT_GAS_PROVIDER);
        } else {
            toReturn.addParameter(BigInteger.class, GAS_PRICE)
                    .addParameter(BigInteger.class, GAS_LIMIT)
                    .addStatement(
                            "super($N, $N, $N, $N, $N, $N)",
                            BINARY,
                            CONTRACT_ADDRESS,
                            WEB3J,
                            authName,
                            GAS_PRICE,
                            GAS_LIMIT)
                    .addAnnotation(Deprecated.class);
        }

        return toReturn.build();
    }

    private MethodSpec buildDeploy(
            String className,
            AbiDefinition functionDefinition,
            Class authType,
            String authName,
            boolean withGasProvider)
            throws ClassNotFoundException {

        boolean isPayable = functionDefinition.isPayable();

        MethodSpec.Builder methodBuilder =
                getDeployMethodSpec(className, authType, authName, isPayable, withGasProvider);
        String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());

        if (!inputParams.isEmpty()) {
            return buildDeployWithParams(
                    methodBuilder, className, inputParams, authName, isPayable, withGasProvider);
        } else {
            return buildDeployNoParams(
                    methodBuilder, className, authName, isPayable, withGasProvider);
        }
    }

    private static MethodSpec buildDeployWithParams(
            MethodSpec.Builder methodBuilder,
            String className,
            String inputParams,
            String authName,
            boolean isPayable,
            boolean withGasProvider) {

        methodBuilder.addStatement(
                "$T encodedConstructor = $T.encodeConstructor(" + "$T.<$T>asList($L)" + ")",
                String.class,
                FunctionEncoder.class,
                Arrays.class,
                Type.class,
                inputParams);
        if (isPayable && !withGasProvider) {
            methodBuilder.addStatement(
                    "return deployRemoteCall("
                            + "$L.class, $L, $L, $L, $L, getDeploymentBinary(), encodedConstructor, $L)",
                    className,
                    WEB3J,
                    authName,
                    GAS_PRICE,
                    GAS_LIMIT,
                    INITIAL_VALUE);
            methodBuilder.addAnnotation(Deprecated.class);
        } else if (isPayable && withGasProvider) {
            methodBuilder.addStatement(
                    "return deployRemoteCall("
                            + "$L.class, $L, $L, $L, getDeploymentBinary(), encodedConstructor, $L)",
                    className,
                    WEB3J,
                    authName,
                    CONTRACT_GAS_PROVIDER,
                    INITIAL_VALUE);
        } else if (!isPayable && !withGasProvider) {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class, $L, $L, $L, $L, getDeploymentBinary(), encodedConstructor)",
                    className,
                    WEB3J,
                    authName,
                    GAS_PRICE,
                    GAS_LIMIT);
            methodBuilder.addAnnotation(Deprecated.class);
        } else {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class, $L, $L, $L, getDeploymentBinary(), encodedConstructor)",
                    className,
                    WEB3J,
                    authName,
                    CONTRACT_GAS_PROVIDER);
        }

        return methodBuilder.build();
    }

    private static MethodSpec buildDeployNoParams(
            MethodSpec.Builder methodBuilder,
            String className,
            String authName,
            boolean isPayable,
            boolean withGasProvider) {
        if (isPayable && !withGasProvider) {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class, $L, $L, $L, $L, getDeploymentBinary(), \"\", $L)",
                    className,
                    WEB3J,
                    authName,
                    GAS_PRICE,
                    GAS_LIMIT,
                    INITIAL_VALUE);
            methodBuilder.addAnnotation(Deprecated.class);
        } else if (isPayable && withGasProvider) {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class, $L, $L, $L, getDeploymentBinary(), \"\", $L)",
                    className,
                    WEB3J,
                    authName,
                    CONTRACT_GAS_PROVIDER,
                    INITIAL_VALUE);
        } else if (!isPayable && !withGasProvider) {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class, $L, $L, $L, $L, getDeploymentBinary(), \"\")",
                    className,
                    WEB3J,
                    authName,
                    GAS_PRICE,
                    GAS_LIMIT);
            methodBuilder.addAnnotation(Deprecated.class);
        } else {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class, $L, $L, $L, getDeploymentBinary(), \"\")",
                    className,
                    WEB3J,
                    authName,
                    CONTRACT_GAS_PROVIDER);
        }

        return methodBuilder.build();
    }

    private static MethodSpec.Builder getDeployMethodSpec(
            String className,
            Class authType,
            String authName,
            boolean isPayable,
            boolean withGasProvider) {
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("deploy")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(buildRemoteCall(TypeVariableName.get(className, Type.class)))
                        .addParameter(Web3j.class, WEB3J)
                        .addParameter(authType, authName);
        if (isPayable && !withGasProvider) {
            return builder.addParameter(BigInteger.class, GAS_PRICE)
                    .addParameter(BigInteger.class, GAS_LIMIT)
                    .addParameter(BigInteger.class, INITIAL_VALUE);
        } else if (isPayable && withGasProvider) {
            return builder.addParameter(ContractGasProvider.class, CONTRACT_GAS_PROVIDER)
                    .addParameter(BigInteger.class, INITIAL_VALUE);
        } else if (!isPayable && withGasProvider) {
            return builder.addParameter(ContractGasProvider.class, CONTRACT_GAS_PROVIDER);
        } else {
            return builder.addParameter(BigInteger.class, GAS_PRICE)
                    .addParameter(BigInteger.class, GAS_LIMIT);
        }
    }

    private static MethodSpec buildLoad(
            String className, Class authType, String authName, boolean withGasProvider) {
        MethodSpec.Builder toReturn =
                MethodSpec.methodBuilder("load")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(TypeVariableName.get(className, Type.class))
                        .addParameter(String.class, CONTRACT_ADDRESS)
                        .addParameter(Web3j.class, WEB3J)
                        .addParameter(authType, authName);

        if (withGasProvider) {
            toReturn.addParameter(ContractGasProvider.class, CONTRACT_GAS_PROVIDER)
                    .addStatement(
                            "return new $L($L, $L, $L, $L)",
                            className,
                            CONTRACT_ADDRESS,
                            WEB3J,
                            authName,
                            CONTRACT_GAS_PROVIDER);
        } else {
            toReturn.addParameter(BigInteger.class, GAS_PRICE)
                    .addParameter(BigInteger.class, GAS_LIMIT)
                    .addStatement(
                            "return new $L($L, $L, $L, $L, $L)",
                            className,
                            CONTRACT_ADDRESS,
                            WEB3J,
                            authName,
                            GAS_PRICE,
                            GAS_LIMIT)
                    .addAnnotation(Deprecated.class);
        }

        return toReturn.build();
    }

    String addParameters(MethodSpec.Builder methodBuilder, List<AbiDefinition.NamedType> namedTypes)
            throws ClassNotFoundException {
        return addParameters(methodBuilder, namedTypes, this.useNativeJavaTypes);
    }

    String addParameters(
            MethodSpec.Builder methodBuilder,
            List<AbiDefinition.NamedType> namedTypes,
            boolean useNativeJavaTypes)
            throws ClassNotFoundException {

        final List<ParameterSpec> inputParameterTypes =
                buildParameterTypes(namedTypes, useJavaPrimitiveTypes);

        final List<ParameterSpec> nativeInputParameterTypes =
                new ArrayList<>(inputParameterTypes.size());

        for (int i = 0; i < inputParameterTypes.size(); ++i) {
            final TypeName typeName = getTypenameForArray(namedTypes.get(i), useJavaPrimitiveTypes);
            final String paramName =
                    !SourceVersion.isName(inputParameterTypes.get(i).name)
                            ? "_" + inputParameterTypes.get(i).name
                            : inputParameterTypes.get(i).name;
            nativeInputParameterTypes.add(ParameterSpec.builder(typeName, paramName).build());
        }

        methodBuilder.addParameters(nativeInputParameterTypes);

        if (useNativeJavaTypes) {
            return Collection.join(
                    inputParameterTypes,
                    ", \n",
                    // this results in fully qualified names being generated
                    this::createMappedParameterTypes);
        } else {
            return Collection.join(inputParameterTypes, ", ", parameterSpec -> parameterSpec.name);
        }
    }

    private TypeName getTypenameForArray(
            AbiDefinition.NamedType namedType, boolean useJavaPrimitiveTypes)
            throws ClassNotFoundException {
        final TypeName typeName;
        if (namedType.getType().equals("tuple")) {
            typeName = structClassNameMap.get(namedType.structIdentifier());
        } else if (namedType.getType().startsWith("tuple") && namedType.getType().contains("[")) {
            typeName = buildStructArrayTypeName(namedType, true);
        } else {
            typeName = getWrapperType(buildParameterType(namedType, useJavaPrimitiveTypes).type);
        }
        return typeName;
    }

    private String createMappedParameterTypes(ParameterSpec parameterSpec) {
        if (parameterSpec.type instanceof ParameterizedTypeName) {
            List<TypeName> typeNames = ((ParameterizedTypeName) parameterSpec.type).typeArguments;
            if (typeNames.size() != 1) {
                throw new UnsupportedOperationException(
                        "Only a single parameterized type is supported");
            } else if (structClassNameMap.values().stream()
                    .map(ClassName::simpleName)
                    .anyMatch(
                            name ->
                                    name.equals(
                                            ((ClassName)
                                                            ((ParameterizedTypeName)
                                                                            parameterSpec.type)
                                                                    .typeArguments.get(0))
                                                    .simpleName()))) {
                String structName =
                        structClassNameMap.values().stream()
                                .map(ClassName::simpleName)
                                .filter(
                                        name ->
                                                name.equals(
                                                        ((ClassName)
                                                                        ((ParameterizedTypeName)
                                                                                        parameterSpec
                                                                                                .type)
                                                                                .typeArguments.get(
                                                                                        0))
                                                                .simpleName()))
                                .collect(Collectors.toList())
                                .get(0);
                return "new "
                        + parameterSpec.type
                        + "("
                        + structName
                        + ".class, "
                        + parameterSpec.name
                        + ")";
            } else {
                String parameterSpecType = parameterSpec.type.toString();
                TypeName typeName = typeNames.get(0);
                String typeMapInput = typeName + ".class";
                String componentType = typeName.toString();
                if (typeName instanceof ParameterizedTypeName) {
                    List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
                    if (typeArguments.size() != 1) {
                        throw new UnsupportedOperationException(
                                "Only a single parameterized type is supported");
                    }
                    TypeName innerTypeName = typeArguments.get(0);
                    componentType = ((ParameterizedTypeName) typeName).rawType.toString();
                    parameterSpecType =
                            ((ParameterizedTypeName) parameterSpec.type).rawType
                                    + "<"
                                    + componentType
                                    + ">";
                    typeMapInput = componentType + ".class,\n" + innerTypeName + ".class";
                }
                return "new "
                        + parameterSpecType
                        + "(\n"
                        + "        "
                        + componentType
                        + ".class,\n"
                        + "        org.web3j.abi.Utils.typeMap("
                        + parameterSpec.name
                        + ", "
                        + typeMapInput
                        + "))";
            }
        } else if (structClassNameMap.values().stream()
                .map(ClassName::simpleName)
                .noneMatch(name -> name.equals(parameterSpec.type.toString()))) {
            String constructor = "new " + parameterSpec.type + "(";
            if (Address.class.getCanonicalName().equals(parameterSpec.type.toString())
                    && addressLength != Address.DEFAULT_LENGTH) {

                constructor += (addressLength * java.lang.Byte.SIZE) + ", ";
            }
            return constructor + parameterSpec.name + ")";
        } else {
            return parameterSpec.name;
        }
    }

    private TypeName getWrapperType(TypeName typeName) {
        if (useNativeJavaTypes) {
            return getNativeType(typeName);
        } else {
            return typeName;
        }
    }

    private TypeName getWrapperRawType(TypeName typeName) {
        if (useNativeJavaTypes) {
            if (typeName instanceof ParameterizedTypeName) {
                return ClassName.get(List.class);
            }
            return getNativeType(typeName);
        } else {
            return typeName;
        }
    }

    private TypeName getIndexedEventWrapperType(TypeName typeName) {
        if (useNativeJavaTypes) {
            return getEventNativeType(typeName);
        } else {
            return typeName;
        }
    }

    static TypeName getNativeType(TypeName typeName) {

        if (typeName instanceof ParameterizedTypeName) {
            return getNativeType((ParameterizedTypeName) typeName);
        }

        String simpleName = ((ClassName) typeName).simpleName();

        if (simpleName.equals(Address.class.getSimpleName())) {
            return TypeName.get(String.class);
        } else if (simpleName.startsWith("Uint")) {
            return TypeName.get(BigInteger.class);
        } else if (simpleName.equals(Utf8String.class.getSimpleName())) {
            return TypeName.get(String.class);
        } else if (simpleName.startsWith("Bytes") || simpleName.equals("DynamicBytes")) {
            return TypeName.get(byte[].class);
        } else if (simpleName.startsWith("Bool")) {
            return TypeName.get(java.lang.Boolean.class);
            // boolean cannot be a parameterized type
        } else if (simpleName.equals(Byte.class.getSimpleName())) {
            return TypeName.get(java.lang.Byte.class);
        } else if (simpleName.equals(Char.class.getSimpleName())) {
            return TypeName.get(Character.class);
        } else if (simpleName.equals(Double.class.getSimpleName())) {
            return TypeName.get(java.lang.Double.class);
        } else if (simpleName.equals(Float.class.getSimpleName())) {
            return TypeName.get(java.lang.Float.class);
        } else if (simpleName.equals(Int.class.getSimpleName())) {
            return TypeName.get(Integer.class);
        } else if (simpleName.equals(Long.class.getSimpleName())) {
            return TypeName.get(java.lang.Long.class);
        } else if (simpleName.equals(Short.class.getSimpleName())) {
            return TypeName.get(java.lang.Short.class);
        } else if (simpleName.startsWith("Int")) {
            return TypeName.get(BigInteger.class);
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported type: " + typeName + ", no native type mapping exists.");
        }
    }

    static TypeName getNativeType(ParameterizedTypeName parameterizedTypeName) {
        List<TypeName> typeNames = parameterizedTypeName.typeArguments;
        List<TypeName> nativeTypeNames = new ArrayList<>(typeNames.size());
        for (TypeName enclosedTypeName : typeNames) {
            nativeTypeNames.add(getNativeType(enclosedTypeName));
        }
        return ParameterizedTypeName.get(
                ClassName.get(List.class),
                nativeTypeNames.toArray(new TypeName[nativeTypeNames.size()]));
    }

    static TypeName getEventNativeType(TypeName typeName) {
        if (typeName instanceof ParameterizedTypeName) {
            return TypeName.get(byte[].class);
        }

        String simpleName = ((ClassName) typeName).simpleName();
        if (simpleName.equals(Utf8String.class.getSimpleName())) {
            return TypeName.get(byte[].class);
        } else {
            return getNativeType(typeName);
        }
    }

    private ParameterSpec buildParameterType(AbiDefinition.NamedType namedType, boolean primitives)
            throws ClassNotFoundException {
        List<ParameterSpec> parameterSpecs =
                buildParameterTypes(Collections.singletonList(namedType), primitives);

        if (parameterSpecs.isEmpty()) {
            throw new ClassNotFoundException();
        }

        return parameterSpecs.get(0);
    }

    private List<ParameterSpec> buildParameterTypes(
            List<AbiDefinition.NamedType> namedTypes, boolean primitives)
            throws ClassNotFoundException {

        List<ParameterSpec> result = new ArrayList<>(namedTypes.size());
        for (int i = 0; i < namedTypes.size(); i++) {
            AbiDefinition.NamedType namedType = namedTypes.get(i);

            String name = createValidParamName(namedType.getName(), i);
            String type = namedTypes.get(i).getType();

            if (type.equals("tuple")) {
                result.add(
                        ParameterSpec.builder(
                                        structClassNameMap.get(namedType.structIdentifier()), name)
                                .build());
            } else if (type.startsWith("tuple") && type.contains("[")) {
                result.add(
                        ParameterSpec.builder(buildStructArrayTypeName(namedType, primitives), name)
                                .build());
            } else {
                result.add(ParameterSpec.builder(buildTypeName(type, primitives), name).build());
            }
        }
        return result;
    }

    /**
     * Public Solidity arrays and maps require an unnamed input parameter - multiple if they require
     * a struct type.
     *
     * @param name parameter name
     * @param idx parameter index
     * @return non-empty parameter name
     */
    static String createValidParamName(String name, int idx) {
        if (name == null || name.equals("")) {
            return "param" + idx;
        } else {
            if (!SourceVersion.isName(name)) {
                name = "_" + name;
            }
            return name;
        }
    }

    private List<TypeName> buildTypeNames(
            List<AbiDefinition.NamedType> namedTypes, boolean primitives)
            throws ClassNotFoundException {

        List<TypeName> result = new ArrayList<>(namedTypes.size());
        for (AbiDefinition.NamedType namedType : namedTypes) {
            if (namedType.getType().equals("tuple")) {
                result.add(structClassNameMap.get(namedType.structIdentifier()));
            } else if (namedType.getType().startsWith("tuple")
                    && namedType.getType().contains("[")) {
                result.add(buildStructArrayTypeName(namedType, primitives));
            } else {
                result.add(buildTypeName(namedType.getType(), primitives));
            }
        }
        return result;
    }

    /**
     * Builds the array of struct type name. In case of using the Java native types, we return the
     * <code>List<struct></code> class Else, we return the Web3j generated types.
     *
     * @param namedType Array of structs namedType
     * @param useNativeJavaTypes Set to true for java native types
     * @return ParametrizedTypeName of the array of structs, eg, <code>StaticArray3<StructName>
     *     </code>
     */
    private TypeName buildStructArrayTypeName(NamedType namedType, Boolean useNativeJavaTypes) {
        String structName = resolveStructName(namedType);

        if (useNativeJavaTypes)
            return ParameterizedTypeName.get(
                    ClassName.get(List.class), ClassName.get("", structName));

        String arrayLength =
                namedType
                        .getType()
                        .substring(
                                namedType.getType().indexOf('[') + 1,
                                namedType.getType().indexOf(']'));
        if (!arrayLength.isEmpty() && Integer.parseInt(arrayLength) > 0) {
            return ParameterizedTypeName.get(
                    ClassName.get("org.web3j.abi.datatypes.generated", "StaticArray" + arrayLength),
                    ClassName.get("", structName));
        } else {
            return ParameterizedTypeName.get(
                    ClassName.get(DynamicArray.class), ClassName.get("", structName));
        }
    }

    private String resolveStructName(NamedType namedType) {
        String structName;
        if (namedType.getInternalType().isEmpty()) {
            structName =
                    structClassNameMap
                            .get(
                                    structsNamedTypeList.stream()
                                            .filter(struct -> isSameStruct(namedType, struct))
                                            .collect(Collectors.toList())
                                            .get(0)
                                            .structIdentifier())
                            .simpleName();

        } else {
            String structArray =
                    namedType
                            .getInternalType()
                            .substring(namedType.getInternalType().lastIndexOf(" ") + 1);
            structName =
                    structArray.substring(
                            structArray.lastIndexOf(".") + 1, structArray.indexOf("["));
        }
        return structName;
    }

    MethodSpec buildFunction(AbiDefinition functionDefinition) throws ClassNotFoundException {
        return buildFunction(functionDefinition, true);
    }

    MethodSpec buildFunction(AbiDefinition functionDefinition, boolean useUpperCase)
            throws ClassNotFoundException {
        return buildFunctions(functionDefinition, useUpperCase).get(0);
    }

    List<MethodSpec> buildFunctions(AbiDefinition functionDefinition)
            throws ClassNotFoundException {
        return buildFunctions(functionDefinition, true);
    }

    List<MethodSpec> buildFunctions(AbiDefinition functionDefinition, boolean useUpperCase)
            throws ClassNotFoundException {
        return buildFunctions(functionDefinition, useUpperCase, false);
    }

    List<MethodSpec> buildFunctions(
            AbiDefinition functionDefinition, boolean useUpperCase, boolean generateViceversa)
            throws ClassNotFoundException {

        List<MethodSpec> results = new ArrayList<>(2);
        String functionName = functionDefinition.getName();

        boolean pureOrView = functionDefinition.isPureOrView();

        if (generateBothCallAndSend) {
            final String funcNamePrefix;
            if (pureOrView ^ generateViceversa) {
                funcNamePrefix = "call";
            } else {
                funcNamePrefix = "send";
            }
            // Prefix function name to avoid naming collision
            functionName = funcNamePrefix + "_" + functionName;
        } else {
            // If the solidity function name is a reserved word
            // in the current java version prepend it with "_"
            if (!SourceVersion.isName(functionName)) {
                functionName = "_" + functionName;
            }
        }

        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder(functionName).addModifiers(Modifier.PUBLIC);

        final String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
        final List<TypeName> outputParameterTypes =
                buildTypeNames(functionDefinition.getOutputs(), useJavaPrimitiveTypes);

        if (pureOrView ^ generateViceversa) {
            // Avoid generating runtime exception call
            if (functionDefinition.hasOutputs()) {
                buildConstantFunction(
                        functionDefinition,
                        methodBuilder,
                        outputParameterTypes,
                        inputParams,
                        useUpperCase);

                results.add(methodBuilder.build());
            }
        } else {
            buildTransactionFunction(
                    functionDefinition,
                    methodBuilder,
                    inputParams,
                    useUpperCase,
                    generateViceversa);
            results.add(methodBuilder.build());
        }

        if (generateBothCallAndSend && !generateViceversa) {
            results.addAll(buildFunctions(functionDefinition, useUpperCase, true));
        }

        // Create function that returns the ABI encoding of the Solidity function call.
        if (abiFuncs) {
            functionName = "getABI_" + functionName;
            methodBuilder =
                    MethodSpec.methodBuilder(functionName)
                            .addModifiers(Modifier.PUBLIC)
                            .addModifiers(Modifier.STATIC);
            addParameters(methodBuilder, functionDefinition.getInputs());
            buildAbiFunction(functionDefinition, methodBuilder, inputParams, useUpperCase);
            results.add(methodBuilder.build());
        }

        return results;
    }

    MethodSpec buildLinkLibraryMethod() {
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder("linkLibraries")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(
                                ParameterizedTypeName.get(
                                        ClassName.get(List.class),
                                        ClassName.get(Contract.LinkReference.class)),
                                "references")
                        .addStatement(
                                LIBRARIES_LINKED_BINARY
                                        + " = "
                                        + "linkBinaryWithReferences("
                                        + BINARY
                                        + ", references)");

        return methodBuilder.build();
    }

    private void buildConstantFunction(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            List<TypeName> outputParameterTypes,
            String inputParams,
            boolean useUpperCase)
            throws ClassNotFoundException {

        String functionName = functionDefinition.getName();

        if (outputParameterTypes.isEmpty()) {
            methodBuilder.addStatement(
                    "throw new RuntimeException"
                            + "(\"cannot call constant function with void return type\")");
        } else if (outputParameterTypes.size() == 1) {

            TypeName typeName = outputParameterTypes.get(0);
            TypeName nativeReturnTypeName;
            if (functionDefinition.getOutputs().get(0).getType().equals("tuple")) {
                nativeReturnTypeName =
                        structClassNameMap.get(
                                functionDefinition.getOutputs().get(0).structIdentifier());
            } else if (functionDefinition.getOutputs().get(0).getType().startsWith("tuple")
                    && functionDefinition.getOutputs().get(0).getType().contains("[")) {
                nativeReturnTypeName = ClassName.get(List.class);
            } else if (useNativeJavaTypes) {
                nativeReturnTypeName = getWrapperRawType(typeName);
            } else {
                nativeReturnTypeName = getWrapperType(typeName);
            }
            methodBuilder.returns(buildRemoteFunctionCall(nativeReturnTypeName));

            methodBuilder.addStatement(
                    "final $T function = "
                            + "new $T($N, \n$T.<$T>asList($L), "
                            + "\n$T.<$T<?>>asList(new $T<$T>() {}))",
                    Function.class,
                    Function.class,
                    funcNameToConst(functionName, useUpperCase),
                    Arrays.class,
                    Type.class,
                    inputParams,
                    Arrays.class,
                    TypeReference.class,
                    TypeReference.class,
                    typeName);

            if (useNativeJavaTypes) {
                if (nativeReturnTypeName.equals(ClassName.get(List.class))) {
                    // We return list. So all the list elements should
                    // also be converted to native types
                    TypeName listType = ParameterizedTypeName.get(List.class, Type.class);

                    CodeBlock.Builder callCode = CodeBlock.builder();
                    callCode.addStatement(
                            "$T result = "
                                    + "($T) executeCallSingleValueReturn(function, $T.class)",
                            listType,
                            listType,
                            nativeReturnTypeName);
                    callCode.addStatement("return convertToNative(result)");

                    TypeSpec callableType =
                            TypeSpec.anonymousClassBuilder("")
                                    .addSuperinterface(
                                            ParameterizedTypeName.get(
                                                    ClassName.get(Callable.class),
                                                    nativeReturnTypeName))
                                    .addMethod(
                                            MethodSpec.methodBuilder("call")
                                                    .addAnnotation(Override.class)
                                                    .addAnnotation(
                                                            AnnotationSpec.builder(
                                                                            SuppressWarnings.class)
                                                                    .addMember(
                                                                            "value",
                                                                            "$S",
                                                                            "unchecked")
                                                                    .build())
                                                    .addModifiers(Modifier.PUBLIC)
                                                    .addException(Exception.class)
                                                    .returns(nativeReturnTypeName)
                                                    .addCode(callCode.build())
                                                    .build())
                                    .build();

                    methodBuilder.addStatement(
                            "return new $T(function,\n$L)",
                            buildRemoteFunctionCall(nativeReturnTypeName),
                            callableType);
                } else {
                    methodBuilder.addStatement(
                            "return executeRemoteCallSingleValueReturn(function, $T.class)",
                            nativeReturnTypeName);
                }
            } else {
                methodBuilder.addStatement("return executeRemoteCallSingleValueReturn(function)");
            }
        } else {
            final List<TypeName> returnTypes = new ArrayList<>();
            for (int i = 0; i < functionDefinition.getOutputs().size(); ++i) {
                if (functionDefinition.getOutputs().get(i).getType().equals("tuple")) {
                    returnTypes.add(
                            structClassNameMap.get(
                                    functionDefinition.getOutputs().get(i).structIdentifier()));
                } else if (functionDefinition.getOutputs().get(i).getType().startsWith("tuple")
                        && functionDefinition.getOutputs().get(i).getType().contains("[")) {
                    returnTypes.add(
                            buildStructArrayTypeName(functionDefinition.getOutputs().get(i), true));
                } else {
                    returnTypes.add(getWrapperType(outputParameterTypes.get(i)));
                }
            }

            ParameterizedTypeName parameterizedTupleType =
                    ParameterizedTypeName.get(
                            ClassName.get(
                                    "org.web3j.tuples.generated", "Tuple" + returnTypes.size()),
                            returnTypes.toArray(new TypeName[0]));

            methodBuilder.returns(buildRemoteFunctionCall(parameterizedTupleType));

            buildVariableLengthReturnFunctionConstructor(
                    methodBuilder, functionName, inputParams, outputParameterTypes, useUpperCase);

            buildTupleResultContainer(methodBuilder, parameterizedTupleType, outputParameterTypes);
        }
    }

    private static ParameterizedTypeName buildRemoteCall(TypeName typeName) {
        return ParameterizedTypeName.get(ClassName.get(RemoteCall.class), typeName);
    }

    private static ParameterizedTypeName buildRemoteFunctionCall(TypeName typeName) {
        return ParameterizedTypeName.get(ClassName.get(RemoteFunctionCall.class), typeName);
    }

    private void buildTransactionFunction(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            String inputParams,
            boolean useUpperCase,
            boolean isViceversa)
            throws ClassNotFoundException {

        if (functionDefinition.hasOutputs() && !isViceversa) {
            reporter.report(
                    String.format(
                            "Definition of the function %s returns a value but is not defined as a view function. "
                                    + "Please ensure it contains the view modifier if you want to read the return value",
                            functionDefinition.getName()));
        }

        if (functionDefinition.isPayable()) {
            methodBuilder.addParameter(BigInteger.class, WEI_VALUE);
        }

        String functionName = functionDefinition.getName();

        methodBuilder.returns(buildRemoteFunctionCall(TypeName.get(TransactionReceipt.class)));

        methodBuilder.addStatement(
                "final $T function = new $T(\n$N, \n$T.<$T>asList($L), \n$T"
                        + ".<$T<?>>emptyList())",
                Function.class,
                Function.class,
                funcNameToConst(functionName, useUpperCase),
                Arrays.class,
                Type.class,
                inputParams,
                Collections.class,
                TypeReference.class);
        if (functionDefinition.isPayable()) {
            methodBuilder.addStatement(
                    "return executeRemoteCallTransaction(function, $N)", WEI_VALUE);
        } else {
            methodBuilder.addStatement("return executeRemoteCallTransaction(function)");
        }
    }

    private void buildAbiFunction(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            String inputParams,
            boolean useUpperCase)
            throws ClassNotFoundException {

        if (functionDefinition.isPayable()) {
            methodBuilder.addParameter(BigInteger.class, WEI_VALUE);
        }

        String functionName = functionDefinition.getName();

        methodBuilder.returns(TypeName.get(String.class));

        methodBuilder.addStatement(
                "final $T function = new $T(\n$N, \n$T.<$T>asList($L), \n$T"
                        + ".<$T<?>>emptyList())",
                Function.class,
                Function.class,
                funcNameToConst(functionName, useUpperCase),
                Arrays.class,
                Type.class,
                inputParams,
                Collections.class,
                TypeReference.class);
        methodBuilder.addStatement("return org.web3j.abi.FunctionEncoder.encode(function)");
    }

    TypeSpec buildEventResponseObject(
            String className,
            List<org.web3j.codegen.SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<org.web3j.codegen.SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters) {

        TypeSpec.Builder builder =
                TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        builder.superclass(BaseEventResponse.class);
        for (org.web3j.codegen.SolidityFunctionWrapper.NamedTypeName namedType :
                indexedParameters) {
            final TypeName typeName;
            if (namedType.getType().equals("tuple")) {
                typeName = structClassNameMap.get(namedType.structIdentifier());
            } else if (namedType.getType().startsWith("tuple")
                    && namedType.getType().contains("[")) {
                typeName = buildStructArrayTypeName(namedType.namedType, true);
            } else {
                typeName = getIndexedEventWrapperType(namedType.typeName);
            }
            String name =
                    createValidParamName(namedType.getName(), indexedParameters.indexOf(namedType));
            builder.addField(typeName, name, Modifier.PUBLIC);
        }

        for (org.web3j.codegen.SolidityFunctionWrapper.NamedTypeName namedType :
                nonIndexedParameters) {
            final TypeName typeName;
            if (namedType.getType().equals("tuple")) {
                typeName = structClassNameMap.get(namedType.structIdentifier());
            } else if (namedType.getType().startsWith("tuple")
                    && namedType.getType().contains("[")) {
                typeName = buildStructArrayTypeName(namedType.namedType, true);
            } else {
                typeName = getWrapperType(namedType.typeName);
            }
            String name =
                    createValidParamName(
                            namedType.getName(),
                            (nonIndexedParameters.indexOf(namedType) + indexedParameters.size()));
            builder.addField(typeName, name, Modifier.PUBLIC);
        }

        return builder.build();
    }

    MethodSpec buildEventFlowableFunction(
            String responseClassName,
            String functionName,
            List<org.web3j.codegen.SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<org.web3j.codegen.SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters)
            throws ClassNotFoundException {

        String generatedFunctionName = Strings.lowercaseFirstLetter(functionName) + "EventFlowable";
        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(
                        ClassName.get(Flowable.class), ClassName.get("", responseClassName));

        return MethodSpec.methodBuilder(generatedFunctionName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(EthFilter.class, FILTER)
                .returns(parameterizedTypeName)
                .addStatement(
                        "return web3j.ethLogFlowable(filter).map(log -> "
                                + getEventFromLogFunctionName(functionName)
                                + "(log))")
                .build();
    }

    MethodSpec buildDefaultEventFlowableFunction(String responseClassName, String functionName) {

        String generatedFunctionName = Strings.lowercaseFirstLetter(functionName) + "EventFlowable";
        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(
                        ClassName.get(Flowable.class), ClassName.get("", responseClassName));

        MethodSpec.Builder flowableMethodBuilder =
                MethodSpec.methodBuilder(generatedFunctionName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(DefaultBlockParameter.class, START_BLOCK)
                        .addParameter(DefaultBlockParameter.class, END_BLOCK)
                        .returns(parameterizedTypeName);

        flowableMethodBuilder
                .addStatement(
                        "$1T filter = new $1T($2L, $3L, " + "getContractAddress())",
                        EthFilter.class,
                        START_BLOCK,
                        END_BLOCK)
                .addStatement(
                        "filter.addSingleTopic($T.encode("
                                + buildEventDefinitionName(functionName)
                                + "))",
                        EventEncoder.class)
                .addStatement("return " + generatedFunctionName + "(filter)");

        return flowableMethodBuilder.build();
    }

    MethodSpec buildEventTransactionReceiptFunction(
            String responseClassName,
            String functionName,
            List<NamedTypeName> indexedParameters,
            List<NamedTypeName> nonIndexedParameters) {

        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(
                        ClassName.get(List.class), ClassName.get("", responseClassName));

        String generatedFunctionName =
                "get" + Strings.capitaliseFirstLetter(functionName) + "Events";
        MethodSpec.Builder transactionMethodBuilder =
                MethodSpec.methodBuilder(generatedFunctionName)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(TransactionReceipt.class, "transactionReceipt")
                        .returns(parameterizedTypeName);

        transactionMethodBuilder
                .addStatement(
                        "$T valueList = staticExtractEventParametersWithLog("
                                + buildEventDefinitionName(functionName)
                                + ", "
                                + "transactionReceipt)",
                        ParameterizedTypeName.get(List.class, Contract.EventValuesWithLog.class))
                .addStatement(
                        "$1T responses = new $1T(valueList.size())",
                        ParameterizedTypeName.get(
                                ClassName.get(ArrayList.class),
                                ClassName.get("", responseClassName)))
                .beginControlFlow(
                        "for ($T eventValues : valueList)", Contract.EventValuesWithLog.class)
                .addStatement("$1T typedResponse = new $1T()", ClassName.get("", responseClassName))
                .addCode(
                        buildTypedResponse(
                                "typedResponse", indexedParameters, nonIndexedParameters, false))
                .addStatement("responses.add(typedResponse)")
                .endControlFlow();

        transactionMethodBuilder.addStatement("return responses");
        return transactionMethodBuilder.build();
    }

    MethodSpec buildEventLogFunction(
            String responseClassName,
            String functionName,
            List<NamedTypeName> indexedParameters,
            List<NamedTypeName> nonIndexedParameters) {

        String generatedFunctionName = getEventFromLogFunctionName(functionName);
        return MethodSpec.methodBuilder(generatedFunctionName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Log.class, "log")
                .returns(ClassName.get("", responseClassName))
                .addStatement(
                        "$T eventValues = staticExtractEventParametersWithLog("
                                + buildEventDefinitionName(functionName)
                                + ", log)",
                        Contract.EventValuesWithLog.class)
                .addStatement("$1T typedResponse = new $1T()", ClassName.get("", responseClassName))
                .addCode(
                        buildTypedResponse(
                                "typedResponse", indexedParameters, nonIndexedParameters, true))
                .addStatement("return typedResponse")
                .build();
    }

    private static String getEventFromLogFunctionName(String functionName) {
        return "get" + Strings.capitaliseFirstLetter(functionName) + "EventFromLog";
    }

    List<MethodSpec> buildEventFunctions(
            AbiDefinition functionDefinition,
            TypeSpec.Builder classBuilder,
            Map<String, Integer> eventsCount)
            throws ClassNotFoundException {
        List<AbiDefinition.NamedType> inputs = functionDefinition.getInputs();

        List<NamedTypeName> parameters = new ArrayList<>();
        List<NamedTypeName> indexedParameters = new ArrayList<>();
        List<NamedTypeName> nonIndexedParameters = new ArrayList<>();

        for (AbiDefinition.NamedType namedType : inputs) {
            final TypeName typeName;
            if (namedType.getType().equals("tuple")) {
                typeName = structClassNameMap.get(namedType.structIdentifier());
            } else if (namedType.getType().startsWith("tuple")
                    && namedType.getType().contains("[")) {
                typeName = buildStructArrayTypeName(namedType, false);
            } else {
                typeName = buildTypeName(namedType.getType(), useJavaPrimitiveTypes);
            }
            NamedTypeName parameter = new NamedTypeName(namedType, typeName);
            if (namedType.isIndexed()) {
                indexedParameters.add(parameter);
            } else {
                nonIndexedParameters.add(parameter);
            }
            parameters.add(parameter);
        }

        String functionName = functionDefinition.getName();

        classBuilder.addField(
                createEventDefinition(functionName, parameters, eventsCount, functionDefinition));

        functionName = functionDefinition.getName();

        String responseClassName = Strings.capitaliseFirstLetter(functionName) + "EventResponse";

        classBuilder.addType(
                buildEventResponseObject(
                        responseClassName, indexedParameters, nonIndexedParameters));

        List<MethodSpec> methods = new ArrayList<>();
        methods.add(
                buildEventTransactionReceiptFunction(
                        responseClassName, functionName, indexedParameters, nonIndexedParameters));
        methods.add(
                buildEventLogFunction(
                        responseClassName, functionName, indexedParameters, nonIndexedParameters));
        methods.add(
                buildEventFlowableFunction(
                        responseClassName, functionName, indexedParameters, nonIndexedParameters));
        methods.add(buildDefaultEventFlowableFunction(responseClassName, functionName));
        return methods;
    }

    CodeBlock buildTypedResponse(
            String objectName,
            List<org.web3j.codegen.SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<org.web3j.codegen.SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters,
            boolean flowable) {
        CodeBlock.Builder builder = CodeBlock.builder();
        if (flowable) {
            builder.addStatement("$L.log = log", objectName);
        } else {
            builder.addStatement("$L.log = eventValues.getLog()", objectName);
        }
        for (int i = 0; i < indexedParameters.size(); i++) {
            final NamedTypeName namedTypeName = indexedParameters.get(i);
            final String nativeConversion;
            boolean needsArrayCast = false;
            if (useNativeJavaTypes
                    && structClassNameMap.values().stream()
                            .map(ClassName::simpleName)
                            .noneMatch(
                                    name -> name.equals(namedTypeName.getTypeName().toString()))) {
                if (namedTypeName.typeName instanceof ParameterizedTypeName
                        && isNotArrayOfStructs(namedTypeName)) {
                    nativeConversion = ".getNativeValueCopy()";
                    needsArrayCast = true;
                } else {
                    nativeConversion = ".getValue()";
                }
            } else {
                nativeConversion = "";
            }
            final TypeName indexedEventWrapperType;
            if (namedTypeName.getType().equals("tuple")) {
                indexedEventWrapperType = structClassNameMap.get(namedTypeName.structIdentifier());
            } else if (namedTypeName.getType().startsWith("tuple")
                    && namedTypeName.getType().contains("[")) {
                indexedEventWrapperType = buildStructArrayTypeName(namedTypeName.namedType, true);
            } else {
                indexedEventWrapperType = getIndexedEventWrapperType(namedTypeName.getTypeName());
            }
            if (needsArrayCast) {
                builder.addStatement(
                        "$L.$L = ($T) (($T) eventValues.getIndexedValues().get($L))"
                                + nativeConversion,
                        objectName,
                        createValidParamName(namedTypeName.getName(), i),
                        indexedEventWrapperType,
                        Array.class,
                        i);
            } else {
                builder.addStatement(
                        "$L.$L = ($T) eventValues.getIndexedValues().get($L)" + nativeConversion,
                        objectName,
                        createValidParamName(namedTypeName.getName(), i),
                        indexedEventWrapperType,
                        i);
            }
        }

        for (int i = 0; i < nonIndexedParameters.size(); i++) {
            final NamedTypeName namedTypeName = nonIndexedParameters.get(i);
            final String nativeConversion;
            boolean needsArrayCast = false;
            if (useNativeJavaTypes
                    && structClassNameMap.values().stream()
                            .map(ClassName::simpleName)
                            .noneMatch(
                                    name -> name.equals(namedTypeName.getTypeName().toString()))) {
                if (namedTypeName.typeName instanceof ParameterizedTypeName
                        && isNotArrayOfStructs(namedTypeName)) {
                    nativeConversion = ".getNativeValueCopy()";
                    needsArrayCast = true;
                } else {
                    nativeConversion = ".getValue()";
                }
            } else {
                nativeConversion = "";
            }
            final TypeName nonIndexedEventWrapperType;
            if (nonIndexedParameters.get(i).getType().equals("tuple")) {
                nonIndexedEventWrapperType =
                        structClassNameMap.get(namedTypeName.structIdentifier());
            } else if (nonIndexedParameters.get(i).getType().startsWith("tuple")
                    && nonIndexedParameters.get(i).getType().contains("[")) {
                nonIndexedEventWrapperType =
                        buildStructArrayTypeName(namedTypeName.namedType, true);
            } else {
                nonIndexedEventWrapperType =
                        getWrapperType(nonIndexedParameters.get(i).getTypeName());
            }
            if (needsArrayCast) {
                builder.addStatement(
                        "$L.$L = ($T) (($T) eventValues.getNonIndexedValues().get($L))"
                                + nativeConversion,
                        objectName,
                        createValidParamName(namedTypeName.getName(), i + indexedParameters.size()),
                        nonIndexedEventWrapperType,
                        Array.class,
                        i);
            } else {
                builder.addStatement(
                        "$L.$L = ($T) eventValues.getNonIndexedValues().get($L)" + nativeConversion,
                        objectName,
                        createValidParamName(namedTypeName.getName(), i + indexedParameters.size()),
                        nonIndexedEventWrapperType,
                        i);
            }
        }
        return builder.build();
    }

    private boolean isNotArrayOfStructs(NamedTypeName namedTypeName) {
        return structClassNameMap.values().stream()
                .map(ClassName::simpleName)
                .noneMatch(
                        name ->
                                name.equals(
                                        ((ClassName)
                                                        ((ParameterizedTypeName)
                                                                        namedTypeName.typeName)
                                                                .typeArguments.get(0))
                                                .simpleName()));
    }

    static TypeName buildTypeName(String typeDeclaration) throws ClassNotFoundException {
        return buildTypeName(typeDeclaration, false);
    }

    static TypeName buildTypeName(String typeDeclaration, boolean primitives)
            throws ClassNotFoundException {

        final String solidityType = trimStorageDeclaration(typeDeclaration);

        final TypeReference typeReference =
                TypeReference.makeTypeReference(solidityType, false, primitives);

        return TypeName.get(typeReference.getType());
    }

    private static Class<?> getStaticArrayTypeReferenceClass(String type) {
        try {
            return Class.forName("org.web3j.abi.datatypes.generated.StaticArray" + type);
        } catch (ClassNotFoundException e) {
            // Unfortunately we can't encode it's length as a type if it's > 32.
            return StaticArray.class;
        }
    }

    private static String trimStorageDeclaration(String type) {
        if (type.endsWith(" storage") || type.endsWith(" memory")) {
            return type.split(" ")[0];
        } else {
            return type;
        }
    }

    private static void buildVariableLengthReturnFunctionConstructor(
            MethodSpec.Builder methodBuilder,
            String functionName,
            String inputParameters,
            List<TypeName> outputParameterTypes,
            boolean useUpperCase)
            throws ClassNotFoundException {

        List<Object> objects = new ArrayList<>();
        objects.add(Function.class);
        objects.add(Function.class);
        objects.add(funcNameToConst(functionName, useUpperCase));

        objects.add(Arrays.class);
        objects.add(Type.class);
        objects.add(inputParameters);

        objects.add(Arrays.class);
        objects.add(TypeReference.class);
        for (TypeName outputParameterType : outputParameterTypes) {
            objects.add(TypeReference.class);
            objects.add(outputParameterType);
        }

        String asListParams =
                Collection.join(outputParameterTypes, ", ", typeName -> "new $T<$T>() {}");

        methodBuilder.addStatement(
                "final $T function = new $T($N, \n$T.<$T>asList($L), \n$T"
                        + ".<$T<?>>asList("
                        + asListParams
                        + "))",
                objects.toArray());
    }

    private void buildTupleResultContainer(
            MethodSpec.Builder methodBuilder,
            ParameterizedTypeName tupleType,
            List<TypeName> outputParameterTypes)
            throws ClassNotFoundException {

        List<TypeName> typeArguments = tupleType.typeArguments;

        CodeBlock.Builder tupleConstructor = CodeBlock.builder();
        tupleConstructor
                .addStatement(
                        "$T results = executeCallMultipleValueReturn(function)",
                        ParameterizedTypeName.get(List.class, Type.class))
                .add("return new $T(", tupleType)
                .add("$>$>");

        String resultStringNativeList = "\nconvertToNative(($T) results.get($L).getValue())";

        int size = typeArguments.size();
        ClassName classList = ClassName.get(List.class);

        for (int i = 0; i < size; i++) {
            TypeName param = outputParameterTypes.get(i);
            TypeName convertTo = typeArguments.get(i);

            String resultStringSimple = "\n($T) results.get($L)";
            final TypeName finalConvertTo = convertTo;
            if (useNativeJavaTypes
                    && structClassNameMap.values().stream()
                            .map(ClassName::simpleName)
                            .noneMatch(name -> name.equals(finalConvertTo.toString()))) {
                resultStringSimple += ".getValue()";
            }

            String resultString = resultStringSimple;

            // If we use native java types we need to convert
            // elements of arrays to native java types too
            if (useNativeJavaTypes && param instanceof ParameterizedTypeName) {
                ParameterizedTypeName oldContainer = (ParameterizedTypeName) param;
                ParameterizedTypeName newContainer = (ParameterizedTypeName) convertTo;
                if (newContainer.rawType.compareTo(classList) == 0
                        && newContainer.typeArguments.size() == 1) {
                    convertTo =
                            ParameterizedTypeName.get(classList, oldContainer.typeArguments.get(0));
                    resultString = resultStringNativeList;
                }
            }

            tupleConstructor.add(resultString, convertTo, i);
            tupleConstructor.add(i < size - 1 ? ", " : ");\n");
        }
        tupleConstructor.add("$<$<");

        TypeSpec callableType =
                TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(
                                ParameterizedTypeName.get(ClassName.get(Callable.class), tupleType))
                        .addMethod(
                                MethodSpec.methodBuilder("call")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addException(Exception.class)
                                        .returns(tupleType)
                                        .addCode(tupleConstructor.build())
                                        .build())
                        .build();

        methodBuilder.addStatement(
                "return new $T(function,\n$L)", buildRemoteFunctionCall(tupleType), callableType);
    }

    private static CodeBlock buildVariableLengthEventInitializer(
            String eventName, List<NamedTypeName> parameterTypes) {

        List<Object> objects = new ArrayList<>();
        objects.add(Event.class);
        objects.add(eventName);

        objects.add(Arrays.class);
        objects.add(TypeReference.class);
        for (NamedTypeName parameterType : parameterTypes) {
            objects.add(TypeReference.class);
            objects.add(parameterType.getTypeName());
        }

        String asListParams =
                parameterTypes.stream()
                        .map(
                                type -> {
                                    if (type.isIndexed()) {
                                        return "new $T<$T>(true) {}";
                                    } else {
                                        return "new $T<$T>() {}";
                                    }
                                })
                        .collect(Collectors.joining(", "));

        return CodeBlock.builder()
                .addStatement(
                        "new $T($S, \n" + "$T.<$T<?>>asList(" + asListParams + "))",
                        objects.toArray())
                .build();
    }

    private List<AbiDefinition> loadContractDefinition(String abi) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(abi, AbiDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    private static String funcNameToConst(String funcName, boolean useUpperCase) {
        if (useUpperCase) {
            return FUNC_NAME_PREFIX + funcName.toUpperCase();
        } else {
            return FUNC_NAME_PREFIX + funcName;
        }
    }

    private static class NamedTypeName {
        private final TypeName typeName;
        private final AbiDefinition.NamedType namedType;

        NamedTypeName(AbiDefinition.NamedType namedType, TypeName typeName) {
            this.namedType = namedType;
            this.typeName = typeName;
        }

        public String getName() {
            return namedType.getName();
        }

        public String getType() {
            return namedType.getType();
        }

        public TypeName getTypeName() {
            return typeName;
        }

        public boolean isIndexed() {
            return namedType.isIndexed();
        }

        public String structIdentifier() {
            return namedType.structIdentifier();
        }
    }
}
