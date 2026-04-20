package com.pidev.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ParticipationListController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button resetBtn;
    @FXML private Label searchInfoLabel;
    @FXML private TableView<?> table;
    @FXML private TableColumn<?, ?> colId, colEvenement, colDate, colPlaces, colStatut, colPaiement, colActions;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO: load participations, configure table columns
    }

    @FXML private void onAdd() { /* TODO: navigate to add participation form */ }
    @FXML private void onSearch() { /* TODO: filter table */ }
    @FXML private void onReset() { /* TODO: clear search */ }
}
