package TEST.Business;

import javafx.embed.swing.JFXPanel;
import javafx.scene.canvas.Canvas;
import org.example.craftuml.Business.AssociationManager;
import org.example.craftuml.models.UseCaseDiagrams.Actor;
import org.example.craftuml.models.UseCaseDiagrams.Association;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AssociationManagerTest {

    private static List<Actor> actors;
    private static List<UseCase> useCases;
    private static List<Association> associations;
    private static AssociationManager associationManager;

    @BeforeAll
    static void setUpClass() {
        // Initialize JavaFX environment
        new JFXPanel();
    }

    @BeforeEach
    void setUp() {
        // Set up actors, use cases, and associations before each test
        actors = new ArrayList<>();
        useCases = new ArrayList<>();
        associations = new ArrayList<>();

        // Add some test actors and use cases
        Actor actor1 = new Actor("Actor 1");
        Actor actor2 = new Actor("Actor 2");
        actors.add(actor1);
        actors.add(actor2);

        UseCase useCase1 = new UseCase("Use Case 1");
        UseCase useCase2 = new UseCase("Use Case 2");
        useCases.add(useCase1);
        useCases.add(useCase2);

        // Initialize AssociationManager
        associationManager = new AssociationManager(useCases, actors);
    }

    @Test
    void testCreateAssociation_Successful() {
        Actor actor = actors.get(0);
        UseCase useCase = useCases.get(0);

        boolean created = associationManager.createAssociation(useCase, actor, associations);

        assertTrue(created, "Association should be created successfully");
        assertEquals(1, associations.size(), "Associations list should have one association");
        assertTrue(useCase.getAssociations().contains(actor), "Use case should contain the associated actor");
    }

    @Test
    void testCreateAssociation_AlreadyExists() {
        Actor actor = actors.get(0);
        UseCase useCase = useCases.get(0);

        // Create the first association
        associationManager.createAssociation(useCase, actor, associations);

        // Attempt to create the same association again
        boolean created = associationManager.createAssociation(useCase, actor, associations);

        assertFalse(created, "Duplicate association should not be created");
        assertEquals(1, associations.size(), "Associations list should still have one association");
    }

    @Test
    void testIsUseCaseAssociated() {
        Actor actor = actors.get(0);
        UseCase useCase = useCases.get(0);

        // Initially, no association exists
        assertFalse(associationManager.isUseCaseAssociated(useCase, actor), "Use case should not be associated with the actor initially");

        // Create an association
        associationManager.createAssociation(useCase, actor, associations);

        // Verify the association exists
        assertTrue(associationManager.isUseCaseAssociated(useCase, actor), "Use case should be associated with the actor after creation");
    }

    @Test
    void testDrawAssociationLine() {
        Actor actor = actors.get(0);
        UseCase useCase = useCases.get(0);

        // Set positions for the actor and use case
        actor.setPosition(100, 100);
        useCase.setPosition(200, 200);

        // Create a canvas for drawing
        Canvas canvas = new Canvas(400, 400);

        // Draw the association line
        associationManager.drawAssociationLine(actor, useCase, canvas);

        // Verify that no exceptions were thrown during the drawing
        assertNotNull(canvas.getGraphicsContext2D(), "GraphicsContext should not be null after drawing");
    }
}
