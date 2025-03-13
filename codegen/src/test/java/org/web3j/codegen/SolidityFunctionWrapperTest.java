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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.web3j.TempFileProvider;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.StaticArray10;
import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.StaticArray3;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.AbiDefinition.NamedType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.web3j.codegen.SolidityFunctionWrapper.buildTypeName;
import static org.web3j.codegen.SolidityFunctionWrapper.createValidParamName;
import static org.web3j.codegen.SolidityFunctionWrapper.getEventNativeType;
import static org.web3j.codegen.SolidityFunctionWrapper.getNativeType;

public class SolidityFunctionWrapperTest extends TempFileProvider {

    private SolidityFunctionWrapper solidityFunctionWrapper;
    private SolidityFunctionWrapper solidityFunctionWrapperBoth;

    private GenerationReporter generationReporter;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        generationReporter = mock(GenerationReporter.class);
        solidityFunctionWrapper =
                new SolidityFunctionWrapper(
                        true, false, false, Address.DEFAULT_LENGTH, generationReporter);
        solidityFunctionWrapperBoth =
                new SolidityFunctionWrapper(
                        true, false, true, Address.DEFAULT_LENGTH, generationReporter);
    }

    @Test
    public void testCreateValidParamName() {
        assertEquals(createValidParamName("param", 1), ("param"));
        assertEquals(createValidParamName("", 1), ("param1"));
        assertEquals(createValidParamName("class", 1), ("_class"));
    }

    @Test
    public void testBuildTypeName() throws Exception {
        assertEquals(buildTypeName("uint256"), (ClassName.get(Uint256.class)));
        assertEquals(buildTypeName("uint64"), (ClassName.get(Uint64.class)));
        assertEquals(buildTypeName("string"), (ClassName.get(Utf8String.class)));

        assertEquals(
                buildTypeName("uint256[]"),
                (ParameterizedTypeName.get(DynamicArray.class, Uint256.class)));

        assertEquals(
                buildTypeName("uint256[] storage"),
                (ParameterizedTypeName.get(DynamicArray.class, Uint256.class)));

        assertEquals(
                buildTypeName("uint256[] memory"),
                (ParameterizedTypeName.get(DynamicArray.class, Uint256.class)));

        assertEquals(
                buildTypeName("uint256[10]"),
                (ParameterizedTypeName.get(StaticArray10.class, Uint256.class)));

        assertEquals(
                buildTypeName("uint256[33]"),
                (ParameterizedTypeName.get(StaticArray.class, Uint256.class)));

        assertEquals(
                buildTypeName("uint256[10][3]"),
                (ParameterizedTypeName.get(
                        ClassName.get(StaticArray3.class),
                        ParameterizedTypeName.get(StaticArray10.class, Uint256.class))));

        assertEquals(
                buildTypeName("uint256[2][]"),
                (ParameterizedTypeName.get(
                        ClassName.get(DynamicArray.class),
                        ParameterizedTypeName.get(StaticArray2.class, Uint256.class))));

        assertEquals(
                buildTypeName("uint256[33][]"),
                (ParameterizedTypeName.get(
                        ClassName.get(DynamicArray.class),
                        ParameterizedTypeName.get(StaticArray.class, Uint256.class))));

        assertEquals(
                buildTypeName("uint256[][]"),
                (ParameterizedTypeName.get(
                        ClassName.get(DynamicArray.class),
                        ParameterizedTypeName.get(DynamicArray.class, Uint256.class))));
    }

    @Test
    public void testGetNativeType() {
        assertEquals(getNativeType(TypeName.get(Address.class)), (TypeName.get(String.class)));
        assertEquals(getNativeType(TypeName.get(Uint256.class)), (TypeName.get(BigInteger.class)));
        assertEquals(getNativeType(TypeName.get(Int256.class)), (TypeName.get(BigInteger.class)));
        assertEquals(getNativeType(TypeName.get(Utf8String.class)), (TypeName.get(String.class)));
        assertEquals(getNativeType(TypeName.get(Bool.class)), (TypeName.get(Boolean.class)));
        assertEquals(getNativeType(TypeName.get(Bytes32.class)), (TypeName.get(byte[].class)));
        assertEquals(getNativeType(TypeName.get(DynamicBytes.class)), (TypeName.get(byte[].class)));
    }

    @Test
    public void testGetNativeTypeParameterized() {
        assertEquals(
                getNativeType(
                        ParameterizedTypeName.get(
                                ClassName.get(DynamicArray.class), TypeName.get(Address.class))),
                (ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(String.class))));
    }

    @Test
    public void testGetNativeTypeInvalid() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> getNativeType(TypeName.get(BigInteger.class)));
    }

    @Test
    public void testGetEventNativeType() {
        assertEquals(
                getEventNativeType(TypeName.get(Utf8String.class)), (TypeName.get(byte[].class)));
    }

    @Test
    public void testGetEventNativeTypeParameterized() {
        assertEquals(
                getEventNativeType(
                        ParameterizedTypeName.get(
                                ClassName.get(DynamicArray.class), TypeName.get(Address.class))),
                (TypeName.get(byte[].class)));
    }

    @Test
    public void testBuildFunctionTransaction() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(new NamedType("param", "uint8")),
                        "functionName",
                        Collections.emptyList(),
                        "type",
                        false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        String expected =
                "public org.web3j.protocol.core.RemoteFunctionCall<org.web3j.protocol.core.methods.response.TransactionReceipt> functionName(\n"
                        + "    java.math.BigInteger param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(\n"
                        + "      FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Collections.<org.web3j.abi.TypeReference<?>>emptyList());\n"
                        + "  return executeRemoteCallTransaction(function);\n"
                        + "}\n";

        assertEquals(methodSpec.toString(), (expected));
    }

    @Test
    public void testBuildingFunctionTransactionThatReturnsValueReportsWarning() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(new NamedType("param", "uint8")),
                        "functionName",
                        Arrays.asList(new NamedType("result", "uint8")),
                        "type",
                        false);

        solidityFunctionWrapper.buildFunction(functionDefinition);

        verify(generationReporter)
                .report(
                        "Definition of the function functionName returns a value but is not defined as a view function. "
                                + "Please ensure it contains the view modifier if you want to read the return value");
    }

    @Test
    public void testBuildPayableFunctionTransaction() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(new NamedType("param", "uint8")),
                        "functionName",
                        Collections.emptyList(),
                        "type",
                        true);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        String expected =
                "public org.web3j.protocol.core.RemoteFunctionCall<org.web3j.protocol.core.methods.response.TransactionReceipt> functionName(\n"
                        + "    java.math.BigInteger param, java.math.BigInteger weiValue) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(\n"
                        + "      FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Collections.<org.web3j.abi.TypeReference<?>>emptyList());\n"
                        + "  return executeRemoteCallTransaction(function, weiValue);\n"
                        + "}\n";

        assertEquals(methodSpec.toString(), (expected));
    }

    @Test
    public void testBuildFunctionConstantSingleValueReturn() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        true,
                        Arrays.asList(new NamedType("param", "uint8")),
                        "functionName",
                        Arrays.asList(new NamedType("result", "int8")),
                        "type",
                        false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        String expected =
                "public org.web3j.protocol.core.RemoteFunctionCall<java.math.BigInteger> functionName(\n"
                        + "    java.math.BigInteger param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Int8>() {}));\n"
                        + "  return executeRemoteCallSingleValueReturn(function, java.math.BigInteger.class);\n"
                        + "}\n";

        assertEquals((expected), methodSpec.toString());
    }

    @Test
    public void testBuildFunctionConstantSingleValueRawListReturn() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        true,
                        Arrays.asList(new NamedType("param", "uint8")),
                        "functionName",
                        Arrays.asList(new NamedType("result", "address[]")),
                        "type",
                        false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        String expected =
                "public org.web3j.protocol.core.RemoteFunctionCall<java.util.List> functionName(\n"
                        + "    java.math.BigInteger param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>>() {}));\n"
                        + "  return new org.web3j.protocol.core.RemoteFunctionCall<java.util.List>(function,\n"
                        + "      new java.util.concurrent.Callable<java.util.List>() {\n"
                        + "        @java.lang.Override\n"
                        + "        @java.lang.SuppressWarnings(\"unchecked\")\n"
                        + "        public java.util.List call() throws java.lang.Exception {\n"
                        + "          java.util.List<org.web3j.abi.datatypes.Type> result = (java.util.List<org.web3j.abi.datatypes.Type>) executeCallSingleValueReturn(function, java.util.List.class);\n"
                        + "          return convertToNative(result);\n"
                        + "        }\n"
                        + "      });\n"
                        + "}\n";

        assertEquals((expected), methodSpec.toString());
    }

    @Test
    public void testBuildFunctionConstantDynamicArrayRawListReturn() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        true,
                        Arrays.asList(new NamedType("param", "uint8[]")),
                        "functionName",
                        Arrays.asList(new NamedType("result", "address[]")),
                        "type",
                        false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        String expected =
                "public org.web3j.protocol.core.RemoteFunctionCall<java.util.List> functionName(\n"
                        + "    java.util.List<java.math.BigInteger> param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint8>(\n"
                        + "              org.web3j.abi.datatypes.generated.Uint8.class,\n"
                        + "              org.web3j.abi.Utils.typeMap(param, org.web3j.abi.datatypes.generated.Uint8.class))), \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>>() {}));\n"
                        + "  return new org.web3j.protocol.core.RemoteFunctionCall<java.util.List>(function,\n"
                        + "      new java.util.concurrent.Callable<java.util.List>() {\n"
                        + "        @java.lang.Override\n"
                        + "        @java.lang.SuppressWarnings(\"unchecked\")\n"
                        + "        public java.util.List call() throws java.lang.Exception {\n"
                        + "          java.util.List<org.web3j.abi.datatypes.Type> result = (java.util.List<org.web3j.abi.datatypes.Type>) executeCallSingleValueReturn(function, java.util.List.class);\n"
                        + "          return convertToNative(result);\n"
                        + "        }\n"
                        + "      });\n"
                        + "}\n";

        assertEquals((expected), methodSpec.toString());
    }

    @Test
    public void testBuildFunctionConstantMultiDynamicArrayRawListReturn() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        true,
                        Arrays.asList(new NamedType("param", "uint8[][]")),
                        "functionName",
                        Arrays.asList(new NamedType("result", "address[]")),
                        "type",
                        false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        String expected =
                "public org.web3j.protocol.core.RemoteFunctionCall<java.util.List> functionName(\n"
                        + "    java.util.List<java.util.List<java.math.BigInteger>> param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.DynamicArray>(\n"
                        + "              org.web3j.abi.datatypes.DynamicArray.class,\n"
                        + "              org.web3j.abi.Utils.typeMap(param, org.web3j.abi.datatypes.DynamicArray.class,\n"
                        + "      org.web3j.abi.datatypes.generated.Uint8.class))), \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>>() {}));\n"
                        + "  return new org.web3j.protocol.core.RemoteFunctionCall<java.util.List>(function,\n"
                        + "      new java.util.concurrent.Callable<java.util.List>() {\n"
                        + "        @java.lang.Override\n"
                        + "        @java.lang.SuppressWarnings(\"unchecked\")\n"
                        + "        public java.util.List call() throws java.lang.Exception {\n"
                        + "          java.util.List<org.web3j.abi.datatypes.Type> result = (java.util.List<org.web3j.abi.datatypes.Type>) executeCallSingleValueReturn(function, java.util.List.class);\n"
                        + "          return convertToNative(result);\n"
                        + "        }\n"
                        + "      });\n"
                        + "}\n";

        assertEquals((expected), methodSpec.toString());
    }

    @Test
    public void testBuildFunctionStructArrayParameterAndReturn() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        true,
                        Arrays.asList(
                                new NamedType(
                                        "a",
                                        "tuple[3]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "nuu",
                                                        "tuple",
                                                        Arrays.asList(
                                                                new NamedType(
                                                                        "foo",
                                                                        "tuple",
                                                                        Arrays.asList(
                                                                                new NamedType(
                                                                                        "id",
                                                                                        "string",
                                                                                        null,
                                                                                        "string",
                                                                                        false),
                                                                                new NamedType(
                                                                                        "name",
                                                                                        "string",
                                                                                        null,
                                                                                        "string",
                                                                                        false)),
                                                                        "struct ComplexStorage.Foo",
                                                                        false)),
                                                        "struct ComplexStorage.Nuu",
                                                        false)),
                                        "struct ComplexStorage.Nar[3]",
                                        false),
                                new NamedType(
                                        "b",
                                        "tuple[3]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "id", "uint256", null, "uint256", false),
                                                new NamedType(
                                                        "data", "uint256", null, "uint256", false)),
                                        "struct ComplexStorage.Bar[3]",
                                        false),
                                new NamedType(
                                        "c",
                                        "tuple[]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "id", "string", null, "string", false),
                                                new NamedType(
                                                        "name", "string", null, "string", false)),
                                        "struct ComplexStorage.Foo[]",
                                        false),
                                new NamedType(
                                        "d",
                                        "tuple[]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "nuu",
                                                        "tuple",
                                                        Arrays.asList(
                                                                new NamedType(
                                                                        "foo",
                                                                        "tuple",
                                                                        Arrays.asList(
                                                                                new NamedType(
                                                                                        "id",
                                                                                        "string",
                                                                                        null,
                                                                                        "string",
                                                                                        false),
                                                                                new NamedType(
                                                                                        "name",
                                                                                        "string",
                                                                                        null,
                                                                                        "string",
                                                                                        false)),
                                                                        "struct ComplexStorage.Foo",
                                                                        false)),
                                                        "struct ComplexStorage.Nuu",
                                                        false)),
                                        "struct ComplexStorage.Nar[]",
                                        false),
                                new NamedType(
                                        "e",
                                        "tuple[3]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "id", "string", null, "string", false),
                                                new NamedType(
                                                        "name", "string", null, "string", false)),
                                        "struct ComplexStorage.Foo[3]",
                                        false)),
                        "idNarBarFooArrays",
                        Arrays.asList(
                                new NamedType(
                                        "",
                                        "tuple[3]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "nuu",
                                                        "tuple",
                                                        Arrays.asList(
                                                                new NamedType(
                                                                        "foo",
                                                                        "tuple",
                                                                        Arrays.asList(
                                                                                new NamedType(
                                                                                        "id",
                                                                                        "string",
                                                                                        null,
                                                                                        "string",
                                                                                        false),
                                                                                new NamedType(
                                                                                        "name",
                                                                                        "string",
                                                                                        null,
                                                                                        "string",
                                                                                        false)),
                                                                        "struct ComplexStorage.Foo",
                                                                        false)),
                                                        "struct ComplexStorage.Nuu",
                                                        false)),
                                        "struct ComplexStorage.Nar[3]",
                                        false),
                                new NamedType(
                                        "",
                                        "tuple[3]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "id", "uint256", null, "uint256", false),
                                                new NamedType(
                                                        "data", "uint256", null, "uint256", false)),
                                        "struct ComplexStorage.Bar[3]",
                                        false),
                                new NamedType(
                                        "",
                                        "tuple[]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "id", "string", null, "string", false),
                                                new NamedType(
                                                        "name", "string", null, "string", false)),
                                        "struct ComplexStorage.Foo[]",
                                        false),
                                new NamedType(
                                        "",
                                        "tuple[]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "nuu",
                                                        "tuple",
                                                        Arrays.asList(
                                                                new NamedType(
                                                                        "foo",
                                                                        "tuple",
                                                                        Arrays.asList(
                                                                                new NamedType(
                                                                                        "id",
                                                                                        "string",
                                                                                        null,
                                                                                        "string",
                                                                                        false),
                                                                                new NamedType(
                                                                                        "name",
                                                                                        "string",
                                                                                        null,
                                                                                        "string",
                                                                                        false)),
                                                                        "struct ComplexStorage.Foo",
                                                                        false)),
                                                        "struct ComplexStorage.Nuu",
                                                        false)),
                                        "struct ComplexStorage.Nar[]",
                                        false),
                                new NamedType(
                                        "",
                                        "tuple[3]",
                                        Arrays.asList(
                                                new NamedType(
                                                        "id", "string", null, "string", false),
                                                new NamedType(
                                                        "name", "string", null, "string", false)),
                                        "struct ComplexStorage.Foo[3]",
                                        false)),
                        "function",
                        false,
                        "pure");

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        String expected =
                "public org.web3j.protocol.core.RemoteFunctionCall<org.web3j.tuples.generated.Tuple5<java.util.List<Nar>, java.util.List<Bar>, java.util.List<Foo>, java.util.List<Nar>, java.util.List<Foo>>> idNarBarFooArrays(\n"
                        + "    java.util.List<Nar> a, java.util.List<Bar> b, java.util.List<Foo> c, java.util.List<Nar> d,\n"
                        + "    java.util.List<Foo> e) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_IDNARBARFOOARRAYS, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.StaticArray3<Nar>(\n"
                        + "              Nar.class,\n"
                        + "              org.web3j.abi.Utils.typeMap(a, Nar.class)), \n"
                        + "      new org.web3j.abi.datatypes.generated.StaticArray3<Bar>(\n"
                        + "              Bar.class,\n"
                        + "              org.web3j.abi.Utils.typeMap(b, Bar.class)), \n"
                        + "      new org.web3j.abi.datatypes.DynamicArray<Foo>(\n"
                        + "              Foo.class,\n"
                        + "              org.web3j.abi.Utils.typeMap(c, Foo.class)), \n"
                        + "      new org.web3j.abi.datatypes.DynamicArray<Nar>(\n"
                        + "              Nar.class,\n"
                        + "              org.web3j.abi.Utils.typeMap(d, Nar.class)), \n"
                        + "      new org.web3j.abi.datatypes.generated.StaticArray3<Foo>(\n"
                        + "              Foo.class,\n"
                        + "              org.web3j.abi.Utils.typeMap(e, Foo.class))), \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.StaticArray3<Nar>>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.StaticArray3<Bar>>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.DynamicArray<Foo>>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.DynamicArray<Nar>>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.StaticArray3<Foo>>() {}));\n"
                        + "  return new org.web3j.protocol.core.RemoteFunctionCall<org.web3j.tuples.generated.Tuple5<java.util.List<Nar>, java.util.List<Bar>, java.util.List<Foo>, java.util.List<Nar>, java.util.List<Foo>>>(function,\n"
                        + "      new java.util.concurrent.Callable<org.web3j.tuples.generated.Tuple5<java.util.List<Nar>, java.util.List<Bar>, java.util.List<Foo>, java.util.List<Nar>, java.util.List<Foo>>>() {\n"
                        + "        @java.lang.Override\n"
                        + "        public org.web3j.tuples.generated.Tuple5<java.util.List<Nar>, java.util.List<Bar>, java.util.List<Foo>, java.util.List<Nar>, java.util.List<Foo>> call(\n"
                        + "            ) throws java.lang.Exception {\n"
                        + "          java.util.List<org.web3j.abi.datatypes.Type> results = executeCallMultipleValueReturn(function);\n"
                        + "          return new org.web3j.tuples.generated.Tuple5<java.util.List<Nar>, java.util.List<Bar>, java.util.List<Foo>, java.util.List<Nar>, java.util.List<Foo>>(\n"
                        + "              convertToNative((java.util.List<Nar>) results.get(0).getValue()), \n"
                        + "              convertToNative((java.util.List<Bar>) results.get(1).getValue()), \n"
                        + "              convertToNative((java.util.List<Foo>) results.get(2).getValue()), \n"
                        + "              convertToNative((java.util.List<Nar>) results.get(3).getValue()), \n"
                        + "              convertToNative((java.util.List<Foo>) results.get(4).getValue()));\n"
                        + "        }\n"
                        + "      });\n"
                        + "}\n";

        assertEquals(expected, methodSpec.toString());
    }

    @Test
    public void testBuildFunctionConstantInvalid() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        true,
                        Collections.singletonList(new NamedType("param", "uint8")),
                        "functionName",
                        Collections.emptyList(),
                        "type",
                        false);

        List<MethodSpec> methodSpecs = solidityFunctionWrapper.buildFunctions(functionDefinition);
        assertTrue(methodSpecs.isEmpty());
    }

    @Test
    public void testBuildFunctionConstantMultipleValueReturn() throws Exception {

        AbiDefinition functionDefinition =
                new AbiDefinition(
                        true,
                        Arrays.asList(
                                new NamedType("param1", "uint8"),
                                new NamedType("param2", "uint32")),
                        "functionName",
                        Arrays.asList(
                                new NamedType("result1", "int8"),
                                new NamedType("result2", "int32")),
                        "type",
                        false);

        MethodSpec methodSpec = solidityFunctionWrapper.buildFunction(functionDefinition);

        String expected =
                "public org.web3j.protocol.core.RemoteFunctionCall<org.web3j.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>> functionName(\n"
                        + "    java.math.BigInteger param1, java.math.BigInteger param2) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param1), \n"
                        + "      new org.web3j.abi.datatypes.generated.Uint32(param2)), \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Int8>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Int32>() {}));\n"
                        + "  return new org.web3j.protocol.core.RemoteFunctionCall<org.web3j.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>>(function,\n"
                        + "      new java.util.concurrent.Callable<org.web3j.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>>() {\n"
                        + "        @java.lang.Override\n"
                        + "        public org.web3j.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger> call()\n"
                        + "            throws java.lang.Exception {\n"
                        + "          java.util.List<org.web3j.abi.datatypes.Type> results = executeCallMultipleValueReturn(function);\n"
                        + "          return new org.web3j.tuples.generated.Tuple2<java.math.BigInteger, java.math.BigInteger>(\n"
                        + "              (java.math.BigInteger) results.get(0).getValue(), \n"
                        + "              (java.math.BigInteger) results.get(1).getValue());\n"
                        + "        }\n"
                        + "      });\n"
                        + "}\n";

        assertEquals((expected), methodSpec.toString());
    }

    @Test
    public void testBuildEventConstantMultipleValueReturn() throws Exception {

        NamedType id = new NamedType("id", "string", true);
        NamedType fromAddress = new NamedType("from", "address");
        NamedType toAddress = new NamedType("to", "address");
        NamedType value = new NamedType("value", "uint256");
        NamedType message = new NamedType("message", "string");
        fromAddress.setIndexed(true);
        toAddress.setIndexed(true);

        AbiDefinition functionDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(id, fromAddress, toAddress, value, message),
                        "Transfer",
                        new ArrayList<>(),
                        "event",
                        false);
        TypeSpec.Builder builder = TypeSpec.classBuilder("testClass");

        builder.addMethods(
                solidityFunctionWrapper.buildEventFunctions(
                        functionDefinition,
                        builder,
                        solidityFunctionWrapper.getDuplicatedEventNames(
                                Collections.singletonList(functionDefinition))));

        String expected =
                "class testClass {\n"
                        + "  public static final org.web3j.abi.datatypes.Event TRANSFER_EVENT = new org.web3j.abi.datatypes.Event(\"Transfer\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Utf8String>(true) {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Address>(true) {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Address>(true) {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Utf8String>() {}));\n  ;\n\n"
                        + "  public static java.util.List<TransferEventResponse> getTransferEvents(\n"
                        + "      org.web3j.protocol.core.methods.response.TransactionReceipt transactionReceipt) {\n"
                        + "    java.util.List<org.web3j.tx.Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);\n"
                        + "    java.util.ArrayList<TransferEventResponse> responses = new java.util.ArrayList<TransferEventResponse>(valueList.size());\n"
                        + "    for (org.web3j.tx.Contract.EventValuesWithLog eventValues : valueList) {\n"
                        + "      TransferEventResponse typedResponse = new TransferEventResponse();\n"
                        + "      typedResponse.log = eventValues.getLog();\n"
                        + "      typedResponse.id = (byte[]) eventValues.getIndexedValues().get(0).getValue();\n"
                        + "      typedResponse.from = (java.lang.String) eventValues.getIndexedValues().get(1).getValue();\n"
                        + "      typedResponse.to = (java.lang.String) eventValues.getIndexedValues().get(2).getValue();\n"
                        + "      typedResponse.value = (java.math.BigInteger) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "      typedResponse.message = (java.lang.String) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "      responses.add(typedResponse);\n"
                        + "    }\n"
                        + "    return responses;\n"
                        + "  }\n"
                        + "\n"
                        + "  public static TransferEventResponse getTransferEventFromLog(\n"
                        + "      org.web3j.protocol.core.methods.response.Log log) {\n"
                        + "    org.web3j.tx.Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);\n"
                        + "    TransferEventResponse typedResponse = new TransferEventResponse();\n"
                        + "    typedResponse.log = log;\n"
                        + "    typedResponse.id = (byte[]) eventValues.getIndexedValues().get(0).getValue();\n"
                        + "    typedResponse.from = (java.lang.String) eventValues.getIndexedValues().get(1).getValue();\n"
                        + "    typedResponse.to = (java.lang.String) eventValues.getIndexedValues().get(2).getValue();\n"
                        + "    typedResponse.value = (java.math.BigInteger) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "    typedResponse.message = (java.lang.String) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "    return typedResponse;\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(\n"
                        + "      org.web3j.protocol.core.methods.request.EthFilter filter) {\n"
                        + "    return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter startBlock,\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter endBlock) {\n"
                        + "    org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, getContractAddress());\n"
                        + "    filter.addSingleTopic(org.web3j.abi.EventEncoder.encode(TRANSFER_EVENT));\n"
                        + "    return transferEventFlowable(filter);\n"
                        + "  }\n"
                        + "\n"
                        + "  public static class TransferEventResponse extends org.web3j.protocol.core.methods.response.BaseEventResponse {\n"
                        + "    public byte[] id;\n"
                        + "\n"
                        + "    public java.lang.String from;\n"
                        + "\n"
                        + "    public java.lang.String to;\n"
                        + "\n"
                        + "    public java.math.BigInteger value;\n"
                        + "\n"
                        + "    public java.lang.String message;\n"
                        + "  }\n"
                        + "}\n";

        assertEquals(expected, builder.build().toString());
    }

    @Test
    public void testBuildEventWithNamedAndNoNamedParameters() throws Exception {

        NamedType id = new NamedType("id", "string", true);
        NamedType fromAddress = new NamedType("", "address");
        NamedType toAddress = new NamedType("to", "address");
        NamedType value = new NamedType("", "uint256");
        NamedType message = new NamedType("message", "string");
        NamedType bytesVal = new NamedType("", "bytes10");
        fromAddress.setIndexed(true);
        toAddress.setIndexed(true);

        AbiDefinition functionDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(id, fromAddress, toAddress, bytesVal, value, message),
                        "Transfer",
                        new ArrayList<>(),
                        "event",
                        false);
        TypeSpec.Builder builder = TypeSpec.classBuilder("testClass");

        builder.addMethods(
                solidityFunctionWrapper.buildEventFunctions(
                        functionDefinition,
                        builder,
                        solidityFunctionWrapper.getDuplicatedEventNames(
                                Collections.singletonList(functionDefinition))));

        String expected =
                "class testClass {\n"
                        + "  public static final org.web3j.abi.datatypes.Event TRANSFER_EVENT = new org.web3j.abi.datatypes.Event(\"Transfer\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Utf8String>(true) {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Address>(true) {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Address>(true) {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Bytes10>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Utf8String>() {}));\n  ;\n\n"
                        + "  public static java.util.List<TransferEventResponse> getTransferEvents(\n"
                        + "      org.web3j.protocol.core.methods.response.TransactionReceipt transactionReceipt) {\n"
                        + "    java.util.List<org.web3j.tx.Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);\n"
                        + "    java.util.ArrayList<TransferEventResponse> responses = new java.util.ArrayList<TransferEventResponse>(valueList.size());\n"
                        + "    for (org.web3j.tx.Contract.EventValuesWithLog eventValues : valueList) {\n"
                        + "      TransferEventResponse typedResponse = new TransferEventResponse();\n"
                        + "      typedResponse.log = eventValues.getLog();\n"
                        + "      typedResponse.id = (byte[]) eventValues.getIndexedValues().get(0).getValue();\n"
                        + "      typedResponse.param1 = (java.lang.String) eventValues.getIndexedValues().get(1).getValue();\n"
                        + "      typedResponse.to = (java.lang.String) eventValues.getIndexedValues().get(2).getValue();\n"
                        + "      typedResponse.param3 = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "      typedResponse.param4 = (java.math.BigInteger) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "      typedResponse.message = (java.lang.String) eventValues.getNonIndexedValues().get(2).getValue();\n"
                        + "      responses.add(typedResponse);\n"
                        + "    }\n"
                        + "    return responses;\n"
                        + "  }\n"
                        + "\n"
                        + "  public static TransferEventResponse getTransferEventFromLog(\n"
                        + "      org.web3j.protocol.core.methods.response.Log log) {\n"
                        + "    org.web3j.tx.Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);\n"
                        + "    TransferEventResponse typedResponse = new TransferEventResponse();\n"
                        + "    typedResponse.log = log;\n"
                        + "    typedResponse.id = (byte[]) eventValues.getIndexedValues().get(0).getValue();\n"
                        + "    typedResponse.param1 = (java.lang.String) eventValues.getIndexedValues().get(1).getValue();\n"
                        + "    typedResponse.to = (java.lang.String) eventValues.getIndexedValues().get(2).getValue();\n"
                        + "    typedResponse.param3 = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "    typedResponse.param4 = (java.math.BigInteger) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "    typedResponse.message = (java.lang.String) eventValues.getNonIndexedValues().get(2).getValue();\n"
                        + "    return typedResponse;\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(\n"
                        + "      org.web3j.protocol.core.methods.request.EthFilter filter) {\n"
                        + "    return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter startBlock,\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter endBlock) {\n"
                        + "    org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, getContractAddress());\n"
                        + "    filter.addSingleTopic(org.web3j.abi.EventEncoder.encode(TRANSFER_EVENT));\n"
                        + "    return transferEventFlowable(filter);\n"
                        + "  }\n"
                        + "\n"
                        + "  public static class TransferEventResponse extends org.web3j.protocol.core.methods.response.BaseEventResponse {\n"
                        + "    public byte[] id;\n"
                        + "\n"
                        + "    public java.lang.String param1;\n"
                        + "\n"
                        + "    public java.lang.String to;\n"
                        + "\n"
                        + "    public byte[] param3;\n"
                        + "\n"
                        + "    public java.math.BigInteger param4;\n"
                        + "\n"
                        + "    public java.lang.String message;\n"
                        + "  }\n"
                        + "}\n";

        assertEquals((expected), builder.build().toString());
    }

    @Test
    public void testBuildEventWithNativeList() throws Exception {

        NamedType array = new NamedType("array", "uint256[]");

        AbiDefinition functionDefinition =
                new AbiDefinition(
                        false, Arrays.asList(array), "Transfer", new ArrayList<>(), "event", false);
        TypeSpec.Builder builder = TypeSpec.classBuilder("testClass");

        builder.addMethods(
                solidityFunctionWrapper.buildEventFunctions(
                        functionDefinition,
                        builder,
                        solidityFunctionWrapper.getDuplicatedEventNames(
                                Collections.singletonList(functionDefinition))));

        String expected =
                "class testClass {\n"
                        + "  public static final org.web3j.abi.datatypes.Event TRANSFER_EVENT = new org.web3j.abi.datatypes.Event(\"Transfer\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>>() {}));\n  ;\n\n"
                        + "  public static java.util.List<TransferEventResponse> getTransferEvents(\n"
                        + "      org.web3j.protocol.core.methods.response.TransactionReceipt transactionReceipt) {\n"
                        + "    java.util.List<org.web3j.tx.Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);\n"
                        + "    java.util.ArrayList<TransferEventResponse> responses = new java.util.ArrayList<TransferEventResponse>(valueList.size());\n"
                        + "    for (org.web3j.tx.Contract.EventValuesWithLog eventValues : valueList) {\n"
                        + "      TransferEventResponse typedResponse = new TransferEventResponse();\n"
                        + "      typedResponse.log = eventValues.getLog();\n"
                        + "      typedResponse.array = (java.util.List<java.math.BigInteger>) ((org.web3j.abi.datatypes.Array) eventValues.getNonIndexedValues().get(0)).getNativeValueCopy();\n"
                        + "      responses.add(typedResponse);\n"
                        + "    }\n"
                        + "    return responses;\n"
                        + "  }\n"
                        + "\n"
                        + "  public static TransferEventResponse getTransferEventFromLog(\n"
                        + "      org.web3j.protocol.core.methods.response.Log log) {\n"
                        + "    org.web3j.tx.Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);\n"
                        + "    TransferEventResponse typedResponse = new TransferEventResponse();\n"
                        + "    typedResponse.log = log;\n"
                        + "    typedResponse.array = (java.util.List<java.math.BigInteger>) ((org.web3j.abi.datatypes.Array) eventValues.getNonIndexedValues().get(0)).getNativeValueCopy();\n"
                        + "    return typedResponse;\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(\n"
                        + "      org.web3j.protocol.core.methods.request.EthFilter filter) {\n"
                        + "    return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter startBlock,\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter endBlock) {\n"
                        + "    org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, getContractAddress());\n"
                        + "    filter.addSingleTopic(org.web3j.abi.EventEncoder.encode(TRANSFER_EVENT));\n"
                        + "    return transferEventFlowable(filter);\n"
                        + "  }\n"
                        + "\n"
                        + "  public static class TransferEventResponse extends org.web3j.protocol.core.methods.response.BaseEventResponse {\n"
                        + "    public java.util.List<java.math.BigInteger> array;\n"
                        + "  }\n"
                        + "}\n";

        assertEquals((expected), builder.build().toString());
    }

    @Test
    public void testBuildFuncNameConstants() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(new NamedType("param", "uint8")),
                        "functionName",
                        Collections.emptyList(),
                        "function",
                        true);
        TypeSpec.Builder builder = TypeSpec.classBuilder("testClass");

        builder.addFields(
                solidityFunctionWrapper.buildFuncNameConstants(
                        Collections.singletonList(functionDefinition)));

        String expected =
                "class testClass {\n"
                        + "  public static final java.lang.String FUNC_FUNCTIONNAME = \"functionName\";\n"
                        + "}\n";

        assertEquals(builder.build().toString(), (expected));
    }

    @Test
    public void testBuildFunctionDuplicatedEventNames() throws Exception {
        AbiDefinition firstEventDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(
                                new NamedType("action", "string", false),
                                new NamedType("pauseState", "bool", false)),
                        "eventName",
                        Collections.emptyList(),
                        "event",
                        false);
        AbiDefinition secondEventDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(
                                new NamedType("cToken", "address", false),
                                new NamedType("action", "string", false),
                                new NamedType("pauseState", "bool", false)),
                        "eventName",
                        Collections.emptyList(),
                        "event",
                        false);
        TypeSpec.Builder builder = TypeSpec.classBuilder("testClass");
        builder.addMethods(
                solidityFunctionWrapper.buildFunctionDefinitions(
                        "testClass",
                        builder,
                        Arrays.asList(firstEventDefinition, secondEventDefinition)));

        String expected =
                "class testClass {\n"
                        + "  public static final org.web3j.abi.datatypes.Event EVENTNAME1_EVENT = new org.web3j.abi.datatypes.Event(\"eventName\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Utf8String>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Bool>() {}));\n"
                        + "  ;\n"
                        + "\n"
                        + "  public static final org.web3j.abi.datatypes.Event EVENTNAME_EVENT = new org.web3j.abi.datatypes.Event(\"eventName\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Address>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Utf8String>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Bool>() {}));\n"
                        + "  ;\n"
                        + "\n"
                        + "  public static java.util.List<EventName1EventResponse> getEventName1Events(\n"
                        + "      org.web3j.protocol.core.methods.response.TransactionReceipt transactionReceipt) {\n"
                        + "    java.util.List<org.web3j.tx.Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(EVENTNAME1_EVENT, transactionReceipt);\n"
                        + "    java.util.ArrayList<EventName1EventResponse> responses = new java.util.ArrayList<EventName1EventResponse>(valueList.size());\n"
                        + "    for (org.web3j.tx.Contract.EventValuesWithLog eventValues : valueList) {\n"
                        + "      EventName1EventResponse typedResponse = new EventName1EventResponse();\n"
                        + "      typedResponse.log = eventValues.getLog();\n"
                        + "      typedResponse.action = (java.lang.String) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "      typedResponse.pauseState = (java.lang.Boolean) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "      responses.add(typedResponse);\n"
                        + "    }\n"
                        + "    return responses;\n"
                        + "  }\n"
                        + "\n"
                        + "  public static EventName1EventResponse getEventName1EventFromLog(\n"
                        + "      org.web3j.protocol.core.methods.response.Log log) {\n"
                        + "    org.web3j.tx.Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(EVENTNAME1_EVENT, log);\n"
                        + "    EventName1EventResponse typedResponse = new EventName1EventResponse();\n"
                        + "    typedResponse.log = log;\n"
                        + "    typedResponse.action = (java.lang.String) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "    typedResponse.pauseState = (java.lang.Boolean) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "    return typedResponse;\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<EventName1EventResponse> eventName1EventFlowable(\n"
                        + "      org.web3j.protocol.core.methods.request.EthFilter filter) {\n"
                        + "    return web3j.ethLogFlowable(filter).map(log -> getEventName1EventFromLog(log));\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<EventName1EventResponse> eventName1EventFlowable(\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter startBlock,\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter endBlock) {\n"
                        + "    org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, getContractAddress());\n"
                        + "    filter.addSingleTopic(org.web3j.abi.EventEncoder.encode(EVENTNAME1_EVENT));\n"
                        + "    return eventName1EventFlowable(filter);\n"
                        + "  }\n"
                        + "\n"
                        + "  public static java.util.List<EventNameEventResponse> getEventNameEvents(\n"
                        + "      org.web3j.protocol.core.methods.response.TransactionReceipt transactionReceipt) {\n"
                        + "    java.util.List<org.web3j.tx.Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(EVENTNAME_EVENT, transactionReceipt);\n"
                        + "    java.util.ArrayList<EventNameEventResponse> responses = new java.util.ArrayList<EventNameEventResponse>(valueList.size());\n"
                        + "    for (org.web3j.tx.Contract.EventValuesWithLog eventValues : valueList) {\n"
                        + "      EventNameEventResponse typedResponse = new EventNameEventResponse();\n"
                        + "      typedResponse.log = eventValues.getLog();\n"
                        + "      typedResponse.cToken = (java.lang.String) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "      typedResponse.action = (java.lang.String) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "      typedResponse.pauseState = (java.lang.Boolean) eventValues.getNonIndexedValues().get(2).getValue();\n"
                        + "      responses.add(typedResponse);\n"
                        + "    }\n"
                        + "    return responses;\n"
                        + "  }\n"
                        + "\n"
                        + "  public static EventNameEventResponse getEventNameEventFromLog(\n"
                        + "      org.web3j.protocol.core.methods.response.Log log) {\n"
                        + "    org.web3j.tx.Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(EVENTNAME_EVENT, log);\n"
                        + "    EventNameEventResponse typedResponse = new EventNameEventResponse();\n"
                        + "    typedResponse.log = log;\n"
                        + "    typedResponse.cToken = (java.lang.String) eventValues.getNonIndexedValues().get(0).getValue();\n"
                        + "    typedResponse.action = (java.lang.String) eventValues.getNonIndexedValues().get(1).getValue();\n"
                        + "    typedResponse.pauseState = (java.lang.Boolean) eventValues.getNonIndexedValues().get(2).getValue();\n"
                        + "    return typedResponse;\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<EventNameEventResponse> eventNameEventFlowable(\n"
                        + "      org.web3j.protocol.core.methods.request.EthFilter filter) {\n"
                        + "    return web3j.ethLogFlowable(filter).map(log -> getEventNameEventFromLog(log));\n"
                        + "  }\n"
                        + "\n"
                        + "  public io.reactivex.Flowable<EventNameEventResponse> eventNameEventFlowable(\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter startBlock,\n"
                        + "      org.web3j.protocol.core.DefaultBlockParameter endBlock) {\n"
                        + "    org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, getContractAddress());\n"
                        + "    filter.addSingleTopic(org.web3j.abi.EventEncoder.encode(EVENTNAME_EVENT));\n"
                        + "    return eventNameEventFlowable(filter);\n"
                        + "  }\n"
                        + "\n"
                        + "  public static class EventName1EventResponse extends org.web3j.protocol.core.methods.response.BaseEventResponse {\n"
                        + "    public java.lang.String action;\n"
                        + "\n"
                        + "    public java.lang.Boolean pauseState;\n"
                        + "  }\n"
                        + "\n"
                        + "  public static class EventNameEventResponse extends org.web3j.protocol.core.methods.response.BaseEventResponse {\n"
                        + "    public java.lang.String cToken;\n"
                        + "\n"
                        + "    public java.lang.String action;\n"
                        + "\n"
                        + "    public java.lang.Boolean pauseState;\n"
                        + "  }\n"
                        + "}\n";

        assertEquals(builder.build().toString(), (expected));
    }

    @Test
    public void testBuildFunctionTransactionAndCall() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        false,
                        Arrays.asList(new NamedType("param", "uint8")),
                        "functionName",
                        Arrays.asList(new NamedType("result", "int8")),
                        "type",
                        false);

        List<MethodSpec> methodSpecs =
                solidityFunctionWrapperBoth.buildFunctions(functionDefinition);

        String expectedSend =
                "public org.web3j.protocol.core.RemoteFunctionCall<org.web3j.protocol.core.methods.response.TransactionReceipt> send_functionName(\n"
                        + "    java.math.BigInteger param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(\n"
                        + "      FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Collections.<org.web3j.abi.TypeReference<?>>emptyList());\n"
                        + "  return executeRemoteCallTransaction(function);\n"
                        + "}\n";

        String expectedCall =
                "public org.web3j.protocol.core.RemoteFunctionCall<java.math.BigInteger> call_functionName(\n"
                        + "    java.math.BigInteger param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Int8>() {}));\n"
                        + "  return executeRemoteCallSingleValueReturn(function, java.math.BigInteger.class);\n"
                        + "}\n";

        assertEquals(2, methodSpecs.size());
        assertEquals(expectedSend, methodSpecs.get(0).toString());
        assertEquals(expectedCall, methodSpecs.get(1).toString());
    }

    @Test
    public void testBuildFunctionConstantSingleValueReturnAndTransaction() throws Exception {
        AbiDefinition functionDefinition =
                new AbiDefinition(
                        true,
                        Arrays.asList(new NamedType("param", "uint8")),
                        "functionName",
                        Arrays.asList(new NamedType("result", "int8")),
                        "type",
                        false);

        List<MethodSpec> methodSpecs =
                solidityFunctionWrapperBoth.buildFunctions(functionDefinition);

        String expectedCall =
                "public org.web3j.protocol.core.RemoteFunctionCall<java.math.BigInteger> call_functionName(\n"
                        + "    java.math.BigInteger param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Int8>() {}));\n"
                        + "  return executeRemoteCallSingleValueReturn(function, java.math.BigInteger.class);\n"
                        + "}\n";

        String expectedSend =
                "public org.web3j.protocol.core.RemoteFunctionCall<org.web3j.protocol.core.methods.response.TransactionReceipt> send_functionName(\n"
                        + "    java.math.BigInteger param) {\n"
                        + "  final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(\n"
                        + "      FUNC_FUNCTIONNAME, \n"
                        + "      java.util.Arrays.<org.web3j.abi.datatypes.Type>asList(new org.web3j.abi.datatypes.generated.Uint8(param)), \n"
                        + "      java.util.Collections.<org.web3j.abi.TypeReference<?>>emptyList());\n"
                        + "  return executeRemoteCallTransaction(function);\n"
                        + "}\n";

        assertEquals(2, methodSpecs.size());
        assertEquals(expectedCall, methodSpecs.get(0).toString());
        assertEquals(expectedSend, methodSpecs.get(1).toString());
    }

    @Test
    public void testBuildFunctionLinkBinaryWithReferences() throws Exception {
        MethodSpec methodSpec = solidityFunctionWrapper.buildLinkLibraryMethod();

        String expected =
                "public static void linkLibraries(java.util.List<org.web3j.tx.Contract.LinkReference> references) {\n"
                        + "  librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);\n"
                        + "}\n";

        assertEquals(methodSpec.toString(), (expected));
    }

    @Test
    public void testBinaryWithUnlinkedLibraryLengthOver65534() throws Exception {
        solidityFunctionWrapper.createBinaryDefinition(
                "0x"
                        + "a".repeat(40000)
                        + "__$927c5a12e2f339676f56d42ec1c0537964$__"
                        + "a".repeat(40000));
    }

    @Test
    public void testBuildCustomErrorDefinitionsWithEmptyNameField() throws Exception {
        List<AbiDefinition> abiDefinitions =
                List.of(
                        new AbiDefinition(
                                false,
                                List.of(new NamedType("reason", "string")),
                                "Error",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                List.of(new NamedType("account", "address")),
                                "",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                Collections.emptyList(),
                                "",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                Collections.emptyList(),
                                "Empty",
                                Collections.emptyList(),
                                "error",
                                false));
        String expectedJavaCode =
                "class MyContract {\n"
                        + "  public static final org.web3j.abi.datatypes.CustomError ERROR_ERROR = new org.web3j.abi.datatypes.CustomError(\"Error\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Utf8String>() {}));\n"
                        + "  ;\n\n"
                        + "  public static final org.web3j.abi.datatypes.CustomError EMPTY_ERROR = new org.web3j.abi.datatypes.CustomError(\"Empty\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList());\n"
                        + "  ;\n}\n";

        TypeSpec.Builder builder = TypeSpec.classBuilder("MyContract");
        solidityFunctionWrapper.buildFunctionDefinitions("MyContract", builder, abiDefinitions);

        assertEquals(expectedJavaCode, builder.build().toString());
    }

    @Test
    public void testBuildCustomErrorDefinitionsWithDuplicateNames() throws Exception {
        List<AbiDefinition> abiDefinitions =
                List.of(
                        new AbiDefinition(
                                false,
                                List.of(new NamedType("account", "address")),
                                "invalidAccess",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                List.of(new NamedType("account", "address")),
                                "InvalidAccesS",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                Arrays.asList(
                                        new NamedType("account", "address"),
                                        new NamedType("reason", "string")),
                                "InvalidAccess",
                                Collections.emptyList(),
                                "error",
                                false));
        String expectedJavaCode =
                "class MyContract {\n"
                        + "  public static final org.web3j.abi.datatypes.CustomError INVALIDACCESS2_ERROR = new org.web3j.abi.datatypes.CustomError(\"invalidAccess\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Address>() {}));\n"
                        + "  ;\n\n"
                        + "  public static final org.web3j.abi.datatypes.CustomError INVALIDACCESS1_ERROR = new org.web3j.abi.datatypes.CustomError(\"InvalidAccesS\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Address>() {}));\n"
                        + "  ;\n\n"
                        + "  public static final org.web3j.abi.datatypes.CustomError INVALIDACCESS_ERROR = new org.web3j.abi.datatypes.CustomError(\"InvalidAccess\", \n"
                        + "      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Address>() {}, new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Utf8String>() {}));\n"
                        + "  ;\n}\n";

        TypeSpec.Builder builder = TypeSpec.classBuilder("MyContract");
        solidityFunctionWrapper.buildFunctionDefinitions("MyContract", builder, abiDefinitions);

        assertEquals(expectedJavaCode, builder.build().toString());
    }

    @Test
    public void testCreateCustomErrorList() {
        List<AbiDefinition> abiDefinitions =
                List.of(
                        new AbiDefinition(
                                false,
                                List.of(new NamedType("reason", "string")),
                                "Error",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                Collections.emptyList(),
                                "",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                Collections.emptyList(),
                                "Empty",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                List.of(new NamedType("account", "address")),
                                "invalidAccess",
                                Collections.emptyList(),
                                "error",
                                false),
                        new AbiDefinition(
                                false,
                                Arrays.asList(
                                        new NamedType("account", "address"),
                                        new NamedType("reason", "string")),
                                "InvalidAccess",
                                Collections.emptyList(),
                                "error",
                                false));
        String expectedJavaCode =
                "class MyContract {\n"
                        + "  public static final java.util.List CUSTOM_ERRORS = java.util.Arrays.<org.web3j.abi.datatypes.CustomError>asList(\n"
                        + "      ERROR_ERROR,\n"
                        + "      EMPTY_ERROR,\n"
                        + "      INVALIDACCESS1_ERROR,\n"
                        + "      INVALIDACCESS_ERROR);\n"
                        + "  ;\n}\n";

        TypeSpec.Builder builder = TypeSpec.classBuilder("MyContract");
        solidityFunctionWrapper.createCustomErrorList(builder, abiDefinitions);

        assertEquals(expectedJavaCode, builder.build().toString());
    }

    @Test
    public void testCreateCustomErrorListWithoutCustomErrors() {
        List<AbiDefinition> abiDefinitions = List.of();
        String expectedJavaCode = "class MyContract {\n}\n";

        TypeSpec.Builder builder = TypeSpec.classBuilder("MyContract");
        solidityFunctionWrapper.createCustomErrorList(builder, abiDefinitions);

        assertEquals(expectedJavaCode, builder.build().toString());
    }
}
