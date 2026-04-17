package com.pidev.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.util.ResourceBundle;

public class ParticipationShowController implements Initializable {

    @FXML private Label idBadge, breadcrumbId;
    @FXML private Label evenementValue, dateValue, nbrValue, statutValue, paiementValue;
    @FXML private Label lieuValue, evtDateValue;
    @FXML private HBox lieuRow, evtDateRow;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO: load participation data and populate fields
    }

    @FXML private void onBack() { /* TODO: navigate to list */ }
    @FXML private void onEdit() { /* TODO: navigate to edit form */ }
    @FXML private void onDelete() { /* TODO: confirm and delete */ }
}
