module com.pidev.pidev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires kotlin.stdlib;

    opens com.pidev to javafx.fxml;
    opens com.pidev.controllers to javafx.fxml;
    opens com.pidev.entities to javafx.fxml;
    opens com.pidev.controllers.blog to javafx.fxml;

    exports com.pidev;
    exports com.pidev.entities;
    exports com.pidev.controllers;
    exports com.pidev.controllers.blog;
}