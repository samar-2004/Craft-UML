package org.example.craftuml.Service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.craftuml.Business.ActorService;
import org.example.craftuml.Business.AssociationService;
import org.example.craftuml.Business.UseCaseService;
import org.example.craftuml.Business.UseCaseRelationService;
import org.example.craftuml.models.UseCaseDiagrams.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UseCaseDashboardController {

    @FXML
    private Canvas drawingCanvas;

    @FXML
    private ListView<String> modelInfoList;

    private UseCaseDiagram activeDiagram;

    private double dragStartX = 0;
    private double dragStartY = 0;
    private UseCase activeUseCase;
    private boolean resizing = false;
    private double initialX, initialY;
    private double initialWidth;
    private double initialHeight;
    private List<Actor> actors = new ArrayList<>();
    private ActorService actorDAO = new ActorService(actors);
    private List<UseCase> useCases = new ArrayList<>();
    private UseCaseService useCaseDAO = new UseCaseService(useCases);
    private List<Association> associations = new ArrayList<>();
    private AssociationService associationDAO = new AssociationService(useCases,actors);
    private Object draggedElement = null; // Keeps track of the current dragged element
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private List<UseCaseToUseCaseRelation> includeRelations = new ArrayList<>();
    private List<UseCaseToUseCaseRelation> extendRelations = new ArrayList<>();
    private UseCaseRelationService useCaseRelationDAO = new UseCaseRelationService(includeRelations,extendRelations);

    private static final double RESIZE_MARGIN = 10;

    private Actor draggingActor = null; // Declare this at the class level
    private UseCase draggingUseCase = null;
    @FXML
    public void initialize() {
        initializeResizeHandlers();
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

    private void showContextMenu(MouseEvent event, String diagramType) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            if ("usecase".equals(diagramType)) {
                if (activeUseCase != null) {
                    // Add code for editing the UseCase
                }
            }
        });

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            if ("usecase".equals(diagramType)) {
                if (activeUseCase != null) {
                    // Delete the UseCase from the diagram using the UseCase object
                    activeDiagram.removeUseCase(activeUseCase);
                    redrawCanvas();
                }
            }
        });

        contextMenu.getItems().addAll(editItem, deleteItem);
        contextMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
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

                enableNameEdit();
                enableDragging();
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
                enableNameEdit();
                enableDragging();
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

    private void enableDragging() {
        // Mouse moved: Change cursor to move symbol when over a draggable element
        drawingCanvas.setOnMouseMoved(event -> {
            boolean isHovering = false;

            // Check if hovering over any actor
            if (ActorService.isHoveringOverActor(event.getX(), event.getY(), actors)) {
                drawingCanvas.setCursor(Cursor.MOVE); // Change cursor to move symbol
                isHovering = true;
            }

            // Check if hovering over any use case (if not hovering over an actor)
            if (!isHovering && UseCaseService.isHoveringOverUseCase(event.getX(), event.getY(), useCases)) {
                drawingCanvas.setCursor(Cursor.MOVE); // Change cursor to move symbol
                isHovering = true;
            }

            // Reset to default cursor if not hovering over any draggable element
            if (!isHovering) {
                drawingCanvas.setCursor(Cursor.DEFAULT);
            }
        });

        // Mouse pressed: Detect the element being dragged
        drawingCanvas.setOnMousePressed(event -> {
            draggedElement = null;
            dragOffsetX = 0;
            dragOffsetY = 0;

            // Check if an actor is clicked
            draggedElement = ActorService.getClickedActor(event.getX(), event.getY(), actors);
            if (draggedElement != null) {
                dragOffsetX = event.getX() - ((Actor) draggedElement).getX();
                dragOffsetY = event.getY() - ((Actor) draggedElement).getY();
                return;
            }

            // Check if a use case is clicked
            draggedElement = UseCaseService.getClickedUseCase(event.getX(), event.getY(), useCases);
            if (draggedElement != null) {
                dragOffsetX = event.getX() - ((UseCase) draggedElement).getX();
                dragOffsetY = event.getY() - ((UseCase) draggedElement).getY();
            }
        });

        // Mouse dragged: Update the position of the dragged element
        drawingCanvas.setOnMouseDragged(event -> {
            if (draggedElement != null) {
                if (draggedElement instanceof Actor) {
                    ActorService.updateActorPosition(
                            (Actor) draggedElement,
                            event.getX(),
                            event.getY(),
                            dragOffsetX,
                            dragOffsetY,
                            drawingCanvas.getWidth(),
                            drawingCanvas.getHeight()
                    );
                } else if (draggedElement instanceof UseCase) {
                    UseCaseService.updateUseCasePosition(
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

    private void enableNameEdit() {
        drawingCanvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Detect double-click
                double clickX = event.getX();
                double clickY = event.getY();

                // Check if an actor was clicked
                Actor clickedActor = actorDAO.findActorByPosition(clickX, clickY); // Delegating to ActorDAO
                if (clickedActor != null) {
                    editActorName(clickedActor);
                    return; // Exit to avoid checking use cases
                }

                // Delegate use case click logic to UseCaseDAO
                UseCase clickedUseCase = useCaseDAO.findClickedUseCase(clickX, clickY);
                if (clickedUseCase != null) {
                    editUseCaseName(clickedUseCase);
                }
            }
        });

        drawingCanvas.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) { // Detect right-click for context menu
                double clickX = event.getX();
                double clickY = event.getY();

                // Check if an actor was clicked
                Actor clickedActor = actorDAO.findActorByPosition(clickX, clickY); // Delegating to ActorDAO
                if (clickedActor != null) {
                    showContextMenu(clickedActor, event.getScreenX(), event.getScreenY(), "actor");
                    event.consume(); // Prevent further processing of the right-click event
                    return; // Exit after finding the first clicked actor
                }

                // Use UseCaseDAO to check if a use case was clicked
                UseCase clickedUseCase = useCaseDAO.findClickedUseCase(clickX, clickY);
                if (clickedUseCase != null) {
                    showContextMenu(clickedUseCase, event.getScreenX(), event.getScreenY(), "useCase");
                    event.consume();
                }
            }
        });
    }

    private void showContextMenu(Object element, double screenX, double screenY, String type) {
        ContextMenu contextMenu = new ContextMenu();

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
                actors.remove(element); // Remove actor from list
            } else if ("useCase".equals(type)) {
                useCases.remove(element); // Remove use case from list
            }
            redrawCanvas(); // Redraw the canvas after removal
        });

        contextMenu.getItems().addAll(editItem, deleteItem);
        contextMenu.show(drawingCanvas, screenX, screenY);
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

        ComboBox<Actor> actorComboBox = new ComboBox<>();
        ComboBox<UseCase> useCaseComboBox = new ComboBox<>();

        actorComboBox.setItems(FXCollections.observableArrayList(actors));
        useCaseComboBox.setItems(FXCollections.observableArrayList(useCases));

        actorComboBox.setPromptText("Select Actor");
        useCaseComboBox.setPromptText("Select Use Case");

        Button confirmButton = new Button("Confirm");
        confirmButton.setDisable(true);

        actorComboBox.setOnAction(event -> checkSelection(actorComboBox, useCaseComboBox, confirmButton));
        useCaseComboBox.setOnAction(event -> checkSelection(actorComboBox, useCaseComboBox, confirmButton));

        confirmButton.setOnAction(event -> {
            Actor selectedActor = actorComboBox.getValue();
            UseCase selectedUseCase = useCaseComboBox.getValue();
            if (selectedActor != null && selectedUseCase != null) {
                if (associationDAO.createAssociation(selectedUseCase, selectedActor, associations)) {
                    associationDAO.drawAssociationLine(selectedActor, selectedUseCase, drawingCanvas);
                } else {
                    showAlert("Association Exists", "This actor is already associated with the selected use case.");
                }
                selectionStage.close();
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

    private void checkSelection(ComboBox<Actor> actorComboBox, ComboBox<UseCase> useCaseComboBox, Button confirmButton) {
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
        ComboBox<UseCase> useCaseComboBox1 = new ComboBox<>();
        ComboBox<UseCase> useCaseComboBox2 = new ComboBox<>();

        // Populate the ComboBoxes with observable list
        ObservableList<UseCase> observableUseCaseList = FXCollections.observableArrayList(useCases);
        useCaseComboBox1.setItems(observableUseCaseList);
        useCaseComboBox2.setItems(observableUseCaseList);

        // Show only UseCase name (without actor or axis)
        useCaseComboBox1.setCellFactory(lv -> new ListCell<UseCase>() {
            @Override
            protected void updateItem(UseCase item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName()); // Only show UseCase name
                }
            }
        });

        useCaseComboBox2.setCellFactory(lv -> new ListCell<UseCase>() {
            @Override
            protected void updateItem(UseCase item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName()); // Only show UseCase name
                }
            }
        });

        useCaseComboBox1.setPromptText("Select First Use Case");
        useCaseComboBox2.setPromptText("Select Second Use Case");

        // Disable comboBox2's selected use case
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
            UseCase selectedUseCase1 = useCaseComboBox1.getValue();
            UseCase selectedUseCase2 = useCaseComboBox2.getValue();

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

    private void checkUseCaseSelection(ComboBox<UseCase> comboBox1, ComboBox<UseCase> comboBox2, Button confirmButton, String relationType) {
        UseCase useCase1 = comboBox1.getValue();
        UseCase useCase2 = comboBox2.getValue();

        if (useCase1 != null && useCase2 != null) {
            if (useCase1.equals(useCase2)) {
                showAlert("Error", "A use case cannot have a relation with itself.");
                confirmButton.setDisable(true);
            } else {
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

    private void updateComboBoxOptions(ComboBox<UseCase> useCaseComboBox1, ComboBox<UseCase> useCaseComboBox2) {
        UseCase selectedUseCase1 = useCaseComboBox1.getValue();
        UseCase selectedUseCase2 = useCaseComboBox2.getValue();

        ObservableList<UseCase> options = FXCollections.observableArrayList(useCases);
        options.remove(selectedUseCase1);
        options.remove(selectedUseCase2);

        useCaseComboBox2.setItems(options);
    }

    // New project creation logic (Placeholder method)
    public void handleNewProject() {
        System.out.println("New project created.");
        // Additional logic to create a new project
    }

    // Open an existing project (Placeholder method)
    public void handleOpenProject() {
        System.out.println("Opening project...");
        // Logic to open an existing project
    }

    // Save current project (Placeholder method)
    public void handleSaveProject() {
        System.out.println("Saving project...");
        // Logic to save the current project
    }

    // Exit the application (Placeholder method)
    public void handleExit() {
        System.out.println("Exiting application...");
        // Logic to exit the application
    }

    // Generate code (Placeholder method)
    public void handleGenerateCode() {
        System.out.println("Generating code...");
        // Logic to generate code for the diagram
    }

    // Export diagram (Placeholder method)
    public void handleExportDiagram() {
        System.out.println("Exporting diagram...");
        // Logic to export the diagram
    }

    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Craft UML");
        alert.setContentText("Craft UML - A tool for creating UML diagrams and generating code.");
        alert.showAndWait();
    }

    public void setActiveDiagram(UseCaseDiagram diagram) {
        this.activeDiagram = diagram;
    }

}

