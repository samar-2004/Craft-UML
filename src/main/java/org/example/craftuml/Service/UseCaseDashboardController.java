package org.example.craftuml.Service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
    private List<UseCase> useCases = new ArrayList<>();
    private List<Association> associations = new ArrayList<>();
    private Object draggedElement = null; // Keeps track of the current dragged element
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private List<UseCaseToUseCaseRelation> includeRelations = new ArrayList<>();
    private List<UseCaseToUseCaseRelation> extendRelations = new ArrayList<>();

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

    private void handleUseCaseDiagram() {
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
            drawActor(actor);
        }
        for (UseCase useCase : useCases)
        {
            drawUseCase(useCase);
        }
        for (Association association : associations)
        {
            drawAssociationLine(association.getActor(),association.getUseCase());
        }
        for (UseCaseToUseCaseRelation include : includeRelations){
            drawUseCaseRelation(include.getUseCase1(),include.getUseCase2(),include.getRelationType());
        }
        for (UseCaseToUseCaseRelation extend : extendRelations){
            drawUseCaseRelation(extend.getUseCase1(),extend.getUseCase2(),extend.getRelationType());
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
        TextInputDialog dialog = new TextInputDialog("Actor Name");
        dialog.setTitle("Add Actor");
        dialog.setHeaderText("Enter the Actor Name");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(actorName -> {
            // Check for duplicate actor name in the `actors` list
            boolean nameExists = actors.stream().anyMatch(actor -> actor.getName().equalsIgnoreCase(actorName));

            if (nameExists) {
                // Show error message
                showErrorMessage("An actor with this name already exists.");
            } else {
                // Create and add the actor
                Actor actor = new Actor(actorName);
                actors.add(actor);

                // Position calculation (modify as needed)
                double x = 50 + actors.size() * 50; // Example positioning logic
                double y = 100 + actors.size() * 30;

                actor.setX(x);
                actor.setY(y);

                redrawCanvas();

                enableNameEdit();
                enableDragging();
            }
        });
    }

    private void drawActor(Actor actor) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        // Adjust the size for the actor
        double headSize = 20;  // Smaller head size
        double bodyHeight = 25; // Shorter body
        double armLength = 20;  // Shorter arms
        double legLength = 30;  // Shorter legs

        // Draw the actor's head (outline of the circle, no fill)
        gc.setStroke(Color.BLACK);  // Set stroke color to black for the outline
        gc.setLineWidth(2);  // Optional: Adjust line width for a clearer outline
        gc.strokeOval(actor.getX(), actor.getY(), headSize, headSize);  // Head as an outline circle

        // Draw the actor's body (line)
        gc.strokeLine(actor.getX() + headSize / 2, actor.getY() + headSize, actor.getX() + headSize / 2, actor.getY() + headSize + bodyHeight); // Body

        // Draw the actor's arms (lines)
        gc.strokeLine(actor.getX(), actor.getY() + headSize + 10, actor.getX() + headSize, actor.getY() + headSize + 10); // Arms

        // Draw the actor's legs (lines)
        gc.strokeLine(actor.getX() + headSize / 2, actor.getY() + headSize + bodyHeight, actor.getX(), actor.getY() + headSize + bodyHeight + legLength); // Left leg
        gc.strokeLine(actor.getX() + headSize / 2, actor.getY() + headSize + bodyHeight, actor.getX() + headSize, actor.getY() + headSize + bodyHeight + legLength); // Right leg

        // Draw the actor's name below the figure
        gc.setFill(Color.BLACK);  // Fill color for the name text
        gc.fillText(actor.getName(), actor.getX() + 5, actor.getY() + headSize + bodyHeight + legLength + 10);  // Name below the actor
    }
