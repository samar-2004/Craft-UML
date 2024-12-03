package org.example.craftuml.models.UseCaseDiagrams;

import javafx.scene.text.Text;

public class UseCaseToUseCaseRelation {
    private UseCase useCase1;
    private UseCase useCase2;
    private String relationType;

    public UseCaseToUseCaseRelation(UseCase useCase1, UseCase useCase2, String relationType) {
        this.useCase1 = useCase1;
        this.useCase2 = useCase2;
        this.relationType = relationType;
    }

    public UseCase getUseCase1() {
        return useCase1;
    }

    public void setUseCase1(UseCase useCase1) {
        this.useCase1 = useCase1;
    }

    public UseCase getUseCase2() {
        return useCase2;
    }

    public void setUseCase2(UseCase useCase2) {
        this.useCase2 = useCase2;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public void addRelation(Text relation){
        relationType = String.valueOf(relation);
    }
}

