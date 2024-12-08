package org.example.craftuml.models.UseCaseDiagrams;

import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import org.example.craftuml.models.DiagramComponent;

import java.util.List;

public class Actor implements DiagramComponent {
    private String name; // Name of the actor
    private double x;
    private double y;
    private double dragOffsetX; // Offset during dragging
    private double dragOffsetY; // Offset during dragging
    private double width =30;
    private double height=90;
    private Rectangle actorRectangle;  // Rectangle representing the actor on the canvas
    private List<Association> associations; // List to store associations with use cases

    // Constructor
    public Actor(String name) {
        this.name = name;
        this.x = 0;  // Default position
        this.y = 0;  // Default position
        actorRectangle = new Rectangle(x, y, width, height); // Initialize the actor's rectangle

        // Add mouse events to the actor's rectangle
        enableMouseEvents();
    }

    private void enableMouseEvents() {
        actorRectangle.setOnMousePressed(this::onMousePressed);
        actorRectangle.setOnMouseDragged(this::onMouseDragged);
        actorRectangle.setOnMouseReleased(this::onMouseReleased);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getDragOffsetX() {
        return dragOffsetX;
    }

    public void setDragOffsetX(double dragOffsetX) {
        this.dragOffsetX = dragOffsetX;
    }

    public double getDragOffsetY() {
        return dragOffsetY;
    }

    public void setDragOffsetY(double dragOffsetY) {
        this.dragOffsetY = dragOffsetY;
    }

    public void onMousePressed(MouseEvent event) {
        // Logic to handle when mouse is pressed on the actor
        System.out.println("Actor clicked: " + name);
    }

    public void onMouseDragged(MouseEvent event) {
        // Update position of actor while dragging
        setX(event.getSceneX() - width / 2); // Center actor at mouse position
        setY(event.getSceneY() - height / 2); // Center actor at mouse position
        actorRectangle.setX(x); // Update rectangle position
        actorRectangle.setY(y);
    }

    private void onMouseReleased(MouseEvent event) {
        // Logic to handle when mouse is released
        System.out.println("Mouse released on actor: " + name);
    }

    public boolean isClicked(double mouseX, double mouseY) {
        double radius = 20; // Adjust if you changed the circle size
        double centerX = this.x + radius;
        double centerY = this.y + radius;

        // Check if the click is within the circle's radius
        double distance = Math.sqrt(Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2));
        return distance <= radius;
    }


    // Setters and getters for x, y, width, height
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    @Override
    public void setPosition(double x, double y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Position coordinates cannot be negative");
        }
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Actor{" +
                "name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    // Method to get the actor's rectangle (for rendering)
    public Rectangle getActorRectangle() {
        return actorRectangle;
    }

    public void addAssociation(Association association) {
        associations.add(association);
    }

    // Getter for associations
    public List<Association> getAssociations() {
        return associations;
    }

}
