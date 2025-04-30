package TEST.models.ClassDiagrams;

import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.ClassDiagrams.MethodData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class InterfaceDataTest {

    @Test
    void testDefaultConstructor() {
        InterfaceData interfaceData = new InterfaceData();

        assertNotNull(interfaceData.getMethods(), "Methods list should not be null");
        assertTrue(interfaceData.getMethods().isEmpty(), "Methods list should be empty by default");
    }

    @Test
    void testSetNameValid() {
        InterfaceData interfaceData = new InterfaceData();
        interfaceData.setName("ValidInterfaceName");

        assertEquals("ValidInterfaceName", interfaceData.getName(), "Name should match the valid input");
    }

    @Test
    void testSetNameInvalid() {
        InterfaceData interfaceData = new InterfaceData();

        assertThrows(IllegalArgumentException.class, () -> interfaceData.setName(null));
        assertThrows(IllegalArgumentException.class, () -> interfaceData.setName(""));
        assertThrows(IllegalArgumentException.class, () -> interfaceData.setName("Invalid Name"));
    }

    @Test
    void testSetPosition() {
        InterfaceData interfaceData = new InterfaceData();

        interfaceData.setPosition(100.5, 200.5);
        assertEquals(100.5, interfaceData.getX(), "X position should be set correctly");
        assertEquals(200.5, interfaceData.getY(), "Y position should be set correctly");

        interfaceData.setX(150.0);
        interfaceData.setY(250.0);
        assertEquals(150.0, interfaceData.getX(), "X position should match the new value");
        assertEquals(250.0, interfaceData.getY(), "Y position should match the new value");
    }

    @Test
    void testSetMethods() {
        InterfaceData interfaceData = new InterfaceData();
        List<MethodData> methods = new ArrayList<>();
        MethodData method1 = new MethodData("+", "method1()", "void");
        MethodData method2 = new MethodData("-", "method2()", "int");

        methods.add(method1);
        methods.add(method2);

        interfaceData.setMethods(methods);
        List<MethodData> retrievedMethods = interfaceData.getMethods();

        assertEquals(2, retrievedMethods.size(), "Methods list size should match the input list");
        assertEquals(method1, retrievedMethods.get(0), "First method should match the input");
        assertEquals(method2, retrievedMethods.get(1), "Second method should match the input");
    }

    @Test
    void testSetDimensions() {
        InterfaceData interfaceData = new InterfaceData();

        interfaceData.setWidth(300.0);
        interfaceData.setHeight(400.0);

        assertEquals(300.0, interfaceData.getWidth(), "Width should match the set value");
        assertEquals(400.0, interfaceData.getHeight(), "Height should match the set value");
    }
}
