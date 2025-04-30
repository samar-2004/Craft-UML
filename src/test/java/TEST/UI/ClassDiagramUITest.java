package TEST.UI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.example.craftuml.HelloApplication;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.UI.classDiagramUI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClassDiagramUITest {

    private Canvas canvas;
    private ClassDiagram classDiagram;
    private VBox vbox;
    private Button okButton;
    private classDiagramUI classDiagramUI;

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
        classDiagram = new ClassDiagram();
        canvas = new Canvas();
        vbox = new VBox();
        okButton = new Button();
        classDiagramUI = new classDiagramUI(canvas, classDiagram, null);

        Platform.runLater(() -> when(mockAlert.showAndWait()).thenReturn(null));
    }

    @Test
    void testAddAttributeField() {
        Platform.runLater(() -> {
            int initialCount = vbox.getChildren().size();
            classDiagramUI.addAttributeField(vbox, null);
            assertEquals(initialCount + 1, vbox.getChildren().size(), "Attribute field should be added.");
        });
    }


    @Test
    void testAddMethodField() {
        Platform.runLater(() -> {
            int initialCount = vbox.getChildren().size();
            classDiagramUI.addMethodField(vbox, null, okButton);
            assertEquals(initialCount + 1, vbox.getChildren().size(), "Method field should be added.");
        });
    }

    @Test
    void testValidateMethodName_validName() {
        Platform.runLater(() -> {
            String validMethodName = "myMethod()";
            boolean result = classDiagramUI.validateMethodName(validMethodName);
            assertTrue(result, "Method name should be valid.");
        });
    }

    @Test
    void testValidateMethodName_invalidName() {
        Platform.runLater(() -> {
            String invalidMethodName = "()invalidMethod";
            boolean result = classDiagramUI.validateMethodName(invalidMethodName);
            assertFalse(result, "Method name should be invalid.");
        });
    }

    @Test
    void testAreFieldsFilled_allFieldsValid() {
        Platform.runLater(() -> {
            vbox.getChildren().add(mock(javafx.scene.Node.class));
            boolean result = classDiagramUI.areFieldsFilled(vbox);
            assertTrue(result, "All fields should be filled.");
        });
    }

    @Test
    void testShowClassDiagramDialog_validInput() {
        Platform.runLater(() -> {
            classDiagram.setName("MyClass");
            ClassDiagram returnedDiagram = classDiagramUI.showClassDiagramDialog();
            assertNotNull(returnedDiagram, "Class diagram should be returned.");
            assertEquals("MyClass", returnedDiagram.getName(), "Class diagram name should match.");
        });
    }

    @Test
    void testShowClassDiagramDialog_invalidInput() {
        Platform.runLater(() -> {
            classDiagram.setName("");
            ClassDiagram returnedDiagram = classDiagramUI.showClassDiagramDialog();
            assertNull(returnedDiagram, "Class diagram should not be created with invalid input.");
        });
    }

    @Test
    void testErrorHandling_showError() {
        Platform.runLater(() -> {
            classDiagramUI.showError("Test Error");
            verify(mockAlert, times(1)).showAndWait();
        });
    }

    @Test
    void testButtonDisabling_invalidMethod() {
        Platform.runLater(() -> {
            when(okButton.isDisable()).thenReturn(true);
            classDiagramUI.addMethodField(vbox, null, okButton);
            assertTrue(okButton.isDisable(), "OK button should be disabled for invalid input.");
        });
    }
}
