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
import java.util.Arrays;
import java.util.List;

import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.PropertySpec;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.ParameterizedTypeName;
import com.squareup.kotlinpoet.TypeSpec;
import com.squareup.kotlinpoet.TypeVariableName;
import com.squareup.kotlinpoet.KModifier;


import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;

/** Generator class for creating all the different numeric type variants. */
public class AbiTypesGenerator extends Generator {

//    private static final String CODEGEN_WARNING = buildWarning(AbiTypesGenerator.class);

    private static final String DEFAULT = "DEFAULT";

    public static void main(String[] args) throws Exception {
        AbiTypesGenerator abiTypesGenerator = new AbiTypesGenerator();
        if (args.length == 1) {
            abiTypesGenerator.generate(args[0]);
        } else {
            abiTypesGenerator.generate(System.getProperty("user.dir") + "/abi/src/main/java/");
        }
    }

    private void generate(String destinationDir) throws IOException {
        generateIntTypes(Int.class, destinationDir);
        generateIntTypes(Uint.class, destinationDir);

        // TODO: Enable once Solidity supports fixed types - see
        // https://github.com/ethereum/solidity/issues/409
        // generateFixedTypes(Fixed.class, destinationDir);
        // generateFixedTypes(Ufixed.class, destinationDir);

        generateBytesTypes(destinationDir);
        generateStaticArrayTypes(destinationDir);
    }

    private <T extends Type> void generateIntTypes(Class<T> superclass, String path)
            throws IOException {
        String packageName = createPackageName(superclass);
        ClassName className;

        for (int bitSize = 8; bitSize <= Type.MAX_BIT_LENGTH; bitSize += 8) {
            className = new ClassName(packageName, superclass.getSimpleName() + bitSize);

            FunSpec constructorSpec =
                    FunSpec.constructorBuilder()
                            .addModifiers(KModifier.PUBLIC)
                            .addParameter("value",BigInteger.class)
                            .addStatement("super(%L, %N)", bitSize, "value")
                            .build();

            FunSpec overideConstructorSpec =
                    FunSpec.constructorBuilder()
                            .addModifiers(KModifier.PUBLIC)
                            .addParameter("value", long.class)
                            .addStatement("this(BigInteger.valueOf(value))")
                            .build();

            PropertySpec defaultFieldSpec =
                    PropertySpec.builder(DEFAULT,
                                    className,
                                    KModifier.PUBLIC,
                                    KModifier.FINAL,
                                    KModifier.FINAL)
                            .initializer("new %T(BigInteger.ZERO)", className)
                            .build();

            TypeSpec intType =
                    TypeSpec.classBuilder(className.getSimpleName())
                            .superclass(superclass)
                            .addModifiers(KModifier.PUBLIC)
                            .addProperty(defaultFieldSpec)
                            .addFunctions(Arrays.asList(constructorSpec, overideConstructorSpec))
                            .build();

            write(packageName, intType, path);
        }
    }

    private <T extends Type> void generateFixedTypes(Class<T> superclass, String path)
            throws IOException {
        String packageName = createPackageName(superclass);
        ClassName className;

        for (int mBitSize = 8; mBitSize < Type.MAX_BIT_LENGTH; mBitSize += 8) {
            inner:
            for (int nBitSize = 8; nBitSize < Type.MAX_BIT_LENGTH; nBitSize += 8) {

                if (mBitSize + nBitSize > Type.MAX_BIT_LENGTH) {
                    break inner;
                }

                FunSpec constructorSpec1 =
                        FunSpec.constructorBuilder()
                                .addModifiers(KModifier.PUBLIC)
                                .addParameter("value", BigInteger.class)
                                .addStatement("super(%L, %L, %N)", mBitSize, nBitSize, "value")
                                .build();

                FunSpec constructorSpec2 =
                        FunSpec.constructorBuilder()
                                .addModifiers(KModifier.PUBLIC)
                                .addParameter("mBitSize", int.class)
                                .addParameter("nBitSize", int.class)
                                .addParameter("m", BigInteger.class)
                                .addParameter("n", BigInteger.class)
                                .addStatement("super(%L, %L, %N, %N)", mBitSize, nBitSize, "m", "n")
                                .build();

                className =
                        new ClassName(
                                packageName,
                                superclass.getSimpleName() + mBitSize + "x" + nBitSize);

                PropertySpec defaultFieldSpec =
                        PropertySpec.builder(DEFAULT,
                                        className,
                                        KModifier.PUBLIC,
                                        KModifier.FINAL,
                                        KModifier.FINAL)
                                .initializer("new %T(BigInteger.ZERO)", className)
                                .build();

                TypeSpec fixedType =
                        TypeSpec.classBuilder(className.getSimpleName())
                                .superclass(superclass)
                                .addModifiers(KModifier.PUBLIC)
                                .addProperty(defaultFieldSpec)
                                .addFunction(constructorSpec1)
                                .addFunction(constructorSpec2)
                                .build();

                write(packageName, fixedType, path);
            }
        }
    }

