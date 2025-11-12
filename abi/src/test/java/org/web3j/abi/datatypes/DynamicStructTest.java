//Testing override method 

package org.web3j.codegen;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.web3j.abi.datatypes.Address;

public class DynamicStructTest {

    @Test
    public void testToStringForStruct() {
       Uint256 value1 = new Uint256(123);
       Address value2 = new Address("0x1234567890123456789012345678901234567890");
       DynamicStruct struct = new DynamicStruct(value1, value2);

       String output = struct.toString();
       System.out.println("toString output: " + output);
       
       // Optional: add assertion
       //assertTrue(output.contains("field0: 123"));
       //assertTrue(output.contains("field1: 0x1234567890123456789012345678901234567890"));

       //Assertions
       assertTrue(output.contains("123"));
       assertTrue(output.contains("0x1234567890123456789012345678901234567890"));
    }
}