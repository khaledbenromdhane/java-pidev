package com.pidev.controllers;

import com.pidev.SessionManager;
import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.collections.FXCollections;
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
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML private TableView<User>            tableUser;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colPrenom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colTelephone;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TextField                  tfRecherche;
    @FXML private ComboBox<String>           cbTri;
    @FXML private Label                      lblCompteur;
    @FXML private Label                      lblAdminNom;
    @FXML private Label                      lblStatTotal;
    @FXML private Label                      lblStatAdmin;
    @FXML private Label                      lblStatUser;

    private final CrudService service = new CrudService();
    private List<User> tousLesUsers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Nom admin
        User admin = SessionManager.getInstance().getUserConnecte();
        if (admin != null) lblAdminNom.setText(admin.getNom() + " " + admin.getPrenom());

        // Tri options
        cbTri.getItems().addAll("ID (croissant)", "Nom (A→Z)", "Nom (Z→A)",
                "Prénom (A→Z)", "Role (ADMIN en premier)");
        cbTri.setValue("ID (croissant)");

        chargerDonnees();

        // Recherche dynamique
        tfRecherche.textProperty().addListener((obs, o, n) -> filtrerEtTrier());
        cbTri.valueProperty().addListener((obs, o, n) -> filtrerEtTrier());
    }

    private void chargerDonnees() {
        tousLesUsers = service.afficher();
        majStats();
        filtrerEtTrier();
    }

    private void majStats() {
        long admins  = tousLesUsers.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).count();
        long simples = tousLesUsers.stream().filter(u -> "USER".equalsIgnoreCase(u.getRole())).count();
        lblStatTotal.setText(String.valueOf(tousLesUsers.size()));
        lblStatAdmin.setText(String.valueOf(admins));
        lblStatUser.setText(String.valueOf(simples));
    }

    private void filtrerEtTrier() {
        String recherche = tfRecherche.getText().trim().toLowerCase();
        List<User> filtre = tousLesUsers.stream()
                .filter(u -> u.getNom().toLowerCase().contains(recherche)
                        || u.getPrenom().toLowerCase().contains(recherche)
                        || u.getEmail().toLowerCase().contains(recherche)
                        || u.getRole().toLowerCase().contains(recherche))
                .collect(Collectors.toList());

        switch (Objects.requireNonNullElse(cbTri.getValue(), "")) {
            case "Nom (A→Z)"             -> filtre.sort(Comparator.comparing(User::getNom));
            case "Nom (Z→A)"             -> filtre.sort(Comparator.comparing(User::getNom).reversed());
            case "Prénom (A→Z)"          -> filtre.sort(Comparator.comparing(User::getPrenom));
            case "Role (ADMIN en premier)"-> filtre.sort(Comparator.comparing(User::getRole));
            default                       -> filtre.sort(Comparator.comparing(User::getId_user));
        }

        tableUser.setItems(FXCollections.observableArrayList(filtre));
        lblCompteur.setText(filtre.size() + " utilisateur(s) trouvé(s)");
    }

    @FXML public void actualiser() { chargerDonnees(); }

    @FXML private void afficherStats() { /* déjà ici */ }

    @FXML private void afficherUsers() { chargerDonnees(); }

    @FXML private void ouvrirAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/UserForm.fxml"));
            Parent root = loader.load();
            UserFormController ctrl = loader.getController();
            ctrl.setMode("AJOUTER", null, null);
            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> chargerDonnees());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void ouvrirModifier() {
        User sel = tableUser.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherAlerte("Sélectionnez un utilisateur."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/UserForm.fxml"));
            Parent root = loader.load();
            UserFormController ctrl = loader.getController();
            ctrl.setMode("MODIFIER", sel, null);
            Stage stage = new Stage();
            stage.setTitle("Modifier un utilisateur");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> chargerDonnees());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void supprimer() {
        User sel = tableUser.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherAlerte("Sélectionnez un utilisateur."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Supprimer " + sel.getNom() + " " + sel.getPrenom() + " ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) { service.supprimer(sel.getId_user()); chargerDonnees(); }
        });
    }

    @FXML private void retourAccueil() {
        naviguerVers("/com/pidev/UserHome.fxml", "Espace Utilisateur", 1100, 700);
    }

    @FXML private void deconnecter() {
        SessionManager.getInstance().deconnecter();
        naviguerVers("/com/pidev/Login.fxml", "Connexion", 420, 420);
    }

    private void naviguerVers(String fxml, String titre, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tableUser.getScene().getWindow();
            stage.setTitle(titre);
            stage.setScene(new Scene(root, w, h));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void afficherAlerte(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setContentText(msg);
        a.showAndWait();
    }
}
