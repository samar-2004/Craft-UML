package org.example.craftuml.models.UseCaseDiagrams;

import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.craftuml.models.DiagramComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an actor in a use case diagram.
 * <p>
 * An actor is a key element in a use case diagram, typically representing a user or an external system
 * that interacts with the system being modeled. This class provides functionalities to manage an actor's
 * properties such as name, position, and size, as well as its associations with use cases.
 * <p>
 * The visual representation of an actor is managed using a rectangle drawn on a canvas, and this class
 * includes mouse event handlers for interactivity, such as dragging and repositioning.
 * <p>
 * Each actor can also maintain a list of associations it has with various use cases in the diagram.
 */
public class Actor implements DiagramComponent {
    /**
     * The name of the actor, typically used to identify it in the diagram.
     */
    private String name;

    /**
     * The x-coordinate of the actor's position on the canvas.
     */
    private double x;

    /**
     * The y-coordinate of the actor's position on the canvas.
     */
    private double y;

    /**
     * The offset in the x-direction during dragging the actor.
     * This helps to adjust the actor's position relative to the cursor when being dragged.
     */
    private double dragOffsetX;

    /**
     * The offset in the y-direction during dragging the actor.
     * This helps to adjust the actor's position relative to the cursor when being dragged.
     */
    private double dragOffsetY;

    /**
     * The width of the actor's rectangle on the canvas. Default value is 30.
     */
    private double width = 30;

    /**
     * The height of the actor's rectangle on the canvas. Default value is 90.
     */
    private double height = 90;

    /**
     * The rectangle representing the actor's visual representation on the canvas.
     */
    private Rectangle actorRectangle;

    /**
     * A list of associations that the actor has with use cases.
     * This list stores all the associations between this actor and use cases in the diagram.
     */
    private List<Association> associations;


    /**
     * Constructor to initialize an Actor object with a specified name.
     * The actor is placed at the default position (0, 0) on the canvas,
     * and a rectangle representing the actor is created with the default width and height.
     *
     * @param name The name of the actor to initialize.
     */
    public Actor(String name) {
        this.name = name;
        this.x = 0;  // Default position
        this.y = 0;  // Default position
        actorRectangle = new Rectangle(x, y, width, height); // Initialize the actor's rectangle
        associations = new ArrayList<>();
        // Add mouse events to the actor's rectangle
        enableMouseEvents();
    }


    /**
     * Enables mouse events for the actor's rectangle, allowing it to be dragged and repositioned.
     * This method sets up handlers for mouse pressed, dragged, and released events.
     */
    private void enableMouseEvents() {
        actorRectangle.setOnMousePressed(this::onMousePressed);
        actorRectangle.setOnMouseDragged(this::onMouseDragged);
        actorRectangle.setOnMouseReleased(this::onMouseReleased);
    }

    /**
     * Gets the width of the actor's rectangle.
     *
     * @return The width of the actor's rectangle.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the width of the actor's rectangle.
     *
     * @param width The width to set for the actor's rectangle.
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Gets the height of the actor's rectangle.
     *
     * @return The height of the actor's rectangle.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the height of the actor's rectangle.
     *
     * @param height The height to set for the actor's rectangle.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Handles the mouse press event when the user clicks on the actor.
     * This method prints the actor's name to the console when clicked.
     *
     * @param event The MouseEvent associated with the mouse press.
     */
    public void onMousePressed(MouseEvent event) {
        // Logic to handle when mouse is pressed on the actor
        System.out.println("Actor clicked: " + name);
    }

    /**
     * Handles the mouse drag event while the actor is being dragged.
     * This method updates the position of the actor to follow the mouse cursor.
     *
     * @param event The MouseEvent associated with the mouse drag.
     */
    public void onMouseDragged(MouseEvent event) {
        // Update position of actor while dragging
        setX(event.getSceneX() - width / 2); // Center actor at mouse position
        setY(event.getSceneY() - height / 2); // Center actor at mouse position
        actorRectangle.setX(x); // Update rectangle position
        actorRectangle.setY(y);
    }

    /**
     * Handles the mouse release event when the user releases the mouse button after dragging the actor.
     * This method prints the actor's name to the console when the mouse is released.
     *
     * @param event The MouseEvent associated with the mouse release.
     */
    private void onMouseReleased(MouseEvent event) {
        // Logic to handle when mouse is released
        System.out.println("Mouse released on actor: " + name);
    }

    /**
     * Checks if the actor was clicked within a certain radius from its center.
     * This method is used to determine if a mouse click is inside the actor's bounding area.
     *
     * @param mouseX The x-coordinate of the mouse click.
     * @param mouseY The y-coordinate of the mouse click.
     * @return true if the mouse click is within the actor's bounds; false otherwise.
     */
    public boolean isClicked(double mouseX, double mouseY) {
        double radius = 20; // Adjust if you changed the circle size
        double centerX = this.x + radius;
        double centerY = this.y + radius;

        // Check if the click is within the circle's radius
        double distance = Math.sqrt(Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2));
        return distance <= radius;
    }


    /**
     * Sets the x-coordinate of the actor.
     *
     * @param x The new x-coordinate to set for the actor.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of the actor.
     *
     * @param y The new y-coordinate to set for the actor.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Gets the name of the actor.
     *
     * @return The name of the actor.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the actor. The name cannot be null or empty.
     *
     * @param name The name to set for the actor.
     * @throws IllegalArgumentException If the provided name is null or empty.
     */
    @Override
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    /**
     * Sets the position of the actor on the canvas.
     * The x and y coordinates must be non-negative.
     *
     * @param x The x-coordinate to set for the actor.
     * @param y The y-coordinate to set for the actor.
     * @throws IllegalArgumentException If either of the position coordinates is negative.
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
     * Gets the x-coordinate of the actor.
     *
     * @return The x-coordinate of the actor.
     */
    @Override
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the actor.
     *
     * @return The y-coordinate of the actor.
     */
    @Override
    public double getY() {
        return y;
    }

    /**
     * Returns a string representation of the actor, including its name and position (x, y).
     *
     * @return A string representation of the actor.
     */
    @Override
    public String toString() {
        return "Actor{" +
                "name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    /**
     * Retrieves the rectangle representing the actor's visual representation on the canvas.
     * This rectangle can be used for rendering purposes.
     *
     * @return The {@link Rectangle} representing the actor's visual bounds.
     */
    public Rectangle getActorRectangle() {
        return actorRectangle;
    }

    /**
     * Adds an association to the list of associations connected to this actor.
     *
     * @param association The {@link Association} to be added. It represents a relationship
     *                     between this actor and a use case in the diagram.
     * @throws IllegalArgumentException If the provided association is null.
     */
    public void addAssociation(Association association) {
        associations.add(association);
    }

    /**
     * Retrieves the list of associations connected to this actor.
     * Each association represents a relationship between this actor and one or more use cases.
     *
     * @return A {@link List} of {@link Association} objects linked to this actor.
     */
    public List<Association> getAssociations() {
        return associations;
    }

}
