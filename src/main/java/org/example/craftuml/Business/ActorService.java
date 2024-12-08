package org.example.craftuml.Business;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.craftuml.models.UseCaseDiagrams.Actor;

import java.util.List;

public class ActorService {

    private List<Actor> actors;

    // Constructor to initialize the actors list
    public ActorService(List<Actor> actors) {
        this.actors = actors;
    }

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

    // Find an actor by position
    public Actor findActorByPosition(double x, double y) {
        for (Actor actor : actors) {
            if (x >= actor.getX() && x <= actor.getX() + actor.getWidth() &&
                    y >= actor.getY() && y <= actor.getY() + actor.getHeight()) {
                return actor;
            }
        }
        return null;
    }

    // Check for duplicate name excluding the given actor
    public boolean isDuplicateNameExcludingActor(String newName, Actor excludedActor) {
        return actors.stream()
                .anyMatch(existingActor -> existingActor.getName().equalsIgnoreCase(newName) && existingActor != excludedActor);
    }

    // Update actor's name
    public void updateActorName(Actor actor, String newName) {
        actor.setName(newName);
    }


    public boolean isDuplicateActorName(String actorName) {
        return actors.stream().anyMatch(actor -> actor.getName().equalsIgnoreCase(actorName));
    }

    public void addActor(String actorName) {
        Actor actor = new Actor(actorName);

        // Position calculation (modify as needed)
        double x = (actors.size() + 1) * 50; // Example positioning logic
        double y = 100 + (actors.size() + 1) * 30;

        actor.setX(x);
        actor.setY(y);

        actors.add(actor);
    }

    public static boolean isHoveringOverActor(double x, double y, List<Actor> actors) {
        for (Actor actor : actors) {
            if (x >= actor.getX() && x <= actor.getX() + actor.getWidth() &&
                    y >= actor.getY() && y <= actor.getY() + actor.getHeight()) {
                return true;
            }
        }
        return false;
    }

    public static Actor getClickedActor(double x, double y, List<Actor> actors) {
        for (Actor actor : actors) {
            if (x >= actor.getX() && x <= actor.getX() + actor.getWidth() &&
                    y >= actor.getY() && y <= actor.getY() + actor.getHeight()) {
                return actor;
            }
        }
        return null;
    }

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

    public List<Actor> getActors() {
        return actors;
    }

}

