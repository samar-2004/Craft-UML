package org.example.craftuml.models.UseCaseDiagrams;

import javafx.scene.text.Text;

/**
 * Represents a relationship between two use cases in a use case diagram.
 * This class models the relationship type (such as "extends", "includes") between two functionalities or requirements
 * in the system, which are represented as {@link UseCase} objects. The relationship helps define how the two use cases
 * interact or depend on each other, providing insight into system behavior.
 *
 * The relationship type can describe various scenarios such as:
 * <ul>
 *     <li>"extends": One use case extends the functionality of another.</li>
 *     <li>"includes": One use case includes the functionality of another.</li>
 * </ul>
 * This class allows for getting and setting the related use cases and their relationship type.
 */
public class UseCaseToUseCaseRelation {
    /**
     * The first use case involved in the relationship.
     * This represents one of the system's functionalities or requirements that interacts with the second use case.
     */
    private UseCase useCase1;

    /**
     * The second use case involved in the relationship.
     * This represents another functionality or requirement that is related to the first use case.
     */
    private UseCase useCase2;

    /**
     * The type of relationship between the two use cases.
     * This can specify the nature of the connection, such as "extends", "includes", or other types of relations.
     */
    private String relationType;

    /**
     * Constructs a new UseCaseToUseCaseRelation with the specified use cases and relation type.
     *
     * @param useCase1 The first use case involved in the relationship.
     * @param useCase2 The second use case involved in the relationship.
     * @param relationType The type of relationship between the two use cases (e.g., "extends", "includes").
     */
    public UseCaseToUseCaseRelation(UseCase useCase1, UseCase useCase2, String relationType) {
        this.useCase1 = useCase1;
        this.useCase2 = useCase2;
        this.relationType = relationType;
    }

    /**
     * Gets the first use case in the relationship.
     *
     * @return The first {@link UseCase} involved in the relationship.
     */
    public UseCase getUseCase1() {
        return useCase1;
    }

    /**
     * Sets the first use case in the relationship.
     *
     * @param useCase1 The first {@link UseCase} to set.
     */
    public void setUseCase1(UseCase useCase1) {
        this.useCase1 = useCase1;
    }

    /**
     * Gets the second use case in the relationship.
     *
     * @return The second {@link UseCase} involved in the relationship.
     */
    public UseCase getUseCase2() {
        return useCase2;
    }

    /**
     * Sets the second use case in the relationship.
     *
     * @param useCase2 The second {@link UseCase} to set.
     */

    public void setUseCase2(UseCase useCase2) {
        this.useCase2 = useCase2;
    }

    /**
     * Gets the type of relationship between the two use cases.
     *
     * @return A {@link String} representing the type of relationship (e.g., "extends", "includes").
     */
    public String getRelationType() {
        return relationType;
    }

    /**
     * Sets the type of relationship between the two use cases.
     *
     * @param relationType A {@link String} representing the type of relationship to set.
     */
    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    /**
     * Updates the relationship type based on the provided {@link Text} object.
     * This method can be used to change the relation type dynamically.
     *
     * @param relation The {@link Text} object containing the new relation type.
     */
    public void addRelation(Text relation){
        relationType = relation.getText();
    }
}

