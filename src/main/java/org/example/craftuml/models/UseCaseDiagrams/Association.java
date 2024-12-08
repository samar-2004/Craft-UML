package org.example.craftuml.models.UseCaseDiagrams;

import java.awt.*;

public class Association {
    private Actor actor;        // Reference to the actor
    private UseCase useCase;    // Reference to the use case
    private double startX, startY;  // Coordinates of the start point (actor's position)
    private double endX, endY;    // Coordinates of the end point (use case's position)

    // Constructor to initialize the association
    public Association(Actor actor, UseCase useCase, double startX, double startY, double endX, double endY) {
        this.actor = actor;
        this.useCase = useCase;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public Association(Actor actor, UseCase useCase) {
        this.actor = actor;
        this.useCase = useCase;
        this.startX = Double.NaN;
        this.startY = Double.NaN;
        this.endX = Double.NaN;
        this.endY = Double.NaN;
    }


    public Association() {

    }

    // Getters and Setters
    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public UseCase getUseCase() {
        return useCase;
    }

    public void setUseCase(UseCase useCase) {
        this.useCase = useCase;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    // Method to draw the association (e.g., a line between actor and use case)
    public void drawAssociation(Graphics g) {
        // For simplicity, assuming Graphics is available
        g.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
    }

    // Method to check if a point is close to the start or end point for interaction
    public boolean isNear(double x, double y) {
        // Simple distance check for association endpoints
        double threshold = 10; // Example threshold for proximity
        double startDistance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
        double endDistance = Math.sqrt(Math.pow(x - endX, 2) + Math.pow(y - endY, 2));

        return startDistance <= threshold || endDistance <= threshold;
    }

    @Override
    public String toString() {
        return "Association: " + actor.getName() + " <-> " + useCase.getName();
    }
}

