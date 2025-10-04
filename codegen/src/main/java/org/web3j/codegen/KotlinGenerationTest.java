package org.web3j.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class KotlinGenerationTest {
    public static void main(String[] args) throws Exception {
        // Create temporary directory
        File tempDir = Files.createTempDirectory("kotlin_test_output").toFile();
        System.out.println("Temp directory: " + tempDir.getAbsolutePath());

        // Create test ABI content
        String testAbiContent = "[{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_message\",\"type\":\"string\"}],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"inputs\":[],\"name\":\"getMessage\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_message\",\"type\":\"string\"}],\"name\":\"setMessage\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";

        // Create test BIN content (simplified for testing)
        String testBinContent = "608060405234801561001057600080fd5b5060405161047f38038061047f8339818101604052602081101561003357600080fd5b810190808051604051939291908464010000000082111561005357600080fd5b8382019150602082018581111561006957600080fd5b825186600182028301116401000000008211171561008657600080fd5b8083526020830192505050908051906020019080838360005b838110156100ba5780820151818401526020810190506100a4565b50505050905090810190601f1680156100e75780820380516001836020036101000a031916815260200191505b50604052505050806000908051906020019061010492919061010b565b50506101b0565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061014c57805160ff191683800117855561017a565b8280016001018555821561017a579182015b8281111561017957825182559160200191906001019061015e565b5b509050610187919061018b565b5090565b6101ad91905b808211156101a957600081600090555060010161019156";

        // Create temp ABI and BIN files
        File tempAbiFile = new File(tempDir, "HelloWorld.abi");
        File tempBinFile = new File(tempDir, "HelloWorld.bin");
        Files.write(tempAbiFile.toPath(), testAbiContent.getBytes());
        Files.write(tempBinFile.toPath(), testBinContent.getBytes());

        // Set up parameters for SolidityFunctionWrapperGenerator
        String[] params = new String[] {
            "-a", tempAbiFile.getAbsolutePath(),
            "-b", tempBinFile.getAbsolutePath(),
            "-o", tempDir.getAbsolutePath(),
            "-p", "com.test.contracts",
            "-kt" // Add Kotlin flag to generate Kotlin files
        };

        // Call the main method to generate wrapper
        System.out.println("Generating wrapper...");
        org.web3j.codegen.SolidityFunctionWrapperGenerator.main(params);

        // Check if Kotlin file was generated
        File generatedFile = new File(tempDir, "com/test/contracts/HelloWorld.kt");
        if (generatedFile.exists()) {
            System.out.println("SUCCESS: Kotlin file was generated at: " + generatedFile.getAbsolutePath());
        } else {
            File javaFile = new File(tempDir, "com/test/contracts/HelloWorld.java");
            if (javaFile.exists()) {
                System.out.println("FAILURE: Java file was generated instead of Kotlin: " + javaFile.getAbsolutePath());
            } else {
                System.out.println("FAILURE: No file was generated at expected location");
                // List all files in the output directory
                listFiles(new File(tempDir, "com/test/contracts"));
            }
        }
    }

    private static void listFiles(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            System.out.println("Files in " + directory.getAbsolutePath() + ":");
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println("  " + file.getName());
                }
            } else {
                System.out.println("  (none)");
            }
        } else {
            System.out.println("Directory does not exist: " + directory.getAbsolutePath());
            // Try to list parent directory
            File parent = directory.getParentFile();
            if (parent != null && parent.exists()) {
                listFiles(parent);
            }
        }
    }
}
