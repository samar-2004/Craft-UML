package TEST.models;

import org.example.craftuml.models.Section;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

class SectionTest {

    @Test
    void testConstructorAndGetters() {
        // Create a list of items
        List<Object> items = Arrays.asList("Item 1", "Item 2", "Item 3");

        // Create a Section object with the title and items
        Section section = new Section("Section Title", items);

        // Verify that the title is set correctly
        assertEquals("Section Title", section.getTitle());

        // Verify that the items are set correctly
        assertEquals(items, section.getItems());
    }

    @Test
    void testEmptyItemsList() {
        // Create an empty items list
        List<Object> emptyItems = Arrays.asList();

        // Create a Section object with the empty list
        Section section = new Section("Empty Section", emptyItems);

        // Verify that the title is set correctly
        assertEquals("Empty Section", section.getTitle());

        // Verify that the items list is empty
        assertTrue(section.getItems().isEmpty());
    }

    @Test
    void testNullItems() {
        // Create a Section object with null items list
        Section section = new Section("Null Items Section", null);

        // Verify that the title is set correctly
        assertEquals("Null Items Section", section.getTitle());

        // Verify that the items list is null
        assertNull(section.getItems());
    }
}

