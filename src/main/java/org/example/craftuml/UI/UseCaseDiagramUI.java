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

public class UseCaseDiagramUI {

    private UseCaseDiagram currentUseCaseDiagram;
    private Canvas drawingCanvas;
    private GraphicsContext gc;

    // Constructor with only the Canvas (default diagram)
    public UseCaseDiagramUI(Canvas drawingCanvas) {
        if (drawingCanvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.drawingCanvas = drawingCanvas;
        this.gc = drawingCanvas.getGraphicsContext2D(); // Get the GraphicsContext for drawing
        this.currentUseCaseDiagram = new UseCaseDiagram();
    }

    // Constructor with both Canvas and UseCaseDiagram
    public UseCaseDiagramUI(Canvas drawingCanvas, UseCaseDiagram useCaseDiagram) {
        if (drawingCanvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.drawingCanvas = drawingCanvas;
        this.gc = drawingCanvas.getGraphicsContext2D(); // Get the GraphicsContext for drawing
        this.currentUseCaseDiagram = useCaseDiagram;
    }

    public UseCaseDiagramUI(UseCaseDiagram useCaseDiagram) {
        this.currentUseCaseDiagram = useCaseDiagram;
    }

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



    // Checks if all fields in the provided VBox are filled
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


    // Adds a new actor field to the actors VBox
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

    // Adds a new use case field to the use cases VBox
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

    // Collect actors from the actor VBox
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

    // Collect use cases from the use cases VBox
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

    // Show error dialog
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
