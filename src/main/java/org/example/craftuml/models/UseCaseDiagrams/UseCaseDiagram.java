package org.example.craftuml.models.UseCaseDiagrams;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.craftuml.models.DiagramComponent;
import java.util.ArrayList;
import java.util.List;

public class UseCaseDiagram {
    private String name;
    private List<UseCase> useCases;
    private double width = 700;  // Set default width
    private List<Association> associations;
    private  List<UseCaseToUseCaseRelation> useCaseRelations;

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    private double height = 700; // Set default height// List to store all use cases
    private List<Actor> actors;                    // List to store all actors

    double x;

    public void setUseCases(List<UseCase> useCases) {
        this.useCases = useCases;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    double y;

    public UseCaseDiagram(String name, double x, double y) {
        setName(name);
        this.x = x;
        this.y = y;
        this.actors = new ArrayList<>();
    }

    public UseCaseDiagram() {
        useCases = new ArrayList<>();
        actors = new ArrayList<>();
    }

//    public void addUseCase(String useCaseName) {
//        if (useCaseName == null || useCaseName.trim().isEmpty()) {
//            throw new IllegalArgumentException("UseCase name cannot be null or empty");
//        }
//        useCases.add(new UseCase(useCaseName));
//    }

    public void addUseCase(UseCase useCase) {
        useCases.add(useCase); // Assuming useCases is a list of UseCase objects
    }

//    public void removeUseCase(String useCaseName) {
//        UseCase useCase = findUseCaseByName(useCaseName);
//        if (useCase != null) {
//            useCases.remove(useCase);
//        }
//    }

    public void removeUseCase(UseCase useCase) {
        useCases.remove(useCase); // Assuming useCases is a list of UseCase objects
    }

    public void addActor(String actorName) {
        if (actorName == null || actorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Actor name cannot be null or empty");
        }
        actors.add(new Actor(actorName));
    }

    public void removeActor(String actorName) {
        Actor actor = findActorByName(actorName);
        if (actor != null) {
            actors.remove(actor);
        }
    }

    public void addUseCaseRelation(UseCaseToUseCaseRelation relation) {
        // Add the relation to the list of relations
        useCaseRelations.add(relation);

        // Optionally, you can update the diagram or call any other method to refresh the view
        updateDiagram();
    }




    public List<Association> getAssociations(){
        return associations;
    }

    public List<UseCaseToUseCaseRelation> getUseCaseRelations() {return  useCaseRelations;}


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

    public List<UseCaseToUseCaseRelation> getAllUseCaseRelations() {
        return useCaseRelations;
    }

    private void updateDiagram() {
        // Redraw the entire diagram (this can be more optimized based on your needs)
//        clearCanvas();
//        drawUseCaseRelations(drawingCanvas.getGraphicsContext2D());
//        drawActorsAndUseCases();
    }


    public List<UseCase> getUseCases() {
        return new ArrayList<>(useCases);
    }

    public List<Actor> getActors() {
        return new ArrayList<>(actors);  // Return a copy to avoid external modification
    }


    public void display(GraphicsContext gc) {
        double width = calculateDiagramWidth(gc);
        double height = calculateDiagramHeight();

        // Draw the background rectangle for the diagram
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        // Draw the name line
        gc.strokeLine(x, y + 30, x + width, y + 30);

        // Draw the actors
        double actorY = y + 45;
        for (Actor actor : actors) {
            for (Association association : actor.getAssociations()) {
                gc.strokeLine(actor.getX() + actor.getWidth() / 2, actor.getY() + actor.getHeight() / 2,
                        association.getUseCase().getX() + association.getUseCase().getWidth() / 2,
                        association.getUseCase().getY() + association.getUseCase().getHeight() / 2);
            }
        }

        // Draw the use cases
        double useCaseY = y + 45 + (actors.size() * 20);
        for (UseCase useCase : useCases) {
            gc.fillText(useCase.getName(), x + 10, useCaseY);
            useCaseY += 20;
        }
    }

    private double calculateDiagramWidth(GraphicsContext gc) {
        double maxWidth = 0;

        // Calculate width based on actor and use case names
        Text tempText = new Text(name);
        tempText.setFont(gc.getFont());
        maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());

        for (Actor actor : actors) {
            tempText = new Text(actor.getName());
            maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());
        }

        for (UseCase useCase : useCases) {
            tempText = new Text(useCase.getName());
            maxWidth = Math.max(maxWidth, tempText.getLayoutBounds().getWidth());
        }

        return maxWidth + 40;
    }

    private double calculateDiagramHeight() {
        double actorHeight = 30 * actors.size();
        double useCaseHeight = 30 * useCases.size();
        return 30 + actorHeight + useCaseHeight + 10;
    }

    // Helper methods to find specific Actor, UseCase, or Relationship by name
    private Actor findActorByName(String name) {
        for (Actor actor : actors) {
            if (actor.getName().equals(name)) {
                return actor;
            }
        }
        return null;
    }

    private UseCase findUseCaseByName(String name) {
        for (UseCase useCase : useCases) {
            if (useCase.getName().equals(name)) {
                return useCase;
            }
        }
        return null;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
