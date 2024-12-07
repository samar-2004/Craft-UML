package TEST.models;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.craftuml.HelloApplication;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.Relationship;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class RelationshipTest {

    private ClassDiagram sourceClass;
    private ClassDiagram targetClass;
    private InterfaceData targetInterface;
    private GraphicsContext graphicsContext;
    private Rectangle obstacle;

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
        // Mock the objects
        sourceClass = mock(ClassDiagram.class);
        targetClass = mock(ClassDiagram.class);
        targetInterface = mock(InterfaceData.class);
        graphicsContext = mock(GraphicsContext.class);
        obstacle = new Rectangle(0, 0, 100, 100);

        // Mock values for sourceClass and targetClass as needed
        when(sourceClass.getX()).thenReturn(100.0);
        when(sourceClass.getY()).thenReturn(100.0);
        when(targetClass.getX()).thenReturn(200.0);
        when(targetClass.getY()).thenReturn(200.0);
        when(sourceClass.getWidth()).thenReturn(50.0);
        when(sourceClass.getHeight()).thenReturn(50.0);
        when(targetClass.getWidth()).thenReturn(50.0);
        when(targetClass.getHeight()).thenReturn(50.0);

        // Mock Alert behavior
        when(mockAlert.showAndWait()).thenReturn(null);
    }

    @Test
    void testRelationshipConstructorWithClassDiagram() {
        // Arrange
        String type = "association";
        String sourceMultiplicity = "1";
        String targetMultiplicity = "1";
        String relationName = "Test Relation";

        // Act
        Relationship relationship = new Relationship(sourceClass, targetClass, type, sourceMultiplicity, targetMultiplicity, Arrays.asList(obstacle), relationName);

        // Assert
        assertNotNull(relationship);
        assertEquals("Test Relation", relationship.getRelationType());
        assertEquals(sourceClass, relationship.getSourceClass());
        assertEquals(targetClass, relationship.getTargetClass());
        assertEquals(type, relationship.getType());
        assertEquals(sourceMultiplicity, relationship.getSourceClassMultiplicity());
        assertEquals(targetMultiplicity, relationship.getTargetClassMultiplicity());
    }

    @Test
    void testRelationshipConstructorWithInterfaceData() {
        // Arrange
        String type = "realization";
        String sourceMultiplicity = "0..1";
        String targetMultiplicity = "1";

        // Act
        Relationship relationship = new Relationship(sourceClass, targetInterface, type, sourceMultiplicity, targetMultiplicity, Arrays.asList(obstacle));

        // Assert
        assertNotNull(relationship);
        assertEquals(type, relationship.getType());
        assertEquals(sourceMultiplicity, relationship.getSourceClassMultiplicity());
        assertEquals(targetMultiplicity, relationship.getTargetClassMultiplicity());
    }

    @Test
    void testSetRelationType() {
        // Arrange
        Relationship relationship = new Relationship(sourceClass, targetClass, "association", "1", "1", Arrays.asList(obstacle), "Test Relation");

        // Act
        relationship.setRelationType("composition");

        // Assert
        assertEquals("composition", relationship.getRelationType());
    }

    @Test
    void testDraw() {
        // Arrange
        Relationship relationship = new Relationship(sourceClass, targetClass, "association", "1", "1", Arrays.asList(obstacle), "Test Relation");

        // Act
        relationship.draw(graphicsContext);

        // Assert
        verify(graphicsContext, times(1)).setLineWidth(2);
        verify(graphicsContext, times(1)).setStroke(Color.BLACK);
        verify(graphicsContext, times(1)).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void testDrawSelfAssociation() {
        // Arrange
        Relationship relationship = new Relationship(sourceClass, targetClass, "association", "1", "1", Arrays.asList(obstacle), "Self Association");

        // Act
        relationship.draw(graphicsContext);

        // Assert
        verify(graphicsContext, times(1)).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void testCalculateOrthogonalBorderIntersection() {
        // Arrange
        Relationship relationship = new Relationship(sourceClass, targetClass, "association", "1", "1", Arrays.asList(obstacle), "Test Relation");

        double rectX = 100.0;
        double rectY = 100.0;
        double rectWidth = 50.0;
        double rectHeight = 50.0;
        double sourceX = 200.0;
        double sourceY = 200.0;
        double targetX = 250.0;
        double targetY = 250.0;

        // Act
        double[] result = relationship.calculateOrthogonalBorderIntersection(rectX, rectY, rectWidth, rectHeight, sourceX, sourceY, targetX, targetY);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.length);
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        Relationship relationship = new Relationship(sourceClass, targetClass, "association", "1", "1", Arrays.asList(obstacle), "Test Relation");

        // Act & Assert
        relationship.setRelationName("New Relation");
        assertEquals("New Relation", relationship.getRelationName());

        relationship.setSourceMultiplicity("0..1");
        assertEquals("0..1", relationship.getSourceClassMultiplicity());

        relationship.setTargetMultiplicity("1..*");
        assertEquals("1..*", relationship.getTargetClassMultiplicity());
    }
}
