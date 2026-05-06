package esprit.tn.controllers;

import esprit.tn.models.formation;
import esprit.tn.services.serviceformation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class AddEvaluationController implements Initializable {
    @FXML private ComboBox<formation> formationComboBox;
    @FXML private VBox questionsContainer;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private final serviceformation formationService = new serviceformation();
    private Stage dialogStage;
    private final List<QuestionUI> questionUIs = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger les formations dans le ComboBox
        List<formation> formations = formationService.getAllFormations();
        formationComboBox.setItems(FXCollections.observableArrayList(formations));
        formationComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(formation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_form());
            }
        });
        formationComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(formation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_form());
            }
        });
        // Générer dynamiquement les questions (simulées ici)
        generateAIQuestions();
    }

    private void generateAIQuestions() {
        // Simuler des questions générées par l'IA
        List<String> questions = Arrays.asList(
            "La formation était-elle claire ?",
            "Le contenu était-il pertinent ?",
            "Le formateur a-t-il bien expliqué ?",
            "Recommanderiez-vous cette formation ?"
        );
        questionsContainer.getChildren().clear();
        questionUIs.clear();
        for (String q : questions) {
            Label label = new Label(q);
            ComboBox<Integer> noteBox = new ComboBox<>(FXCollections.observableArrayList(1,2,3,4,5));
            noteBox.setPromptText("Note (1-5)");
            questionsContainer.getChildren().add(new VBox(label, noteBox));
            questionUIs.add(new QuestionUI(label, noteBox));
        }
    }

    @FXML
    private void handleSave() {
        if (formationComboBox.getValue() == null) {
            showError("Sélection requise", "Veuillez sélectionner une formation à évaluer.");
            return;
        }
        for (QuestionUI q : questionUIs) {
            if (q.noteBox.getValue() == null) {
                showError("Note manquante", "Veuillez répondre à toutes les questions.");
                return;
            }
        }
        // Ici, vous pouvez enregistrer l'évaluation dans la base de données
        showSuccess("Évaluation enregistrée", "Merci pour votre retour !");
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe utilitaire pour lier question et ComboBox
    private static class QuestionUI {
        Label label;
        ComboBox<Integer> noteBox;
        QuestionUI(Label label, ComboBox<Integer> noteBox) {
            this.label = label;
            this.noteBox = noteBox;
        }
    }
}

