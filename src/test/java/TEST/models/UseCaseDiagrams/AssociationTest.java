package TEST.models.UseCaseDiagrams;


import org.example.craftuml.models.UseCaseDiagrams.Association;
import org.example.craftuml.models.UseCaseDiagrams.Actor;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssociationTest {

    private Actor actor;
    private UseCase useCase;
    private Association association;

    @BeforeEach
    void setUp() {
        // Set up test objects before each test
        actor = new Actor("Actor 1");
        useCase = new UseCase("Use Case 1");
        association = new Association(actor, useCase, 100, 200, 300, 400);
    }

    @Test
    void testAssociationCreationWithCoordinates() {
        assertEquals(100, association.getStartX(), "Start X should be 100");
        assertEquals(200, association.getStartY(), "Start Y should be 200");
        assertEquals(300, association.getEndX(), "End X should be 300");
        assertEquals(400, association.getEndY(), "End Y should be 400");
    }

    @Test
    void testAssociationCreationWithoutCoordinates() {
        Association associationWithoutCoords = new Association(actor, useCase);
        assertTrue(Double.isNaN(associationWithoutCoords.getStartX()), "Start X should be NaN");
        assertTrue(Double.isNaN(associationWithoutCoords.getStartY()), "Start Y should be NaN");
        assertTrue(Double.isNaN(associationWithoutCoords.getEndX()), "End X should be NaN");
        assertTrue(Double.isNaN(associationWithoutCoords.getEndY()), "End Y should be NaN");
    }

    @Test
    void testSettersAndGetters() {
        association.setStartX(500);
        association.setStartY(600);
        association.setEndX(700);
        association.setEndY(800);

        assertEquals(500, association.getStartX(), "Start X should be updated to 500");
        assertEquals(600, association.getStartY(), "Start Y should be updated to 600");
        assertEquals(700, association.getEndX(), "End X should be updated to 700");
        assertEquals(800, association.getEndY(), "End Y should be updated to 800");
    }

    @Test
    void testIsNearStartPoint() {
        assertTrue(association.isNear(100, 200), "Point (100, 200) should be near the start point");
        assertFalse(association.isNear(150, 250), "Point (150, 250) should not be near the start point");
    }

    @Test
    void testIsNearEndPoint() {
        assertTrue(association.isNear(300, 400), "Point (300, 400) should be near the end point");
        assertFalse(association.isNear(350, 450), "Point (350, 450) should not be near the end point");
    }

    @Test
    void testToString() {
        String expectedString = "Association: Actor 1 <-> Use Case 1";
        assertEquals(expectedString, association.toString(), "toString should return the correct format");
    }
}
