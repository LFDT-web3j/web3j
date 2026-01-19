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
import java.util.ArrayList;
import java.util.List;


import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.CodeBlock;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.PropertySpec;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.TypeName;
import com.squareup.kotlinpoet.TypeSpec;
import com.squareup.kotlinpoet.TypeVariableName;

import org.web3j.tuples.Tuple;
import org.web3j.utils.Strings;

/** A class for generating arbitrary sized tuples. */
public class TupleGenerator extends Generator {

    static final int LIMIT = 20;
    static final String PACKAGE_NAME = "org.web3j.tuples.generated";
    static final String CLASS_NAME = "Tuple";

    private static final String SIZE = "SIZE";
    private static final String RESULT = "result";
    private static final String VALUE = "value";

    public static void main(String[] args) throws IOException {
        TupleGenerator tupleGenerator = new TupleGenerator();
        if (args.length == 1) {
            tupleGenerator.generate(args[0]);
        } else {
            tupleGenerator.generate(System.getProperty("user.dir") + "/tuples/src/main/java/");
        }
    }

    private void generate(String destinationDir) throws IOException {
        for (int i = 1; i <= LIMIT; i++) {
            TypeSpec typeSpec = createTuple(i);

            write(PACKAGE_NAME, typeSpec, destinationDir);
        }
    }

    private TypeSpec createTuple(int size) {
        String javadoc = "@deprecated use 'component%L' method instead \n @return returns a value";
        String className = CLASS_NAME + size;
        TypeName tupleTypeName = ClassName.Companion.bestGuess("org.web3j.tuples.Tuple");
        TypeSpec.Builder typeSpecBuilder =
                TypeSpec.classBuilder(className)
                        .addSuperinterface(tupleTypeName, CodeBlock.of("%T", TypeVariableName.get("T")))
                        .addProperty(
                                PropertySpec.builder( SIZE, int.class)
                                        .addModifiers(
                                                KModifier.PRIVATE, KModifier.FINAL, KModifier.FINAL)
                                        .initializer("%L", size)
                                        .build());

        FunSpec.Builder constructorBuilder =
                FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC);

        List<FunSpec> methodSpecs = new ArrayList<>(size);

        for (int i = 1; i <= size; i++) {
            String value = VALUE + i;
            TypeVariableName typeVariableName = TypeVariableName.get("T" + i);

            typeSpecBuilder
                    .addTypeVariable(typeVariableName)
                    .addProperty( value, typeVariableName, KModifier.PRIVATE, KModifier.FINAL);

                    constructorBuilder
                    .addParameter(value,typeVariableName)
                            .addStatement("this.%N = %N", value, value);

            FunSpec getterSpec =
                    FunSpec.builder("get" + Strings.capitaliseFirstLetter(value))
                            .addAnnotation(Deprecated.class)
                            .addModifiers(KModifier.PUBLIC)
                            .returns(typeVariableName)
                            .addStatement("return %N", value)
                            .build();
            methodSpecs.add(getterSpec);

            FunSpec getterSpec2 =
                    FunSpec.builder("component" + i)
                            .addModifiers(KModifier.PUBLIC)
                            .returns(typeVariableName)
                            .addStatement("return %N", value)
                            .build();
            methodSpecs.add(getterSpec2);
        }

        FunSpec constructorSpec = constructorBuilder.build();
        FunSpec sizeSpec = generateSizeSpec();
        FunSpec equalsSpec = generateEqualsSpec(className, size);
        FunSpec hashCodeSpec = generateHashCodeSpec(size);
        FunSpec toStringSpec = generateToStringSpec(size);

        return typeSpecBuilder
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                .addFunction(constructorSpec)
                .addFunctions(methodSpecs)
                .addFunction(sizeSpec)
                .addFunction(equalsSpec)
                .addFunction(hashCodeSpec)
                .addFunction(toStringSpec)
                .build();
    }

    private FunSpec generateSizeSpec() {
        return FunSpec.builder("getSize")
                .addAnnotation(Override.class)
                .addModifiers(KModifier.PUBLIC)
                .returns(int.class)
                .addStatement("return %L", SIZE)
                .build();
    }

    private FunSpec generateEqualsSpec(String className, int size) {
        FunSpec.Builder equalsSpecBuilder =
                FunSpec.builder("equals")
                        .addAnnotation(Override.class)
                        .addModifiers(KModifier.PUBLIC)
                        .addParameter( "o", Object.class)
                        .returns(boolean.class)
                        .beginControlFlow("if (this == o)")
                        .addStatement("return true")
                        .endControlFlow()
                        .beginControlFlow("if (o == null || getClass() != o.getClass())")
                        .addStatement("return false")
                        .endControlFlow();

        String typeParams = Strings.repeat('?', size).replaceAll("\\?", "?, ");
        typeParams = typeParams.substring(0, typeParams.length() - 2);
        String wildcardClassName = className + "<" + typeParams + ">";

        String name = "tuple" + size;
        equalsSpecBuilder.addStatement(
                "%L %L = (%L) o", wildcardClassName, name, wildcardClassName);

        for (int i = 1; i < size; i++) {
            String value = VALUE + i;

            equalsSpecBuilder
                    .beginControlFlow(
                            "if (%L != null ? !%L.equals(%L.%L) : %L.%L != null)",
                            value,
                            value,
                            name,
                            value,
                            name,
                            value)
                    .addStatement("return false")
                    .endControlFlow();
        }

        String lastValue = VALUE + size;
        equalsSpecBuilder.addStatement(
                "return %L != null ? %L.equals(%L.%L) : %L.%L == null",
                lastValue,
                lastValue,
                name,
                lastValue,
                name,
                lastValue);

        return equalsSpecBuilder.build();
    }

    private FunSpec generateHashCodeSpec(int size) {
        FunSpec.Builder hashCodeSpec =
                FunSpec.builder("hashCode")
                        .addAnnotation(Override.class)
                        .addModifiers(KModifier.PUBLIC)
                        .returns(int.class)
                        .addStatement("int %L = %L.hashCode()", RESULT, VALUE + 1);

        for (int i = 2; i <= size; i++) {
            String value = "value" + i;
            hashCodeSpec.addStatement(
                    "%L = 31 * %L + (%L != null ? %L.hashCode() : 0)",
                    RESULT,
                    RESULT,
                    value,
                    value);
        }

        hashCodeSpec.addStatement("return %L", RESULT);

        return hashCodeSpec.build();
    }

    private FunSpec generateToStringSpec(int size) {
        String toString = "return \"" + CLASS_NAME + size + "{\" +\n";
        String firstValue = VALUE + 1;
        toString += "\"" + firstValue + "=\"" + " + " + firstValue + " +\n";

        for (int i = 2; i <= size; i++) {
            String value = VALUE + i;
            toString += "\", " + value + "=\"" + " + " + value + " +\n";
        }

        toString += "\"}\"";

        return FunSpec.builder("toString")
                .addAnnotation(Override.class)
                .addModifiers(KModifier.PUBLIC)
                .returns(String.class)
                .addStatement(toString)
                .build();
    }
}
