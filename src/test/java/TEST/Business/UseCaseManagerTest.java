package TEST.Business;


import org.example.craftuml.Business.UseCaseManager;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseDiagram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UseCaseManagerTest {

    private UseCaseManager useCaseManager;
    private UseCaseDiagram activeDiagram;

    @BeforeEach
    void setUp() {
        List<UseCase> useCases = new ArrayList<>();
        useCaseManager = new UseCaseManager(useCases);

        // Setup an active diagram for boundary checks
        activeDiagram = new UseCaseDiagram();
        activeDiagram.setPosition(0, 0);
        activeDiagram.setWidth(500);
        activeDiagram.setHeight(500);
    }

    @Test
    void testAddUseCase_Success() {
        UseCase useCase = useCaseManager.addUseCase("Login", 100, 100, activeDiagram);

        assertNotNull(useCase);
        assertEquals("Login", useCase.getName());
        assertEquals(100, useCase.getX());
        assertEquals(100, useCase.getY());
        assertTrue(useCaseManager.getUseCases().contains(useCase));
    }

    @Test
    void testAddUseCase_DuplicateName() {
        useCaseManager.addUseCase("Login", 100, 100, activeDiagram);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                useCaseManager.addUseCase("Login", 150, 150, activeDiagram)
        );
        assertEquals("A use case with this name already exists.", exception.getMessage());
    }

    @Test
    void testAddUseCase_OutOfBounds() {
        UseCase useCase = useCaseManager.addUseCase("Register", 600, 600, activeDiagram);

        assertNotNull(useCase);
        assertEquals("Register", useCase.getName());
        assertEquals(activeDiagram.getWidth() - UseCase.DEFAULT_WIDTH, useCase.getX());
        assertEquals(activeDiagram.getHeight() - UseCase.DEFAULT_HEIGHT, useCase.getY());
    }

    @Test
    void testEditUseCaseName_Success() {
        UseCase useCase = useCaseManager.addUseCase("Login", 100, 100, activeDiagram);
        useCaseManager.editUseCaseName(useCase, "SignIn");

        assertEquals("SignIn", useCase.getName());
    }

    @Test
    void testEditUseCaseName_DuplicateName() {
        useCaseManager.addUseCase("Login", 100, 100, activeDiagram);
        UseCase useCase2 = useCaseManager.addUseCase("Register", 150, 150, activeDiagram);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                useCaseManager.editUseCaseName(useCase2, "Login")
        );
        assertEquals("A use case with this name already exists.", exception.getMessage());
    }

    @Test
    void testFindClickedUseCase() {
        UseCase useCase = useCaseManager.addUseCase("Login", 100, 100, activeDiagram);
        UseCase foundUseCase = useCaseManager.findClickedUseCase(110, 110);

        assertNotNull(foundUseCase);
        assertEquals(useCase, foundUseCase);
    }

    @Test
    void testFindClickedUseCase_NotFound() {
        useCaseManager.addUseCase("Login", 100, 100, activeDiagram);
        UseCase foundUseCase = useCaseManager.findClickedUseCase(500, 500);

        assertNull(foundUseCase);
    }

    @Test
    void testUpdateUseCasePosition() {
        UseCase useCase = useCaseManager.addUseCase("Login", 100, 100, activeDiagram);

        UseCaseManager.updateUseCasePosition(useCase, 450, 450, 10, 10, activeDiagram);

        assertEquals(400, useCase.getX());
        assertEquals(440, useCase.getY());
    }

    @Test
    void testUpdateUseCasePosition_OutOfBounds() {
        UseCase useCase = useCaseManager.addUseCase("Login", 100, 100, activeDiagram);

        UseCaseManager.updateUseCasePosition(useCase, 600, 600, 10, 10, activeDiagram);

        assertEquals(activeDiagram.getWidth() - UseCase.DEFAULT_WIDTH, useCase.getX());
        assertEquals(activeDiagram.getHeight() - UseCase.DEFAULT_HEIGHT, useCase.getY());
    }

    @Test
    void testIsHoveringOverUseCase() {
        useCaseManager.addUseCase("Login", 100, 100, activeDiagram);

        boolean isHovering = UseCaseManager.isHoveringOverUseCase(110, 110, useCaseManager.getUseCases());

        assertTrue(isHovering);
    }

    @Test
    void testIsHoveringOverUseCase_NotHovering() {
        useCaseManager.addUseCase("Login", 100, 100, activeDiagram);

        boolean isHovering = UseCaseManager.isHoveringOverUseCase(500, 500, useCaseManager.getUseCases());

        assertFalse(isHovering);
    }
}

