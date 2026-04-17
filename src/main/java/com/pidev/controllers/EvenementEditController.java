package com.pidev.controllers;

import com.pidev.entities.Evenement;
import com.pidev.services.EvenementJdbcService;
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

public class EvenementEditController implements Initializable {

    @FXML private Label idBadge, breadcrumbId;
    @FXML private TextField nomField, heureField, lieuField, prixField;
    @FXML private ComboBox<String> typeBox;
    @FXML private Spinner<Integer> participantsSpinner;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox paiementCheck;
    @FXML private Button imageBtn;
    @FXML private Label imageNameLabel, nomError, typeError, dateError, heureError, lieuError, descError, prixError, imageError;

    private final EvenementJdbcService service = new EvenementJdbcService();
    private File selectedImageFile;
    private Runnable onSaved;
    private Evenement currentEvent;
    private Integer eventId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        typeBox.getItems().setAll(Evenement.TYPES);
        paiementCheck.selectedProperty().addListener((obs, oldV, paid) -> updatePrixVisibility());
        updatePrixVisibility();

        if (eventId != null) {
            loadEvent();
        }
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
        if (nomField != null) {
            loadEvent();
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void onBack() {
        closeWindow();
    }

    @FXML
    private void onSave() {
        if (currentEvent == null) {
            showError("Erreur", "Événement introuvable.");
            return;
        }

        clearErrors();
        Map<String, Object> data = buildFormData();
        Map<String, String> errors = service.validate(data, true);
        if (!errors.isEmpty()) {
            applyErrors(errors);
            return;
        }

        try {
            Evenement updated = toEntity(data);
            updated.setId(currentEvent.getId());
            updated.setImage(service.storeImage(selectedImageFile, currentEvent.getImage()));
            service.update(updated);

            showInfo("Succès", "Événement modifié avec succès !");
            if (onSaved != null) {
                onSaved.run();
            }
            closeWindow();
        } catch (Exception ex) {
            showError("Erreur", "Impossible de modifier l'événement.");
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

    private void loadEvent() {
        currentEvent = service.findById(eventId).orElse(null);
        if (currentEvent == null) {
            return;
        }

        idBadge.setText("# ID: " + currentEvent.getId());
        breadcrumbId.setText("Modifier #" + currentEvent.getId());
        nomField.setText(currentEvent.getNom());
        typeBox.setValue(currentEvent.getTypeEvenement());
        participantsSpinner.getValueFactory().setValue(currentEvent.getNbrParticipant());
        datePicker.setValue(currentEvent.getDate());
        heureField.setText(currentEvent.getHeure() == null ? "" : currentEvent.getHeure().format(DateTimeFormatter.ofPattern("HH:mm")));
        lieuField.setText(currentEvent.getLieu());
        descriptionArea.setText(currentEvent.getDescription());
        paiementCheck.setSelected(Boolean.TRUE.equals(currentEvent.getPaiement()));
        prixField.setText(currentEvent.getPrix() == null ? "" : String.valueOf(currentEvent.getPrix()));
        imageNameLabel.setText(currentEvent.getImage() == null || currentEvent.getImage().isBlank() ? "Aucun fichier choisi" : currentEvent.getImage());
        updatePrixVisibility();
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

        showError("Validation", errors.values().iterator().next());
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
