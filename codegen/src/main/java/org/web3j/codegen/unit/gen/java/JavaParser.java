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
package org.web3j.codegen.unit.gen.java;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.web3j.codegen.unit.gen.Parser;
import org.web3j.codegen.unit.gen.utils.MappingHelper;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import static org.web3j.codegen.unit.gen.utils.NameUtils.toCamelCase;

/*
 * Class that provides parsing utility between Unit Generation and poet generators.
 */
public class JavaParser extends Parser {

    public JavaParser(Class<?> theContract, Method method, MappingHelper mappingHelper) {
        super(theContract, method, mappingHelper);
    }

    public String generateAssertionPoetStringTypes() {
        Type returnType = getMethodReturnType();
        Object[] body = generatePlaceholderValues();
        StringBuilder symbolBuilder = new StringBuilder();
        symbolBuilder.append("%T.");
        if (returnType.equals(TransactionReceipt.class)) {
            symbolBuilder.append("assertTrue(%L.isStatusOK())");
        } else {
            symbolBuilder.append("assertEquals(");
            if (returnType.getTypeName().contains("Tuple")) {
                symbolBuilder.append("new %T(");
                for (Type t : getTypeArray(returnType)) {
                    symbolBuilder.append(mappingHelper.getPoetFormat().get(t)).append(", ");
                }
                symbolBuilder.deleteCharAt(symbolBuilder.lastIndexOf(", "));
                symbolBuilder.append(")");
            } else {
                symbolBuilder.append(mappingHelper.getPoetFormat().get(body[0]));
            }
            symbolBuilder.append(", ");
            symbolBuilder.append("%L");
            symbolBuilder.append(")");
        }

        return symbolBuilder.toString();
    }

    @Override
    public String generatePoetStringTypes() {
        StringBuilder symbolBuilder = new StringBuilder();
        if (getMethodReturnType().equals(theContract)) {
            symbolBuilder.append("%L = %T.");
        } else {
            symbolBuilder.append("%T %L = %L.");
        }
        symbolBuilder
                .append(method.getName())
                .append("(")
                .append(getPoetFormatSpecifier())
                .append(").send()");

        return symbolBuilder.toString();
    }

    @Override
    protected Object getDefaultValueForType(Class<?> type) {
        if (mappingHelper.getDefaultValueMap().containsKey(type)) {
            return mappingHelper.getDefaultValueMap().get(type);
        } else {
            return toCamelCase(type);
        }
    }

    @Override
    protected String getPoetFormatSpecifier() {
        List<String> generated = new ArrayList<>();
    Arrays.asList(method.getParameterTypes())
        .forEach(
            type ->
                generated.add(
                    mappingHelper.getPoetFormat().getOrDefault(type, "%L")));
        return String.join(", ", generated);
    }
}
