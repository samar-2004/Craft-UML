package org.example.craftuml.Service;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.example.craftuml.UI.InterfaceDiagramUI;
import org.example.craftuml.UI.classDiagramUI;
import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.ClassDiagrams.MethodData;
import org.example.craftuml.models.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
        System.out.println("New Project Clicked");
    }

    @FXML
    private void handleOpenProject() {
        System.out.println("Open Project Clicked");
    }

    @FXML
    private void handleSaveProject() {
        System.out.println("Save Project Clicked");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleUndo() {
        System.out.println("Undo Clicked");
    }

    @FXML
    private void handleRedo() {
        System.out.println("Redo Clicked");
    }

    @FXML
    private void handleManageComponents()
    {
        System.out.println("Manage Components Clicked");
    }

    @FXML
    private void handleManageRelationships()
    {
        System.out.println("Manage Relationships Clicked");
    }

    @FXML
    private void handleGenerateCode() {System.out.println("Generate Code Clicked");}

    @FXML
    private void handleExportDiagram()
    {
        System.out.println("Export Diagram Clicked");
    }

    @FXML
    private void handleAbout()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Craft UML");
        alert.setContentText("Craft UML - A tool for creating UML diagrams and generating code.");
        alert.showAndWait();
    }
}
