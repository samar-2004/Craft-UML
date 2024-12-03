package org.example.craftuml;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ChooseDiagramController {
    @FXML
    private Button useCaseButton;

    @FXML
    private Button classButton;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        useCaseButton.setOnAction(event -> openUseCaseDashboard());
        classButton.setOnAction(event -> openClassDashboard());
    }

    private void openUseCaseDashboard() {
        System.out.println("Button clicked, opening Use Case Dashboard...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("use-case-dashboard-view.fxml"));
            Scene dashboardScene = new Scene(loader.load());
            stage.setScene(dashboardScene);
            stage.setTitle("Use Case Diagram Dashboard");
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

    private void openClassDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("class-dashboard-view.fxml"));
            Scene dashboardScene = new Scene(loader.load());
            stage.setScene(dashboardScene);
            stage.setTitle("Class Diagram DashBoard");
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
