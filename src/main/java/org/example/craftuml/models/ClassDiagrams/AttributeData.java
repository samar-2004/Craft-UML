package org.example.craftuml.models.ClassDiagrams;

public class AttributeData {
    private String accessModifier;
    private String name;
    private String dataType;

    public AttributeData(String accessModifier, String name, String dataType) {
        this.accessModifier = accessModifier;
        this.name = name;
        this.dataType = dataType;
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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}

