package org.example.craftuml.models.UseCaseDiagrams;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.craftuml.models.DiagramComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Use Case Diagram which visually illustrates the use cases and their relationships
 * in a system. The diagram includes actors (users or external systems), use cases (functionalities),
 * associations between actors and use cases, and relations between use cases (such as "extends" or
 * "includes").
 *
 * This class provides methods to add and remove use cases, actors, and relationships, as well as
 * configure the diagram's position and dimensions on a canvas. It also provides helper methods for
 * finding specific actors and use cases by name.
 *
 * The diagram is represented in a 2D space with coordinates (x, y) for its position and width/height
 * values for its size. The class is designed for integration with JavaFX, as indicated by the use of
 * `GraphicsContext` for potential rendering operations.
 *
 * Use Case Diagrams are essential in UML (Unified Modeling Language) to visually represent the
 * interaction between users and the system's functionality.
 */
public class UseCaseDiagram {
    /**
     * The name of the use case diagram. This is typically a label that identifies the diagram.
     */
    private String name;

    /**
     * A list of {@link UseCase} objects representing the use cases in the diagram.
     * Use cases represent the functional requirements of the system.
     */
    private List<UseCase> useCases;

    /**
     * The width of the use case diagram. This value defines how wide the diagram is on the canvas.
     * Default value is 700.
     */
    private double width = 700;

    /**
     * A list of {@link Association} objects representing the associations between actors and use cases.
     * These associations depict interactions between system users (actors) and system functionality (use cases).
     */
    private List<Association> associations;

    /**
     * A list of {@link UseCaseToUseCaseRelation} objects representing the relationships between use cases.
     * This can represent dependencies, extensions, or inclusions between use cases.
     */
    private List<UseCaseToUseCaseRelation> useCaseRelations;

    /**
     * The x-coordinate for the position of the use case diagram on the canvas.
     * This value defines where the diagram starts horizontally.
     */
    private double x;

    /**
     * The y-coordinate for the position of the use case diagram on the canvas.
     * This value defines where the diagram starts vertically.
     */
    private double y;

    /**
     * The height of the use case diagram. This value determines how tall the diagram is on the canvas.
     * Default value is 700.
     */
    private double height = 700;

    /**
     * A list of {@link Actor} objects representing the actors in the use case diagram.
     * Actors are external entities (such as users or systems) that interact with the system.
     */
    private List<Actor> actors;



    /**
     * Gets the height of the use case diagram.
     *
     * @return The height of the use case diagram.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the height of the use case diagram.
     * This value defines how tall the diagram is on the canvas.
     *
     * @param height The new height of the use case diagram.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Gets the width of the use case diagram.
     *
     * @return The width of the use case diagram.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the width of the use case diagram.
     * This value defines how wide the diagram is on the canvas.
     *
     * @param width The new width of the use case diagram.
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Sets the list of use cases for the diagram.
     * This list defines all the use cases that are part of the diagram.
     *
     * @param useCases The list of use cases to set.
     */
    public void setUseCases(List<UseCase> useCases) {
        this.useCases = useCases;
    }

    /**
     * Sets the list of actors for the diagram.
     * This list defines all the actors that interact with the use cases in the diagram.
     *
     * @param actors The list of actors to set.
     */
    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    /**
     * Gets the x-coordinate of the use case diagram on the canvas.
     *
     * @return The x-coordinate of the use case diagram.
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of the use case diagram on the canvas.
     * This value defines where the diagram starts horizontally.
     *
     * @param x The new x-coordinate for the diagram.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets the y-coordinate of the use case diagram on the canvas.
     *
     * @return The y-coordinate of the use case diagram.
     */
    public double getY() {
        return y;
    }


