package com.pidev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pidev/views/evenement-list.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 850);
        scene.setFill(javafx.scene.paint.Color.web("#0b0b12"));

        primaryStage.setTitle("ArtisEvent – Front Office");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
