module com.example.java {
    requires java.sql;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.web;
    requires java.desktop;
    requires org.apache.pdfbox;


    opens com.example.java to javafx.fxml;
    opens com.example.java.controllers to javafx.fxml;

    exports com.example.java;
    exports com.example.java.controllers;
}
