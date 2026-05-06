package esprit.tn;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage stage)  {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(root, 1400, 900);
            stage.setTitle("Formation Manager - Gestion des Formations et Évaluations");
            stage.setScene(scene);
            stage.setWidth(1400);
            stage.setHeight(900);
            stage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

}
