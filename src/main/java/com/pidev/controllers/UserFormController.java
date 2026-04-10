package com.pidev.controllers;

import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class UserFormController implements Initializable {

    @FXML private Label            lblTitre;
    @FXML private TextField        tfNom;
    @FXML private TextField        tfPrenom;
    @FXML private TextField        tfEmail;
    @FXML private TextField        tfTelephone;
    @FXML private PasswordField    pfPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label            lblMessage;

    private final CrudService   service = new CrudService();
    private String              mode;
    private User                userAModifier;
    private UserListController  listController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbRole.getItems().addAll("ADMIN", "USER");
        cbRole.setValue("USER");
    }

    public void setMode(String mode, User user, UserListController listController) {
        this.mode           = mode;
        this.userAModifier  = user;
        this.listController = listController;

        if ("MODIFIER".equals(mode) && user != null) {
            lblTitre.setText("✏️ Modifier l'utilisateur");
            tfNom.setText(user.getNom());
            tfPrenom.setText(user.getPrenom());
            tfEmail.setText(user.getEmail());
            tfTelephone.setText(user.getTelephone());
            pfPassword.setText(user.getPassword());
            cbRole.setValue(user.getRole());
        } else {
            lblTitre.setText("➕ Ajouter un utilisateur");
        }
    }

    @FXML
    private void enregistrer() {
        String nom       = tfNom.getText().trim();
        String prenom    = tfPrenom.getText().trim();
        String email     = tfEmail.getText().trim();
        String telephone = tfTelephone.getText().trim();
        String password  = pfPassword.getText().trim();

        // ── 1. Champs vides ──────────────────────────────────────────────────────
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
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

        // ── Enregistrement ───────────────────────────────────────────────────────
        if ("AJOUTER".equals(mode)) {
            User u = new User(nom, prenom, password, email, telephone, cbRole.getValue());
            service.ajouter(u);
            afficherSucces("✅ Utilisateur ajouté avec succès !");
        } else if ("MODIFIER".equals(mode)) {
            userAModifier.setNom(nom);
            userAModifier.setPrenom(prenom);
            userAModifier.setEmail(email);
            userAModifier.setTelephone(telephone);
            userAModifier.setPassword(password);
            userAModifier.setRole(cbRole.getValue());
            service.modifier(userAModifier);
            afficherSucces("✅ Utilisateur modifié avec succès !");
        }

        if (listController != null) listController.actualiser();
        fermerFenetre();
    }

    @FXML
    private void annuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.close();
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
