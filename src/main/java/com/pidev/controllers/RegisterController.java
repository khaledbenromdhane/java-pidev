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

public class RegisterController {

    @FXML private TextField     tfNom;
    @FXML private TextField     tfPrenom;
    @FXML private TextField     tfEmail;
    @FXML private TextField     tfTelephone;
    @FXML private PasswordField pfPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label         lblMessage;

    private final CrudService service = new CrudService();

    @FXML
    private void sInscrire() {
        String nom             = tfNom.getText().trim();
        String prenom          = tfPrenom.getText().trim();
        String email           = tfEmail.getText().trim();
        String telephone       = tfTelephone.getText().trim();
        String password        = pfPassword.getText().trim();
        String confirmPassword = pfConfirmPassword.getText().trim();

        // ── 1. Champs vides ──────────────────────────────────────────────────────
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            afficherErreur("⚠️ Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // ── 2. Nom et prénom (lettres uniquement) ────────────────────────────────
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s]+")) {
            afficherErreur("⚠️ Le nom ne doit contenir que des lettres.");
            return;
        }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s]+")) {
            afficherErreur("⚠️ Le prénom ne doit contenir que des lettres.");
            return;
        }

        // ── 3. Email valide ──────────────────────────────────────────────────────
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            afficherErreur("⚠️ Format d'email invalide. Ex: nom@email.com");
            return;
        }

        // ── 4. Téléphone numérique ───────────────────────────────────────────────
        if (!telephone.isEmpty() && !telephone.matches("^[0-9]{8,15}$")) {
            afficherErreur("⚠️ Téléphone invalide (8 à 15 chiffres uniquement).");
            return;
        }

        // ── 5. Longueur mot de passe ─────────────────────────────────────────────
        if (password.length() < 4) {
            afficherErreur("⚠️ Mot de passe trop court (min. 4 caractères).");
            return;
        }

        // ── 6. Confirmation mot de passe ─────────────────────────────────────────
        if (!password.equals(confirmPassword)) {
            afficherErreur("⚠️ Les mots de passe ne correspondent pas.");
            return;
        }

        // ── 7. Email déjà utilisé ─────────────────────────────────────────────────
        boolean emailExiste = service.afficher().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (emailExiste) {
            afficherErreur("⚠️ Cet email est déjà utilisé.");
            return;
        }

        // ── Enregistrement ───────────────────────────────────────────────────────
        User user = new User(nom, prenom, password, email, telephone, "USER", null);
        service.ajouter(user);
        afficherSucces("✅ Inscription réussie ! Vous pouvez vous connecter.");

        tfNom.clear(); tfPrenom.clear(); tfEmail.clear();
        tfTelephone.clear(); pfPassword.clear(); pfConfirmPassword.clear();
    }

    @FXML
    private void ouvrirLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.setTitle("Connexion");
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
