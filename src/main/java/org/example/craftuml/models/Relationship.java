package org.example.craftuml.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Relationship {
    private ClassDiagram sourceClass;
    private ClassDiagram targetClass;
    private InterfaceData targetInterface;
    public String type; // "association", "composition", "aggregation","Realization"
    private String sourceClassMultiplicity;
    private String targetClassMultiplicity;
    private String relationName;
    private static final double OFFSET = 50.0;
    private List<Rectangle> obstacles = new ArrayList<>();
    double startX,startY,endX,endY;
    private final StringProperty relationNameProperty = new SimpleStringProperty();

    public Relationship(ClassDiagram sourceClass, ClassDiagram targetClass, String type, String sourceClassMultiplicity, String targetClassMultiplicity,List<Rectangle> obstacles,String relationName) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
        this.type = type;
        this.sourceClassMultiplicity = sourceClassMultiplicity;
        this.targetClassMultiplicity = targetClassMultiplicity;
        this.obstacles=obstacles;
        this.relationName = relationName;
        this.relationNameProperty.set(type);
    }
    public Relationship(ClassDiagram sourceClass, InterfaceData targetInterface, String type, String sourceClassMultiplicity, String targetClassMultiplicity, List<Rectangle> obstacles) {
        this.sourceClass = sourceClass;
        this.targetInterface = targetInterface;
        this.type = type;
        this.sourceClassMultiplicity = sourceClassMultiplicity;
        this.targetClassMultiplicity = targetClassMultiplicity;
        this.obstacles=obstacles;
        this.relationNameProperty.set(type);
    }

    public String getRelationType() {
        return relationNameProperty.get();
    }

    public void setRelationType(String relationName) {
        this.relationNameProperty.set(relationName);
    }
    public StringProperty relationNameProperty() {
        return relationNameProperty;
    }
    public void draw(GraphicsContext gc)
    {
        double x1 = sourceClass.getX();
        double y1 = sourceClass.getY();
        double x2 = targetClass.getX();
        double y2 = targetClass.getY();


        double sourceClassWidth = sourceClass.getWidth();
        double sourceClassHeight = sourceClass.getHeight();
        double targetClassWidth = targetClass.getWidth();
        double targetClassHeight = targetClass.getHeight();

        double[] adjustedSourceClass = calculateOrthogonalBorderIntersection(
                x1, y1, sourceClassWidth, sourceClassHeight, x1, y1, x2, y2
        );
        double[] adjustedTargetClass = calculateOrthogonalBorderIntersection(
                x2, y2, targetClassWidth, targetClassHeight, x2, y2, x1, y1
        );

        double adjX1 = adjustedSourceClass[0];
        double adjY1 = adjustedSourceClass[1];
        double adjX2 = adjustedTargetClass[0];
        double adjY2 = adjustedTargetClass[1];


        startX = adjX1;
        startY = adjY1;
        endX = adjX2;
        endY = adjY2;

        double midX = adjX1;
        double midY = adjY2;

        gc.setLineWidth(2);


        if (type.equals("association") && sourceClass == targetClass) {
            drawSelfAssociation(gc, x1, y1, sourceClassWidth, sourceClassHeight);
            return;
        }

        double arrowLength;
        if (type.equals("composition") || type.equals("aggregation"))
        {
              arrowLength = 20.0;
        }
        else
        {
              arrowLength = 0;
        }

        double angle = Math.atan2(adjY2 - adjY1, adjX2 - adjX1);


        double baseX = adjX2 - arrowLength * Math.cos(angle);
        double baseY = adjY2 - arrowLength * Math.sin(angle);

        gc.setStroke(Color.BLACK);

        gc.strokeLine(adjX1, adjY1, baseX, baseY);

        double mid2X = adjX1 + (adjX2 - adjX1) / 2;
        double mid2Y = adjY1 + (adjY2 - adjY1) / 2;
        gc.setFill(Color.BLACK);
        gc.fillText(relationName, mid2X, mid2Y - 10);

        drawMultiplicity(gc,
                adjX1, adjY1, sourceClassMultiplicity,
                adjX2, adjY2, targetClassMultiplicity,
                x1, y1, x1 + sourceClassWidth, y1 + sourceClassHeight,
                x2, y2, x2 + targetClassWidth, y2 + targetClassHeight);


        if (type.equals("composition") || type.equals("aggregation")) {
            drawDiamondArrowhead(gc, type, adjX1, adjY1, adjX2, adjY2);
        }
    }
    private void drawSelfAssociation(GraphicsContext gc, double x, double y, double width, double height) {

        double loopOffsetX = width / 2 ;
        double loopOffsetY = height / 2; ;

        double startX = x + width;
        double startY = y + height / 2;
        double cornerX = startX + loopOffsetX;
        double cornerY = startY + loopOffsetY;
        double endX = x + width;
        double endY = cornerY;

        gc.setStroke(Color.BLACK);
        gc.strokeLine(startX, startY, cornerX, startY);
        gc.strokeLine(cornerX, startY, cornerX, cornerY);
        gc.strokeLine(cornerX, cornerY, endX, cornerY);

        double midX = startX + (endX - startX) / 2;
        double midY = startY + (cornerY - startY) / 2;
        gc.setFill(Color.BLACK);
        gc.fillText(relationName, midX, midY - 10);

        drawMultiplicity(gc,
                startX, startY, sourceClassMultiplicity,
                endX, cornerY, targetClassMultiplicity,
                x, y, x + width, y + height, // Source class bounds
                x, y, x + width, y + height  // Target class bounds (same as source for self-association)
        );

    }



    public void drawRealization(GraphicsContext gc)
    {
        double[] originalDashes = gc.getLineDashes();

        double x1 = sourceClass.getX();
        double y1 = sourceClass.getY();
        double x2 = targetInterface.getX();
        double y2 = targetInterface.getY();

        double sourceClassWidth = sourceClass.getWidth();
        double sourceClassHeight = sourceClass.getHeight();
        double targetInterfaceWidth = targetInterface.getWidth();
        double targetInterfaceHeight = targetInterface.getHeight();

        double[] adjustedSourceClass = calculateOrthogonalBorderIntersection(
                x1, y1, sourceClassWidth, sourceClassHeight, x1, y1, x2, y2
        );
        double[] adjustedTargetInterface = calculateOrthogonalBorderIntersection(
                x2, y2, targetInterfaceWidth, targetInterfaceHeight, x2, y2, x1, y1
        );
        double adjX1 = adjustedSourceClass[0];
        double adjY1 = adjustedSourceClass[1];
        double adjX2 = adjustedTargetInterface[0];
        double adjY2 = adjustedTargetInterface[1];

        startX = adjX1;
        startY = adjY1;
        endX = adjX2;
        endY = adjY2;

        gc.setLineWidth(2);


        gc.setLineDashes(10, 5);

        gc.setStroke(Color.BLACK);

        double arrowLength = 15.0;
        double angle = Math.atan2(adjY2 - adjY1, adjX2 - adjX1);

        double stopX = adjX2 - arrowLength * Math.cos(angle);
        double stopY = adjY2 - arrowLength * Math.sin(angle);

        gc.setStroke(Color.BLACK);

        gc.strokeLine(adjX1, adjY1, stopX, stopY);

        gc.setLineDashes(originalDashes);
        drawEmptyArrowhead(gc, stopX, stopY, adjX2, adjY2);
    }


    public void drawGeneralization(GraphicsContext gc)
    {
        double[] originalDashes = gc.getLineDashes();

        double x1 = sourceClass.getX();
        double y1 = sourceClass.getY();
        double x2 = targetClass.getX();
        double y2 = targetClass.getY();

        double sourceClassWidth = sourceClass.getWidth();
        double sourceClassHeight = sourceClass.getHeight();
        double targetInterfaceWidth = targetClass.getWidth();
        double targetInterfaceHeight = targetClass.getHeight();

        double[] adjustedSourceClass = calculateOrthogonalBorderIntersection(
                x1, y1, sourceClassWidth, sourceClassHeight, x1, y1, x2, y2
        );
        double[] adjustedTargetInterface = calculateOrthogonalBorderIntersection(
                x2, y2, targetInterfaceWidth, targetInterfaceHeight, x2, y2, x1, y1
        );
        double adjX1 = adjustedSourceClass[0];
        double adjY1 = adjustedSourceClass[1];
        double adjX2 = adjustedTargetInterface[0];
        double adjY2 = adjustedTargetInterface[1];

        startX = adjX1;
        startY = adjY1;
        endX = adjX2;
        endY = adjY2;

        gc.setLineWidth(2);

        double arrowLength = 15.0;
        double angle = Math.atan2(adjY2 - adjY1, adjX2 - adjX1);

        double stopX = adjX2 - arrowLength * Math.cos(angle);
        double stopY = adjY2 - arrowLength * Math.sin(angle);

        // Draw the dashed line from source to the point before the arrowhead
        gc.setStroke(Color.BLACK);

        gc.strokeLine(adjX1, adjY1, stopX, stopY);

        gc.setLineDashes(originalDashes);
        // Draw the solid arrowhead
        drawEmptyArrowhead(gc, stopX, stopY, adjX2, adjY2);
    }


    private void drawEmptyArrowhead(GraphicsContext gc, double x1, double y1, double x2, double y2)
    {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowLength = 15.0;
        double arrowWidth = 7.0;

        double xTip = x2;
        double yTip = y2;

        double xLeft = x2 - arrowLength * Math.cos(angle) + arrowWidth * Math.sin(angle);
        double yLeft = y2 - arrowLength * Math.sin(angle) - arrowWidth * Math.cos(angle);

        double xRight = x2 - arrowLength * Math.cos(angle) - arrowWidth * Math.sin(angle);
        double yRight = y2 - arrowLength * Math.sin(angle) + arrowWidth * Math.cos(angle);

        gc.setStroke(Color.BLACK);
        gc.strokePolygon(new double[]{xTip, xLeft, xRight}, new double[]{yTip, yLeft, yRight}, 3);
    }

    private void drawDiamondArrowhead(GraphicsContext gc, String type, double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double diamondLength = 10.0;
        double diamondWidth = 7.0;

        double xTip = x2;
        double yTip = y2;

        double xBase = x2 - diamondLength * Math.cos(angle);
        double yBase = y2 - diamondLength * Math.sin(angle);

        double xLeft = xBase - diamondWidth * Math.sin(angle);
        double yLeft = yBase + diamondWidth * Math.cos(angle);

        double xRight = xBase + diamondWidth * Math.sin(angle);
        double yRight = yBase - diamondWidth * Math.cos(angle);

        double xBack = xBase - diamondLength * Math.cos(angle);
        double yBack = yBase - diamondLength * Math.sin(angle);

        if (type.equals("composition")) {
            gc.setFill(Color.BLACK);
            gc.fillPolygon(
                    new double[]{xTip, xLeft, xBack, xRight},
                    new double[]{yTip, yLeft, yBack, yRight},
                    4
            );
        } else if (type.equals("aggregation")) {
            gc.setStroke(Color.BLACK);
            gc.strokePolygon(
                    new double[]{xTip, xLeft, xBack, xRight},
                    new double[]{yTip, yLeft, yBack, yRight},
                    4
            );
        }
    }




    public double[] calculateOrthogonalBorderIntersection(
            double rectX, double rectY, double rectWidth, double rectHeight,
            double sourceX, double sourceY, double targetX, double targetY) {

        double[] sourcePoints = {
                rectX, rectY,                           // Top-left corner
                rectX + rectWidth, rectY,               // Top-right corner
                rectX, rectY + rectHeight,              // Bottom-left corner
                rectX + rectWidth, rectY + rectHeight   // Bottom-right corner
        };

        // Points for the target class
        double[] targetPoints = {
                targetX, targetY,                       // Top-left corner
                targetX + rectWidth, targetY,           // Top-right corner
                targetX, targetY + rectHeight,          // Bottom-left corner
                targetX + rectWidth, targetY + rectHeight  // Bottom-right corner
        };

        double minDistance = Double.MAX_VALUE;
        double[] closestSourcePoint = new double[2];
        double[] closestTargetPoint = new double[2];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double distance = Math.sqrt(Math.pow(sourcePoints[i * 2] - targetPoints[j * 2], 2) +
                        Math.pow(sourcePoints[i * 2 + 1] - targetPoints[j * 2 + 1], 2));

                if (distance < minDistance) {
                    minDistance = distance;
                    closestSourcePoint[0] = sourcePoints[i * 2];
                    closestSourcePoint[1] = sourcePoints[i * 2 + 1];
                    closestTargetPoint[0] = targetPoints[j * 2];
                    closestTargetPoint[1] = targetPoints[j * 2 + 1];
                }
            }
        }

        return new double[]{closestSourcePoint[0], closestSourcePoint[1], closestTargetPoint[0], closestTargetPoint[1]};
    }


    private void drawMultiplicity(GraphicsContext gc, double x1, double y1, String sourceMultiplicity,
                                  double x2, double y2, String targetMultiplicity,
                                  double sourceMinX, double sourceMinY, double sourceMaxX, double sourceMaxY,
                                  double targetMinX, double targetMinY, double targetMaxX, double targetMaxY) {
        if ((sourceMultiplicity == null || Objects.equals(sourceMultiplicity, "0")) &&
                (targetMultiplicity == null || Objects.equals(targetMultiplicity, "0"))) {
            return;
        }

        final double offset = 15;

        if (sourceMultiplicity != null && !sourceMultiplicity.equals("0")) {
            double textX = x1;
            double textY = y1;

            if (x1 <= sourceMinX) {
                textX = sourceMinX - offset;
            } else if (x1 >= sourceMaxX) {
                textX = sourceMaxX + offset;
            }

            if (y1 <= sourceMinY) {
                textY = sourceMinY - offset;
            } else if (y1 >= sourceMaxY) {
                textY = sourceMaxY + offset;
            }

            gc.fillText(sourceMultiplicity, textX, textY);
        }

        if (targetMultiplicity != null && !targetMultiplicity.equals("0")) {
            double textX = x2;
            double textY = y2;

            if (x2 <= targetMinX) {
                textX = targetMinX - offset;
            } else if (x2 >= targetMaxX) {
                textX = targetMaxX + offset;
            }

            if (y2 <= targetMinY) {
                textY = targetMinY - offset;
            } else if (y2 >= targetMaxY) {
                textY = targetMaxY + offset;
            }

            gc.fillText(targetMultiplicity, textX, textY);
        }
    }


    public void setStartX(double startX) {
        this.startX = startX;
    }
    public void setStartY(double startY) {
        this.startY = startY;
    }
    public void setEndX(double endX) {
        this.endX = endX;
    }
    public void setEndY(double endY) {
        this.endY = endY;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setSourceClass(ClassDiagram sourceClass) {
        this.sourceClass = sourceClass;
    }

    public void setTargetClass(ClassDiagram targetClass) {
        this.targetClass = targetClass;
    }

    public void setTargetInterface(InterfaceData targetInterface) {
        this.targetInterface = targetInterface;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSourceMultiplicity(String sourceClassMultiplicity) {
        this.sourceClassMultiplicity = sourceClassMultiplicity;
    }

    public void setTargetMultiplicity(String targetClassMultiplicity) {
        this.targetClassMultiplicity = targetClassMultiplicity;
    }

    public ClassDiagram getSourceClass() {
        return sourceClass;
    }

    public ClassDiagram getTargetClass() {
        return targetClass;
    }

    public InterfaceData getTargetInterface() {
        return targetInterface;
    }

    public String getSourceClassMultiplicity() {
        return sourceClassMultiplicity;
    }

    public String getTargetClassMultiplicity() {
        return targetClassMultiplicity;
    }

    public String getRelationName() {
        return relationName;
    }

    public List<Rectangle> getObstacles() {
        return obstacles;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public void setObstacles(List<Rectangle> obstacles) {
        this.obstacles = obstacles;
    }

    public String getType() {
        return type;
    }

}


