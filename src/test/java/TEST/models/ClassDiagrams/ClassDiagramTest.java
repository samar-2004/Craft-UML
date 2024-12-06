package TEST.models.ClassDiagrams;

import javafx.scene.shape.Rectangle;
import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.MethodData;
import org.example.craftuml.models.Relationship;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.ArrayList;
import java.util.List;

class ClassDiagramTest {

    @Test
    void testConstructorAndDefaults() {
        ClassDiagram classDiagram = new ClassDiagram("TestClass", 100, 200);

        assertEquals("TestClass", classDiagram.getName());
        assertEquals(100, classDiagram.getX());
        assertEquals(200, classDiagram.getY());
        assertTrue(classDiagram.getAttributes().isEmpty());
        assertTrue(classDiagram.getMethods().isEmpty());
    }

    @Test
    void testSetNameValid() {
        ClassDiagram classDiagram = new ClassDiagram();
        classDiagram.setName("ValidName");
        assertEquals("ValidName", classDiagram.getName());
    }

    @Test
    void testSetNameInvalid() {
        ClassDiagram classDiagram = new ClassDiagram();
        assertThrows(IllegalArgumentException.class, () -> classDiagram.setName(null));
        assertThrows(IllegalArgumentException.class, () -> classDiagram.setName(""));
        assertThrows(IllegalArgumentException.class, () -> classDiagram.setName("Invalid Name"));
    }

    @Test
    void testAddAndSetAttributes() {
        ClassDiagram classDiagram = new ClassDiagram();
        AttributeData attribute1 = new AttributeData("+", "attribute1", "String");
        AttributeData attribute2 = new AttributeData("-", "attribute2", "int");

        classDiagram.addAttributes(attribute1);
        classDiagram.addAttributes(attribute2);

        List<AttributeData> attributes = classDiagram.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals(attribute1, attributes.get(0));
        assertEquals(attribute2, attributes.get(1));

        // Test setAttributes
        List<AttributeData> newAttributes = new ArrayList<>();
        newAttributes.add(new AttributeData("#", "attribute3", "boolean"));
        classDiagram.setAttributes(newAttributes);

        assertEquals(1, classDiagram.getAttributes().size());
        assertEquals(newAttributes.get(0), classDiagram.getAttributes().get(0));
    }

    @Test
    void testSetAttributesInvalid() {
        ClassDiagram classDiagram = new ClassDiagram();
        assertThrows(IllegalArgumentException.class, () -> classDiagram.setAttributes(null));
    }

    @Test
    void testAddAndSetMethods() {
        ClassDiagram classDiagram = new ClassDiagram();
        MethodData method1 = new MethodData("+", "method1()", "void");
        MethodData method2 = new MethodData("-", "method2()", "int");

        classDiagram.addMethods(method1);
        classDiagram.addMethods(method2);

        List<MethodData> methods = classDiagram.getMethods();
        assertEquals(2, methods.size());
        assertEquals(method1, methods.get(0));
        assertEquals(method2, methods.get(1));

        // Test setMethods
        List<MethodData> newMethods = new ArrayList<>();
        newMethods.add(new MethodData("#", "method3()", "boolean"));
        classDiagram.setMethods(newMethods);

        assertEquals(1, classDiagram.getMethods().size());
        assertEquals(newMethods.get(0), classDiagram.getMethods().get(0));
    }

    @Test
    void testSetMethodsInvalid() {
        ClassDiagram classDiagram = new ClassDiagram();
        assertThrows(IllegalArgumentException.class, () -> classDiagram.setMethods(null));
    }

    @Test
    void testSetAndGetPosition() {
        ClassDiagram classDiagram = new ClassDiagram("PositionTest", 10, 20);

        classDiagram.setPosition(50, 60);
        assertEquals(50, classDiagram.getX());
        assertEquals(60, classDiagram.getY());

        classDiagram.setX(100);
        classDiagram.setY(200);
        assertEquals(100, classDiagram.getX());
        assertEquals(200, classDiagram.getY());
    }

    @Test
    void testRectangleHandling() {
        ClassDiagram classDiagram = new ClassDiagram();
        Rectangle rectangle = new Rectangle(10, 20, 100, 50);
        classDiagram.setRectangle(rectangle);
        assertEquals(rectangle, classDiagram.getRectangle());
    }

}
