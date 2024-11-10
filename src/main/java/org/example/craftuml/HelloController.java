package org.example.craftuml;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

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
        loadMainDashboard();
    }

    private void loadMainDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-dashboard-view.fxml"));
            Scene dashboardScene = new Scene(loader.load());
            stage.setScene(dashboardScene);
            Platform.runLater(() -> {
                stage.setWidth(1920);
                stage.setHeight(1080);
                stage.setMaximized(true);
                stage.show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}