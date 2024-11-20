package org.example.craftuml.models.ClassDiagrams;


import org.example.craftuml.models.DiagramComponent;

import java.util.ArrayList;
import java.util.List;

public class InterfaceData implements DiagramComponent {
    private String name;
    private List<MethodData> methods;
    private double x, y;

    public InterfaceData(String name,List<MethodData> methods) {
        this.name = name;
        this.methods = methods;
    }
    public InterfaceData()
    {
        this.methods = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }
    public List<MethodData> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodData> methods) {
        this.methods = methods;
    }

    public void setX(double newX) {
        this.x = newX;
    }

    public void setY(double newY) {
        this.y = newY;
    }
}