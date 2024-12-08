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

public class UseCaseDashboardController {

    @FXML
    private Canvas drawingCanvas;

    @FXML
    private ListView<String> modelInfoList;


    private ObservableList<String> modelNames = FXCollections.observableArrayList();
    private ObservableList<Object> modelObjects = FXCollections.observableArrayList();
    private UseCaseDiagram activeDiagram;
    private Actor activeActor;
    private UseCase activeUseCase;
    private ContextMenu currentContextMenu;

    private double dragStartX = 0;
    private double dragStartY = 0;
    private boolean resizing = false;
    private double initialX, initialY;
    private double initialWidth;
    private double initialHeight;
    private boolean isSaveable = false;
    private List<Actor> actors = new ArrayList<>();
    private ActorManager actorDAO = new ActorManager(actors);
    private List<UseCase> useCases = new ArrayList<>();
    private UseCaseManager useCaseDAO = new UseCaseManager(useCases);
    private List<Association> associations = new ArrayList<>();
    private AssociationManager associationDAO = new AssociationManager(useCases,actors);
    private Object draggedElement = null; // Keeps track of the current dragged element
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private List<UseCaseToUseCaseRelation> includeRelations = new ArrayList<>();
    private List<UseCaseToUseCaseRelation> extendRelations = new ArrayList<>();
    private UseCaseRelationManager useCaseRelationDAO = new UseCaseRelationManager(includeRelations,extendRelations);

    private static final double RESIZE_MARGIN = 10;

    private Actor draggingActor = null; // Declare this at the class level
    private UseCase draggingUseCase = null;

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

    private boolean isMouseOverDiagramName(double mouseX, double mouseY) {
        // Check if the mouse is over the diagram name area
        return mouseX >= activeDiagram.getX() && mouseX <= activeDiagram.getX() + activeDiagram.getWidth() &&
                mouseY >= activeDiagram.getY() && mouseY <= activeDiagram.getY() + 20;  // Adjust 20 for name area height
    }

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

    private void handleMouseReleased(MouseEvent event) {
    }

