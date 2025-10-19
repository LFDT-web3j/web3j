/*
 * Copyright 2020 Web3 Labs Ltd.
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
package org.web3j.codegen.unit.gen.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.web3j.tuples.Tuple;
import org.web3j.tuples.generated.Tuple2;

public class JavaMappingHelper implements MappingHelper {
    private Map<Class, Object> defaultValueMap = new HashMap<>();
    private Map<Class, String> poetFormat = new HashMap<>();

    public JavaMappingHelper() {
        defaultValueMap.put(String.class, "REPLACE_ME");
        defaultValueMap.put(BigInteger.class, "BigInteger.ONE");
        defaultValueMap.put(List.class, ArrayList.class);
        defaultValueMap.put(Tuple.class, Tuple.class);
        defaultValueMap.put(byte[].class, byte[].class);
        defaultValueMap.put(Boolean.class, true);
        poetFormat.put(Boolean.class, "%L");
        poetFormat.put(String.class, "%S");
        poetFormat.put(BigInteger.class, "%T.ONE");
        poetFormat.put(List.class, "%L");
        poetFormat.put(Tuple.class, "%T()");
        poetFormat.put(Tuple2.class, "%T()");
        poetFormat.put(byte[].class, "%L");
    }

    public Map<Class, Object> getDefaultValueMap() {
        return defaultValueMap;
    }

    public Map<Class, String> getPoetFormat() {
        return poetFormat;
    }
}
