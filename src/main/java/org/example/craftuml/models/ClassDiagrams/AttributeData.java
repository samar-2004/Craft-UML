package org.example.craftuml.models.ClassDiagrams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an attribute in a class diagram, encapsulating its access modifier, name, and data type.
 *
 * <p>This class is primarily used for modeling attributes in class diagrams, providing metadata
 * about the attribute's visibility, identifier, and type. It includes a no-argument constructor
 * for default initialization and another constructor for setting all fields.</p>
 */
public class AttributeData {

    private String accessModifier; // The visibility of the attribute (e.g., "private", "public").
    private String name;           // The name of the attribute.
    private String dataType;       // The data type of the attribute (e.g., "String", "int").

    /**
     * Default constructor for creating an instance of {@code AttributeData}.
     *
     * <p>Initializes a new {@code AttributeData} object with default values.</p>
     */
    public AttributeData() {
    }

    /**
     * Constructs an {@code AttributeData} object with specified access modifier, name, and data type.
     *
     * @param accessModifier the visibility of the attribute (e.g., "private", "public").
     * @param name the name of the attribute.
     * @param dataType the data type of the attribute (e.g., "String", "int").
     */
    public AttributeData(String accessModifier, String name, String dataType) {
        this.accessModifier = accessModifier;
        this.name = name;
        this.dataType = dataType;
    }

    /**
     * Retrieves the access modifier of the attribute.
     *
     * @return the access modifier (e.g., "private", "public").
     */
    public String getAccessModifier() {
        return accessModifier;
    }

    /**
     * Sets the access modifier of the attribute.
     *
     * @param accessModifier the new access modifier (e.g., "private", "public").
     */
    public void setAccessModifier(String accessModifier) {
        this.accessModifier = accessModifier;
    }

    /**
     * Retrieves the name of the attribute.
     *
     * @return the name of the attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the attribute.
     *
     * @param name the new name of the attribute.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the data type of the attribute.
     *
     * @return the data type of the attribute (e.g., "String", "int").
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type of the attribute.
     *
     * @param dataType the new data type of the attribute (e.g., "String", "int").
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