    private boolean isNearBorder(double mouseX, double mouseY) {
        return mouseX >= activeDiagram.getX() + activeDiagram.getWidth() - RESIZE_MARGIN
                && mouseX <= activeDiagram.getX() + activeDiagram.getWidth() + RESIZE_MARGIN
                && mouseY >= activeDiagram.getY() + activeDiagram.getHeight() - RESIZE_MARGIN
                && mouseY <= activeDiagram.getY() + activeDiagram.getHeight() + RESIZE_MARGIN;
    }


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
            associationDAO.drawAssociationLine(association.getActor(),association.getUseCase(),drawingCanvas);
        }
        for (UseCaseToUseCaseRelation include : includeRelations)
        {
            useCaseRelationDAO.drawUseCaseRelation(include.getUseCase1(),include.getUseCase2(),include.getRelationType(),drawingCanvas.getGraphicsContext2D());
        }
        for (UseCaseToUseCaseRelation extend : extendRelations)
        {
            useCaseRelationDAO.drawUseCaseRelation(extend.getUseCase1(),extend.getUseCase2(),extend.getRelationType(),drawingCanvas.getGraphicsContext2D());
        }
        updateListView();
        isSaveable = false;

    }

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
            if (actorDAO.isDuplicateActorName(actorName)) {
                // Show error message
                showErrorMessage("An actor with this name already exists.");
            } else {
                // Add the actor using ActorDAO
                actorDAO.addActor(actorName);

                redrawCanvas();
                enableInteractivity();
            }
        });
    }

    private void editActorName(Actor actor) {
        TextInputDialog dialog = new TextInputDialog(actor.getName());
        dialog.setTitle("Edit Actor Name");
        dialog.setHeaderText("Edit the name of the selected actor");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            // Check for duplicate name, excluding the current actor
            if (actorDAO.isDuplicateNameExcludingActor(newName, actor)) { // Delegating to ActorDAO
                showErrorMessage("An actor with this name already exists.");
            } else {
                actorDAO.updateActorName(actor, newName); // Delegating to ActorDAO
                redrawCanvas();
            }
        });
    }

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
                double x = 50 + (useCaseDAO.getUseCases().size() % 5) * 120;
                double y = 50 + (useCaseDAO.getUseCases().size() / 5) * 80;

                // Add the new use case
                UseCase newUseCase = useCaseDAO.addUseCase(useCaseName, x, y,activeDiagram);

                // Redraw canvas and enable features
                redrawCanvas();
                enableInteractivity();

            } catch (IllegalArgumentException e) {
                showErrorMessage(e.getMessage());
            }
        });
    }

    private void editUseCaseName(UseCase useCase) {
        TextInputDialog dialog = new TextInputDialog(useCase.getName());
        dialog.setTitle("Edit Use Case Name");
        dialog.setHeaderText("Edit the name of the selected use case");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            try {
                // Update name via DAO
                useCaseDAO.editUseCaseName(useCase, newName);

                // Redraw canvas
                redrawCanvas();
            } catch (IllegalArgumentException e) {
                showErrorMessage(e.getMessage());
            }
        });
    }

    private void drawElement(Object element) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        if (element instanceof Actor) {
            Actor actor = (Actor) element;
            actorDAO.drawActor(gc, actor); // Delegate drawing to ActorDAO
        } else if (element instanceof UseCase) {
            UseCase useCase = (UseCase) element;
            useCaseDAO.drawUseCase(gc, useCase); // Delegate drawing to UseCaseDAO
        } else {
            throw new IllegalArgumentException("Unsupported element type: " + element.getClass().getSimpleName());
        }
    }

    private boolean isActiveDiagramSelected() {
        // Replace with your actual logic to check if a use case diagram is active
        return activeDiagram != null ;
    }

    private boolean isUpperCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String regex = "([A-Z][a-z0-9]*)+(\\s[A-Z][a-z0-9]*)*"; // UpperCamelCase regex
        return name.matches(regex);
    }
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

    private void handleRightClick(MouseEvent event) {
        double clickX = event.getX();
        double clickY = event.getY();

        // Check if an actor was clicked
        Actor clickedActor = actorDAO.findActorByPosition(clickX, clickY);
        if (clickedActor != null) {
            showContextMenu(clickedActor, event.getScreenX(), event.getScreenY(), "actor");
            event.consume(); // Prevent further processing
            return;
        }

        // Use UseCaseDAO to check if a use case was clicked
        UseCase clickedUseCase = useCaseDAO.findClickedUseCase(clickX, clickY);
        if (clickedUseCase != null) {
            showContextMenu(clickedUseCase, event.getScreenX(), event.getScreenY(), "useCase");
            event.consume(); // Prevent further processing
        }
    }

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
    private void removeActorAssociations(Actor actor) {
        // Remove all associations involving the actor
        associations.removeIf(association -> association.getActor().equals(actor));
    }

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

    private void closeContextMenu() {
        if (currentContextMenu != null) {
            currentContextMenu.hide();
        }
    }


    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
                    if (associationDAO.createAssociation(selectedUseCase, selectedActor, associations)) {
                        associationDAO.drawAssociationLine(selectedActor, selectedUseCase, drawingCanvas);
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

    // Helper method to enable/disable confirm button based on selections
    private void checkSelection(ComboBox<String> actorComboBox, ComboBox<String> useCaseComboBox, Button confirmButton) {
        confirmButton.setDisable(actorComboBox.getValue() == null || useCaseComboBox.getValue() == null);
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
                    if (useCaseRelationDAO.hasExtendRelation(useCase1, useCase2)) {
                        showAlert("Error", "An <<extend>> relation already exists between these use cases.");
                        confirmButton.setDisable(true);
                    } else {
                        confirmButton.setDisable(false);
                    }
                } else if (relationType.equals("extend")) {
                    if (useCaseRelationDAO.hasIncludeRelation(useCase1, useCase2)) {
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


    private void createUseCaseRelation(UseCase useCase1, UseCase useCase2, String relationType) {
        boolean success = useCaseRelationDAO.createRelation(useCase1, useCase2, relationType);

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


    // New project creation logic (Placeholder method)
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

    private Actor findActorByName(String name) {
        for (Actor actor : actors) {
            if (actor.getName().equals(name)) {
                return actor;
            }
        }
        return null;
    }

    private UseCase findUseCaseByName(String name) {
        for (UseCase useCase : useCases) {
            if (useCase.getName().equals(name)) {
                return useCase;
            }
        }
        return null;
    }


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


    // Exit the application (Placeholder method)
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
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

    public void setActiveDiagram(UseCaseDiagram diagram) {
        this.activeDiagram = diagram;
    }

}

