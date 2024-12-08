package TEST.models.UseCaseDiagrams;

import org.example.craftuml.models.UseCaseDiagrams.Actor;
import org.example.craftuml.models.UseCaseDiagrams.Association;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.util.List;

class ActorTest {

    private Actor actor;

    @BeforeEach
    void setUp() {
        // Initialize the actor before each test
        actor = new Actor("TestActor");
    }

    @Test
    void testConstructor() {
        // Test if the actor is correctly initialized
        assertEquals("TestActor", actor.getName());
        assertEquals(0, actor.getX(), 0.01);
        assertEquals(0, actor.getY(), 0.01);
        assertEquals(30, actor.getWidth(), 0.01);
        assertEquals(90, actor.getHeight(), 0.01);
    }

    @Test
    void testSetName() {
        actor.setName("NewActor");
        assertEquals("NewActor", actor.getName());

        // Test invalid name
        assertThrows(IllegalArgumentException.class, () -> actor.setName(""));
        assertThrows(IllegalArgumentException.class, () -> actor.setName(null));
    }

    @Test
    void testSetPosition() {
        actor.setPosition(100, 200);
        assertEquals(100, actor.getX(), 0.01);
        assertEquals(200, actor.getY(), 0.01);

        // Test invalid position
        assertThrows(IllegalArgumentException.class, () -> actor.setPosition(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> actor.setPosition(0, -1));
    }
//
//    @Test
//    void testDragging() {
//        // Simulate the press event
//        MouseEvent pressEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED, 0.0, 0.0, 0.0, 0.0,
//                null, 1, false, false, false, false,
//                false, false, false, false, false, null);
//
//        // Simulate the drag event
//        MouseEvent dragEvent = new MouseEvent(MouseEvent.MOUSE_DRAGGED, 100.0, 200.0, 0.0, 0.0,
//                null, 1, false, false, false, false,
//                false, false, false, false, false, null);
//
//        // Simulate mouse press and drag events
//        actor.onMousePressed(pressEvent);
//        actor.onMouseDragged(dragEvent);
//
//        // Assert that the actor moved to the correct position
//        // The actor should move to (100 - width/2, 200 - height/2)
//        assertEquals(70, actor.getX(), 0.01); // x = 100 - width/2
//        assertEquals(155, actor.getY(), 0.01); // y = 200 - height/2
//    }

    @Test
    void testIsClicked() {
        // Actor at (0,0) with default width and height
        assertTrue(actor.isClicked(15, 45)); // Inside the actor's rectangle
        assertFalse(actor.isClicked(100, 100)); // Outside the actor's rectangle
    }

    @Test
    void testGetActorRectangle() {
        // Test if the rectangle is correctly initialized
        Rectangle rectangle = actor.getActorRectangle();
        assertNotNull(rectangle);
        assertEquals(30, rectangle.getWidth(), 0.01);
        assertEquals(90, rectangle.getHeight(), 0.01);
    }

    @Test
    void testAddAssociation() {
        Association association = new Association(); // You can mock or create a simple test association
        actor.addAssociation(association);
        List<Association> associations = actor.getAssociations();
        assertNotNull(associations);
        assertEquals(1, associations.size());
        assertEquals(association, associations.get(0));
    }
}
