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

/**
 * Represents a relationship between two entities (either classes or a class and an interface) in a class diagram.
 * Supports various relationship types such as association, composition, aggregation, realization, and generalization.
 * Provides methods for drawing these relationships, including handling multiplicity, obstacles, and different arrowheads for visualization.
 *
 * Fields include the source and target entities, the relationship type, multiplicities, and obstacles, along with graphical properties
 * for drawing the relationship line and arrows.
 */
public class Relationship {
    /**
     * The source class of the relationship.
     * This represents the class that is at the origin of the relationship.
     */
    private ClassDiagram sourceClass;

    /**
     * The target class of the relationship.
     * This represents the class or interface at the other end of the relationship.
     */
    private ClassDiagram targetClass;

    /**
     * The target interface of the relationship, if applicable.
     * Represents the interface involved in a "realization" relationship.
     */
    private InterfaceData targetInterface;

    /**
     * The type of relationship. Possible values include:
     * <ul>
     *     <li>"association"</li>
     *     <li>"composition"</li>
     *     <li>"aggregation"</li>
     *     <li>"Realization"</li>
     * </ul>
     */
    public String type;

    /**
     * The multiplicity of the source class in the relationship (e.g., "1", "0..*", etc.).
     */
    private String sourceClassMultiplicity;

    /**
     * The multiplicity of the target class in the relationship (e.g., "1", "0..*", etc.).
     */
    private String targetClassMultiplicity;

    /**
     * The name of the relationship, which could represent the role or nature of the relationship.
     */
    private String relationName;

    /**
     * A constant offset value used for drawing or positioning the relationship on a canvas.
     */
    private static final double OFFSET = 50.0;

    /**
     * A list of obstacles that may interfere with drawing the relationship on the canvas.
     * Obstacles are typically represented as rectangles that need to be considered when placing or drawing the relationship.
     */
    private List<Rectangle> obstacles = new ArrayList<>();

    /**
     * The starting x and y coordinates of the relationship on the canvas.
     * These represent the initial position of the line or arrow that connects the source and target classes.
     */
    double startX, startY;

    /**
     * The ending x and y coordinates of the relationship on the canvas.
     * These represent the final position of the line or arrow connecting the source and target classes.
     */
    double endX, endY;

    /**
     * A property representing the name of the relationship.
     * This property allows for binding in a UI context (e.g., in JavaFX).
     */
    private final StringProperty relationNameProperty = new SimpleStringProperty();

    /**
     * Constructs a new Relationship between two classes with specified attributes.
     * This constructor is used for relationships between two classes, such as association, composition, or aggregation.
     *
     * @param sourceClass The source class in the relationship.
     * @param targetClass The target class in the relationship.
     * @param type The type of relationship (e.g., "association", "composition", "aggregation", etc.).
     * @param sourceClassMultiplicity The multiplicity at the source class end (e.g., "1", "0..*", etc.).
     * @param targetClassMultiplicity The multiplicity at the target class end (e.g., "1", "0..*", etc.).
     * @param obstacles A list of obstacles (rectangles) that may affect the relationship's drawing on the canvas.
     * @param relationName The name of the relationship, which typically represents the role or nature of the relationship.
     */
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

    /**
     * Constructs a new Relationship between a class and an interface with specified attributes.
     * This constructor is used for relationships where a class realizes an interface (e.g., realization).
     *
     * @param sourceClass The source class in the relationship.
     * @param targetInterface The target interface in the relationship.
     * @param type The type of relationship (e.g., "realization").
     * @param sourceClassMultiplicity The multiplicity at the source class end (e.g., "1", "0..*", etc.).
     * @param targetClassMultiplicity The multiplicity at the target interface end (e.g., "1", "0..*", etc.).
     * @param obstacles A list of obstacles (rectangles) that may affect the relationship's drawing on the canvas.
     */
    public Relationship(ClassDiagram sourceClass, InterfaceData targetInterface, String type, String sourceClassMultiplicity, String targetClassMultiplicity, List<Rectangle> obstacles) {
        this.sourceClass = sourceClass;
        this.targetInterface = targetInterface;
        this.type = type;
        this.sourceClassMultiplicity = sourceClassMultiplicity;
        this.targetClassMultiplicity = targetClassMultiplicity;
        this.obstacles=obstacles;
        this.relationNameProperty.set(type);
    }

    /**
     * Gets the relation type of this relationship.
     *
     * @return The relation type, represented as a {@link String}, such as "association", "composition", etc.
     */
    public String getRelationType() {
        return relationNameProperty.get();
    }

    /**
     * Sets the relation type of this relationship.
     *
     * @param relationName The relation type to set, represented as a {@link String}.
     */
    public void setRelationType(String relationName) {
        this.relationNameProperty.set(relationName);
    }

    /**
     * Provides access to the property that holds the relation name.
     * This allows for property binding in UI frameworks like JavaFX.
     *
     * @return The {@link StringProperty} containing the relation name.
     */
    public StringProperty relationNameProperty() {
        return relationNameProperty;
    }

