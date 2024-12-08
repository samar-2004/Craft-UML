package org.example.craftuml.Controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The HelloController class manages the primary screen for the Craft UML application.
 * It handles the display of a welcome message and manages the user interface events,
 * such as key presses, which transition to the diagram selection dashboard.
 * <p>
 * The controller also manages a fade animation on a label, providing visual feedback to
 * the user, and sets up event listeners for key presses to trigger the transition to
 * the dashboard view.
 * Default constructor for the `HelloController` class.
 * This constructor initializes the controller for the main screen of the Craft UML application.
 * It is required by JavaFX for loading the controller associated with the FXML file.
 */
public class HelloController {
    /**
     * The main stage for the application, used for managing the window's
     * properties and transitions.
     */
    private Stage stage;

    /**
     * A label used to display messages or instructions on the user interface.
     * For instance, it can be used to show text such as "Press any key to continue."
     */
    @FXML
    private Label pressKeyLabel;

    /**
     * Sets the main {@link Stage} for the controller and configures the event
     * handling for key presses when the stage is shown.
     *
     * @param stage The main stage of the application, used to manage the
     *              window properties and events.
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        stage.setOnShown(event -> {
            Scene scene = stage.getScene();
            scene.setOnKeyPressed(this::handleKeyPress);
        });
    }

    /**
     * Initializes the controller by setting up any necessary animations or event
     * listeners after the UI components have been loaded.
     * This method creates a fade animation for the {@link Label} (pressKeyLabel)
     * and configures the key press event handler for the stage scene.
     *
     * <p>The fade animation continuously fades the label in and out, providing
     * visual feedback to the user.</p>
     */
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

    /**
     * Handles the key press event and initiates the action to select the dashboard.
     * This method is triggered whenever a key is pressed while the stage is visible.
     * It calls the {@link #SelectDashboard()} method to transition to the dashboard.
     *
     * @param event The key event generated when a key is pressed.
     */
    private void handleKeyPress(KeyEvent event) {
        SelectDashboard();

    }

    /**
     * Opens a new stage to allow the user to choose a diagram type. This method
     * loads the {@link ChooseDiagramController} and sets up the scene for the
     * diagram selection view.
     *
     * <p>The method initializes the new window as a maximized stage, sets its
     * title, and ensures that the new window is a child of the main stage for
     * proper ownership. In case of an error during the loading or initialization,
     * the exception is printed to the console.</p>
     */
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