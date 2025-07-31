/*
 * Copyright 2024 Web3 Labs Ltd.
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
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneraterTestUtils {

    public static void verifyGeneratedCode(String sourceFile) throws IOException {
        File file = new File(sourceFile);
        assertTrue(file.exists(), "Generated file does not exist: " + sourceFile);
        
        String content = new String(Files.readAllBytes(Paths.get(sourceFile)));
        assertTrue(content.contains("package "), "Generated code should contain a package declaration");
        assertTrue(content.contains("class "), "Generated code should contain a class declaration");
        assertTrue(!content.trim().isEmpty(), "Generated code should not be empty");
        int openBraces = content.length() - content.replace("{", "").length();
        int closeBraces = content.length() - content.replace("}", "").length();
        assertTrue(openBraces == closeBraces, "Braces should be balanced in generated code");
    }
}
