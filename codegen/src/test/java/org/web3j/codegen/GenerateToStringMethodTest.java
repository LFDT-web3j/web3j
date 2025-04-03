import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import com.squareup.javapoet.MethodSpec;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import javax.lang.model.SourceVersion;


public class GenerateToStringMethodTest {

    @Test
    public void testGenerateToStringMethod() {
        // Step 1: Create mock components for the struct fields
        //AbiDefinition.NamedType component1 = new AbiDefinition.NamedType("name", "string", false);
        //AbiDefinition.NamedType component2 = new AbiDefinition.NamedType("age", "uint256", false);
        AbiDefinition.NamedType component1 = new AbiDefinition.NamedType("name", "string", new ArrayList<>(), "", false);
        AbiDefinition.NamedType component2 = new AbiDefinition.NamedType("age", "string", new ArrayList<>(), "", false);

        // Add components to the list
        List<AbiDefinition.NamedType> components = Arrays.asList(component1, component2);

        // Step 2: Call the generateToStringMethod() with the components
        MethodSpec toStringMethod = generateToStringMethod(components);

        // Step 3: Generate the expected toString output
        //String expectedFormat = "\"{ name='\" + name + \"', age='\" + age + \"' }\"";
        //String expectedMethod = "return \"{ name='\" + name + \"', age='\" + age + \"' }\";";
        //assertEquals(expectedMethod, actualMethodBody.trim(), "Generated method does not match expected output.");
        String actualMethodBody = toStringMethod.toString();
        System.out.println("Generated Method:\n" + actualMethodBody);
        System.out.flush();
        
        StringBuilder format = new StringBuilder("\"{ ");
        if (!components.isEmpty()) {
            format.setLength(format.length() - 2);
        };
        format.append("}\"");
        //assertTrue(actualMethodBody.contains("return \"{ name='\" + name + \"', age='\" + age + \"' }\";"));

        // Step 4: Ensure the generated method matches the expected format
        //String actualMethodBody = toStringMethod.toString();
        //assertTrue(actualMethodBody.contains("return \"{ name='\" + name + \"', age='\" + age + \"' }\";"));
        //assertTrue(actualMethodBody.contains(expectedFormat), "The toString method was not generated correctly.");
        assertTrue(actualMethodBody.contains("return \"{ name='\" + name + \"', age='\" + age + \"' }\";"),
        "Generated toString method is incorrect.");

    }

    private MethodSpec generateToStringMethod(List<AbiDefinition.NamedType> components) {
        // Original method code goes here (you can copy the method provided above)
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toString")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class);
        
        StringBuilder format = new StringBuilder("\"{ ");
        List<Object> args = new ArrayList<>();

        for (AbiDefinition.NamedType component : components) {
            String fieldName = !SourceVersion.isName(component.getName()) ? 
                "_" + component.getName() : component.getName();
            format.append(fieldName).append("='\" + ").append(fieldName).append(" + \"', ");
        }

        format.append("}\"");

        builder.addStatement("return " + format.toString());
        return builder.build();
    }
}