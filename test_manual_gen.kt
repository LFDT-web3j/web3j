import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.codegen.SolidityFunctionWrapper
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.AbiDefinition
import java.math.BigInteger
import java.io.File

fun main() {
    try {
        val abiContent = """[{"constant":true,"inputs":[],"name":"getMessage","outputs":[{"name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"newMessage","type":"string"}],"name":"setMessage","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function"}]"""
        val binaryContent = "0x608060405234801561001057600080fd5b50336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061047f806100606000396000f3fe"
        
        // Parse ABI JSON
        val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
        val abiDefinitions: List<AbiDefinition> = objectMapper.readValue(
            abiContent, 
            objectMapper.typeFactory.constructCollectionType(List::class.java, AbiDefinition::class.java)
        )
        
        val wrapper = SolidityFunctionWrapper(
            true,  // useJavaPrimitiveTypes
            false, // abiFuncs
            20,    // addressLength
            { msg -> println("Reporter: $msg") }, // reporter
            false  // generateBothCallAndSend
        )
        
        val outputDir = "/Users/gauthammohanraj/Developer/web3j/test_output"
        File(outputDir).mkdirs()
        
        wrapper.generateJavaFiles(
            "HelloWorld",
            binaryContent,
            abiDefinitions,
            outputDir,
            "org.web3j.test",
            emptyMap()
        )
        
        println("Wrapper generated successfully!")
        
        // Read and print the generated file
        val generatedFile = File("$outputDir/org/web3j/test/HelloWorld.kt")
        if (generatedFile.exists()) {
            println("Generated file content:")
            println(generatedFile.readText())
        } else {
            println("Generated file not found at: ${generatedFile.absolutePath}")
            // List files in the directory
            File("$outputDir/org/web3j/test").listFiles()?.forEach {
                println("Found file: ${it.name}")
            }
        }
        
    } catch (e: Exception) {
        e.printStackTrace()
    }
}