package org.example.craftuml.models.ClassDiagrams;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.craftuml.models.DiagramComponent;
import org.example.craftuml.models.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class diagram, which is a visual representation of a class in the Unified Modeling Language (UML).
 * The class diagram contains the class's name, attributes (fields), methods, relationships with other classes,
 * and visual properties such as position, width, and height. It also provides functionality to manipulate the
 * class's attributes, methods, and relationships.
 * <p>
 * This class implements the {@link DiagramComponent} interface and can be used to create and manage class
 * diagrams in a graphical interface, such as for use in a UML diagramming tool.
 * </p>
 */

public class ClassDiagram implements DiagramComponent {
    /**
     * A list of the attributes (fields) of the class.
     */
    private List<AttributeData> attributes;

    /**
     * A list of the methods of the class.
     */
    private List<MethodData> methods;

    /**
     * The x-coordinate of the position of the class diagram on the canvas.
     */
    private double x;

    /**
     * The y-coordinate of the position of the class diagram on the canvas.
     */
    private double y;

    /**
     * The height of the class diagram.
     */
    private double height;

    /**
     * The width of the class diagram.
     */
    private double width;

    /**
     * A constant padding value used for spacing within the class diagram.
     */
    private static final double PADDING = 10;

    /**
     * A constant font used for rendering text in the class diagram.
     */
    private static final Font FONT = Font.font("Arial", 12);

    /**
     * A rectangle that represents the boundaries of the class diagram on the canvas.
     */
    private Rectangle diagramRectangle;

    /**
     * A list of relationships associated with the class diagram.
     */
    private List<Relationship> relationships = new ArrayList<>();

    /**
     * A property holding the name of the interface. This property can be observed for changes
     * and updated dynamically. Use {@link #nameProperty()} to access this property.
     */
    private final StringProperty name = new SimpleStringProperty();


    /**
     * Constructor to initialize a class diagram with the specified name, x and y position.
     *
     * @param name The name of the class diagram.
     * @param x The x-coordinate for the class diagram's position on the canvas.
     * @param y The y-coordinate for the class diagram's position on the canvas.
     */
    public ClassDiagram(String name, double x, double y) {
        setName(name);
        this.x = x;
        this.y = y;
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    /**
     * Default constructor to initialize an empty class diagram with no attributes or methods.
     */
    public ClassDiagram()
    {
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    /**
     * Gets the name of the class diagram.
     *
     * @return The name of the class diagram.
     */
    public String getName() {
        return name.get();
    }

    /**
     * Sets the name of the class diagram. The name cannot be empty, null, or contain spaces.
     *
     * @param name The name to set for the class diagram.
     * @throws IllegalArgumentException If the provided name is empty, null, or contains spaces.
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty() || name.contains(" ")) {
            throw new IllegalArgumentException("Class name cannot be empty or contain spaces.");
        }
        this.name.set(name);
    }

    /**
     * Returns the name property of the interface.
     * This method allows for property bindings and listeners.
     *
     * @return The StringProperty representing the name of the interface.
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Sets the rectangle that represents the boundaries of the class diagram.
     *
     * @param rect The Rectangle to set for the class diagram.
     */
    public void setRectangle(Rectangle rect) {
        this.diagramRectangle = rect;
    }

    /**
     * Gets the rectangle that represents the boundaries of the class diagram.
     *
     * @return The Rectangle representing the boundaries of the class diagram.
     */
    public Rectangle getRectangle() {
        return this.diagramRectangle;
    }

    /**
     * Sets the list of attributes for the class diagram. The list cannot be null.
     *
     * @param attributes The list of attributes to set for the class diagram.
     * @throws IllegalArgumentException If the provided attributes list is null.
     */

    public void setAttributes(List<AttributeData> attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Attributes list cannot be null.");
        }
        this.attributes = new ArrayList<>(attributes);
    }

    /**
     * Sets the list of methods for the class diagram. The list cannot be null.
     *
     * @param methods The list of methods to set for the class diagram.
     * @throws IllegalArgumentException If the provided methods list is null.
     */
    public void setMethods(List<MethodData> methods) {
        if (methods == null) {
            throw new IllegalArgumentException("Methods list cannot be null.");
        }
        this.methods = new ArrayList<>(methods);
    }

    /**
     * Adds an attribute to the class diagram's list of attributes.
     *
     * @param ad The AttributeData to add to the class diagram.
     */
    public void addAttributes(AttributeData ad) {
        attributes.add(ad);
    }

    /**
     * Adds a method to the class diagram's list of methods.
     *
     * @param md The MethodData to add to the class diagram.
     */
    public void addMethods(MethodData md) {
        methods.add(md);
    }

    /**
     * Gets the list of attributes associated with the class diagram.
     *
     * @return A list of AttributeData representing the attributes of the class diagram.
     */
    public List<AttributeData> getAttributes() {
        return attributes;
    }

    /**
            * Gets the list of methods associated with the class diagram.
            *
            * @return A list of MethodData representing the methods of the class diagram.
            */
    public List<MethodData> getMethods() {
        return methods;
    }

    /**
     * Gets the x-coordinate of the class diagram.
     *
     * @return The x-coordinate of the class diagram.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the class diagram.
     *
     * @return The y-coordinate of the class diagram.
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the x-coordinate of the class diagram.
     *
     * @param x The x-coordinate to set for the class diagram.
     */
    public void setX(double x)
    {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of the class diagram.
     *
     * @param y The y-coordinate to set for the class diagram.
     */
    public void setY(double y)
    {
        this.y = y;
    }

    /**
     * Sets the position of the class diagram by specifying both the x and y coordinates.
     * This method implements the setPosition method from the DiagramComponent interface.
     *
     * @param x The x-coordinate to set for the class diagram.
     * @param y The y-coordinate to set for the class diagram.
     */
    @Override
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the width of the class diagram.
     *
     * @param width The width to set for the class diagram.
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Sets the height of the class diagram.
     *
     * @param height The height to set for the class diagram.
     */
    public void setHeight(double height) {
        this.height = height;
    }


    /**
     * Gets the width of the class diagram.
     *
     * @return The width of the class diagram.
     */
    public double getWidth() {
       return this.width;
    }

    /**
     * Gets the height of the class diagram.
     *
     * @return The height of the class diagram.
     */
    public double getHeight() {
        return this.height;
    }
}
