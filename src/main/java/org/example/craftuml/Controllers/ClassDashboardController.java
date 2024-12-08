package org.example.craftuml.Controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
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

/**
 * Controller for managing the class dashboard in the application.
 * This class is responsible for handling user interactions related to
 * class diagrams, including generating code, exporting diagrams, and
 * providing functionality for various UI elements (e.g., buttons, menus).
 * It manages the actions that can be performed on class diagrams, such as
 * adding, removing, and updating class attributes and methods.
 * The class also handles the export and save functionality for the class
 * diagrams in various formats (e.g., text, image).
 *
 * <p>This class is typically linked to a UI framework like JavaFX, and is
 * responsible for responding to user input and performing corresponding
 * actions within the application, such as showing alerts, generating code,
 * or saving diagrams.</p>
 */
public class ClassDashboardController {
    /**
     * The canvas used to draw the class diagram.
     * It is defined as an FXML element, allowing it to be accessed and manipulated in the controller.
     */
    @FXML
    private Canvas drawingCanvas;
    /**
     * The scroll pane that allows users to scroll through the class diagram when it exceeds the visible area.
     * It is defined as an FXML element.
     */
    @FXML
    private ScrollPane scrollPane;

    /**
     * The list view displaying information about the class model.
     * The list is populated with model information, such as class names and other relevant data.
     */
    @FXML
    private ListView<String> modelInfoList = new ListView<>();

    /**
     * An observable list holding names of models to be displayed in the `modelInfoList`.
     */
    private ObservableList<String> modelNames = FXCollections.observableArrayList();

    /**
     * An observable list that holds the model objects to be displayed and interacted with in the diagram.
     */
    private ObservableList<Object> modelObjects = FXCollections.observableArrayList();

    /**
     * The active class diagram being edited or displayed.
     */
    private ClassDiagram classDiagram, activeDiagram;

    /**
     * The active interface diagram being edited or displayed.
     */
    private InterfaceData interfaceDiagram, activeInterface;

    /**
     * The active relationship being managed within the diagram.
     */
    private Relationship activeRelationship;
    /**
     * Flag indicating whether the source class diagram is being dragged.
     */
    private boolean isDraggingSource = false;

    /**
     * Flag indicating whether the target class diagram is being dragged.
     */
    private boolean isDraggingTarget = false;

    /**
     * Flag indicating whether the current diagram can be saved.
     */
    private boolean isSaveable = false;

    /**
     * A list of obstacles (rectangles) used for collision detection or boundaries in the diagram.
     */
    private List<Rectangle> obstacles = new ArrayList<>();

    /**
     * A list of interface diagrams that are part of the current diagram.
     */
    private List<InterfaceData> interfaceDiagrams = new ArrayList<>();

    /**
     * A list of class diagrams that are part of the current diagram.
     */
    private List<ClassDiagram> classDiagrams = new ArrayList<>();

    /**
     * The starting X-coordinate of a drag action.
     */
    private double dragStartX = 0;

    /**
     * The starting Y-coordinate of a drag action.
     */
    private double dragStartY = 0;


    /**
     * A list of relationship objects representing associations in the diagram.
     */
    private List<Relationship> associations = new ArrayList<>();

    /**
     * A list of relationship objects representing compositions in the diagram.
     */
    private List<Relationship> compositions = new ArrayList<>();

    /**
     * A list of relationship objects representing aggregations in the diagram.
     */
    private List<Relationship> aggregations = new ArrayList<>();

    /**
     * A list of relationship objects representing realizations in the diagram.
     */
    private List<Relationship> realizations = new ArrayList<>();

    /**
     * A list of relationship objects representing generalizations in the diagram.
     */
    private List<Relationship> generalizations = new ArrayList<>();
    /**
     * The context menu used for interacting with the diagram objects (e.g., classes, relationships).
     */
    private ContextMenu contextMenu;


    /**
     * The `initialize()` method is responsible for setting up the initial state and actions for the class diagram dashboard.
     * It prepares the canvas for interaction, sets up the model info list to display various diagram elements,
     * and adds listeners to handle updates to class diagrams, interface diagrams, and relationships.
     * The method also organizes the list of model items into sections for better categorization.
     */

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

    /**
     * Adds listeners to the relation name properties of each relationship in the given list.
     * This ensures that any changes to the name of a relationship trigger a refresh of the list view.
     *
     * @param relationships the list of relationships to add listeners to
     */
    private void addRelationshipListeners(List<Relationship> relationships) {
        relationships.forEach(relationship ->
                relationship.relationNameProperty().addListener((obs, oldName, newName) -> updateListView()));
    }

