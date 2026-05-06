package com.pidev.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.util.ResourceBundle;

public class EvenementShowController implements Initializable {

    @FXML private Label idBadge, breadcrumbName;
    @FXML private Label nomValue, typeValue, participantsValue, dateValue, heureValue, lieuValue, paiementValue, descriptionValue;
    @FXML private ImageView eventImage;
    @FXML private HBox imageContainer;
    @FXML private Button deleteBtn, editBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO: load event data and populate fields
    }

    @FXML private void onBack() { /* TODO: navigate to list */ }
    @FXML private void onEdit() { /* TODO: navigate to edit form */ }
    @FXML private void onDelete() { /* TODO: confirm and delete */ }
}
