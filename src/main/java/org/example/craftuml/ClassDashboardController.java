package org.example.craftuml;

import javafx.fxml.FXML;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.craftuml.UI.classDiagramUI;
import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.MethodData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ClassDashboardController
{
    @FXML
    private Canvas drawingCanvas;

    @FXML
    private ListView<String> modelInfoList;

    private ClassDiagram classDiagram,activeDiagram;

    private List<ClassDiagram> classDiagrams = new ArrayList<>();


    @FXML
    private void handleClassDiagram() {

        classDiagramUI classDiagramUI = new classDiagramUI(drawingCanvas);
        classDiagram = classDiagramUI.showClassDiagramDialog();

        double newX = 20 + (classDiagrams.size() * 100) % (drawingCanvas.getWidth() - 100);
        double newY = 20 + ((classDiagrams.size() * 100) / (drawingCanvas.getWidth() - 100)) * 100;

        classDiagram.setX(newX);
        classDiagram.setY(newY);

        classDiagrams.add(classDiagram);
        createClassDiagram(classDiagram);
    }

    private double dragStartX = 0;
    private double dragStartY = 0;


    private void createClassDiagram(ClassDiagram classDiagram)
    {

        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        double x = classDiagram.getX();
        double y = classDiagram.getY();

        double height = 1;

        int attributeCount = classDiagram.getAttributes().size();
        int methodCount = classDiagram.getMethods().size();

        height += Math.max(attributeCount, 1) * 30;
        height += Math.max(methodCount, 1) * 30;

        double classNameHeight = 30;

        int totalItems = attributeCount + methodCount;

        double attributeHeight = 0;
        double methodHeight = 0;

        if (totalItems > 0) {
            double attributeProportion = (double) attributeCount / totalItems;
            double methodProportion = (double) methodCount / totalItems;

            attributeHeight = (height - classNameHeight) * attributeProportion;
            methodHeight = (height - classNameHeight) * methodProportion;
        } else {
            attributeHeight = (height - classNameHeight) / 2;
            methodHeight = (height - classNameHeight) / 2;
        }

        double maxWidth = 0;

        for (AttributeData at : classDiagram.getAttributes()) {
            String attributeText = at.getAccessModifier() + " " + at.getName() + " : " + at.getDataType();
            Text tempText = new Text(attributeText);
            tempText.setFont(gc.getFont());
            double textWidth = tempText.getLayoutBounds().getWidth();
            maxWidth = Math.max(maxWidth, textWidth);
        }

        for (MethodData md : classDiagram.getMethods()) {
            String methodText = md.getAccessModifier() + " " + md.getName() + " : " + md.getReturnType();
            Text tempText = new Text(methodText);
            tempText.setFont(gc.getFont());
            double textWidth = tempText.getLayoutBounds().getWidth();
            maxWidth = Math.max(maxWidth, textWidth);
        }

        double width = maxWidth + 40;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        gc.strokeLine(x, y + classNameHeight, x + width, y + classNameHeight);
        gc.strokeLine(x, y + classNameHeight + attributeHeight, x + width, y + classNameHeight + attributeHeight);

        gc.setFill(Color.BLACK);
        String className = classDiagram.getName();

        Text tempText = new Text(className);
        tempText.setFont(gc.getFont());
        double classNameWidth = tempText.getLayoutBounds().getWidth();

        gc.fillText(className, x + (width - classNameWidth) / 2, y + classNameHeight / 2 + 10);

        double attrY = y + classNameHeight + 15;
        for (AttributeData at : classDiagram.getAttributes()) {
            StringBuilder attribute = new StringBuilder();
            attribute.append(at.getAccessModifier()).append(" ");
            attribute.append(at.getName()).append(" : ");
            attribute.append(at.getDataType());

            gc.fillText(attribute.toString(), x + 10, attrY);
            attrY += 20;
        }

        double methY = y + classNameHeight + attributeHeight + 15;
        for (MethodData md : classDiagram.getMethods()) {
            StringBuilder method = new StringBuilder();
            method.append(md.getAccessModifier()).append(" ");
            method.append(md.getName()).append(" : ");
            method.append(md.getReturnType());

            gc.fillText(method.toString(), x + 10, methY);
            methY += 20;
        }

        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        contextMenu.getItems().addAll(editItem, deleteItem);

        drawingCanvas.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                boolean found = false;
                for (ClassDiagram diagram : classDiagrams) {
                    if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {

                        activeDiagram = diagram;
                        contextMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    activeDiagram = null;
                    contextMenu.hide();
                }
            } else if (event.isPrimaryButtonDown()) {
                contextMenu.hide();
                for (ClassDiagram diagram : classDiagrams) {
                    if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                        activeDiagram = diagram;
                        dragStartX = event.getX() - diagram.getX();
                        dragStartY = event.getY() - diagram.getY();
                        break;
                    }
                }
            }
        });

        deleteItem.setOnAction(event -> {
            if (activeDiagram != null) {
                deleteClassDiagram(activeDiagram);
            } else {
                System.out.println("No diagram selected to delete.");
            }
        });

        editItem.setOnAction(event -> {
            if (activeDiagram != null) {
                editClassDiagram(activeDiagram);
            } else {
                System.out.println("No diagram selected to edit.");
            }
        });


        drawingCanvas.setOnMouseDragged(event -> {
            if (activeDiagram != null)
            {
                double newX = event.getX() - dragStartX;
                double newY = event.getY() - dragStartY;

                activeDiagram.setX(newX);
                activeDiagram.setY(newY);

                gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                for (ClassDiagram diagram : classDiagrams) {
                    createClassDiagram(diagram);
                }
            }
        });

        drawingCanvas.setOnMouseReleased(event -> {

        });

        drawingCanvas.setOnMouseEntered(event -> drawingCanvas.setCursor(Cursor.MOVE));
        drawingCanvas.setOnMouseExited(event -> drawingCanvas.setCursor(Cursor.DEFAULT));
    }

    private void deleteClassDiagram(ClassDiagram classDiagram) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this class diagram?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            classDiagrams.remove(classDiagram);
            activeDiagram = null;
            redrawCanvas();
        }
    }

    private void redrawCanvas()
    {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        for (ClassDiagram diagram : classDiagrams) {
            createClassDiagram(diagram);
        }
    }

    private void editClassDiagram(ClassDiagram classDiagram) {
        classDiagramUI classDiagramUI = new classDiagramUI(drawingCanvas, classDiagram);
        ClassDiagram updatedDiagram = classDiagramUI.showClassDiagramDialog();

        if (updatedDiagram != null)
        {
            activeDiagram.setName(updatedDiagram.getName());
            activeDiagram.setAttributes(updatedDiagram.getAttributes());
            activeDiagram.setMethods(updatedDiagram.getMethods());

            redrawCanvas();
        }
    }

    private boolean isWithinBounds(double mouseX, double mouseY, ClassDiagram diagram,GraphicsContext gc) {
        double x = diagram.getX();
        double y = diagram.getY();
        double width = calculateDiagramWidth(diagram,gc);
        double height = calculateDiagramHeight(diagram);
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private double calculateDiagramWidth(ClassDiagram diagram, GraphicsContext gc) {
        double maxWidth = 0;
        Text tempText = new Text(diagram.getName());
        tempText.setFont(gc.getFont());
        maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());

        for (AttributeData attribute : diagram.getAttributes()) {
            String attributeText = attribute.getAccessModifier() + " " + attribute.getName() + " : " + attribute.getDataType();
            tempText = new Text(attributeText);
            tempText.setFont(gc.getFont());
            maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());
        }
        for (MethodData method : diagram.getMethods()) {
            String methodText = method.getAccessModifier() + " " + method.getName() + " : " + method.getReturnType();
            tempText = new Text(methodText);
            tempText.setFont(gc.getFont());
            maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());
        }

        return maxWidth + 40;
    }

    private double calculateDiagramHeight(ClassDiagram diagram) {
        double classNameHeight = 30;
        double attributeHeight = Math.max(30 * diagram.getAttributes().size(), 30);
        double methodHeight = Math.max(30 * diagram.getMethods().size(), 30);

        return classNameHeight + attributeHeight + methodHeight + 10;
    }


    @FXML
    private void handleNewProject() {
        System.out.println("New Project Clicked");
    }

    @FXML
    private void handleOpenProject() {
        System.out.println("Open Project Clicked");
    }

    @FXML
    private void handleSaveProject() {
        System.out.println("Save Project Clicked");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleUndo() {
        System.out.println("Undo Clicked");
    }

    @FXML
    private void handleRedo() {
        System.out.println("Redo Clicked");
    }

    @FXML
    private void handleManageComponents()
    {
        System.out.println("Manage Components Clicked");
    }

    @FXML
    private void handleManageRelationships()
    {
        System.out.println("Manage Relationships Clicked");
    }

    @FXML
    private void handleGenerateCode()
    {
        System.out.println("Generate Code Clicked");
    }

    @FXML
    private void handleExportDiagram()
    {
        System.out.println("Export Diagram Clicked");
    }

    @FXML
    private void handleAbout()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Craft UML");
        alert.setContentText("Craft UML - A tool for creating UML diagrams and generating code.");
        alert.showAndWait();
    }
}
