package TEST.models.ClassDiagrams;

import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttributeDataTest {

    @Test
    void testDefaultConstructor() {
        AttributeData attribute = new AttributeData();

        assertNull(attribute.getAccessModifier(), "Default accessModifier should be null");
        assertNull(attribute.getName(), "Default name should be null");
        assertNull(attribute.getDataType(), "Default dataType should be null");
    }

    @Test
    void testParameterizedConstructor() {
        AttributeData attribute = new AttributeData("+", "attributeName", "String");

        assertEquals("+", attribute.getAccessModifier(), "Access modifier should match the constructor argument");
        assertEquals("attributeName", attribute.getName(), "Name should match the constructor argument");
        assertEquals("String", attribute.getDataType(), "Data type should match the constructor argument");
    }

    @Test
    void testSetAccessModifier() {
        AttributeData attribute = new AttributeData();
        attribute.setAccessModifier("#");

        assertEquals("#", attribute.getAccessModifier(), "Access modifier should match the set value");
    }

    @Test
    void testSetName() {
        AttributeData attribute = new AttributeData();
        attribute.setName("newName");

        assertEquals("newName", attribute.getName(), "Name should match the set value");
    }

    @Test
    void testSetDataType() {
        AttributeData attribute = new AttributeData();
        attribute.setDataType("int");

        assertEquals("int", attribute.getDataType(), "Data type should match the set value");
    }
}
