package org.example.craftuml.Tests;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.example.craftuml.Service.UseCaseDashboardController;
import org.example.craftuml.models.UseCaseDiagrams.UseCaseDiagram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;


public class UseCaseDiagramTest {


    private UseCaseDashboardController controller;

    @Mock
    private Alert alertMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UseCaseDashboardController(); // Replace with the actual initialization
    }

    @Test
    public void testHandleAddUseCaseDiagram_NoActiveDiagram() {
        // Ensure no active diagram
        controller.setActiveDiagram(null);

        // Mock the handleUseCaseDiagram() method
        UseCaseDashboardController spyController = spy(controller);
        doNothing().when(spyController).handleUseCaseDiagram();

        // Trigger the method
        spyController.handleAddUseCaseDiagram(new ActionEvent());

        // Verify that handleUseCaseDiagram() was called
        verify(spyController, times(1)).handleUseCaseDiagram();
    }

    @Test
    public void testHandleAddUseCaseDiagram_WithActiveDiagram_UserConfirms() {
        // Set an active diagram
        controller.setActiveDiagram(new UseCaseDiagram());

        // Mock the alert response (no need to mock the Alert class itself)
        when(alertMock.showAndWait()).thenReturn(Optional.of(ButtonType.YES));

        // Trigger the method
        controller.handleAddUseCaseDiagram(new ActionEvent());

        // Verify that handleUseCaseDiagram() was called
        verify(controller, times(1)).handleUseCaseDiagram();
    }


    @Test
    public void testHandleAddUseCaseDiagram_WithActiveDiagram_UserCancels() {
        // Set an active diagram
        controller.setActiveDiagram(new UseCaseDiagram());

        // Mock the handleUseCaseDiagram() method
        UseCaseDashboardController spyController = spy(controller);
        doNothing().when(spyController).handleUseCaseDiagram();

        // Mock the alert and user cancellation
        when(alertMock.showAndWait()).thenReturn(Optional.of(ButtonType.NO));

        // Trigger the method
        spyController.handleAddUseCaseDiagram(new ActionEvent());

        // Verify that handleUseCaseDiagram() was NOT called
        verify(spyController, times(0)).handleUseCaseDiagram();
    }
}
