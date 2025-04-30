package org.example.craftuml.models.ClassDiagrams;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.craftuml.models.DiagramComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an interface diagram component in a UML model.
 * This class holds information about the interface's name, its methods,
 * and its position and size on a canvas. It provides methods for getting
 * and setting these properties, including ensuring that the interface name
 * is valid (non-null, non-empty, and does not contain spaces).
 * It implements the {@link DiagramComponent} interface, allowing it to
 * be positioned on a canvas and interact with other diagram components.
 */

public class InterfaceData implements DiagramComponent {
    /**
     * A list of the methods of the interface, represented by {@link MethodData} objects.
     * This list contains all the methods defined for the interface.
     */
    private List<MethodData> methods;

    /**
     * The x-coordinate of the position of the interface diagram on the canvas.
     * This value is used to determine where the diagram is placed horizontally.
     */
    private double x;

    /**
     * The y-coordinate of the position of the interface diagram on the canvas.
     * This value is used to determine where the diagram is placed vertically.
     */
    private double y;

    /**
     * The height of the interface diagram. This value represents the vertical size of the diagram's bounding box.
     */
    private double height;

    /**
     * The width of the interface diagram. This value represents the horizontal size of the diagram's bounding box.
     */
    private double width;

    /**
     * A constant padding value used for spacing within the interface diagram, ensuring that elements do not overlap.
     */
    private static final double PADDING = 10;

    /**
     * A constant font used for rendering text in the interface diagram. This font is applied to text elements like the interface name.
     */
    private static final Font FONT = Font.font("Arial", 12);

    /**
     * A property that holds the name of the interface diagram. This is used for data binding and dynamic updates.
     */
    private final StringProperty name = new SimpleStringProperty();

    /**
     * Default constructor to initialize an empty InterfaceData object with an empty list of methods.
     * The name property is not initialized here, and must be set separately.
     */
    public InterfaceData()
    {
        this.methods = new ArrayList<>();
    }

    /**
     * Gets the name of the interface.
     *
     * @return The name of the interface as a {@link String}.
     */
    public String getName() {
        return name.get();
    }


    /**
     * Sets the name of the interface. The name cannot be empty, null, or contain spaces.
     *
     * @param name The name to set for the interface.
     * @throws IllegalArgumentException If the provided name is empty, null, or contains spaces.
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty() || name.contains(" ")) {
            throw new IllegalArgumentException("Class name cannot be empty or contain spaces.");
        }
        this.name.set(name);
    }

    /**
     * Returns the name property of the interface. This allows for data binding with JavaFX components.
     *
     * @return The {@link StringProperty} representing the name of the interface.
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Sets the position of the interface by specifying both the x and y coordinates.
     * This method implements the setPosition method from the DiagramComponent interface.
     *
     * @param x The x-coordinate to set for the interface.
     * @param y The y-coordinate to set for the interface.
     */
    @Override
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate of the interface.
     *
     * @return The x-coordinate of the interface.
     */
    @Override
    public double getX() {
        return this.x;
    }

    /**
     * Gets the y-coordinate of the interface.
     *
     * @return The y-coordinate of the interface.
     */
    @Override
    public double getY() {
        return this.y;
    }

    /**
     * Gets the list of methods associated with the interface.
     *
     * @return A list of {@link MethodData} representing the methods of the interface.
     */
    public List<MethodData> getMethods() {
        return methods;
    }

    /**
     * Sets the list of methods for the interface.
     *
     * @param methods The list of methods to set for the interface.
     */
    public void setMethods(List<MethodData> methods) {
        this.methods = methods;
    }

    /**
     * Sets the x-coordinate of the interface.
     *
     * @param newX The new x-coordinate to set for the interface.
     */
    public void setX(double newX) {
        this.x = newX;
    }

    /**
     * Sets the y-coordinate of the interface.
     *
     * @param newY The new y-coordinate to set for the interface.
     */
    public void setY(double newY) {
        this.y = newY;
    }

    /**
     * Sets the width of the interface.
     *
     * @param width The width to set for the interface.
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Sets the height of the interface.
     *
     * @param height The height to set for the interface.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Gets the width of the interface.
     *
     * @return The width of the interface.
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Gets the height of the interface.
     *
     * @return The height of the interface.
     */
    public double getHeight() {
        return this.height;
    }

}