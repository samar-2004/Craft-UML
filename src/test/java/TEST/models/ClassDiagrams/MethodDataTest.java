package TEST.models.ClassDiagrams;

import org.example.craftuml.models.ClassDiagrams.MethodData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MethodDataTest {

    @Test
    void testConstructorAndDefaults() {
        MethodData methodData = new MethodData("public", "methodName", "String");

        assertEquals("public", methodData.getAccessModifier(), "Access modifier should be set correctly");
        assertEquals("methodName", methodData.getName(), "Method name should be set correctly");
        assertEquals("String", methodData.getReturnType(), "Return type should be set correctly");
    }

    @Test
    void testSetAccessModifier() {
        MethodData methodData = new MethodData();
        methodData.setAccessModifier("private");

        assertEquals("private", methodData.getAccessModifier(), "Access modifier should be set correctly");
    }

    @Test
    void testSetName() {
        MethodData methodData = new MethodData();
        methodData.setName("newMethod");

        assertEquals("newMethod", methodData.getName(), "Method name should be set correctly");
    }

    @Test
    void testSetReturnType() {
        MethodData methodData = new MethodData();
        methodData.setReturnType("void");

        assertEquals("void", methodData.getReturnType(), "Return type should be set correctly");
    }

    @Test
    void testInvalidName() {
        MethodData methodData = new MethodData();
        assertThrows(IllegalArgumentException.class, () -> methodData.setName(null), "Method name cannot be null");
        assertThrows(IllegalArgumentException.class, () -> methodData.setName(""), "Method name cannot be empty");
    }

    @Test
    void testSetAllFields() {
        MethodData methodData = new MethodData();
        methodData.setAccessModifier("protected");
        methodData.setName("exampleMethod");
        methodData.setReturnType("int");

        assertEquals("protected", methodData.getAccessModifier(), "Access modifier should match");
        assertEquals("exampleMethod", methodData.getName(), "Method name should match");
        assertEquals("int", methodData.getReturnType(), "Return type should match");
    }
}
