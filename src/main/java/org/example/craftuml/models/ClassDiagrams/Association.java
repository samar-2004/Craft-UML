package org.example.craftuml.models.ClassDiagrams;

import org.example.craftuml.models.DiagramComponent;

class Association implements DiagramComponent {
    private ClassDiagram class1;
    private ClassDiagram class2;
    private double x, y;

    public Association(ClassDiagram class1, ClassDiagram class2) {
        this.class1 = class1;
        this.class2 = class2;
    }

    @Override
    public String getName() {
        return class1.getName() + " - " + class2.getName();
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }
}