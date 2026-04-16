package com.pidev.controllers;

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

        // ── Validation ───────────────────────────────────────────────────────────
        if (email.isEmpty() || password.isEmpty()) {
            afficherErreur("⚠️ Veuillez remplir tous les champs.");
            return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            afficherErreur("⚠️ Format d'email invalide. Ex: nom@email.com");
            return;
        }
        if (password.length() < 4) {
            afficherErreur("⚠️ Mot de passe trop court (min. 4 caractères).");
            return;
        }

        // ── Vérification en base ─────────────────────────────────────────────────
        List<User> users = service.afficher();
        User userTrouve = users.stream()
                .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (userTrouve != null) {
            // Logic for DB-based ban will be restored once columns are identified (Debug logs)
            /* 
            if (userTrouve.getEst_signale() == 1) {
                afficherErreur("🚫 Votre compte a été banni par un administrateur.");
                return;
            }
            */
            com.pidev.tools.AuthSession.setCurrentUser(userTrouve);
            afficherSucces("✅ Connexion réussie !");
            ouvrirAccueil(userTrouve);
        } else {
            afficherErreur("❌ Email ou mot de passe incorrect.");
        }
    }

    private void ouvrirAccueil(User user) {
        try {
            System.out.println("DEBUG: User Role from DB = '" + user.getRole() + "'");
            
            String fxml = "/com/pidev/Dashboard.fxml";
            String role = (user.getRole() != null) ? user.getRole().toLowerCase() : "";
            
            if (role.contains("admin")) {
                System.out.println("DEBUG: User identified as ADMIN -> Loading AdminDashboard");
                fxml = "/com/pidev/AdminDashboard.fxml";
            } else {
                System.out.println("DEBUG: User identified as MOCK/USER -> Loading Standard Dashboard");
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setTitle("Tableau de Bord - " + user.getNom() + " [" + user.getRole() + "]");
            stage.setScene(new Scene(root, 1100, 720));
        } catch (IOException e) {
            afficherErreur("Erreur ouverture : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setTitle("Inscription");
            stage.setScene(new Scene(root, 450, 560));
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
