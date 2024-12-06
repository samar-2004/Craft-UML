package TEST.UI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.example.craftuml.HelloApplication;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.ClassDiagrams.MethodData;
import org.example.craftuml.UI.InterfaceDiagramUI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InterfaceDiagramUITest {

    private Canvas canvas;
    private InterfaceData interfaceDiagram;
    private InterfaceDiagramUI interfaceDiagramUI;

    @Mock
    private Alert mockAlert;

    @BeforeAll
    static void initializeJavaFX() {
        if (!Platform.isFxApplicationThread()) {
            Application.launch(HelloApplication.class);
        }
    }

    @BeforeEach
    void setUp() {
        interfaceDiagram = new InterfaceData();
        canvas = new Canvas();
        interfaceDiagramUI = new InterfaceDiagramUI(canvas, interfaceDiagram, null);

        Platform.runLater(() -> when(mockAlert.showAndWait()).thenReturn(null));
    }

    @Test
    void testConstructor_nullCanvas() {
        assertThrows(IllegalArgumentException.class, () -> new InterfaceDiagramUI(null, interfaceDiagram, null));
    }

    @Test
    void testConstructor_withInterfaceData() {
        InterfaceDiagramUI ui = new InterfaceDiagramUI(canvas, interfaceDiagram, null);
        assertNotNull(ui);
    }

    @Test
    void testValidateInterfaceName_valid() {
        Platform.runLater(() -> {
            String validName = "MyInterface";
            TextField classNameField = new TextField(validName);
            boolean result = interfaceDiagramUI.validateInterfaceName(validName, classNameField);
            assertTrue(result, "Interface name should be valid.");
        });
    }

    @Test
    void testValidateInterfaceName_invalid() {
        Platform.runLater(() -> {
            String invalidName = "Invalid Name ";
            TextField classNameField = new TextField(invalidName);
            boolean result = interfaceDiagramUI.validateInterfaceName(invalidName, classNameField);
            assertFalse(result, "Interface name should be invalid.");
        });
    }

    @Test
    void testAddMethodField() {
        Platform.runLater(() -> {
            VBox methodsVBox = new VBox();
            interfaceDiagramUI.addMethodField(methodsVBox, null);
            assertEquals(1, methodsVBox.getChildren().size(), "Method field should be added.");
        });
    }

    @Test
    void testAreFieldsFilled_allFieldsValid() {
        Platform.runLater(() -> {
            VBox methodsVBox = new VBox();
            ComboBox<String> accessCombo = new ComboBox<>();
            accessCombo.setValue("+");
            TextField nameField = new TextField("methodName");
            TextField returnTypeField = new TextField("void");

            VBox methodVBox = new VBox();
            methodVBox.getChildren().addAll(accessCombo, nameField, returnTypeField);
            methodsVBox.getChildren().add(methodVBox);

            boolean result = interfaceDiagramUI.areFieldsFilled(methodsVBox);
            assertTrue(result, "All method fields should be filled.");
        });
    }

    @Test
    void testAreFieldsFilled_someFieldsInvalid() {
        Platform.runLater(() -> {
            VBox methodsVBox = new VBox();
            ComboBox<String> accessCombo = new ComboBox<>();
            accessCombo.setValue("+");
            TextField nameField = new TextField("");
            TextField returnTypeField = new TextField("void");

            VBox methodVBox = new VBox();
            methodVBox.getChildren().addAll(accessCombo, nameField, returnTypeField);
            methodsVBox.getChildren().add(methodVBox);

            boolean result = interfaceDiagramUI.areFieldsFilled(methodsVBox);
            assertFalse(result, "Fields should not be considered filled.");
        });
    }

    @Test
    void testShowError() {
        Platform.runLater(() -> {
            interfaceDiagramUI.showError("Test Error");
            verify(mockAlert, times(1)).showAndWait();
        });
    }

    @Test
    void testShowInterfaceDiagramDialog_validInput() {
        Platform.runLater(() -> {
            interfaceDiagram.setName("MyInterface");
            InterfaceData returnedInterface = interfaceDiagramUI.showInterfaceDiagramDialog();
            assertNotNull(returnedInterface, "Interface diagram should be returned.");
            assertEquals("MyInterface", returnedInterface.getName(), "Interface name should match.");
        });
    }

    @Test
    void testShowInterfaceDiagramDialog_invalidInput() {
        Platform.runLater(() -> {
            interfaceDiagram.setName("");
            InterfaceData returnedInterface = interfaceDiagramUI.showInterfaceDiagramDialog();
            assertNull(returnedInterface, "Interface diagram should not be created with invalid input.");
        });
    }
}
