package org.example.craftuml.Business;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseDiagram;

import java.util.ArrayList;
import java.util.List;

public class UseCaseManager {

    private List<UseCase> useCases = new ArrayList<>();

    // Constructor to initialize the actors list
    public UseCaseManager(List<UseCase> useCases) {
        this.useCases = useCases;
    }

    public boolean isDuplicateName(String name) {
        return useCases.stream().anyMatch(useCase -> useCase.getName().equalsIgnoreCase(name));
    }

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

    public List<UseCase> getUseCases() {
        return useCases;
    }

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
     * Finds a UseCase by clicking position.
     * @param x The x-coordinate of the click.
     * @param y The y-coordinate of the click.
     * @return The clicked UseCase if found, otherwise null.
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

    public static UseCase getClickedUseCase(double x, double y, List<UseCase> useCases) {
        for (UseCase useCase : useCases) {
            if (x >= useCase.getX() && x <= useCase.getX() + useCase.getWidth() &&
                    y >= useCase.getY() && y <= useCase.getY() + useCase.getHeight()) {
                return useCase;
            }
        }
        return null;
    }

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

