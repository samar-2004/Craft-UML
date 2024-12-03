package org.example.craftuml.models.UseCaseDiagrams;

import org.example.craftuml.models.DiagramComponent;

import java.util.ArrayList;
import java.util.List;

public class UseCase implements DiagramComponent {
    private String name; // Name of the use case
    private double x;
    private double width = 100; // Default width
    private double height = 50; // Default height
    private double dragOffsetX; // Offset during dragging
    private double dragOffsetY; // Offset during dragging
    private List<Actor> associatedActors = new ArrayList<>();  // List to store associated actors

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
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

    private double y; // Coordinates for the use case in the diagram

    // Constructor
    public UseCase(String name) {
        this.name = name;
        this.x = 0; // Default position
        this.y = 0; // Default position
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
        return "UseCase{" +
                "name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    public void addAssociation(Actor actor) {
        if (actor != null && !associatedActors.contains(actor)) {
            associatedActors.add(actor);  // Add actor to the list if not already present
        }
    }

    // Getter for associations
    public List<Actor> getAssociations() {
        return associatedActors;  // Return the list of associated actors
    }
}