    /**
     * Updates the model info list view by categorizing diagram items into sections.
     * It organizes class diagrams, interface diagrams, and relationships into separate sections and
     * refreshes the list view with the latest data.
     */
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
        modelInfoList.setItems(FXCollections.observableList(modelNames));
        isSaveable = false;

    }

    /**
     * Redraws the entire canvas by clearing it and then re-rendering all class diagrams,
     * interface diagrams, and relationships.
     * This method iterates over all the stored diagrams and relationships and invokes
     * their respective drawing methods to update the canvas display.
     */
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
        isSaveable = false;
    }

    /**
     * Handles the creation and addition of a new class diagram to the drawing canvas.
     * The method displays a dialog for creating the new class diagram, calculates its
     * position on the canvas, and adds it to the list of class diagrams. It also updates
     * the list view and renders the diagram on the canvas.
     */
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

    /**
     * Adds a given class diagram as an obstacle to the drawing area.
     * This method calculates the dimensions of the class diagram and adds
     * a rectangle representing the obstacle to the list of obstacles.
     *
     * @param classDiagram the class diagram to be added as an obstacle
     */
    public void addClassDiagramAsObstacle(ClassDiagram classDiagram) {
        double x = classDiagram.getX();
        double y = classDiagram.getY();
        double width = calculateDiagramWidth(classDiagram, drawingCanvas.getGraphicsContext2D());
        double height = calculateDiagramHeight(classDiagram);
        obstacles.add(new Rectangle(x, y, width, height));
    }

    /**
     * Creates and renders a class diagram on the drawing canvas.
     * This method calculates the dimensions of the class diagram based on the number of attributes and methods,
     * then draws the diagram (with name, attributes, and methods) on the canvas.
     * It also handles the diagram's bounding box and context menu for interactions.
     *
     * @param classDiagram the class diagram to be drawn
     */
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

    /**
     * Deletes a class diagram from the list of class diagrams and all related relationships.
     * A confirmation alert is shown before the deletion. Relationships involving the class diagram
     * are removed from the associations, compositions, aggregations, realizations, and generalizations lists.
     * After deletion, the canvas is redrawn, and the ListView is updated accordingly.
     *
     * @param classDiagram The class diagram to be deleted.
     */
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

    /**
     * Updates the ListView to remove the given class diagram and all related relationships.
     * The method iterates through all the model objects and filters out the ones related to the class diagram
     * to be deleted. Then, it updates the ListView to reflect the changes.
     *
     * @param classDiagram The class diagram to be removed from the ListView.
     */
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

    /**
     * Edits the given class diagram by opening a dialog for updating its name, attributes, and methods.
     * If the user confirms the changes, the diagram is updated with the new values.
     * The canvas is redrawn after the update.
     *
     * @param classDiagram The class diagram to be edited.
     */
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

    /**
     * Checks if a given point (mouseX, mouseY) is within the bounds of the specified class diagram.
     * The bounds are calculated based on the diagram's position, width, and height.
     *
     * @param mouseX The x-coordinate of the mouse pointer.
     * @param mouseY The y-coordinate of the mouse pointer.
     * @param diagram The class diagram to check against.
     * @param gc The graphics context used to calculate diagram width and height.
     * @return true if the point is within the bounds of the diagram, false otherwise.
     */
    private boolean isWithinBounds(double mouseX, double mouseY, ClassDiagram diagram, GraphicsContext gc) {
        double x = diagram.getX();
        double y = diagram.getY();
        double width = calculateDiagramWidth(diagram, gc);
        double height = calculateDiagramHeight(diagram);
        boolean isWithin = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        return isWithin;
    }


    /**
     * Calculates the width of a given class diagram based on its name, attributes, and methods.
     * The width is determined by the longest text element in the diagram, with padding added.
     *
     * @param diagram The class diagram whose width is to be calculated.
     * @param gc The graphics context used to measure text width.
     * @return The calculated width of the diagram.
     */
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

    /**
     * Calculates the height of a given class diagram based on its attributes and methods.
     * The height includes space for the class name, attributes, methods, and some padding.
     *
     * @param diagram The class diagram whose height is to be calculated.
     * @return The calculated height of the diagram.
     */
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

    /**
     * Initializes the event handlers for interactions on the drawing canvas.
     * Handles mouse presses, drags, releases, moves, and clicks for class diagrams, interfaces, and relationships.
     * Sets up context menus, dragging functionalities, and resizing behaviors.
     */
    private void initializeCanvasHandlers() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

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

    /**
     * Resizes the canvas if the coordinates (x, y) exceed the current canvas size.
     * The method ensures that the canvas is enlarged by a padding amount to accommodate new points.
     *
     * @param x The x-coordinate to check against the canvas width.
     * @param y The y-coordinate to check against the canvas height.
     */
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

    /**
     * Closes the context menu if it is currently displayed.
     * This method checks if the context menu is visible and hides it.
     */
    private void closeContextMenu() {
        if (contextMenu != null && contextMenu.isShowing()) {
            contextMenu.hide();
        }
    }

    /**
     * Edits an existing relationship in the diagram by updating the source, target, and other relationship properties.
     * Depending on the type of relationship (association, aggregation, composition, realization, or generalization),
     * the method prompts the user to provide new values and updates the relationship accordingly.
     *
     * @param activeRelationship The active relationship object to be edited.
     */
    private void editRelationship(Relationship activeRelationship) {
        if (activeRelationship != null) {
            String sourceName = activeRelationship.getSourceClass().getName();
            String targetName = activeRelationship.getType().equals("Realization") ? activeRelationship.getTargetInterface().getName() : activeRelationship.getTargetClass().getName();
            String relationshipName = activeRelationship.getRelationName();
            String sourceMultiplicity = activeRelationship.getSourceClassMultiplicity();
            String targetMultiplicity = activeRelationship.getTargetClassMultiplicity();

            if (!activeRelationship.getType().equals("Realization") && !activeRelationship.getType().equals("Generalization")) {
                String[] namesAndRelationshipName = promptForSourceAndTargetClassesWithDefaults(sourceName, targetName,activeRelationship.getType(), sourceMultiplicity, targetMultiplicity, relationshipName);

                if (namesAndRelationshipName == null) return;

                String newSourceName = namesAndRelationshipName[0];
                String newTargetName = namesAndRelationshipName[1];
                String newSourceMul = namesAndRelationshipName[2];
                String newTargetMul = namesAndRelationshipName[3];
                String newRelationshipName = namesAndRelationshipName[4];

                try {
                    ClassDiagram newSource = findDiagramByName(newSourceName);
                    ClassDiagram newTarget = findDiagramByName(newTargetName);
                    boolean sourceTargetSwapped = (newSource.equals(activeRelationship.getTargetClass()) && newTarget.equals(activeRelationship.getSourceClass()));

                    if ((!newSource.equals(activeRelationship.getSourceClass()) || !newTarget.equals(activeRelationship.getTargetClass())) && !sourceTargetSwapped) {
                        Relationship existingRelationship = findExistingRelationship(activeRelationship);
                        if (existingRelationship != null) {
                            switch (existingRelationship.getType()) {
                                case "association" -> associations.remove(existingRelationship);
                                case "aggregation" -> aggregations.remove(existingRelationship);
                                case "composition" -> compositions.remove(existingRelationship);
                            }
                        }
                    }

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

            else if (activeRelationship.getType().equals("Realization")) {
                String[] namesAndRelationshipName = promptForSourceAndTargetClassesWithDefaults2(sourceName, targetName);

                if (namesAndRelationshipName == null) return;

                String newSourceName = namesAndRelationshipName[0];
                String newTargetName = namesAndRelationshipName[1];

                try {
                    ClassDiagram newSource = findDiagramByName(newSourceName);
                    InterfaceData newTarget = findInterfaceDiagramByName(newTargetName);

                    if (!newSource.equals(activeRelationship.getSourceClass()) || !newTarget.equals(activeRelationship.getTargetInterface())) {
                        Relationship existingRelationship = findExistingRelationship(activeRelationship);
                        if (existingRelationship != null) {
                            realizations.remove(existingRelationship);
                        }
                    }

                    activeRelationship.setSourceClass(newSource);
                    activeRelationship.setTargetInterface(newTarget);

                    redrawCanvas();

                } catch (IllegalArgumentException e) {
                    showErrorAlert(e.getMessage());
                }
            }
            // Edit for Generalization relationships
            else if (activeRelationship.getType().equals("Generalization")) {
                String[] namesAndRelationshipName = promptForGeneralization(sourceName, targetName);

                if (namesAndRelationshipName == null) return;

                String newSourceName = namesAndRelationshipName[0];
                String newTargetName = namesAndRelationshipName[1];

                try {
                    ClassDiagram newSource = findDiagramByName(newSourceName);
                    ClassDiagram newTarget = findDiagramByName(newTargetName);

                    // Check if the source and target have changed, including the case where they are swapped
                    boolean sourceTargetSwapped = (newSource.equals(activeRelationship.getTargetClass()) && newTarget.equals(activeRelationship.getSourceClass()));

                    if ((!newSource.equals(activeRelationship.getSourceClass()) || !newTarget.equals(activeRelationship.getTargetClass())) && !sourceTargetSwapped) {
                        // Find and remove existing relationship if necessary
                        Relationship existingRelationship = findExistingRelationship(activeRelationship);
                        if (existingRelationship != null) {
                            generalizations.remove(existingRelationship);
                        }
                    }

                    activeRelationship.setSourceClass(newSource);
                    activeRelationship.setTargetClass(newTarget);

                    redrawCanvas();

                } catch (IllegalArgumentException e) {
                    showErrorAlert(e.getMessage());
                }
            }
            else {
                // Handle other relationship types (e.g., association, aggregation, composition, etc.)
                handleAdditionalRelationships(activeRelationship);
            }
        }
    }

    /**
     * Finds and returns an existing relationship in the diagram if one exists.
     * The method checks through various types of relationships (realization, association, aggregation, composition, generalization) to find a match.
     *
     * @param activeRelationship The relationship to search for.
     * @return The found relationship, or null if no match is found.
     */
    private Relationship findExistingRelationship(Relationship activeRelationship) {
        for (Relationship relationship : realizations) {
            if (relationship.equals(activeRelationship)) {
                return relationship;
            }
        }
        for (Relationship relationship : associations) {
            if (relationship.equals(activeRelationship)) {
                return relationship;
            }
        }
        for (Relationship relationship : aggregations) {
            if (relationship.equals(activeRelationship)) {
                return relationship;
            }
        }
        for (Relationship relationship : compositions) {
            if (relationship.equals(activeRelationship)) {
                return relationship;
            }
        }
        for (Relationship relationship : generalizations) {
            if (relationship.equals(activeRelationship)) {
                return relationship;
            }
        }

        return null;
    }

    /**
     * Handles additional types of relationships (association, aggregation, composition, and generalization).
     * Based on the type of relationship, the corresponding method is called to add or edit the relationship.
     *
     * @param activeRelationship The relationship to handle, based on its type.
     */
    private void handleAdditionalRelationships(Relationship activeRelationship) {
        switch (activeRelationship.getType()) {
            case "association":
                handleAddAssociation();
                break;
            case "aggregation":
                handleAddAggregation();
                break;
            case "composition":
                handleAddComposition();
                break;
            case "generalization":
                handleAddGeneralization();
                break;
            default:
                break;
        }
    }

    /**
     * Prompts the user to select source and target class diagrams for a generalization relationship.
     * This method opens a dialog to let the user pick source and target classes and returns the selected values.
     *
     * @param defaultSource The default source class diagram name.
     * @param defaultTarget The default target class diagram name.
     * @return An array of strings containing the source and target class names, multiplicities, and relationship name.
     */
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

    /**
     * Prompts the user to select source and target class diagrams, as well as entering a relationship name.
     * This method is used specifically for relationships between a class and an interface.
     *
     * @param sourceName The default source class diagram name.
     * @param targetName The default target interface diagram name.
     * @return An array containing the selected source and target names.
     */
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

    /**
     * Checks if the mouse coordinates are within a specified tolerance of the relationship line.
     * The method calculates the distance between the mouse position and the line and returns true if it's within the tolerance.
     *
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     * @param relationship The relationship to check.
     * @param gc The graphics context used for drawing.
     * @return True if the mouse is within the tolerance of the relationship line, otherwise false.
     */
    private boolean isWithinBounds(double mouseX, double mouseY, Relationship relationship, GraphicsContext gc) {
        double startX = relationship.getStartX();
        double startY = relationship.getStartY();
        double endX = relationship.getEndX();
        double endY = relationship.getEndY();

        double tolerance = 5.0;
        return isPointNearLine(mouseX, mouseY, startX, startY, endX, endY, tolerance);
    }

    /**
     * Checks if a given point is near a line segment, within a specified tolerance.
     * The distance from the point to the line is calculated, and if it's less than the tolerance, the method returns true.
     *
     * @param pointX The x-coordinate of the point.
     * @param pointY The y-coordinate of the point.
     * @param startX The x-coordinate of the start point of the line.
     * @param startY The y-coordinate of the start point of the line.
     * @param endX The x-coordinate of the end point of the line.
     * @param endY The y-coordinate of the end point of the line.
     * @param tolerance The tolerance within which the point should be considered near the line.
     * @return True if the point is within the tolerance of the line, otherwise false.
     */
    private boolean isPointNearLine(double pointX, double pointY, double startX, double startY, double endX, double endY, double tolerance) {
        double lineLength = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        double distance = Math.abs((endY - startY) * pointX - (endX - startX) * pointY + endX * startY - endY * startX) / lineLength;
        return distance <= tolerance;
    }

    /**
     * Displays a context menu at the location of a mouse event, offering options to edit or delete the selected diagram.
     * The menu options vary based on the type of diagram (class, interface, or relationship).
     *
     * @param event The mouse event that triggered the context menu.
     * @param diagramType The type of diagram (class, interface, or relationship) to determine the menu options.
     */
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

    /**
     * Deletes the specified relationship from its respective collection based on the relationship type.
     * The relationship is removed from the appropriate list (associations, compositions, etc.),
     * and the ListView is updated. Additionally, the canvas is redrawn after the deletion.
     *
     * @param activeRelationship The relationship to delete.
     */
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

    /**
     * Removes the specified relationship from the model's list view by filtering out the relationship
     * from the list of model objects and model names, and then updating the list view.
     *
     * @param activeRelationship The relationship to remove from the list view.
     */
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

    /**
     * Handles the addition of an interface diagram. This method opens the dialog for the user to create a new interface
     * diagram, positions it on the canvas, and updates the list of interface diagrams.
     *
     * @see InterfaceDiagramUI#showInterfaceDiagramDialog()
     */
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

    /**
     * Creates and draws the interface diagram on the canvas, including its name and methods.
     * The interface diagram is drawn with a specified stereotype and method list, with the size
     * dynamically calculated based on the content.
     *
     * @param interfaceDiagram The interface diagram to be drawn on the canvas.
     */
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

    /**
     * Calculates the width of the given text string when rendered with the specified graphics context's font.
     * This method uses a temporary `Text` object to determine the layout bounds of the text.
     *
     * @param text The text whose width is to be calculated.
     * @param gc The graphics context used to retrieve the font.
     * @return The width of the text in pixels.
     */
    private double calculateTextWidth(String text, GraphicsContext gc) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        return tempText.getLayoutBounds().getWidth();
    }

    /**
     * Prompts the user with a confirmation dialog to delete the given interface diagram. If confirmed,
     * it removes the interface diagram from the list of interface diagrams, deletes any associated
     * realization relationships, and updates the canvas.
     *
     * @param interfaceDiagram The interface diagram to be deleted.
     */
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

    /**
     * Removes the specified interface diagram from the list view by filtering it out from the model's
     * lists of names and objects. This ensures the ListView is updated to reflect the removal.
     *
     * @param interfaceDiagram The interface diagram to remove from the list view.
     */
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

    /**
     * Opens a dialog to edit the specified interface diagram. If the diagram is updated,
     * the new name and methods are applied to the diagram, and the canvas is redrawn.
     *
     * @param interfaceDiagram The interface diagram to edit.
     */
    private void editInterfaceDiagram(InterfaceData interfaceDiagram) {
        InterfaceDiagramUI interfaceDiagramUI = new InterfaceDiagramUI(drawingCanvas, interfaceDiagram,interfaceDiagrams);
        InterfaceData updatedDiagram = interfaceDiagramUI.showInterfaceDiagramDialog();

        if (updatedDiagram != null) {
            interfaceDiagram.setName(updatedDiagram.getName());
            interfaceDiagram.setMethods(updatedDiagram.getMethods());
            redrawCanvas();
        }
    }

    /**
     * Checks whether the given mouse coordinates are within the bounds of the specified interface diagram on the canvas.
     *
     * @param mouseX The X coordinate of the mouse pointer.
     * @param mouseY The Y coordinate of the mouse pointer.
     * @param diagram The interface diagram to check.
     * @param gc The graphics context used to calculate the diagram's dimensions.
     * @return True if the mouse pointer is within the bounds of the diagram, otherwise false.
     */
    private boolean isWithinBounds(double mouseX, double mouseY, InterfaceData diagram, GraphicsContext gc) {
        double x = diagram.getX();
        double y = diagram.getY();
        double width = calculateDiagramWidth(diagram, gc);
        double height = calculateDiagramHeight(diagram);
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /**
     * Calculates the width of a diagram based on its name and methods.
     * The width is calculated as the maximum width of the diagram name and its methods, with some padding applied.
     *
     * @param diagram The interface diagram whose width is to be calculated.
     * @param gc      The graphics context to use for text measurements.
     * @return The calculated width of the diagram.
     */
    private double calculateDiagramWidth(InterfaceData diagram, GraphicsContext gc) {
        double maxWidth = calculateTextWidth("<<interface>>", gc);
        maxWidth = Math.max(maxWidth, calculateTextWidth(diagram.getName(), gc));
        for (MethodData method : diagram.getMethods()) {
            String methodText = method.getAccessModifier() + " " + method.getName() + " : " + method.getReturnType();
            maxWidth = Math.max(maxWidth, calculateTextWidth(methodText, gc));
        }
        return maxWidth * 1.3;
    }

    /**
     * Calculates the height of a diagram based on its name and methods.
     * The height is determined by the height of the interface name and the methods.
     *
     * @param diagram The interface diagram whose height is to be calculated.
     * @return The calculated height of the diagram.
     */
    private double calculateDiagramHeight(InterfaceData diagram) {
        double interfaceNameHeight = 40;
        double methodHeight = Math.max(30 * diagram.getMethods().size(), 30);
        return interfaceNameHeight + methodHeight;
    }

    /**
     * Prompts the user to select source and target class diagrams, specify multiplicities,
     * and optionally enter a relationship name for a given relationship type.
     *
     * @param relationType The type of relationship (e.g., "association").
     * @return An array containing the source diagram name, target diagram name,
     *         source multiplicity, target multiplicity, and the relationship name.
     */
    private String[] promptForSourceAndTargetClasses(String relationType) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add "+relationType.toUpperCase());
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

        // Flag to avoid recursive listener triggers
        final boolean[] isUpdating = {false};

// Dynamic filtering for targetBox based on sourceBox selection
        sourceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating[0]) return; // Skip updates if already updating
            isUpdating[0] = true;

            String currentTargetSelection = targetBox.getValue(); // Preserve current selection
            targetBox.getItems().clear();

            for (ClassDiagram diagram : classDiagrams) {
                // Exclude the selected source from the target options unless it's an association
                if (newValue == null || (diagram.getName().equals(newValue) && !relationType.equalsIgnoreCase("association"))) {
                    continue;
                }
                targetBox.getItems().add(diagram.getName());
            }

            // Restore previous selection if valid; otherwise, select the first valid option
            if (currentTargetSelection != null && targetBox.getItems().contains(currentTargetSelection)) {
                targetBox.setValue(currentTargetSelection);
            } else if (!targetBox.getItems().isEmpty()) {
                targetBox.setValue(targetBox.getItems().get(0));
            }

            isUpdating[0] = false;
        });

