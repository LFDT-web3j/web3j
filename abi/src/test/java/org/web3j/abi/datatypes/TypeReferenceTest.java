//DO not FORget about dynamically decoding structs ; requires changes to TypeDecoder.decode() ; 

import org.junit.jupiter.api.Test;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Struct;
import org.web3j.abi.datatypes.Utf8String;
//import org.web3j.abi.datatypes.Uint256;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeReferenceTest {
    @Test
    public void testStructTypeReferenceToString() {
        List<String> fieldNames = Arrays.asList("name", "balance");
        List<TypeReference<?>> fieldTypes = Arrays.asList(
            TypeReference.create(Utf8String.class),
            TypeReference.create(Uint256.class)
        );

        TypeReference.StructTypeReference<Struct> ref = new TypeReference.StructTypeReference<>("User", fieldNames, fieldTypes);
        //String expected = "StructType(User) {name: class org.web3j.abi.datatypes.Utf8String, balance: class org.web3j.abi.datatypes.Uint256}";
        String expected = "StructType(User) {name: class org.web3j.abi.datatypes.Utf8String, balance: class org.web3j.abi.datatypes.generated.Uint256}";
        //System.out.println("Actual:   " + ref.toString());
        //System.out.println("Expected: " + expected);
        assertEquals(expected, ref.toString());
    }
}