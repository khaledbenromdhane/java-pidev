package com.pidev.controllers;

import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class UserListController {

    @FXML private FlowPane userContainer;
    @FXML private TextField searchField;

    private final CrudService service = new CrudService();

    @FXML
    public void initialize() {
        actualiser();
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            actualiser();
        });
    }

    @FXML
    public void actualiser() {
        userContainer.getChildren().clear();
        List<User> users = service.afficher();
        String query = searchField.getText().toLowerCase().trim();

        for (User u : users) {
            if (!query.isEmpty() && !u.getNom().toLowerCase().contains(query) && !u.getEmail().toLowerCase().contains(query)) {
                continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/UserCard.fxml"));
                Parent card = loader.load();
                UserCardController controller = loader.getController();
                controller.setData(u, this::actualiser);
                userContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void retour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/pidev/AdminDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirAjouter() {
        // Implementation for adding user could be done here similar to existing logic
    }
}
