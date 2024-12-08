package org.example.craftuml.Business;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseToUseCaseRelation;

import java.util.List;

public class UseCaseRelationService {
    private List<UseCaseToUseCaseRelation> includeRelations;
    private List<UseCaseToUseCaseRelation> extendRelations;

    public UseCaseRelationService(List<UseCaseToUseCaseRelation> includeRelations, List<UseCaseToUseCaseRelation> extendRelations) {
        this.includeRelations = includeRelations;
        this.extendRelations = extendRelations;
    }

    public boolean addIncludeRelation(UseCase useCase1, UseCase useCase2) {
        if (hasIncludeRelation(useCase1, useCase2)) {
            return false; // Relation already exists
        }
        includeRelations.add(new UseCaseToUseCaseRelation(useCase1, useCase2, "include"));
        return true;
    }

    public boolean addExtendRelation(UseCase useCase1, UseCase useCase2) {
        if (hasExtendRelation(useCase1, useCase2)) {
            return false; // Relation already exists
        }
        extendRelations.add(new UseCaseToUseCaseRelation(useCase1, useCase2, "extend"));
        return true;
    }

    public boolean hasIncludeRelation(UseCase useCase1, UseCase useCase2) {
        for (UseCaseToUseCaseRelation relation : includeRelations) {
            if ((relation.getUseCase1().equals(useCase1) && relation.getUseCase2().equals(useCase2)) ||
                    (relation.getUseCase1().equals(useCase2) && relation.getUseCase2().equals(useCase1))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasExtendRelation(UseCase useCase1, UseCase useCase2) {
        for (UseCaseToUseCaseRelation relation : extendRelations) {
            if ((relation.getUseCase1().equals(useCase1) && relation.getUseCase2().equals(useCase2)) ||
                    (relation.getUseCase1().equals(useCase2) && relation.getUseCase2().equals(useCase1))) {
                return true;
            }
        }
        return false;
    }

    public void drawUseCaseRelation(UseCase useCase1, UseCase useCase2, String relationType, GraphicsContext gc) {

        // Use case centers
        double useCase1CenterX = useCase1.getX() + useCase1.getWidth() / 2;
        double useCase1CenterY = useCase1.getY() + useCase1.getHeight() / 2;
        double useCase2CenterX = useCase2.getX() + useCase2.getWidth() / 2;
        double useCase2CenterY = useCase2.getY() + useCase2.getHeight() / 2;

        // Nearest boundary points
        Point2D start = getNearestBoundaryPoint(
                useCase1.getX(), useCase1.getY(), useCase1.getWidth(), useCase1.getHeight(),
                useCase2CenterX, useCase2CenterY,
                true // Use case is elliptical
        );

        Point2D end = getNearestBoundaryPoint(
                useCase2.getX(), useCase2.getY(), useCase2.getWidth(), useCase2.getHeight(),
                useCase1CenterX, useCase1CenterY,
                true // Use case is elliptical
        );

        // Draw dotted line
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.setLineDashes(10); // Dotted line pattern
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
        gc.setLineDashes(0); // Reset dash pattern

        // Draw arrowhead
        drawArrowhead(gc, start.getX(), start.getY(), end.getX(), end.getY());

        // Draw relation label
        double labelX = (start.getX() + end.getX()) / 2;
        double labelY = (start.getY() + end.getY()) / 2;
        gc.setFill(Color.BLACK);
        // Draw relation label above the line
        double offset = 40; // Adjust the vertical offset as needed
        gc.fillText("<<" + relationType + ">>", labelX, labelY - offset);

    }

    /**
     * Draws an arrowhead pointing from (x1, y1) to (x2, y2).
     */
    private void drawArrowhead(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowLength = 10;
        double arrowAngle = Math.toRadians(30); // Angle of arrowhead sides

        // Calculate points for the arrowhead
        double xArrow1 = x2 - arrowLength * Math.cos(angle - arrowAngle);
        double yArrow1 = y2 - arrowLength * Math.sin(angle - arrowAngle);
        double xArrow2 = x2 - arrowLength * Math.cos(angle + arrowAngle);
        double yArrow2 = y2 - arrowLength * Math.sin(angle + arrowAngle);

        // Draw the arrowhead
        gc.strokeLine(x2, y2, xArrow1, yArrow1);
        gc.strokeLine(x2, y2, xArrow2, yArrow2);
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

    public boolean createRelation(UseCase useCase1, UseCase useCase2, String relationType) {
        if (useCase1.equals(useCase2)) {
            return false; // Prevent self-relation
        }

        if (relationType.equals("include")) {
            return addIncludeRelation(useCase1, useCase2);
        } else if (relationType.equals("extend")) {
            return addExtendRelation(useCase1, useCase2);
        }

        return false;
    }
}
