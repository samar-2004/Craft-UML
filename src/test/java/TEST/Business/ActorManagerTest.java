package TEST.Business;

import org.example.craftuml.Business.ActorManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.craftuml.models.UseCaseDiagrams.Actor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActorManagerTest {

    private ActorManager actorManager;
    private List<Actor> actors;

    @BeforeEach
    void setUp() {
        // Initialize the actors list and ActorManager before each test
        actors = new ArrayList<>();
        actorManager = new ActorManager(actors);
    }

    @Test
    void testAddActor() {
        // Add a new actor
        actorManager.addActor("Actor 1");

        // Verify the actor is added
        assertEquals(1, actors.size());
        assertEquals("Actor 1", actors.get(0).getName());
    }

    @Test
    void testIsDuplicateActorName() {
        // Add a new actor
        actorManager.addActor("Actor 1");

        // Check for duplicate actor names
        assertTrue(actorManager.isDuplicateActorName("Actor 1"));
        assertFalse(actorManager.isDuplicateActorName("Actor 2"));
    }

    @Test
    void testUpdateActorName() {
        // Add a new actor
        actorManager.addActor("Actor 1");

        // Update actor's name
        Actor actor = actors.get(0);
        actorManager.updateActorName(actor, "Updated Actor");

        // Verify the actor's name is updated
        assertEquals("Updated Actor", actor.getName());
    }

    @Test
    void testIsHoveringOverActor() {
        // Add a new actor
        actorManager.addActor("Actor 1");

        // Check if a point is hovering over the actor
        Actor actor = actors.get(0);
        assertTrue(ActorManager.isHoveringOverActor(actor.getX() + 5, actor.getY() + 5, actors));  // Inside the actor
        assertFalse(ActorManager.isHoveringOverActor(actor.getX() + 100, actor.getY() + 100, actors));  // Outside the actor
    }

    @Test
    void testGetClickedActor() {
        // Add a new actor
        actorManager.addActor("Actor 1");

        // Get the clicked actor
        Actor actor = ActorManager.getClickedActor(actors.get(0).getX() + 5, actors.get(0).getY() + 5, actors);
        assertNotNull(actor);
        assertEquals("Actor 1", actor.getName());

        // Test clicking outside the actor
        actor = ActorManager.getClickedActor(100, 100, actors);
        assertNull(actor);
    }

//    @Test
//    void testDrawActor() {
//        // This test will not run a full drawing, but it can check if drawing calls were made correctly.
//        // For actual drawing tests, you can use a mock of GraphicsContext, but this is a simplified check.
//
//        GraphicsContext mockGc = new MockGraphicsContext();
//
//        // Add a new actor
//        actorManager.addActor("Actor 1");
//
//        // Get the actor
//        Actor actor = actors.get(0);
//
//        // Draw the actor (this will call the method and rely on the mock to verify drawing behavior)
//        actorManager.drawActor(mockGc, actor);
//
//        // We can't assert drawing operations, but you can verify the behavior by checking method calls in the mock (if implemented)
//    }

    @Test
    void testFindActorByPosition() {
        // Add actors
        actorManager.addActor("Actor 1");
        actorManager.addActor("Actor 2");

        // Find actor by position
        Actor actor = actorManager.findActorByPosition(actors.get(0).getX() + 5, actors.get(0).getY() + 5);
        assertNotNull(actor);
        assertEquals("Actor 1", actor.getName());

        // Try a position that doesn't match
        actor = actorManager.findActorByPosition(100, 100);
        assertNull(actor);
    }

    @Test
    void testUpdateActorPosition() {
        // Add a new actor
        actorManager.addActor("Actor 1");

        // Get the actor and update its position
        Actor actor = actors.get(0);
        double initialX = actor.getX();
        double initialY = actor.getY();

        // Simulate a mouse move
        actorManager.updateActorPosition(actor, initialX + 50, initialY + 50, 10, 10, 500, 500);

        // Verify the actor's new position
        assertEquals(initialX + 50 - 10, actor.getX());  // Apply offset
        assertEquals(initialY + 50 - 10, actor.getY());  // Apply offset
    }
}
