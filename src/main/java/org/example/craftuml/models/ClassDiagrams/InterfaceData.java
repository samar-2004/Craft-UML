package org.example.craftuml.models.ClassDiagrams;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.craftuml.models.DiagramComponent;

import java.util.ArrayList;
import java.util.List;

public class InterfaceData implements DiagramComponent {
    private List<MethodData> methods;
    private double x, y;
    private double height,width;
    private static final double PADDING = 10;
    private static final Font FONT = Font.font("Arial", 12);
    private final StringProperty name = new SimpleStringProperty();

    public InterfaceData()
    {
        this.methods = new ArrayList<>();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty() || name.contains(" ")) {
            throw new IllegalArgumentException("Class name cannot be empty or contain spaces.");
        }
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
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

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

}