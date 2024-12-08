package org.example.craftuml.Business;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.craftuml.models.UseCaseDiagrams.Actor;

import java.util.List;

/**
 * The `ActorManager` class manages a collection of `Actor` objects in a use case diagram.
 * It provides methods to add, update, draw, and interact with actors on a canvas,
 * including functionality to check for duplicate names and ensure actors remain within canvas boundaries.
 */
public class ActorManager {

    /**
     * A list of `Actor` objects representing the actors in the use case diagram.
     * Used to manage and store the actors for the diagram.
     */
    private List<Actor> actors;

    /**
     * Constructor to initialize the ActorManager with a list of actors.
     *
     * @param actors A list of `Actor` objects to be managed.
     */
    public ActorManager(List<Actor> actors) {
        this.actors = actors;
    }

    /**
     * Draws an actor on the provided graphics context.
     * The actor is drawn using basic shapes, including a head (circle), body (line), arms (line), and legs (lines).
     * The actor's name is displayed below the figure.
     *
     * @param gc The `GraphicsContext` used for drawing.
     * @param actor The `Actor` to be drawn.
     */
    public void drawActor(GraphicsContext gc, Actor actor) {
        // Adjust the size for the actor
        double headSize = 20;  // Smaller head size
        double bodyHeight = 25; // Shorter body
        double armLength = 20;  // Shorter arms
        double legLength = 30;  // Shorter legs

        // Draw the actor's head (outline of the circle, no fill)
        gc.setStroke(Color.BLACK);  // Set stroke color to black for the outline
        gc.setLineWidth(2);  // Optional: Adjust line width for a clearer outline
        gc.strokeOval(actor.getX(), actor.getY(), headSize, headSize);  // Head as an outline circle

        // Draw the actor's body (line)
        gc.strokeLine(actor.getX() + headSize / 2, actor.getY() + headSize, actor.getX() + headSize / 2, actor.getY() + headSize + bodyHeight); // Body

        // Draw the actor's arms (lines)
        gc.strokeLine(actor.getX(), actor.getY() + headSize + 10, actor.getX() + headSize, actor.getY() + headSize + 10); // Arms

        // Draw the actor's legs (lines)
        gc.strokeLine(actor.getX() + headSize / 2, actor.getY() + headSize + bodyHeight, actor.getX(), actor.getY() + headSize + bodyHeight + legLength); // Left leg
        gc.strokeLine(actor.getX() + headSize / 2, actor.getY() + headSize + bodyHeight, actor.getX() + headSize, actor.getY() + headSize + bodyHeight + legLength); // Right leg

        // Draw the actor's name below the figure
        gc.setFill(Color.BLACK);  // Fill color for the name text
        gc.fillText(actor.getName(), actor.getX() + 5, actor.getY() + headSize + bodyHeight + legLength + 10);  // Name below the actor
    }

    /**
     * Finds an actor by their position (x, y) coordinates.
     *
     * @param x The x-coordinate to check.
     * @param y The y-coordinate to check.
     * @return The `Actor` object at the specified position, or `null` if no actor is found.
     */
    public Actor findActorByPosition(double x, double y) {
        for (Actor actor : actors) {
            if (x >= actor.getX() && x <= actor.getX() + actor.getWidth() &&
                    y >= actor.getY() && y <= actor.getY() + actor.getHeight()) {
                return actor;
            }
        }
        return null;
    }

    /**
     * Checks if there is a duplicate actor name excluding the given actor.
     *
     * @param newName The new name to check.
     * @param excludedActor The actor to exclude from the check.
     * @return `true` if there is a duplicate name, `false` otherwise.
     */
    public boolean isDuplicateNameExcludingActor(String newName, Actor excludedActor) {
        return actors.stream()
                .anyMatch(existingActor -> existingActor.getName().equalsIgnoreCase(newName) && existingActor != excludedActor);
    }

    /**
     * Updates the name of the given actor.
     *
     * @param actor The actor whose name is to be updated.
     * @param newName The new name to set for the actor.
     */
    public void updateActorName(Actor actor, String newName) {
        actor.setName(newName);
    }

    /**
     * Checks if an actor with the given name already exists.
     *
     * @param actorName The name of the actor to check.
     * @return `true` if an actor with the same name exists, `false` otherwise.
     */
    public boolean isDuplicateActorName(String actorName) {
        return actors.stream().anyMatch(actor -> actor.getName().equalsIgnoreCase(actorName));
    }

    /**
     * Adds a new actor with the specified name to the list of actors.
     * The actor's position is calculated based on the current number of actors.
     *
     * @param actorName The name of the new actor to be added.
     */
    public void addActor(String actorName) {
        Actor actor = new Actor(actorName);

        // Position calculation (modify as needed)
        double x = (actors.size() + 1) * 50; // Example positioning logic
        double y = 100 + (actors.size() + 1) * 30;

        actor.setX(x);
        actor.setY(y);

        actors.add(actor);
    }

    /**
     * Checks if a given point (x, y) is hovering over any actor within the list of actors.
     *
     * @param x The x-coordinate of the point to check.
     * @param y The y-coordinate of the point to check.
     * @param actors A list of `Actor` objects to check against.
     * @return `true` if the point is over any actor, `false` otherwise.
     */
    public static boolean isHoveringOverActor(double x, double y, List<Actor> actors) {
        for (Actor actor : actors) {
            if (x >= actor.getX() && x <= actor.getX() + actor.getWidth() &&
                    y >= actor.getY() && y <= actor.getY() + actor.getHeight()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the actor located at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate to check.
     * @param y The y-coordinate to check.
     * @param actors A list of `Actor` objects to search through.
     * @return The `Actor` at the specified position, or `null` if no actor is found.
     */
    public static Actor getClickedActor(double x, double y, List<Actor> actors) {
        for (Actor actor : actors) {
            if (x >= actor.getX() && x <= actor.getX() + actor.getWidth() &&
                    y >= actor.getY() && y <= actor.getY() + actor.getHeight()) {
                return actor;
            }
        }
        return null;
    }

    /**
     * Updates the position of an actor based on the mouse coordinates, while ensuring
     * that the actor stays within the bounds of the drawing canvas.
     *
     * @param actor The `Actor` to move.
     * @param mouseX The current x-coordinate of the mouse.
     * @param mouseY The current y-coordinate of the mouse.
     * @param offsetX The horizontal offset between the actor's position and the mouse.
     * @param offsetY The vertical offset between the actor's position and the mouse.
     * @param canvasWidth The width of the canvas to constrain the actor within.
     * @param canvasHeight The height of the canvas to constrain the actor within.
     */
    public static void updateActorPosition(Actor actor, double mouseX, double mouseY, double offsetX, double offsetY,
                                           double canvasWidth, double canvasHeight) {
        double newX = mouseX - offsetX;
        double newY = mouseY - offsetY;

        // Clamp the position of the Actor to stay within the bounds of the whole drawingCanvas
        double maxX = canvasWidth - actor.getWidth();  // Right boundary of the canvas
        double minX = 0;  // Left boundary of the canvas
        double maxY = canvasHeight - actor.getHeight();  // Bottom boundary of the canvas
        double minY = 0;  // Top boundary of the canvas

        // Apply constraints
        actor.setX(Math.max(minX, Math.min(newX, maxX)));
        actor.setY(Math.max(minY, Math.min(newY, maxY)));
    }

    /**
     * Retrieves the list of actors managed by this actor manager.
     *
     * @return A list of `Actor` objects.
     */
    public List<Actor> getActors() {
        return actors;
    }

}

