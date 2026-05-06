package esprit.tn.controllers;

import esprit.tn.models.evaluation;
import esprit.tn.models.formation;
import esprit.tn.services.serviceevaluation;
import esprit.tn.services.serviceformation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewEvaluationsController implements Initializable {
    @FXML private TableView<evaluation> evaluationsTable;
    @FXML private TableColumn<evaluation, String> formationCol;
    @FXML private TableColumn<evaluation, Integer> noteCol;
    @FXML private TableColumn<evaluation, java.sql.Date> dateCol;
    @FXML private Button closeBtn;

    private final serviceevaluation evaluationService = new serviceevaluation();
    private final serviceformation formationService = new serviceformation();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formationCol.setCellValueFactory(cellData -> {
            int formationId = cellData.getValue().getId_formation();
            formation f = formationService.getFormationById(formationId);
            return new javafx.beans.property.SimpleStringProperty(f != null ? f.getNom_form() : "?");
        });
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date_eval"));
        loadEvaluations();
    }

    private void loadEvaluations() {
        List<evaluation> evaluations = evaluationService.getAllEvaluations();
        ObservableList<evaluation> data = FXCollections.observableArrayList(evaluations);
        evaluationsTable.setItems(data);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}