    /**
     * Sets the y-coordinate of the use case diagram on the canvas.
     * This value defines where the diagram starts vertically.
     *
     * @param y The new y-coordinate for the diagram.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Constructor to initialize a UseCaseDiagram with a specific name and coordinates (x, y).
     * Initializes lists for actors, use cases, use case relations, and associations.
     *
     * @param name The name of the use case diagram.
     * @param x The x-coordinate of the use case diagram's position.
     * @param y The y-coordinate of the use case diagram's position.
     */
    public UseCaseDiagram(String name, double x, double y) {
        setName(name);
        this.x = x;
        this.y = y;
        this.actors = new ArrayList<>();
        useCases = new ArrayList<>();
        useCaseRelations = new ArrayList<>();
        associations = new ArrayList<>();
    }

    /**
     * Default constructor that initializes lists for use cases, actors, use case relations, and associations.
     */
    public UseCaseDiagram() {
        useCases = new ArrayList<>();
        actors = new ArrayList<>();
        useCaseRelations = new ArrayList<>();
        associations = new ArrayList<>();
    }

    /**
     * Adds a use case to the list of use cases in the diagram.
     *
     * @param useCase The use case to add to the diagram.
     */
    public void addUseCase(UseCase useCase) {
        useCases.add(useCase); // Assuming useCases is a list of UseCase objects
    }

    /**
     * Removes a specific use case from the list of use cases in the diagram.
     *
     * @param useCase The use case to remove from the diagram.
     */
    public void removeUseCase(UseCase useCase) {
        useCases.remove(useCase);
    }

    /**
     * Adds a new actor to the list of actors in the diagram.
     * The actor is created with the specified name.
     *
     * @param actorName The name of the actor to add.
     * @throws IllegalArgumentException If the actor name is null or empty.
     */
    public void addActor(String actorName) {
        if (actorName == null || actorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Actor name cannot be null or empty");
        }
        actors.add(new Actor(actorName));
    }

    /**
     * Removes an actor from the list of actors in the diagram based on the specified actor's name.
     *
     * @param actorName The name of the actor to remove.
     */
    public void removeActor(String actorName) {
        Actor actor = findActorByName(actorName);
        if (actor != null) {
            actors.remove(actor);
        }
    }

    /**
     * Adds a use case relation to the list of use case relations in the diagram.
     * Optionally, it can update the diagram to reflect the changes.
     *
     * @param relation The use case relation to add to the diagram.
     */
    public void addUseCaseRelation(UseCaseToUseCaseRelation relation) {
        // Add the relation to the list of relations
        useCaseRelations.add(relation);

        // Optionally, you can update the diagram or call any other method to refresh the view
        updateDiagram();
    }

    /**
     * Gets the list of associations in the diagram.
     *
     * @return A list of all associations in the diagram.
     */
    public List<Association> getAssociations(){
        return associations;
    }

    /**
     * Gets the list of use case relations in the diagram.
     *
     * @return A list of all use case relations in the diagram.
     */
    public List<UseCaseToUseCaseRelation> getUseCaseRelations() {return  useCaseRelations;}

    /**
     * Adds a use case relation between two use cases with a specified relation type.
     * This method checks if the relation already exists before adding it to the diagram.
     * Optionally, it can update the diagram to reflect the changes.
     *
     * @param useCase1 The first use case in the relation.
     * @param useCase2 The second use case in the relation.
     * @param relationType The type of the relation (e.g., "extends", "includes").
     */
    public void addUseCaseRelation(UseCase useCase1, UseCase useCase2, String relationType) {
        if (!isUseCaseRelationExists(useCase1, useCase2, relationType)) {
            UseCaseToUseCaseRelation relation = new UseCaseToUseCaseRelation(useCase1, useCase2, relationType);
            useCaseRelations.add(relation);
            // Optionally, update the diagram view if needed
            updateDiagram();
        } else {
            System.out.println("Relation already exists!");
        }
    }

    /**
     * Indicates that the instance is a use case diagram.
     *
     * @return true, as the instance is a use case diagram.
     */
    public boolean isUseCaseDiagram() {
        return true; // This indicates the instance is a use case diagram
    }

