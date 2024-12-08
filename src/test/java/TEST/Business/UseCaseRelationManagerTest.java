package TEST.Business;

import org.example.craftuml.Business.UseCaseRelationManager;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseToUseCaseRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UseCaseRelationManagerTest {

    private UseCaseRelationManager relationManager;
    private List<UseCaseToUseCaseRelation> includeRelations;
    private List<UseCaseToUseCaseRelation> extendRelations;

    private UseCase useCase1;
    private UseCase useCase2;
    private UseCase useCase3;

    @BeforeEach
    void setUp() {
        includeRelations = new ArrayList<>();
        extendRelations = new ArrayList<>();
        relationManager = new UseCaseRelationManager(includeRelations, extendRelations);

        useCase1 = new UseCase("Use Case 1");
        useCase2 = new UseCase("Use Case 2");
        useCase3 = new UseCase("Use Case 3");
    }

    @Test
    void testAddIncludeRelation() {
        assertTrue(relationManager.addIncludeRelation(useCase1, useCase2));
        assertEquals(1, includeRelations.size());

        UseCaseToUseCaseRelation relation = includeRelations.get(0);
        assertEquals("include", relation.getRelationType());
        assertEquals(useCase1, relation.getUseCase1());
        assertEquals(useCase2, relation.getUseCase2());
    }

    @Test
    void testAddIncludeRelationAlreadyExists() {
        relationManager.addIncludeRelation(useCase1, useCase2);
        assertFalse(relationManager.addIncludeRelation(useCase1, useCase2));
        assertEquals(1, includeRelations.size());
    }

    @Test
    void testAddExtendRelation() {
        assertTrue(relationManager.addExtendRelation(useCase2, useCase3));
        assertEquals(1, extendRelations.size());

        UseCaseToUseCaseRelation relation = extendRelations.get(0);
        assertEquals("extend", relation.getRelationType());
        assertEquals(useCase2, relation.getUseCase1());
        assertEquals(useCase3, relation.getUseCase2());
    }

    @Test
    void testAddExtendRelationAlreadyExists() {
        relationManager.addExtendRelation(useCase2, useCase3);
        assertFalse(relationManager.addExtendRelation(useCase2, useCase3));
        assertEquals(1, extendRelations.size());
    }

    @Test
    void testHasIncludeRelation() {
        relationManager.addIncludeRelation(useCase1, useCase2);
        assertTrue(relationManager.hasIncludeRelation(useCase1, useCase2));
        assertFalse(relationManager.hasIncludeRelation(useCase1, useCase3));
    }

    @Test
    void testHasExtendRelation() {
        relationManager.addExtendRelation(useCase2, useCase3);
        assertTrue(relationManager.hasExtendRelation(useCase2, useCase3));
        assertFalse(relationManager.hasExtendRelation(useCase1, useCase3));
    }

    @Test
    void testCreateRelationInclude() {
        assertTrue(relationManager.createRelation(useCase1, useCase2, "include"));
        assertEquals(1, includeRelations.size());
        assertEquals(0, extendRelations.size());
    }

    @Test
    void testCreateRelationExtend() {
        assertTrue(relationManager.createRelation(useCase2, useCase3, "extend"));
        assertEquals(0, includeRelations.size());
        assertEquals(1, extendRelations.size());
    }

    @Test
    void testCreateRelationInvalidType() {
        assertFalse(relationManager.createRelation(useCase1, useCase2, "invalid"));
        assertEquals(0, includeRelations.size());
        assertEquals(0, extendRelations.size());
    }

    @Test
    void testCreateSelfRelation() {
        assertFalse(relationManager.createRelation(useCase1, useCase1, "include"));
        assertEquals(0, includeRelations.size());
        assertEquals(0, extendRelations.size());
    }
}
