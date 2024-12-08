package org.example.craftuml.Business;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.craftuml.models.UseCaseDiagrams.Actor;
import org.example.craftuml.models.UseCaseDiagrams.Association;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;

import java.util.List;

public class AssociationService {
    private List<UseCase> useCases;
    private List<Actor> actors;

    public AssociationService(List<UseCase> useCases, List<Actor> actors) {
        this.useCases = useCases;
        this.actors = actors;
    }

    // Create an association and check if the association already exists
    public boolean createAssociation(UseCase useCase, Actor actor, List<Association> associations) {
        if (!isUseCaseAssociated(useCase, actor)) {
            Association association = new Association(actor, useCase);
            associations.add(association);
            useCase.addAssociation(actor);
            return true;
        }
        return false;
    }

    public boolean isUseCaseAssociated(UseCase useCase, Actor actor) {
        return useCase.getAssociations().contains(actor);
    }

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
