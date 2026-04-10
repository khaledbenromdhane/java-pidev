package com.pidev.controllers;

import com.pidev.entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EvenementFormController {
    @FXML private TextField nomField;
    @FXML private ComboBox<String> typeBox;
    @FXML private Spinner<Integer> participantsSpinner;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField lieuField;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox paiementCheck;
    @FXML private TextField imageField;

    private Evenement evenement;
    private boolean editable = true;

    @FXML
    private void initialize() {
        typeBox.getItems().setAll(Evenement.TYPES);
        heureField.setText("18:00");
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        if (evenement != null) {
            nomField.setText(evenement.getNom());
            typeBox.getSelectionModel().select(evenement.getTypeEvenement());
            participantsSpinner.getValueFactory().setValue(evenement.getNbrParticipant());
            datePicker.setValue(evenement.getDate());
            if (evenement.getHeure() != null) {
                heureField.setText(evenement.getHeure().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            lieuField.setText(evenement.getLieu());
            descriptionArea.setText(evenement.getDescription());
            paiementCheck.setSelected(Boolean.TRUE.equals(evenement.getPaiement()));
            imageField.setText(evenement.getImage());
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        nomField.setDisable(!editable);
        typeBox.setDisable(!editable);
        participantsSpinner.setDisable(!editable);
        datePicker.setDisable(!editable);
        heureField.setDisable(!editable);
        lieuField.setDisable(!editable);
        descriptionArea.setDisable(!editable);
        paiementCheck.setDisable(!editable);
        imageField.setDisable(!editable);
    }

    @FXML
    private void onSave() {
        if (!editable) {
            close();
            return;
        }
        if (evenement == null) {
            evenement = new Evenement();
        }
        evenement.setNom(nomField.getText());
        evenement.setTypeEvenement(typeBox.getValue());
        evenement.setNbrParticipant(participantsSpinner.getValue());
        evenement.setDate(datePicker.getValue());
        evenement.setLieu(lieuField.getText());
        evenement.setDescription(descriptionArea.getText());
        evenement.setPaiement(paiementCheck.isSelected());
        evenement.setImage(imageField.getText());
        evenement.setHeure(parseTime(heureField.getText()));
        close();
    }

    @FXML
    private void onBack() {
        close();
    }

    private void close() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
