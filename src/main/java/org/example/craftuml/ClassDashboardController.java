package org.example.craftuml;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.craftuml.UI.InterfaceDiagramUI;
import org.example.craftuml.UI.classDiagramUI;
import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.ClassDiagrams.MethodData;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.craftuml.UI.InterfaceDiagramUI;
import org.example.craftuml.UI.classDiagramUI;
import org.example.craftuml.models.ClassDiagrams.AttributeData;
import org.example.craftuml.models.ClassDiagrams.ClassDiagram;
import org.example.craftuml.models.ClassDiagrams.InterfaceData;
import org.example.craftuml.models.ClassDiagrams.MethodData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
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
    private InterfaceData interfaceDiagram,activeInterface;

    private List<InterfaceData> interfaceDiagrams =new ArrayList<>();

    private List<ClassDiagram> classDiagrams = new ArrayList<>();
    private double dragStartX = 0;
    private double dragStartY = 0;
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);



    @FXML
    public void initialize() {
        // Initialize the canvas mouse handlers once
        initializeCanvasHandlers();
    }
    @FXML
    private void handleClassDiagram()
    {

        classDiagramUI classDiagramUI = new classDiagramUI(drawingCanvas);
        classDiagram = classDiagramUI.showClassDiagramDialog();

        double newX = 20 + (classDiagrams.size() * 100) % (drawingCanvas.getWidth() - 100);
        double newY = 20 + ((classDiagrams.size() * 100) / (drawingCanvas.getWidth() - 100)) * 100;

        System.out.println("\nclass diagram : "+ classDiagram.getName()+"\n");
        classDiagram.setX(newX);
        classDiagram.setY(newY);
        classDiagrams.add(classDiagram);
        createClassDiagram(classDiagram);
    }

    public void createClassDiagram(ClassDiagram classDiagram)
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

        MenuItem editItem = new MenuItem(" Edit     ");
        MenuItem deleteItem = new MenuItem(" Delete    ");
        contextMenu.getItems().addAll(editItem, deleteItem);
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
    public void redrawCanvas() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        // Draw class diagrams
        for (ClassDiagram diagram : classDiagrams) {
            createClassDiagram(diagram);
        }

        // Draw interface diagrams
        for (InterfaceData diagram : interfaceDiagrams) {
            createInterfaceDiagram(diagram);
        }


    }

    // Example method for drawing an association

    private void editClassDiagram(ClassDiagram classDiagram) {
        classDiagramUI classDiagramUI = new classDiagramUI(drawingCanvas, classDiagram);
        ClassDiagram updatedDiagram = classDiagramUI.showClassDiagramDialog();

        if (updatedDiagram != null) {
            activeDiagram.setName(updatedDiagram.getName());
            activeDiagram.setAttributes(updatedDiagram.getAttributes());
            activeDiagram.setMethods(updatedDiagram.getMethods());

            redrawCanvas();
        }
    }

    private boolean isWithinBounds(double mouseX, double mouseY, ClassDiagram diagram, GraphicsContext gc) {
        double x = diagram.getX();
        double y = diagram.getY();
        double width = calculateDiagramWidth(diagram, gc);
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





    private void initializeCanvasHandlers() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        drawingCanvas.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                boolean found = false;
                activeDiagram = null;
                activeInterface = null;

                for (ClassDiagram diagram : classDiagrams) {
                    if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                        activeDiagram = diagram;
                        showContextMenu(event, "class");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (InterfaceData diagram : interfaceDiagrams) {
                        if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                            activeInterface = diagram;
                            showContextMenu(event, "interface");
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    activeDiagram = null;
                    activeInterface = null;
                }
            } else if (event.isPrimaryButtonDown()) {
                activeDiagram = null;
                activeInterface = null;

                for (ClassDiagram diagram : classDiagrams) {
                    if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                        activeDiagram = diagram;
                        dragStartX = event.getX() - diagram.getX();
                        dragStartY = event.getY() - diagram.getY();
                        break;
                    }
                }

                if (activeDiagram == null) {
                    for (InterfaceData diagram : interfaceDiagrams) {
                        if (isWithinBounds(event.getX(), event.getY(), diagram, gc)) {
                            activeInterface = diagram;
                            dragStartX = event.getX() - diagram.getX();
                            dragStartY = event.getY() - diagram.getY();
                            break;
                        }
                    }
                }
            }
        });

        drawingCanvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (activeDiagram != null) {
                    editClassDiagram(activeDiagram);
                } else if (activeInterface != null) {
                    editInterfaceDiagram(activeInterface);
                }
            }
        });

        drawingCanvas.setOnMouseDragged(event -> {
            if (activeDiagram != null) {
                double newX = event.getX() - dragStartX;
                double newY = event.getY() - dragStartY;

                activeDiagram.setX(newX);
                activeDiagram.setY(newY);
                redrawCanvas();
            } else if (activeInterface != null) {
                double newX = event.getX() - dragStartX;
                double newY = event.getY() - dragStartY;

                activeInterface.setX(newX);
                activeInterface.setY(newY);
                redrawCanvas();
            }
        });

        drawingCanvas.setOnMouseReleased(event -> {
        });

        drawingCanvas.setOnMouseEntered(event -> drawingCanvas.setCursor(Cursor.MOVE));
        drawingCanvas.setOnMouseExited(event -> drawingCanvas.setCursor(Cursor.DEFAULT));
    }

    private void showContextMenu(MouseEvent event, String diagramType) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");

        contextMenu.getItems().addAll(editItem, deleteItem);

        if (diagramType.equals("class")) {
            editItem.setOnAction(e -> {
                if (activeDiagram != null) editClassDiagram(activeDiagram);
            });
            deleteItem.setOnAction(e -> {
                if (activeDiagram != null) deleteClassDiagram(activeDiagram);
            });
        } else if (diagramType.equals("interface")) {
            editItem.setOnAction(e -> {
                if (activeInterface != null) editInterfaceDiagram(activeInterface);
            });
            deleteItem.setOnAction(e -> {
                if (activeInterface != null) deleteInterfaceDiagram(activeInterface);
            });
        }

        contextMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
    }


    @FXML
    private void handleAddInterface() {
        InterfaceDiagramUI interfaceDiagramUI = new InterfaceDiagramUI(drawingCanvas);
        interfaceDiagram = interfaceDiagramUI.showInterfaceDiagramDialog();

        double newX = 20 + (interfaceDiagrams.size() * 100) % (drawingCanvas.getWidth() - 100);
        double newY = 300 + ((interfaceDiagrams.size() * 100) / (drawingCanvas.getWidth() - 100)) * 100;

        interfaceDiagram.setX(newX);
        interfaceDiagram.setY(newY);

        interfaceDiagrams.add(interfaceDiagram);
        createInterfaceDiagram(interfaceDiagram);
    }


    public void createInterfaceDiagram(InterfaceData interfaceDiagram) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        double x = interfaceDiagram.getX();
        double y = interfaceDiagram.getY();
        double interfaceNameHeight = 40;
        double methodHeight = Math.max(30 * interfaceDiagram.getMethods().size(), 30);

        String stereotypeText = "<<interface>>";
        String interfaceName = interfaceDiagram.getName();
        double maxWidth = calculateTextWidth(stereotypeText, gc);
        maxWidth = Math.max(maxWidth, calculateTextWidth(interfaceName, gc));
        for (MethodData md : interfaceDiagram.getMethods()) {
            String methodText = md.getAccessModifier() + " " + md.getName() + " : " + md.getReturnType();
            maxWidth = Math.max(maxWidth, calculateTextWidth(methodText, gc));
        }

        double width = maxWidth * 1.3;
        double height = interfaceNameHeight + methodHeight;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        gc.strokeLine(x, y + interfaceNameHeight, x + width, y + interfaceNameHeight);

        gc.setFill(Color.BLACK);
        double stereotypeWidth = calculateTextWidth(stereotypeText, gc);
        gc.fillText(stereotypeText, x + (width - stereotypeWidth) / 2, y + 15);

        gc.setFont(Font.font(gc.getFont().getFamily(), Font.getDefault().getSize() + 2));
        double interfaceNameWidth = calculateTextWidth(interfaceName, gc);
        gc.fillText(interfaceName, x + (width - interfaceNameWidth) / 2, y + 35);

        gc.setFont(Font.font(gc.getFont().getFamily(), Font.getDefault().getSize()));

        double methodStartY = y + interfaceNameHeight + 15;
        for (MethodData md : interfaceDiagram.getMethods()) {
            String methodText = md.getAccessModifier() + " " + md.getName() + " : " + md.getReturnType();
            gc.fillText(methodText, x + 10, methodStartY);
            methodStartY += 20;
        }


        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        contextMenu.getItems().addAll(editItem, deleteItem);

    }

    private double calculateTextWidth(String text, GraphicsContext gc) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        return tempText.getLayoutBounds().getWidth();
    }

    private void deleteInterfaceDiagram(InterfaceData interfaceDiagram) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this interface diagram?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            interfaceDiagrams.remove(interfaceDiagram);
            activeDiagram = null;
            redrawCanvas();
        }
    }

    private void editInterfaceDiagram(InterfaceData interfaceDiagram) {
        InterfaceDiagramUI interfaceDiagramUI = new InterfaceDiagramUI(drawingCanvas, interfaceDiagram);
        InterfaceData updatedDiagram = interfaceDiagramUI.showInterfaceDiagramDialog();

        if (updatedDiagram != null) {
            interfaceDiagram.setName(updatedDiagram.getName());
            interfaceDiagram.setMethods(updatedDiagram.getMethods());
            redrawCanvas();
        }
    }

    private boolean isWithinBounds(double mouseX, double mouseY, InterfaceData diagram, GraphicsContext gc) {
        double x = diagram.getX();
        double y = diagram.getY();
        double width = calculateDiagramWidth(diagram, gc);
        double height = calculateDiagramHeight(diagram);
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private double calculateDiagramWidth(InterfaceData diagram, GraphicsContext gc) {
        double maxWidth = calculateTextWidth("<<interface>>", gc);
        maxWidth = Math.max(maxWidth, calculateTextWidth(diagram.getName(), gc));
        for (MethodData method : diagram.getMethods()) {
            String methodText = method.getAccessModifier() + " " + method.getName() + " : " + method.getReturnType();
            maxWidth = Math.max(maxWidth, calculateTextWidth(methodText, gc));
        }
        return maxWidth * 1.3;
    }

    private double calculateDiagramHeight(InterfaceData diagram) {
        double interfaceNameHeight = 40;
        double methodHeight = Math.max(30 * diagram.getMethods().size(), 30);
        return interfaceNameHeight + methodHeight;
    }




    @FXML
    private void handleAddAssociation() {
        System.out.println("Add Association button clicked");
    }

    @FXML
    private void handleAddComposition() {
        System.out.println("Add Composition button clicked");
    }

    // Handles adding an aggregation to the diagram
    @FXML
    private void handleAddAggregation() {
        System.out.println("Add Aggregation button clicked");
        // Logic for adding an aggregation (a line with an empty diamond)
    }

    // Handles setting multiplicity for a relationship
    @FXML
    private void handleSetMultiplicity() {
        System.out.println("Set Multiplicity button clicked");
        // Logic for adding multiplicity to the relationship (e.g., "1", "*", "0..1")
    }



    @FXML
    private void handleNewProject() {
        System.out.println("New Project Clicked");
    }

    @FXML
    private void handleSaveProject() {
        // Create a FileChooser to allow the user to select the save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Start XML document
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<Project>\n");

                // Write class diagrams
                writer.write("    <ClassDiagrams>\n");
                for (ClassDiagram diagram : classDiagrams) {
                    writer.write("        <ClassDiagram>\n");
                    writer.write("            <Name>" + diagram.getName() + "</Name>\n");
                    writer.write("            <Attributes>\n");
                    for (AttributeData attribute : diagram.getAttributes()) {
                        writer.write("                <Attribute>\n");
                        writer.write("                    <AccessModifier>" + attribute.getAccessModifier() + "</AccessModifier>\n");
                        writer.write("                    <DataType>" + attribute.getDataType() + "</DataType>\n");
                        writer.write("                    <Name>" + attribute.getName() + "</Name>\n");
                        writer.write("                </Attribute>\n");
                    }
                    writer.write("            </Attributes>\n");
                    writer.write("            <Methods>\n");
                    for (MethodData method : diagram.getMethods()) {
                        writer.write("                <Method>\n");
                        writer.write("                    <AccessModifier>" + method.getAccessModifier() + "</AccessModifier>\n");
                        writer.write("                    <ReturnType>" + method.getReturnType() + "</ReturnType>\n");
                        writer.write("                    <Name>" + method.getName() + "</Name>\n");
                        writer.write("                </Method>\n");
                    }
                    writer.write("            </Methods>\n");
                    writer.write("        </ClassDiagram>\n");
                }
                writer.write("    </ClassDiagrams>\n");

                // Write interface diagrams
                writer.write("    <InterfaceDiagrams>\n");
                for (InterfaceData diagram : interfaceDiagrams) {
                    writer.write("        <InterfaceDiagram>\n");
                    writer.write("            <Name>" + diagram.getName() + "</Name>\n");
                    writer.write("            <Methods>\n");
                    for (MethodData method : diagram.getMethods()) {
                        writer.write("                <Method>\n");
                        writer.write("                    <AccessModifier>" + method.getAccessModifier() + "</AccessModifier>\n");
                        writer.write("                    <ReturnType>" + method.getReturnType() + "</ReturnType>\n");
                        writer.write("                    <Name>" + method.getName() + "</Name>\n");
                        writer.write("                </Method>\n");
                    }
                    writer.write("            </Methods>\n");
                    writer.write("        </InterfaceDiagram>\n");
                }
                writer.write("    </InterfaceDiagrams>\n");

                // End XML document
                writer.write("</Project>\n");

                showAlert(Alert.AlertType.INFORMATION, "Save Project", "Project saved successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Project", "Failed to save the project.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleOpenProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Project");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try {
                // Clear existing diagrams
                classDiagrams.clear();
                interfaceDiagrams.clear();
                redrawCanvas(); // Clear the canvas

                // Load and parse the XML file
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                document.getDocumentElement().normalize();

                // Load Class Diagrams
                NodeList classDiagramNodes = document.getElementsByTagName("ClassDiagram");
                for (int i = 0; i < classDiagramNodes.getLength(); i++) {
                    Element classElement = (Element) classDiagramNodes.item(i);
                    String className = classElement.getElementsByTagName("Name").item(0).getTextContent();

                    ClassDiagram classDiagram = new ClassDiagram();
                    classDiagram.setName(className);

                    // Load Attributes
                    NodeList attributeNodes = classElement.getElementsByTagName("Attribute");
                    for (int j = 0; j < attributeNodes.getLength(); j++) {
                        Element attributeElement = (Element) attributeNodes.item(j);
                        String accessModifier = attributeElement.getElementsByTagName("AccessModifier").item(0).getTextContent();
                        String dataType = attributeElement.getElementsByTagName("DataType").item(0).getTextContent();
                        String attributeName = attributeElement.getElementsByTagName("Name").item(0).getTextContent();

                        AttributeData attribute = new AttributeData();
                        attribute.setAccessModifier(accessModifier);
                        attribute.setDataType(dataType);
                        attribute.setName(attributeName);
                        classDiagram.getAttributes().add(attribute);
                    }

                    // Load Methods
                    NodeList methodNodes = classElement.getElementsByTagName("Method");
                    for (int j = 0; j < methodNodes.getLength(); j++) {
                        Element methodElement = (Element) methodNodes.item(j);
                        String accessModifier = methodElement.getElementsByTagName("AccessModifier").item(0).getTextContent();
                        String returnType = methodElement.getElementsByTagName("ReturnType").item(0).getTextContent();
                        String methodName = methodElement.getElementsByTagName("Name").item(0).getTextContent();

                        MethodData method = new MethodData();
                        method.setAccessModifier(accessModifier);
                        method.setReturnType(returnType);
                        method.setName(methodName);
                        classDiagram.getMethods().add(method);
                    }


                    // Add the class diagram to the list
                    classDiagrams.add(classDiagram);
                }

                // Load Interface Diagrams
                NodeList interfaceDiagramNodes = document.getElementsByTagName("InterfaceDiagram");
                for (int i = 0; i < interfaceDiagramNodes.getLength(); i++) {
                    Element interfaceElement = (Element) interfaceDiagramNodes.item(i);
                    String interfaceName = interfaceElement.getElementsByTagName("Name").item(0).getTextContent();

                    InterfaceData interfaceDiagram = new InterfaceData();
                    interfaceDiagram.setName(interfaceName);

                    // Load Methods
                    NodeList methodNodes = interfaceElement.getElementsByTagName("Method");
                    for (int j = 0; j < methodNodes.getLength(); j++) {
                        Element methodElement = (Element) methodNodes.item(j);
                        String accessModifier = methodElement.getElementsByTagName("AccessModifier").item(0).getTextContent();
                        String returnType = methodElement.getElementsByTagName("ReturnType").item(0).getTextContent();
                        String methodName = methodElement.getElementsByTagName("Name").item(0).getTextContent();

                        MethodData method = new MethodData();
                        method.setAccessModifier(accessModifier);
                        method.setReturnType(returnType);
                        method.setName(methodName);
                        interfaceDiagram.getMethods().add(method);
                    }

                    // Add the interface diagram to the list
                    interfaceDiagrams.add(interfaceDiagram);
                }

                // Redraw the canvas with the loaded diagrams
                redrawCanvas();
                showAlert(Alert.AlertType.INFORMATION, "Open Project", "Project loaded successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Open Project", "Failed to load the project.");
                e.printStackTrace();
            }
        }
    }

    // Existing methods ...

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Generated Code");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Map to hold class names for checking associations
                Map<String, ClassDiagram> classMap = new HashMap<>();
                for (ClassDiagram diagram : classDiagrams) {
                    classMap.put(diagram.getName(), diagram);
                }

                // Generate Java code for each class diagram
                for (ClassDiagram diagram : classDiagrams) {
                    StringBuilder classCode = new StringBuilder();

                    // Generate class header
                    classCode.append("public class ").append(diagram.getName()).append(" {\n\n");

                    // Generate fields (attributes)
                    for (AttributeData attribute : diagram.getAttributes()) {
                        classCode.append("    ")
                                .append(mapAccessModifier(attribute.getAccessModifier()))
                                .append(" ")
                                .append(attribute.getDataType())
                                .append(" ")
                                .append(attribute.getName())
                                .append("; // Attribute\n");
                    }
                    classCode.append("\n");

                    // Generate methods
                    for (MethodData method : diagram.getMethods()) {
                        classCode.append("    ")
                                .append(mapAccessModifier(method.getAccessModifier()))
                                .append(" ")
                                .append(method.getReturnType())
                                .append(" ")
                                .append(method.getName())
                                .append("() {\n")
                                .append("        // TODO: Add method implementation\n")
                                .append("    }\n\n");
                    }

                    // Check for associations (based on attribute data types)
                    for (AttributeData attribute : diagram.getAttributes()) {
                        if (classMap.containsKey(attribute.getDataType())) {
                            classCode.append("    // Association: ").append(diagram.getName())
                                    .append(" has a reference to ").append(attribute.getDataType()).append("\n");
                        }
                    }

                    classCode.append("}\n\n");
                    writer.write(classCode.toString());
                }

                showAlert(Alert.AlertType.INFORMATION, "Generate Code", "Code generated successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Generate Code", "Failed to save the code.");
                e.printStackTrace();
            }
        }
    }

    private String mapAccessModifier(String accessModifier) {
        switch (accessModifier) {
            case "private":
                return "private";
            case "protected":
                return "protected";
            case "public":
                return "public";
            default:
                return ""; // Default to package-private
        }
    }

    @FXML


    private void handleExportDiagram() {
        try {
            // Step 1: Take a snapshot of the canvas
            WritableImage snapshot = drawingCanvas.snapshot(null, null);

            // Step 2: Open a FileChooser for saving the image
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Diagram");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG Files", "*.jpg"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

            // Step 3: Show save dialog to get the destination file
            File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
            if (file != null) {
                // Step 4: Write the snapshot to the file
                String extension = getFileExtension(file.getName());
                if (extension.equals("jpg") || extension.equals("png")) {
                    ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), extension, file);
                    System.out.println("Diagram exported successfully to: " + file.getAbsolutePath());
                } else {
                    showError("Invalid File Type", "Please save the file with .jpg or .png extension.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Export Failed", "An error occurred while exporting the diagram: " + e.getMessage());
        }
    }

    // Helper Method: Get file extension
    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1).toLowerCase();
    }

    // Helper Method: Show error dialog
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
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
