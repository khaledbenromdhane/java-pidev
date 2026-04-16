package com.pidev.controllers;

import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

public class UserCardController {

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label statusLabel;
    @FXML private Label roleLabel;
    @FXML private Label phoneLabel;
    @FXML private Button banBtn;

    private User user;
    private final CrudService userService = new CrudService();
    private Runnable onRefresh;

    public void setData(User user, Runnable refreshCallback) {
        this.user = user;
        this.onRefresh = refreshCallback;

        nameLabel.setText(user.getNom() + " " + user.getPrenom());
        emailLabel.setText(user.getEmail());
        avatarLabel.setText(user.getNom().substring(0, 1).toUpperCase());
        roleLabel.setText(user.getRole());
        phoneLabel.setText(user.getTelephone());
        
        updateStatusUI();
    }

    private void updateStatusUI() {
        if (user.getEst_signale() == 1) {
            statusLabel.setText("BANNI");
            statusLabel.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 8;");
            banBtn.setText("Débannir");
            banBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #27ae60; -fx-text-fill: #27ae60; -fx-border-radius: 10; -fx-padding: 5 15;");
        } else {
            statusLabel.setText("ACTIF");
            statusLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 8;");
            banBtn.setText("Bannir");
            banBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ff6b6b; -fx-text-fill: #ff6b6b; -fx-border-radius: 10; -fx-padding: 5 15;");
        }
    }

    @FXML
    private void handleBan() {
        TextInputDialog dialog = new TextInputDialog("Violation des règles");
        dialog.setTitle("Bannissement");
        dialog.setHeaderText("Bannir l'utilisateur : " + user.getEmail());
        dialog.setContentText("Raison :");
        
        dialog.showAndWait().ifPresent(reason -> {
            user.setEst_signale(1);
            user.setRaison_signalement(reason);
            userService.modifier(user);
            updateStatusUI();
        });
    }

    @FXML
    private void handleUnban() {
        user.setEst_signale(0);
        user.setRaison_signalement("");
        userService.modifier(user);
        updateStatusUI();
    }

    @FXML
    private void handleEdit() {
        // Implementation for opening edit form could go here
    }
}
