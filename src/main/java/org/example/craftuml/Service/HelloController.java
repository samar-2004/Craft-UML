package org.example.craftuml.Service;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class HelloController {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;

        stage.setOnShown(event -> {
            Scene scene = stage.getScene();
            scene.setOnKeyPressed(this::handleKeyPress);
        });
    }

    @FXML
    public void initialize() {
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