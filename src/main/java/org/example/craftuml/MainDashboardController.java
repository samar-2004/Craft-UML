package org.example.craftuml;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class MainDashboardController {
    @FXML
    public void handleCreateDiagram() {
        System.out.println("Create Diagram button clicked.");
        // Code to open the diagram creation view
    }

    @FXML
    public void handleExportDiagram() {
        System.out.println("Export Diagram button clicked.");
        // Code to export the current diagram
    }

    @FXML
    public void handleGenerateCode() {
        System.out.println("Generate Code button clicked.");
        // Code to generate code for the current class diagram
    }

    @FXML
    public void handleManageProject() {
        System.out.println("Manage Project button clicked.");
        // Code to manage project artifacts
    }

    @FXML
    public void handleSaveProject() {
        System.out.println("Save Project button clicked.");
        // Code to save the project
    }

    @FXML
    public void handleLoadProject() {
        System.out.println("Load Project button clicked.");
        // Code to load an existing project
    }
}
