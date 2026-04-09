module com.pidev.pidev {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;


    opens com.pidev to javafx.fxml;
    exports com.pidev;
}