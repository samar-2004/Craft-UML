package org.example.craftuml.UI;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.ClassDiagrams.MethodData;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the user interface for an interface diagram, allowing for the visualization and manipulation
 * of an interface diagram on a drawing canvas.
 * This class provides functionality for creating a new interface diagram or modifying an existing one.
 */
public class InterfaceDiagramUI {

    /**
     * Represents the interface diagram data being displayed and interacted with in the UI.
     */
    private InterfaceData interfaceDiagram;

    /**
     * The canvas on which the interface diagram will be drawn. This canvas serves as the primary visual area
     * for rendering the interface diagram elements.
     */
    private Canvas drawingCanvas;

    /**
     * A label used to display error messages to the user. This label is used to notify the user of any issues
     * or validation failures related to the interface diagram.
     */
    Label errorLabel = new Label();

    /**
     * A list of existing interface diagrams. This list is used to check for existing diagrams and to update
     * or create new interface diagrams in the context of this user interface.
     */
    private List<InterfaceData> interfaces;


    /**
     * Constructs a new InterfaceDiagramUI with a given drawing canvas and list of interfaces.
     * The new interface diagram will be initialized as an empty interface diagram.
     *
     * @param drawingCanvas The canvas on which the interface diagram will be drawn. Cannot be null.
     * @param interfaces A list of existing interface diagrams to interact with. Cannot be null.
     * @throws IllegalArgumentException if the drawingCanvas is null.
     */
    public InterfaceDiagramUI(Canvas drawingCanvas,List<InterfaceData> interfaces)
    {
        if (drawingCanvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.drawingCanvas = drawingCanvas;
        this.interfaceDiagram = new InterfaceData();
        this.interfaces = interfaces;
    }

    /**
     * Constructs a new InterfaceDiagramUI with a given drawing canvas, an existing interface diagram,
     * and a list of interfaces.
     *
     * @param drawingCanvas The canvas on which the interface diagram will be drawn. Cannot be null.
     * @param interfaceDiagram The existing interface diagram to be displayed. Cannot be null.
     * @param interfaces A list of existing interface diagrams to interact with. Cannot be null.
     * @throws IllegalArgumentException if the drawingCanvas is null.
     */
    public InterfaceDiagramUI(Canvas drawingCanvas, InterfaceData interfaceDiagram,List<InterfaceData> interfaces) {
        if (drawingCanvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.drawingCanvas = drawingCanvas;
        this.interfaceDiagram = interfaceDiagram;
        this.interfaces = interfaces;
    }

    /**
     * Displays a dialog for adding or editing an interface diagram. The dialog allows the user to input the
     * interface name, add or remove methods, and validate the input before saving.
     *
     * @return The InterfaceData object representing the interface diagram after the user has added or edited it.
     */
    public InterfaceData showInterfaceDiagramDialog() {

        Stage inputStage = new Stage();
        errorLabel.setVisible(false);
        inputStage.setTitle("Add or Edit Interface");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label classNameLabel = new Label("Interface Name:");
        classNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        TextField classNameField = new TextField();
        classNameField.setPromptText("Enter Interface Name");
        classNameField.setPrefWidth(300);


        inputStage.setOnCloseRequest(event -> {
            interfaceDiagram = null;
            inputStage.close();
        });

        if (interfaceDiagram.getName() != null) {
            classNameField.setText(interfaceDiagram.getName());
        }

        classNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains(" ")) {
                classNameField.setStyle("-fx-border-color: red;");
            } else {
                classNameField.setStyle("");
                interfaceDiagram.setName(classNameField.getText());
            }
        });


        Label methodsLabel = new Label("Methods:");
        methodsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        VBox methodsVBox = new VBox(10);
        methodsVBox.setPadding(new Insets(10));
        methodsVBox.setVisible(true);

        for (MethodData method : interfaceDiagram.getMethods()) {
            addMethodField(methodsVBox, method);
        }

        Button addMethodButton = new Button("+");
        Button removeMethodButton = new Button("-");

        addMethodButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        removeMethodButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        HBox methodButtons = new HBox(10, addMethodButton, removeMethodButton);

        addMethodButton.setOnAction(e -> addMethodField(methodsVBox, null));
        removeMethodButton.setOnAction(e -> {
            if (!methodsVBox.getChildren().isEmpty()) {
                methodsVBox.getChildren().remove(methodsVBox.getChildren().size() - 1);
            }
        });

        HBox buttonBox = new HBox(10);
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        cancelButton.setOnAction(e -> {
            interfaceDiagram = null;
            inputStage.close();
        });

