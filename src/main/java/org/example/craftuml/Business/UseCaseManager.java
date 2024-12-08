package org.example.craftuml.Business;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseDiagram;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of `UseCase` objects within a `UseCaseDiagram`.
 * Provides functionality for adding, editing, drawing, and detecting interactions with use cases in the diagram.
 */

public class UseCaseManager {

    /**
     * A list of `UseCase` objects representing the use cases in the diagram.
     * Used to store and manage all the use cases.
     */
    private List<UseCase> useCases = new ArrayList<>();

    /**
     * Constructor to initialize the `UseCaseManager` with a pre-existing list of use cases.
     *
     * @param useCases A list of `UseCase` objects to initialize the manager.
     */
    public UseCaseManager(List<UseCase> useCases) {
        this.useCases = useCases;
    }

    /**
     * Checks if a use case name already exists in the list of use cases.
     *
     * @param name The name to check for duplicates.
     * @return `true` if a use case with the specified name exists, `false` otherwise.
     */
    public boolean isDuplicateName(String name) {
        return useCases.stream().anyMatch(useCase -> useCase.getName().equalsIgnoreCase(name));
    }

    /**
     * Adds a new use case to the diagram with the specified name and position.
     * The method ensures that the use case is not out of bounds and does not duplicate an existing name.
     *
     * @param name The name of the new use case.
     * @param x The x-coordinate of the new use case's position.
     * @param y The y-coordinate of the new use case's position.
     * @param activeDiagram The active diagram in which the use case will be added.
     * @return The created `UseCase` object.
     * @throws IllegalArgumentException if a use case with the given name already exists.
     */
    public UseCase addUseCase(String name, double x, double y, UseCaseDiagram activeDiagram) throws IllegalArgumentException {
        if (isDuplicateName(name)) {
            throw new IllegalArgumentException("A use case with this name already exists.");
        }

        // Clamp x and y to ensure the UseCase is within the active diagram
        double minX = activeDiagram.getX();
        double maxX = activeDiagram.getX() + activeDiagram.getWidth();
        double minY = activeDiagram.getY();
        double maxY = activeDiagram.getY() + activeDiagram.getHeight();

        // Ensure the UseCase is within bounds
        x = Math.max(minX, Math.min(x, maxX - UseCase.DEFAULT_WIDTH));
        y = Math.max(minY, Math.min(y, maxY - UseCase.DEFAULT_HEIGHT));

        UseCase useCase = new UseCase(name);
        useCase.setX(x);
        useCase.setY(y);
        useCases.add(useCase);
        return useCase;
    }

    /**
     * Edits the name of an existing use case.
     * Throws an exception if the new name is empty or if it conflicts with an existing use case name.
     *
     * @param useCase The `UseCase` to edit.
     * @param newName The new name for the use case.
     * @throws IllegalArgumentException if the new name is empty or already taken by another use case.
     */
    public void editUseCaseName(UseCase useCase, String newName) throws IllegalArgumentException {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Use case name cannot be empty.");
        }

        // Check if a duplicate name already exists
        for (UseCase uc : useCases) {
            if (!uc.equals(useCase) && uc.getName().equalsIgnoreCase(newName)) {
                throw new IllegalArgumentException("A use case with this name already exists.");
            }
        }

