module com.pidev.pidev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires kotlin.stdlib;
    requires java.desktop;
    requires com.google.gson;
    requires org.controlsfx.controls;
    requires java.mail;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires jdk.httpserver;
    opens com.pidev to javafx.fxml;
    opens com.pidev.controllers to javafx.fxml;
    opens com.pidev.entities to javafx.fxml;
    opens com.pidev.controllers.blog to javafx.fxml;
    requires cloudinary.http44;
    requires cloudinary.core;

    exports com.pidev;
    exports com.pidev.entities;
    exports com.pidev.controllers;
    exports com.pidev.controllers.blog;
}