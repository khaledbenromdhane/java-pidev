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
 * Contrôleur pour le formulaire de modification de formation
 *
 * @author ESPRIT
 * @version 1.0
 */
public class UpdateFormationController implements Initializable {

    // Services
    private final serviceformation formationService = new serviceformation();

    // Éléments FXML
    @FXML private TextField nomFormField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    // Référence à la fenêtre parente
    private Stage dialogStage;
    
    // La formation en cours de modification
    private formation currentFormation;

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
        saveBtn.setTooltip(new Tooltip("Enregistrer les modifications"));
        cancelBtn.setTooltip(new Tooltip("Annuler et fermer la fenêtre"));
    }

    /**
     * Définit la référence à la fenêtre de dialogue
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Remplit le formulaire avec les données de la formation sélectionnée
     */
    public void setFormationData(formation f) {
        this.currentFormation = f;
        
        nomFormField.setText(f.getNom_form());
        typeComboBox.setValue(f.getType());
        descriptionArea.setText(f.getDescription());
        if (f.getDate_form() != null) {
            datePicker.setValue(f.getDate_form().toLocalDate());
        }
    }

    /**
     * Gestionnaire pour le bouton Enregistrer
     */
    @FXML
    private void handleSave() {
        if (validateForm()) {
            // Mettre à jour l'objet actuel
            currentFormation.setNom_form(nomFormField.getText().trim());
            currentFormation.setType(typeComboBox.getValue().trim());
            currentFormation.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : "");
            currentFormation.setDate_form(Date.valueOf(datePicker.getValue()));

            if (formationService.updateFormation(currentFormation)) {
                showSuccess("Formation modifiée", "La formation '" + currentFormation.getNom_form() + "' a été modifiée avec succès!");
                dialogStage.close();
            } else {
                showError("Erreur de modification", "Impossible de modifier la formation. Vérifiez la connexion à la base de données.");
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
        }

        if (errors.length() > 0) {
            showError("Erreurs de validation", errors.toString());
            return false;
        }

        return true;
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
