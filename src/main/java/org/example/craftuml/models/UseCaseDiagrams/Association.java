package org.example.craftuml.models.UseCaseDiagrams;

import java.awt.*;

public class Association {
    /**
     * The actor involved in the association. This reference links to the actor that is part of the association.
     */
    private Actor actor;

    /**
     * The use case involved in the association. This reference links to the use case that is part of the association.
     */
    private UseCase useCase;

    /**
     * The x-coordinate of the start point of the association. This point typically represents the actor's position.
     */
    private double startX;

    /**
     * The y-coordinate of the start point of the association. This point typically represents the actor's position.
     */
    private double startY;

    /**
     * The x-coordinate of the end point of the association. This point typically represents the position of the use case.
     */
    private double endX;

    /**
     * The y-coordinate of the end point of the association. This point typically represents the position of the use case.
     */
    private double endY;


    /**
     * Constructor to initialize an Association object with the specified actor, use case, and coordinates for both start and end points.
     *
     * @param actor The actor involved in the association.
     * @param useCase The use case involved in the association.
     * @param startX The x-coordinate of the starting point (actor's position).
     * @param startY The y-coordinate of the starting point (actor's position).
     * @param endX The x-coordinate of the end point (use case's position).
     * @param endY The y-coordinate of the end point (use case's position).
     */
    public Association(Actor actor, UseCase useCase, double startX, double startY, double endX, double endY) {
        this.actor = actor;
        this.useCase = useCase;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    /**
     * Constructor to initialize an Association object with the specified actor and use case, without defining the start and end points.
     * The start and end points will be set to {@link Double#NaN} initially.
     *
     * @param actor The actor involved in the association.
     * @param useCase The use case involved in the association.
     */
    public Association(Actor actor, UseCase useCase) {
        this.actor = actor;
        this.useCase = useCase;
        this.startX = Double.NaN;
        this.startY = Double.NaN;
        this.endX = Double.NaN;
        this.endY = Double.NaN;
    }


    /**
     * Default constructor for the Association class.
     * Initializes the association without specifying any actor, use case, or coordinates.
     */
    public Association() {

    }

    /**
     * Gets the actor involved in the association.
     *
     * @return The {@link Actor} associated with this Association.
     */
    public Actor getActor() {
        return actor;
    }

    /**
     * Sets the actor involved in the association.
     *
     * @param actor The {@link Actor} to set for this Association.
     */
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    /**
     * Gets the use case involved in the association.
     *
     * @return The {@link UseCase} associated with this Association.
     */
    public UseCase getUseCase() {
        return useCase;
    }

    /**
     * Sets the use case involved in the association.
     *
     * @param useCase The {@link UseCase} to set for this Association.
     */
    public void setUseCase(UseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Gets the x-coordinate of the start point (actor's position).
     *
     * @return The x-coordinate of the start point.
     */
    public double getStartX() {
        return startX;
    }

    /**
     * Sets the x-coordinate of the start point (actor's position).
     *
     * @param startX The x-coordinate to set for the start point.
     */
    public void setStartX(double startX) {
        this.startX = startX;
    }

    /**
     * Gets the y-coordinate of the start point (actor's position).
     *
     * @return The y-coordinate of the start point.
     */
    public double getStartY() {
        return startY;
    }

    /**
     * Sets the y-coordinate of the start point (actor's position).
     *
     * @param startY The y-coordinate to set for the start point.
     */
    public void setStartY(double startY) {
        this.startY = startY;
    }

    /**
     * Gets the x-coordinate of the end point (use case's position).
     *
     * @return The x-coordinate of the end point.
     */
    public double getEndX() {
        return endX;
    }

    /**
     * Sets the x-coordinate of the end point (use case's position).
     *
     * @param endX The x-coordinate to set for the end point.
     */
    public void setEndX(double endX) {
        this.endX = endX;
    }

    /**
     * Gets the y-coordinate of the end point (use case's position).
     *
     * @return The y-coordinate of the end point.
     */
    public double getEndY() {
        return endY;
    }

    /**
     * Sets the y-coordinate of the end point (use case's position).
     *
     * @param endY The y-coordinate to set for the end point.
     */
    public void setEndY(double endY) {
        this.endY = endY;
    }

    /**
     * Draws the association between the actor and use case on a given {@link Graphics} context.
     * This method assumes that the {@link Graphics} object is available for drawing.
     * It draws a line between the start and end points, representing the association.
     *
     * @param g The {@link Graphics} context used to draw the line between the actor and use case.
     */
    public void drawAssociation(Graphics g) {
        // For simplicity, assuming Graphics is available
        g.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
    }

    /**
     * Checks if a given point (x, y) is near either the start or end point of the association.
     * This method calculates the distance from the given point to both the start and end points
     * and returns true if either distance is within a certain threshold.
     *
     * @param x The x-coordinate of the point to check.
     * @param y The y-coordinate of the point to check.
     * @return {@code true} if the point is close enough to either the start or end point of the association,
     *         {@code false} otherwise.
     */
    public boolean isNear(double x, double y) {
        // Simple distance check for association endpoints
        double threshold = 10; // Example threshold for proximity
        double startDistance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
        double endDistance = Math.sqrt(Math.pow(x - endX, 2) + Math.pow(y - endY, 2));

        return startDistance <= threshold || endDistance <= threshold;
    }

    /**
     * Returns a string representation of the association between the actor and use case.
     * This method provides a formatted string describing the association in the form:
     * "Association: actorName <-> useCaseName".
     *
     * @return A string representing the association between the actor and use case.
     */
    @Override
    public String toString() {
        return "Association: " + actor.getName() + " <-> " + useCase.getName();
    }
}

