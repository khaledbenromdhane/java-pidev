package com.pidev.controllers;

import com.pidev.SessionManager;
import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class LoginController {

    @FXML private TextField     tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private Label         lblMessage;

    private final CrudService service = new CrudService();

    @FXML
    private void seConnecter() {
        String email    = tfEmail.getText().trim();
        String password = pfPassword.getText().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            afficherErreur("⚠️ Veuillez remplir tous les champs."); return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            afficherErreur("⚠️ Format d'email invalide."); return;
        }
        if (password.length() < 4) {
            afficherErreur("⚠️ Mot de passe trop court (min. 4 caractères)."); return;
        }

        // Vérification en base
        List<User> users = service.afficher();
        User userTrouve = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password))
                .findFirst().orElse(null);

        if (userTrouve != null) {
            SessionManager.getInstance().setUserConnecte(userTrouve);
            afficherSucces("✅ Connexion réussie !");

            // ── Redirection selon le rôle ─────────────────────────────────────
            if ("ADMIN".equalsIgnoreCase(userTrouve.getRole())) {
                naviguerVers("/com/pidev/AdminDashboard.fxml", "Administration", 1100, 700);
            } else {
                naviguerVers("/com/pidev/UserHome.fxml", "Espace Utilisateur", 1100, 700);
            }
        } else {
            afficherErreur("❌ Email ou mot de passe incorrect.");
        }
    }

    @FXML
    private void ouvrirRegister() {
        naviguerVers("/com/pidev/Register.fxml", "Inscription", 450, 580);
    }

    @FXML
    private void ouvrirMdpOublie() {
        naviguerVers("/com/pidev/ForgotPassword.fxml", "Récupération de mot de passe", 430, 500);
    }

    private void naviguerVers(String fxml, String titre, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setTitle(titre);
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (IOException e) {
            afficherErreur("Erreur : " + e.getMessage());
        }
    }

    private void afficherErreur(String msg) {
        lblMessage.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
        lblMessage.setText(msg);
    }

    private void afficherSucces(String msg) {
        lblMessage.setStyle("-fx-text-fill: green; -fx-font-size: 12;");
        lblMessage.setText(msg);
    }
}
