package TEST.models;

import static org.junit.jupiter.api.Assertions.*;

import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.util.List;


public class RelationshipTest {

    private ClassDiagram sourceClass;
    private ClassDiagram targetClass;
    private List<Rectangle> obstacles;

    @BeforeEach
    public void setup() {
        sourceClass = new ClassDiagram("Class1", 0, 100);  // Example dimensions
        targetClass = new ClassDiagram("Class2", 100, 100);  // Example dimensions
        obstacles = new ArrayList<>();
        obstacles.add(new Rectangle(50, 50, 30, 30));  // Example obstacle
    }

    @Test
    public void testRelationshipConstructor() {
        Relationship relationship = new Relationship(
                sourceClass,
                targetClass,
                "association",
                "1",
                "0..*",
                obstacles,
                "Association"
        );

        assertNotNull(relationship);
        assertEquals("association", relationship.getRelationType());
        assertEquals("1", relationship.getSourceClassMultiplicity());
        assertEquals("0..*", relationship.getTargetClassMultiplicity());
        assertEquals("Association", relationship.getRelationName());
    }

    @Test
    public void testSetAndGetRelationType() {
        Relationship relationship = new Relationship(
                sourceClass,
                targetClass,
                "association",
                "1",
                "0..*",
                obstacles,
                "Association"
        );

        relationship.setRelationType("composition");
        assertEquals("composition", relationship.getRelationType());
    }

    @Test
    public void testDrawAssociation() {
        Relationship relationship = new Relationship(
                sourceClass,
                targetClass,
                "association",
                "1",
                "0..*",
                obstacles,
                "Association"
        );

        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        relationship.draw(gc);

        assertTrue(true);
    }

    @Test
    public void testDrawRealization() {
        InterfaceData targetInterface = new InterfaceData();
        Relationship relationship = new Relationship(
                sourceClass,
                targetInterface,
                "realization",
                "1",
                "1",
                obstacles
        );

        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        relationship.drawRealization(gc);

        assertTrue(true);
    }
    @Test
    public void testDrawGeneralization() {
        Relationship relationship = new Relationship(
                sourceClass,
                targetClass,
                "generalization",
                "1",
                "1",
                obstacles,
                "Generalization"
        );

        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        relationship.drawGeneralization(gc);

        assertTrue(true);
    }

    @Test
    public void testDrawSelfAssociation() {
        sourceClass.setX(100);
        sourceClass.setY(100);
        sourceClass.setWidth(100);
        sourceClass.setHeight(100);

        Relationship relationship = new Relationship(
                sourceClass,
                sourceClass,
                "association",
                "1",
                "1",
                obstacles,
                "Self Association"
        );

        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        relationship.draw(gc);

        assertTrue(true);
    }

    @Test
    public void testDrawMultiplicity() {
        Relationship relationship = new Relationship(
                sourceClass,
                targetClass,
                "association",
                "1",
                "0..*",
                obstacles,
                "Association"
        );

        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        relationship.drawMultiplicity(
                gc,
                sourceClass.getX(),
                sourceClass.getY(),
                "1",
                targetClass.getX(),
                targetClass.getY(),
                "0..*",
                0, 0, 100, 100, 200, 200, 300, 300
        );

        assertTrue(true);
    }
    @Test
    public void testDrawEmptyArrowhead() {
        Relationship relationship = new Relationship(
                sourceClass,
                targetClass,
                "association",
                "1",
                "0..*",
                obstacles,
                "Association"
        );

        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        relationship.drawEmptyArrowhead(gc, 100, 100, 200, 200);

        assertTrue(true);
    }
    @Test
    void testCalculateOrthogonalBorderIntersection() {
        Relationship relationship = new Relationship(sourceClass,
                targetClass,
                "association",
                "1",
                "0..*",
                obstacles,
                "Association");

        double rectX = 50, rectY = 50, rectWidth = 100, rectHeight = 100;
        double sourceX = 75, sourceY = 75;
        double targetX = 200, targetY = 200;

        double[] result = relationship.calculateOrthogonalBorderIntersection(
                rectX, rectY, rectWidth, rectHeight, sourceX, sourceY, targetX, targetY
        );

        assertNotNull(result);
        assertEquals(4, result.length);
    }




}