    private <T extends Type> void generateBytesTypes(String path) throws IOException {
        String packageName = createPackageName(Bytes.class);
        ClassName className;

        for (int byteSize = 1; byteSize <= 32; byteSize++) {

            FunSpec constructorSpec =
                    FunSpec.constructorBuilder()
                            .addModifiers(KModifier.PUBLIC)
                            .addParameter("value",byte[].class)
                            .addStatement("super(%L, %N)", byteSize, "value")
                            .build();

            className = new ClassName(packageName, Bytes.class.getSimpleName() + byteSize);

            PropertySpec defaultFieldSpec =
                    PropertySpec.builder(  DEFAULT,
                                    className,
                                    KModifier.PUBLIC,
                                    KModifier.FINAL,
                                    KModifier.FINAL)
                            .initializer("new %T(new byte[%L])", className, byteSize)
                            .build();

            TypeSpec bytesType =
                    TypeSpec.classBuilder(className.getSimpleName())
                            .superclass(Bytes.class)
                            .addModifiers(KModifier.PUBLIC)
                            .addProperty(defaultFieldSpec)
                            .addFunction(constructorSpec)
                            .build();

            write(packageName, bytesType, path);
        }
    }

    private <T extends Type> void generateStaticArrayTypes(String path) throws IOException {
        String packageName = createPackageName(StaticArray.class);
        ClassName className;

        for (int length = 0; length <= StaticArray.MAX_SIZE_OF_STATIC_ARRAY; length++) {

            TypeVariableName typeVariableName = TypeVariableName.get("T");

            FunSpec oldConstructorSpec =
                    FunSpec.constructorBuilder()
                            .addAnnotation(Deprecated.class)
                            .addModifiers(KModifier.PUBLIC)
                            .addParameter("values",
                                    ParameterizedTypeName.get(
                                            new ClassName("java.util", "List"), typeVariableName).getClass()
                                    )
                            .addStatement("super(%L, %N)", length, "values")
                            .build();

            FunSpec oldArrayOverloadConstructorSpec =
                    FunSpec.constructorBuilder()
                            .addAnnotation(Deprecated.class)
                            .addAnnotation(SafeVarargs.class)
                            .addModifiers(KModifier.PUBLIC)
                            // Use Object[] as the runtime type for varargs; actual generic info is erased at runtime
                            .addParameter("values", Object[].class)
                                    .addStatement("super(%L, %N)", length, "values")
                            .build();

            FunSpec constructorSpec =
                    FunSpec.constructorBuilder()
                            .addModifiers(KModifier.PUBLIC)
                            .addParameter("type",
                                    ParameterizedTypeName.get(
                                            new ClassName("java.lang", "Class"), typeVariableName)
                                    )
                            .addParameter("values",
                                    ParameterizedTypeName.get(
                                            new ClassName("java.util", "List"), typeVariableName)
                                    )
                            .addStatement("super(type, %L, values)", length)
                            .build();

            FunSpec arrayOverloadConstructorSpec =
                    FunSpec.constructorBuilder()
                            .addAnnotation(SafeVarargs.class)
                            .addModifiers(KModifier.PUBLIC)
                            .addParameter("type",
                                    ParameterizedTypeName.get(
                                            new ClassName("java.lang", "Class"), typeVariableName)
                                    )
                            // Use Object[] for varargs parameter at runtime
                            .addParameter("values", Object[].class)
                            .addStatement("super(type, %L, values)", length)
                            .build();

            className = new ClassName(packageName, StaticArray.class.getSimpleName() + length);

            TypeSpec bytesType =
                    TypeSpec.classBuilder(className.getSimpleName())
                            .addTypeVariable(typeVariableName)
                            .superclass(
                                    ParameterizedTypeName.get(
                                            new ClassName("org.web3j.abi.datatypes", "StaticArray"), typeVariableName))
                            .addModifiers(KModifier.PUBLIC)
                            .addFunctions(
                                    Arrays.asList(
                                            oldConstructorSpec, oldArrayOverloadConstructorSpec))
                            .addFunctions(
                                    Arrays.asList(constructorSpec, arrayOverloadConstructorSpec))
                            .build();

            write(packageName, bytesType, path);
        }
    }

    static String createPackageName(Class<?> cls) {
        return getPackageName(cls) + ".generated";
    }

    static String getPackageName(Class<?> cls) {
        return cls.getPackage().getName();
    }
}
