package org.web3j.codegen;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class hello1 {
    public static void main(String[] args) throws Exception {
        Path projectRoot = Paths.get("/Users/gauthammohanraj/Developer/web3j");

        String abiFile = projectRoot.resolve("codegen/build/resources/main/solidity/HelloWorld.abi").toString();
        String binFile = projectRoot.resolve("codegen/build/resources/main/solidity/HelloWorld.bin").toString();

        String outputDir = projectRoot.resolve("src/main/java").toString();
        String packageName = "com.yourorg.contracts";

        Files.createDirectories(Paths.get(outputDir));

        String[] params = {
                "-a", abiFile,
                "-b", binFile,
                "-o", outputDir,
                "-p", packageName
        };

        // Run the generator
        SolidityFunctionWrapperGenerator.main(params);

        // Build the path to the generated wrapper
        Path wrapperPath = Paths.get(outputDir,
                packageName.replace('.', '/'),
                "HelloWorld.java");

        String content = String.join("\n", Files.readAllLines(wrapperPath));
        System.out.println("======= Generated Contract =======");
        System.out.println(content);
    }
}
