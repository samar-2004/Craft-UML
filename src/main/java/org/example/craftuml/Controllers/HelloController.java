package org.example.craftuml.Controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HelloController {
    private Stage stage;
    @FXML
    private Label pressKeyLabel;

    public void setStage(Stage stage) {
        this.stage = stage;

        stage.setOnShown(event -> {
            Scene scene = stage.getScene();
            scene.setOnKeyPressed(this::handleKeyPress);
        });
    }

    @FXML
    public void initialize() {
        if (pressKeyLabel != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.8), pressKeyLabel);
            fadeTransition.setFromValue(1.0);  // Fully visible
            fadeTransition.setToValue(0.1);   // Mostly transparent
            fadeTransition.setCycleCount(FadeTransition.INDEFINITE); // Repeat indefinitely
            fadeTransition.setAutoReverse(true); // Reverse the fade direction

            // Start the animation
            fadeTransition.play();
        }
        if (this.stage != null && this.stage.getScene() != null) {
            Scene scene = this.stage.getScene();
            scene.setOnKeyPressed(this::handleKeyPress);
        }
    }

    private void handleKeyPress(KeyEvent event) {
        SelectDashboard();

    }

    private void SelectDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/craftuml/choose-diagram-view.fxml"));
            Stage chooseDiagramStage = new Stage();
            chooseDiagramStage.setScene(new Scene(loader.load()));

            ChooseDiagramController controller = loader.getController();
            controller.setStage(chooseDiagramStage);

            chooseDiagramStage.setMaximized(true);

            chooseDiagramStage.setTitle("Choose Diagram Type");
            chooseDiagramStage.initOwner(stage);
            chooseDiagramStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}