    /**
     * Draws the visual representation of the relationship between the source and target class (or interface).
     * The relationship is drawn as a line with optional multiplicity and arrowheads depending on the type.
     *
     * @param gc The {@link GraphicsContext} used to draw the relationship on the canvas.
     */
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

    /**
     * Draws the visual representation of a self-association for a class.
     * A self-association is represented by a loop at the edge of the class rectangle.
     *
     * @param gc The {@link GraphicsContext} used to draw the self-association on the canvas.
     * @param x The x-coordinate of the top-left corner of the class.
     * @param y The y-coordinate of the top-left corner of the class.
     * @param width The width of the class rectangle.
     * @param height The height of the class rectangle.
     */
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

    /**
     * Draws a realization relationship between a class and an interface.
     * This method is typically used for realization relationships (e.g., "implements" or "realizes").
     *
     * @param gc The {@link GraphicsContext} used to draw the realization relationship on the canvas.
     */
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

    /**
     * Draws a generalization relationship between the source class and target class (or interface).
     * The relationship is represented by a dashed line and an arrowhead, with the arrowhead pointing from
     * the target class/interface to the source class.
     *
     * @param gc The {@link GraphicsContext} used to draw the generalization relationship on the canvas.
     */
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

    /**
     * Draws an empty arrowhead (a simple triangular arrow) at the given coordinates.
     * The arrowhead is used to indicate the direction of the relationship, typically at the end of a line.
     *
     * @param gc The {@link GraphicsContext} used to draw the arrowhead on the canvas.
     * @param x1 The x-coordinate of the starting point of the arrow.
     * @param y1 The y-coordinate of the starting point of the arrow.
     * @param x2 The x-coordinate of the end point of the arrow (tip).
     * @param y2 The y-coordinate of the end point of the arrow (tip).
     */
    public void drawEmptyArrowhead(GraphicsContext gc, double x1, double y1, double x2, double y2)
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

    /**
     * Draws a diamond-shaped arrowhead for the given relationship type (composition or aggregation).
     * The arrowhead is used to visually indicate the relationship type between two classes, with the
     * shape of the arrowhead differing based on the type of relationship.
     *
     * @param gc The {@link GraphicsContext} used to draw the diamond-shaped arrowhead on the canvas.
     * @param type The type of relationship, either "composition" or "aggregation".
     * @param x1 The x-coordinate of the starting point of the arrow.
     * @param y1 The y-coordinate of the starting point of the arrow.
     * @param x2 The x-coordinate of the end point of the arrow (tip).
     * @param y2 The y-coordinate of the end point of the arrow (tip).
     */
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

    /**
     * Calculates the orthogonal intersection points of a line between two points (source and target) with the
     * edges of two rectangles (representing the source and target classes).
     * The method computes the closest points on the edges of both rectangles and returns these intersection points.
     *
     * @param rectX The x-coordinate of the top-left corner of the source class rectangle.
     * @param rectY The y-coordinate of the top-left corner of the source class rectangle.
     * @param rectWidth The width of the source class rectangle.
     * @param rectHeight The height of the source class rectangle.
     * @param sourceX The x-coordinate of the source class.
     * @param sourceY The y-coordinate of the source class.
     * @param targetX The x-coordinate of the target class.
     * @param targetY The y-coordinate of the target class.
     * @return A double array containing the closest points for the source and target class rectangles,
     *         in the form: [sourceX, sourceY, targetX, targetY].
     */
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


