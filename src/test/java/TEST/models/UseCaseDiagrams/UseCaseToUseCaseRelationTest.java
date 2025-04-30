package TEST.models.UseCaseDiagrams;

import javafx.scene.text.Text;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseToUseCaseRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UseCaseToUseCaseRelationTest {

    private UseCase useCase1;
    private UseCase useCase2;
    private UseCaseToUseCaseRelation relation;

    @BeforeEach
    void setUp() {
        // Initialize the UseCase objects
        useCase1 = new UseCase("Login");
        useCase2 = new UseCase("Register");

        // Initialize the relation object with useCase1, useCase2 and a relation type
        relation = new UseCaseToUseCaseRelation(useCase1, useCase2, "include");
    }

    @Test
    void testConstructor() {
        // Test the constructor to ensure values are correctly set
        assertEquals("Login", relation.getUseCase1().getName());
        assertEquals("Register", relation.getUseCase2().getName());
        assertEquals("include", relation.getRelationType());
    }

    @Test
    void testGettersAndSetters() {
        // Test setters and getters to ensure they work as expected
        relation.setRelationType("extend");
        assertEquals("extend", relation.getRelationType());

        UseCase newUseCase = new UseCase("Logout");
        relation.setUseCase1(newUseCase);
        assertEquals("Logout", relation.getUseCase1().getName());
    }


    @Test
    void testAddRelation() {
        // Test the addRelation method by passing a new relation type as a Text object
        Text relationText = new Text("include");
        relation.addRelation(relationText);
        assertEquals("include", relation.getRelationType());

        // Also test adding a different relation type as a Text object
        Text relationText2 = new Text("generalization");
        relation.addRelation(relationText2);
        assertEquals("generalization", relation.getRelationType());
    }


    @Test
    void testInequality() {
        // Test that relations are not equal if they have different relation types
        UseCaseToUseCaseRelation relation1 = new UseCaseToUseCaseRelation(useCase1, useCase2, "include");
        UseCaseToUseCaseRelation relation2 = new UseCaseToUseCaseRelation(useCase1, useCase2, "extend");

        assertNotEquals(relation1, relation2);  // They should not be equal due to different relation type
    }
}
