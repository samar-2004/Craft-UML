package org.example.craftuml.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.craftuml.Business.ActorManager;
import org.example.craftuml.Business.AssociationManager;
import org.example.craftuml.Business.UseCaseManager;
import org.example.craftuml.Business.UseCaseRelationManager;
import org.example.craftuml.models.Section;
import org.example.craftuml.models.UseCaseDiagrams.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing the use case dashboard in the Craft UML application.
 * This class handles the user interface interactions for managing use case diagrams,
 * including actions like creating, editing, and displaying use case diagrams, as well as
 * exporting and handling file operations.
 * It is responsible for updating the view based on user actions and interactions.
 *
 * The class manages the state of the application related to use case diagrams and provides
 * methods for interacting with the user interface components like buttons, text fields,
 * and diagrams. It ensures that the dashboard is updated correctly based on the user's
 * selections and inputs.
 */

public class UseCaseDashboardController {

    /**
     * The canvas on which the use case diagram is drawn. This is where all elements
     * of the diagram, such as actors, use cases, and associations, are rendered.
     */
    @FXML
    private Canvas drawingCanvas;

    /**
     * A list view that displays information about the models (use cases, actors, etc.)
     * associated with the current use case diagram.
     */
    @FXML
    private ListView<String> modelInfoList;

    /**
     * A list containing the names of the models associated with the current use case diagram.
     * This list is observable, meaning any changes are reflected in the user interface.
     */
    private ObservableList<String> modelNames = FXCollections.observableArrayList();

    /**
     * A list containing the objects associated with the use case diagram.
     * This list is observable and can be updated dynamically.
     */
    private ObservableList<Object> modelObjects = FXCollections.observableArrayList();

    /**
     * The active use case diagram that the user is currently working with.
     */
    private UseCaseDiagram activeDiagram;

    /**
     * The current context menu that appears when interacting with elements on the diagram.
     */
    private ContextMenu currentContextMenu;

    /**
     * The starting X coordinate when a drag operation begins.
     */
    private double dragStartX = 0;

    /**
     * The starting Y coordinate when a drag operation begins.
     */
    private double dragStartY = 0;

    /**
     * A flag indicating whether an element is being resized.
     */
    private boolean resizing = false;

    /**
     * The initial width of an element before resizing.
     */
    private double initialWidth;

    /**
     * The initial height of an element before resizing.
     */
    private double initialHeight;

    /**
     * A flag indicating whether the current diagram is saveable.
     */
    private boolean isSaveable = false;

    /**
     * A list of actors associated with the current use case diagram.
     */
    private List<Actor> actors = new ArrayList<>();

    /**
     * The manager for the actors in the use case diagram.
     */
    private ActorManager actorManager = new ActorManager(actors);

    /**
     * A list of use cases associated with the current use case diagram.
     */
    private List<UseCase> useCases = new ArrayList<>();

    /**
     * The manager for the use cases in the use case diagram.
     */
    private UseCaseManager useCaseManager = new UseCaseManager(useCases);

    /**
     * A list of associations between use cases and actors in the use case diagram.
     */
    private List<Association> associations = new ArrayList<>();

    /**
     * The manager for the associations between use cases and actors.
     */
    private AssociationManager associationManager = new AssociationManager(useCases, actors);

    /**
     * The element currently being dragged on the canvas.
     */
    private Object draggedElement = null;

    /**
     * The X offset of the dragged element from its original position.
     */
    private double dragOffsetX = 0;

    /**
     * The Y offset of the dragged element from its original position.
     */
    private double dragOffsetY = 0;

    /**
     * A list of include relations between use cases.
     */
    private List<UseCaseToUseCaseRelation> includeRelations = new ArrayList<>();

    /**
     * A list of extend relations between use cases.
     */
    private List<UseCaseToUseCaseRelation> extendRelations = new ArrayList<>();

    /**
     * The manager for the relations between use cases, including include and extend relations.
     */
    private UseCaseRelationManager useCaseRelationManager = new UseCaseRelationManager(includeRelations, extendRelations);

    /**
     * The margin size for resizing elements on the canvas.
     * Elements are considered to be resized when the mouse is within this margin.
     */
    private static final double RESIZE_MARGIN = 10;

