package esprit.tn.controllers;

import esprit.tn.models.formation;
import esprit.tn.services.serviceformation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le formulaire d'ajout de formation
 *
 * @author ESPRIT
 * @version 1.0
 */
public class AddFormationController implements Initializable {

    // Services
    private serviceformation formationService = new serviceformation();

    // Éléments FXML
    @FXML private TextField nomFormField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    // Référence à la fenêtre parente pour la fermer après l'ajout
    private Stage dialogStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration des tooltips
        nomFormField.setTooltip(new Tooltip("Nom de la formation (obligatoire)"));
        typeComboBox.setTooltip(new Tooltip("Type de formation (obligatoire)"));
        descriptionArea.setTooltip(new Tooltip("Description détaillée de la formation"));
        datePicker.setTooltip(new Tooltip("Date de la formation (obligatoire)"));

        // Initialisation des types de formation
        typeComboBox.setItems(FXCollections.observableArrayList(
            "Technique", "Management", "Développement Personnel", "Langues", "Autre"
        ));

        // Configuration des boutons
        saveBtn.setTooltip(new Tooltip("Enregistrer la nouvelle formation"));
        cancelBtn.setTooltip(new Tooltip("Annuler et fermer la fenêtre"));
    }

    /**
     * Définit la référence à la fenêtre de dialogue
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Gestionnaire pour le bouton Enregistrer
     */
    @FXML
    private void handleSave() {
        if (validateForm()) {
            formation newFormation = createFormationFromForm();

            if (formationService.addFormation(newFormation)) {
                showSuccess("Formation ajoutée", "La formation '" + newFormation.getNom_form() + "' a été ajoutée avec succès!");
                dialogStage.close();
            } else {
                showError("Erreur d'ajout", "Impossible d'ajouter la formation. Vérifiez la connexion à la base de données.");
            }
        }
    }

    /**
     * Gestionnaire pour le bouton Annuler
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Valide les données du formulaire
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Validation du nom
        if (nomFormField.getText() == null || nomFormField.getText().trim().isEmpty()) {
            errors.append("• Le nom de la formation est obligatoire\n");
        }

        // Validation du type
        if (typeComboBox.getValue() == null || typeComboBox.getValue().trim().isEmpty()) {
            errors.append("• Le type de formation est obligatoire\n");
        }

        // Validation de la date
        if (datePicker.getValue() == null) {
            errors.append("• La date de formation est obligatoire\n");
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            errors.append("• La date de formation ne peut pas être dans le passé\n");
        }

        if (errors.length() > 0) {
            showError("Erreurs de validation", errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Crée un objet Formation à partir des données du formulaire
     */
    private formation createFormationFromForm() {
        String nomForm = nomFormField.getText().trim();
        String type = typeComboBox.getValue().trim();
        String description = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";
        Date dateForm = Date.valueOf(datePicker.getValue());

        return new formation(nomForm, type, description, dateForm);
    }

    /**
     * Affiche une boîte de dialogue de succès
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
