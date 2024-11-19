package org.example.craftuml.models.UseCaseDiagrams;

import org.example.craftuml.models.DiagramComponent;

import java.util.ArrayList;
import java.util.List;

// Use Case Diagram Model
public class UseCaseDiagram {
    private String name;
    private List<DiagramComponent> components;

    public UseCaseDiagram(String name) {
        this.name = name;
        this.components = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addComponent(DiagramComponent component) {
        components.add(component);
    }

    public List<DiagramComponent> getComponents() {
        return components;
    }
}
