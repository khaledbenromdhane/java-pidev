package com.pidev.controllers;

import com.pidev.SessionManager;
import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfilController implements Initializable {

    @FXML private TextField     tfNom;
    @FXML private TextField     tfPrenom;
    @FXML private TextField     tfEmail;
    @FXML private TextField     tfTelephone;
    @FXML private TextField     tfRole;
    @FXML private PasswordField pfPassword;
    @FXML private Label         lblMessage;
    @FXML private Label         lblSidebarNom;
    @FXML private Label         lblSidebarRole;
    @FXML private ImageView     ivSidebarAvatar, ivProfilPhoto;
    @FXML private javafx.scene.shape.Circle clipSidebar, clipProfil;

    private final CrudService service = new CrudService();
    private User userConnecte;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("DEBUG Profil: Initialisation...");
        try {
            userConnecte = SessionManager.getInstance().getUserConnecte();
            if (userConnecte != null) {
                chargerDonnees();
                configurerClips();
            } else {
                System.err.println("⚠️ Aucun utilisateur connecté dans le profil !");
            }
            System.out.println("✅ Profil initialisé");
        } catch (Exception e) {
            System.err.println("❌ Erreur critique initialisation Profil : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void allerStudio() {
        naviguerVers("/com/pidev/AvatarStudio.fxml", "Avatar Studio", 600, 700);
    }

    private void chargerDonnees() {
        tfNom.setText(userConnecte.getNom());
        tfPrenom.setText(userConnecte.getPrenom());
        tfEmail.setText(userConnecte.getEmail());
        tfTelephone.setText(userConnecte.getTelephone());
        tfRole.setText(userConnecte.getRole());
        lblSidebarNom.setText(userConnecte.getNom() + " " + userConnecte.getPrenom());
        lblSidebarRole.setText(userConnecte.getRole());
        chargerPhoto();
    }

    private void configurerClips() {
        // Rendre les images circulaires
        javafx.scene.shape.Circle c1 = new javafx.scene.shape.Circle(35, 35, 35);
        ivSidebarAvatar.setClip(c1);
        
        javafx.scene.shape.Circle c2 = new javafx.scene.shape.Circle(50, 50, 50);
        ivProfilPhoto.setClip(c2);
    }

    private void chargerPhoto() {
        String photoPath = userConnecte.getPhoto();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                javafx.scene.image.Image img;
                if (photoPath.startsWith("http")) {
                    img = new javafx.scene.image.Image(photoPath, true);
                } else {
                    img = new javafx.scene.image.Image(new java.io.File(photoPath).toURI().toString());
                }
                ivProfilPhoto.setImage(img);
                ivSidebarAvatar.setImage(img);
            } catch (Exception e) {
                chargerAvatarParDefaut();
            }
        } else {
            chargerAvatarParDefaut();
        }
    }

    private void chargerAvatarParDefaut() {
        try {
            String name = java.net.URLEncoder.encode(userConnecte.getNom() + " " + userConnecte.getPrenom(), "UTF-8");
            String avatarUrl = "https://api.dicebear.com/7.x/initials/png?seed=" + name + "&backgroundColor=00acc1,1e88e5,5e35b1";
            javafx.scene.image.Image img = new javafx.scene.image.Image(avatarUrl, true);
            ivProfilPhoto.setImage(img);
            ivSidebarAvatar.setImage(img);
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void choisirPhoto() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(tfNom.getScene().getWindow());

        if (selectedFile != null) {
            userConnecte.setPhoto(selectedFile.getAbsolutePath());
            chargerPhoto();
            afficherSucces("✅ Photo sélectionnée. N'oubliez pas d'enregistrer !");
        }
    }

    @FXML
    private void enregistrer() {
        String nom       = tfNom.getText().trim();
        String prenom    = tfPrenom.getText().trim();
        String email     = tfEmail.getText().trim();
        String telephone = tfTelephone.getText().trim();
        String password  = pfPassword.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            afficherErreur("⚠️ Nom, prénom et email sont obligatoires."); return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            afficherErreur("⚠️ Format d'email invalide."); return;
        }
        if (!telephone.isEmpty() && !telephone.matches("^[0-9]{8,15}$")) {
            afficherErreur("⚠️ Téléphone invalide (8 à 15 chiffres)."); return;
        }
        if (!password.isEmpty() && password.length() < 4) {
            afficherErreur("⚠️ Mot de passe trop court (min. 4 caractères)."); return;
        }

        userConnecte.setNom(nom);
        userConnecte.setPrenom(prenom);
        userConnecte.setEmail(email);
        userConnecte.setTelephone(telephone);
        if (!password.isEmpty()) userConnecte.setPassword(password);

        service.modifier(userConnecte);
        SessionManager.getInstance().setUserConnecte(userConnecte);
        lblSidebarNom.setText(userConnecte.getNom() + " " + userConnecte.getPrenom());
        afficherSucces("✅ Profil mis à jour avec succès !");
        pfPassword.clear();
    }

    // ── Retour selon le rôle ─────────────────────────────────────────────────────
    @FXML
    private void retourAccueil() {
        System.out.println("DEBUG Profil: Tentative de retour accueil pour role: " + userConnecte.getRole());
        if ("ADMIN".equalsIgnoreCase(userConnecte.getRole())) {
            naviguerVers("/com/pidev/AdminDashboard.fxml", "Administration", 1100, 700);
        } else {
            naviguerVers("/com/pidev/UserHome.fxml", "Espace Utilisateur", 1100, 700);
        }
    }

    @FXML
    private void deconnecter() {
        SessionManager.getInstance().deconnecter();
        naviguerVers("/com/pidev/Login.fxml", "Connexion", 420, 420);
    }

    private void naviguerVers(String fxml, String titre, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.setTitle(titre);
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (IOException e) { e.printStackTrace(); }
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
