package org.example.craftuml.models.UseCaseDiagrams;

import javafx.scene.paint.Color;
import org.example.craftuml.models.DiagramComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a use case in a use case diagram. A use case is a specific action or interaction
 * that a system performs in response to a request from an actor. This class contains properties
 * related to the use case's position, size, and associated actors, as well as methods to manipulate
 * and retrieve these properties. The use case is visually represented by a rectangle on the diagram canvas.
 *
 * A use case can have multiple associated actors, and it provides functionality to handle user interactions
 * such as dragging and resizing. The name of the use case serves as an identifier in the diagram.
 */

public class UseCase implements DiagramComponent {
    /**
     * The default width of the use case. Used if no specific width is set.
     */
    public static final double DEFAULT_WIDTH = 100;

    /**
     * The default height of the use case. Used if no specific height is set.
     */
    public static final double DEFAULT_HEIGHT = 50;

    /**
     * The name of the use case.
     * This represents the label or identifier for the use case in the diagram.
     */
    private String name;

    /**
     * The x-coordinate of the position of the use case diagram on the canvas.
     * This value determines where the use case is placed horizontally.
     */
    private double x;

    /**
     * The width of the use case diagram.
     * The default value is {@link #DEFAULT_WIDTH} but can be changed.
     */
    private double width = DEFAULT_WIDTH;

    /**
     * The height of the use case diagram.
     * The default value is {@link #DEFAULT_HEIGHT} but can be modified.
     */
    private double height = DEFAULT_HEIGHT;

    /**
     * The offset in the x-direction during dragging of the use case.
     * This helps in handling the movement of the use case within the canvas.
     */
    private double dragOffsetX;

    /**
     * The offset in the y-direction during dragging of the use case.
     * This helps in handling the movement of the use case within the canvas.
     */
    private double dragOffsetY;

    /**
     * A list of {@link Actor} objects that are associated with the use case.
     * This list represents the actors that interact with the use case in the system.
     */
    private List<Actor> associatedActors = new ArrayList<>();


    /**
     * Sets the x-coordinate of the use case diagram on the canvas.
     *
     * @param x The new x-coordinate to set for the use case.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of the use case diagram on the canvas.
     *
     * @param y The new y-coordinate to set for the use case.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Gets the width of the use case diagram.
     *
     * @return The width of the use case diagram.
     */
    public double getWidth() {
        return width;
    }


    /**
     * Gets the height of the use case diagram.
     *
     * @return The height of the use case diagram.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Gets the x-offset of the use case during dragging.
     *
     * @return The x-offset during dragging.
     */
    public double getDragOffsetX() {
        return dragOffsetX;
    }

    /**
     * Sets the x-offset of the use case during dragging.
     *
     * @param dragOffsetX The new x-offset during dragging.
     */
    public void setDragOffsetX(double dragOffsetX) {
        this.dragOffsetX = dragOffsetX;
    }

    /**
     * Gets the y-offset of the use case during dragging.
     *
     * @return The y-offset during dragging.
     */
    public double getDragOffsetY() {
        return dragOffsetY;
    }

    /**
     * Sets the y-offset of the use case during dragging.
     *
     * @param dragOffsetY The new y-offset during dragging.
     */
    public void setDragOffsetY(double dragOffsetY) {
        this.dragOffsetY = dragOffsetY;
    }

    /**
     * The y-coordinate of the position of the use case diagram on the canvas.
     * This value determines where the use case is placed vertically.
     */
    private double y; // Coordinates for the use case in the diagram

    /**
     * Constructor to initialize a UseCase object with the specified name.
     * The default position is (0, 0), and the default size is set via {@link #DEFAULT_WIDTH} and {@link #DEFAULT_HEIGHT}.
     *
     * @param name The name of the use case.
     */
    // Constructor
    public UseCase(String name) {
        this.name = name;
        this.x = 0; // Default position
        this.y = 0; // Default position
    }


    /**
     * Gets the name of the use case.
     *
     * @return The name of the use case.
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Sets the name of the use case. The name cannot be null or empty.
     *
     * @param name The name to set for the use case.
     * @throws IllegalArgumentException If the name is null or empty.
     */
    @Override
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    /**
     * Sets the position of the use case on the canvas.
     * The coordinates (x, y) cannot be negative.
     *
     * @param x The new x-coordinate for the use case.
     * @param y The new y-coordinate for the use case.
     * @throws IllegalArgumentException If either x or y is negative.
     */
    @Override
    public void setPosition(double x, double y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Position coordinates cannot be negative");
        }
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate of the use case on the canvas.
     *
     * @return The x-coordinate of the use case.
     */
    @Override
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the use case on the canvas.
     *
     * @return The y-coordinate of the use case.
     */
    @Override
    public double getY() {
        return y;
    }

    /**
     * Returns a string representation of the use case, including its name and position.
     *
     * @return A string representing the use case with its name and coordinates.
     */
    @Override
    public String toString() {
        return "UseCase{" +
                "name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    /**
     * Adds an actor to the list of associated actors. The actor is added only if it is not already associated.
     *
     * @param actor The actor to associate with the use case.
     */
    public void addAssociation(Actor actor) {
        if (actor != null && !associatedActors.contains(actor)) {
            associatedActors.add(actor);  // Add actor to the list if not already present
        }
    }

    /**
     * Gets the list of actors associated with this use case.
     *
     * @return The list of associated actors.
     */
    public List<Actor> getAssociations() {
        return associatedActors;  // Return the list of associated actors
    }
}
