package org.example.craftuml.Service;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.example.craftuml.UI.InterfaceDiagramUI;
import org.example.craftuml.UI.classDiagramUI;
import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.ClassDiagrams.MethodData;
import org.example.craftuml.models.Relationship;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class ClassDashboardController {
    @FXML
    private Canvas drawingCanvas;

    @FXML
    private ListView<String> modelInfoList;

    private ClassDiagram classDiagram, activeDiagram;
    private InterfaceData interfaceDiagram, activeInterface;
    private Relationship activeRelationship;
    private boolean isDraggingSource = false;
    private boolean isDraggingTarget = false;

    private List<Rectangle> obstacles = new ArrayList<>();

    private List<InterfaceData> interfaceDiagrams = new ArrayList<>();


    private List<ClassDiagram> classDiagrams = new ArrayList<>();
    private double dragStartX = 0;
    private double dragStartY = 0;

    private List<Relationship> associations = new ArrayList<>();
    private List<Relationship> compositions = new ArrayList<>();
    private List<Relationship> aggregations = new ArrayList<>();
    private List<Relationship> realizations = new ArrayList<>();
    private List<Relationship> generalizations = new ArrayList<>();
    private ContextMenu contextMenu;



    @FXML
    public void initialize() {

        initializeCanvasHandlers();
    }

    public void redrawCanvas() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        for (ClassDiagram diagram : classDiagrams) {
            createClassDiagram(diagram);
        }

        for (InterfaceData diagram : interfaceDiagrams) {
            createInterfaceDiagram(diagram);
        }

        for (Relationship relationship : associations) {
            relationship.draw(gc);
        }
        for (Relationship relationship : compositions) {
            relationship.draw(gc);
        }
        for (Relationship relationship : aggregations) {
            relationship.draw(gc);
        }
        for (Relationship relationship : realizations) {
            relationship.drawRealization(gc);
        }
        for (Relationship relationship : generalizations) {
            relationship.drawGeneralization(gc);
        }

    }

    @FXML
    private void handleClassDiagram() {
        classDiagramUI classDiagramUI = new classDiagramUI(drawingCanvas);
        classDiagram = classDiagramUI.showClassDiagramDialog();

        double newX = 20 + (classDiagrams.size() * 100) % (drawingCanvas.getWidth() - 100);
        double newY = 20 + ((classDiagrams.size() * 100) / (drawingCanvas.getWidth() - 100)) * 100;

        classDiagram.setX(newX);
        classDiagram.setY(newY);
        classDiagrams.add(classDiagram);
        createClassDiagram(classDiagram);
    }

    public void addClassDiagramAsObstacle(ClassDiagram classDiagram) {
        double x = classDiagram.getX();
        double y = classDiagram.getY();
        double width = calculateDiagramWidth(classDiagram, drawingCanvas.getGraphicsContext2D());
        double height = calculateDiagramHeight(classDiagram);
        obstacles.add(new Rectangle(x, y, width, height));
    }


    public void createClassDiagram(ClassDiagram classDiagram) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        double x = classDiagram.getX();
        double y = classDiagram.getY();

        double height = 1;

        int attributeCount = classDiagram.getAttributes().size();
        int methodCount = classDiagram.getMethods().size();

        height += Math.max(attributeCount, 1) * 30;
        height += Math.max(methodCount, 1) * 30;

        double classNameHeight = 30;

        int totalItems = attributeCount + methodCount;

        double attributeHeight = 0;
        double methodHeight = 0;

        if (totalItems > 0) {
            double attributeProportion = (double) attributeCount / totalItems;
            double methodProportion = (double) methodCount / totalItems;

            attributeHeight = (height - classNameHeight) * attributeProportion;
            methodHeight = (height - classNameHeight) * methodProportion;
        } else {
            attributeHeight = (height - classNameHeight) / 2;
            methodHeight = (height - classNameHeight) / 2;
        }

        double maxWidth = 0;

        for (AttributeData at : classDiagram.getAttributes()) {
            String attributeText = at.getAccessModifier() + " " + at.getName() + " : " + at.getDataType();
            Text tempText = new Text(attributeText);
            tempText.setFont(gc.getFont());
            double textWidth = tempText.getLayoutBounds().getWidth();
            maxWidth = Math.max(maxWidth, textWidth);
        }

        for (MethodData md : classDiagram.getMethods()) {
            String methodText = md.getAccessModifier() + " " + md.getName() + " : " + md.getReturnType();
            Text tempText = new Text(methodText);
            tempText.setFont(gc.getFont());
            double textWidth = tempText.getLayoutBounds().getWidth();
            maxWidth = Math.max(maxWidth, textWidth);
        }

        double width = maxWidth + 40;

        classDiagram.setWidth(width);
        classDiagram.setHeight(height);

        Rectangle diagramRectangle = new Rectangle(x, y, width, height);
        classDiagram.setRectangle(diagramRectangle);

        addClassDiagramAsObstacle(classDiagram);

        if (classDiagram == activeDiagram) {
            gc.setStroke(new Color(0.47, 0.35, 0.65, 1.0));
        } else {
            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        gc.strokeLine(x, y + classNameHeight, x + width, y + classNameHeight);
        gc.strokeLine(x, y + classNameHeight + attributeHeight, x + width, y + classNameHeight + attributeHeight);

        gc.setFill(Color.BLACK);
        String className = classDiagram.getName();

        Text tempText = new Text(className);
        tempText.setFont(gc.getFont());
        double classNameWidth = tempText.getLayoutBounds().getWidth();

        gc.fillText(className, x + (width - classNameWidth) / 2, y + classNameHeight / 2 + 10);

        double attrY = y + classNameHeight + 15;
        for (AttributeData at : classDiagram.getAttributes()) {
            StringBuilder attribute = new StringBuilder();
            attribute.append(at.getAccessModifier()).append(" ");
            attribute.append(at.getName()).append(" : ");
            attribute.append(at.getDataType());

            gc.fillText(attribute.toString(), x + 10, attrY);
            attrY += 20;
        }

        double methY = y + classNameHeight + attributeHeight + 15;
        for (MethodData md : classDiagram.getMethods()) {
            StringBuilder method = new StringBuilder();
            method.append(md.getAccessModifier()).append(" ");
            method.append(md.getName()).append(" : ");
            method.append(md.getReturnType());

            gc.fillText(method.toString(), x + 10, methY);
            methY += 20;
        }

        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem(" Edit     ");
        MenuItem deleteItem = new MenuItem(" Delete    ");
        contextMenu.getItems().addAll(editItem, deleteItem);
    }

    private void deleteClassDiagram(ClassDiagram classDiagram) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this class diagram?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            associations.removeIf(relationship ->
                    relationship.getSourceClass().equals(classDiagram) ||
                            relationship.getTargetClass().equals(classDiagram)
            );

            compositions.removeIf(relationship ->
                    relationship.getSourceClass().equals(classDiagram) ||
                            relationship.getTargetClass().equals(classDiagram)
            );

            aggregations.removeIf(relationship ->
                    relationship.getSourceClass().equals(classDiagram) ||
                            relationship.getTargetClass().equals(classDiagram)
            );
            realizations.removeIf(relationship ->
                    relationship.getSourceClass().equals(classDiagram) ||
                            (relationship.getTargetInterface() != null &&
                                    relationship.getTargetInterface().equals(classDiagram))
            );
            generalizations.removeIf(relationship ->
                    relationship.getSourceClass().equals(classDiagram) ||
                            relationship.getTargetClass().equals(classDiagram)
            );
            classDiagrams.remove(classDiagram);
            activeDiagram = null;
            redrawCanvas();
        }
    }


    private void editClassDiagram(ClassDiagram classDiagram) {
        classDiagramUI classDiagramUI = new classDiagramUI(drawingCanvas, classDiagram);
        ClassDiagram updatedDiagram = classDiagramUI.showClassDiagramDialog();

        if (updatedDiagram != null) {
            activeDiagram.setName(updatedDiagram.getName());
            activeDiagram.setAttributes(updatedDiagram.getAttributes());
            activeDiagram.setMethods(updatedDiagram.getMethods());

            redrawCanvas();
        }
    }

    private boolean isWithinBounds(double mouseX, double mouseY, ClassDiagram diagram, GraphicsContext gc) {
        // Get the position and size of the diagram
        double x = diagram.getX();
        double y = diagram.getY();
        double width = calculateDiagramWidth(diagram, gc);
        double height = calculateDiagramHeight(diagram);

        // Check if mouse is within the diagram's bounds
        boolean isWithin = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        return isWithin;
    }

    private double calculateDiagramWidth(ClassDiagram diagram, GraphicsContext gc) {
        double maxWidth = 0;
        Text tempText = new Text(diagram.getName());
        tempText.setFont(gc.getFont());
        maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());

        for (AttributeData attribute : diagram.getAttributes()) {
            String attributeText = attribute.getAccessModifier() + " " + attribute.getName() + " : " + attribute.getDataType();
            tempText = new Text(attributeText);
            tempText.setFont(gc.getFont());
            maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());
        }
        for (MethodData method : diagram.getMethods()) {
            String methodText = method.getAccessModifier() + " " + method.getName() + " : " + method.getReturnType();
            tempText = new Text(methodText);
            tempText.setFont(gc.getFont());
            maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());
        }
        return maxWidth + 40;
    }

    private double calculateDiagramHeight(ClassDiagram diagram) {
        double classNameHeight = 30;  // Fixed height for class name
        double attributeHeight = 0;
        for (AttributeData attribute : diagram.getAttributes()) {
            // You can estimate or calculate the height based on the text size
            Text tempText = new Text(attribute.getAccessModifier() + " " + attribute.getName() + " : " + attribute.getDataType());
            tempText.setFont(new Font("Arial", 12)); // Use the actual font being used
            attributeHeight += tempText.getLayoutBounds().getHeight();
        }

        double methodHeight = 0;
        for (MethodData method : diagram.getMethods()) {
            Text tempText = new Text(method.getAccessModifier() + " " + method.getName() + " : " + method.getReturnType());
            tempText.setFont(new Font("Arial", 12)); // Use the actual font being used
            methodHeight += tempText.getLayoutBounds().getHeight();
        }

        return classNameHeight + attributeHeight + methodHeight + 10; // Adding padding
    }

    private void initializeCanvasHandlers() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        double tolerance = 5.0;

        drawingCanvas.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                boolean found = false;
                activeDiagram = null;
                activeInterface = null;
                activeRelationship = null;

                for (ClassDiagram diagram : classDiagrams) {
                    if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                        activeDiagram = diagram;
                        showContextMenu(event, "class");
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    for (InterfaceData diagram : interfaceDiagrams) {
                        if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                            activeInterface = diagram;
                            showContextMenu(event, "interface");
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    for (Relationship relationship : associations) {
                        if (isWithinBounds(event.getX(), event.getY(), relationship, gc)) {
                            activeRelationship = relationship;
                            showContextMenu(event, "relationship");
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    for (Relationship relationship : compositions) {
                        if (isWithinBounds(event.getX(), event.getY(), relationship, gc)) {
                            activeRelationship = relationship;
                            showContextMenu(event, "relationship");
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    for (Relationship relationship : aggregations) {
                        if (isWithinBounds(event.getX(), event.getY(), relationship, gc)) {
                            activeRelationship = relationship;
                            showContextMenu(event, "relationship");
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    for (Relationship relationship : generalizations) {
                        if (isWithinBounds(event.getX(), event.getY(), relationship, gc)) {
                            activeRelationship = relationship;
                            showContextMenu(event, "relationship");
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    for (Relationship relationship : realizations) {
                        if (isWithinBounds(event.getX(), event.getY(), relationship, gc)) {
                            activeRelationship = relationship;
                            showContextMenu(event, "relationship");
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    activeDiagram = null;
                    activeInterface = null;
                    activeRelationship = null;
                }

                redrawCanvas();
            } else if (event.isPrimaryButtonDown()) {
                activeDiagram = null;
                activeInterface = null;
                activeRelationship = null;

                for (ClassDiagram diagram : classDiagrams) {
                    if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                        activeDiagram = diagram;
                        dragStartX = event.getX() - diagram.getX();
                        dragStartY = event.getY() - diagram.getY();
                        break;
                    }
                }

                if (activeDiagram == null) {
                    for (InterfaceData diagram : interfaceDiagrams) {
                        if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                            activeInterface = diagram;
                            dragStartX = event.getX() - diagram.getX();
                            dragStartY = event.getY() - diagram.getY();
                            break;
                        }
                    }
                }
                if (activeInterface == null) {
                    for (Relationship relationship : associations) {
                        if (isWithinBounds(event.getX(), event.getY(), relationship, gc)) {
                            activeRelationship = relationship;
                            break;
                        }
                    }
                }
            }
        });

        drawingCanvas.setOnMouseDragged(event -> {
            if (activeDiagram != null) {
                // Handle dragging ClassDiagram
                double newX = event.getX() - dragStartX;
                double newY = event.getY() - dragStartY;

                activeDiagram.setX(newX);
                activeDiagram.setY(newY);

                redrawCanvas();
            } else if (activeInterface != null) {
                double newX = event.getX() - dragStartX;
                double newY = event.getY() - dragStartY;

                activeInterface.setX(newX);
                activeInterface.setY(newY);

                redrawCanvas();
            }
        });

        drawingCanvas.setOnMouseReleased(event -> {
            isDraggingSource = false;
            isDraggingTarget = false;
        });

        drawingCanvas.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            boolean isOverClassDiagram = false;
            for (ClassDiagram diagram : classDiagrams) {
                if (isWithinBounds(mouseX, mouseY, diagram, gc)) {
                    isOverClassDiagram = true;
                    break;
                }
            }

            boolean isOverInterfaceDiagram = false;
            for (InterfaceData diagram : interfaceDiagrams) {
                if (isWithinBounds(mouseX, mouseY, diagram, gc)) {
                    isOverInterfaceDiagram = true;
                    break;
                }
            }
            boolean isOverRelationship = false;
            for (Relationship relationship : associations) {
                if (isWithinBounds(mouseX, mouseY, relationship, gc)) {
                    isOverRelationship = true;
                    break;
                }
            }
            for (Relationship relationship : compositions) {
                if (isWithinBounds(mouseX, mouseY, relationship, gc)) {
                    isOverRelationship = true;
                    break;
                }
            }
            for (Relationship relationship : aggregations) {
                if (isWithinBounds(mouseX, mouseY, relationship, gc)) {
                    isOverRelationship = true;
                    break;
                }
            }
            for (Relationship relationship : generalizations) {
                if (isWithinBounds(mouseX, mouseY, relationship, gc)) {
                    isOverRelationship = true;
                    break;
                }
            }
            for (Relationship relationship : realizations) {
                if (isWithinBounds(mouseX, mouseY, relationship, gc)) {
                    isOverRelationship = true;
                    break;
                }
            }

            if (isOverClassDiagram || isOverInterfaceDiagram) {
                drawingCanvas.setCursor(Cursor.MOVE);
            } else if (isOverRelationship) {
                drawingCanvas.setCursor(Cursor.HAND);
            } else {
                drawingCanvas.setCursor(Cursor.DEFAULT);
            }
        });


        drawingCanvas.setOnMouseExited(event ->
        {
            drawingCanvas.setCursor(Cursor.DEFAULT);
        });
        drawingCanvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
            {
                if (activeDiagram != null) {
                    editClassDiagram(activeDiagram);
                } else if (activeInterface != null) {
                    editInterfaceDiagram(activeInterface);
                } else if (activeRelationship != null) {
                    editRelationship(activeRelationship);
                }
                closeContextMenu();
            } else
            {
                if (activeDiagram == null && activeInterface == null && activeRelationship == null) {
                    closeContextMenu();
                }
            }
        });

    }
    private void closeContextMenu() {
        if (contextMenu != null && contextMenu.isShowing()) {
            contextMenu.hide();
        }
    }

    private void editRelationship(Relationship activeRelationship) {
        if (activeRelationship != null && !activeRelationship.getType().equals("Realization"))
        {
            String sourceName = activeRelationship.getSourceClass().getName();
            String targetName = activeRelationship.getTargetClass().getName();
            String relationshipName = activeRelationship.getRelationName();

            String[] namesAndRelationshipName = promptForSourceAndTargetClassesWithDefaults(sourceName, targetName, relationshipName);

            if (namesAndRelationshipName == null) return;

            String newSourceName = namesAndRelationshipName[0];
            String newTargetName = namesAndRelationshipName[1];
            String newRelationshipName = namesAndRelationshipName[2];

            try {
                ClassDiagram newSource = findDiagramByName(newSourceName);
                ClassDiagram newTarget = findDiagramByName(newTargetName);

                activeRelationship.setSourceClass(newSource);
                activeRelationship.setTargetClass(newTarget);
                activeRelationship.setRelationName(newRelationshipName);
                redrawCanvas();
            } catch (IllegalArgumentException e) {
                showErrorAlert(e.getMessage());
            }
        }
        else if (activeRelationship != null && activeRelationship.getType().equals("Realization")) {
            String sourceName = activeRelationship.getSourceClass().getName();
            String targetName = activeRelationship.getTargetInterface().getName();

            String[] namesAndRelationshipName = promptForSourceAndTargetClassesWithDefaults2(sourceName, targetName);

            if (namesAndRelationshipName == null) return;

            String newSourceName = namesAndRelationshipName[0];
            String newTargetName = namesAndRelationshipName[1];

            try {
                ClassDiagram newSource = findDiagramByName(newSourceName);
                InterfaceData newTarget = findInterfaceDiagramByName(newTargetName);

                // Update the active relationship with the new values
                activeRelationship.setSourceClass(newSource);
                activeRelationship.setTargetInterface(newTarget);
                redrawCanvas();
            } catch (IllegalArgumentException e) {
                showErrorAlert(e.getMessage());
            }
        }


        else
        {
            if(activeRelationship.getType().equals("association"))
            handleAddAssociation();

            else if(activeRelationship.getType().equals("aggregation"))
                handleAddAggregation();

            else if(activeRelationship.getType().equals("composition"))
                handleAddComposition();

            else if(activeRelationship.getType().equals("generalization"))
                handleAddGeneralization();
        }


    }

    private String[] promptForSourceAndTargetClassesWithDefaults2(String sourceName, String targetName) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add/Edit Relationship");
        dialog.setHeaderText("Select the source and target class diagrams and enter a relationship name.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<String> sourceBox = new ComboBox<>();
        ComboBox<String> targetBox = new ComboBox<>();

        for (ClassDiagram diagram : classDiagrams) {
            sourceBox.getItems().add(diagram.getName());
        }
        for(InterfaceData diagram : interfaceDiagrams)
        {
            targetBox.getItems().add(diagram.getName());
        }

        // Set the default values if they are provided
        sourceBox.setValue(sourceName);
        targetBox.setValue(targetName);

        sourceBox.setPromptText("Select Source");
        targetBox.setPromptText("Select Target");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Source:"), 0, 0);
        grid.add(sourceBox, 1, 0);
        grid.add(new Label("Target:"), 0, 1);
        grid.add(targetBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Handle the result when the user clicks OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (sourceBox.getValue() != null && targetBox.getValue() != null) {
                    return new String[] { sourceBox.getValue(), targetBox.getValue()};
                }
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private boolean isWithinBounds(double mouseX, double mouseY, Relationship relationship, GraphicsContext gc) {
        double startX = relationship.getStartX();
        double startY = relationship.getStartY();
        double endX = relationship.getEndX();
        double endY = relationship.getEndY();

        double tolerance = 5.0;
        return isPointNearLine(mouseX, mouseY, startX, startY, endX, endY, tolerance);
    }

    private boolean isPointNearLine(double pointX, double pointY, double startX, double startY, double endX, double endY, double tolerance) {
        double lineLength = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        double distance = Math.abs((endY - startY) * pointX - (endX - startX) * pointY + endX * startY - endY * startX) / lineLength;
        return distance <= tolerance;
    }


    private void showContextMenu(MouseEvent event, String diagramType) {
        contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("   Edit   ");
        MenuItem deleteItem = new MenuItem("   Delete   ");

        contextMenu.getItems().addAll(editItem, deleteItem);

        if (diagramType.equals("class")) {
            editItem.setOnAction(e -> {
                if (activeDiagram != null) editClassDiagram(activeDiagram);
            });
            deleteItem.setOnAction(e -> {
                if (activeDiagram != null) deleteClassDiagram(activeDiagram);
            });
        } else if (diagramType.equals("interface")) {
            editItem.setOnAction(e -> {
                if (activeInterface != null) editInterfaceDiagram(activeInterface);
            });
            deleteItem.setOnAction(e -> {
                if (activeInterface != null) deleteInterfaceDiagram(activeInterface);
            });
        }
        else if(diagramType.equals("relationship"))
        {
            editItem.setOnAction(e -> {
                if (activeRelationship != null) editRelationship(activeRelationship);
            });
            deleteItem.setOnAction(e -> {
                if (activeRelationship != null) deleteRelationship(activeRelationship);
            });
        }

        contextMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
    }

    private void deleteRelationship(Relationship activeRelationship) {

            if (activeRelationship.getType().equals("association")) {
                associations.remove(activeRelationship);
            } else if (activeRelationship.getType().equals("composition")) {
                compositions.remove(activeRelationship);
            } else if (activeRelationship.getType().equals("aggregation")) {
                aggregations.remove(activeRelationship);
            } else if (activeRelationship.getType().equals("realization")) {
                realizations.remove(activeRelationship);
            } else if (activeRelationship.getType().equals("Generalization")) {
                generalizations.remove(activeRelationship);
            }
            activeRelationship = null;
            redrawCanvas();
    }

    @FXML
    private void handleAddInterface() {
        InterfaceDiagramUI interfaceDiagramUI = new InterfaceDiagramUI(drawingCanvas);
        interfaceDiagram = interfaceDiagramUI.showInterfaceDiagramDialog();

        double newX = 20 + (interfaceDiagrams.size() * 100) % (drawingCanvas.getWidth() - 100);
        double newY = 300 + ((interfaceDiagrams.size() * 100) / (drawingCanvas.getWidth() - 100)) * 100;

        interfaceDiagram.setX(newX);
        interfaceDiagram.setY(newY);

        interfaceDiagrams.add(interfaceDiagram);
        createInterfaceDiagram(interfaceDiagram);
    }


    public void createInterfaceDiagram(InterfaceData interfaceDiagram) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        double x = interfaceDiagram.getX();
        double y = interfaceDiagram.getY();
        double interfaceNameHeight = 40;
        double methodHeight = Math.max(30 * interfaceDiagram.getMethods().size(), 30);

        String stereotypeText = "<<interface>>";
        String interfaceName = interfaceDiagram.getName();
        double maxWidth = calculateTextWidth(stereotypeText, gc);
        maxWidth = Math.max(maxWidth, calculateTextWidth(interfaceName, gc));
        for (MethodData md : interfaceDiagram.getMethods()) {
            String methodText = md.getAccessModifier() + " " + md.getName() + " : " + md.getReturnType();
            maxWidth = Math.max(maxWidth, calculateTextWidth(methodText, gc));
        }

        double width = maxWidth * 1.3;
        double height = interfaceNameHeight + methodHeight;

        interfaceDiagram.setWidth(width);
        interfaceDiagram.setHeight(height);

        if (interfaceDiagram == activeInterface) {
            gc.setStroke(new Color(0.47, 0.35, 0.65, 1.0));
        } else {
            gc.setStroke(Color.BLACK);
        }
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        gc.strokeLine(x, y + interfaceNameHeight, x + width, y + interfaceNameHeight);

        gc.setFill(Color.BLACK);
        double stereotypeWidth = calculateTextWidth(stereotypeText, gc);
        gc.fillText(stereotypeText, x + (width - stereotypeWidth) / 2, y + 15);

        gc.setFont(Font.font(gc.getFont().getFamily(), Font.getDefault().getSize() + 2));
        double interfaceNameWidth = calculateTextWidth(interfaceName, gc);
        gc.fillText(interfaceName, x + (width - interfaceNameWidth) / 2, y + 35);

        gc.setFont(Font.font(gc.getFont().getFamily(), Font.getDefault().getSize()));

        double methodStartY = y + interfaceNameHeight + 15;
        for (MethodData md : interfaceDiagram.getMethods()) {
            String methodText = md.getAccessModifier() + " " + md.getName() + " : " + md.getReturnType();
            gc.fillText(methodText, x + 10, methodStartY);
            methodStartY += 20;
        }


        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        contextMenu.getItems().addAll(editItem, deleteItem);

    }

    private double calculateTextWidth(String text, GraphicsContext gc) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        return tempText.getLayoutBounds().getWidth();
    }

    private void deleteInterfaceDiagram(InterfaceData interfaceDiagram) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this interface diagram?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            interfaceDiagrams.remove(interfaceDiagram);
            activeDiagram = null;
            redrawCanvas();
        }
    }

    private void editInterfaceDiagram(InterfaceData interfaceDiagram) {
        InterfaceDiagramUI interfaceDiagramUI = new InterfaceDiagramUI(drawingCanvas, interfaceDiagram);
        InterfaceData updatedDiagram = interfaceDiagramUI.showInterfaceDiagramDialog();

        if (updatedDiagram != null) {
            interfaceDiagram.setName(updatedDiagram.getName());
            interfaceDiagram.setMethods(updatedDiagram.getMethods());
            redrawCanvas();
        }
    }

    private boolean isWithinBounds(double mouseX, double mouseY, InterfaceData diagram, GraphicsContext gc) {
        double x = diagram.getX();
        double y = diagram.getY();
        double width = calculateDiagramWidth(diagram, gc);
        double height = calculateDiagramHeight(diagram);
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private double calculateDiagramWidth(InterfaceData diagram, GraphicsContext gc) {
        double maxWidth = calculateTextWidth("<<interface>>", gc);
        maxWidth = Math.max(maxWidth, calculateTextWidth(diagram.getName(), gc));
        for (MethodData method : diagram.getMethods()) {
            String methodText = method.getAccessModifier() + " " + method.getName() + " : " + method.getReturnType();
            maxWidth = Math.max(maxWidth, calculateTextWidth(methodText, gc));
        }
        return maxWidth * 1.3;
    }

    private double calculateDiagramHeight(InterfaceData diagram) {
        double interfaceNameHeight = 40;
        double methodHeight = Math.max(30 * diagram.getMethods().size(), 30);
        return interfaceNameHeight + methodHeight;
    }


    private String[] promptForSourceAndTargetClasses() {

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add Relationship");
        dialog.setHeaderText("Select the source and target class diagrams and enter a relationship name.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<String> sourceBox = new ComboBox<>();
        ComboBox<String> targetBox = new ComboBox<>();

        TextField relationshipNameField = new TextField();
        relationshipNameField.setPromptText("Enter Name (optional)");

        for (ClassDiagram diagram : classDiagrams) {
            sourceBox.getItems().add(diagram.getName());
            targetBox.getItems().add(diagram.getName());
        }

        sourceBox.setPromptText("Select Source");
        targetBox.setPromptText("Select Target");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Source:"), 0, 0);
        grid.add(sourceBox, 1, 0);
        grid.add(new Label("Target:"), 0, 1);
        grid.add(targetBox, 1, 1);
        grid.add(new Label("Relationship Name:"), 0, 2);
        grid.add(relationshipNameField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (sourceBox.getValue() != null && targetBox.getValue() != null) {
                    // Return an array with source class, target class, and relationship name
                    String relationshipName = relationshipNameField.getText().trim();
                    return new String[] { sourceBox.getValue(), targetBox.getValue(), relationshipName };
                }
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null); // Return null if no result is found
    }

    private String[] promptForSourceAndTargetClassesWithDefaults(String defaultSource, String defaultTarget, String defaultRelationshipName) {

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add/Edit Relationship");
        dialog.setHeaderText("Select the source and target class diagrams and enter a relationship name.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<String> sourceBox = new ComboBox<>();
        ComboBox<String> targetBox = new ComboBox<>();

        TextField relationshipNameField = new TextField();
        relationshipNameField.setPromptText("Enter Name (optional)");

        // Add the class diagram names to the combo boxes
        for (ClassDiagram diagram : classDiagrams) {
            sourceBox.getItems().add(diagram.getName());
            targetBox.getItems().add(diagram.getName());
        }

        // Set the default values if they are provided
        sourceBox.setValue(defaultSource);
        targetBox.setValue(defaultTarget);
        relationshipNameField.setText(defaultRelationshipName);

        sourceBox.setPromptText("Select Source");
        targetBox.setPromptText("Select Target");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Source:"), 0, 0);
        grid.add(sourceBox, 1, 0);
        grid.add(new Label("Target:"), 0, 1);
        grid.add(targetBox, 1, 1);
        grid.add(new Label("Relationship Name:"), 0, 2);
        grid.add(relationshipNameField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Handle the result when the user clicks OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (sourceBox.getValue() != null && targetBox.getValue() != null) {
                    // Return an array with the source class, target class, and relationship name
                    String relationshipName = relationshipNameField.getText().trim();
                    return new String[] { sourceBox.getValue(), targetBox.getValue(), relationshipName };
                }
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null); // Return null if no result is found
    }

    private ClassDiagram findDiagramByName(String name) {
        for (ClassDiagram diagram : classDiagrams) {
            if (diagram.getName().equals(name)) {
                return diagram;
            }
        }
        throw new IllegalArgumentException("No class diagram found with name: " + name);
    }


    @FXML
    private void handleAddAssociation() {

        String[] namesAndRelationshipName = promptForSourceAndTargetClasses();
        if (namesAndRelationshipName == null) return;

        String sourceName = namesAndRelationshipName[0];
        String targetName = namesAndRelationshipName[1];
        String relationshipName = namesAndRelationshipName[2];

        try {
            ClassDiagram source = findDiagramByName(sourceName);
            ClassDiagram target = findDiagramByName(targetName);

            if (activeRelationship == null)
            {
                Relationship association = new Relationship(source, target, "association", "1", "1", obstacles, relationshipName);
                associations.add(association);
            }
            else
            {
                activeRelationship.setSourceClass(source);
                activeRelationship.setTargetClass(target);
                activeRelationship.setRelationName(relationshipName);
                activeRelationship.setType("association");
                activeRelationship.setSourceMultiplicity("1");
                activeRelationship.setTargetMultiplicity("1");
            }

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    @FXML
    private void handleAddComposition() {
        String[] namesAndRelationshipName = promptForSourceAndTargetClasses();
        if (namesAndRelationshipName == null) return;

        String sourceName = namesAndRelationshipName[0];  // Source class name
        String targetName = namesAndRelationshipName[1];  // Target class name
        String relationshipName = namesAndRelationshipName[2];  // Relationship name (optional)

        try {
            ClassDiagram source = findDiagramByName(sourceName);
            ClassDiagram target = findDiagramByName(targetName);

            if (activeRelationship == null) {
                // If no active relationship, create a new one
                Relationship composition = new Relationship(source, target, "composition", "0", "0", obstacles, relationshipName);
                compositions.add(composition);
            } else
            {
                activeRelationship.setSourceClass(source);
                activeRelationship.setTargetClass(target);
                activeRelationship.setRelationName(relationshipName);
                activeRelationship.setType("composition");
                activeRelationship.setSourceMultiplicity("0");
                activeRelationship.setTargetMultiplicity("0");
            }

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    @FXML
    private void handleAddAggregation() {
        String[] namesAndRelationshipName = promptForSourceAndTargetClasses();
        if (namesAndRelationshipName == null) return;

        String sourceName = namesAndRelationshipName[0];  // Source class name
        String targetName = namesAndRelationshipName[1];  // Target class name
        String relationshipName = namesAndRelationshipName[2];  // Relationship name (optional)

        try {
            ClassDiagram source = findDiagramByName(sourceName);
            ClassDiagram target = findDiagramByName(targetName);

            if (activeRelationship == null) {
                // If no active relationship, create a new one
                Relationship aggregation = new Relationship(source, target, "aggregation", "0", "0", obstacles, relationshipName);
                aggregations.add(aggregation);
            } else {
                // If active relationship exists, edit it
                activeRelationship.setSourceClass(source);
                activeRelationship.setTargetClass(target);
                activeRelationship.setRelationName(relationshipName);
                activeRelationship.setType("aggregation");
                activeRelationship.setSourceMultiplicity("0");
                activeRelationship.setTargetMultiplicity("0");
            }

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    @FXML
    public void handleAddGeneralization() {
        // Prompt the user to select source, target, and the relationship name
        String[] namesAndRelationshipName = promptForSourceAndTargetClasses();
        if (namesAndRelationshipName == null) return;

        String sourceName = namesAndRelationshipName[0];  // Source class name
        String targetName = namesAndRelationshipName[1];  // Target class name
        String relationshipName = namesAndRelationshipName[2];  // Relationship name (optional)

        try {
            ClassDiagram source = findDiagramByName(sourceName);
            ClassDiagram target = findDiagramByName(targetName);

            if (activeRelationship == null) {
                // If no active relationship, create a new one
                Relationship generalization = new Relationship(source, target, "Generalization", "0", "0", obstacles, relationshipName);
                generalizations.add(generalization);
            } else {
                // If active relationship exists, edit it
                activeRelationship.setSourceClass(source);
                activeRelationship.setTargetClass(target);
                activeRelationship.setRelationName(relationshipName);
                activeRelationship.setType("Generalization");
                activeRelationship.setSourceMultiplicity("0");
                activeRelationship.setTargetMultiplicity("0");
            }

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }


    @FXML
    public void handleAddRealization() {
        Pair<String, String> names = promptForSourceAndTargetInterface();
        if (names == null) return;

        try {
            ClassDiagram source = findDiagramByName(names.getKey());
            InterfaceData target = findInterfaceDiagramByName(names.getValue());

            // Create a new Realization relationship and add it to the list
            Relationship realization = new Relationship(source, target, "Realization", "0", "0", obstacles);
            realizations.add(realization);

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    private InterfaceData findInterfaceDiagramByName(String value) {
        for (InterfaceData diagram : interfaceDiagrams) {
            if (diagram.getName().equals(value)) {
                return diagram;
            }
        }
        throw new IllegalArgumentException("No interface diagram found with name: " + value);
    }

    private Pair<String, String> promptForSourceAndTargetInterface() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Realization");
        dialog.setHeaderText("Select the source and target diagrams.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<String> sourceBox = new ComboBox<>();
        ComboBox<String> targetBox = new ComboBox<>();

        for (ClassDiagram diagram : classDiagrams) {
            sourceBox.getItems().add(diagram.getName());
        }
        for (InterfaceData diagram : interfaceDiagrams) {
            targetBox.getItems().add(diagram.getName());
        }

        sourceBox.setPromptText("Select Source Class");
        targetBox.setPromptText("Select Target Interface");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Source:"), 0, 0);
        grid.add(sourceBox, 1, 0);
        grid.add(new Label("Target:"), 0, 1);
        grid.add(targetBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (sourceBox.getValue() != null && targetBox.getValue() != null) {
                    return new Pair<>(sourceBox.getValue(), targetBox.getValue());
                }
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        return result.orElse(null);

    }


    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleSetMultiplicity() {
        System.out.println("Set Multiplicity button clicked");
    }

    @FXML
    private void handleNewProject() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("New Project");
        confirmationAlert.setHeaderText("Are you sure you want to create a new project?");
        confirmationAlert.setContentText("Unsaved changes will be lost.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            clearWorkspace();
            System.out.println("New project initialized.");
        } else {
            System.out.println("New project creation canceled.");
        }
    }
    private void clearWorkspace() {
        activeDiagram = null;
        activeInterface = null;
        activeRelationship = null;
        classDiagram = null;
        interfaceDiagram = null;

        obstacles.clear();
        interfaceDiagrams.clear();
        classDiagrams.clear();
        associations.clear();
        compositions.clear();
        aggregations.clear();
        realizations.clear();
        generalizations.clear();

        isDraggingSource = false;
        isDraggingTarget = false;

        dragStartX = 0;
        dragStartY = 0;

        if (drawingCanvas != null) {
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        }

        if (modelInfoList != null) {
            modelInfoList.getItems().clear();
        }

        if (contextMenu != null) {
            contextMenu.hide();
        }
        System.out.println("Workspace cleared and reset to initial state.");
    }


    @FXML
    private void handleSaveProject() {
        // Create a FileChooser to allow the user to select the save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Start XML document
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<Project>\n");

                // Write class diagrams
                writer.write("    <ClassDiagrams>\n");
                for (ClassDiagram diagram : classDiagrams) {
                    writer.write("        <ClassDiagram>\n");
                    writer.write("            <Name>" + diagram.getName() + "</Name>\n");
                    writer.write("            <Attributes>\n");
                    for (AttributeData attribute : diagram.getAttributes()) {
                        writer.write("                <Attribute>\n");
                        writer.write("                    <AccessModifier>" + attribute.getAccessModifier() + "</AccessModifier>\n");
                        writer.write("                    <DataType>" + attribute.getDataType() + "</DataType>\n");
                        writer.write("                    <Name>" + attribute.getName() + "</Name>\n");
                        writer.write("                </Attribute>\n");
                    }
                    writer.write("            </Attributes>\n");
                    writer.write("            <Methods>\n");
                    for (MethodData method : diagram.getMethods()) {
                        writer.write("                <Method>\n");
                        writer.write("                    <AccessModifier>" + method.getAccessModifier() + "</AccessModifier>\n");
                        writer.write("                    <ReturnType>" + method.getReturnType() + "</ReturnType>\n");
                        writer.write("                    <Name>" + method.getName() + "</Name>\n");
                        writer.write("                </Method>\n");
                    }
                    writer.write("            </Methods>\n");
                    writer.write("        </ClassDiagram>\n");
                }
                writer.write("    </ClassDiagrams>\n");

                // Write interface diagrams
                writer.write("    <InterfaceDiagrams>\n");
                for (InterfaceData diagram : interfaceDiagrams) {
                    writer.write("        <InterfaceDiagram>\n");
                    writer.write("            <Name>" + diagram.getName() + "</Name>\n");
                    writer.write("            <Methods>\n");
                    for (MethodData method : diagram.getMethods()) {
                        writer.write("                <Method>\n");
                        writer.write("                    <AccessModifier>" + method.getAccessModifier() + "</AccessModifier>\n");
                        writer.write("                    <ReturnType>" + method.getReturnType() + "</ReturnType>\n");
                        writer.write("                    <Name>" + method.getName() + "</Name>\n");
                        writer.write("                </Method>\n");
                    }
                    writer.write("            </Methods>\n");
                    writer.write("        </InterfaceDiagram>\n");
                }
                writer.write("    </InterfaceDiagrams>\n");

                // End XML document
                writer.write("</Project>\n");

                showAlert(Alert.AlertType.INFORMATION, "Save Project", "Project saved successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Project", "Failed to save the project.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleOpenProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Project");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try {
                // Clear existing diagrams
                classDiagrams.clear();
                interfaceDiagrams.clear();
                redrawCanvas(); // Clear the canvas

                // Load and parse the XML file
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                document.getDocumentElement().normalize();

                // Load Class Diagrams
                NodeList classDiagramNodes = document.getElementsByTagName("ClassDiagram");
                for (int i = 0; i < classDiagramNodes.getLength(); i++) {
                    Element classElement = (Element) classDiagramNodes.item(i);
                    String className = classElement.getElementsByTagName("Name").item(0).getTextContent();

                    ClassDiagram classDiagram = new ClassDiagram();
                    classDiagram.setName(className);

                    // Load Attributes
                    NodeList attributeNodes = classElement.getElementsByTagName("Attribute");
                    for (int j = 0; j < attributeNodes.getLength(); j++) {
                        Element attributeElement = (Element) attributeNodes.item(j);
                        String accessModifier = attributeElement.getElementsByTagName("AccessModifier").item(0).getTextContent();
                        String dataType = attributeElement.getElementsByTagName("DataType").item(0).getTextContent();
                        String attributeName = attributeElement.getElementsByTagName("Name").item(0).getTextContent();

                        AttributeData attribute = new AttributeData();
                        attribute.setAccessModifier(accessModifier);
                        attribute.setDataType(dataType);
                        attribute.setName(attributeName);
                        classDiagram.getAttributes().add(attribute);
                    }

                    // Load Methods
                    NodeList methodNodes = classElement.getElementsByTagName("Method");
                    for (int j = 0; j < methodNodes.getLength(); j++) {
                        Element methodElement = (Element) methodNodes.item(j);
                        String accessModifier = methodElement.getElementsByTagName("AccessModifier").item(0).getTextContent();
                        String returnType = methodElement.getElementsByTagName("ReturnType").item(0).getTextContent();
                        String methodName = methodElement.getElementsByTagName("Name").item(0).getTextContent();

                        MethodData method = new MethodData();
                        method.setAccessModifier(accessModifier);
                        method.setReturnType(returnType);
                        method.setName(methodName);
                        classDiagram.getMethods().add(method);
                    }


                    // Add the class diagram to the list
                    classDiagrams.add(classDiagram);
                }

                // Load Interface Diagrams
                NodeList interfaceDiagramNodes = document.getElementsByTagName("InterfaceDiagram");
                for (int i = 0; i < interfaceDiagramNodes.getLength(); i++) {
                    Element interfaceElement = (Element) interfaceDiagramNodes.item(i);
                    String interfaceName = interfaceElement.getElementsByTagName("Name").item(0).getTextContent();

                    InterfaceData interfaceDiagram = new InterfaceData();
                    interfaceDiagram.setName(interfaceName);

                    // Load Methods
                    NodeList methodNodes = interfaceElement.getElementsByTagName("Method");
                    for (int j = 0; j < methodNodes.getLength(); j++) {
                        Element methodElement = (Element) methodNodes.item(j);
                        String accessModifier = methodElement.getElementsByTagName("AccessModifier").item(0).getTextContent();
                        String returnType = methodElement.getElementsByTagName("ReturnType").item(0).getTextContent();
                        String methodName = methodElement.getElementsByTagName("Name").item(0).getTextContent();

                        MethodData method = new MethodData();
                        method.setAccessModifier(accessModifier);
                        method.setReturnType(returnType);
                        method.setName(methodName);
                        interfaceDiagram.getMethods().add(method);
                    }

                    // Add the interface diagram to the list
                    interfaceDiagrams.add(interfaceDiagram);
                }

                // Redraw the canvas with the loaded diagrams
                redrawCanvas();
                showAlert(Alert.AlertType.INFORMATION, "Open Project", "Project loaded successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Open Project", "Failed to load the project.");
                e.printStackTrace();
            }
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleExit() {
        Alert exitConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
        exitConfirmation.setTitle("Exit Application");
        exitConfirmation.setHeaderText("Are you sure you want to exit?");
        exitConfirmation.setContentText("Any unsaved changes will be lost.");

        Optional<ButtonType> result = exitConfirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        } else {
            System.out.println("Exit canceled by the user.");
        }
    }

    @FXML
    private void handleGenerateCode()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Generated Code");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Map to hold class names for checking associations
                Map<String, ClassDiagram> classMap = new HashMap<>();
                for (ClassDiagram diagram : classDiagrams) {
                    classMap.put(diagram.getName(), diagram);
                }

                for (ClassDiagram diagram : classDiagrams) {
                    StringBuilder classCode = new StringBuilder();

                    classCode.append("public class ").append(diagram.getName()).append(" {\n\n");

                    for (AttributeData attribute : diagram.getAttributes()) {
                        classCode.append("    ")
                                .append(mapAccessModifier(attribute.getAccessModifier()))
                                .append(" ")
                                .append(attribute.getDataType())
                                .append(" ")
                                .append(attribute.getName())
                                .append("; // Attribute\n");
                    }
                    classCode.append("\n");

                    for (MethodData method : diagram.getMethods()) {
                        classCode.append("    ")
                                .append(mapAccessModifier(method.getAccessModifier()))
                                .append(" ")
                                .append(method.getReturnType())
                                .append(" ")
                                .append(method.getName())
                                .append("() {\n")
                                .append("        // TODO: Add method implementation\n")
                                .append("    }\n\n");
                    }

                    for (AttributeData attribute : diagram.getAttributes()) {
                        if (classMap.containsKey(attribute.getDataType())) {
                            classCode.append("    // Association: ").append(diagram.getName())
                                    .append(" has a reference to ").append(attribute.getDataType()).append("\n");
                        }
                    }

                    classCode.append("}\n\n");
                    writer.write(classCode.toString());
                }

                showAlert(Alert.AlertType.INFORMATION, "Generate Code", "Code generated successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Generate Code", "Failed to save the code.");
                e.printStackTrace();
            }
        }
    }

    private String mapAccessModifier(String accessModifier) {
        switch (accessModifier) {
            case "private":
                return "private";
            case "protected":
                return "protected";
            case "public":
                return "public";
            default:
                return ""; // Default to package-private
        }
    }


    @FXML
    private void handleExportDiagram (){
        try {
            // Step 1: Take a snapshot of the canvas
            WritableImage snapshot = drawingCanvas.snapshot(null, null);

            // Step 2: Open a FileChooser for saving the image
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Diagram");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG Files", "*.jpg"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

            // Step 3: Show save dialog to get the destination file
            File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
            if (file != null) {
                // Step 4: Write the snapshot to the file
                String extension = getFileExtension(file.getName());
                if (extension.equals("jpg") || extension.equals("png")) {
                    ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), extension, file);
                    System.out.println("Diagram exported successfully to: " + file.getAbsolutePath());
                } else {
                    showError("Invalid File Type", "Please save the file with .jpg or .png extension.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Export Failed", "An error occurred while exporting the diagram: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1).toLowerCase();
    }


    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Craft UML");
        alert.setHeaderText("Craft UML - UML Diagramming Tool");

        String content = "Craft UML is a tool designed to help users create UML diagrams with ease. " +
                "The application supports various types of diagrams including class diagrams, " +
                "sequence diagrams, and use-case diagrams. Additionally, it allows users to generate " +
                "code based on the designed diagrams, streamlining the software development process.\n\n" +
                "Creators:\n" +
                "Samar\n" +
                "Noman\n" +
                "Hassaan\n\n" +
                "Version: 1.0.0\n" +
                "For more information, visit: www.craftuml.com";

        alert.setContentText(content);

        TextFlow textFlow = new TextFlow();
        Text text = new Text(content);
        text.setStyle("-fx-font-size: 14px; -fx-font-family: Arial; -fx-fill: #333;");
        textFlow.getChildren().add(text);

        // Set the preferred width of the textFlow to control wrapping
        textFlow.setMaxWidth(400); // You can adjust this value based on your preference

        // Add TextFlow to the alert
        alert.getDialogPane().setContent(textFlow);

        // Set the maximum width of the alert dialog to fit the screen
        alert.getDialogPane().setPrefWidth(450);  // Adjust width as necessary to fit within the screen size

        // Optionally, set a minimum height
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.getDialogPane().getStyleClass().add("about-alert");

        alert.showAndWait();
    }


}
