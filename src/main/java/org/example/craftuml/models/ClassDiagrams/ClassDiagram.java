package org.example.craftuml.models.ClassDiagrams;

import org.example.craftuml.ClassDashboardController;
import org.example.craftuml.models.DiagramComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClassDiagram implements DiagramComponent {
    private String name;
    private List<AttributeData> attributes;
    private List<MethodData> methods;
    private double x, y;

    public ClassDiagram(String name, double x, double y) {
        setName(name);
        this.x = x;
        this.y = y;
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
    }
    public ClassDiagram()
    {
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
    }
    public void setAttributes(List<AttributeData> attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Attributes list cannot be null.");
        }
        this.attributes = new ArrayList<>(attributes);
    }

    public void setMethods(List<MethodData> methods) {
        if (methods == null) {
            throw new IllegalArgumentException("Methods list cannot be null.");
        }
        this.methods = new ArrayList<>(methods);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty() || name.contains(" ")) {
            throw new IllegalArgumentException("Class name cannot be empty or contain spaces.");
        }
        this.name = name;
    }

    public void addAttributes(AttributeData ad) {
        attributes.add(ad);
    }

    public void addMethods(MethodData md) {
        methods.add(md);
    }

    public List<AttributeData> getAttributes() {
        return attributes;
    }

    public List<MethodData> getMethods() {
        return methods;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x)
    {
        this.x = x;
    }
    public void setY(double y)
    {
        this.y = y;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }


}