//    private void enableActorDrag(Actor actor) {
//        // Set cursor to move when mouse hovers over the actor
//        drawingCanvas.setOnMouseMoved(event -> {
//            if (event.getX() >= actor.getX() && event.getX() <= actor.getX() + actor.getWidth() &&
//                    event.getY() >= actor.getY() && event.getY() <= actor.getY() + actor.getHeight()) {
//                drawingCanvas.setCursor(Cursor.MOVE);  // Change cursor to move symbol
//            } else {
//                drawingCanvas.setCursor(Cursor.DEFAULT);  // Reset cursor when not hovering over the actor
//            }
//        });
//
//        // Reset previous event handlers to avoid overwriting issues
//        drawingCanvas.setOnMousePressed(event -> {
//            // Check if the mouse is inside the actor area
//            if (event.getX() >= actor.getX() && event.getX() <= actor.getX() + actor.getWidth() &&
//                    event.getY() >= actor.getY() && event.getY() <= actor.getY() + actor.getHeight()) {
//                actor.setDragOffsetX(event.getX() - actor.getX()); // Store the offset during drag
//                actor.setDragOffsetY(event.getY() - actor.getY());
//            }
//        });
//
//        drawingCanvas.setOnMouseDragged(event -> {
//            if (actor.getDragOffsetX() != 0 && actor.getDragOffsetY() != 0) {
//                // Calculate new position of the actor based on mouse movement
//                double newX = event.getX() - actor.getDragOffsetX();
//                double newY = event.getY() - actor.getDragOffsetY();
//
//                // Update position with constraints if necessary
//                actor.setX(newX);
//                actor.setY(newY);
//
//                redrawCanvas();
//            }
//        });
//
//        drawingCanvas.setOnMouseReleased(event -> {
//            actor.setDragOffsetX(0);  // Reset drag offset when dragging stops
//            actor.setDragOffsetY(0);
//            drawingCanvas.setCursor(Cursor.DEFAULT);  // Reset cursor when drag ends
//        });
//    }

