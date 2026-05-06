package esprit.tn.controllers;

import esprit.tn.models.formation;
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

public class ViewFormationsController implements Initializable {
    @FXML private TableView<formation> formationsTable;
    @FXML private TableColumn<formation, String> nomCol;
    @FXML private TableColumn<formation, String> typeCol;
    @FXML private TableColumn<formation, String> descriptionCol;
    @FXML private TableColumn<formation, java.sql.Date> dateCol;
    @FXML private Button closeBtn;

    private final serviceformation formationService = new serviceformation();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom_form"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date_form"));
        loadFormations();
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

