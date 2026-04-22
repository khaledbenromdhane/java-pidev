package com.example.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class BackMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/java/back/MainDashboard.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 760);

        URL cssUrl = getClass().getResource("/com/example/java/styles/styles.css");
        if (cssUrl == null) {
            throw new IllegalStateException("CSS introuvable : /com/example/java/styles/styles.css");
        }

        scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}