package org.example.craftuml.models.ClassDiagrams;

public class MethodData {
    private String accessModifier;
    private String name;
    private String returnType;

    public MethodData(String accessModifier, String name, String returnType) {
        this.accessModifier = accessModifier;
        this.name = name;
        this.returnType = returnType;
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public void setAccessModifier(String accessModifier) {
        this.accessModifier = accessModifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
