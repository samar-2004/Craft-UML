package org.example.craftuml.Business;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.craftuml.models.UseCaseDiagrams.Actor;
import org.example.craftuml.models.UseCaseDiagrams.Association;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;

import java.util.List;

/**
 * Manages the associations between actors and use cases in a use case diagram.
 * Provides functionality for creating associations, checking existing associations,
 * and drawing association lines between actors and use cases on a canvas.
 */
public class AssociationManager {
    /**
     * A list of `UseCase` objects representing the use cases in the diagram.
     * This list is used to manage and store the use cases for the diagram.
     */
    private List<UseCase> useCases;

    /**
     * A list of `Actor` objects representing the actors in the diagram.
     * This list is used to manage and store the actors for the diagram.
     */
    private List<Actor> actors;

    /**
     * Constructor to initialize the `AssociationManager` with lists of `UseCase` and `Actor`.
     *
     * @param useCases A list of `UseCase` objects representing the use cases in the diagram.
     * @param actors A list of `Actor` objects representing the actors in the diagram.
     */
    public AssociationManager(List<UseCase> useCases, List<Actor> actors) {
        this.useCases = useCases;
        this.actors = actors;
    }

    /**
     * Creates an association between a `UseCase` and an `Actor`, if one does not already exist.
     * The association is then added to the provided list of associations, and the use case's list of associations is updated.
     *
     * @param useCase The `UseCase` to associate with an actor.
     * @param actor The `Actor` to associate with a use case.
     * @param associations The list where the new association will be added.
     * @return `true` if the association was created, `false` if the association already exists.
     */
    public boolean createAssociation(UseCase useCase, Actor actor, List<Association> associations) {
        if (!isUseCaseAssociated(useCase, actor)) {
            Association association = new Association(actor, useCase);
            associations.add(association);
            useCase.addAssociation(actor);
            return true;
        }
        return false;
    }

    /**
     * Checks if a given `Actor` is already associated with a `UseCase`.
     *
     * @param useCase The `UseCase` to check for associations.
     * @param actor The `Actor` to check for association with the `UseCase`.
     * @return `true` if the actor is already associated with the use case, `false` otherwise.
     */
    public boolean isUseCaseAssociated(UseCase useCase, Actor actor) {
        return useCase.getAssociations().contains(actor);
    }

    /**
     * Draws an association line between an `Actor` and a `UseCase` on the provided canvas.
     * The line is drawn from the nearest boundary of the actor to the nearest boundary of the use case.
     *
     * @param actor The `Actor` to draw the association line from.
     * @param useCase The `UseCase` to draw the association line to.
     * @param drawingCanvas The `Canvas` on which the association line will be drawn.
     */
    public void drawAssociationLine(Actor actor, UseCase useCase, Canvas drawingCanvas) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        double actorCenterX = actor.getX() + actor.getWidth() / 2;
        double actorCenterY = actor.getY() + actor.getHeight() / 2;
        double useCaseCenterX = useCase.getX() + useCase.getWidth() / 2;
        double useCaseCenterY = useCase.getY() + useCase.getHeight() / 2;

        Point2D actorBoundaryPoint = getNearestBoundaryPoint(
                actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(),
                useCaseCenterX, useCaseCenterY, false
        );

        Point2D useCaseBoundaryPoint = getNearestBoundaryPoint(
                useCase.getX(), useCase.getY(), useCase.getWidth(), useCase.getHeight(),
                actorCenterX, actorCenterY, true
        );

        gc.strokeLine(actorBoundaryPoint.getX(), actorBoundaryPoint.getY(),
                useCaseBoundaryPoint.getX(), useCaseBoundaryPoint.getY());
    }

    /**
     * Finds the nearest boundary point of a shape (either actor or use case) to a target point (actor or use case center).
     * This method handles both rectangular and elliptical shapes for accurate boundary point calculation.
     *
     * @param x The x-coordinate of the top-left corner of the shape.
     * @param y The y-coordinate of the top-left corner of the shape.
     * @param width The width of the shape.
     * @param height The height of the shape.
     * @param targetX The x-coordinate of the target point (the center of the other shape).
     * @param targetY The y-coordinate of the target point (the center of the other shape).
     * @param isEllipse Boolean flag indicating if the shape is an ellipse (`true`) or a rectangle (`false`).
     * @return A `Point2D` object representing the nearest boundary point of the shape.
     */
    private Point2D getNearestBoundaryPoint(double x, double y, double width, double height,
                                            double targetX, double targetY, boolean isEllipse) {
        if (isEllipse) {
            double a = width / 2.0;
            double b = height / 2.0;
            double xCenter = x + a;
            double yCenter = y + b;

            double dx = targetX - xCenter;
            double dy = targetY - yCenter;

            double magnitude = Math.sqrt(dx * dx + dy * dy);
            double nx = dx / magnitude;
            double ny = dy / magnitude;

            double boundaryX = xCenter + a * nx;
            double boundaryY = yCenter + b * ny;

            return new Point2D(boundaryX - 8, boundaryY);
        } else {
            double rectCenterX = x + width / 2.0;
            double rectCenterY = y + height / 2.0;

            double dx = targetX - rectCenterX;
            double dy = targetY - rectCenterY;

            double absDx = Math.abs(dx);
            double absDy = Math.abs(dy);

            double scale = (absDx / width > absDy / height) ? width / 2.0 / absDx : height / 2.0 / absDy;

            double boundaryX = rectCenterX + dx * scale;
            double boundaryY = rectCenterY + dy * scale;

            return new Point2D(boundaryX, boundaryY);
        }
    }
}
