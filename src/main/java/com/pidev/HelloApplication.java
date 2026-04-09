package com.pidev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Hello");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 320, 200);

        primaryStage.setTitle("Hello");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
