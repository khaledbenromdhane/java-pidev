module com.pidev.pidev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires kotlin.stdlib;
    requires java.desktop;        // ← pour java.awt.Color
    requires com.github.librepdf.openpdf;
    requires javafx.base;
    requires java.mail;

    opens com.pidev to javafx.fxml;
    opens com.pidev.controllers to javafx.fxml;
    opens com.pidev.entities to javafx.fxml;

    exports com.pidev;
    exports com.pidev.entities;
    exports com.pidev.controllers;
}