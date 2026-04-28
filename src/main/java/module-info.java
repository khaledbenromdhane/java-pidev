open module com.pidev.pidev {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;

    requires java.sql;
    requires java.net.http;

    requires com.google.zxing;
    requires com.google.zxing.javase;

    requires java.desktop;
    requires webcam.capture;
    requires org.json;

    requires kotlin.stdlib;

    exports com.pidev;
    exports com.pidev.entities;
    exports com.pidev.controllers;
    exports com.pidev.services;
    exports com.pidev.tools;
}