//    private void enableActorNameEdit() {
//        drawingCanvas.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 2) { // Detect double-click
//                double clickX = event.getX();
//                double clickY = event.getY();
//
//                // Use an array as a mutable wrapper for the clicked actor
//                final Actor[] clickedActorHolder = {null};
//
//                for (Actor actor : actors) {
//                    if (clickX >= actor.getX() && clickX <= actor.getX() + 30 &&
//                            clickY >= actor.getY() && clickY <= actor.getY() + 90) {
//                        clickedActorHolder[0] = actor;
//                        break;
//                    }
//                }
//
//                if (clickedActorHolder[0] != null) {
//                    // Show dialog to edit actor name
//                    TextInputDialog dialog = new TextInputDialog(clickedActorHolder[0].getName());
//                    dialog.setTitle("Edit Actor Name");
//                    dialog.setHeaderText("Edit the name of the selected actor");
//                    dialog.setContentText("Name:");
//
//                    Optional<String> result = dialog.showAndWait();
//                    result.ifPresent(newName -> {
//                        clickedActorHolder[0].setName(newName); // Update actor's name
//                        redrawCanvas(); // Refresh the canvas to show the updated name
//                    });
//                }
//            }
//        });
//    }

    @FXML
    private void handleAddUseCase() {
        TextInputDialog dialog = new TextInputDialog("UseCase Name");
        dialog.setTitle("Add Use Case");
        dialog.setHeaderText("Enter the Use Case Name");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(useCaseName -> {
            // Check for duplicate use case name in the `useCases` list
            boolean nameExists = useCases.stream().anyMatch(useCase -> useCase.getName().equalsIgnoreCase(useCaseName));

            if (nameExists) {
                // Show error message
                showErrorMessage("A use case with this name already exists.");
            } else {
                // Create and add the use case
                UseCase useCase = new UseCase(useCaseName);
                useCases.add(useCase);

                // Position calculation (modify as needed)
                double x = 50 + useCases.size() * 50; // Example positioning logic
                double y = 200 + useCases.size() * 30;

                useCase.setX(x);
                useCase.setY(y);

                redrawCanvas();

                enableNameEdit();
                enableDragging();
            }
        });
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
            for (Actor actor : actors) {
                if (event.getX() >= actor.getX() && event.getX() <= actor.getX() + actor.getWidth() &&
                        event.getY() >= actor.getY() && event.getY() <= actor.getY() + actor.getHeight()) {
                    drawingCanvas.setCursor(Cursor.MOVE); // Change cursor to move symbol
                    isHovering = true;
                    break;
                }
            }

            // Check if hovering over any use case (if not hovering over an actor)
            if (!isHovering) {
                for (UseCase useCase : useCases) {
                    if (event.getX() >= useCase.getX() && event.getX() <= useCase.getX() + useCase.getWidth() &&
                            event.getY() >= useCase.getY() && event.getY() <= useCase.getY() + useCase.getHeight()) {
                        drawingCanvas.setCursor(Cursor.MOVE); // Change cursor to move symbol
                        isHovering = true;
                        break;
                    }
                }
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
            for (Actor actor : actors) {
                if (event.getX() >= actor.getX() && event.getX() <= actor.getX() + actor.getWidth() &&
                        event.getY() >= actor.getY() && event.getY() <= actor.getY() + actor.getHeight()) {
                    draggedElement = actor;
                    dragOffsetX = event.getX() - actor.getX();
                    dragOffsetY = event.getY() - actor.getY();
                    break;
                }
            }

            // Check if a use case is clicked (if no actor was clicked)
            if (draggedElement == null) {
                for (UseCase useCase : useCases) {
                    if (event.getX() >= useCase.getX() && event.getX() <= useCase.getX() + useCase.getWidth() &&
                            event.getY() >= useCase.getY() && event.getY() <= useCase.getY() + useCase.getHeight()) {
                        draggedElement = useCase;
                        dragOffsetX = event.getX() - useCase.getX();
                        dragOffsetY = event.getY() - useCase.getY();
                        break;
                    }
                }
            }
        });

        // Mouse dragged: Update the position of the dragged element
        drawingCanvas.setOnMouseDragged(event -> {
            if (draggedElement != null) {
                double newX = event.getX() - dragOffsetX;
                double newY = event.getY() - dragOffsetY;

                // Check if the dragged element is a UseCase
                if (draggedElement instanceof UseCase) {
                    UseCase useCase = (UseCase) draggedElement;

                    // Assuming ActiveDiagram is a predefined container with its own boundaries
                    double activeDiagramX = activeDiagram.getX();  // ActiveDiagram's top-left X
                    double activeDiagramY = activeDiagram.getY();  // ActiveDiagram's top-left Y
                    double activeDiagramWidth = activeDiagram.getWidth();  // ActiveDiagram's width
                    double activeDiagramHeight = activeDiagram.getHeight();  // ActiveDiagram's height

                    // Clamp the newX and newY to ensure the use case stays within the bounds of the ActiveDiagram
                    double maxX = activeDiagramX + activeDiagramWidth - useCase.getWidth();  // Right boundary of ActiveDiagram
                    double minX = activeDiagramX;  // Left boundary of ActiveDiagram
                    double maxY = activeDiagramY + activeDiagramHeight - useCase.getHeight();  // Bottom boundary of ActiveDiagram
                    double minY = activeDiagramY;  // Top boundary of ActiveDiagram

                    // Apply the constraints for UseCase
                    newX = Math.max(minX, Math.min(newX, maxX));
                    newY = Math.max(minY, Math.min(newY, maxY));

                    // Update the position of the UseCase within the ActiveDiagram
                    useCase.setX(newX);
                    useCase.setY(newY);
                }

                // Check if the dragged element is an Actor
                else if (draggedElement instanceof Actor) {
                    Actor actor = (Actor) draggedElement;

                    // Clamp the position of the Actor to stay within the bounds of the whole drawingCanvas
                    double maxX = drawingCanvas.getWidth() - actor.getWidth();  // Right boundary of the canvas
                    double minX = 0;  // Left boundary of the canvas
                    double maxY = drawingCanvas.getHeight() - actor.getHeight();  // Bottom boundary of the canvas
                    double minY = 0;  // Top boundary of the canvas

                    // Apply the constraints for Actor
                    newX = Math.max(minX, Math.min(newX, maxX));
                    newY = Math.max(minY, Math.min(newY, maxY));

                    // Update the position of the Actor within the drawingCanvas
                    actor.setX(newX);
                    actor.setY(newY);
                }

                redrawCanvas();  // Refresh the canvas after updating the position
            }
        });


        // Mouse released: Stop dragging
        drawingCanvas.setOnMouseReleased(event -> {
            draggedElement = null;
        });
    }