    /**
     * Initializes the dashboard by setting up resize handlers for the drawing canvas
     * and configuring the ListView for displaying model information. The method also
     * defines the cell factory for the ListView to customize the display of different
     * types of model items (actors, use cases, associations, etc.).
     * <p>
     * The method updates the ListView by grouping related items into sections (e.g., actors,
     * use cases, relationships) and applies different styles based on the type of item.
     * </p>
     */
    @FXML
    public void initialize() {
        initializeResizeHandlers();

        modelInfoList.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setStyle("");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index >= 0 && index < modelObjects.size()) {
                        Object modelItem = modelObjects.get(index);

                        if (modelItem instanceof Section) {
                            setText(item);
                            setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #f0f0f0;");
                        } else {
                            setText(item);

                            // Individual item styles
                            if (modelItem instanceof Actor) {
                                setStyle("-fx-font-weight: bold; -fx-background-color: #ffe6e6;");
                            } else if (modelItem instanceof UseCase) {
                                setStyle("-fx-font-weight: bold; -fx-background-color: #e6ffe6;");
                            } else if (modelItem instanceof Association) {
                                setStyle("-fx-font-style: italic ;  -fx-background-color: #f5f5dc;");
                            } else if (modelItem instanceof UseCaseToUseCaseRelation) {
                                setStyle("-fx-font-style: italic ; -fx-background-color: #d9e6ff;");
                            }
                        }
                    } else {
                        setText(null);
                        setGraphic(null);
                    }
                }
            }
        });
        updateListView();
    }

    /**
     * Updates the ListView by clearing existing data and adding sections with related items.
     * Sections include actors, use cases, and relationships. Each section is a collection
     * of model items grouped together by type (e.g., actors, use cases, associations).
     * <p>
     * The method updates the ListView with sections, where each section is represented
     * by a title followed by the corresponding items (actors, use cases, relationships).
     * </p>
     */
    private void updateListView() {
        modelNames.clear();
        modelObjects.clear();


        // Create sections for each type of item
        List<Section> sections = new ArrayList<>();

        // ACTORS section
        if (!actors.isEmpty()) {
            List<Object> actorItems = new ArrayList<>(actors);
            sections.add(new Section("ACTORS", actorItems));
        }

        // USE CASES section
        if (!useCases.isEmpty()) {
            List<Object> useCaseItems = new ArrayList<>(useCases);
            sections.add(new Section("USE CASES", useCaseItems));
        }

        // RELATIONSHIPS section
        List<Object> relationshipItems = new ArrayList<>();
        relationshipItems.addAll(associations);
        relationshipItems.addAll(includeRelations);
        relationshipItems.addAll(extendRelations);
        if (!relationshipItems.isEmpty()) {
            sections.add(new Section("RELATIONSHIPS", relationshipItems));
        }

        // Add sections to modelObjects and modelNames
        String SPACE = " "; // Add padding between sections
        boolean firstSection = true;

        for (Section section : sections) {
            if (!firstSection) {
                modelNames.add(SPACE);
                modelObjects.add(null);
            }
            firstSection = false;

            // Add section title
            modelNames.add(section.getTitle());
            modelObjects.add(section);

            // Add items within the section
            for (Object item : section.getItems()) {
                if (item instanceof Actor) {
                    modelNames.add(((Actor) item).getName());
                } else if (item instanceof UseCase) {
                    modelNames.add(((UseCase) item).getName());
                } else if (item instanceof Association) {
                    modelNames.add(item.toString()); // Use the Association's `toString()` implementation
                } else if (item instanceof UseCaseToUseCaseRelation) {
                    UseCaseToUseCaseRelation relation = (UseCaseToUseCaseRelation) item;
                    String relationText = relation.getUseCase1().getName() +
                            " <<" + relation.getRelationType() + ">> " +
                            relation.getUseCase2().getName();
                    modelNames.add(relationText);
                } else {
                    modelNames.add("Unknown");
                }
                modelObjects.add(item);
            }
        }

        modelInfoList.setItems(FXCollections.observableList(modelNames));

        isSaveable = false;
    }

    /**
     * Initializes the resize handlers for the drawing canvas. This method sets up
     * event handlers for mouse events that allow the user to resize elements within
     * the canvas, including mouse moved, pressed, dragged, and released events.
     */

    private void initializeResizeHandlers() {
        drawingCanvas.setOnMouseMoved(event -> handleMouseMove(event));
        drawingCanvas.setOnMousePressed(event -> handleMousePressed(event));
        drawingCanvas.setOnMouseDragged(event -> handleMouseDragged(event));
        drawingCanvas.setOnMouseReleased(event -> handleMouseReleased(event));
        drawingCanvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && isMouseOverDiagramName(event.getX(), event.getY())) {
                // Double-click detected on diagram name
                handleEditDiagramName();
            }
            if (event.isSecondaryButtonDown() && isMouseOverDiagramName(event.getX(), event.getY())) {
                // Right-click detected on diagram name
                handleEditDiagramName();
            }
        });
    }

    /**
     * Checks if the mouse is currently over the diagram name area in the drawing canvas.
     * The area is determined based on the X and Y coordinates of the mouse and the
     * position of the diagram name.
     *
     * @param mouseX The X-coordinate of the mouse pointer.
     * @param mouseY The Y-coordinate of the mouse pointer.
     * @return True if the mouse is over the diagram name area, otherwise false.
     */
    private boolean isMouseOverDiagramName(double mouseX, double mouseY) {
        // Check if the mouse is over the diagram name area
        return mouseX >= activeDiagram.getX() && mouseX <= activeDiagram.getX() + activeDiagram.getWidth() &&
                mouseY >= activeDiagram.getY() && mouseY <= activeDiagram.getY() + 20;  // Adjust 20 for name area height
    }

    /**
     * Opens a dialog to allow the user to edit the name of the active diagram.
     * The dialog contains a text field pre-filled with the current diagram name.
     * The user can modify the name and click "Update" to apply the changes.
     * <p>
     * If the user cancels the operation or leaves the text field empty, no changes
     * are made to the diagram name. After updating the name, the canvas is redrawn
     * to reflect the change.
     * </p>
     */
    private void handleEditDiagramName() {
        if (activeDiagram == null) return;

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Use Case Diagram Name");
        dialog.setHeaderText("Edit Diagram Name: ");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event -> event.consume());
        stage.initStyle(StageStyle.UTILITY);

        TextField textField = new TextField(activeDiagram.getName());
        textField.setPromptText("Enter New Diagram Name");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        Node updateButton = dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.setDisable(true);

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(textField);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return textField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            // Update the diagram's name
            activeDiagram.setName(newName);

            // Redraw the canvas with the updated diagram name
            redrawCanvas();
        });
    }

    /**
     * Handles mouse move events on the drawing canvas. Changes the cursor to a
     * resize cursor when the mouse is near the border of the active diagram.
     * <p>
     * The cursor will change to the `SE_RESIZE` type when hovering near the border
     * of the diagram to indicate that resizing is possible. The default cursor is used
     * when the mouse is not near the border.
     * </p>
     *
     * @param event The mouse event triggered when the mouse is moved.
     */
    private void handleMouseMove(MouseEvent event) {
        if (activeDiagram == null) return;

        double mouseX = event.getX();
        double mouseY = event.getY();

        if (isNearBorder(mouseX, mouseY)) {
            drawingCanvas.setCursor(Cursor.SE_RESIZE);  // Change the cursor to resize when near border
        } else {
            drawingCanvas.setCursor(Cursor.DEFAULT);  // Default cursor when not near border
        }
    }

    /**
     * Handles mouse press events on the drawing canvas. Begins the resizing process
     * when the mouse is pressed near the border of the active diagram.
     * <p>
     * This method detects if the mouse press is near the border of the active diagram,
     * and if so, it initializes the resizing process by storing the current mouse position
     * and the initial dimensions of the diagram.
     * </p>
     *
     * @param event The mouse event triggered when the mouse is pressed.
     */
    private void handleMousePressed(MouseEvent event) {
        if (activeDiagram == null) return;

        double mouseX = event.getX();
        double mouseY = event.getY();

        // If the mouse is on the border, start resizing
        if (isNearBorder(mouseX, mouseY)) {
            resizing = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            initialWidth = activeDiagram.getWidth();
            initialHeight = activeDiagram.getHeight();
        }
    }

    /**
     * Handles mouse drag events on the drawing canvas. Resizes the active diagram
     * as the mouse is dragged when resizing is in progress.
     * <p>
     * This method calculates the difference between the current mouse position and the
     * starting position when the resizing began. The active diagram's width and height
     * are then adjusted accordingly.
     * </p>
     *
     * @param event The mouse event triggered when the mouse is dragged.
     */
    private void handleMouseDragged(MouseEvent event) {
        if (resizing && activeDiagram != null)
        {
            double deltaX = event.getX() - dragStartX;
            double deltaY = event.getY() - dragStartY;

            activeDiagram.setWidth(initialWidth + deltaX);
            activeDiagram.setHeight(initialHeight + deltaY);

            redrawCanvas();
        }
    }

    /**
     * Handles mouse release events on the drawing canvas. Ends the resizing process.
     * <p>
     * This method is called when the mouse button is released, indicating the end
     * of the resizing operation. No further actions are taken in this method.
     * </p>
     *
     * @param event The mouse event triggered when the mouse button is released.
     */
    private void handleMouseReleased(MouseEvent event) {
    }

    /**
     * Determines if the mouse cursor is near the border of the active diagram.
     * <p>
     * This method checks if the mouse coordinates are within a defined margin from the
     * bottom-right corner of the active diagram. This is used to detect when the user
     * is hovering near the corner of the diagram for resizing purposes.
     * </p>
     *
     * @param mouseX The X coordinate of the mouse pointer.
     * @param mouseY The Y coordinate of the mouse pointer.
     * @return true if the mouse is within the resize margin of the diagram's bottom-right corner, false otherwise.
     */
    private boolean isNearBorder(double mouseX, double mouseY) {
        return mouseX >= activeDiagram.getX() + activeDiagram.getWidth() - RESIZE_MARGIN
                && mouseX <= activeDiagram.getX() + activeDiagram.getWidth() + RESIZE_MARGIN
                && mouseY >= activeDiagram.getY() + activeDiagram.getHeight() - RESIZE_MARGIN
                && mouseY <= activeDiagram.getY() + activeDiagram.getHeight() + RESIZE_MARGIN;
    }

    /**
     * Handles the action of adding a new use case diagram by displaying a confirmation
     * dialog if there is an active diagram. The user is prompted to confirm if they
     * want to create a new diagram, which would delete the existing one.
     * <p>
     * If confirmed, a new use case diagram is created, and the existing diagram is discarded.
     * </p>
     *
     * @param actionEvent The action event triggered when the "Add Use Case Diagram" button is clicked.
     */

    @FXML
    public void handleAddUseCaseDiagram(ActionEvent actionEvent) {
        if (activeDiagram != null) {
            // Show an attention dialog when there is already an active diagram
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Create New Diagram");
            alert.setHeaderText("Are you sure you want to create a new diagram?");
            alert.setContentText("This will delete the existing diagram and revert all changes.");

            // Add button options for Yes and No
            ButtonType yesButton = new ButtonType("Yes");
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                // User confirmed, proceed to create a new diagram
                handleUseCaseDiagram();
            } else {
                // User canceled, do nothing
                return;
            }
        } else {
            // No active diagram, proceed with creating a new one
            handleUseCaseDiagram();
        }
    }

    /**
     * Displays a dialog for creating a new use case diagram by allowing the user
     * to enter a name for the diagram. The new diagram is then created and positioned
     * at the top-center of the canvas.
     * <p>
     * If the user cancels or does not enter a name, no new diagram is created. Upon
     * successful creation, the canvas is cleared and the new diagram is drawn.
     * </p>
     */
    public void handleUseCaseDiagram() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Use Case Diagram");
        dialog.setHeaderText("Enter Diagram Name: ");

        // Set window properties for a more polished look
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event -> event.consume());
        stage.initStyle(StageStyle.UTILITY);

        // Create and style the text field
        TextField textField = new TextField();
        textField.setPromptText("Enter Diagram Name");
        textField.setStyle("-fx-font-size: 14px; -fx-padding: 5; -fx-background-color: #f4f4f4; -fx-border-radius: 5px; -fx-border-color: #ccc;");

        // Create and style the buttons
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Style dialog pane
        dialog.getDialogPane().setStyle("-fx-background-color: #ffffff; -fx-border-radius: 10; -fx-padding: 15;");

        // Style the "Create" button
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5px; -fx-pref-width: 80px; -fx-padding: 5px;");

        // Style the "Cancel" button
        Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-font-size: 14px; -fx-border-radius: 5px;-fx-border-color: #cccccc; -fx-pref-width: 80px; -fx-padding: 5px;");

