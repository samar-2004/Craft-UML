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
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.ClassDiagrams.MethodData;

import java.util.ArrayList;
import java.util.List;

public class InterfaceDiagramUI {

    private InterfaceData interfaceDiagram;
    private Canvas drawingCanvas;

    public InterfaceDiagramUI(Canvas drawingCanvas)
    {
        if (drawingCanvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.drawingCanvas = drawingCanvas;
        this.interfaceDiagram = new InterfaceData();
    }
    public InterfaceDiagramUI(Canvas drawingCanvas, InterfaceData interfaceDiagram) {
        this(drawingCanvas);
        this.interfaceDiagram = interfaceDiagram;
    }

    public InterfaceData showInterfaceDiagramDialog() {
        Stage inputStage = new Stage();
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
            if (!areFieldsFilled(methodsVBox)) {
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

    private boolean validateInterfaceName(String interfaceName, TextField field) {
        if (interfaceName == null || interfaceName.trim().isEmpty() || interfaceName.contains(" ")) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showError("Invalid Interface Name! It cannot be empty or contain spaces.");
            return false;
        }
        field.setStyle("");
        return true;
    }

    private boolean areFieldsFilled(VBox vBox) {
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
    private void addMethodField(VBox methodsVBox, MethodData existingMethod) {
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

        Label errorLabel = new Label();
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
    private boolean validateMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return false;
        }

        int openParenIndex = methodName.indexOf('(');
        int closeParenIndex = methodName.lastIndexOf(')');

        return closeParenIndex == methodName.length() - 1 && openParenIndex > 0 && openParenIndex < closeParenIndex;
    }


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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