//    private void enableUseCaseDrag(UseCase useCase) {
//        // Change cursor to move symbol when hovering over the use case
//        drawingCanvas.setOnMouseMoved(event -> {
//            if (event.getX() >= useCase.getX() && event.getX() <= useCase.getX() + useCase.getWidth() &&
//                    event.getY() >= useCase.getY() && event.getY() <= useCase.getY() + useCase.getHeight()) {
//                drawingCanvas.setCursor(Cursor.MOVE);  // Change cursor to move symbol
//            } else {
//                drawingCanvas.setCursor(Cursor.DEFAULT);  // Reset cursor when not hovering over the use case
//            }
//        });
//
//        // Reset previous event handlers to avoid overwriting issues
//        drawingCanvas.setOnMousePressed(event -> {
//            // Check if the mouse is inside the use case area
//            if (event.getX() >= useCase.getX() && event.getX() <= useCase.getX() + useCase.getWidth() &&
//                    event.getY() >= useCase.getY() && event.getY() <= useCase.getY() + useCase.getHeight()) {
//                useCase.setDragOffsetX(event.getX() - useCase.getX()); // Store the offset during drag
//                useCase.setDragOffsetY(event.getY() - useCase.getY());
//            }
//        });
//
//        drawingCanvas.setOnMouseDragged(event -> {
//            if (useCase.getDragOffsetX() != 0 && useCase.getDragOffsetY() != 0) {
//                // Calculate new position of the use case based on mouse movement
//                double newX = event.getX() - useCase.getDragOffsetX();
//                double newY = event.getY() - useCase.getDragOffsetY();
//
//                // Keep the use case inside the active diagram bounds
//                double minX = activeDiagram.getX() + 10;
//                double minY = activeDiagram.getY() + 10;
//                double maxX = activeDiagram.getX() + activeDiagram.getWidth() - useCase.getWidth() - 10;
//                double maxY = activeDiagram.getY() + activeDiagram.getHeight() - useCase.getHeight() - 10;
//
//                // Update position with constraints
//                useCase.setX(Math.max(minX, Math.min(newX, maxX)));
//                useCase.setY(Math.max(minY, Math.min(newY, maxY)));
//
//                redrawCanvas();
//            }
//        });
//
//        drawingCanvas.setOnMouseReleased(event -> {
//            useCase.setDragOffsetX(0);  // Reset drag offset when dragging stops
//            useCase.setDragOffsetY(0);
//            drawingCanvas.setCursor(Cursor.DEFAULT);  // Reset cursor when drag ends
//        });
//    }

