package org.example.craftuml.models;

/**
 * Represents a generic component in a diagram. Any element that can be part of a diagram, such as actors, use cases,
 * or relationships, should implement this interface. This interface provides basic functionality for setting and
 * retrieving the name and position (x and y coordinates) of a diagram component.
 *
 * Implementing classes should provide concrete implementations of the methods to handle specific components,
 * but they must support the ability to:
 * <ul>
 *     <li>Get and set the name of the component.</li>
 *     <li>Set and retrieve the position (x, y) of the component on the diagram.</li>
 * </ul>
 */
public interface DiagramComponent {
    /**
     * Gets the name of the diagram component.
     *
     * @return A {@link String} representing the name of the component.
     */
    String getName();
    /**
     * Sets the name of the diagram component.
     *
     * @param name The name to set for the component.
     */
    void setName(String name);
    /**
     * Sets the position of the diagram component on the canvas.
     *
     * @param x The x-coordinate of the component's position.
     * @param y The y-coordinate of the component's position.
     */
    void setPosition(double x, double y);
    /**
     * Gets the x-coordinate of the diagram component's position.
     *
     * @return The x-coordinate of the component.
     */
    double getX();
    /**
     * Gets the y-coordinate of the diagram component's position.
     *
     * @return The y-coordinate of the component.
     */
    double getY();
}
