package org.example.craftuml.models;

import java.util.List;

public class Section {
    private String title;
    private List<Object> items;

    public Section(String title, List<Object> items) {
        this.title = title;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public List<Object> getItems() {
        return items;
    }
}
