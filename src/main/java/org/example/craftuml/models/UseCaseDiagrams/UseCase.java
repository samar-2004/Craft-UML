package org.example.craftuml.models.UseCaseDiagrams;

import org.example.craftuml.models.DiagramComponent;

class UseCase implements DiagramComponent {
    private String name;
    private double x, y;

    public UseCase(String name) {
        this.name = name;
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
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }
}