    /**
     * Checks whether a specific use case relation exists between two use cases.
     *
     * @param useCase1 The first use case in the relation.
     * @param useCase2 The second use case in the relation.
     * @param relationType The type of the relation (e.g., "extends", "includes").
     * @return true if the relation already exists, false otherwise.
     */
    private boolean isUseCaseRelationExists(UseCase useCase1, UseCase useCase2, String relationType) {
        for (UseCaseToUseCaseRelation relation : useCaseRelations) {
            if (relation.getUseCase1().equals(useCase1.getName()) &&
                    relation.getUseCase2().equals(useCase2.getName()) &&
                    relation.getRelationType().equals(relationType)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Removes a specific use case relation from the diagram based on the use case names and relation type.
     * Optionally, it can update the diagram to reflect the changes.
     *
     * @param useCase1 The first use case in the relation.
     * @param useCase2 The second use case in the relation.
     * @param relationType The type of the relation to remove.
     */
    public void removeUseCaseRelation(UseCase useCase1, UseCase useCase2, String relationType) {
        UseCaseToUseCaseRelation relationToRemove = null;
        for (UseCaseToUseCaseRelation relation : useCaseRelations) {
            if (relation.getUseCase1().equals(useCase1.getName()) &&
                    relation.getUseCase2().equals(useCase2.getName()) &&
                    relation.getRelationType().equals(relationType)) {
                relationToRemove = relation;
                break;
            }
        }

        if (relationToRemove != null) {
            useCaseRelations.remove(relationToRemove);
            // Optionally, update the diagram view if needed
            updateDiagram();
        } else {
            System.out.println("Relation not found!");
        }
    }

    /**
     * Gets all the use case relations in the diagram.
     *
     * @return A list of all use case relations in the diagram.
     */
    public List<UseCaseToUseCaseRelation> getAllUseCaseRelations() {
        return useCaseRelations;
    }


    /**
     * Updates the diagram to reflect any changes, such as adding or removing relations.
     * This method may involve redrawing the entire diagram.
     * (For optimization, the redrawing process can be customized.)
     */
    private void updateDiagram() {
        // Redraw the entire diagram (this can be more optimized based on your needs)
//        clearCanvas();
//        drawUseCaseRelations(drawingCanvas.getGraphicsContext2D());
//        drawActorsAndUseCases();
    }


    /**
     * Gets a list of all use cases in the diagram.
     * The list is returned as a new ArrayList to avoid external modification of the original list.
     *
     * @return A list of all use cases in the diagram.
     */
    public List<UseCase> getUseCases() {
        return new ArrayList<>(useCases);
    }

    /**
     * Gets a list of all actors in the diagram.
     * The list is returned as a new ArrayList to avoid external modification of the original list.
     *
     * @return A list of all actors in the diagram.
     */
    public List<Actor> getActors() {
        return new ArrayList<>(actors);  // Return a copy to avoid external modification
    }


    /**
     * Helper method to find an actor by its name.
     * Searches the list of actors and returns the actor that matches the given name.
     *
     * @param name The name of the actor to search for.
     * @return The {@link Actor} object if found, otherwise {@code null}.
     */
    private Actor findActorByName(String name) {
        for (Actor actor : actors) {
            if (actor.getName().equals(name)) {
                return actor;
            }
        }
        return null;
    }

    /**
     * Helper method to find a use case by its name.
     * Searches the list of use cases and returns the use case that matches the given name.
     *
     * @param name The name of the use case to search for.
     * @return The {@link UseCase} object if found, otherwise {@code null}.
     */
    private UseCase findUseCaseByName(String name) {
        for (UseCase useCase : useCases) {
            if (useCase.getName().equals(name)) {
                return useCase;
            }
        }
        return null;
    }


    /**
     * Gets the name of the use case diagram.
     *
     * @return The name of the diagram.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the use case diagram.
     *
     * @param name The name to set for the diagram.
     */
    public void setName(String name) {
        this.name = name;
    }
}
