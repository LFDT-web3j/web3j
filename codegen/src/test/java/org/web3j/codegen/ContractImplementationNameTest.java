package org.web3j.codegen;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.web3j.TempFileProvider;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.web3j.codegen.FunctionWrapperGenerator.JAVA_TYPES_ARG;

public class ContractImplementationNameTest extends TempFileProvider {

    @Test
    public void testGetImplementationNameAtRuntime() throws Exception {
        String contractName = "TestContract";
        String packageName = "org.web3j.test.generated";
        
        // Use a simple ABI with one function
        AbiDefinition function = new AbiDefinition();
        function.setName("hi");
        function.setType("function");
        function.setInputs(Collections.emptyList());
        function.setOutputs(Collections.emptyList());
        function.setConstant(true);
        function.setPayable(false);
        function.setStateMutability("view");

        // Generate the Java file
        new SolidityFunctionWrapper(true, 160, false)
                .generateJavaFiles(
                        contractName,
                        "0x1234",
                        Arrays.asList(function),
                        tempDirPath,
                        packageName,
                        Collections.emptyMap());

        // Compile the generated file
        String sourcePath = tempDirPath + File.separator + 
                packageName.replace('.', File.separatorChar) + File.separator + 
                contractName + ".java";
        
        GeneraterTestUtils.verifyGeneratedCode(sourcePath);

        // Load and verify the class
        File classesDir = new File(tempDirPath);
        URLClassLoader classLoader = new URLClassLoader(new URL[]{classesDir.toURI().toURL()});
        Class<?> contractClass = classLoader.loadClass(packageName + "." + contractName);
        
        // Call getImplementationName and assert
        // Use the public static load method with non-null dummies to avoid NPE in base Contract class
        java.lang.reflect.Method loadMethod = contractClass.getMethod("load", 
                String.class, org.web3j.protocol.Web3j.class, 
                org.web3j.crypto.Credentials.class, 
                org.web3j.tx.gas.ContractGasProvider.class);
        
        org.web3j.protocol.Web3j web3j = (org.web3j.protocol.Web3j) java.lang.reflect.Proxy.newProxyInstance(
                org.web3j.protocol.Web3j.class.getClassLoader(),
                new Class[]{org.web3j.protocol.Web3j.class},
                (proxy, method1, args1) -> null);

        Object instance = loadMethod.invoke(null, 
                "0x0000000000000000000000000000000000000000", 
                web3j,
                org.web3j.crypto.Credentials.create("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"), 
                new org.web3j.tx.gas.StaticGasProvider(java.math.BigInteger.ZERO, java.math.BigInteger.ZERO));
        
        java.lang.reflect.Method method = contractClass.getMethod("getImplementationName");
        Object result = method.invoke(instance);
        
        assertEquals(contractName, result, "The implementation name should match the contract name used during generation");
    }
}