//    private void enableUseCaseNameEdit() {
//        // Right-click context menu for Edit and Delete
//        drawingCanvas.setOnMousePressed(event -> {
//            if (event.isSecondaryButtonDown()) { // Right-click detection
//                double clickX = event.getX();
//                double clickY = event.getY();
//
//                final UseCase clickedUseCase = findClickedUseCase(clickX, clickY);
//
//                if (clickedUseCase != null) {
//                    // Create the context menu
//                    ContextMenu contextMenu = new ContextMenu();
//
//                    // Edit option
//                    MenuItem editItem = new MenuItem("Edit");
//                    editItem.setOnAction(e -> {
//                        TextInputDialog dialog = new TextInputDialog(clickedUseCase.getName());
//                        dialog.setTitle("Edit Use Case Name");
//                        dialog.setHeaderText("Edit the name of the selected use case");
//                        dialog.setContentText("Name:");
//
//                        Optional<String> result = dialog.showAndWait();
//                        result.ifPresent(newName -> {
//                            clickedUseCase.setName(newName);
//                            redrawCanvas();
//                        });
//                    });
//
//                    // Delete option
//                    MenuItem deleteItem = new MenuItem("Delete");
//                    deleteItem.setOnAction(e -> {
//                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                        alert.setTitle("Delete Use Case");
//                        alert.setHeaderText("Are you sure you want to delete this use case?");
//                        alert.setContentText("This action cannot be undone.");
//
//                        Optional<ButtonType> confirmation = alert.showAndWait();
//                        if (confirmation.isPresent() && confirmation.get() == ButtonType.OK) {
//                            deleteUseCase(clickedUseCase);
//                            redrawCanvas();
//                        }
//                    });
//
//                    contextMenu.getItems().addAll(editItem, deleteItem);
//                    contextMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
//
//                    event.consume(); // Prevent right-click from also being interpreted as double-click
//                }
//            }
//        });
//
//        // Handle double-click to edit name
//        drawingCanvas.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 2) { // Double-click detection
//                double clickX = event.getX();
//                double clickY = event.getY();
//
//                final UseCase clickedUseCase = findClickedUseCase(clickX, clickY);
//
//                if (clickedUseCase != null) {
//                    // Show dialog to edit use case name
//                    TextInputDialog dialog = new TextInputDialog(clickedUseCase.getName());
//                    dialog.setTitle("Edit Use Case Name");
//                    dialog.setHeaderText("Edit the name of the selected use case");
//                    dialog.setContentText("Name:");
//
//                    Optional<String> result = dialog.showAndWait();
//                    result.ifPresent(newName -> {
//                        clickedUseCase.setName(newName);
//                        redrawCanvas();
//                    });
//                }
//            }
//        });
//    }

    private void enableNameEdit() {
        drawingCanvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Detect double-click
                double clickX = event.getX();
                double clickY = event.getY();

                // Check if an actor was clicked
                Actor clickedActor = findClickedActor(clickX, clickY);
                if (clickedActor != null) {
                    editActorName(clickedActor);
                    return; // Exit to avoid checking use cases
                }

                // Check if a use case was clicked
                UseCase clickedUseCase = findClickedUseCase(clickX, clickY);
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
                Actor clickedActor = findClickedActor(clickX, clickY);
                if (clickedActor != null) {
                    showContextMenu(clickedActor, event.getScreenX(), event.getScreenY(), "actor");
                    event.consume(); // Prevent further processing of the right-click event
                    return; // Exit after finding the first clicked actor
                }

                // Check if a use case was clicked
                UseCase clickedUseCase = findClickedUseCase(clickX, clickY);
                if (clickedUseCase != null) {
                    showContextMenu(clickedUseCase, event.getScreenX(), event.getScreenY(), "useCase");
                    event.consume(); // Prevent further processing of the right-click event
                    return; // Exit after finding the first clicked use case
                }
            }
        });
    }

    private Actor findClickedActor(double x, double y) {
        for (Actor actor : actors) {
            if (x >= actor.getX() && x <= actor.getX() + actor.getWidth() &&
                    y >= actor.getY() && y <= actor.getY() + actor.getHeight()) {
                return actor;
            }
        }
        return null;
    }

    private UseCase findClickedUseCase(double x, double y) {
        for (UseCase useCase : useCases) {
            if (x >= useCase.getX() && x <= useCase.getX() + useCase.getWidth() &&
                    y >= useCase.getY() && y <= useCase.getY() + useCase.getHeight()) {
                return useCase;
            }
        }
        return null;
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

    private void editActorName(Actor actor) {
        TextInputDialog dialog = new TextInputDialog(actor.getName());
        dialog.setTitle("Edit Actor Name");
        dialog.setHeaderText("Edit the name of the selected actor");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            // Check for duplicate name, excluding the current actor
            boolean nameExists = actors.stream()
                    .anyMatch(existingActor -> existingActor.getName().equalsIgnoreCase(newName) && existingActor != actor);

            if (nameExists) {
                showErrorMessage("An actor with this name already exists.");
            } else {
                actor.setName(newName);
                redrawCanvas();
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
            // Check for duplicate name, excluding the current use case
            boolean nameExists = useCases.stream()
                    .anyMatch(existingUseCase -> existingUseCase.getName().equalsIgnoreCase(newName) && existingUseCase != useCase);

            if (nameExists) {
                showErrorMessage("A use case with this name already exists.");
            } else {
                useCase.setName(newName);
                redrawCanvas();
            }
        });
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


//    private UseCase findClickedUseCase(double clickX, double clickY) {
//        for (UseCase useCase : useCases) {
//            if (clickX >= useCase.getX() && clickX <= useCase.getX() + 150 &&
//                    clickY >= useCase.getY() && clickY <= useCase.getY() + 60) {
//                return useCase;
//            }
//        }
//        return null;
//    }

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

    private void drawUseCase(UseCase useCase) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        String name = useCase.getName();

        // Set font
        gc.setFont(new Font("Arial", 14));
        Text textHelper = new Text();
        textHelper.setFont(gc.getFont());

        double maxWidth = 100; // Maximum width for wrapping text

        // Split text into wrapped lines
        List<String> lines = new ArrayList<>();
        String[] words = name.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine + (currentLine.length() > 0 ? " " : "") + word;
            textHelper.setText(testLine);
            if (textHelper.getBoundsInLocal().getWidth() <= maxWidth) {
                currentLine.append((currentLine.length() > 0 ? " " : "")).append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        // Calculate dimensions
        double textHeight = textHelper.getBoundsInLocal().getHeight();
        double ovalWidth = Math.max(maxWidth + 50, lines.stream()
                .mapToDouble(line -> {
                    textHelper.setText(line);
                    return textHelper.getBoundsInLocal().getWidth();
                }).max().orElse(0) + 20); // Add padding
        double ovalHeight = Math.max(50, textHeight * lines.size() + 20); // Add padding

        // Draw the oval
        gc.setFill(Color.WHITE);
        gc.fillOval(useCase.getX(), useCase.getY(), ovalWidth, ovalHeight);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(useCase.getX(), useCase.getY(), ovalWidth, ovalHeight);

        // Center text in the oval
        gc.setFill(Color.BLACK);
        double centerX = useCase.getX() + ovalWidth / 2;
        double totalTextHeight = textHeight * lines.size();
        double centerY = useCase.getY() + (ovalHeight - totalTextHeight) / 2 + textHeight;

        // Draw each line of text
        for (int i = 0; i < lines.size(); i++) {
            textHelper.setText(lines.get(i));
            double lineWidth = textHelper.getBoundsInLocal().getWidth();
            double startX = centerX - lineWidth / 2;
            double startY = centerY + i * textHeight;  // Adjust vertical position for each line
            gc.fillText(lines.get(i), startX, startY);
        }
    }

    @FXML
    private void handleAddAssociation() {
        if (activeDiagram != null) {
            // Open a new window for selecting actor and use case
            openSelectionWindow();
        }
    }

    private void openSelectionWindow() {
        // Create a new Stage (window)
        Stage selectionStage = new Stage();
        selectionStage.setTitle("Select Actor and Use Case");

        selectionStage.setResizable(false);

        // Create ComboBoxes
        ComboBox<Actor> actorComboBox = new ComboBox<>();
        ComboBox<UseCase> useCaseComboBox = new ComboBox<>();

        // Populate the ComboBoxes
        ObservableList<Actor> observableActorList = FXCollections.observableArrayList(actors);
        ObservableList<UseCase> observableUseCaseList = FXCollections.observableArrayList(useCases);

        actorComboBox.setItems(observableActorList);
        useCaseComboBox.setItems(observableUseCaseList);

        actorComboBox.setPromptText("Select Actor");
        useCaseComboBox.setPromptText("Select Use Case");

        // Show only the name of Actor and UseCase (not the axis or other data)
        actorComboBox.setCellFactory(lv -> new ListCell<Actor>() {
            @Override
            protected void updateItem(Actor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName()); // Only show Actor name
                }
            }
        });

        useCaseComboBox.setCellFactory(lv -> new ListCell<UseCase>() {
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

        // Create a button for confirmation
        Button confirmButton = new Button("Confirm");
        confirmButton.setDisable(true); // Disable until both are selected

        // Enable the confirm button only when both are selected
        actorComboBox.setOnAction(event -> checkSelection(actorComboBox, useCaseComboBox, confirmButton));
        useCaseComboBox.setOnAction(event -> checkSelection(actorComboBox, useCaseComboBox, confirmButton));

        // Set the Confirm button action
        confirmButton.setOnAction(event -> {
            Actor selectedActor = actorComboBox.getValue();
            UseCase selectedUseCase = useCaseComboBox.getValue();

            // Ensure both values are selected
            if (selectedActor != null && selectedUseCase != null) {
                createAssociation(selectedUseCase, selectedActor);
                selectionStage.close(); // Close the window after confirming the selection
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
        // Enable the Confirm button only if both selections are made
        if (actorComboBox.getValue() != null && useCaseComboBox.getValue() != null) {
            confirmButton.setDisable(false);
        } else {
            confirmButton.setDisable(true);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createAssociation(UseCase useCase, Actor actor) {
        // Check if the actor is already associated with the use case
        if (!isUseCaseAssociated(useCase, actor)) {
            Association association = new Association(actor, useCase);

            associations.add(association);

            // Create the association (you may have a method for adding the actor to the use case's associations)
            useCase.addAssociation(actor);

            // Optionally, draw the association if needed
            drawAssociationLine(actor, useCase);
        } else {
            showAlert("Association Exists", "This actor is already associated with the selected use case.");
        }
    }

    private void drawAssociationLine(Actor actor, UseCase useCase) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        // Actor's center point
        double actorCenterX = actor.getX() + actor.getWidth() / 2;
        double actorCenterY = actor.getY() + actor.getHeight() / 2;

        // Use Case's center point
        double useCaseCenterX = useCase.getX() + useCase.getWidth() / 2;
        double useCaseCenterY = useCase.getY() + useCase.getHeight() / 2;

        // Calculate the nearest point on actor's boundary
        Point2D actorBoundaryPoint = getNearestBoundaryPoint(
                actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(),
                useCaseCenterX, useCaseCenterY
        );

        // Calculate the nearest point on use case's boundary
        Point2D useCaseBoundaryPoint = getNearestBoundaryPoint(
                useCase.getX(), useCase.getY(), useCase.getWidth(), useCase.getHeight(),
                actorCenterX, actorCenterY
        );

        // Draw the line between the calculated boundary points
        gc.strokeLine(actorBoundaryPoint.getX(), actorBoundaryPoint.getY(),
                useCaseBoundaryPoint.getX(), useCaseBoundaryPoint.getY());
    }

    /**
     * Calculates the nearest point on the rectangle's boundary to the given point.
     * @param rectX      Top-left X of the rectangle.
     * @param rectY      Top-left Y of the rectangle.
     * @param rectWidth  Width of the rectangle.
     * @param rectHeight Height of the rectangle.
     * @param pointX     X-coordinate of the external point.
     * @param pointY     Y-coordinate of the external point.
     * @return The nearest point on the rectangle's boundary.
     */
    private Point2D getNearestBoundaryPoint(double rectX, double rectY, double rectWidth, double rectHeight,
                                            double pointX, double pointY) {
        // Calculate the rectangle's center
        double centerX = rectX + rectWidth / 2;
        double centerY = rectY + rectHeight / 2;

        // Calculate the deltas
        double dx = pointX - centerX;
        double dy = pointY - centerY;

        // Determine which side of the rectangle to snap to
        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);

        if (absDx > absDy) {
            // Closer to the left or right side
            if (dx > 0) {
                // Right side
                return new Point2D(rectX + 1.5 * rectWidth, centerY + dy * (rectWidth / 2) / absDx);
            } else {
                // Left side
                return new Point2D(rectX, centerY + dy * (rectWidth / 2) / absDx);
            }
        } else {
            // Closer to the top or bottom side
            if (dy > 0) {
                // Bottom side
                return new Point2D(centerX + dx * (rectHeight / 2) / absDy, rectY + rectHeight);
            } else {
                // Top side
                return new Point2D(centerX + dx * (rectHeight / 2) / absDy, rectY);
            }
        }
    }

    private boolean isUseCaseAssociated(UseCase useCase, Actor actor) {
        // Check if the actor is already associated with the use case
        return useCase.getAssociations().contains(actor);
    }

    @FXML
    private void handleAddInclude() {
        if (activeDiagram != null) {
            openUseCaseSelectionWindow("include");
        }
    }
    @FXML
    private void handleAddExtend() {
        if (activeDiagram != null) {
            openUseCaseSelectionWindow("extend");
        }
    }

    public void addIncludeRelation(UseCase useCase1, UseCase useCase2) {
        includeRelations.add(new UseCaseToUseCaseRelation(useCase1, useCase2, "include"));
        drawUseCaseRelation(useCase1,useCase2,"include");
    }

    public void addExtendRelation(UseCase useCase1, UseCase useCase2) {
        extendRelations.add(new UseCaseToUseCaseRelation(useCase1, useCase2, "extend"));
        drawUseCaseRelation(useCase1,useCase2,"include");
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
                    if (hasExtendRelation(useCase1, useCase2)) {
                        showAlert("Error", "An <<extend>> relation already exists between these use cases.");
                        confirmButton.setDisable(true);
                    } else {
                        confirmButton.setDisable(false);
                    }
                } else if (relationType.equals("extend")) {
                    if (hasIncludeRelation(useCase1, useCase2)) {
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
        if (useCase1.equals(useCase2)) {
            showAlert("Error", "A use case cannot have a relation with itself.");
            return;
        }

        if (relationType.equals("include")) {
            addIncludeRelation(useCase1, useCase2);
        } else if (relationType.equals("extend")) {
            addExtendRelation(useCase1, useCase2);
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

    private void drawUseCaseRelation(UseCase useCase1, UseCase useCase2, String relationType) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        // Use case centers
        double useCase1CenterX = useCase1.getX() + useCase1.getWidth() / 2;
        double useCase1CenterY = useCase1.getY() + useCase1.getHeight() / 2;
        double useCase2CenterX = useCase2.getX() + useCase2.getWidth() / 2;
        double useCase2CenterY = useCase2.getY() + useCase2.getHeight() / 2;

        // Nearest boundary points
        Point2D start = getNearestBoundaryPoint(
                useCase1.getX(), useCase1.getY(), useCase1.getWidth(), useCase1.getHeight(),
                useCase2CenterX, useCase2CenterY
        );

        Point2D end = getNearestBoundaryPoint(
                useCase2.getX(), useCase2.getY(), useCase2.getWidth(), useCase2.getHeight(),
                useCase1CenterX, useCase1CenterY
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

}

