package com.pidev.controllers;

import com.pidev.SessionManager;
import com.pidev.entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserHomeController implements Initializable {

    @FXML private Label lblBienvenue;
    @FXML private Label lblNomUser;
    @FXML private Label lblRoleUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SessionManager.getInstance().getUserConnecte();
        if (user != null) {
            lblBienvenue.setText("Bonjour, " + user.getNom() + " " + user.getPrenom() + " 👋");
            lblNomUser.setText(user.getNom() + " " + user.getPrenom());
            lblRoleUser.setText(user.getRole());
        }
    }

    @FXML private void allerFormation() {
        afficherInfo("Formation", "Le module Formation sera bientôt disponible !");
    }

    @FXML private void allerEvenement() {
        afficherInfo("Événement", "Le module Événement sera bientôt disponible !");
    }

    @FXML private void allerPublication() {
        afficherInfo("Publication", "Le module Publication sera bientôt disponible !");
    }

    @FXML private void allerGalerie() {
        afficherInfo("Galerie", "Le module Galerie sera bientôt disponible !");
    }

    @FXML private void allerProfil() {
        naviguerVers("/com/pidev/Profil.fxml", "Mon Profil", 1000, 650);
    }

    @FXML private void deconnecter() {
        SessionManager.getInstance().deconnecter();
        naviguerVers("/com/pidev/Login.fxml", "Connexion", 420, 420);
    }

    private void naviguerVers(String fxml, String titre, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) lblBienvenue.getScene().getWindow();
            stage.setTitle(titre);
            stage.setScene(new Scene(root, w, h));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherInfo(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
