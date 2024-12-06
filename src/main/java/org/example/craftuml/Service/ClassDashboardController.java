package org.example.craftuml.Service;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.Cursor;
import javafx.scene.Node;
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
import org.example.craftuml.models.Section;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ClassDashboardController {
    @FXML
    private Canvas drawingCanvas;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ListView<String> modelInfoList = new ListView<>();
    private ObservableList<String> modelNames = FXCollections.observableArrayList();
    private ObservableList<Object> modelObjects = FXCollections.observableArrayList();

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

        modelInfoList.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Clear any previous styles
                setStyle("");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index >= 0 && index < modelObjects.size()) {  // Ensure the index is within bounds
                        Object modelItem = modelObjects.get(index);

                        // Check if the item is a section title
                        if (modelItem instanceof Section) {
                            setText(item);
                            setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #f0f0f0;");
                        } else {
                            setText(item);

                            // Apply specific styles to individual items
                            if (modelItem instanceof ClassDiagram) {
                                setStyle("-fx-font-weight: bold; -fx-background-color: #e6f7ff;");
                            } else if (modelItem instanceof InterfaceData) {
                                setStyle("-fx-font-style: italic; -fx-background-color: #fff5e6;");
                            } else if (modelItem instanceof Relationship) {
                                setStyle("-fx-font-size: 14px; -fx-background-color: #B0D0A0;");
                            }
                        }
                    } else {
                        // Index is out of bounds, handle gracefully
                        setText(null);
                        setGraphic(null);
                    }
                }
            }

        });
            updateListView();

        classDiagrams.forEach(diagram ->
                diagram.nameProperty().addListener((obs, oldName, newName) -> updateListView()));

        interfaceDiagrams.forEach(diagram ->
                diagram.nameProperty().addListener((obs, oldName, newName) -> updateListView()));

        addRelationshipListeners(associations);
        addRelationshipListeners(compositions);
        addRelationshipListeners(aggregations);
        addRelationshipListeners(realizations);
        addRelationshipListeners(generalizations);
     }

    private void addRelationshipListeners(List<Relationship> relationships) {
        relationships.forEach(relationship ->
                relationship.relationNameProperty().addListener((obs, oldName, newName) -> updateListView()));
    }
    private void updateListView() {
        modelNames.clear();
        modelObjects.clear();

        // Create sections for each type of item
        List<Section> sections = new ArrayList<>();

        // Only create and add "CLASSES" section if there are class diagrams
        if (!classDiagrams.isEmpty()) {
            List<Object> classItems = new ArrayList<>(classDiagrams);
            sections.add(new Section("CLASSES", classItems));
        }

        // Only create and add "INTERFACES" section if there are interface diagrams
        if (!interfaceDiagrams.isEmpty()) {
            List<Object> interfaceItems = new ArrayList<>(interfaceDiagrams);
            sections.add(new Section("INTERFACES", interfaceItems));
        }

        // Only create and add "RELATIONSHIPS" section if there are relationships
        List<Object> relationshipItems = new ArrayList<>();
        relationshipItems.addAll(associations);
        relationshipItems.addAll(compositions);
        relationshipItems.addAll(aggregations);
        relationshipItems.addAll(realizations);
        relationshipItems.addAll(generalizations);

        if (!relationshipItems.isEmpty()) {
            sections.add(new Section("RELATIONSHIPS", relationshipItems));
        }

        String SPACE = " "; // You can adjust the number of spaces to add more padding

        // Add sections to modelObjects and modelNames if they are non-empty
        boolean firstSection = true; // Flag to track if it's the first section
        for (Section section : sections) {
            // Add space before each section except the first one
            if (!firstSection) {
                modelNames.add(SPACE);  // Adding space between sections
                modelObjects.add(null); // Just to add space in the modelObjects
            }
            firstSection = false; // Set the flag to false after the first section

            // Add section title as header
            modelNames.add(section.getTitle());
            modelObjects.add(section); // Add section object for reference

            // Add items of the section
            for (Object item : section.getItems()) {
                modelNames.add(item instanceof ClassDiagram ? ((ClassDiagram) item).getName()
                        : item instanceof InterfaceData ? ((InterfaceData) item).getName()
                        : ((Relationship) item).getRelationType());
                modelObjects.add(item); // Add item for reference
            }
        }
        modelInfoList.setItems(FXCollections.observableList(modelNames));;
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
        updateListView();
    }
    @FXML
    private void handleClassDiagram() {
        classDiagramUI classDiagramUI = new classDiagramUI(drawingCanvas,classDiagrams);
        classDiagram = classDiagramUI.showClassDiagramDialog();

        double newX = 20 + (classDiagrams.size() * 100) % (drawingCanvas.getWidth() - 100);
        double newY = 20 + ((classDiagrams.size() * 100) / (drawingCanvas.getWidth() - 100)) * 100;

        classDiagram.setX(newX);
        classDiagram.setY(newY);
        classDiagrams.add(classDiagram);
        classDiagram.nameProperty().addListener((obs, oldName, newName) -> updateListView());
        updateListView();

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
        String className = classDiagram.getName();
        Text tempText = new Text(className);
        tempText.setFont(gc.getFont());
        double classNameWidth = tempText.getLayoutBounds().getWidth();
        maxWidth = Math.max(maxWidth, classNameWidth);

        for (AttributeData at : classDiagram.getAttributes()) {
            String attributeText = at.getAccessModifier() + " " + at.getName() + " : " + at.getDataType();
            tempText = new Text(attributeText);
            tempText.setFont(gc.getFont());
            double textWidth = tempText.getLayoutBounds().getWidth();
            maxWidth = Math.max(maxWidth, textWidth);
        }

        for (MethodData md : classDiagram.getMethods()) {
            String methodText = md.getAccessModifier() + " " + md.getName() + " : " + md.getReturnType();
            tempText = new Text(methodText);
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
        String classNameText = classDiagram.getName();

        tempText = new Text(classNameText);
        tempText.setFont(gc.getFont());
        double classNameTextWidth = tempText.getLayoutBounds().getWidth();

        gc.fillText(className, x + (width - classNameTextWidth) / 2, y + classNameHeight / 2 + 10);

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

            deleteClassDiagramFromListView(classDiagram);

            activeDiagram = null;
            redrawCanvas();
        }
    }

    private void deleteClassDiagramFromListView(ClassDiagram classDiagram) {
        List<String> updatedModelNames = new ArrayList<>();
        List<Object> updatedModelObjects = new ArrayList<>();

        for (int i = 0; i < modelNames.size(); i++) {
            Object modelItem = modelObjects.get(i);

            if (modelItem instanceof Section) {
                updatedModelNames.add(modelNames.get(i));
                updatedModelObjects.add(modelObjects.get(i));
            }
            else if (modelItem instanceof ClassDiagram && modelItem.equals(classDiagram)) {
                continue;
            }
            else if (modelItem instanceof Relationship) {
                Relationship relationship = (Relationship) modelItem;
                boolean isRelated = relationship.getSourceClass().equals(classDiagram) ||
                        relationship.getTargetClass().equals(classDiagram) ||
                        (relationship.getTargetInterface() != null &&
                                relationship.getTargetInterface().equals(classDiagram));

                if (isRelated) {
                    continue; // Skip this relationship
                }
            }

            // Otherwise, keep the item in the new lists
            updatedModelNames.add(modelNames.get(i));
            updatedModelObjects.add(modelObjects.get(i));
        }

        // Now, update the model names and objects
        modelNames.setAll(updatedModelNames);
        modelObjects.setAll(updatedModelObjects);

        // Update the ListView to reflect the changes
        modelInfoList.setItems(modelNames);
        updateListView();  // This ensures the ListView is re-rendered with the correct data
    }


    private void editClassDiagram(ClassDiagram classDiagram) {
        classDiagramUI classDiagramUI = new classDiagramUI(drawingCanvas, classDiagram,classDiagrams);
        ClassDiagram updatedDiagram = classDiagramUI.showClassDiagramDialog();

        if (updatedDiagram != null) {
            activeDiagram.setName(updatedDiagram.getName());
            activeDiagram.setAttributes(updatedDiagram.getAttributes());
            activeDiagram.setMethods(updatedDiagram.getMethods());

            redrawCanvas();
        }
    }

    private boolean isWithinBounds(double mouseX, double mouseY, ClassDiagram diagram, GraphicsContext gc) {
        double x = diagram.getX();
        double y = diagram.getY();
        double width = calculateDiagramWidth(diagram, gc);
        double height = calculateDiagramHeight(diagram);
        boolean isWithin = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        return isWithin;
    }

    private double calculateDiagramWidth(ClassDiagram diagram, GraphicsContext gc) {
        double maxWidth = 0;

        Text tempText = new Text(diagram.getName());
        tempText.setFont(gc.getFont());
        maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());

        if (!diagram.getAttributes().isEmpty()) {
            for (AttributeData attribute : diagram.getAttributes()) {
                String attributeText = attribute.getAccessModifier() + " " + attribute.getName() + " : " + attribute.getDataType();
                tempText = new Text(attributeText);
                tempText.setFont(gc.getFont());
                maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());
            }
        }

        if (!diagram.getMethods().isEmpty()) {
            for (MethodData method : diagram.getMethods()) {
                String methodText = method.getAccessModifier() + " " + method.getName() + " : " + method.getReturnType();
                tempText = new Text(methodText);
                tempText.setFont(gc.getFont());
                maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());
            }
        }

        return maxWidth + 40;
    }

    private double calculateDiagramHeight(ClassDiagram diagram) {
        double classNameHeight = 30;
        double attributeHeight = 0;
        for (AttributeData attribute : diagram.getAttributes())
        {
            Text tempText = new Text(attribute.getAccessModifier() + " " + attribute.getName() + " : " + attribute.getDataType());
            tempText.setFont(new Font("Arial", 12));
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

                resizeCanvasIfNeeded(newX, newY);
                redrawCanvas();
            } else if (activeInterface != null) {
                double newX = event.getX() - dragStartX;
                double newY = event.getY() - dragStartY;

                activeInterface.setX(newX);
                activeInterface.setY(newY);

                resizeCanvasIfNeeded(newX, newY);
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
    private void resizeCanvasIfNeeded(double x, double y) {
        double currentWidth = drawingCanvas.getWidth();
        double currentHeight = drawingCanvas.getHeight();
        double padding = 100; // Padding to prevent too frequent resizing

        if (x + padding > currentWidth) {
            drawingCanvas.setWidth(x + padding);
        }
        if (y + padding > currentHeight) {
            drawingCanvas.setHeight(y + padding);
        }
    }
    private void closeContextMenu() {
        if (contextMenu != null && contextMenu.isShowing()) {
            contextMenu.hide();
        }
    }

    private void editRelationship(Relationship activeRelationship) {
        if (activeRelationship != null && !activeRelationship.getType().equals("Realization")
                && !activeRelationship.getType().equals("Generalization"))
        {
            String sourceName = activeRelationship.getSourceClass().getName();
            String targetName = activeRelationship.getTargetClass().getName();
            String relationshipName = activeRelationship.getRelationName();
            String sourceMultiplicity = activeRelationship.getSourceClassMultiplicity();
            String targetMultiplicity = activeRelationship.getTargetClassMultiplicity();

            String[] namesAndRelationshipName = promptForSourceAndTargetClassesWithDefaults(sourceName, targetName,sourceMultiplicity,targetMultiplicity, relationshipName);

            if (namesAndRelationshipName == null) return;

            String newSourceName = namesAndRelationshipName[0];
            String newTargetName = namesAndRelationshipName[1];
            String newSourceMul = namesAndRelationshipName[2];
            String newTargetMul = namesAndRelationshipName[3];
            String newRelationshipName = namesAndRelationshipName[4];

            try {
                ClassDiagram newSource = findDiagramByName(newSourceName);
                ClassDiagram newTarget = findDiagramByName(newTargetName);

                activeRelationship.setSourceClass(newSource);
                activeRelationship.setTargetClass(newTarget);
                activeRelationship.setRelationName(newRelationshipName);
                activeRelationship.setSourceMultiplicity(newSourceMul);
                activeRelationship.setTargetMultiplicity(newTargetMul);
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
                activeRelationship.setSourceClass(newSource);
                activeRelationship.setTargetInterface(newTarget);
                redrawCanvas();
            } catch (IllegalArgumentException e) {
                showErrorAlert(e.getMessage());
            }
        }
        else if (activeRelationship != null && activeRelationship.getType().equals("Generalization")) {
            // Editing a Generalization relationship
            String sourceName = activeRelationship.getSourceClass().getName();
            String targetName = activeRelationship.getTargetClass().getName();

            String[] namesAndRelationshipName = promptForGeneralization(sourceName, targetName);

            if (namesAndRelationshipName == null) return;

            String newSourceName = namesAndRelationshipName[0];
            String newTargetName = namesAndRelationshipName[1];

            try {
                ClassDiagram newSource = findDiagramByName(newSourceName);
                ClassDiagram newTarget = findDiagramByName(newTargetName);

                activeRelationship.setSourceClass(newSource);
                activeRelationship.setTargetClass(newTarget);

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
    private String[] promptForGeneralization(String defaultSource, String defaultTarget) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add/Edit Generalization Relationship");
        dialog.setHeaderText("Select the source and target class diagrams for Generalization.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // ComboBoxes for source and target classes
        ComboBox<String> sourceBox = new ComboBox<>();
        ComboBox<String> targetBox = new ComboBox<>();

        // Populate class diagram ComboBoxes
        for (ClassDiagram diagram : classDiagrams) {
            sourceBox.getItems().add(diagram.getName());
            targetBox.getItems().add(diagram.getName());
        }

        sourceBox.setValue(defaultSource);
        targetBox.setValue(defaultTarget);

        String sourceMultiplicity = "0";
        String targetMultiplicity = "0";

        // Empty for Generalization

        // Disable OK button initially
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        // Change listener to enable the OK button only when both source and target classes are selected
        ChangeListener<Object> enableOkButtonListener = (obs, oldVal, newVal) -> {
            okButton.setDisable(
                    sourceBox.getValue() == null ||
                            targetBox.getValue() == null
            );
        };

        sourceBox.valueProperty().addListener(enableOkButtonListener);
        targetBox.valueProperty().addListener(enableOkButtonListener);

        // Layout using GridPane
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
                    return new String[]{
                            sourceBox.getValue(),
                            targetBox.getValue(),
                            sourceMultiplicity, // Always "0..0" for source
                            targetMultiplicity, // Always "0..0" for target
                            "" // Empty relationship name for Generalization
                    };
                }
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null); // Return null if no result is found
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
        System.out.println("Deleting Relationship: " + activeRelationship.getType());

        if (activeRelationship.getType().equals("association")) {
            associations.remove(activeRelationship);
            System.out.println("Removed from Associations: " + activeRelationship.getType());
        } else if (activeRelationship.getType().equals("composition")) {
            compositions.remove(activeRelationship);
            System.out.println("Removed from Compositions: " + activeRelationship.getType());
        } else if (activeRelationship.getType().equals("aggregation")) {
            aggregations.remove(activeRelationship);
            System.out.println("Removed from Aggregations: " + activeRelationship.getType());
        } else if (activeRelationship.getType().equals("Realization")) {
            realizations.remove(activeRelationship);
            System.out.println("Removed from Realizations: " + activeRelationship.getType());
        } else if (activeRelationship.getType().equals("Generalization")) {
            generalizations.remove(activeRelationship);
            System.out.println("Removed from Generalizations: " + activeRelationship.getType());
        }
        deleteSpecificRelationshipFromListView(activeRelationship);
        activeRelationship = null;

        System.out.println("Active Relationship reset to null.");
        modelInfoList.refresh();

        redrawCanvas();
    }

    private void deleteSpecificRelationshipFromListView(Relationship activeRelationship) {
        List<String> updatedModelNames = new ArrayList<>();
        List<Object> updatedModelObjects = new ArrayList<>();
        for (int i = 0; i < modelNames.size(); i++)
        {
            Object modelItem = modelObjects.get(i);

            if (modelItem instanceof Section) {
                updatedModelNames.add(modelNames.get(i));
                updatedModelObjects.add(modelObjects.get(i));
            }
            else if (modelItem instanceof Relationship && modelItem.equals(activeRelationship))
            {
                continue;
            }
            else {
                updatedModelNames.add(modelNames.get(i));
                updatedModelObjects.add(modelObjects.get(i));
            }
        }


        modelNames.setAll(updatedModelNames);
        modelObjects.setAll(updatedModelObjects);

        modelInfoList.setItems(modelNames);

        updateListView();
    }


    @FXML
    private void handleAddInterface() {
        InterfaceDiagramUI interfaceDiagramUI = new InterfaceDiagramUI(drawingCanvas,interfaceDiagrams);
        interfaceDiagram = interfaceDiagramUI.showInterfaceDiagramDialog();

        double newX = 20 + (interfaceDiagrams.size() * 100) % (drawingCanvas.getWidth() - 100);
        double newY = 300 + ((interfaceDiagrams.size() * 100) / (drawingCanvas.getWidth() - 100)) * 100;

        interfaceDiagram.setX(newX);
        interfaceDiagram.setY(newY);

        interfaceDiagrams.add(interfaceDiagram);
        interfaceDiagram.nameProperty().addListener((obs, oldName, newName) -> updateListView());
        updateListView();

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
            // Remove realization relationships involving the interface
            realizations.removeIf(relationship ->
                    relationship.getTargetInterface() != null &&
                            relationship.getTargetInterface().equals(interfaceDiagram)
            );

            interfaceDiagrams.remove(interfaceDiagram);

            deleteInterfaceDiagramFromListView(interfaceDiagram);

            activeDiagram = null;
            redrawCanvas();
        }
    }
    private void deleteInterfaceDiagramFromListView(InterfaceData interfaceDiagram) {
        List<String> updatedModelNames = new ArrayList<>();
        List<Object> updatedModelObjects = new ArrayList<>();

        for (int i = 0; i < modelNames.size(); i++) {
            Object modelItem = modelObjects.get(i);

            if (modelItem instanceof Section) {
                // Keep sections unchanged
                updatedModelNames.add(modelNames.get(i));
                updatedModelObjects.add(modelObjects.get(i));
            }
            else if (modelItem instanceof InterfaceData && modelItem.equals(interfaceDiagram)) {
                continue;
            }
            else if (modelItem instanceof Relationship) {
                Relationship relationship = (Relationship) modelItem;
                boolean isRelated =
                        relationship.getTargetInterface() != null &&
                                relationship.getTargetInterface().equals(interfaceDiagram);

                if (isRelated) {
                    continue;
                }
            }

            // Otherwise, keep the item in the new lists
            updatedModelNames.add(modelNames.get(i));
            updatedModelObjects.add(modelObjects.get(i));
        }

        // Update the model names and objects
        modelNames.setAll(updatedModelNames);
        modelObjects.setAll(updatedModelObjects);

        // Update the ListView to reflect the changes
        modelInfoList.setItems(modelNames);
        updateListView();  // Refresh the ListView
    }



    private void editInterfaceDiagram(InterfaceData interfaceDiagram) {
        InterfaceDiagramUI interfaceDiagramUI = new InterfaceDiagramUI(drawingCanvas, interfaceDiagram,interfaceDiagrams);
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
        dialog.setHeaderText("Select the source and target class diagrams, enter a relationship name (optional), and specify multiplicities.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<String> sourceBox = new ComboBox<>();
        ComboBox<String> targetBox = new ComboBox<>();

        // ComboBoxes for multiplicities
        ComboBox<String> sourceMultiplicityBox = new ComboBox<>();
        ComboBox<String> targetMultiplicityBox = new ComboBox<>();

        // Populate class diagram ComboBoxes
        for (ClassDiagram diagram : classDiagrams) {
            sourceBox.getItems().add(diagram.getName());
            targetBox.getItems().add(diagram.getName());
        }

        String[] multiplicities = {"No multiplicity", "1", "0..1", "0..*", "1..*"};
        sourceMultiplicityBox.getItems().addAll(multiplicities);
        targetMultiplicityBox.getItems().addAll(multiplicities);

        sourceBox.setPromptText("Select Source");
        targetBox.setPromptText("Select Target");
        sourceMultiplicityBox.setPromptText("Source Multiplicity");
        targetMultiplicityBox.setPromptText("Target Multiplicity");

        TextField relationshipNameField = new TextField();
        relationshipNameField.setPromptText("Enter Name (optional)");

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        ChangeListener<Object> enableOkButtonListener = (obs, oldVal, newVal) -> {
            okButton.setDisable(
                    sourceBox.getValue() == null ||
                            targetBox.getValue() == null ||
                            sourceMultiplicityBox.getValue() == null ||
                            targetMultiplicityBox.getValue() == null
            );
        };

        sourceBox.valueProperty().addListener(enableOkButtonListener);
        targetBox.valueProperty().addListener(enableOkButtonListener);
        sourceMultiplicityBox.valueProperty().addListener(enableOkButtonListener);
        targetMultiplicityBox.valueProperty().addListener(enableOkButtonListener);

        // Layout using GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Source:"), 0, 0);
        grid.add(sourceBox, 1, 0);
        grid.add(new Label("Source Multiplicity:"), 0, 1);
        grid.add(sourceMultiplicityBox, 1, 1);
        grid.add(new Label("Target:"), 0, 2);
        grid.add(targetBox, 1, 2);
        grid.add(new Label("Target Multiplicity:"), 0, 3);
        grid.add(targetMultiplicityBox, 1, 3);
        grid.add(new Label("Relationship Name:"), 0, 4);
        grid.add(relationshipNameField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (sourceBox.getValue() != null && targetBox.getValue() != null &&
                        sourceMultiplicityBox.getValue() != null && targetMultiplicityBox.getValue() != null) {
                    String relationshipName = relationshipNameField.getText().trim();
                    return new String[]{
                            sourceBox.getValue(),
                            targetBox.getValue(),
                            sourceMultiplicityBox.getValue().equals("No multiplicity") ? "0" : sourceMultiplicityBox.getValue(),
                            targetMultiplicityBox.getValue().equals("No multiplicity") ? "0" : targetMultiplicityBox.getValue(),
                            relationshipName
                    };
                }
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null);
    }


    private String[] promptForSourceAndTargetClassesWithDefaults(
            String defaultSource,
            String defaultTarget,
            String defaultSourceMultiplicity,
            String defaultTargetMultiplicity,
            String defaultRelationshipName
    )
    {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Edit Relationship");
        dialog.setHeaderText("Select the source and target class diagrams, specify multiplicities, and enter a relationship name.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // ComboBoxes for source and target classes
        ComboBox<String> sourceBox = new ComboBox<>();
        ComboBox<String> targetBox = new ComboBox<>();

        // ComboBoxes for multiplicities
        ComboBox<String> sourceMultiplicityBox = new ComboBox<>();
        ComboBox<String> targetMultiplicityBox = new ComboBox<>();

        // Populate class diagram ComboBoxes
        for (ClassDiagram diagram : classDiagrams) {
            sourceBox.getItems().add(diagram.getName());
            targetBox.getItems().add(diagram.getName());
        }

        // Populate multiplicity ComboBoxes, including "No multiplicity"
        String[] multiplicities = {"No multiplicity", "1", "0..1", "0..*", "1..*"};
        sourceMultiplicityBox.getItems().addAll(multiplicities);
        targetMultiplicityBox.getItems().addAll(multiplicities);

        // Set default values for ComboBoxes
        sourceBox.setValue(defaultSource);
        targetBox.setValue(defaultTarget);
        sourceMultiplicityBox.setValue(defaultSourceMultiplicity == "0" ? "No multiplicity" : defaultSourceMultiplicity);
        targetMultiplicityBox.setValue(defaultTargetMultiplicity == "0" ? "No multiplicity" : defaultTargetMultiplicity);

        // TextField for relationship name
        TextField relationshipNameField = new TextField();
        relationshipNameField.setPromptText("Enter Name (optional)");
        relationshipNameField.setText(defaultRelationshipName);

        // Disable OK button initially
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        // Change listener to enable the OK button only when all mandatory fields are filled
        ChangeListener<Object> enableOkButtonListener = (obs, oldVal, newVal) -> {
            okButton.setDisable(
                    sourceBox.getValue() == null ||
                            targetBox.getValue() == null ||
                            sourceMultiplicityBox.getValue() == null ||
                            targetMultiplicityBox.getValue() == null
            );
        };

        sourceBox.valueProperty().addListener(enableOkButtonListener);
        targetBox.valueProperty().addListener(enableOkButtonListener);
        sourceMultiplicityBox.valueProperty().addListener(enableOkButtonListener);
        targetMultiplicityBox.valueProperty().addListener(enableOkButtonListener);

        // Layout using GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Source:"), 0, 0);
        grid.add(sourceBox, 1, 0);
        grid.add(new Label("Source Multiplicity:"), 0, 1);
        grid.add(sourceMultiplicityBox, 1, 1);
        grid.add(new Label("Target:"), 0, 2);
        grid.add(targetBox, 1, 2);
        grid.add(new Label("Target Multiplicity:"), 0, 3);
        grid.add(targetMultiplicityBox, 1, 3);
        grid.add(new Label("Relationship Name:"), 0, 4);
        grid.add(relationshipNameField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (sourceBox.getValue() != null && targetBox.getValue() != null &&
                        sourceMultiplicityBox.getValue() != null && targetMultiplicityBox.getValue() != null) {
                    String relationshipName = relationshipNameField.getText().trim();
                    return new String[]{
                            sourceBox.getValue(),
                            targetBox.getValue(),
                            sourceMultiplicityBox.getValue().equals("No multiplicity") ? "0" : sourceMultiplicityBox.getValue(),
                            targetMultiplicityBox.getValue().equals("No multiplicity") ? "0" : targetMultiplicityBox.getValue(),
                            relationshipName
                    };
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
        addRelationship("association");
    }

    @FXML
    private void handleAddComposition() {
        addRelationship("composition");
    }

    @FXML
    private void handleAddAggregation() {
        addRelationship("aggregation");
    }

    @FXML
    private void handleAddGeneralization() {
        addGeneralization();
    }

    private void addGeneralization() {
        String[] details = promptForSourceAndTargetClassesWithoutExtras();
        if (details == null) return;

        String sourceName = details[0];
        String targetName = details[1];

        try {
            ClassDiagram source = findDiagramByName(sourceName);
            ClassDiagram target = findDiagramByName(targetName);

            if (source == null || target == null) {
                showErrorAlert("Source or Target diagram not found.");
                return;
            }

            // Handle Generalization type relationship
            if (activeRelationship == null) {
                Relationship relationship = new Relationship(
                        source,
                        target,
                        "Generalization", // Relationship type
                        "0", // Default multiplicity for Generalization
                        "0", // Default multiplicity for Generalization
                        null, // No obstacles for Generalization
                        "" // Empty relationship name
                );

                addRelationshipToList(relationship);
            } else {
                activeRelationship.setSourceClass(source);
                activeRelationship.setTargetClass(target);
                activeRelationship.setRelationName(""); // Empty name for Generalization
                activeRelationship.setType("Generalization");
                activeRelationship.setSourceMultiplicity("0");
                activeRelationship.setTargetMultiplicity("0");
            }

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    private String[] promptForSourceAndTargetClassesWithoutExtras() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add Generalization");
        dialog.setHeaderText("Select the source and target class diagrams.");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<String> sourceBox = new ComboBox<>();
        ComboBox<String> targetBox = new ComboBox<>();

        // Populate class diagram ComboBoxes
        for (ClassDiagram diagram : classDiagrams) {
            sourceBox.getItems().add(diagram.getName());
            targetBox.getItems().add(diagram.getName());
        }

        sourceBox.setPromptText("Select Source");
        targetBox.setPromptText("Select Target");

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        ChangeListener<Object> enableOkButtonListener = (obs, oldVal, newVal) -> {
            okButton.setDisable(
                    sourceBox.getValue() == null || targetBox.getValue() == null
            );
        };

        sourceBox.valueProperty().addListener(enableOkButtonListener);
        targetBox.valueProperty().addListener(enableOkButtonListener);

        // Layout using GridPane
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
                    return new String[]{
                            sourceBox.getValue(),
                            targetBox.getValue(),
                    };
                }
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null); // Return null if no result is found
    }


    private void addRelationship(String relationshipType) {
        String[] details = promptForSourceAndTargetClasses();
        if (details == null) return;

        String sourceName = details[0];
        String targetName = details[1];
        String sourceMultiplicity = details[2];
        String targetMultiplicity = details[3];
        String relationshipName = details[4];

        try {
            ClassDiagram source = findDiagramByName(sourceName);
            ClassDiagram target = findDiagramByName(targetName);

            if (source == null || target == null) {
                showErrorAlert("Source or Target diagram not found.");
                return;
            }

            if (activeRelationship == null) {
                Relationship relationship = new Relationship(
                        source,
                        target,
                        relationshipType,
                        sourceMultiplicity,
                        targetMultiplicity,
                        obstacles,
                        relationshipName
                );

                addRelationshipToList(relationship);
            } else {
                activeRelationship.setSourceClass(source);
                activeRelationship.setTargetClass(target);
                activeRelationship.setRelationName(relationshipName);
                activeRelationship.setType(relationshipType);
                activeRelationship.setSourceMultiplicity(sourceMultiplicity);
                activeRelationship.setTargetMultiplicity(targetMultiplicity);
            }

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }
    private void addRelationshipToList(Relationship relationship) {
        switch (relationship.getType().toLowerCase()) {
            case "association":
                associations.add(relationship);
                break;
            case "composition":
                compositions.add(relationship);
                break;
            case "aggregation":
                aggregations.add(relationship);
                break;
            case "realization":
                realizations.add(relationship);
                break;
            case "generalization":
                generalizations.add(relationship);
                break;
            default:
                throw new IllegalArgumentException("Invalid relationship type.");
        }
        relationship.relationNameProperty().addListener((obs, oldName, newName) -> updateListView());
        updateListView();
    }


    @FXML
    public void handleAddRealization() {
        Pair<String, String> names = promptForSourceAndTargetInterface();
        if (names == null) return;

        try {
            ClassDiagram source = findDiagramByName(names.getKey());
            InterfaceData target = findInterfaceDiagramByName(names.getValue());

            Relationship realization = new Relationship(source, target, "Realization", "0", "0", obstacles);
            realizations.add(realization);

            realization.relationNameProperty().addListener((obs, oldName, newName) -> updateListView());
            updateListView();

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
    private void handleNewProject() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("New Project");
        confirmationAlert.setHeaderText("Are you sure you want to create a new project?");
        confirmationAlert.setContentText("Unsaved changes will be lost.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            clearWorkspace();
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
    }


    @FXML
    private void handleSaveProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Start XML document
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<Project>\n");

                // Save Class Diagrams
                writer.write("    <ClassDiagrams>\n");
                for (ClassDiagram diagram : classDiagrams) {
                    writer.write("        <ClassDiagram>\n");
                    writer.write("            <Name>" + diagram.getName() + "</Name>\n");
                    writer.write("            <X>" + diagram.getX() + "</X>\n");  // Save X coordinate
                    writer.write("            <Y>" + diagram.getY() + "</Y>\n");  // Save Y coordinate
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

                // Save Interface Diagrams
                writer.write("    <InterfaceDiagrams>\n");
                for (InterfaceData diagram : interfaceDiagrams) {
                    writer.write("        <InterfaceDiagram>\n");
                    writer.write("            <Name>" + diagram.getName() + "</Name>\n");
                    writer.write("            <X>" + diagram.getX() + "</X>\n");  // Save X coordinate
                    writer.write("            <Y>" + diagram.getY() + "</Y>\n");  // Save Y coordinate
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


                writer.write("    <Relationships>\n");
                for (Relationship relationship : associations) {
                    saveRelationship(writer, relationship, "association");
                }
                for (Relationship relationship : aggregations) {
                    saveRelationship(writer, relationship, "aggregation");
                }
                for (Relationship relationship : compositions) {
                    saveRelationship(writer, relationship, "composition");
                }
                for (Relationship relationship : realizations) {
                    saveRelationship(writer, relationship, "Realization");
                }
                for (Relationship relationship : generalizations) {
                    saveRelationship(writer, relationship, "Generalization");
                }
                writer.write("    </Relationships>\n");

                writer.write("</Project>\n");

                showAlert(Alert.AlertType.INFORMATION, "Save Project", "Project saved successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Project", "Failed to save the project.");
                e.printStackTrace();
            }
        }
    }

    private void saveRelationship(BufferedWriter writer, Relationship relationship, String type) throws IOException {
        writer.write("        <Relationship>\n");
        writer.write("            <Type>" + type + "</Type>\n");
        writer.write("            <Source>" + relationship.getSourceClass().getName() + "</Source>\n");

        // If it's a Realization, the target is an interface, otherwise it's a class
        String targetName = (relationship.getTargetInterface() != null) ? relationship.getTargetInterface().getName() : relationship.getTargetClass().getName();
        writer.write("            <Target>" + targetName + "</Target>\n");

        writer.write("            <Name>" + relationship.getRelationName() + "</Name>\n");
        writer.write("            <SourceMultiplicity>" + relationship.getSourceClassMultiplicity() + "</SourceMultiplicity>\n");
        writer.write("            <TargetMultiplicity>" + relationship.getTargetClassMultiplicity() + "</TargetMultiplicity>\n");


        writer.write("            <SourceX>" + relationship.getStartX() + "</SourceX>\n");  // Source X
        writer.write("            <SourceY>" + relationship.getStartY() + "</SourceY>\n");  // Source Y
        writer.write("            <TargetX>" + relationship.getEndX() + "</TargetX>\n");    // Target X
        writer.write("            <TargetY>" + relationship.getEndY() + "</TargetY>\n");    // Target Y

        writer.write("        </Relationship>\n");
    }

    @FXML
    private void handleOpenProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Project");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try {
                // Clear existing diagrams and relationships
                classDiagrams.clear();
                interfaceDiagrams.clear();
                associations.clear();
                aggregations.clear();
                compositions.clear();
                realizations.clear();
                generalizations.clear();
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
                    ClassDiagram classDiagram = loadClassDiagram(classElement);
                    classDiagrams.add(classDiagram);
                }

                // Load Interface Diagrams
                NodeList interfaceDiagramNodes = document.getElementsByTagName("InterfaceDiagram");
                for (int i = 0; i < interfaceDiagramNodes.getLength(); i++) {
                    Element interfaceElement = (Element) interfaceDiagramNodes.item(i);
                    InterfaceData interfaceDiagram = loadInterfaceDiagram(interfaceElement);
                    interfaceDiagrams.add(interfaceDiagram);
                }

                // Load Relationships
                NodeList relationshipNodes = document.getElementsByTagName("Relationship");
                for (int i = 0; i < relationshipNodes.getLength(); i++) {
                    Element relationshipElement = (Element) relationshipNodes.item(i);
                    Relationship relationship = loadRelationship(relationshipElement);
                    addToRelationshipList(relationship);
                }

                redrawCanvas();
                showAlert(Alert.AlertType.INFORMATION, "Open Project", "Project loaded successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Open Project", "Failed to load the project.");
                e.printStackTrace();
            }
        }
    }

    private ClassDiagram loadClassDiagram(Element classElement) {
        String className = classElement.getElementsByTagName("Name").item(0).getTextContent();
        double x = Double.parseDouble(classElement.getElementsByTagName("X").item(0).getTextContent()); // Load X coordinate
        double y = Double.parseDouble(classElement.getElementsByTagName("Y").item(0).getTextContent()); // Load Y coordinate
        ClassDiagram classDiagram = new ClassDiagram();
        classDiagram.setName(className);
        classDiagram.setX(x);  // Set X coordinate
        classDiagram.setY(y);
        // Load Attributes
        NodeList attributeNodes = classElement.getElementsByTagName("Attribute");
        for (int j = 0; j < attributeNodes.getLength(); j++) {
            Element attributeElement = (Element) attributeNodes.item(j);
            AttributeData attribute = new AttributeData();
            attribute.setAccessModifier(attributeElement.getElementsByTagName("AccessModifier").item(0).getTextContent());
            attribute.setDataType(attributeElement.getElementsByTagName("DataType").item(0).getTextContent());
            attribute.setName(attributeElement.getElementsByTagName("Name").item(0).getTextContent());
            classDiagram.getAttributes().add(attribute);
        }

        // Load Methods
        NodeList methodNodes = classElement.getElementsByTagName("Method");
        for (int j = 0; j < methodNodes.getLength(); j++) {
            Element methodElement = (Element) methodNodes.item(j);
            MethodData method = new MethodData();
            method.setAccessModifier(methodElement.getElementsByTagName("AccessModifier").item(0).getTextContent());
            method.setReturnType(methodElement.getElementsByTagName("ReturnType").item(0).getTextContent());
            method.setName(methodElement.getElementsByTagName("Name").item(0).getTextContent());
            classDiagram.getMethods().add(method);
        }

        return classDiagram;
    }

    private InterfaceData loadInterfaceDiagram(Element interfaceElement) {
        String interfaceName = interfaceElement.getElementsByTagName("Name").item(0).getTextContent();
        double x = Double.parseDouble(interfaceElement.getElementsByTagName("X").item(0).getTextContent());  // Load X coordinate
        double y = Double.parseDouble(interfaceElement.getElementsByTagName("Y").item(0).getTextContent());  // Load Y coordinate

        InterfaceData interfaceDiagram = new InterfaceData();
        interfaceDiagram.setName(interfaceName);
        interfaceDiagram.setX(x);  // Set X coordinate
        interfaceDiagram.setY(y);

        // Load Methods
        NodeList methodNodes = interfaceElement.getElementsByTagName("Method");
        for (int j = 0; j < methodNodes.getLength(); j++) {
            Element methodElement = (Element) methodNodes.item(j);
            MethodData method = new MethodData();
            method.setAccessModifier(methodElement.getElementsByTagName("AccessModifier").item(0).getTextContent());
            method.setReturnType(methodElement.getElementsByTagName("ReturnType").item(0).getTextContent());
            method.setName(methodElement.getElementsByTagName("Name").item(0).getTextContent());
            interfaceDiagram.getMethods().add(method);
        }

        return interfaceDiagram;
    }

    private Relationship loadRelationship(Element relationshipElement) {
        // Extract standard relationship fields
        String type = relationshipElement.getElementsByTagName("Type").item(0).getTextContent();
        String sourceName = relationshipElement.getElementsByTagName("Source").item(0).getTextContent();
        String targetName = relationshipElement.getElementsByTagName("Target").item(0).getTextContent();
        String relationName = relationshipElement.getElementsByTagName("Name").item(0).getTextContent();

        // Extract coordinates
        double sourceX = Double.parseDouble(relationshipElement.getElementsByTagName("SourceX").item(0).getTextContent());
        double sourceY = Double.parseDouble(relationshipElement.getElementsByTagName("SourceY").item(0).getTextContent());
        double targetX = Double.parseDouble(relationshipElement.getElementsByTagName("TargetX").item(0).getTextContent());
        double targetY = Double.parseDouble(relationshipElement.getElementsByTagName("TargetY").item(0).getTextContent());

        // Extract multiplicities
        String sourceMultiplicity = relationshipElement.getElementsByTagName("SourceMultiplicity").item(0).getTextContent();
        String targetMultiplicity = relationshipElement.getElementsByTagName("TargetMultiplicity").item(0).getTextContent();

        // Find source and target diagrams/interfaces
        ClassDiagram source = findDiagramByName(sourceName);
        ClassDiagram target = null;
        InterfaceData targetInterface = null;

        if (type.equals("Realization")) {
            targetInterface = findInterfaceDiagramByName(targetName);
        } else {
            target = findDiagramByName(targetName);
        }

        // Create the relationship with multiplicities
        Relationship relationship = new Relationship(source, target, type, sourceMultiplicity, targetMultiplicity, obstacles, relationName);

        // Set coordinates
        relationship.setStartX(sourceX); // Set SourceX
        relationship.setStartY(sourceY); // Set SourceY
        relationship.setEndX(targetX);   // Set TargetX
        relationship.setEndY(targetY);   // Set TargetY

        if (type.equals("Realization")) {
            relationship.setTargetInterface(targetInterface);
        }
        return relationship;
    }

    private void addToRelationshipList(Relationship relationship) {
        switch (relationship.getType()) {
            case "association":
                associations.add(relationship);
                break;
            case "aggregation":
                aggregations.add(relationship);
                break;
            case "composition":
                compositions.add(relationship);
                break;
            case "Realization":
                realizations.add(relationship);
                break;
            case "Generalization":
                generalizations.add(relationship);
                break;
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
                " and use-case diagrams. Additionally, it allows users to generate " +
                "code based on the designed diagrams, streamlining the software development process.\n\n" +
                "Creators:\n" +
                "SAMAR\n" +
                "NOMAN\n" +
                "HASSAAN\n\n" +
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
