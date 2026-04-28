package com.pidev.controllers.blog;

import com.pidev.entities.Publication;
import com.pidev.entities.User;
import com.pidev.services.PublicationService;
import com.pidev.tools.AuthSession;
import com.pidev.tools.BadWordsFilter;
import com.pidev.tools.GeminiVisionUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Base64;

public class PublicationFormController {

    @FXML private TextField titreField;
    @FXML private TextArea descArea;
    @FXML private TextField imageField;
    @FXML private Label titreError;
    @FXML private Label descError;
    @FXML private Label imageError;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button analyzeBtn;

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
            com.pidev.tools.NotificationUtil.showSuccess("Cloudinary", "Téléchargement de l'image vers le cloud...");
            
            // Perform upload in a background thread to keep UI responsive
            new Thread(() -> {
                String cloudUrl = com.pidev.services.CloudinaryService.getInstance().uploadImage(selectedFile);
                javafx.application.Platform.runLater(() -> {
                    if (cloudUrl != null) {
                        imageField.setText(cloudUrl);
                        com.pidev.tools.NotificationUtil.showSuccess("Cloudinary", "Image hébergée avec succès !");
                    } else {
                        com.pidev.tools.NotificationUtil.showError("Cloudinary", "Échec du téléchargement vers le cloud.");
                        // Fallback to local URI so AI features still work
                        imageField.setText(selectedFile.toURI().toString());
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleAiAnalyze() {
        String imagePath = imageField.getText().trim();
        if (imagePath.isEmpty()) {
            com.pidev.tools.NotificationUtil.showError("Erreur AI", "Veuillez d'abord uploader une image (📁).");
            return;
        }

        com.pidev.tools.NotificationUtil.showSuccess("AI en cours...", "Gemini analyse votre image, veuillez patienter...");
        if(analyzeBtn != null) {
            analyzeBtn.setDisable(true);
            analyzeBtn.setText("⏳ Traitement...");
        }

        new Thread(() -> {
            try {
                String base64 = null;
                if (imagePath.startsWith("file:/")) {
                    java.net.URI uri = new java.net.URI(imagePath);
                    java.io.File file = new java.io.File(uri);
                    byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
                    base64 = java.util.Base64.getEncoder().encodeToString(fileContent);
                } else if (imagePath.startsWith("http")) {
                    base64 = com.pidev.tools.GeminiVisionUtil.getBase64FromUrl(imagePath);
                }

                if (base64 == null) {
                    throw new Exception("Impossible de récupérer les données de l'image.");
                }

                String result = com.pidev.tools.GeminiVisionUtil.analyzeImage(base64);

                javafx.application.Platform.runLater(() -> {
                    if(analyzeBtn != null) {
                        analyzeBtn.setDisable(false);
                        analyzeBtn.setText("✨ AI Analyze");
                    }

                    if (result != null) {
                        String title = "";
                        String desc = "";
                        for (String line : result.split("\n")) {
                            if (line.startsWith("TITLE:")) title = line.replace("TITLE:", "").trim();
                            else if (line.startsWith("DESCRIPTION:")) desc = line.replace("DESCRIPTION:", "").trim();
                        }
                        
                        if (title.isEmpty() && desc.isEmpty()) desc = result;

                        if (titreField.getText().isEmpty() && !title.isEmpty()) titreField.setText(title);
                        if (descArea.getText().isEmpty() && !desc.isEmpty()) descArea.setText(desc);
                        
                        com.pidev.tools.NotificationUtil.showSuccess("Magie AI ✨", "Titre et description générés avec succès !");
                    } else {
                        com.pidev.tools.NotificationUtil.showError("Erreur AI", "Impossible de générer le contenu.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    if(analyzeBtn != null) {
                        analyzeBtn.setDisable(false);
                        analyzeBtn.setText("✨ AI Analyze");
                    }
                    com.pidev.tools.NotificationUtil.showError("Erreur AI", "Une erreur est survenue lors de l'analyse.");
                });
            }
        }).start();
    }

    private void handleSave() {
        if (!validateForm()) return;
        if (!checkImageModeration()) return;

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
            
            // Send email notification
            if (currentUser != null && currentUser.getEmail() != null) {
                String userName = currentUser.getEmail().split("@")[0]; // extract name from email
                com.pidev.tools.EmailUtil.sendPublicationEmail(currentUser.getEmail(), userName, pub.getTitre());
            }
        }
        ((Stage)saveBtn.getScene().getWindow()).close();
    }

    private boolean checkImageModeration() {
        String imagePath = imageField.getText().trim();
        if (imagePath.isEmpty()) return true;

        try {
            String base64 = null;
            if (imagePath.startsWith("file:/")) {
                URI uri = new URI(imagePath);
                File file = new File(uri);
                byte[] fileContent = Files.readAllBytes(file.toPath());
                base64 = Base64.getEncoder().encodeToString(fileContent);
            } else if (imagePath.startsWith("http")) {
                base64 = com.pidev.tools.GeminiVisionUtil.getBase64FromUrl(imagePath);
            }

            if (base64 == null) return true; // Skip if unreachable

            com.pidev.tools.NotificationUtil.showSuccess("Moderation AI", "Verification de l'image en cours...");
            boolean unsafe = GeminiVisionUtil.isSensitiveOrViolentImage(base64);
            if (unsafe) {
                com.pidev.tools.NotificationUtil.showError(
                        "Image refusee",
                        "Image sensible detectee (violence ou contenu inapproprie). Choisissez une autre image."
                );
                showError(imageError, "Image refusee par moderation AI.");
                return false;
            }
            return true;
        } catch (Exception e) {
            return true; // Soft fail on moderation error
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        resetErrors();

        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            showError(titreError, "Le titre est obligatoire.");
            isValid = false;
        } else if (titre.length() < 5 || titre.length() > 100) {
            com.pidev.tools.NotificationUtil.showError("Contrôle de Saisie", "Le titre doit faire entre 5 et 100 caractères.");
            showError(titreError, "Taille invalide.");
            isValid = false;
        } else {
            int countTitre = BadWordsFilter.countBadWords(titre);
            if (countTitre > 0) {
                com.pidev.tools.NotificationUtil.showModerationStorm(
                        countTitre,
                        "Le titre contient " + countTitre + " mot(s) interdit(s)."
                );
                showError(titreError, "Le titre contient des mots inappropriés.");
                isValid = false;
            }
        }

        String desc = descArea.getText().trim();
        if (desc.isEmpty()) {
            showError(descError, "Le contenu est obligatoire.");
            isValid = false;
        } else if (desc.length() < 20) {
            com.pidev.tools.NotificationUtil.showError("Contrôle de Saisie", "La description doit comporter au moins 20 caractères.");
            showError(descError, "Description trop courte.");
            isValid = false;
        } else {
            int countDesc = BadWordsFilter.countBadWords(desc);
            if (countDesc > 0) {
                com.pidev.tools.NotificationUtil.showModerationStorm(
                        countDesc,
                        "Le contenu contient " + countDesc + " mot(s) interdit(s)."
                );
                showError(descError, "Le contenu contient des mots inappropriés.");
                isValid = false;
            }
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
