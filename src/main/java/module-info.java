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


    requires com.fasterxml.jackson.databind;
    requires org.apache.pdfbox;
    requires javafx.swing;

    opens org.example.craftuml to javafx.fxml;
    exports org.example.craftuml;
}