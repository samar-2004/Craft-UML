package org.example.craftuml.models.UseCaseDiagrams;

import org.example.craftuml.models.UseCaseDiagrams.UseCase;

class ActorUseCaseRelationship {
    private Actor actor;
    private UseCase useCase;

    public ActorUseCaseRelationship(Actor actor, UseCase useCase) {
        this.actor = actor;
        this.useCase = useCase;
    }

    public String getRelation() {
        return actor.getName() + " interacts with " + useCase.getName();
    }
}