package org.web3j.codegen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.web3j.abi.DefaultFunctionEncoder;
import org.web3j.abi.DefaultFunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.reflection.Parameterized;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class StructDynamicArrayDecodeTest {

    private SolidityFunctionWrapper solidityFunctionWrapper;
    private GenerationReporter generationReporter;

    @BeforeEach
    public void setUp() {
        generationReporter = mock(GenerationReporter.class);
        solidityFunctionWrapper = new SolidityFunctionWrapper(true, false, false, 20, generationReporter);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStructGenerationWithArrays() throws Exception {
        AbiDefinition.NamedType addressArray = new AbiDefinition.NamedType("addrArr", "address[]");
        AbiDefinition.NamedType uintArray = new AbiDefinition.NamedType("uintArr", "uint256[]");
        AbiDefinition.NamedType uintStaticArray = new AbiDefinition.NamedType("uintStatic", "uint256[10]");
        AbiDefinition.NamedType nestedAddrArray = new AbiDefinition.NamedType("nestedAddr", "address[][]");
        AbiDefinition.NamedType dynamicBytes = new AbiDefinition.NamedType("rawBytes", "bytes");

        AbiDefinition.NamedType structType = new AbiDefinition.NamedType("MyStruct", "tuple");
        structType.setInternalType("struct MyContract.MyStruct");
        structType.setComponents(Arrays.asList(addressArray, uintArray, uintStaticArray, nestedAddrArray, dynamicBytes));

        AbiDefinition function = new AbiDefinition();
        function.setType("function");
        function.setName("getStruct");
        function.setOutputs(Collections.singletonList(structType));

        java.lang.reflect.Method buildStructTypes = SolidityFunctionWrapper.class.getDeclaredMethod("buildStructTypes", List.class);
        buildStructTypes.setAccessible(true);
        List<TypeSpec> structs = (List<TypeSpec>) buildStructTypes.invoke(solidityFunctionWrapper, Collections.singletonList(function));

        TypeSpec myStruct = structs.get(0);

        // address[] -> annotated
        assertHasParameterizedAnnotation(myStruct, "addrArr", Address.class);
        // uint256[] -> annotated
        assertHasParameterizedAnnotation(myStruct, "uintArr", Uint256.class);
        // address[][] -> annotated
        assertHasParameterizedAnnotation(myStruct, "nestedAddr", Address.class);
        // Static array -> NOT annotated
        assertNoParameterizedAnnotation(myStruct, "uintStatic");
        // Non-array -> NOT annotated
        assertNoParameterizedAnnotation(myStruct, "rawBytes");
    }

    @Test
    public void testDecodingWithParameterizedAnnotation() {
        String encodedAddressArray = 
                "0000000000000000000000000000000000000000000000000000000000000020" +
                "0000000000000000000000000000000000000000000000000000000000000002" +
                "0000000000000000000000001234567890123456789012345678901234567890" +
                "0000000000000000000000000987654321098765432109876543210987654321";
        String encodedStruct = "0000000000000000000000000000000000000000000000000000000000000020" + encodedAddressArray;

        DefaultFunctionReturnDecoder decoder = new DefaultFunctionReturnDecoder();
        @SuppressWarnings("unchecked")
        List<TypeReference<Type>> typeReferences = Collections.singletonList(
                (TypeReference<Type>) (TypeReference) new TypeReference<TestStruct>() {});

        List<Type> results = decoder.decodeFunctionResult(encodedStruct, typeReferences);
        
        assertNotNull(results);
        TestStruct decoded = (TestStruct) results.get(0);
        assertEquals(2, decoded.addrArr.getValue().size());
    }

    @Test
    public void testDecodingNestedWithParameterizedAnnotation() {
        // ABI encoding of address[][] containing [[address1, address2]]
        // Dynamic offset for nestedAddr: 0x20 (32 bytes)
        // nestedAddr length: 1
        // nestedAddr[0] offset: 0x20 (32 bytes relative to nestedAddr start)
        // nestedAddr[0] length: 2
        // address1: 0x1234567890123456789012345678901234567890
        // address2: 0x0987654321098765432109876543210987654321
        String encoded = 
                "0000000000000000000000000000000000000000000000000000000000000020" + // struct offset to nestedAddr
                "0000000000000000000000000000000000000000000000000000000000000020" + // nestedAddr length = 1
                "0000000000000000000000000000000000000000000000000000000000000001" + 
                "0000000000000000000000000000000000000000000000000000000000000020" + // inner array offset = 32
                "0000000000000000000000000000000000000000000000000000000000000002" + // inner array length = 2
                "0000000000000000000000001234567890123456789012345678901234567890" + // address 1
                "0000000000000000000000000987654321098765432109876543210987654321";  // address 2

        DefaultFunctionReturnDecoder decoder = new DefaultFunctionReturnDecoder();
        @SuppressWarnings("unchecked")
        List<TypeReference<Type>> typeReferences = Collections.singletonList(
                (TypeReference<Type>) (TypeReference) new TypeReference<TestNestedStruct>() {});

        List<Type> results = decoder.decodeFunctionResult(encoded, typeReferences);
        
        assertNotNull(results);
        TestNestedStruct decoded = (TestNestedStruct) results.get(0);
        assertEquals(1, decoded.nestedAddr.getValue().size());
        assertEquals(2, decoded.nestedAddr.getValue().get(0).getValue().size());
    }

    @Test
    public void testEncodingAndDecodingNestedStructRoundtrip() {
        Address addr1 = new Address("0x1234567890123456789012345678901234567890");
        Address addr2 = new Address("0x0987654321098765432109876543210987654321");
        DynamicArray<Address> innerArray = new DynamicArray<>(Address.class, addr1, addr2);
        DynamicArray<DynamicArray<Address>> outerArray = new DynamicArray<>(
                (Class<DynamicArray<Address>>) (Class) DynamicArray.class,
                innerArray
        );

        TestNestedStruct originalStruct = new TestNestedStruct(outerArray);

        Function function = new Function(
                "testFunction",
                Collections.singletonList(originalStruct),
                Collections.emptyList()
        );

        DefaultFunctionEncoder encoder = new DefaultFunctionEncoder();
        String encoded = encoder.encode(function);
        
        // Strip method ID (4 bytes/8 chars) and "0x"
        String encodedParameters = encoded.substring(10);

        DefaultFunctionReturnDecoder decoder = new DefaultFunctionReturnDecoder();
        @SuppressWarnings("unchecked")
        List<TypeReference<Type>> typeReferences = Collections.singletonList(
                (TypeReference<Type>) (TypeReference) new TypeReference<TestNestedStruct>() {});

        List<Type> results = decoder.decodeFunctionResult(encodedParameters, typeReferences);

        assertNotNull(results);
        assertEquals(1, results.size());
        TestNestedStruct decodedStruct = (TestNestedStruct) results.get(0);
        
        assertEquals(1, decodedStruct.nestedAddr.getValue().size());
        assertEquals(2, decodedStruct.nestedAddr.getValue().get(0).getValue().size());
        assertEquals(addr1.getValue(), decodedStruct.nestedAddr.getValue().get(0).getValue().get(0).getValue());
        assertEquals(addr2.getValue(), decodedStruct.nestedAddr.getValue().get(0).getValue().get(1).getValue());
    }

    public static class TestStruct extends DynamicStruct {
        public DynamicArray<Address> addrArr;
        public TestStruct(@Parameterized(type = Address.class) DynamicArray<Address> addrArr) {
            super(addrArr);
            this.addrArr = addrArr;
        }
    }

    public static class TestNestedStruct extends DynamicStruct {
        public DynamicArray<DynamicArray<Address>> nestedAddr;
        public TestNestedStruct(@Parameterized(type = Address.class) DynamicArray<DynamicArray<Address>> nestedAddr) {
            super(nestedAddr);
            this.nestedAddr = nestedAddr;
        }
    }

    private void assertHasParameterizedAnnotation(TypeSpec typeSpec, String paramName, Class<?> expectedType) {
        MethodSpec constructor = typeSpec.methodSpecs.stream()
                .filter(m -> m.isConstructor() && m.parameters.size() > 0)
                .findFirst()
                .get();

        ParameterSpec parameter = constructor.parameters.stream()
                .filter(p -> p.name.equals(paramName))
                .findFirst()
                .get();

        AnnotationSpec annotation = parameter.annotations.stream()
                .filter(a -> a.type.toString().equals(Parameterized.class.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing @Parameterized on " + paramName));
        
        assertTrue(annotation.members.get("type").toString().contains(expectedType.getSimpleName()));
    }

    private void assertNoParameterizedAnnotation(TypeSpec typeSpec, String paramName) {
        MethodSpec constructor = typeSpec.methodSpecs.stream()
                .filter(m -> m.isConstructor() && m.parameters.size() > 0)
                .findFirst()
                .get();

        ParameterSpec parameter = constructor.parameters.stream()
                .filter(p -> p.name.equals(paramName))
                .findFirst()
                .get();

        boolean present = parameter.annotations.stream()
                .anyMatch(a -> a.type.toString().equals(Parameterized.class.getName()));
        assertFalse(present, paramName + " should NOT have annotation");
    }
}
