package org.example.craftuml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.craftuml.Controllers.HelloController;

import java.io.IOException;

/**
 * The HelloApplication class is the entry point for the JavaFX application.
 * It sets up and launches the primary application window, including the FXML layout and controller.
 */
public class HelloApplication extends Application {
    /**
     * Default constructor for the HelloApplication class.
     * Initializes the application without any specific setup.
     */
    public HelloApplication() {
        super();
    }

    /**
     * Initializes and displays the primary application window.
     * This method loads the FXML layout file, initializes the controller, and sets up the scene for the primary stage.
     *
     * @param primaryStage The primary stage for the application, provided by the JavaFX runtime.
     * @throws IOException If there is an error loading the FXML layout file.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setMaximized(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();
        HelloController controller = loader.getController();
        controller.setStage(primaryStage); 
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Craft UML");
        primaryStage.show();

    }
    /**
     * The main method is the entry point for the Java application.
     * It launches the JavaFX application by calling the launch() method.
     *
     * @param args Command-line arguments passed to the application (unused).
     */
    public static void main(String[] args) {
        launch();
    }
}