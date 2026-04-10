module com.pidev.pidev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;


    opens com.pidev to javafx.fxml;
    opens com.pidev.controllers to javafx.fxml;
    exports com.pidev;
    exports com.pidev.entities;
}
