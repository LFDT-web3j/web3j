package org.web3j.codegen

class hello {
    fun main() {
        val abiFile = "build/resources/main/solidity/HelloWorld.abi"
        val binFile = "build/resources/main/solidity/HelloWorld.bin"
        val outputDir = "src/main/java"
        val packageName = "com.yourorg.contracts"

        val params = arrayOf<String?>(
            "-a", abiFile,
            "-b", binFile,
            "-o", outputDir,
            "-p", packageName
        )
        SolidityFunctionWrapperGenerator.main(params)
    }
}