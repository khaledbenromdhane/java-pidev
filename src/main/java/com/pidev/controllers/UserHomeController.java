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
    @FXML private javafx.scene.shape.Circle circleTopAvatar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SessionManager.getInstance().getUserConnecte();
        if (user != null) {
            lblBienvenue.setText("Bonjour, " + user.getNom() + " " + user.getPrenom() + " 👋");
            lblNomUser.setText(user.getNom() + " " + user.getPrenom());
            lblRoleUser.setText(user.getRole());
            chargerPhoto(user);
        }
    }

    private void chargerPhoto(User user) {
        String photoPath = user.getPhoto();
        System.out.println("DEBUG Home: photoPath = " + photoPath);

        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                javafx.scene.image.Image img;
                if (photoPath.startsWith("http")) {
                    String encodedPath = photoPath.replace(" ", "%20");
                    img = new javafx.scene.image.Image(encodedPath, true);
                    setCircleImage(img);
                    System.out.println("✅ Image URL chargée dans le header (encoded)");
                } else {
                    java.io.File file = new java.io.File(photoPath);
                    if (file.exists()) {
                        img = new javafx.scene.image.Image(file.toURI().toString());
                        setCircleImage(img);
                        System.out.println("✅ Image locale chargée dans le header");
                    } else {
                        System.err.println("⚠️ Fichier image non trouvé : " + photoPath);
                        chargerAvatarParDefaut(user);
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur chargement image : " + e.getMessage());
                chargerAvatarParDefaut(user);
            }
        } else {
            chargerAvatarParDefaut(user);
        }
    }

    private void chargerAvatarParDefaut(User user) {
        try {
            String name = java.net.URLEncoder.encode(user.getNom() + " " + user.getPrenom(), "UTF-8");
            String avatarUrl = "https://api.dicebear.com/7.x/initials/png?seed=" + name + "&backgroundColor=00acc1,1e88e5,5e35b1";
            
            javafx.scene.image.Image img = new javafx.scene.image.Image(avatarUrl, true);
            img.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    javafx.application.Platform.runLater(() -> setCircleImage(img));
                }
            });
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void setCircleImage(javafx.scene.image.Image img) {
        if (img == null || img.isError()) return;

        if (img.getProgress() == 1.0) {
            circleTopAvatar.setFill(new javafx.scene.paint.ImagePattern(img, 0, 0, 1, 1, true));
        } else {
            img.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !img.isError()) {
                    javafx.application.Platform.runLater(() -> {
                        circleTopAvatar.setFill(new javafx.scene.paint.ImagePattern(img, 0, 0, 1, 1, true));
                    });
                }
            });
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
            System.out.println("DEBUG Home: Navigation vers " + fxml);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            
            Stage stage = (Stage) lblBienvenue.getScene().getWindow();
            stage.setTitle(titre);
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            System.out.println("✅ Navigation Home réussie");
        } catch (IOException e) {
            System.err.println("❌ Erreur navigation Home : " + e.getMessage());
            e.printStackTrace();
            afficherInfo("Erreur", "Impossible de charger la page : " + fxml);
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue Home : " + e.getMessage());
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
