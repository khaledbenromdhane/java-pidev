package com.pidev.controllers;

import com.pidev.entities.User;
import com.pidev.tools.AuthSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        User user = AuthSession.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom());
        }
    }

    @FXML
    private void ouvrirGestion() {
        navigate("/com/pidev/UserList.fxml", "Gestion des Utilisateurs");
    }

    @FXML
    private void ouvrirBlog() {
        User user = AuthSession.getCurrentUser();
        String role = (user != null && user.getRole() != null) ? user.getRole().toLowerCase() : "";
        
        if (role.contains("admin")) {
            navigate("/com/pidev/blog/AdminBlogMain.fxml", "Espace Modération - Publications");
        } else {
            navigate("/com/pidev/blog/BlogMain.fxml", "Tunarche Blog");
        }
    }

    @FXML
    private void ouvrirModerationComments() {
        navigate("/com/pidev/blog/AdminBlogMain.fxml", "Espace Modération - Commentaires");
    }

    @FXML
    private void ouvrirStats() {
        navigate("/com/pidev/AdminStats.fxml", "Analyses & Statistiques Platforme");
    }

    @FXML
    private void seDeconnecter() {
        AuthSession.logout();
        navigate("/com/pidev/Login.fxml", "Connexion");
    }

    private void navigate(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Erreur de navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