// Dynamic filtering for sourceBox based on targetBox selection
        targetBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating[0]) return; // Skip updates if already updating
            isUpdating[0] = true;

            String currentSourceSelection = sourceBox.getValue(); // Preserve current selection
            sourceBox.getItems().clear();

            for (ClassDiagram diagram : classDiagrams) {
                // Exclude the selected target from the source options unless it's an association
                if (newValue == null || (diagram.getName().equals(newValue) && !relationType.equalsIgnoreCase("association"))) {
                    continue;
                }
                sourceBox.getItems().add(diagram.getName());
            }

            // Restore previous selection if valid; otherwise, select the first valid option
            if (currentSourceSelection != null && sourceBox.getItems().contains(currentSourceSelection)) {
                sourceBox.setValue(currentSourceSelection);
            } else if (!sourceBox.getItems().isEmpty()) {
                sourceBox.setValue(sourceBox.getItems().get(0));
            }

            isUpdating[0] = false;
        });



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

    /**
     * Displays a dialog to allow the user to select source and target class diagrams, specify multiplicities,
     * and enter a relationship name. This method also supports default values for all fields to edit an existing
     * relationship.
     *
     * @param defaultSource The default source class diagram to be pre-selected in the dialog.
     * @param defaultTarget The default target class diagram to be pre-selected in the dialog.
     * @param relationType The type of relationship (e.g., "association") that is being edited.
     * @param defaultSourceMultiplicity The default multiplicity for the source class diagram (e.g., "1", "0..1").
     * @param defaultTargetMultiplicity The default multiplicity for the target class diagram (e.g., "1", "0..*").
     * @param defaultRelationshipName The default name of the relationship to be pre-filled in the text field.
     * @return A string array containing the source class, target class, source multiplicity, target multiplicity,
     *         and relationship name, or null if the user cancels the dialog or fails to fill required fields.
     */
    private String[] promptForSourceAndTargetClassesWithDefaults(
            String defaultSource,
            String defaultTarget,
            String relationType,
            String defaultSourceMultiplicity,
            String defaultTargetMultiplicity,
            String defaultRelationshipName
    ) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Edit "+relationType.toUpperCase());
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
        sourceMultiplicityBox.setValue(defaultSourceMultiplicity.equals("0") ? "No multiplicity" : defaultSourceMultiplicity);
        targetMultiplicityBox.setValue(defaultTargetMultiplicity.equals("0") ? "No multiplicity" : defaultTargetMultiplicity);

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

        // Dynamic filtering for targetBox based on sourceBox selection
        // Flag to avoid recursive listener triggers
        final boolean[] isUpdating = {false};

