package org.example.craftuml.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * The `ChooseDiagramController` class handles the user interface for choosing between different types of diagrams.
 * It provides functionality for opening the Use Case Diagram Dashboard or the Class Diagram Dashboard based on user interaction.
 * The class includes two buttons, one for each diagram type, and manages the navigation between different scenes in the application.
 * This controller is used with the FXML file that contains the layout and buttons for diagram selection.
 * Default constructor for the `ChooseDiagramController` class.
 * This constructor is automatically invoked when the controller is initialized by the JavaFX framework.
 * It does not perform any custom initialization, as the controller's setup is handled
 * in the {@link #initialize()} method after the FXML file is loaded.
 */

public class ChooseDiagramController {
    /**
     * The button that triggers the selection of the Use Case diagram.
     * It is defined as an FXML element, which links to the button in the corresponding FXML file.
     */
    @FXML
    private Button useCaseButton;

    /**
     * The button that triggers the selection of the Class diagram.
     * It is defined as an FXML element, which links to the button in the corresponding FXML file.
     */
    @FXML
    private Button classButton;

    /**
     * The Stage that represents the window in which the diagram is to be displayed.
     * It is used for managing the window that will open when a user selects a diagram type.
     */
    private Stage stage;

    /**
     * Sets the stage associated with this component or application.
     * The stage represents the primary window or container for the user interface.
     *
     * @param stage The {@link Stage} to associate with this component.
     *              This stage typically acts as the main window for the application.
     * @throws IllegalArgumentException If the provided stage is null.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Initializes the controller by setting up action handlers for the buttons.
     * This method is automatically called when the controller is loaded. It defines the actions
     * to be executed when the "Use Case" or "Class" diagram buttons are clicked.
     */
    @FXML
    public void initialize() {
        useCaseButton.setOnAction(event -> openUseCaseDashboard());
        classButton.setOnAction(event -> openClassDashboard());
    }

    /**
     * Opens the Use Case Diagram Dashboard when the "Use Case" button is clicked.
     * This method loads the FXML file for the Use Case Dashboard, creates a new scene,
     * and sets it in the stage. It also maximizes the window and sets the window size.
     */
    private void openUseCaseDashboard() {
        System.out.println("Button clicked, opening Use Case Dashboard...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/craftuml/use-case-dashboard-view.fxml"));
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

    /**
     * Opens the Class Diagram Dashboard when the "Class" button is clicked.
     * This method loads the FXML file for the Class Diagram Dashboard, creates a new scene,
     * and sets it in the stage. It also maximizes the window and sets the window size.
     */
    private void openClassDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/craftuml/class-dashboard-view.fxml"));
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
