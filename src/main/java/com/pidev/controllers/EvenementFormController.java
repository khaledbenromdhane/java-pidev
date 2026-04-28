package com.pidev.controllers;

import com.pidev.entities.Evenement;
import com.pidev.services.CrudService;
import com.pidev.services.EvenementJdbcService;
import com.pidev.services.GeminiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class EvenementFormController implements Initializable {

    @FXML private TextField nomField, heureField, prixField;
    @FXML private ComboBox<String> typeBox;
    @FXML private Spinner<Integer> participantsSpinner;
    @FXML private DatePicker datePicker;
    @FXML private TextField lieuField;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox paiementCheck;
    @FXML private Button imageBtn;
    @FXML private Button generateDescBtn;
    @FXML private Label imageNameLabel;
    @FXML private Label generateStatus;
    @FXML private Label nomError, typeError, dateError, heureError, lieuError, descError, prixError, imageError;

    private final EvenementJdbcService service = new EvenementJdbcService();
    private final CrudService<Evenement, Integer> crudService = service;
    private final GeminiService geminiService = new GeminiService();
    private File selectedImageFile;
    private Runnable onSaved;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        typeBox.getItems().setAll(Evenement.TYPES);
        paiementCheck.selectedProperty().addListener((obs, oldV, paid) -> updatePrixVisibility());
        updatePrixVisibility();
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void onBack() {
        closeWindow();
    }

    @FXML
    private void onGenerateDescription() {
        String nom = nomField.getText();
        String type = typeBox.getValue();
        String lieu = lieuField.getText();
        String date = datePicker.getValue() == null ? "" : datePicker.getValue().toString();
        String heure = heureField.getText();
        int nbParticipants = participantsSpinner.getValue();

        // Require at least a name
        if (nom == null || nom.isBlank()) {
            showError("Champ requis", "Veuillez saisir au moins le nom de l'événement avant de générer une description.");
            return;
        }

        generateDescBtn.setDisable(true);
        generateStatus.setText("⏳ Génération en cours…");

        Thread thread = new Thread(() -> {
            try {
                String description = geminiService.generateDescription(nom, type, lieu, date, heure, nbParticipants);
                Platform.runLater(() -> {
                    descriptionArea.setText(description);
                    generateStatus.setText("✅ Description générée !");
                    generateDescBtn.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    generateStatus.setText("❌ Erreur lors de la génération");
                    generateDescBtn.setDisable(false);
                    showError("Erreur IA", "Impossible de générer la description : " + ex.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onSave() {
        clearErrors();

        Map<String, Object> data = buildFormData();
        Map<String, String> errors = service.validate(data, false);
        if (!errors.isEmpty()) {
            applyErrors(errors);
            return;
        }

        try {
            Evenement evt = toEntity(data);
            String storedImage = service.storeImage(selectedImageFile, null);
            evt.setImage(storedImage);
            crudService.create(evt);

            showInfo("Succès", "Événement créé avec succès !");
            if (onSaved != null) {
                onSaved.run();
            }
            closeWindow();
        } catch (Exception ex) {
            showError("Erreur", "Impossible de créer l'événement.");
        }
    }

    @FXML
    private void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        File file = chooser.showOpenDialog(imageBtn.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageNameLabel.setText(file.getName());
        }
    }

    private Map<String, Object> buildFormData() {
        Map<String, Object> data = new HashMap<>();
        data.put("nom", nomField.getText());
        data.put("type_evenement", typeBox.getValue());
        data.put("nbr_participant", String.valueOf(participantsSpinner.getValue()));
        data.put("date", datePicker.getValue() == null ? "" : datePicker.getValue().toString());
        data.put("heure", heureField.getText());
        data.put("lieu", lieuField.getText());
        data.put("description", descriptionArea.getText());
        data.put("paiement", paiementCheck.isSelected());
        data.put("prix", prixField.getText());
        data.put("image_file", selectedImageFile);
        return data;
    }

    private Evenement toEntity(Map<String, Object> data) {
        Evenement evt = new Evenement();
        evt.setNom(data.get("nom").toString().trim());
        evt.setTypeEvenement(data.get("type_evenement").toString());
        evt.setNbrParticipant(Integer.parseInt(data.get("nbr_participant").toString()));
        evt.setDate(datePicker.getValue());
        evt.setHeure(LocalTime.parse(data.get("heure").toString().trim(), DateTimeFormatter.ofPattern("HH:mm")));
        evt.setLieu(data.get("lieu").toString().trim());
        evt.setDescription(data.get("description").toString().trim());
        evt.setPaiement(paiementCheck.isSelected());
        if (paiementCheck.isSelected()) {
            evt.setPrix(Float.parseFloat(data.get("prix").toString().replace(',', '.')));
        } else {
            evt.setPrix(null);
        }
        return evt;
    }

    private void updatePrixVisibility() {
        boolean paid = paiementCheck.isSelected();
        prixField.setDisable(!paid);
        if (!paid) {
            prixField.clear();
        }
    }

    private void clearErrors() {
        hideError(nomError);
        hideError(typeError);
        hideError(dateError);
        hideError(heureError);
        hideError(lieuError);
        hideError(descError);
        hideError(prixError);
        hideError(imageError);
    }

    private void applyErrors(Map<String, String> errors) {
        showIfPresent(errors, "nom", nomError);
        showIfPresent(errors, "type_evenement", typeError);
        showIfPresent(errors, "date", dateError);
        showIfPresent(errors, "heure", heureError);
        showIfPresent(errors, "lieu", lieuError);
        showIfPresent(errors, "description", descError);
        showIfPresent(errors, "prix", prixError);
        showIfPresent(errors, "image", imageError);

        String firstError = errors.values().iterator().next();
        showError("Validation", firstError);
    }

    private void showIfPresent(Map<String, String> errors, String key, Label label) {
        String value = errors.get(key);
        if (value != null) {
            label.setText(value);
            label.setManaged(true);
            label.setVisible(true);
        }
    }

    private void hideError(Label label) {
        label.setText("");
        label.setManaged(false);
        label.setVisible(false);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
