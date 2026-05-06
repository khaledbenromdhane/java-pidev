package esprit.tn.controllers;

import esprit.tn.models.formation;
import esprit.tn.services.serviceformation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ViewFormationsController implements Initializable {
    @FXML private TableView<formation> formationsTable;
    @FXML private TableColumn<formation, String> nomCol;
    @FXML private TableColumn<formation, String> typeCol;
    @FXML private TableColumn<formation, String> descriptionCol;
    @FXML private TableColumn<formation, java.sql.Date> dateCol;
    @FXML private TableColumn<formation, Void> actionsCol;
    @FXML private Button closeBtn;

    private final serviceformation formationService = new serviceformation();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom_form"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date_form"));
        
        setupActionsColumn();
        
        loadFormations();
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<formation, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px;");
                
                editBtn.setOnAction(event -> {
                    formation f = getTableView().getItems().get(getIndex());
                    handleEdit(f);
                });

                deleteBtn.setOnAction(event -> {
                    formation f = getTableView().getItems().get(getIndex());
                    handleDelete(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleEdit(formation f) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateFormation.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier la Formation");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(closeBtn.getScene().getWindow());
            dialogStage.setResizable(false);

            UpdateFormationController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setFormationData(f);

            dialogStage.showAndWait();

            // Refresh table after editing
            loadFormations();
            formationsTable.refresh();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir le formulaire de modification : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleDelete(formation f) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la formation ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la formation '" + f.getNom_form() + "' ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (formationService.deleteFormation(f.getId())) {
                loadFormations();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setContentText("Erreur lors de la suppression de la formation.");
                errorAlert.showAndWait();
            }
        }
    }

    private void loadFormations() {
        List<formation> formations = formationService.getAllFormations();
        ObservableList<formation> data = FXCollections.observableArrayList(formations);
        formationsTable.setItems(data);
    }


    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
