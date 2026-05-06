package esprit.tn.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private VBox mainContainer;

    @FXML
    private void handleAdminLogin() {
        switchScene("/dashboard.fxml", "Tableau de Bord Administrateur");
    }

    @FXML
    private void handleUserLogin() {
        switchScene("/frontFormations.fxml", "Catalogue des Formations");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du changement de scène: " + e.getMessage());
        }
    }
}
