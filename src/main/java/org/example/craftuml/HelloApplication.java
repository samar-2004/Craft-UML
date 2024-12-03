package org.example.craftuml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.craftuml.Service.HelloController;

import java.io.IOException;

public class HelloApplication extends Application {
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
    public static void main(String[] args) {
        launch();
    }
}