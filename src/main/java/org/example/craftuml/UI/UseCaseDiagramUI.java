package org.example.craftuml.UI;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.craftuml.models.UseCaseDiagrams.Actor;
import org.example.craftuml.models.UseCaseDiagrams.UseCase;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseDiagram;

import java.util.ArrayList;
import java.util.List;

/**
 * The UseCaseDiagramUI class provides the user interface for creating and editing use case diagrams.
 * It allows users to define a diagram name, add actors and use cases, and preview the diagram.
 * It is also responsible for handling the visual representation of the diagram on a Canvas.
 */
public class UseCaseDiagramUI {

    /**
     * The current UseCaseDiagram being edited or displayed.
     * This object contains the data and properties of the use case diagram.
     */
    private UseCaseDiagram currentUseCaseDiagram;

    /**
     * The Canvas on which the use case diagram will be drawn.
     * This serves as the visual area where the diagram's elements are rendered.
     */
    private Canvas drawingCanvas;

    /**
     * The GraphicsContext associated with the drawingCanvas.
     * It provides the methods needed to draw shapes, text, and other graphical elements on the canvas.
     */
    private GraphicsContext gc;


    /**
     * Constructor that initializes the UseCaseDiagramUI with a Canvas.
     * This constructor sets up the default UseCaseDiagram and prepares the canvas for drawing.
     *
     * @param drawingCanvas The canvas to be used for drawing the diagram.
     * @throws IllegalArgumentException If the provided canvas is null.
     */
    public UseCaseDiagramUI(Canvas drawingCanvas) {
        if (drawingCanvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.drawingCanvas = drawingCanvas;
        this.gc = drawingCanvas.getGraphicsContext2D(); // Get the GraphicsContext for drawing
        this.currentUseCaseDiagram = new UseCaseDiagram();
    }

    /**
     * Constructor that initializes the UseCaseDiagramUI with both a Canvas and a UseCaseDiagram.
     * This constructor allows editing an existing UseCaseDiagram on the provided canvas.
     *
     * @param drawingCanvas The canvas to be used for drawing the diagram.
     * @param useCaseDiagram The UseCaseDiagram to be edited or displayed.
     * @throws IllegalArgumentException If the provided canvas is null.
     */
    public UseCaseDiagramUI(Canvas drawingCanvas, UseCaseDiagram useCaseDiagram) {
        if (drawingCanvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.drawingCanvas = drawingCanvas;
        this.gc = drawingCanvas.getGraphicsContext2D(); // Get the GraphicsContext for drawing
        this.currentUseCaseDiagram = useCaseDiagram;
    }

    /**
     * Constructor that initializes the UseCaseDiagramUI with a UseCaseDiagram only.
     * This constructor does not set up a canvas, assuming that the diagram is not displayed visually.
     *
     * @param useCaseDiagram The UseCaseDiagram to be used.
     */
    public UseCaseDiagramUI(UseCaseDiagram useCaseDiagram) {
        this.currentUseCaseDiagram = useCaseDiagram;
    }

    /**
     * Displays a dialog allowing the user to create or edit a UseCaseDiagram.
     * This method presents fields for the diagram name, actors, and use cases, and returns the updated diagram when the user clicks "OK".
     *
     * @return The UseCaseDiagram after the user inputs the required information.
     */
    public UseCaseDiagram showUseCaseDiagramDialog() {
        Stage inputStage = new Stage();
        inputStage.setTitle("Create or Edit Use Case Diagram");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        // Diagram name input
        Label diagramNameLabel = new Label("Diagram Name:");
        diagramNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        TextField diagramNameField = new TextField();
        diagramNameField.setPromptText("Enter Diagram Name");
        diagramNameField.setPrefWidth(300);

        inputStage.setOnCloseRequest(event -> {
            currentUseCaseDiagram = null;
            inputStage.close();
        });

        if (currentUseCaseDiagram.getName() != null) {
            diagramNameField.setText(currentUseCaseDiagram.getName());
        }

        diagramNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentUseCaseDiagram.setName(diagramNameField.getText());
        });

        // Actor fields setup
        Label actorsLabel = new Label("Actors:");
        actorsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        VBox actorsVBox = new VBox(10);
        actorsVBox.setPadding(new Insets(10));
        actorsVBox.setVisible(true);

        for (Actor actor : currentUseCaseDiagram.getActors()) {
            addActorField(actorsVBox, actor);
        }

        Button addActorButton = new Button("+");
        Button removeActorButton = new Button("-");

        addActorButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        removeActorButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        HBox actorButtons = new HBox(10, addActorButton, removeActorButton);

        addActorButton.setOnAction(e -> addActorField(actorsVBox, null));
        removeActorButton.setOnAction(e -> {
            if (!actorsVBox.getChildren().isEmpty()) {
                actorsVBox.getChildren().remove(actorsVBox.getChildren().size() - 1);
            }
        });

        // Use Case fields setup
        Label useCasesLabel = new Label("Use Cases:");
        useCasesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        VBox useCasesVBox = new VBox(10);
        useCasesVBox.setPadding(new Insets(10));
        useCasesVBox.setVisible(true);

        for (UseCase useCase : currentUseCaseDiagram.getUseCases()) {
            addUseCaseField(useCasesVBox, useCase);
        }

        Button addUseCaseButton = new Button("+");
        Button removeUseCaseButton = new Button("-");

        addUseCaseButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        removeUseCaseButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        HBox useCaseButtons = new HBox(10, addUseCaseButton, removeUseCaseButton);

        addUseCaseButton.setOnAction(e -> addUseCaseField(useCasesVBox, null));
        removeUseCaseButton.setOnAction(e -> {
            if (!useCasesVBox.getChildren().isEmpty()) {
                useCasesVBox.getChildren().remove(useCasesVBox.getChildren().size() - 1);
            }
        });

        // Button setup (OK & Cancel)
        HBox buttonBox = new HBox(10);
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        cancelButton.setOnAction(e -> {
            currentUseCaseDiagram = null;
            inputStage.close();
        });

        okButton.setStyle("-fx-background-color:#503774; -fx-text-fill:white;");
        cancelButton.setStyle("-fx-background-color:#503774; -fx-text-fill:white;");

        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> {
            String diagramName = diagramNameField.getText().trim();
            if (diagramName.isEmpty()) {
                showError("Diagram name cannot be empty.");
                return;
            }

            if (!areFieldsFilled(actorsVBox)) {
                showError("Please ensure all fields are filled for each actor.");
                return;
            }
            List<Actor> actors = gatherActors(actorsVBox);
            if (!areFieldsFilled(useCasesVBox)) {
                showError("Please ensure all fields are filled for each use case.");
                return;
            }
            List<UseCase> useCases = gatherUseCases(useCasesVBox);

            currentUseCaseDiagram.setName(diagramName);
            currentUseCaseDiagram.setActors(actors);
            currentUseCaseDiagram.setUseCases(useCases);

            inputStage.close();
        });

