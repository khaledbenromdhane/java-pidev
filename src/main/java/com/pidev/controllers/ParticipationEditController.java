package com.pidev.controllers;

import com.pidev.entities.Evenement;
import com.pidev.entities.Participation;
import com.pidev.services.CrudService;
import com.pidev.services.EvenementJdbcService;
import com.pidev.services.ParticipationJdbcService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ParticipationEditController implements Initializable {

    @FXML private Label idBadge, breadcrumbId;
    @FXML private ComboBox<String> evenementBox, statutBox, paiementBox;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> nbrSpinner;
    @FXML private Label evtError, dateError, nbrError, statutError, paiementError;
    @FXML private Label paiementLabel, paiementHint;

    private final ParticipationJdbcService participationService = new ParticipationJdbcService();
    private final CrudService<Participation, Integer> participationCrudService = participationService;
    private final EvenementJdbcService evenementService = new EvenementJdbcService();
    private final Map<String, Evenement> eventsByLabel = new HashMap<>();
    private Runnable onSaved;
    private Integer participationId;
    private Participation currentParticipation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statutBox.getItems().setAll(Participation.STATUTS);
        paiementBox.getItems().setAll(Participation.MODES_PAIEMENT);

        evenementService.findByDateAsc().forEach(evt -> {
            String label = evt.getNom() + " • " + evt.getDate();
            eventsByLabel.put(label, evt);
            evenementBox.getItems().add(label);
        });

        evenementBox.valueProperty().addListener((obs, oldV, newV) -> updatePaiementFields());
        updatePaiementFields();

        if (participationId != null) {
            loadParticipation();
        }
    }

    public void setParticipationId(Integer participationId) {
        this.participationId = participationId;
        if (evenementBox != null) {
            loadParticipation();
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
        if (currentParticipation == null) {
            showError("Erreur", "Participation introuvable.");
            return;
        }

        clearErrors();
        Evenement selectedEvent = eventsByLabel.get(evenementBox.getValue());

        Map<String, Object> data = new HashMap<>();
        data.put("id_evenement", selectedEvent == null ? "" : String.valueOf(selectedEvent.getId()));
        data.put("date_participation", datePicker.getValue() == null ? "" : datePicker.getValue().toString());
        data.put("nbr_participation", String.valueOf(nbrSpinner.getValue()));
        data.put("statut", statutBox.getValue());
        data.put("mode_paiement", paiementBox.isVisible() ? paiementBox.getValue() : "");

        Map<String, String> errors = participationService.validate(data, currentParticipation.getId(), true);
        if (!errors.isEmpty()) {
            applyErrors(errors);
            return;
        }

        try {
            currentParticipation.setEvenement(selectedEvent);
            currentParticipation.setDateParticipation(datePicker.getValue());
            currentParticipation.setNbrParticipation(nbrSpinner.getValue());
            currentParticipation.setStatut(statutBox.getValue());
            currentParticipation.setModePaiement(selectedEvent.getPaiement() ? paiementBox.getValue() : null);
            participationCrudService.update(currentParticipation);

            showInfo("Succès", "Participation modifiée avec succès !");
            if (onSaved != null) {
                onSaved.run();
            }
            closeWindow();
        } catch (Exception ex) {
            showError("Erreur", "Impossible de modifier la participation.");
        }
    }

    private void loadParticipation() {
        currentParticipation = participationCrudService.findById(participationId).orElse(null);
        if (currentParticipation == null) {
            return;
        }

        idBadge.setText("# ID: " + currentParticipation.getId());
        breadcrumbId.setText("Modifier #" + currentParticipation.getId());

        Evenement evt = currentParticipation.getEvenement();
        String eventLabel = evt.getNom() + " • " + evt.getDate();
        evenementBox.setValue(eventLabel);
        datePicker.setValue(currentParticipation.getDateParticipation());
        nbrSpinner.getValueFactory().setValue(currentParticipation.getNbrParticipation());
        statutBox.setValue(currentParticipation.getStatut());
        paiementBox.setValue(currentParticipation.getModePaiement());
        updatePaiementFields();
    }

    private void updatePaiementFields() {
        Evenement selectedEvent = eventsByLabel.get(evenementBox.getValue());
        boolean paid = selectedEvent != null && Boolean.TRUE.equals(selectedEvent.getPaiement());

        paiementLabel.setManaged(paid);
        paiementLabel.setVisible(paid);
        paiementBox.setManaged(paid);
        paiementBox.setVisible(paid);
        paiementHint.setManaged(paid);
        paiementHint.setVisible(paid);

        if (!paid) {
            paiementBox.getSelectionModel().clearSelection();
        }
    }

    private void clearErrors() {
        hideError(evtError);
        hideError(dateError);
        hideError(nbrError);
        hideError(statutError);
        hideError(paiementError);
    }

    private void applyErrors(Map<String, String> errors) {
        showIfPresent(errors, "id_evenement", evtError);
        showIfPresent(errors, "date_participation", dateError);
        showIfPresent(errors, "nbr_participation", nbrError);
        showIfPresent(errors, "statut", statutError);
        showIfPresent(errors, "mode_paiement", paiementError);
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
        Stage stage = (Stage) evenementBox.getScene().getWindow();
        stage.close();
    }
}
