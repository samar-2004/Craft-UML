package TEST.models.UseCaseDiagrams;


import org.example.craftuml.models.UseCaseDiagrams.Actor;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UseCaseTest {

    private UseCase useCase;
    private Actor actor;

    @BeforeEach
    void setUp() {
        useCase = new UseCase("Login");
        actor = new Actor("User");
    }

    @Test
    void testConstructor() {
        // Test the constructor with the name
        assertEquals("Login", useCase.getName());
        assertEquals(0, useCase.getX(), 0.01); // Default x should be 0
        assertEquals(0, useCase.getY(), 0.01); // Default y should be 0
    }

    @Test
    void testSetGetName() {
        // Test setting and getting name
        useCase.setName("Register");
        assertEquals("Register", useCase.getName());

        // Test invalid name
        assertThrows(IllegalArgumentException.class, () -> useCase.setName(null));
        assertThrows(IllegalArgumentException.class, () -> useCase.setName(""));
    }

    @Test
    void testSetPosition() {
        // Test setting position
        useCase.setPosition(100, 200);
        assertEquals(100, useCase.getX());
        assertEquals(200, useCase.getY());

        // Test invalid position
        assertThrows(IllegalArgumentException.class, () -> useCase.setPosition(-1, 200));
        assertThrows(IllegalArgumentException.class, () -> useCase.setPosition(100, -1));
    }

    @Test
    void testAddAssociation() {
        // Add an actor and verify it is added to the associatedActors list
        useCase.addAssociation(actor);
        assertTrue(useCase.getAssociations().contains(actor));

        // Adding the same actor again should not duplicate it
        useCase.addAssociation(actor);
        assertEquals(1, useCase.getAssociations().size());
    }

    @Test
    void testDefaultWidthAndHeight() {
        // Verify default width and height values
        assertEquals(100, useCase.getWidth());
        assertEquals(50, useCase.getHeight());
    }

    @Test
    void testSetDragOffsets() {
        // Test setting and getting drag offsets
        useCase.setDragOffsetX(10);
        useCase.setDragOffsetY(20);
        assertEquals(10, useCase.getDragOffsetX());
        assertEquals(20, useCase.getDragOffsetY());
    }

    @Test
    void testToString() {
        // Test the toString method
        String expectedString = "UseCase{name='Login', x=0.0, y=0.0}";
        assertEquals(expectedString, useCase.toString());
    }
}

