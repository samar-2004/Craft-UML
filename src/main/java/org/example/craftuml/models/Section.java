package org.example.craftuml.models;

import java.util.List;

/**
 * Represents a section that contains a title and a list of items.
 * The items can be of any type, allowing for flexibility in the type of content stored in the section.
 * This class provides methods to access the section title and its items.
 *
 */
public class Section {
    /**
     * The title of the section. It provides a descriptive name for the section.
     */
    private String title;

    /**
     * The list of items in the section. The items can be of any type, depending on the content of the section.
     */
    private List<Object> items;

    /**
     * Constructs a new Section with the specified title and list of items.
     *
     * @param title The title of the section.
     * @param items The list of items in the section.
     */

    public Section(String title, List<Object> items) {
        this.title = title;
        this.items = items;
    }

    /**
     * Gets the title of the section.
     *
     * @return The title of the section.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the list of items in the section.
     *
     * @return The list of items.
     */
    public List<Object> getItems() {
        return items;
    }
}
