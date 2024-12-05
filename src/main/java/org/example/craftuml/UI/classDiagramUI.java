package org.example.craftuml.UI;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.MethodData;

import java.util.ArrayList;
import java.util.List;

public class classDiagramUI
{
    private ClassDiagram currentClassDiagram;
    private Canvas drawingCanvas;
    Label errorLabel = new Label();
    private List<ClassDiagram> classDiagrams;

    public classDiagramUI(Canvas drawingCanvas,List<ClassDiagram> diagrams) {
        if (drawingCanvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.drawingCanvas = drawingCanvas;
        this.currentClassDiagram = new ClassDiagram();
        this.classDiagrams = diagrams;
    }

    public classDiagramUI(Canvas drawingCanvas, ClassDiagram classDiagram,List<ClassDiagram> diagrams) {
        this.drawingCanvas = drawingCanvas;
        this.currentClassDiagram = classDiagram;
        this.classDiagrams = diagrams;
    }

    public ClassDiagram showClassDiagramDialog() {
        errorLabel.setVisible(false);
        Stage inputStage = new Stage();
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");
        inputStage.setTitle("Create or Edit Class Diagram");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label classNameLabel = new Label("Class Name:");
        classNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        TextField classNameField = new TextField();
        classNameField.setPromptText("Enter Class Name");
        classNameField.setPrefWidth(300);


        inputStage.setOnCloseRequest(event -> {
            currentClassDiagram = null;
            inputStage.close();
        });

        if (currentClassDiagram.getName() != null) {
            classNameField.setText(currentClassDiagram.getName());
        }

        classNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains(" ")) {
                classNameField.setStyle("-fx-border-color: red;");
                okButton.setDisable(true);

            } else {
                classNameField.setStyle("");
                currentClassDiagram.setName(classNameField.getText());
                okButton.setDisable(false);
            }
        });

        Label attributesLabel = new Label("Attributes:");
        attributesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        VBox attributesVBox = new VBox(10);
        attributesVBox.setPadding(new Insets(10));
        attributesVBox.setVisible(true);

        for (AttributeData attribute : currentClassDiagram.getAttributes()) {
            addAttributeField(attributesVBox, attribute);
        }

        Button addAttributeButton = new Button("+");
        Button removeAttributeButton = new Button("-");

        addAttributeButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        removeAttributeButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        HBox attributeButtons = new HBox(10, addAttributeButton, removeAttributeButton);

        addAttributeButton.setOnAction(e -> addAttributeField(attributesVBox, null));
        removeAttributeButton.setOnAction(e -> {
            if (!attributesVBox.getChildren().isEmpty()) {
                attributesVBox.getChildren().remove(attributesVBox.getChildren().size() - 1);
            }
        });

        Label methodsLabel = new Label("Methods:");
        methodsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        VBox methodsVBox = new VBox(10);
        methodsVBox.setPadding(new Insets(10));
        methodsVBox.setVisible(true);

        for (MethodData method : currentClassDiagram.getMethods()) {
            addMethodField(methodsVBox, method,okButton);
        }

        Button addMethodButton = new Button("+");
        Button removeMethodButton = new Button("-");

        addMethodButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        removeMethodButton.setStyle("-fx-background-color:#503774; -fx-text-fill: white;");
        HBox methodButtons = new HBox(10, addMethodButton, removeMethodButton);

        addMethodButton.setOnAction(e -> addMethodField(methodsVBox, null,okButton));
        removeMethodButton.setOnAction(e -> {
            if (!methodsVBox.getChildren().isEmpty()) {
                methodsVBox.getChildren().remove(methodsVBox.getChildren().size() - 1);
            }
        });

        HBox buttonBox = new HBox(10);

        cancelButton.setOnAction(e -> {
            currentClassDiagram = null;
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
            if (className.isEmpty()) {
                showError("Class name cannot be empty.");
                return;
            }

            for (ClassDiagram diagram : classDiagrams) {
                if (diagram.getName().equalsIgnoreCase(className)) {
                    showError("A class diagram with this name already exists.");
                    return;
                }
            }

            if (!areFieldsFilled(attributesVBox)) {
                showError("Please ensure all fields are filled for each attribute.");
                return;
            }
            List<AttributeData> attributes = gatherAttributes(attributesVBox);
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


            currentClassDiagram.setName(className);
            currentClassDiagram.setAttributes(attributes);
            currentClassDiagram.setMethods(methods);

            inputStage.close();
        });

        buttonBox.getChildren().addAll(okButton, cancelButton);
        Separator classNameSeparator = new Separator();
        Separator attributesSeparator = new Separator();
        Separator methodsSeparator = new Separator();

        classNameSeparator.setStyle("-fx-background-color:#808080;");
        attributesSeparator.setStyle("-fx-background-color: #808080;");
        methodsSeparator.setStyle("-fx-background-color: #808080;");

        vbox.getChildren().addAll(
                classNameLabel,
                classNameField,
                classNameSeparator,
                attributesLabel,
                attributesVBox,
                attributeButtons,
                attributesSeparator,
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

        return currentClassDiagram;
    }

    private boolean areFieldsFilled(VBox vBox) {
        for (Node node : vBox.getChildren()) {
            if (node instanceof VBox) {
                VBox entryVBox = (VBox) node;
                ComboBox<String> accessCombo = null;
                TextField nameField = null;
                TextField dataField = null;
                Label errorLabel = null;  // Declare the errorLabel variable to check visibility

                // Traverse through inner VBox to check fields and error label
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
                                    } else if (fieldNode instanceof Label && ((Label) fieldNode).getStyleClass().contains("errorLabel")) {
                                        errorLabel = (Label) fieldNode;  // Check for error label
                                    }
                                }
                            }
                        }
                    }
                }

                // If any field is not filled or an error label is visible, return false
                if (accessCombo == null || accessCombo.getValue() == null ||
                        nameField == null || nameField.getText().trim().isEmpty() ||
                        dataField == null || dataField.getText().trim().isEmpty() ||
                        (errorLabel != null && errorLabel.isVisible())) {
                    return false;  // Fields are incomplete or there's an error label visible
                }
            }
        }
        return true;
    }


    private void addAttributeField(VBox attributesVBox, AttributeData existingAttribute) {
        Region separator = new Region();
        separator.setStyle("-fx-background-color: #D3D3D3; -fx-min-height: 1.5px;");
        long attributeCount = attributesVBox.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .count() + 1;
        Label attributeLabel = new Label("Attribute " + attributeCount + ":");
        attributeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox newAttributeVBox = new VBox(10);
        ComboBox<String> accessModifierCombo = new ComboBox<>();
        accessModifierCombo.getItems().addAll("+", "-", "#");
        accessModifierCombo.setValue(existingAttribute != null ? existingAttribute.getAccessModifier() : "+");

        TextField attributeNameField = new TextField();
        attributeNameField.setPromptText("Enter Attribute Name");
        attributeNameField.setText(existingAttribute != null ? existingAttribute.getName() : "");

        TextField dataTypeField = new TextField();
        dataTypeField.setPromptText("Enter Data Type");
        dataTypeField.setText(existingAttribute != null ? existingAttribute.getDataType() : "");

        VBox vbox1 = new VBox(10);
        vbox1.getChildren().addAll(new Label("Access Modifier:"), accessModifierCombo);

        VBox vbox2 = new VBox(10);
        vbox2.getChildren().addAll(new Label("Attribute Name:"), attributeNameField);

        VBox vbox3 = new VBox(10);
        vbox3.getChildren().addAll(new Label("Data Type:"), dataTypeField);

        HBox fieldBox = new HBox(10, vbox1, vbox2, vbox3);

        newAttributeVBox.getChildren().addAll(attributeLabel, fieldBox);
        attributesVBox.getChildren().addAll(newAttributeVBox, separator);
    }

    private void addMethodField(VBox methodsVBox, MethodData existingMethod,Button okButton) {
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
                okButton.setDisable(false);

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

    private List<AttributeData> gatherAttributes(VBox attributesVBox) {
        List<AttributeData> attributes = new ArrayList<>();
        for (Node node : attributesVBox.getChildren()) {
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
                AttributeData attributeData = new AttributeData(accessCombo.getValue(), nameField.getText() , dataField.getText());
                attributes.add(attributeData);
            }
        }
        return attributes;
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
