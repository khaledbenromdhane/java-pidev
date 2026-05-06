package com.pidev.controllers;

import com.pidev.SessionManager;
import com.pidev.entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class AvatarStudioController implements Initializable {

    @FXML private Circle circlePreview;
    @FXML private javafx.scene.control.ProgressIndicator progressLoading;
    @FXML private ComboBox<String> cbGenre;
    @FXML private ComboBox<String> cbVetements;
    @FXML private ComboBox<String> cbCheveux;
    @FXML private ComboBox<String> cbPeau;
    @FXML private ComboBox<String> cbLunettes;
    @FXML private ComboBox<String> cbBarbe;

    private String currentUrl;
    private User user;

    private final String[] cheveuxFemme = {"bigHair", "bob", "bun", "curly", "curvy", "frida", "frizzle", "longHair", "longHairCurly"};
    private final String[] cheveuxHomme = {"dreads", "shaggy", "sides", "shortHair", "turban", "winterHat1", "winterHat2"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        user = SessionManager.getInstance().getUserConnecte();
        
        cbGenre.getItems().addAll("Homme", "Femme");
        cbGenre.setValue("Homme");

        cbVetements.getItems().addAll("blazerAndShirt", "collarAndSweater", "graphicShirt", "hoodie", "overall", "shirtVNeck");
        cbPeau.getItems().addAll("614335", "ae5d29", "d08b5b", "edb98a", "f8d25c", "ffdbb4", "tanned");
        cbLunettes.getItems().addAll("Aucun", "kurt", "prescription01", "prescription02", "round", "sunglasses", "wayfarers");
        cbBarbe.getItems().addAll("Aucun", "beardLight", "beardMedium", "moustacheFancy", "moustacheMagnum");

        cbVetements.setValue("hoodie");
        cbPeau.setValue("ffdbb4");
        cbLunettes.setValue("Aucun");
        cbBarbe.setValue("Aucun");
        
        configurerCheveux("Homme");
        mettreAJourPreview();
    }

    @FXML
    private void changerGenre() {
        String genre = cbGenre.getValue();
        configurerCheveux(genre);
        if ("Femme".equals(genre)) {
            cbBarbe.setValue("Aucun");
            cbBarbe.setDisable(true);
        } else {
            cbBarbe.setDisable(false);
        }
        mettreAJourPreview();
    }

    private void configurerCheveux(String genre) {
        cbCheveux.getItems().clear();
        if ("Femme".equals(genre)) {
            cbCheveux.getItems().addAll(cheveuxFemme);
            cbCheveux.setValue("curvy");
        } else {
            cbCheveux.getItems().addAll(cheveuxHomme);
            cbCheveux.setValue("shaggy");
        }
    }

    @FXML
    private void mettreAJourPreview() {
        String seed = (user != null) ? user.getNom() : "default";
        String vetement = cbVetements.getValue();
        String cheveux = cbCheveux.getValue();
        String peau = cbPeau.getValue();
        String lunettes = cbLunettes.getValue().equals("Aucun") ? "" : cbLunettes.getValue();
        String barbe = cbBarbe.getValue().equals("Aucun") ? "" : cbBarbe.getValue();
        
        String encodedSeed = seed.replace(" ", "%20");
        currentUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + encodedSeed + 
                     "&clothing=" + vetement + 
                     "&top=" + cheveux +
                     "&skinColor=" + peau +
                     "&accessories=" + lunettes +
                     "&facialHair=" + barbe;
        
        System.out.println("DEBUG Studio: Chargement de " + currentUrl);
        
        // Afficher l'indicateur de chargement
        progressLoading.setVisible(true);
        
        // Chargement Asynchrone pour la vitesse (true)
        Image img = new Image(currentUrl, true);
        
        img.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                javafx.application.Platform.runLater(() -> {
                    progressLoading.setVisible(false);
                    if (!img.isError()) {
                        circlePreview.setFill(new ImagePattern(img, 0, 0, 1, 1, true));
                        System.out.println("✅ Preview mise à jour (Full)");
                    }
                });
            }
        });
    }

    @FXML
    private void genererAleatoire() {
        Random r = new Random();
        cbVetements.getSelectionModel().select(r.nextInt(cbVetements.getItems().size()));
        cbCheveux.getSelectionModel().select(r.nextInt(cbCheveux.getItems().size()));
        cbPeau.getSelectionModel().select(r.nextInt(cbPeau.getItems().size()));
        cbLunettes.getSelectionModel().select(r.nextInt(cbLunettes.getItems().size()));
        cbBarbe.getSelectionModel().select(r.nextInt(cbBarbe.getItems().size()));
        mettreAJourPreview();
    }

    @FXML
    private void validerAvatar() {
        if (user != null) {
            user.setPhoto(currentUrl);
            retourProfil();
        }
    }

    @FXML
    private void retourProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/Profil.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) circlePreview.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
