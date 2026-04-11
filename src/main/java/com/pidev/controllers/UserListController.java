package com.pidev.controllers;

import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserListController implements Initializable {

    @FXML private TableView<User>           tableUser;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colPrenom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colTelephone;
    @FXML private TableColumn<User, String>  colRole;

    private final CrudService service = new CrudService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        chargerDonnees();
    }

    private void chargerDonnees() {
        ObservableList<User> liste = FXCollections.observableArrayList(service.afficher());
        tableUser.setItems(liste);
    }

    @FXML
    private void ouvrirAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/UserForm.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setMode("AJOUTER", null, this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherErreur("Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void ouvrirModifier() {
        User selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Veuillez sélectionner un utilisateur à modifier.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/UserForm.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setMode("MODIFIER", selected, this);

            Stage stage = new Stage();
            stage.setTitle("Modifier un utilisateur");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherErreur("Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void supprimer() {
        User selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur");
        confirm.setContentText("Voulez-vous vraiment supprimer " + selected.getNom() + " " + selected.getPrenom() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.supprimer(selected.getId_user());
            chargerDonnees();
        }
    }

    @FXML
    public void actualiser() {
        chargerDonnees();
    }

    private void afficherErreur(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
