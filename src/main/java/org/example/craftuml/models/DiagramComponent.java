package org.example.craftuml.models;

public interface DiagramComponent {
    String getName();
    void setName(String name);
    void setPosition(double x, double y);
    double getX();
    double getY();
}
