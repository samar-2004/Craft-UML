package org.example.craftuml.models.ClassDiagrams;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the data for a method within a class or interface.
 * This class contains the method's access modifier, name, and return type.
 * It provides methods for getting and setting these attributes, as well as constructors
 * for initializing a method's data.
 * <p>
 * The class supports serialization and deserialization through JSON, as indicated by
 * the use of the {@link JsonProperty} annotation.
 * </p>
 */

public class MethodData {
    /**
     * The access modifier of the method (e.g., public, private, protected).
     */
    private String accessModifier;

    /**
     * The name of the method.
     */
    private String name;

    /**
     * The return type of the method (e.g., String, int, void).
     */
    private String returnType;

    /**
     * Default constructor for the MethodData class.
     * Initializes a MethodData object with no specified access modifier, name, or return type.
     */
    public MethodData() {
    }

    /**
     * Constructor to initialize a MethodData object with the specified access modifier, method name, and return type.
     *
     * @param accessModifier The access modifier for the method (e.g., public, private).
     * @param name The name of the method.
     * @param returnType The return type of the method (e.g., String, void).
     */
    public MethodData(String accessModifier, String name, String returnType) {
        this.accessModifier = accessModifier;
        this.name = name;
        this.returnType = returnType;
    }

    /**
     * Gets the access modifier of the method.
     *
     * @return The access modifier of the method (e.g., public, private).
     */
    public String getAccessModifier() {
        return accessModifier;
    }

    /**
     * Sets the access modifier of the method.
     *
     * @param accessModifier The access modifier to set for the method (e.g., public, private).
     */
    public void setAccessModifier(String accessModifier) {
        this.accessModifier = accessModifier;
    }

    /**
     * Gets the name of the method.
     *
     * @return The name of the method as a String.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the method. The name cannot be null or empty.
     *
     * @param name The name to set for the method.
     * @throws IllegalArgumentException If the provided name is null or empty.
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }
        this.name = name;
    }

    /**
     * Gets the return type of the method.
     *
     * @return The return type of the method as a String.
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Sets the return type of the method.
     *
     * @param returnType The return type to set for the method.
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