// Dynamic filtering for targetBox based on sourceBox selection
        sourceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating[0]) return; // Skip updates if already updating
            isUpdating[0] = true;

            String currentTargetSelection = targetBox.getValue(); // Preserve current target selection
            targetBox.getItems().clear();

            for (ClassDiagram diagram : classDiagrams) {
                // Exclude the selected source from the target options unless it's an association
                if (newValue == null || (diagram.getName().equals(newValue) && !relationType.equalsIgnoreCase("association"))) {
                    continue;
                }
                targetBox.getItems().add(diagram.getName());
            }

            // Restore previous target selection if valid; otherwise, select the first valid option
            if (currentTargetSelection != null && targetBox.getItems().contains(currentTargetSelection)) {
                targetBox.setValue(currentTargetSelection);
            } else if (!targetBox.getItems().isEmpty()) {
                targetBox.setValue(targetBox.getItems().get(0));
            }

            isUpdating[0] = false; // Reset the flag
        });

// Dynamic filtering for sourceBox based on targetBox selection
        targetBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating[0]) return; // Skip updates if already updating
            isUpdating[0] = true;

            String currentSourceSelection = sourceBox.getValue(); // Preserve current source selection
            sourceBox.getItems().clear();

            for (ClassDiagram diagram : classDiagrams) {
                // Exclude the selected target from the source options unless it's an association
                if (newValue == null || (diagram.getName().equals(newValue) && !relationType.equalsIgnoreCase("association"))) {
                    continue;
                }
                sourceBox.getItems().add(diagram.getName());
            }

            // Restore previous source selection if valid; otherwise, select the first valid option
            if (currentSourceSelection != null && sourceBox.getItems().contains(currentSourceSelection)) {
                sourceBox.setValue(currentSourceSelection);
            } else if (!sourceBox.getItems().isEmpty()) {
                sourceBox.setValue(sourceBox.getItems().get(0));
            }

            isUpdating[0] = false; // Reset the flag
        });


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

    /**
     * Finds a class diagram by its name.
     *
     * @param name The name of the class diagram to find.
     * @return The matching ClassDiagram object.
     * @throws IllegalArgumentException If no class diagram with the specified name is found.
     */
    private ClassDiagram findDiagramByName(String name) {
        for (ClassDiagram diagram : classDiagrams) {
            if (diagram.getName().equals(name)) {
                return diagram;
            }
        }
        throw new IllegalArgumentException("No class diagram found with name: " + name);
    }

    /**
     * Handles the action of adding an association relationship between class diagrams.
     * This is triggered when the "Add Association" button is clicked.
     */
    @FXML
    private void handleAddAssociation() {
        addRelationship("association");
    }

    /**
     * Handles the addition of a composition relationship by calling the appropriate method.
     */
    @FXML
    private void handleAddComposition() {
        addRelationship("composition");
    }

    /**
     * Handles the addition of an aggregation relationship by calling the appropriate method.
     */
    @FXML
    private void handleAddAggregation() {
        addRelationship("aggregation");
    }

    /**
     * Handles the addition of a generalization relationship by prompting the user to select source and target
     * class diagrams, then checks for existing relationships and updates or creates a new generalization
     * relationship as needed.
     */
    @FXML
    private void handleAddGeneralization() {
        addGeneralization();
    }

    /**
     * Prompts the user to select source and target class diagrams for a generalization relationship. If the
     * selected relationship already exists, it will be updated. If not, a new generalization relationship
     * will be created.
     */
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

            Relationship existingRelationship = null;
            for (Relationship relationship : generalizations)
            {
                if ((relationship.getSourceClass().equals(source) && relationship.getTargetClass().equals(target)) ||
                        (relationship.getSourceClass().equals(target) && relationship.getTargetClass().equals(source)))
                {
                    existingRelationship = relationship;
                    break;
                }
            }

            if (existingRelationship == null) {
                for (Relationship association : associations) {
                    if ((association.getSourceClass().equals(source) && association.getTargetClass().equals(target)) ||
                            (association.getSourceClass().equals(target) && association.getTargetClass().equals(source))) {
                        existingRelationship = association;
                        break;
                    }
                }

                // Check for composition
                if (existingRelationship == null) {
                    for (Relationship composition : compositions) {
                        if ((composition.getSourceClass().equals(source) && composition.getTargetClass().equals(target)) ||
                                (composition.getSourceClass().equals(target) && composition.getTargetClass().equals(source))) {
                            existingRelationship = composition;
                            break;
                        }
                    }
                }

                // Check for aggregation
                if (existingRelationship == null) {
                    for (Relationship aggregation : aggregations) {
                        if ((aggregation.getSourceClass().equals(source) && aggregation.getTargetClass().equals(target)) ||
                                (aggregation.getSourceClass().equals(target) && aggregation.getTargetClass().equals(source))) {
                            existingRelationship = aggregation;
                            break;
                        }
                    }
                }

                if (existingRelationship != null) {
                    if (existingRelationship.getType().equals("association")) {
                        associations.remove(existingRelationship);
                    } else if (existingRelationship.getType().equals("composition")) {
                        compositions.remove(existingRelationship);
                    } else if (existingRelationship.getType().equals("aggregation")) {
                        aggregations.remove(existingRelationship);
                    }
                }
            }
            if (existingRelationship != null) {
                generalizations.remove(existingRelationship);
            }

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

    /**
     * Prompts the user to select source and target class diagrams for a generalization relationship, without
     * additional options for multiplicities or relationship name. This is a simplified version of the dialog
     * for generalizations.
     *
     * @return A string array containing the names of the source and target class diagrams, or null if the user
     *         cancels the dialog or does not make valid selections.
     */
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

        // Flag to avoid recursive listener triggers
        final boolean[] isUpdating = {false};

        // Dynamic filtering for targetBox based on sourceBox selection
        sourceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating[0]) return; // Skip updates if already updating
            isUpdating[0] = true;

            String currentTargetSelection = targetBox.getValue(); // Preserve current target selection
            targetBox.getItems().clear();

            for (ClassDiagram diagram : classDiagrams) {
                // Exclude the selected source from the target options
                if (newValue == null || diagram.getName().equals(newValue)) {
                    continue;
                }
                targetBox.getItems().add(diagram.getName());
            }

            // Restore previous target selection if valid; otherwise, select the first valid option
            if (currentTargetSelection != null && targetBox.getItems().contains(currentTargetSelection)) {
                targetBox.setValue(currentTargetSelection);
            } else if (!targetBox.getItems().isEmpty()) {
                targetBox.setValue(targetBox.getItems().get(0));
            }

            isUpdating[0] = false; // Reset the flag
        });

        // Dynamic filtering for sourceBox based on targetBox selection
        targetBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating[0]) return; // Skip updates if already updating
            isUpdating[0] = true;

            String currentSourceSelection = sourceBox.getValue(); // Preserve current source selection
            sourceBox.getItems().clear();

            for (ClassDiagram diagram : classDiagrams) {
                // Exclude the selected target from the source options
                if (newValue == null || diagram.getName().equals(newValue)) {
                    continue;
                }
                sourceBox.getItems().add(diagram.getName());
            }

            // Restore previous source selection if valid; otherwise, select the first valid option
            if (currentSourceSelection != null && sourceBox.getItems().contains(currentSourceSelection)) {
                sourceBox.setValue(currentSourceSelection);
            } else if (!sourceBox.getItems().isEmpty()) {
                sourceBox.setValue(sourceBox.getItems().get(0));
            }

            isUpdating[0] = false; // Reset the flag
        });

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

    /**
     * Handles the addition of a new relationship based on the given relationship type.
     * This method prompts the user to select source and target classes, along with other relationship details
     * such as multiplicity and name. It checks for existing relationships of all types (association, composition,
     * aggregation, and generalization) between the selected classes and removes them if necessary, before creating
     * and adding a new relationship of the specified type.
     *
     * @param relationshipType The type of the relationship (e.g., "association", "composition", "aggregation").
     */
    private void addRelationship(String relationshipType) {
        String[] details = promptForSourceAndTargetClasses(relationshipType);
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

            // Check all types of relationships (association, composition, aggregation)
            Relationship existingRelationship = null;

            // Check for association
            for (Relationship association : associations)
            {
                if ((association.getSourceClass().equals(source) && association.getTargetClass().equals(target)) ||
                        (association.getSourceClass().equals(target) && association.getTargetClass().equals(source))) {
                    existingRelationship = association;
                    break;
                }
            }

            if (existingRelationship == null) {
                for (Relationship composition : compositions) {
                    if ((composition.getSourceClass().equals(source) && composition.getTargetClass().equals(target)) ||
                            (composition.getSourceClass().equals(target) && composition.getTargetClass().equals(source))) {
                        existingRelationship = composition;
                        break;
                    }
                }
            }

            // Check for aggregation
            if (existingRelationship == null) {
                for (Relationship aggregation : aggregations) {
                    if ((aggregation.getSourceClass().equals(source) && aggregation.getTargetClass().equals(target)) ||
                            (aggregation.getSourceClass().equals(target) && aggregation.getTargetClass().equals(source))) {
                        existingRelationship = aggregation;
                        break;
                    }
                }
            }
            if (existingRelationship == null) {
                for (Relationship generalization : generalizations) {
                    if ((generalization.getSourceClass().equals(source) && generalization.getTargetClass().equals(target)) ||
                            (generalization.getSourceClass().equals(target) && generalization.getTargetClass().equals(source))) {
                        existingRelationship = generalization;
                        break;
                    }
                }
            }

            // If an existing relationship is found, remove it from the appropriate list
            if (existingRelationship != null) {
                if (existingRelationship.getType().equals("association")) {
                    associations.remove(existingRelationship);
                } else if (existingRelationship.getType().equals("composition")) {
                    compositions.remove(existingRelationship);
                } else if (existingRelationship.getType().equals("aggregation")) {
                    aggregations.remove(existingRelationship);
                } else if (existingRelationship.getType().equals("Generalization")) {
                   generalizations.remove(existingRelationship);
                }

            }

            // Create the new relationship and add it to the correct list based on the relationship type
            Relationship newRelationship = new Relationship(
                    source,
                    target,
                    relationshipType,
                    sourceMultiplicity,
                    targetMultiplicity,
                    obstacles,
                    relationshipName
            );

            // Add the new relationship to the appropriate list
            if (relationshipType.equals("association")) {
                associations.add(newRelationship);
            } else if (relationshipType.equals("composition")) {
                compositions.add(newRelationship);
            } else if (relationshipType.equals("aggregation")) {
                aggregations.add(newRelationship);
            }

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    /**
     * Adds the given relationship to the appropriate list based on its type. It also sets up a listener
     * for any changes to the relationship's name, ensuring the list view is updated when the name changes.
     *
     * @param relationship The relationship to be added to the list.
     */
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

    /**
     * Handles the addition of a "Realization" relationship between a class and an interface. This method
     * prompts the user to select the source class and target interface, checks for any existing realization
     * relationship, and if none exists, creates and adds a new realization relationship to the list.
     */
    @FXML
    public void handleAddRealization() {
        Pair<String, String> names = promptForSourceAndTargetInterface();
        if (names == null) return;

        try {
            ClassDiagram source = findDiagramByName(names.getKey());
            InterfaceData target = findInterfaceDiagramByName(names.getValue());

            Relationship existingRelationship = null;
            for (Relationship relationship : realizations) {
                if ((relationship.getSourceClass().equals(source) && relationship.getTargetClass().equals(target)) ||
                        (relationship.getSourceClass().equals(target) && relationship.getTargetClass().equals(source)))
                {
                    existingRelationship = relationship;
                    break;
                }
            }

            if (existingRelationship != null) {
                realizations.remove(existingRelationship);
            }

            Relationship realization = new Relationship(source, target, "Realization", "0", "0", obstacles);
            realizations.add(realization);

            realization.relationNameProperty().addListener((obs, oldName, newName) -> updateListView());
            updateListView();

            redrawCanvas();

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    /**
     * Finds and returns the interface diagram by its name.
     *
     * @param value The name of the interface diagram to search for.
     * @return The interface diagram corresponding to the given name.
     * @throws IllegalArgumentException If no interface diagram with the given name is found.
     */
    private InterfaceData findInterfaceDiagramByName(String value) {
        for (InterfaceData diagram : interfaceDiagrams) {
            if (diagram.getName().equals(value)) {
                return diagram;
            }
        }
        throw new IllegalArgumentException("No interface diagram found with name: " + value);
    }

    /**
     * Prompts the user to select the source and target diagrams for a realization relationship.
     * This method displays a dialog with combo boxes for selecting the source class and target interface.
     *
     * @return A pair containing the names of the selected source class and target interface, or null if no selection is made.
     */
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

    /**
     * Displays an error alert with the given message.
     *
     * @param message The error message to display.
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the creation of a new project by checking if the current project has unsaved changes.
     * If there are unsaved changes, the user is prompted with a confirmation dialog.
     * If the user confirms, the workspace is cleared, otherwise the new project creation is canceled.
     */
    @FXML
    private void handleNewProject() {
        if(isSaveable)
        {
            clearWorkspace();
        }

        else {
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
    }

    /**
     * Clears the workspace by resetting various properties, clearing collections, and removing any graphical
     * representations of class and interface diagrams, relationships, and other model elements.
     * Also resets the state of the drawing canvas.
     */
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

    /**
     * Handles the process of saving the current project to an XML file. The user is prompted to select a location
     * for saving the project. The project's diagrams, relationships, and other relevant information are written
     * to an XML file, with proper formatting and tags.
     *
     * @throws IOException if there is an error during the file writing process.
     */
    @FXML
    private void handleSaveProject()
    {

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
                isSaveable = true;
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Project", "Failed to save the project.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves a relationship to the XML file by writing its details (type, source, target, multiplicities, and coordinates).
     *
     * @param writer the BufferedWriter used to write to the file.
     * @param relationship the Relationship object to be saved.
     * @param type the type of relationship (e.g., "association", "aggregation").
     * @throws IOException if an error occurs during the writing process.
     */
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

    /**
     * Handles the "Open Project" action. This method allows the user to select an XML file
     * containing the project data. It then loads the class diagrams, interface diagrams, and relationships
     * from the file, clears the existing diagrams and relationships, and redraws the canvas.
     *
     */
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

    /**
     * Loads a class diagram from the given XML element representing a class diagram.
     * This includes loading the class name, coordinates, attributes, and methods.
     *
     * @param classElement The XML element representing a class diagram.
     * @return A ClassDiagram object populated with the data from the XML element.
     */
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

    /**
     * Loads an interface diagram from the given XML element representing an interface diagram.
     * This includes loading the interface name, coordinates, and methods.
     *
     * @param interfaceElement The XML element representing an interface diagram.
     * @return An InterfaceData object populated with the data from the XML element.
     */
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

    /**
     * Loads a relationship from the given XML element representing a relationship.
     * This method extracts various relationship properties, including type, source and target,
     * coordinates, and multiplicities, and creates a corresponding Relationship object.
     *
     * @param relationshipElement The XML element representing a relationship.
     * @return A Relationship object populated with the data from the XML element.
     */
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

    /**
     * Adds a relationship to the appropriate list based on its type (association, aggregation, etc.).
     *
     * @param relationship The Relationship object to add to the list.
     */
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

    /**
     * Displays an alert with the specified type, title, and message.
     *
     * @param alertType the type of alert (e.g., INFORMATION, ERROR)
     * @param title the title of the alert window
     * @param message the message to display in the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
            * Displays an error alert with the given title and message.
            *
            * @param title the title of the alert window
     * @param message the message to display in the alert
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the exit action of the application by showing a confirmation dialog.
     * If confirmed, the application will close. Otherwise, the exit is canceled.
     */
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

    /**
     * Handles the generation of code based on the class and interface diagrams.
     * The generated code is saved to a text file selected by the user.
     */
    @FXML
    private void handleGenerateCode()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Generated Code");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Map to hold class names for checking relationships
                Map<String, ClassDiagram> classMap = new HashMap<>();
                classDiagrams.forEach(diagram -> classMap.put(diagram.getName(), diagram));

                // Generate code for interfaces
                for (InterfaceData interfaceDiagram : interfaceDiagrams) {
                    StringBuilder interfaceCode = new StringBuilder();
                    interfaceCode.append("public interface ").append(interfaceDiagram.getName()).append(" {\n\n");

                    for (MethodData method : interfaceDiagram.getMethods()) {
                        interfaceCode.append("    ")
                                .append(mapAccessModifier(method.getAccessModifier()))
                                .append(" ")
                                .append(method.getReturnType())
                                .append(" ")
                                .append(method.getName());
                    }

                    interfaceCode.append("\n}\n\n");
                    writer.write(interfaceCode.toString());
                }

                for (ClassDiagram classDiagram : classDiagrams) {
                    StringBuilder classCode = new StringBuilder();
                    classCode.append("public class ").append(classDiagram.getName());

                    // Check for generalization (extends)
                    Optional<String> parentClass = generalizations.stream()
                            .filter(r -> r.getSourceClass() != null &&
                                    r.getSourceClass().equals(classDiagram) &&
                                    r.getTargetClass() != null)
                            .map(r -> r.getTargetClass().getName())
                            .findFirst();
                    if (parentClass.isPresent()) {
                        classCode.append(" extends ").append(parentClass.get());
                    }

                    // Check for realizations (implements)
                    List<String> implementedInterfaces = realizations.stream()
                            .filter(r -> r.getSourceClass() != null &&
                                    r.getSourceClass().equals(classDiagram) &&
                                    r.getTargetInterface() != null)
                            .map(r -> r.getTargetInterface().getName())
                            .collect(Collectors.toList());
                    if (!implementedInterfaces.isEmpty()) {
                        classCode.append(" implements ").append(String.join(", ", implementedInterfaces));
                    }

                    classCode.append(" {\n\n");

                    // Add attributes
                    for (AttributeData attribute : classDiagram.getAttributes()) {
                        classCode.append("    ")
                                .append(mapAccessModifier(attribute.getAccessModifier()))
                                .append(" ")
                                .append(attribute.getDataType())
                                .append(" ")
                                .append(attribute.getName())
                                .append("; // Attribute\n");
                    }
                    classCode.append("\n");

                    classCode.append("\n    public ").append(classDiagram.getName()).append("() {\n")
                            .append("        // Default constructor\n")
                            .append("    }\n\n");

                    // Add methods
                    for (MethodData method : classDiagram.getMethods()) {
                        classCode.append("    ")
                                .append(mapAccessModifier(method.getAccessModifier()))
                                .append(" ")
                                .append(method.getReturnType())
                                .append(" ")
                                .append(method.getName())
                                .append(" {\n")
                                .append("        // TODO: Add method implementation\n")
                                .append("    }\n\n");
                    }

                    // Add associations
                    for (AttributeData attribute : classDiagram.getAttributes()) {
                        if (classMap.containsKey(attribute.getDataType())) {
                            classCode.append("    // Association: ").append(classDiagram.getName())
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

    /**
     * Maps an access modifier to its corresponding Java keyword.
     *
     * @param accessModifier the access modifier (e.g., "+", "-", "#")
     * @return the corresponding Java access modifier keyword (e.g., "public", "private", "protected")
     */
    private String mapAccessModifier(String accessModifier) {
        switch (accessModifier) {
            case "-":
                return "private";
            case "#":
                return "protected";
            case "+":
                return "public";
            default:
                return ""; // Default to package-private
        }
    }

    /**
     * Exports the current diagram as an image (JPG or PNG).
     * Takes a snapshot of the drawing canvas, crops it, and saves it to a file.
     */
    @FXML
    private void handleExportDiagram() {
        try {
            Bounds drawnBounds = calculateDiagramBounds();
            if (drawnBounds == null) {
                showError("Export Failed", "No diagrams are drawn on the canvas.");
                return;
            }

            // Add a margin around the bounding box
            int margin = 50; // Adjust as needed
            int x = Math.max((int) drawnBounds.getMinX() - margin, 0);
            int y = Math.max((int) drawnBounds.getMinY() - margin, 0);
            int width = Math.min((int) drawnBounds.getWidth() + 2 * margin, (int) drawingCanvas.getWidth() - x);
            int height = Math.min((int) drawnBounds.getHeight() + 2 * margin, (int) drawingCanvas.getHeight() - y);

            // Step 2: Take a snapshot of the canvas
            WritableImage fullSnapshot = drawingCanvas.snapshot(null, null);

            // Step 3: Crop the snapshot to the bounding box
            WritableImage croppedSnapshot = new WritableImage(width, height);
            PixelReader pixelReader = fullSnapshot.getPixelReader();
            PixelWriter pixelWriter = croppedSnapshot.getPixelWriter();

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    pixelWriter.setColor(i, j, pixelReader.getColor(x + i, y + j));
                }
            }

            // Step 4: Open a FileChooser for saving the image
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Diagram");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG Files", "*.jpg"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

            // Step 5: Show save dialog to get the destination file
            File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
            if (file != null) {
                // Step 6: Write the cropped snapshot to the file
                String extension = getFileExtension(file.getName());
                if (extension.equals("jpg") || extension.equals("png")) {
                    ImageIO.write(SwingFXUtils.fromFXImage(croppedSnapshot, null), extension, file);
                    System.out.println("Diagram exported successfully to: " + file.getAbsolutePath());
                    showAlert(Alert.AlertType.INFORMATION, "Export Diagram", "Diagram Exported Successfully.");
                } else {
                    showError("Invalid File Type", "Please save the file with .jpg or .png extension.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Export Failed", "An error occurred while exporting the diagram: " + e.getMessage());
        }
    }

    /**
     * Retrieves the file extension from a given file name.
     *
     * @param fileName the name of the file
     * @return the file extension (e.g., "jpg", "png")
     */
    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1).toLowerCase();
    }

    /**
     * Calculates the bounds of all drawn diagrams on the canvas.
     * If no diagrams are drawn, returns null.
     *
     * @return the bounds of the drawn diagrams or null if no diagrams exist
     */
    private Bounds calculateDiagramBounds() {
        if (classDiagrams.isEmpty() && interfaceDiagrams.isEmpty()) return null;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        // Calculate bounds for ClassDiagrams
        for (ClassDiagram diagram : classDiagrams) {
            double diagramMinX = diagram.getX();
            double diagramMinY = diagram.getY();
            double diagramMaxX = diagram.getX() + diagram.getWidth();
            double diagramMaxY = diagram.getY() + diagram.getHeight();

            minX = Math.min(minX, diagramMinX);
            minY = Math.min(minY, diagramMinY);
            maxX = Math.max(maxX, diagramMaxX);
            maxY = Math.max(maxY, diagramMaxY);
        }

        // Calculate bounds for InterfaceDiagrams
        for (InterfaceData diagram : interfaceDiagrams) {
            double diagramMinX = diagram.getX();
            double diagramMinY = diagram.getY();
            double diagramMaxX = diagram.getX() + diagram.getWidth();
            double diagramMaxY = diagram.getY() + diagram.getHeight();

            minX = Math.min(minX, diagramMinX);
            minY = Math.min(minY, diagramMinY);
            maxX = Math.max(maxX, diagramMaxX);
            maxY = Math.max(maxY, diagramMaxY);
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Handles the "About" action by showing information about the application.
     */
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
