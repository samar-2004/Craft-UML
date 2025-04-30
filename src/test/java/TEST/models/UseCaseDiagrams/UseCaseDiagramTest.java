package TEST.models.UseCaseDiagrams;


import org.example.craftuml.models.UseCaseDiagrams.Actor;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseDiagram;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseToUseCaseRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class UseCaseDiagramTest {

    private UseCaseDiagram diagram;
    private Actor actor1;
    private Actor actor2;
    private UseCase useCase1;
    private UseCase useCase2;

    @BeforeEach
    public void setUp() {
        diagram = new UseCaseDiagram("Test Diagram", 10, 20);
        actor1 = new Actor("Actor1");
        actor2 = new Actor("Actor2");
        useCase1 = new UseCase("UseCase1");
        useCase2 = new UseCase("UseCase2");
    }

    @Test
    public void testAddUseCase() {
        diagram.addUseCase(useCase1);
        assertTrue(diagram.getUseCases().contains(useCase1));
    }

    @Test
    public void testRemoveUseCase() {
        diagram.addUseCase(useCase1);
        diagram.removeUseCase(useCase1);
        assertFalse(diagram.getUseCases().contains(useCase1));
    }

    @Test
    public void testAddActor() {
        diagram.addActor("Actor1");
        assertEquals(1, diagram.getActors().size());
        assertEquals("Actor1", diagram.getActors().get(0).getName());
    }

    @Test
    public void testRemoveActor() {
        diagram.addActor("Actor1");
        diagram.removeActor("Actor1");
        assertEquals(0, diagram.getActors().size());
    }

    @Test
    public void testAddUseCaseRelation() {
        diagram.addUseCase(useCase1);
        diagram.addUseCase(useCase2);
        diagram.addUseCaseRelation(useCase1, useCase2, "extends");

        List<UseCaseToUseCaseRelation> relations = diagram.getUseCaseRelations();
        assertEquals(1, relations.size());
        assertEquals("extends", relations.get(0).getRelationType());
    }

    @Test
    public void testRemoveUseCaseRelation() {
        diagram.addUseCase(useCase1);
        diagram.addUseCase(useCase2);
        diagram.addUseCaseRelation(useCase1, useCase2, "extends");

        diagram.removeUseCaseRelation(useCase1, useCase2, "extends");
        assertEquals(0, diagram.getUseCaseRelations().size());
    }

    @Test
    public void testGetWidth() {
        assertEquals(700, diagram.getWidth());
        diagram.setWidth(800);
        assertEquals(800, diagram.getWidth());
    }

    @Test
    public void testGetHeight() {
        assertEquals(700, diagram.getHeight());
        diagram.setHeight(800);
        assertEquals(800, diagram.getHeight());
    }

    @Test
    public void testIsUseCaseDiagram() {
        assertTrue(diagram.isUseCaseDiagram());
    }

}
