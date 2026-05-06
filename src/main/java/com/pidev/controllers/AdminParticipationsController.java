package com.pidev.controllers;

import com.pidev.entities.Participation;
import com.pidev.services.ParticipationJdbcService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminParticipationsController implements Initializable {

    public static class ParticipationRow {
        private final int id;
        private final Integer userId;
        private final int eventId;
        private final String event;
        private final LocalDate date;
        private final String status;
        private final int nbr;
        private final String paiement;

        public ParticipationRow(int id, Integer userId, int eventId, String event, LocalDate date, String status, int nbr, String paiement) {
            this.id = id;
            this.userId = userId;
            this.eventId = eventId;
            this.event = event;
            this.date = date;
            this.status = status;
            this.nbr = nbr;
            this.paiement = paiement;
        }

        public int getId() { return id; }
        public String getUser() { return userId == null ? "N/A" : "User #" + userId; }
        public String getEvent() { return event; }
        public LocalDate getDate() { return date; }
        public String getStatus() { return status; }
        public int getNbr() { return nbr; }
        public String getPaiement() { return paiement; }
        public int getEventId() { return eventId; }
    }

    @FXML private Label totalPartLabel, acceptedLabel, pendingLabel, refusedLabel;
    @FXML private Label partCountBadge, showingRange, totalPartCount;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter, paiementModeFilter;
    @FXML private TableView<ParticipationRow> participationsTable;
    @FXML private TableColumn<ParticipationRow, Integer> colId, colNbr;
    @FXML private TableColumn<ParticipationRow, String> colUser, colEvent, colStatus, colPaiement;
    @FXML private TableColumn<ParticipationRow, LocalDate> colDate;
    @FXML private TableColumn<ParticipationRow, ParticipationRow> colActions;
    @FXML private Button addPartBtn;
    @FXML private Button sidebarEvtLink;

    private final ObservableList<ParticipationRow> allRows = FXCollections.observableArrayList();
    private FilteredList<ParticipationRow> filteredRows;
    private SortedList<ParticipationRow> sortedRows;
    private final ParticipationJdbcService service = new ParticipationJdbcService();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureColumns();
        bindTable();
        setupFilters();
        reloadData();
    }

    private void configureColumns() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colUser.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUser()));
        colEvent.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEvent()));
        colDate.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDate()));
        colStatus.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus()));
        colNbr.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNbr()).asObject());
        colPaiement.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPaiement()));

        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DATE_FORMATTER.format(item));
            }
        });

        colActions.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✎");
            private final Button delBtn = new Button("🗑");
            private final HBox box = new HBox(6, editBtn, delBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("btn-ghost");
                delBtn.getStyleClass().add("btn-ghost");
            }

            @Override
            protected void updateItem(ParticipationRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                editBtn.setOnAction(e -> openEditModal(item.getId()));
                delBtn.setOnAction(e -> onDelete(item));
                setGraphic(box);
            }
        });
    }

    private void bindTable() {
        filteredRows = new FilteredList<>(allRows, row -> true);
        sortedRows = new SortedList<>(filteredRows);
        sortedRows.comparatorProperty().bind(participationsTable.comparatorProperty());
        participationsTable.setItems(sortedRows);
    }

    private void setupFilters() {
        statusFilter.getItems().setAll("All Status", "En attente", "Confirmée", "Annulée");
        statusFilter.setValue("All Status");

        paiementModeFilter.getItems().setAll("All Payment", "Carte", "Cash", "Gratuit");
        paiementModeFilter.setValue("All Payment");

        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        paiementModeFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
    }

    private void reloadData() {
        allRows.clear();
        service.findAll().forEach(p -> {
            String mode = p.getModePaiement() == null || p.getModePaiement().isBlank() ? "Gratuit" : p.getModePaiement();
            allRows.add(new ParticipationRow(
                    p.getId(),
                    p.getUserId(),
                    p.getEvenement().getId(),
                    p.getEvenement().getNom(),
                    p.getDateParticipation(),
                    p.getStatut(),
                    p.getNbrParticipation(),
                    mode
            ));
        });
        applyFilters();
        updateStats();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String status = statusFilter.getValue();
        String pay = paiementModeFilter.getValue();

        filteredRows.setPredicate(row -> {
            boolean matchesSearch = search.isBlank()
                    || safe(row.getUser()).contains(search)
                    || safe(row.getEvent()).contains(search)
                    || safe(row.getStatus()).contains(search)
                    || safe(row.getPaiement()).contains(search);

            boolean matchesStatus = status == null || status.equals("All Status") || status.equals(row.getStatus());
            boolean matchesPay = pay == null || pay.equals("All Payment") || pay.equals(row.getPaiement());

            return matchesSearch && matchesStatus && matchesPay;
        });

        partCountBadge.setText(String.valueOf(filteredRows.size()));
        totalPartCount.setText(String.valueOf(allRows.size()));
        showingRange.setText(filteredRows.isEmpty() ? "0" : "1–" + filteredRows.size());
    }

    private void updateStats() {
        totalPartLabel.setText(String.valueOf(service.countAll()));
        acceptedLabel.setText(String.valueOf(service.countConfirmed()));
        pendingLabel.setText(String.valueOf(service.countPending()));
        refusedLabel.setText(String.valueOf(service.countCancelled()));
    }

    @FXML
    private void onAddParticipation() {
        openCreateModal();
    }

    @FXML
    private void onNavigateEvenements() {
        NavigationHelper.navigateTo(sidebarEvtLink, NavigationHelper.ADMIN_EVENEMENTS);
    }

    private void openCreateModal() {
        try {
            FXMLLoader loader = NavigationHelper.getLoader(NavigationHelper.PART_NEW);
            Parent root = loader.load();
            ParticipationFormController controller = loader.getController();
            controller.setOnSaved(this::reloadData);
            showModal(root, "Ajouter une participation");
        } catch (IOException ex) {
            showError("Erreur", "Impossible d'ouvrir le formulaire participation.");
        }
    }

    private void openEditModal(int id) {
        try {
            FXMLLoader loader = NavigationHelper.getLoader(NavigationHelper.PART_EDIT);
            Parent root = loader.load();
            ParticipationEditController controller = loader.getController();
            controller.setParticipationId(id);
            controller.setOnSaved(this::reloadData);
            showModal(root, "Modifier la participation #" + id);
        } catch (IOException ex) {
            showError("Erreur", "Impossible d'ouvrir l'édition de la participation.");
        }
    }

    private void onDelete(ParticipationRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer la participation #" + row.getId() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = service.deleteById(row.getId());
            if (ok) {
                showInfo("Succès", "Participation supprimée avec succès !");
                reloadData();
            } else {
                showError("Erreur", "Participation introuvable.");
            }
        }
    }

    private void showModal(Parent root, String title) {
        Stage owner = (Stage) participationsTable.getScene().getWindow();
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private void showInfo(String title, String message) {
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
}
