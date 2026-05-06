package esprit.tn.controllers;

import esprit.tn.models.EvaluationWithFormation;
import esprit.tn.services.serviceevaluation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ViewEvaluationsController implements Initializable {
    @FXML private TableView<EvaluationWithFormation> evaluationsTable;
    @FXML private TableColumn<EvaluationWithFormation, String> formationCol;
    @FXML private TableColumn<EvaluationWithFormation, Integer> noteCol;
    @FXML private TableColumn<EvaluationWithFormation, String> titreCol;
    @FXML private TableColumn<EvaluationWithFormation, String> commentaireCol;
    @FXML private TableColumn<EvaluationWithFormation, Void> actionsCol;
    @FXML private Button closeBtn;

    private final serviceevaluation evaluationService = new serviceevaluation();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formationCol.setCellValueFactory(new PropertyValueFactory<>("nom_formation"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        commentaireCol.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        
        setupActionsColumn();
        loadEvaluations();
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<EvaluationWithFormation, Void>() {
            private final Button deleteBtn = new Button("🗑️");
            private final Button certBtn = new Button("📜");
            private final HBox pane = new HBox(10, certBtn, deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                certBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px;");
                
                deleteBtn.setOnAction(event -> {
                    EvaluationWithFormation eval = getTableView().getItems().get(getIndex());
                    handleDelete(eval);
                });

                certBtn.setOnAction(event -> {
                    EvaluationWithFormation eval = getTableView().getItems().get(getIndex());
                    handleViewCertificate(eval);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EvaluationWithFormation eval = getTableView().getItems().get(getIndex());
                    certBtn.setVisible(eval.getNote() >= 10);
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleViewCertificate(EvaluationWithFormation eval) {
        CertificateWindow certWindow = new CertificateWindow(eval.getNom_formation(), eval.getNote());
        certWindow.show();
    }

    private void handleDelete(EvaluationWithFormation eval) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'évaluation ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette évaluation ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (evaluationService.deleteEvaluation(eval.getId())) {
                loadEvaluations();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setContentText("Erreur lors de la suppression de l'évaluation.");
                errorAlert.showAndWait();
            }
        }
    }

    private void loadEvaluations() {
        List<EvaluationWithFormation> evaluations = evaluationService.getAllEvaluationsWithFormationName();
        ObservableList<EvaluationWithFormation> data = FXCollections.observableArrayList(evaluations);
        evaluationsTable.setItems(data);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
