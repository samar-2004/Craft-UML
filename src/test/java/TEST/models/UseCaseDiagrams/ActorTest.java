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


    @Test
    void testIsClicked() {
        Actor actor = new Actor("TestActor");
        actor.setX(50);  // Set x-coordinate for the actor
        actor.setY(100); // Set y-coordinate for the actor

        // Simulate a click inside the actor's rectangle (within bounds)
        double mouseX = 55; // Inside the actor's rectangle
        double mouseY = 120; // Inside the actor's rectangle

        // Assert that the click is inside the actor's rectangle
        assertTrue(actor.isClicked(mouseX, mouseY), "The actor should have been clicked");// Outside the actor's rectangle
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
