open module com.pidev.pidev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires java.sql;
    requires com.google.zxing;
    requires kotlin.stdlib;

    exports com.pidev;
    exports com.pidev.entities;
    exports com.pidev.controllers;
    exports com.pidev.services;
    exports com.pidev.tools;
}