        String buttonStyle = """
    -fx-background-color: #503774; 
    -fx-text-fill: white; 
    -fx-focus-color: transparent; 
    -fx-faint-focus-color: transparent;
    -fx-background-insets: 0;
""";

        String buttonHoverStyle = """
    -fx-background-color: #785aa6; 
    -fx-text-fill: white; 
    -fx-focus-color: transparent; 
    -fx-faint-focus-color: transparent;
    -fx-background-insets: 0;
""";

        okButton.setStyle(buttonStyle);
        cancelButton.setStyle(buttonStyle);

        okButton.setOnMouseEntered(e -> okButton.setStyle(buttonHoverStyle));
        okButton.setOnMouseExited(e -> okButton.setStyle(buttonStyle));

        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(buttonHoverStyle));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(buttonStyle));

        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> {
            String className = classNameField.getText().trim();
            if (!validateInterfaceName(className, classNameField)) {
                return;
            }

            for (InterfaceData diagram : interfaces) {
                if (diagram.getName().equalsIgnoreCase(className) && !diagram.equals(interfaceDiagram)) {
                    showError("An interface diagram with this name already exists.");
                    return;
                }
            }

            String originalClassName = interfaceDiagram.getName();
            if (originalClassName == null || !originalClassName.equalsIgnoreCase(className)) {
                interfaceDiagram.setName(className);
            }

            if (!areFieldsFilled(methodsVBox)) {
                showError("Please ensure all fields are filled for each method.");
                return;
            }
            if(errorLabel.isVisible())
            {
                showError("Please ensure all fields are filled for each method.");
                return;
            }
            List<MethodData> methods = gatherMethods(methodsVBox);


            interfaceDiagram.setName(className);
            interfaceDiagram.setMethods(methods);

            inputStage.close();
        });

        buttonBox.getChildren().addAll(okButton, cancelButton);
        Separator classNameSeparator = new Separator();
        Separator attributesSeparator = new Separator();
        Separator methodsSeparator = new Separator();

        classNameSeparator.setStyle("-fx-background-color:#808080;");
        methodsSeparator.setStyle("-fx-background-color: #808080;");

        vbox.getChildren().addAll(
                classNameLabel,
                classNameField,
                classNameSeparator,
                methodsLabel,
                methodsVBox,
                methodButtons,
                methodsSeparator,
                buttonBox
        );

        Scene scene = new Scene(new ScrollPane(vbox), 600, 700);
        inputStage.setScene(scene);
        inputStage.initModality(Modality.APPLICATION_MODAL);
        inputStage.showAndWait();

        return interfaceDiagram;
    }

    /**
     * Validates the provided interface name to ensure it is not empty, does not contain spaces, and is a valid name.
     * If the name is invalid, it sets the border color of the input field to red and displays an error message.
     *
     * @param interfaceName The name of the interface to validate.
     * @param field The text field where the interface name is entered.
     * @return true if the interface name is valid, false otherwise.
     */
    public boolean validateInterfaceName(String interfaceName, TextField field) {
        if (interfaceName == null || interfaceName.trim().isEmpty() || interfaceName.contains(" ")) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showError("Invalid Interface Name! It cannot be empty or contain spaces.");
            return false;
        }
        field.setStyle("");
        return true;
    }

    /**
     * Checks if all required fields (access modifier, name, and data type) for methods in the provided VBox are filled.
     * The method iterates over the VBox containing method fields and ensures no fields are left empty.
     *
     * @param vBox The VBox containing method fields to validate.
     * @return true if all fields are filled, false otherwise.
     */
    public boolean areFieldsFilled(VBox vBox) {
        for (Node node : vBox.getChildren()) {
            if (node instanceof VBox) {
                VBox entryVBox = (VBox) node;
                ComboBox<String> accessCombo = null;
                TextField nameField = null;
                TextField dataField = null;

                for (Node innerNode : entryVBox.getChildren()) {
                    if (innerNode instanceof HBox) {
                        HBox hbox = (HBox) innerNode;

                        for (Node hboxChild : hbox.getChildren()) {
                            if (hboxChild instanceof VBox) {
                                VBox fieldVBox = (VBox) hboxChild;

                                for (Node fieldNode : fieldVBox.getChildren()) {
                                    if (fieldNode instanceof ComboBox && accessCombo == null) {
                                        accessCombo = (ComboBox<String>) fieldNode;
                                    } else if (fieldNode instanceof TextField) {
                                        if (nameField == null) {
                                            nameField = (TextField) fieldNode;
                                        } else {
                                            dataField = (TextField) fieldNode;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (accessCombo == null || accessCombo.getValue() == null ||
                        nameField == null || nameField.getText().trim().isEmpty() ||
                        dataField == null || dataField.getText().trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adds a new method input field to the provided VBox for displaying in the interface diagram dialog.
     * This includes input fields for the access modifier, method name, and return type.
     * If an existing method is provided, the fields will be pre-filled with its data.
     *
     * @param methodsVBox The VBox to which the new method input fields will be added.
     * @param existingMethod The existing MethodData object to pre-fill the fields (can be null for new methods).
     */
    public void addMethodField(VBox methodsVBox, MethodData existingMethod) {
        Region separator = new Region();
        separator.setStyle("-fx-background-color: #D3D3D3; -fx-min-height: 1.5px;");

        long methodCount = methodsVBox.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .count() + 1;
        Label methodLabel = new Label("Method " + methodCount + ":");

        methodLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox newMethodVBox = new VBox(10);
        ComboBox<String> accessModifierCombo = new ComboBox<>();
        accessModifierCombo.getItems().addAll("+", "-", "#");
        accessModifierCombo.setValue(existingMethod != null ? existingMethod.getAccessModifier() : "+");

        TextField methodNameField = new TextField();
        methodNameField.setPromptText("Enter Method e.g., methodName()");
        methodNameField.setText(existingMethod != null ? existingMethod.getName() : "");

        TextField returnTypeField = new TextField();
        returnTypeField.setPromptText("Enter Return Type");
        returnTypeField.setText(existingMethod != null ? existingMethod.getReturnType() : "");

        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        methodNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (validateMethodName(newValue)) {

                methodNameField.setStyle("");
                errorLabel.setVisible(false);
            } else
            {
                methodNameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                errorLabel.setText("Invalid method name! Use () at the end and avoid '(' at the start.");
                errorLabel.setVisible(true);
            }
        });

        VBox vbox1 = new VBox(10);
        vbox1.getChildren().addAll(new Label("Access Modifier:"), accessModifierCombo);

        VBox vbox2 = new VBox(10);
        vbox2.getChildren().addAll(new Label("Method Name:"), methodNameField);

        VBox vbox3 = new VBox(10);
        vbox3.getChildren().addAll(new Label("Return Type:"), returnTypeField);

        HBox fieldBox = new HBox(10, vbox1, vbox2, vbox3);

        newMethodVBox.getChildren().addAll(methodLabel, fieldBox);
        methodsVBox.getChildren().addAll(newMethodVBox, separator);
    }

    /**
     * Validates the provided method name to ensure it follows the correct format:
     * - The name must contain parentheses `()` at the end.
     * - The parentheses cannot be empty and must enclose the method name properly.
     *
     * @param methodName The method name to validate.
     * @return true if the method name is valid, false otherwise.
     */
    private boolean validateMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return false;
        }

        int openParenIndex = methodName.indexOf('(');
        int closeParenIndex = methodName.lastIndexOf(')');

        return closeParenIndex == methodName.length() - 1 && openParenIndex > 0 && openParenIndex < closeParenIndex;
    }

    /**
     * Gathers and extracts all method data from the provided VBox, creating a list of MethodData objects.
     * This method retrieves the access modifier, method name, and return type for each method defined in the VBox.
     *
     * @param methodsVBox The VBox containing the method input fields to gather data from.
     * @return A list of MethodData objects representing the methods entered by the user.
     */
    private List<MethodData> gatherMethods(VBox methodsVBox) {
        List<MethodData> methods = new ArrayList<>();
        for (Node node : methodsVBox.getChildren()) {
            if (node instanceof VBox) {
                VBox entryVBox = (VBox) node;
                ComboBox<String> accessCombo = null;
                TextField nameField = null;
                TextField returnTypeField = null;

                for (Node innerNode : entryVBox.getChildren()) {
                    if (innerNode instanceof HBox) {
                        HBox hbox = (HBox) innerNode;

                        for (Node hboxChild : hbox.getChildren()) {
                            if (hboxChild instanceof VBox) {
                                VBox fieldVBox = (VBox) hboxChild;

                                for (Node fieldNode : fieldVBox.getChildren()) {
                                    if (fieldNode instanceof ComboBox && accessCombo == null) {
                                        accessCombo = (ComboBox<String>) fieldNode;
                                    } else if (fieldNode instanceof TextField) {
                                        if (nameField == null) {
                                            nameField = (TextField) fieldNode;
                                        } else {
                                            returnTypeField = (TextField) fieldNode;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                MethodData methodData = new MethodData(
                        accessCombo.getValue(), nameField.getText(), returnTypeField.getText()
                );
                methods.add(methodData);
            }
        }
        return methods;
    }

    /**
     * Displays an error message in an alert dialog. This method is used to show error messages to the user.
     *
     * @param message The error message to be displayed in the alert dialog.
     */
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