// Add hover effect for the cancel button
        cancelButton.setOnMouseEntered(event -> {
            cancelButton.setStyle("-fx-background-color: #f0f0f0; -fx-font-size: 14px; -fx-border-radius: 5px; -fx-border-color: #aaaaaa; -fx-pref-width: 80px; -fx-padding: 5px;");
        });

        cancelButton.setOnMouseExited(event -> {
            cancelButton.setStyle("-fx-background-color: #ffffff; -fx-font-size: 14px; -fx-border-radius: 5px; -fx-border-color: #cccccc; -fx-pref-width: 80px; -fx-padding: 5px;");
        });


        // Disable "Create" button if text field is empty
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty());
        });

        // Set content for the dialog
        dialog.getDialogPane().setContent(textField);

        // Handle result when the dialog is confirmed
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return textField.getText();
            }
            return null;
        });

        // Show dialog and handle the result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(diagramName -> {
            // Create or replace the active diagram
            activeDiagram = new UseCaseDiagram();
            activeDiagram.setName(diagramName);

            // Set diagram position to top-center on the canvas
            double canvasWidth = drawingCanvas.getWidth();
            double canvasHeight = drawingCanvas.getHeight();
            double diagramWidth = activeDiagram.getWidth();
            double diagramHeight = activeDiagram.getHeight();

            // Center horizontally and align at the top
            activeDiagram.setX((canvasWidth - diagramWidth) / 2); // Center horizontally
            activeDiagram.setY(10); // Place at the top (y = 0)

            // Redraw the canvas with the new diagram
            redrawCanvasClearAll();
        });
    }

    /**
     * Clears the canvas and redraws the active diagram. Also clears the lists
     * of actors, use cases, and relationships.
     * <p>
     * This method is called when a new diagram is created, and it ensures that
     * the canvas is cleared and reset for the new diagram.
     * </p>
     */
    private void redrawCanvasClearAll() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight()); // Clear the canvas
        drawUseCaseDiagram(activeDiagram);
        actors.clear();
        useCases.clear();
        associations.clear();
        extendRelations.clear();
        includeRelations.clear();
    }

    /**
     * Clears the canvas and redraws the active diagram, actors, use cases, associations,
     * and relationships. This is used to update the canvas view.
     * <p>
     * It iterates through all relevant elements (actors, use cases, associations, and relations)
     * and draws them on the canvas to reflect the current state of the diagram.
     * </p>
     */
    private void redrawCanvas() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight()); // Clear the canvas
        drawUseCaseDiagram(activeDiagram);

        for (Actor actor:actors)
        {
            drawElement(actor);
        }
        for (UseCase useCase : useCases)
        {
            drawElement(useCase);
        }
        for (Association association : associations)
        {
            associationManager.drawAssociationLine(association.getActor(),association.getUseCase(),drawingCanvas);
        }
        for (UseCaseToUseCaseRelation include : includeRelations)
        {
            useCaseRelationManager.drawUseCaseRelation(include.getUseCase1(),include.getUseCase2(),include.getRelationType(),drawingCanvas.getGraphicsContext2D());
        }
        for (UseCaseToUseCaseRelation extend : extendRelations)
        {
            useCaseRelationManager.drawUseCaseRelation(extend.getUseCase1(),extend.getUseCase2(),extend.getRelationType(),drawingCanvas.getGraphicsContext2D());
        }
        updateListView();
        isSaveable = false;

    }

    /**
     * Draws the use case diagram on the canvas.
     * <p>
     * This method draws a rectangle representing the use case diagram, including the background, border,
     * and the diagram's name centered at the top. The rectangle's position and size are determined by
     * the properties of the provided UseCaseDiagram object.
     * </p>
     *
     * @param diagram The UseCaseDiagram object containing the diagram's properties (position, size, name) to be drawn.
     */
    private void drawUseCaseDiagram(UseCaseDiagram diagram) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE); // Background color for the rectangle
        gc.fillRect(diagram.getX(), diagram.getY(), diagram.getWidth(), diagram.getHeight());

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(diagram.getX(), diagram.getY(), diagram.getWidth(), diagram.getHeight());

        // Draw the name at the top center of the rectangle
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        double textX = diagram.getX() + diagram.getWidth() / 2; // Center horizontally within the diagram
        double textY = diagram.getY() + 10; // Slight offset from the top of the diagram
        gc.fillText(diagram.getName(), textX, textY);
    }

    /**
     * Handles the action of adding a new actor to the active use case diagram.
     * <p>
     * This method displays a dialog for the user to enter the actor's name, ensures the name follows
     * a naming convention (UpperCamelCase), and checks for duplicate actor names. If valid, the actor
     * is added to the diagram and the canvas is redrawn.
     * </p>
     *
     * @throws IllegalArgumentException if the actor name is invalid or if there is a duplicate name.
     */
    @FXML
    private void handleAddActor() {
        if (!isActiveDiagramSelected()) {
            // Show error message if no active diagram
            showErrorMessage("No active diagram. Please create a use case diagram first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Actor Name");
        dialog.setTitle("Add Actor");
        dialog.setHeaderText("Enter the Actor Name");
        dialog.setContentText("Name:");

        // Get the dialog's input field and "OK" button
        TextField inputField = dialog.getEditor();
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        // Initially disable the OK button
        okButton.setDisable(true);

        // Add a listener to the input field to enable/disable the OK button based on the naming convention
        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(!isUpperCamelCase(newValue)); // Enable only if valid
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(actorName -> {
            // Check for duplicate actor name using ActorDAO
            if (actorManager.isDuplicateActorName(actorName)) {
                // Show error message
                showErrorMessage("An actor with this name already exists.");
            } else {
                // Add the actor using ActorDAO
                actorManager.addActor(actorName);

                redrawCanvas();
                enableInteractivity();
            }
        });
    }

    /**
     * Edits the name of an existing actor in the diagram.
     * <p>
     * This method displays a dialog to edit the selected actor's name. It checks if the new name
     * is unique and updates the actor's name if valid. The canvas is then redrawn with the updated actor name.
     * </p>
     *
     * @param actor The actor whose name is to be edited.
     * @throws IllegalArgumentException if the new actor name is invalid or duplicates an existing name.
     */
    private void editActorName(Actor actor) {
        TextInputDialog dialog = new TextInputDialog(actor.getName());
        dialog.setTitle("Edit Actor Name");
        dialog.setHeaderText("Edit the name of the selected actor");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            // Check for duplicate name, excluding the current actor
            if (actorManager.isDuplicateNameExcludingActor(newName, actor)) { // Delegating to ActorDAO
                showErrorMessage("An actor with this name already exists.");
            } else {
                actorManager.updateActorName(actor, newName); // Delegating to ActorDAO
                redrawCanvas();
            }
        });
    }

    /**
     * Handles the action of adding a new use case to the active use case diagram.
     * <p>
     * This method displays a dialog for the user to enter the use case's name, checks if the name follows
     * a valid naming convention, and calculates the appropriate position for the new use case within the canvas.
     * If valid, the use case is added to the diagram and the canvas is redrawn.
     * </p>
     *
     * @throws IllegalArgumentException if the use case name is invalid or if there is a duplicate name.
     */
    @FXML
    private void handleAddUseCase() {
        if (!isActiveDiagramSelected()) {
            showErrorMessage("No active diagram. Please create a use case diagram first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("UseCase Name");
        dialog.setTitle("Add Use Case");
        dialog.setHeaderText("Enter the Use Case Name");
        dialog.setContentText("Name:");

        TextField inputField = dialog.getEditor();
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(!isUpperCamelCase(newValue));
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(useCaseName -> {
            try {
                // Calculate position within canvas bounds
                double x = 50 + (useCaseManager.getUseCases().size() % 5) * 120;
                double y = 50 + (useCaseManager.getUseCases().size() / 5) * 80;

                // Add the new use case
                UseCase newUseCase = useCaseManager.addUseCase(useCaseName, x, y,activeDiagram);

                // Redraw canvas and enable features
                redrawCanvas();
                enableInteractivity();

            } catch (IllegalArgumentException e) {
                showErrorMessage(e.getMessage());
            }
        });
    }

    /**
     * Edits the name of an existing use case in the active use case diagram.
     * <p>
     * This method displays a dialog to allow the user to edit the name of a selected use case. The name is updated
     * via the UseCaseManager. If an invalid name is provided, an error message is shown. After a successful update,
     * the canvas is redrawn to reflect the changes.
     * </p>
     *
     * @param useCase The UseCase object whose name is to be edited.
     * @throws IllegalArgumentException if the new name is invalid or any error occurs during the update process.
     */
    private void editUseCaseName(UseCase useCase) {
        TextInputDialog dialog = new TextInputDialog(useCase.getName());
        dialog.setTitle("Edit Use Case Name");
        dialog.setHeaderText("Edit the name of the selected use case");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            try {
                // Update name via DAO
                useCaseManager.editUseCaseName(useCase, newName);

                // Redraw canvas
                redrawCanvas();
            } catch (IllegalArgumentException e) {
                showErrorMessage(e.getMessage());
            }
        });
    }

    /**
     * Draws the specified element (Actor or UseCase) on the canvas.
     * <p>
     * This method checks the type of the provided element. If it is an instance of Actor, it delegates the drawing
     * to the ActorManager. If it is an instance of UseCase, it delegates the drawing to the UseCaseManager. If the element
     * is of any other type, an exception is thrown.
     * </p>
     *
     * @param element The element (either Actor or UseCase) to be drawn on the canvas.
     * @throws IllegalArgumentException if the element type is not supported.
     */
    private void drawElement(Object element) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        if (element instanceof Actor) {
            Actor actor = (Actor) element;
            actorManager.drawActor(gc, actor); // Delegate drawing to ActorDAO
        } else if (element instanceof UseCase) {
            UseCase useCase = (UseCase) element;
            useCaseManager.drawUseCase(gc, useCase); // Delegate drawing to UseCaseDAO
        } else {
            throw new IllegalArgumentException("Unsupported element type: " + element.getClass().getSimpleName());
        }
    }

    /**
     * Checks if a use case diagram is currently active.
     * <p>
     * This method determines if there is an active use case diagram. If the active diagram is null, it returns false.
     * Otherwise, it returns true, indicating that a use case diagram is active.
     * </p>
     *
     * @return true if a use case diagram is active, false otherwise.
     */
    private boolean isActiveDiagramSelected() {
        // Replace with your actual logic to check if a use case diagram is active
        return activeDiagram != null ;
    }

    /**
     * Checks if a given name follows the UpperCamelCase naming convention.
     * <p>
     * This method uses a regular expression to validate whether the provided name follows the UpperCamelCase convention.
     * The name must start with an uppercase letter, followed by lowercase letters or digits, and may have additional words
     * starting with an uppercase letter. Spaces between words are allowed but each word must follow the camel case format.
     * </p>
     *
     * @param name The name to be checked.
     * @return true if the name follows the UpperCamelCase convention, false otherwise.
     */
    private boolean isUpperCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String regex = "([A-Z][a-z0-9]*)+(\\s[A-Z][a-z0-9]*)*"; // UpperCamelCase regex
        return name.matches(regex);
    }

    /**
     * Enables interactivity on the canvas for drag-and-drop functionality and context menus.
     * <p>
     * This method listens for mouse events on the canvas, including mouse movement, pressing, dragging, and releasing.
     * It changes the cursor when hovering over draggable elements, allows elements to be dragged, and handles right-click
     * events to show a context menu for actors and use cases.
     * </p>
     */
    private void enableInteractivity() {
        // Mouse moved: Change cursor to move symbol when over a draggable element
        drawingCanvas.setOnMouseMoved(event -> {
            boolean isHovering = false;

            // Check if hovering over any actor
            if (ActorManager.isHoveringOverActor(event.getX(), event.getY(), actors)) {
                drawingCanvas.setCursor(Cursor.MOVE); // Change cursor to move symbol
                isHovering = true;
            }

            // Check if hovering over any use case (if not hovering over an actor)
            if (!isHovering && UseCaseManager.isHoveringOverUseCase(event.getX(), event.getY(), useCases)) {
                drawingCanvas.setCursor(Cursor.MOVE); // Change cursor to move symbol
                isHovering = true;
            }

            // Reset to default cursor if not hovering over any draggable element
            if (!isHovering) {
                drawingCanvas.setCursor(Cursor.DEFAULT);
            }
        });

        // Mouse pressed: Detect the element being dragged or right-clicked for context menu
        drawingCanvas.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                closeContextMenu(); // Right-click for context menu
                handleRightClick(event);
            } else { // Left-click or drag action
                closeContextMenu();
                handleMousePress(event);
            }
        });

        // Mouse dragged: Update the position of the dragged element
        drawingCanvas.setOnMouseDragged(event -> {
            if (draggedElement != null) {
                closeContextMenu();
                if (draggedElement instanceof Actor) {
                    ActorManager.updateActorPosition(
                            (Actor) draggedElement,
                            event.getX(),
                            event.getY(),
                            dragOffsetX,
                            dragOffsetY,
                            drawingCanvas.getWidth(),
                            drawingCanvas.getHeight()
                    );
                } else if (draggedElement instanceof UseCase) {
                    UseCaseManager.updateUseCasePosition(
                            (UseCase) draggedElement,
                            event.getX(),
                            event.getY(),
                            dragOffsetX,
                            dragOffsetY,
                            activeDiagram
                    );
                }
                redrawCanvas(); // Refresh the canvas after updating the position
            }
        });

        // Mouse released: Stop dragging
        drawingCanvas.setOnMouseReleased(event -> {
            draggedElement = null;
        });
    }

    /**
     * Handles the right-click event on the canvas to show the appropriate context menu.
     * <p>
     * This method checks if the user right-clicked on an actor or use case. If an actor is clicked, it shows an actor-specific
     * context menu. If a use case is clicked, it shows a use case-specific context menu.
     * </p>
     *
     * @param event The MouseEvent that triggered the right-click action.
     */
    private void handleRightClick(MouseEvent event) {
        double clickX = event.getX();
        double clickY = event.getY();

        // Check if an actor was clicked
        Actor clickedActor = actorManager.findActorByPosition(clickX, clickY);
        if (clickedActor != null) {
            showContextMenu(clickedActor, event.getScreenX(), event.getScreenY(), "actor");
            event.consume(); // Prevent further processing
            return;
        }

        // Use UseCaseDAO to check if a use case was clicked
        UseCase clickedUseCase = useCaseManager.findClickedUseCase(clickX, clickY);
        if (clickedUseCase != null) {
            showContextMenu(clickedUseCase, event.getScreenX(), event.getScreenY(), "useCase");
            event.consume(); // Prevent further processing
        }
    }

    /**
     * Handles the mouse press event on the canvas to detect which element is being clicked or dragged.
     * <p>
     * This method checks if the mouse was pressed on an actor or use case. If so, it initializes the dragging process by
     * calculating the offset between the mouse position and the element's position.
     * </p>
     *
     * @param event The MouseEvent that triggered the mouse press action.
     */
    private void handleMousePress(MouseEvent event) {
        draggedElement = null;
        dragOffsetX = 0;
        dragOffsetY = 0;

        // Check if an actor is clicked
        draggedElement = ActorManager.getClickedActor(event.getX(), event.getY(), actors);
        if (draggedElement != null) {
            dragOffsetX = event.getX() - ((Actor) draggedElement).getX();
            dragOffsetY = event.getY() - ((Actor) draggedElement).getY();
            return;
        }

        // Check if a use case is clicked
        draggedElement = UseCaseManager.getClickedUseCase(event.getX(), event.getY(), useCases);
        if (draggedElement != null) {
            dragOffsetX = event.getX() - ((UseCase) draggedElement).getX();
            dragOffsetY = event.getY() - ((UseCase) draggedElement).getY();
        }
    }

    /**
     * Displays a context menu for editing or deleting an actor or use case when right-clicked.
     * <p>
     * This method creates a context menu with "Edit" and "Delete" options. The "Edit" option opens a dialog to edit the
     * element's name, and the "Delete" option removes the element from the diagram and updates the canvas.
     * </p>
     *
     * @param element The element (actor or use case) for which the context menu is shown.
     * @param screenX The screen X-coordinate of the right-click event.
     * @param screenY The screen Y-coordinate of the right-click event.
     * @param type The type of the element ("actor" or "useCase").
     */
    private void showContextMenu(Object element, double screenX, double screenY, String type) {

        if (currentContextMenu != null) {
            currentContextMenu.hide(); // Hide the previous context menu
        }
        ContextMenu contextMenu = new ContextMenu();
        currentContextMenu = contextMenu;

        // Edit option
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            if ("actor".equals(type)) {
                editActorName((Actor) element);
            } else if ("useCase".equals(type)) {
                editUseCaseName((UseCase) element);
            }
        });

        // Delete option
        MenuItem deleteItem = new MenuItem("Delete");

        deleteItem.setOnAction(e -> {
            if ("actor".equals(type)) {
                Actor actorToDelete = (Actor) element;

                // Remove associations with other elements
                removeActorAssociations(actorToDelete);

                // Remove actor from the list
                actors.remove(actorToDelete);
            } else if ("useCase".equals(type)) {
                UseCase useCaseToDelete = (UseCase) element;

                // Remove associations (e.g., includes, extends, associations)
                removeUseCaseAssociations(useCaseToDelete);

                // Remove use case from the list
                useCases.remove(useCaseToDelete);
            }

            redrawCanvas(); // Redraw the canvas after removal
        });
        contextMenu.getItems().addAll(editItem, deleteItem);
        contextMenu.show(drawingCanvas, screenX, screenY);
    }

    /**
     * Removes all associations involving the specified actor from the diagram.
     * <p>
     * This method removes all associations (e.g., interactions) that involve the specified actor. It is called when
     * an actor is deleted from the diagram.
     * </p>
     *
     * @param actor The actor whose associations are to be removed.
     */
    private void removeActorAssociations(Actor actor) {
        // Remove all associations involving the actor
        associations.removeIf(association -> association.getActor().equals(actor));
    }

    /**
     * Removes all associations involving the specified use case from the diagram.
     * <p>
     * This method removes all associations (e.g., interactions, include, and extend relations) that involve the specified
     * use case. It is called when a use case is deleted from the diagram.
     * </p>
     *
     * @param useCase The use case whose associations are to be removed.
     */
    private void removeUseCaseAssociations(UseCase useCase) {
        // Remove all associations involving the use case
        associations.removeIf(association -> association.getUseCase().equals(useCase));

        // Remove include and extend relationships involving the use case
        includeRelations.removeIf(include ->
                include.getUseCase1().equals(useCase) || include.getUseCase2().equals(useCase)
        );

        extendRelations.removeIf(extend ->
                extend.getUseCase1().equals(useCase) || extend.getUseCase2().equals(useCase)
        );
    }

    /**
     * Closes the currently displayed context menu if one is visible.
     * <p>
     * This method hides any active context menu from the canvas.
     * </p>
     */
    private void closeContextMenu() {
        if (currentContextMenu != null) {
            currentContextMenu.hide();
        }
    }

    /**
     * Displays an error message in an alert dialog.
     * <p>
     * This method shows an alert with an error message, allowing the user to be informed of any issues that occur during
     * the execution of actions on the diagram.
     * </p>
     *
     * @param message The error message to be displayed.
     */
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Deletes a UseCase and removes any associated relations with actors or other use cases.
     * This method ensures that all associations involving the specified use case, including
     * relations with actors and use case-to-use case relationships (include and extend),
     * are removed before the use case is deleted from the list.
     *
     * @param useCase The UseCase to be deleted.
     */
    private void deleteUseCase(UseCase useCase) {
        useCases.remove(useCase);

        // Remove associations related to this use case
        associations.removeIf(association ->
                association.getUseCase() == useCase
        );

        // Remove use case-to-use case relations
        includeRelations.removeIf(relation ->
                relation.getUseCase1() == useCase || relation.getUseCase2() == useCase
        );

        // Remove use case-to-use case relations
        extendRelations.removeIf(relation ->
                relation.getUseCase1() == useCase || relation.getUseCase2() == useCase
        );
    }

    /**
     * Handles the addition of a new association between an actor and a use case.
     * This method checks if there are enough actors and use cases to form an association,
     * and if so, it opens a selection window for the user to choose the specific actor
     * and use case to associate. If not enough elements are available, an error message is displayed.
     */
    @FXML
    private void handleAddAssociation() {
        if (activeDiagram != null) {
            if (!useCases.isEmpty() && !actors.isEmpty()) {
                openSelectionWindow();
            } else {
                showErrorMessage("Not enough UseCases or Actors are present!");
            }
        } else {
            showErrorMessage("No active diagram. Please create a use case diagram first.");
        }
    }

    /**
     * Opens a window for the user to select an actor and a use case to associate.
     * This method creates a combo box for both actors and use cases, allowing the user
     * to select which elements to associate. Once confirmed, an association is created,
     * and the canvas is redrawn.
     */
    private void openSelectionWindow() {
        Stage selectionStage = new Stage();
        selectionStage.setTitle("Select Actor and Use Case");
        selectionStage.setResizable(false);

        ComboBox<String> actorComboBox = new ComboBox<>();
        ComboBox<String> useCaseComboBox = new ComboBox<>();



        // Populate combo boxes with names
        actorComboBox.setItems(FXCollections.observableArrayList(
                actors.stream().map(Actor::getName).toList()));
        useCaseComboBox.setItems(FXCollections.observableArrayList(
                useCases.stream().map(UseCase::getName).toList()));

        actorComboBox.setPromptText("Select Actor");
        useCaseComboBox.setPromptText("Select Use Case");

        Button confirmButton = new Button("Confirm");
        confirmButton.setDisable(true);

        actorComboBox.setOnAction(event -> checkSelection(actorComboBox, useCaseComboBox, confirmButton));
        useCaseComboBox.setOnAction(event -> checkSelection(actorComboBox, useCaseComboBox, confirmButton));

        confirmButton.setOnAction(event -> {
            String selectedActorName = actorComboBox.getValue();
            String selectedUseCaseName = useCaseComboBox.getValue();
            if (selectedActorName != null && selectedUseCaseName != null) {
                Actor selectedActor = actors.stream()
                        .filter(actor -> actor.getName().equals(selectedActorName))
                        .findFirst()
                        .orElse(null);

                UseCase selectedUseCase = useCases.stream()
                        .filter(useCase -> useCase.getName().equals(selectedUseCaseName))
                        .findFirst()
                        .orElse(null);

                if (selectedActor != null && selectedUseCase != null) {
                    if (associationManager.createAssociation(selectedUseCase, selectedActor, associations)) {
                        associationManager.drawAssociationLine(selectedActor, selectedUseCase, drawingCanvas);
                        redrawCanvas();
                    } else {
                        showAlert("Association Exists", "This actor is already associated with the selected use case.");
                    }
                    selectionStage.close();
                } else {
                    showAlert("Error", "Could not find selected actor or use case.");
                }
            } else {
                showAlert("Error", "Please select both an actor and a use case.");
            }
        });

        // Style the window and add padding and margins
        VBox vbox = new VBox(15, actorComboBox, useCaseComboBox, confirmButton);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f7f7f7; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 20; -fx-alignment: center;");

        // Styling the ComboBox and Button
        actorComboBox.setStyle("-fx-font-size: 14px; -fx-pref-width: 250px; -fx-background-color: #ffffff; -fx-border-radius: 5; -fx-border-color: #cccccc;");
        useCaseComboBox.setStyle("-fx-font-size: 14px; -fx-pref-width: 250px; -fx-background-color: #ffffff; -fx-border-radius: 5; -fx-border-color: #cccccc;");
        confirmButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 100px; -fx-border-radius: 5;");

        // Add hover effects to the confirm button
        confirmButton.setOnMouseEntered(e -> confirmButton.setStyle("-fx-font-size: 14px; -fx-background-color: #45a049; -fx-text-fill: white; -fx-pref-width: 100px; -fx-border-radius: 5;"));
        confirmButton.setOnMouseExited(e -> confirmButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 100px; -fx-border-radius: 5;"));

        // Set the window size to a good dimension
        Scene scene = new Scene(vbox, 350, 250); // Adjust window size
        selectionStage.setScene(scene);
        selectionStage.show();
    }

    /**
     * Helper method to enable or disable the confirm button in the selection window
     * based on whether both an actor and a use case are selected.
     *
     * @param actorComboBox The combo box containing the list of actors.
     * @param useCaseComboBox The combo box containing the list of use cases.
     * @param confirmButton The confirm button to be enabled/disabled.
     */
    private void checkSelection(ComboBox<String> actorComboBox, ComboBox<String> useCaseComboBox, Button confirmButton) {
        confirmButton.setDisable(actorComboBox.getValue() == null || useCaseComboBox.getValue() == null);
    }

    /**
     * Displays an error alert with a specified title and message.
     *
     * @param title The title of the alert.
     * @param message The content message of the alert.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the addition of an "include" relationship between use cases.
     * This method opens the use case selection window for users to select
     * use cases to create an "include" relationship between them.
     */
    @FXML
    private void handleAddInclude() {
        if (activeDiagram != null) {
            if (!useCases.isEmpty()) {
                openUseCaseSelectionWindow("include");
            }
        } else {
            showErrorMessage("No active diagram. Please create a use case diagram first.");
        }
    }

    /**
     * Handles the addition of an "extend" relationship between use cases.
     * This method opens the use case selection window for users to select
     * use cases to create an "extend" relationship between them.
     */
    @FXML
    private void handleAddExtend() {
        if (activeDiagram != null) {
            if (!useCases.isEmpty()) {
                openUseCaseSelectionWindow("extend");
            }
        } else {
            showErrorMessage("No active diagram. Please create a use case diagram first.");
        }
    }

    /**
     * Opens a window that allows the user to select two use cases and create a relation (either "include" or "extend").
     * The relation type is passed as an argument to differentiate between the two.
     *
     * @param relationType The type of relation to be created ("include" or "extend").
     */
    private void openUseCaseSelectionWindow(String relationType) {
        // Create a new Stage (window)
        Stage selectionStage = new Stage();
        selectionStage.setTitle("Select Use Cases for " + relationType);
        selectionStage.setResizable(false);

        // Create ComboBoxes for selecting Use Cases
        ComboBox<String> useCaseComboBox1 = new ComboBox<>();
        ComboBox<String> useCaseComboBox2 = new ComboBox<>();

        // Populate the ComboBoxes with use case names
        ObservableList<String> observableUseCaseNames = FXCollections.observableArrayList(
                useCases.stream().map(UseCase::getName).toList()
        );
        useCaseComboBox1.setItems(observableUseCaseNames);
        useCaseComboBox2.setItems(observableUseCaseNames);

        useCaseComboBox1.setPromptText("Select First Use Case");
        useCaseComboBox2.setPromptText("Select Second Use Case");

        // Disable the second ComboBox's selected use case
        useCaseComboBox1.setOnAction(event -> updateComboBoxOptions(useCaseComboBox1, useCaseComboBox2));
        useCaseComboBox2.setOnAction(event -> updateComboBoxOptions(useCaseComboBox1, useCaseComboBox2));

        // Create a button for confirmation
        Button confirmButton = new Button("Confirm");
        confirmButton.setDisable(true); // Disable until both are selected

        // Enable the confirm button only when both are selected
        useCaseComboBox1.setOnAction(event -> checkUseCaseSelection(useCaseComboBox1, useCaseComboBox2, confirmButton, relationType));
        useCaseComboBox2.setOnAction(event -> checkUseCaseSelection(useCaseComboBox1, useCaseComboBox2, confirmButton, relationType));

        // Set the Confirm button action
        confirmButton.setOnAction(event -> {
            String selectedUseCaseName1 = useCaseComboBox1.getValue();
            String selectedUseCaseName2 = useCaseComboBox2.getValue();

            // Map the selected names back to UseCase objects
            UseCase selectedUseCase1 = useCases.stream()
                    .filter(useCase -> useCase.getName().equals(selectedUseCaseName1))
                    .findFirst()
                    .orElse(null);

            UseCase selectedUseCase2 = useCases.stream()
                    .filter(useCase -> useCase.getName().equals(selectedUseCaseName2))
                    .findFirst()
                    .orElse(null);

            // Ensure both values are selected
            if (selectedUseCase1 != null && selectedUseCase2 != null) {
                createUseCaseRelation(selectedUseCase1, selectedUseCase2, relationType);
                selectionStage.close(); // Close the window after confirming the selection
            } else {
                showAlert("Error", "Please select both use cases.");
            }
        });

        // Style the window and add padding and margins
        VBox vbox = new VBox(15, useCaseComboBox1, useCaseComboBox2, confirmButton);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f7f7f7; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 20; -fx-alignment: center;");

        // Styling the ComboBox and Button
        useCaseComboBox1.setStyle("-fx-font-size: 14px; -fx-pref-width: 250px; -fx-background-color: #ffffff; -fx-border-radius: 5; -fx-border-color: #cccccc;");
        useCaseComboBox2.setStyle("-fx-font-size: 14px; -fx-pref-width: 250px; -fx-background-color: #ffffff; -fx-border-radius: 5; -fx-border-color: #cccccc;");
        confirmButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 100px; -fx-border-radius: 5;");

        // Add hover effects to the confirm button
        confirmButton.setOnMouseEntered(e -> confirmButton.setStyle("-fx-font-size: 14px; -fx-background-color: #45a049; -fx-text-fill: white; -fx-pref-width: 100px; -fx-border-radius: 5;"));
        confirmButton.setOnMouseExited(e -> confirmButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 100px; -fx-border-radius: 5;"));

        // Set the window size to a good dimension
        Scene scene = new Scene(vbox, 350, 250); // Adjust window size
        selectionStage.setScene(scene);
        selectionStage.show();
    }

    /**
     * Checks if both use cases are selected and whether they can have the specified relation type.
     * It disables the confirm button if any conditions are not met (e.g., the use cases are the same or incompatible relations exist).
     *
     * @param comboBox1 The first ComboBox for selecting a use case.
     * @param comboBox2 The second ComboBox for selecting a use case.
     * @param confirmButton The button that confirms the selection of the relation.
     * @param relationType The type of relation ("include" or "extend") to be established.
     */
    private void checkUseCaseSelection(ComboBox<String> comboBox1, ComboBox<String> comboBox2, Button confirmButton, String relationType) {
        String useCaseName1 = comboBox1.getValue();
        String useCaseName2 = comboBox2.getValue();

        if (useCaseName1 != null && useCaseName2 != null) {
            if (useCaseName1.equals(useCaseName2)) {
                showAlert("Error", "A use case cannot have a relation with itself.");
                confirmButton.setDisable(true);
            } else {
                UseCase useCase1 = useCases.stream()
                        .filter(uc -> uc.getName().equals(useCaseName1))
                        .findFirst()
                        .orElse(null);

                UseCase useCase2 = useCases.stream()
                        .filter(uc -> uc.getName().equals(useCaseName2))
                        .findFirst()
                        .orElse(null);

                if (relationType.equals("include")) {
                    if (useCaseRelationManager.hasExtendRelation(useCase1, useCase2)) {
                        showAlert("Error", "An <<extend>> relation already exists between these use cases.");
                        confirmButton.setDisable(true);
                    } else {
                        confirmButton.setDisable(false);
                    }
                } else if (relationType.equals("extend")) {
                    if (useCaseRelationManager.hasIncludeRelation(useCase1, useCase2)) {
                        showAlert("Error", "An <<include>> relation already exists between these use cases.");
                        confirmButton.setDisable(true);
                    } else {
                        confirmButton.setDisable(false);
                    }
                }
            }
        } else {
            confirmButton.setDisable(true);
        }
    }

    /**
     * Creates a relation between two selected use cases based on the relation type.
     * If the relation cannot be created, an error message is shown.
     *
     * @param useCase1 The first use case involved in the relation.
     * @param useCase2 The second use case involved in the relation.
     * @param relationType The type of relation to be created ("include" or "extend").
     */
    private void createUseCaseRelation(UseCase useCase1, UseCase useCase2, String relationType) {
        boolean success = useCaseRelationManager.createRelation(useCase1, useCase2, relationType);

        if (!success) {
            if (useCase1.equals(useCase2)) {
                showAlert("Error", "A use case cannot have a relation with itself.");
            } else {
                showAlert("Error", "Relation already exists or could not be created.");
            }
        }
        else {
            redrawCanvas();
        }
    }

    /**
     * Updates the options in the second ComboBox based on the selected items in both ComboBoxes.
     * Ensures that a selected use case from the first ComboBox is not available for selection in the second.
     *
     * @param useCaseComboBox1 The first ComboBox for selecting a use case.
     * @param useCaseComboBox2 The second ComboBox for selecting a use case.
     */
    private void updateComboBoxOptions(ComboBox<String> useCaseComboBox1, ComboBox<String> useCaseComboBox2) {
        String selectedUseCase1 = useCaseComboBox1.getValue();
        String selectedUseCase2 = useCaseComboBox2.getValue();

        // Create a new list of options excluding the selected items
        ObservableList<String> options = FXCollections.observableArrayList(
                useCases.stream().map(UseCase::getName).toList()
        );

        if (selectedUseCase1 != null) {
            options.remove(selectedUseCase1);
        }
        if (selectedUseCase2 != null) {
            options.remove(selectedUseCase2);
        }

        // Update the second ComboBox with the filtered options
        useCaseComboBox2.setItems(options);
    }

    /**
     * Handles the creation of a new project. If there are unsaved changes, the user is prompted for confirmation before proceeding.
     * If confirmed, the workspace is cleared to create a new project.
     */
    public void handleNewProject() {
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
     * Clears the workspace by resetting all diagram elements and variables to their default states.
     * This includes clearing the list of actors, use cases, associations, and relationships.
     * The canvas is also cleared, and any list items in the model info are removed.
     */
    private void clearWorkspace() {
        activeDiagram = null;

        actors.clear();
        useCases.clear();
        extendRelations.clear();
        associations.clear();
        includeRelations.clear();


        dragStartX = 0;
        dragStartY = 0;

        if (drawingCanvas != null) {
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        }

        if (modelInfoList != null) {
            modelInfoList.getItems().clear();
        }
    }

    /**
     * Opens a project by selecting an XML file, reading its content, and loading the use case diagram
     * with actors, use cases, associations, and relationships. If the file is successfully loaded,
     * the diagram, actors, use cases, and relationships are populated, and the canvas is redrawn.
     *
     * @FXML
     */
    @FXML
    public void handleOpenProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Use Case Diagram");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(file);

                // Clear previous data
                actors.clear();
                useCases.clear();
                associations.clear();
                includeRelations.clear();
                extendRelations.clear();

                // Load the diagram
                NodeList diagramNodes = doc.getElementsByTagName("UseCaseDiagram");
                if (diagramNodes.getLength() > 0) {
                    Element diagramElement = (Element) diagramNodes.item(0);
                    activeDiagram = new UseCaseDiagram();
                    activeDiagram.setName(diagramElement.getAttribute("name"));
                    activeDiagram.setX(Double.parseDouble(diagramElement.getAttribute("x")));
                    activeDiagram.setY(Double.parseDouble(diagramElement.getAttribute("y")));
                    activeDiagram.setWidth(Double.parseDouble(diagramElement.getAttribute("width")));
                    activeDiagram.setHeight(Double.parseDouble(diagramElement.getAttribute("height")));

                    System.out.println("Loaded diagram: " + activeDiagram.getName());
                }

                // Load actors
                NodeList actorNodes = doc.getElementsByTagName("Actor");
                for (int i = 0; i < actorNodes.getLength(); i++) {
                    Element actorElement = (Element) actorNodes.item(i);
                    Actor actor = new Actor(actorElement.getAttribute("name"));
                    actor.setX(Double.parseDouble(actorElement.getAttribute("x")));
                    actor.setY(Double.parseDouble(actorElement.getAttribute("y")));
                    actors.add(actor);
                    System.out.println("Loaded actor: " + actor.getName() + " at (" + actor.getX() + ", " + actor.getY() + ")");
                }

                // Load use cases
                NodeList useCaseNodes = doc.getElementsByTagName("UseCase");
                for (int i = 0; i < useCaseNodes.getLength(); i++) {
                    Element useCaseElement = (Element) useCaseNodes.item(i);
                    UseCase useCase = new UseCase(useCaseElement.getAttribute("name"));
                    useCase.setX(Double.parseDouble(useCaseElement.getAttribute("x")));
                    useCase.setY(Double.parseDouble(useCaseElement.getAttribute("y")));
                    useCases.add(useCase);
                    System.out.println("Loaded use case: " + useCase.getName() + " at (" + useCase.getX() + ", " + useCase.getY() + ")");
                }

                // Load associations
                NodeList associationNodes = doc.getElementsByTagName("Association");
                for (int i = 0; i < associationNodes.getLength(); i++) {
                    Element associationElement = (Element) associationNodes.item(i);
                    String actorName = associationElement.getAttribute("actor");
                    String useCaseName = associationElement.getAttribute("useCase");
                    Actor actor = findActorByName(actorName);
                    UseCase useCase = findUseCaseByName(useCaseName);
                    if (actor != null && useCase != null) {
                        associations.add(new Association(actor, useCase));
                        System.out.println("Loaded association: " + actor.getName() + " -> " + useCase.getName());
                    }
                }

                // Load include and extend relationships
                NodeList relationNodes = doc.getElementsByTagName("Relationships").item(0).getChildNodes();
                for (int i = 0; i < relationNodes.getLength(); i++) {
                    if (relationNodes.item(i) instanceof Element) {
                        Element relationElement = (Element) relationNodes.item(i);
                        String from = relationElement.getAttribute("from");
                        String to = relationElement.getAttribute("to");
                        UseCase fromUseCase = findUseCaseByName(from);
                        UseCase toUseCase = findUseCaseByName(to);
                        if (fromUseCase != null && toUseCase != null) {
                            if ("Include".equals(relationElement.getTagName())) {
                                includeRelations.add(new UseCaseToUseCaseRelation(fromUseCase, toUseCase, "include"));
                                System.out.println("Loaded include relation: " + from + " -> " + to);
                            } else if ("Extend".equals(relationElement.getTagName())) {
                                extendRelations.add(new UseCaseToUseCaseRelation(fromUseCase, toUseCase, "extend"));
                                System.out.println("Loaded extend relation: " + from + " -> " + to);
                            }
                        }
                    }
                }

                // Enable dragging for actors and use cases
                for (Actor actor : actors) {
//                    enableDragging();
                    enableInteractivity();
                }
                for (UseCase useCase : useCases) {
//                    enableDragging();
                    enableInteractivity();
                }

                // Redraw canvas (ensure it runs on JavaFX application thread)
                Platform.runLater(() -> redrawCanvas());

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Open Error");
                alert.setHeaderText("An error occurred while opening the file.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * Finds an actor by name from the list of actors.
     *
     * @param name The name of the actor to find.
     * @return The actor object if found, otherwise null.
     */
    private Actor findActorByName(String name) {
        for (Actor actor : actors) {
            if (actor.getName().equals(name)) {
                return actor;
            }
        }
        return null;
    }

    /**
     * Finds a use case by name from the list of use cases.
     *
     * @param name The name of the use case to find.
     * @return The use case object if found, otherwise null.
     */
    private UseCase findUseCaseByName(String name) {
        for (UseCase useCase : useCases) {
            if (useCase.getName().equals(name)) {
                return useCase;
            }
        }
        return null;
    }

    /**
     * Saves the current use case diagram and its elements (actors, use cases, associations, relationships)
     * to an XML file selected by the user.
     *
     * @FXML
     */
    @FXML
    public void handleSaveProject() {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Use Case Diagram");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
            File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
            if (file != null) {
                try {
                    // Create XML document
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.newDocument();
                    // Root element
                    Element rootElement = doc.createElement("UseCaseDiagram");
                    rootElement.setAttribute("name", activeDiagram.getName());
                    rootElement.setAttribute("x", String.valueOf(activeDiagram.getX()));
                    rootElement.setAttribute("y", String.valueOf(activeDiagram.getY()));
                    rootElement.setAttribute("width", String.valueOf(activeDiagram.getWidth()));
                    rootElement.setAttribute("height", String.valueOf(activeDiagram.getHeight()));
                    doc.appendChild(rootElement);
                    // Save actors
                    Element actorsElement = doc.createElement("Actors");
                    for (Actor actor : actors) {
                        Element actorElement = doc.createElement("Actor");
                        actorElement.setAttribute("name", actor.getName());
                        actorElement.setAttribute("x", String.valueOf(actor.getX()));
                        actorElement.setAttribute("y", String.valueOf(actor.getY()));
                        actorsElement.appendChild(actorElement);
                    }
                    rootElement.appendChild(actorsElement);
                    // Save use cases
                    Element useCasesElement = doc.createElement("UseCases");
                    for (UseCase useCase : useCases) {
                        Element useCaseElement = doc.createElement("UseCase");
                        useCaseElement.setAttribute("name", useCase.getName());
                        useCaseElement.setAttribute("x", String.valueOf(useCase.getX()));
                        useCaseElement.setAttribute("y", String.valueOf(useCase.getY()));
                        useCasesElement.appendChild(useCaseElement);
                    }
                    rootElement.appendChild(useCasesElement);
                    // Save associations
                    Element associationsElement = doc.createElement("Associations");
                    for (Association association : associations) {
                        Element associationElement = doc.createElement("Association");
                        associationElement.setAttribute("actor", association.getActor().getName());
                        associationElement.setAttribute("useCase", association.getUseCase().getName());
                        associationsElement.appendChild(associationElement);
                    }
                    rootElement.appendChild(associationsElement);
                    // Save include and extend relationships
                    Element relationsElement = doc.createElement("Relationships");
                    for (UseCaseToUseCaseRelation include : includeRelations) {
                        Element includeElement = doc.createElement("Include");
                        includeElement.setAttribute("from", include.getUseCase1().getName());
                        includeElement.setAttribute("to", include.getUseCase2().getName());
                        relationsElement.appendChild(includeElement);
                    }
                    for (UseCaseToUseCaseRelation extend : extendRelations) {
                        Element extendElement = doc.createElement("Extend");
                        extendElement.setAttribute("from", extend.getUseCase1().getName());
                        extendElement.setAttribute("to", extend.getUseCase2().getName());
                        relationsElement.appendChild(extendElement);
                    }
                    rootElement.appendChild(relationsElement);
                    // Write content to XML file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(file);
                    transformer.transform(source, result);
                    isSaveable = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Save Error");
                    alert.setHeaderText("An error occurred while saving the file.");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }

    }

    /**
     * Handles the exit functionality for the application by displaying a confirmation dialog.
     * If the user confirms, the application will exit, otherwise it will remain open.
     */
    public void handleExit() {
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
     * Handles the export of the diagram as an image file. The diagram is captured as a snapshot,
     * cropped to exclude transparent areas, and then saved to a file specified by the user.
     * The user can select from various image formats such as PNG, JPEG, or BMP.
     */
    @FXML
    public void handleExportDiagram() {
        WritableImage fullSnapshot = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
        drawingCanvas.snapshot(null, fullSnapshot);
        // Crop the snapshot to fit only the relevant content
        WritableImage croppedSnapshot = cropCanvasSnapshot(fullSnapshot);
        // Open a file chooser to save the cropped image
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Diagram");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Files", "*.jpg"),
                new FileChooser.ExtensionFilter("BMP Files", "*.bmp")
        );
        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                // Determine file format
                String fileExtension = getFileExtension(file.getName()).toLowerCase();
                if (!Arrays.asList("png", "jpg", "bmp").contains(fileExtension)) {
                    fileExtension = "png"; // Default to PNG if no valid extension is provided
                }
                // Save the cropped image
                ImageIO.write(SwingFXUtils.fromFXImage(croppedSnapshot, null), fileExtension, file);
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setHeaderText("An error occurred while exporting the diagram.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * Crops the snapshot of the canvas by identifying the non-transparent pixels and adjusting the boundaries.
     *
     * @param fullSnapshot The full snapshot of the canvas to be cropped.
     * @return A cropped version of the snapshot containing only the relevant content.
     */
    private WritableImage cropCanvasSnapshot(WritableImage fullSnapshot) {
        PixelReader pixelReader = fullSnapshot.getPixelReader();
        int minX = (int) fullSnapshot.getWidth();
        int minY = (int) fullSnapshot.getHeight();
        int maxX = 0;
        int maxY = 0;
        // Scan for non-empty pixels
        for (int x = 0; x < fullSnapshot.getWidth(); x++) {
            for (int y = 0; y < fullSnapshot.getHeight(); y++) {
                Color color = pixelReader.getColor(x, y);
                if (!color.equals(Color.TRANSPARENT)) { // Identify non-transparent pixels
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        // Ensure valid bounds for cropping
        if (minX >= maxX || minY >= maxY) {
            return fullSnapshot; // If no content is found, return the full snapshot
        }
        // Crop the snapshot
        int croppedWidth = maxX - minX + 1;
        int croppedHeight = maxY - minY + 1;
        WritableImage croppedImage = new WritableImage(croppedWidth, croppedHeight);
        PixelWriter pixelWriter = croppedImage.getPixelWriter();
        for (int x = 0; x < croppedWidth; x++) {
            for (int y = 0; y < croppedHeight; y++) {
                Color color = ((javafx.scene.image.PixelReader) pixelReader).getColor(minX + x, minY + y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return croppedImage;
    }

    /**
     * Retrieves the file extension from the given file name.
     *
     * @param fileName The name of the file.
     * @return The file extension as a string (e.g., "png", "jpg", "bmp").
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * Displays an error alert with the provided title and message.
     *
     * @param title The title of the error alert.
     * @param message The message to be displayed in the alert.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays information about the application in an "About" dialog.
     * The dialog includes the application's name, creators, version, and website.
     */
    public void handleAbout() {
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

    /**
     * Sets the currently active UseCaseDiagram.
     *
     * @param diagram The UseCaseDiagram to set as the active diagram.
     */
    public void setActiveDiagram(UseCaseDiagram diagram) {
        this.activeDiagram = diagram;
    }

}

