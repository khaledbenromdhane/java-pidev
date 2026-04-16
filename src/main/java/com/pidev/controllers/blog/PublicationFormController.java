package com.pidev.controllers.blog;

import com.pidev.entities.Publication;
import com.pidev.entities.User;
import com.pidev.services.PublicationService;
import com.pidev.tools.AuthSession;
import com.pidev.tools.BadWordsFilter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PublicationFormController {

    @FXML private TextField titreField;
    @FXML private TextArea descArea;
    @FXML private TextField imageField;
    @FXML private Label titreError;
    @FXML private Label descError;
    @FXML private Label imageError;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private final PublicationService pubService = new PublicationService();
    private User currentUser = AuthSession.getCurrentUser();
    private Publication existingPublication = null;

    @FXML
    public void initialize() {
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> ((Stage)saveBtn.getScene().getWindow()).close());
    }

    public void setPublication(Publication pub) {
        this.existingPublication = pub;
        titreField.setText(pub.getTitre());
        descArea.setText(pub.getDescription());
        imageField.setText(pub.getImage());
        saveBtn.setText("METTRE À JOUR");
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(saveBtn.getScene().getWindow());
        if (selectedFile != null) {
            imageField.setText(selectedFile.toURI().toString());
        }
    }

    private void handleSave() {
        if (!validateForm()) return;

        if (existingPublication != null) {
            existingPublication.setTitre(titreField.getText().trim());
            existingPublication.setDescription(descArea.getText().trim());
            existingPublication.setImage(imageField.getText().trim());
            existingPublication.setSlug(existingPublication.getTitre().toLowerCase().replace(" ", "-"));
            pubService.modifier(existingPublication);
        } else {
            Publication pub = new Publication();
            pub.setTitre(titreField.getText().trim());
            pub.setDescription(descArea.getText().trim());
            pub.setImage(imageField.getText().trim());
            pub.setIdUser(currentUser.getId_user());
            pub.setSlug(pub.getTitre().toLowerCase().replace(" ", "-"));
            pubService.ajouter(pub);
        }
        ((Stage)saveBtn.getScene().getWindow()).close();
    }

    private boolean validateForm() {
        boolean isValid = true;
        resetErrors();

        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            showError(titreError, "Le titre est obligatoire.");
            isValid = false;
        } else if (BadWordsFilter.containsBadWords(titre)) {
            showError(titreError, "Le titre contient des mots inappropriés.");
            isValid = false;
        }

        String desc = descArea.getText().trim();
        if (desc.isEmpty()) {
            showError(descError, "Le contenu est obligatoire.");
            isValid = false;
        } else if (BadWordsFilter.containsBadWords(desc)) {
            showError(descError, "Le contenu contient des mots inappropriés.");
            isValid = false;
        }

        String img = imageField.getText().trim();
        if (img.isEmpty()) {
            showError(imageError, "L'image est obligatoire.");
            isValid = false;
        } else if (!img.toLowerCase().startsWith("http") && !img.toLowerCase().startsWith("file")) {
            showError(imageError, "Veuillez entrer une URL valide (http...) ou choisir un fichier local.");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label label, String message) {
        label.setText("⚠️ " + message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void resetErrors() {
        titreError.setVisible(false);
        titreError.setManaged(false);
        descError.setVisible(false);
        descError.setManaged(false);
        imageError.setVisible(false);
        imageError.setManaged(false);
    }
}
