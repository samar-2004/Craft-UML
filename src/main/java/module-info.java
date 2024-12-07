module org.example.craftuml {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.desktop;
    requires jdk.sctp;


    requires com.fasterxml.jackson.databind;
    requires org.apache.pdfbox;
    requires javafx.swing;
    //requires junit;
    requires org.junit.jupiter.api;
    requires org.mockito;

    opens org.example.craftuml to javafx.fxml;
    exports org.example.craftuml;
    exports org.example.craftuml.Service;
    opens org.example.craftuml.Service to javafx.fxml;
    opens org.example.craftuml.Tests to org.junit.platform.commons;
}