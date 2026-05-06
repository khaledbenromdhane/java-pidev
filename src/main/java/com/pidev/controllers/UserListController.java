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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserListController implements Initializable {

    @FXML private TableView<User>            tableUser;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colPrenom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colTelephone;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TextField                  tfRecherche;
    @FXML private ComboBox<String>           cbTri;
    @FXML private Label                      lblCompteur;

    private final CrudService service = new CrudService();
    private List<User>        tousLesUsers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Options de tri
        cbTri.getItems().addAll(
                "Nom (A → Z)",
                "Nom (Z → A)",
                "Prénom (A → Z)",
                "Prénom (Z → A)",
                "Role (ADMIN en premier)"
        );
        cbTri.setValue("Nom (A → Z)");

        chargerDonnees();

        // Recherche dynamique en temps réel
        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerEtTrier());
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> filtrerEtTrier());
    }

    // ── Charger tous les utilisateurs ────────────────────────────────────────────
    private void chargerDonnees() {
        tousLesUsers = service.afficher();
        filtrerEtTrier();
    }

    // ── Filtrer + Trier ───────────────────────────────────────────────────────────
    private void filtrerEtTrier() {
        String recherche = tfRecherche.getText().trim().toLowerCase();

        // Filtrage
        List<User> filtre = tousLesUsers.stream()
                .filter(u ->
                        u.getNom().toLowerCase().contains(recherche) ||
                        u.getPrenom().toLowerCase().contains(recherche) ||
                        u.getEmail().toLowerCase().contains(recherche) ||
                        u.getTelephone().toLowerCase().contains(recherche) ||
                        u.getRole().toLowerCase().contains(recherche)
                )
                .collect(Collectors.toList());

        // Tri
        String tri = cbTri.getValue();
        if (tri != null) {
            switch (tri) {
                case "Nom (A → Z)"            -> filtre.sort(Comparator.comparing(User::getNom));
                case "Nom (Z → A)"            -> filtre.sort(Comparator.comparing(User::getNom).reversed());
                case "Prénom (A → Z)"         -> filtre.sort(Comparator.comparing(User::getPrenom));
                case "Prénom (Z → A)"         -> filtre.sort(Comparator.comparing(User::getPrenom).reversed());
                case "Role (ADMIN en premier)"-> filtre.sort(Comparator.comparing(User::getRole));
            }
        }

        ObservableList<User> liste = FXCollections.observableArrayList(filtre);
        tableUser.setItems(liste);
        lblCompteur.setText("Total : " + filtre.size() + " utilisateur(s)");
    }

    // ── Rechercher (bouton) ───────────────────────────────────────────────────────
    @FXML
    private void rechercher() {
        filtrerEtTrier();
    }

    // ── Réinitialiser ─────────────────────────────────────────────────────────────
    @FXML
    private void reinitialiser() {
        tfRecherche.clear();
        cbTri.setValue("Nom (A → Z)");
        filtrerEtTrier();
    }

    // ── Afficher tous ─────────────────────────────────────────────────────────────
    @FXML
    public void afficherTous() {
        tfRecherche.clear();
        cbTri.setValue("Nom (A → Z)");
        chargerDonnees();
    }

    // ── Actualiser ────────────────────────────────────────────────────────────────
    @FXML
    public void actualiser() {
        chargerDonnees();
    }

    // ── Ajouter ───────────────────────────────────────────────────────────────────
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
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur : " + e.getMessage());
        }
    }

    // ── Modifier ──────────────────────────────────────────────────────────────────
    @FXML
    private void ouvrirModifier() {
        User selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Veuillez sélectionner un utilisateur à modifier.");
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
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur : " + e.getMessage());
        }
    }

    // ── Supprimer ─────────────────────────────────────────────────────────────────
    @FXML
    private void supprimer() {
        User selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur");
        confirm.setContentText("Voulez-vous vraiment supprimer "
                + selected.getNom() + " " + selected.getPrenom() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.supprimer(selected.getId_user());
            chargerDonnees();
        }
    }

    private void afficherAlerte(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