        // Update name
        useCase.setName(newName);
    }

    /**
     * Retrieves the list of all use cases managed by this manager.
     *
     * @return A list of `UseCase` objects.
     */
    public List<UseCase> getUseCases() {
        return useCases;
    }

    /**
     * Draws a `UseCase` object on the given `GraphicsContext`.
     * It calculates the required size for the oval shape based on the name of the use case
     * and positions the text in the center of the oval.
     *
     * @param gc The `GraphicsContext` used for drawing.
     * @param useCase The `UseCase` object to be drawn.
     */
    public void drawUseCase(GraphicsContext gc, UseCase useCase) {
        String name = useCase.getName();

        // Set font for drawing text
        gc.setFont(new Font("Arial", 14));
        Text textHelper = new Text();
        textHelper.setFont(gc.getFont());

        // Calculate the text width and height
        textHelper.setText(name);
        double textWidth = textHelper.getBoundsInLocal().getWidth();
        double textHeight = textHelper.getBoundsInLocal().getHeight();

        // Calculate the oval dimensions
        double ovalWidth = Math.max(0, textWidth + 30); // Add padding
        double ovalHeight = Math.max(50, textHeight + 10); // Add padding

        // Draw the oval
        gc.setFill(Color.WHITE);
        gc.fillOval(useCase.getX(), useCase.getY(), ovalWidth, ovalHeight);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(useCase.getX(), useCase.getY(), ovalWidth, ovalHeight);

        // Center text in the oval
        gc.setFill(Color.BLACK);
        double centerX = useCase.getX() + ovalWidth / 2 + textWidth / 2;
        double centerY = useCase.getY() + ovalHeight / 2.5;

        // Draw the text centered in the oval
        gc.fillText(name, centerX - textWidth / 2, centerY);
    }

    /**
     * Checks if a point is hovering over any `UseCase` in the provided list.
     *
     * @param x The x-coordinate of the point to check.
     * @param y The y-coordinate of the point to check.
     * @param useCases The list of `UseCase` objects to check against.
     * @return `true` if the point is hovering over any `UseCase`, `false` otherwise.
     */
    public static boolean isHoveringOverUseCase(double x, double y, List<UseCase> useCases) {
        for (UseCase useCase : useCases) {
            if (x >= useCase.getX() && x <= useCase.getX() + useCase.getWidth() &&
                    y >= useCase.getY() && y <= useCase.getY() + useCase.getHeight()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a `UseCase` by the click position.
     *
     * @param x The x-coordinate of the click.
     * @param y The y-coordinate of the click.
     * @return The `UseCase` object clicked, or `null` if no `UseCase` was clicked.
     */
    public UseCase findClickedUseCase(double x, double y) {
        for (UseCase useCase : useCases) {
            if (x >= useCase.getX() && x <= useCase.getX() + useCase.getWidth() &&
                    y >= useCase.getY() && y <= useCase.getY() + useCase.getHeight()) {
                return useCase;
            }
        }
        return null;
    }

    /**
     * Finds the `UseCase` object that corresponds to the clicked position in the list of use cases.
     *
     * @param x The x-coordinate of the click.
     * @param y The y-coordinate of the click.
     * @param useCases The list of `UseCase` objects to check against.
     * @return The `UseCase` object clicked, or `null` if no `UseCase` was clicked.
     */
    public static UseCase getClickedUseCase(double x, double y, List<UseCase> useCases) {
        for (UseCase useCase : useCases) {
            if (x >= useCase.getX() && x <= useCase.getX() + useCase.getWidth() &&
                    y >= useCase.getY() && y <= useCase.getY() + useCase.getHeight()) {
                return useCase;
            }
        }
        return null;
    }

    /**
     * Updates the position of a `UseCase` based on the mouse position and offset, ensuring it stays within the bounds of the active diagram.
     *
     * @param useCase The `UseCase` object to move.
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     * @param offsetX The x offset from the original position of the `UseCase`.
     * @param offsetY The y offset from the original position of the `UseCase`.
     * @param activeDiagram The `UseCaseDiagram` to which the `UseCase` belongs, used for boundary checks.
     */
    public static void updateUseCasePosition(UseCase useCase, double mouseX, double mouseY, double offsetX, double offsetY,
                                             UseCaseDiagram activeDiagram) {
        double newX = mouseX - offsetX;
        double newY = mouseY - offsetY;

        // Clamp the position of the UseCase to stay within the bounds of the ActiveDiagram
        double maxX = activeDiagram.getX() + activeDiagram.getWidth() - useCase.getWidth();
        double minX = activeDiagram.getX();
        double maxY = activeDiagram.getY() + activeDiagram.getHeight() - useCase.getHeight();
        double minY = activeDiagram.getY();

        // Apply constraints
        useCase.setX(Math.max(minX, Math.min(newX, maxX)));
        useCase.setY(Math.max(minY, Math.min(newY, maxY)));
    }
}

