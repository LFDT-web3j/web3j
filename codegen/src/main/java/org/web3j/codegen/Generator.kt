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

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import java.io.IOException

/** Common code generator methods for Kotlin code generation. */
open class Generator {

    @Throws(IOException::class)
    fun write(packageName: String, typeSpec: TypeSpec, destinationDir: String) {
        val typeName = typeSpec.name
            ?: throw IllegalArgumentException("TypeSpec provided to write() has no name. This is likely a bug in the generator - ensure all TypeSpec.classBuilder(...) calls supply a non-empty name.")

        if (typeName.isEmpty()) {
            throw IllegalArgumentException("TypeSpec provided to write() has an empty name. This is likely a bug in the generator.")
        }

        val kotlinFile = FileSpec.builder(packageName, typeName)
            .addType(typeSpec)
            .indent("    ")
            .build()

        kotlinFile.writeTo(File(destinationDir))
    }

    companion object {
        fun buildWarning(cls: Class<*>): String {
            return "Auto generated code.\n" +
                    "<p><strong>Do not modify!</strong>\n" +
                    "<p>Please use " + cls.name + " in the \n" +
                    "<a href=\"https://github.com/hyperledger/web3j/tree/main/codegen\">" +
                    "codegen module</a> to update.\n"
        }
    }
}