    /**
     * Draws the multiplicity labels for a relationship, positioning them appropriately near the
     * source and target class elements based on the given coordinates and multiplicity values.
     *
     * @param gc The {@link GraphicsContext} used to draw the multiplicity labels on the canvas.
     * @param x1 The x-coordinate of the source class.
     * @param y1 The y-coordinate of the source class.
     * @param sourceMultiplicity The multiplicity for the source class (e.g., "1", "0..*").
     * @param x2 The x-coordinate of the target class.
     * @param y2 The y-coordinate of the target class.
     * @param targetMultiplicity The multiplicity for the target class (e.g., "1", "0..*").
     * @param sourceMinX The minimum x-coordinate of the source class bounds.
     * @param sourceMinY The minimum y-coordinate of the source class bounds.
     * @param sourceMaxX The maximum x-coordinate of the source class bounds.
     * @param sourceMaxY The maximum y-coordinate of the source class bounds.
     * @param targetMinX The minimum x-coordinate of the target class bounds.
     * @param targetMinY The minimum y-coordinate of the target class bounds.
     * @param targetMaxX The maximum x-coordinate of the target class bounds.
     * @param targetMaxY The maximum y-coordinate of the target class bounds.
     */
    public void drawMultiplicity(GraphicsContext gc, double x1, double y1, String sourceMultiplicity,
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


    /**
     * Sets the starting x-coordinate for the relationship line.
     *
     * @param startX The x-coordinate of the starting point.
     */
    public void setStartX(double startX) {
        this.startX = startX;
    }

    /**
     * Sets the starting y-coordinate for the relationship line.
     *
     * @param startY The y-coordinate of the starting point.
     */
    public void setStartY(double startY) {
        this.startY = startY;
    }

    /**
     * Sets the ending x-coordinate for the relationship line.
     *
     * @param endX The x-coordinate of the ending point.
     */
    public void setEndX(double endX) {
        this.endX = endX;
    }

    /**
     * Sets the ending y-coordinate for the relationship line.
     *
     * @param endY The y-coordinate of the ending point.
     */
    public void setEndY(double endY) {
        this.endY = endY;
    }

    /**
     * Gets the starting x-coordinate for the relationship line.
     *
     * @return The x-coordinate of the starting point.
     */
    public double getStartX() {
        return startX;
    }

    /**
     * Gets the starting y-coordinate for the relationship line.
     *
     * @return The y-coordinate of the starting point.
     */
    public double getStartY() {
        return startY;
    }

    /**
     * Gets the ending x-coordinate for the relationship line.
     *
     * @return The x-coordinate of the ending point.
     */
    public double getEndX() {
        return endX;
    }

    /**
     * Gets the ending y-coordinate for the relationship line.
     *
     * @return The y-coordinate of the ending point.
     */
    public double getEndY() {
        return endY;
    }


    /**
     * Sets the source class for the relationship.
     *
     * @param sourceClass The {@link ClassDiagram} object representing the source class.
     */
    public void setSourceClass(ClassDiagram sourceClass) {
        this.sourceClass = sourceClass;
    }

    /**
     * Sets the target class for the relationship.
     *
     * @param targetClass The {@link ClassDiagram} object representing the target class.
     */
    public void setTargetClass(ClassDiagram targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Sets the target interface for the relationship.
     *
     * @param targetInterface The {@link InterfaceData} object representing the target interface.
     */
    public void setTargetInterface(InterfaceData targetInterface) {
        this.targetInterface = targetInterface;
    }

    /**
     * Sets the type of relationship (e.g., "association", "generalization", "composition").
     *
     * @param type The type of relationship.
     */

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the multiplicity for the source class in the relationship.
     *
     * @param sourceClassMultiplicity The multiplicity for the source class (e.g., "1", "0..*").
     */
    public void setSourceMultiplicity(String sourceClassMultiplicity) {
        this.sourceClassMultiplicity = sourceClassMultiplicity;
    }

    /**
     * Sets the multiplicity for the target class in the relationship.
     *
     * @param targetClassMultiplicity The multiplicity for the target class (e.g., "1", "0..*").
     */
    public void setTargetMultiplicity(String targetClassMultiplicity) {
        this.targetClassMultiplicity = targetClassMultiplicity;
    }

    /**
     * Gets the source class of the relationship.
     *
     * @return The {@link ClassDiagram} object representing the source class.
     */
    public ClassDiagram getSourceClass() {
        return sourceClass;
    }

    /**
     * Gets the target class of the relationship.
     *
     * @return The {@link ClassDiagram} object representing the target class.
     */
    public ClassDiagram getTargetClass() {
        return targetClass;
    }

    /**
     * Gets the target interface of the relationship.
     *
     * @return The {@link InterfaceData} object representing the target interface.
     */
    public InterfaceData getTargetInterface() {
        return targetInterface;
    }

    /**
     * Gets the multiplicity of the source class in the relationship.
     *
     * @return The multiplicity of the source class (e.g., "1", "0..*").
     */
    public String getSourceClassMultiplicity() {
        return sourceClassMultiplicity;
    }

    /**
     * Gets the multiplicity of the target class in the relationship.
     *
     * @return The multiplicity of the target class (e.g., "1", "0..*").
     */
    public String getTargetClassMultiplicity() {
        return targetClassMultiplicity;
    }

    /**
     * Gets the name of the relationship.
     *
     * @return The name of the relationship (e.g., "Inheritance", "Association").
     */
    public String getRelationName() {
        return relationName;
    }

    /**
     * Gets the list of obstacles (rectangles) that may interfere with the drawing of the relationship.
     *
     * @return A list of {@link Rectangle} objects representing the obstacles.
     */
    public List<Rectangle> getObstacles() {
        return obstacles;
    }


    /**
     * Sets the name of the relationship.
     *
     * @param relationName The name to set for the relationship (e.g., "Inheritance", "Association").
     */
    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    /**
     * Sets the list of obstacles (rectangles) that may interfere with the drawing of the relationship.
     *
     * @param obstacles A list of {@link Rectangle} objects representing the obstacles.
     */
    public void setObstacles(List<Rectangle> obstacles) {
        this.obstacles = obstacles;
    }

    /**
     * Gets the type of relationship.
     *
     * @return A string representing the type of relationship (e.g., "association", "generalization").
     */
    public String getType() {
        return type;
    }

}