        buttonBox.getChildren().addAll(okButton, cancelButton);

        vbox.getChildren().addAll(
                diagramNameLabel,
                diagramNameField,
                actorsLabel,
                actorsVBox,
                actorButtons,
                useCasesLabel,
                useCasesVBox,
                useCaseButtons,
                buttonBox
        );

        // Create a Pane to hold the Canvas for drawing
        Pane diagramPane = new Pane();
        diagramPane.getChildren().add(drawingCanvas);

        // Adjust the canvas size and placement as needed
        drawingCanvas.setWidth(500);
        drawingCanvas.setHeight(400);

        // Show diagram preview
        vbox.getChildren().add(new Label("Diagram Preview"));
        vbox.getChildren().add(diagramPane);

        Scene scene = new Scene(new ScrollPane(vbox), 600, 700);
        inputStage.setScene(scene);
        inputStage.initModality(Modality.APPLICATION_MODAL);
        inputStage.showAndWait();

        return currentUseCaseDiagram;
    }


    /**
     * Checks if all fields in the provided VBox are filled.
     * This method iterates through all child nodes of the VBox and checks each TextField
     * to ensure that it contains non-empty text. If any field is empty, it returns false.
     *
     * @param vbox The VBox containing the fields to check.
     * @return true if all fields are filled, false if any field is empty.
     */
    private boolean areFieldsFilled(VBox vbox) {
        for (Node node : vbox.getChildren()) {
            if (node instanceof VBox) {
                VBox entryVBox = (VBox) node;
                for (Node subNode : entryVBox.getChildren()) {
                    if (subNode instanceof TextField) {
                        TextField textField = (TextField) subNode;
                        if (textField.getText().trim().isEmpty()) {
                            return false; // Return false if any TextField is empty
                        }
                    }
                }
            }
        }
        return true; // Return true if all fields are filled
    }


    /**
     * Adds a new actor field to the provided VBox for actors.
     * This method creates a label and text field for entering the actor's name and
     * adds them to the VBox. If an existing actor is provided, its name is set in the text field.
     *
     * @param actorsVBox The VBox where the new actor field will be added.
     * @param existingActor An existing Actor object to pre-fill the name, or null for a new actor.
     */
    private void addActorField(VBox actorsVBox, Actor existingActor) {
        Label actorLabel = new Label("Actor:");
        actorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField actorNameField = new TextField();
        actorNameField.setPromptText("Enter Actor Name");
        actorNameField.setText(existingActor != null ? existingActor.getName() : "");

        VBox newActorVBox = new VBox(10);
        newActorVBox.getChildren().addAll(actorLabel, actorNameField);
        actorsVBox.getChildren().add(newActorVBox);
    }

    /**
     * Adds a new use case field to the provided VBox for use cases.
     * This method creates a label and text field for entering the use case's name and
     * adds them to the VBox. If an existing use case is provided, its name is set in the text field.
     *
     * @param useCasesVBox The VBox where the new use case field will be added.
     * @param existingUseCase An existing UseCase object to pre-fill the name, or null for a new use case.
     */
    private void addUseCaseField(VBox useCasesVBox, UseCase existingUseCase) {
        Label useCaseLabel = new Label("Use Case:");
        useCaseLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField useCaseNameField = new TextField();
        useCaseNameField.setPromptText("Enter Use Case Name");
        useCaseNameField.setText(existingUseCase != null ? existingUseCase.getName() : "");

        VBox newUseCaseVBox = new VBox(10);
        newUseCaseVBox.getChildren().addAll(useCaseLabel, useCaseNameField);
        useCasesVBox.getChildren().add(newUseCaseVBox);
    }

    /**
     * Collects the actors entered in the provided VBox and returns them as a list of Actor objects.
     * This method iterates through each VBox in the actors VBox, retrieves the actor names from the
     * associated TextFields, and creates Actor objects from them.
     *
     * @param actorsVBox The VBox containing actor fields.
     * @return A list of Actor objects created from the input fields.
     */
    private List<Actor> gatherActors(VBox actorsVBox) {
        List<Actor> actors = new ArrayList<>();
        for (Node node : actorsVBox.getChildren()) {
            if (node instanceof VBox) {
                VBox entryVBox = (VBox) node;
                TextField actorNameField = (TextField) entryVBox.getChildren().get(1);
                Actor actor = new Actor(actorNameField.getText().trim());
                actors.add(actor);
            }
        }
        return actors;
    }

    /**
     * Collects the use cases entered in the provided VBox and returns them as a list of UseCase objects.
     * This method iterates through each VBox in the use cases VBox, retrieves the use case names from
     * the associated TextFields, and creates UseCase objects from them.
     *
     * @param useCasesVBox The VBox containing use case fields.
     * @return A list of UseCase objects created from the input fields.
     */
    private List<UseCase> gatherUseCases(VBox useCasesVBox) {
        List<UseCase> useCases = new ArrayList<>();
        for (Node node : useCasesVBox.getChildren()) {
            if (node instanceof VBox) {
                VBox entryVBox = (VBox) node;
                TextField useCaseNameField = (TextField) entryVBox.getChildren().get(1);
                UseCase useCase = new UseCase(useCaseNameField.getText().trim());
                useCases.add(useCase);
            }
        }
        return useCases;
    }

    /**
     * Displays an error dialog with the specified message.
     * This method creates an alert of type ERROR and displays it to the user with the provided content text.
     *
     * @param message The error message to display in the dialog.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
