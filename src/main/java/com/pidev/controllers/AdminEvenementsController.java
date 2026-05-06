package com.pidev.controllers;

import com.pidev.entities.Evenement;
import com.pidev.services.EvenementJdbcService;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminEvenementsController implements Initializable {

    @FXML private Label totalCountLabel, upcomingCountLabel, totalAttendeesLabel, paidCountLabel;
    @FXML private Label evtCountBadge, showingCount, totalEvtCount;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter, paiementFilter;
    @FXML private TableView<Evenement> eventsTable;
    @FXML private TableColumn<Evenement, Integer> colId, colParticipants;
    @FXML private TableColumn<Evenement, String> colNom, colType, colLieu, colPaiement;
    @FXML private TableColumn<Evenement, LocalDate> colDate;
    @FXML private TableColumn<Evenement, LocalTime> colHeure;
    @FXML private TableColumn<Evenement, Evenement> colActions;
    @FXML private Button addEventBtn;
    @FXML private Button sidebarPartLink;

    private final ObservableList<Evenement> allEvents = FXCollections.observableArrayList();
    private FilteredList<Evenement> filteredEvents;
    private SortedList<Evenement> sortedEvents;
    private final EvenementJdbcService service = new EvenementJdbcService();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureColumns();
        bindTable();
        setupFilters();
        reloadData();
    }

    private void configureColumns() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colNom.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNom()));
        colType.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTypeEvenement()));
        colParticipants.setCellValueFactory(data -> new SimpleIntegerProperty(nullToZero(data.getValue().getNbrParticipant())).asObject());
        colDate.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDate()));
        colHeure.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getHeure()));
        colLieu.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getLieu()));
        colPaiement.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(Boolean.TRUE.equals(data.getValue().getPaiement()) ? "Payant" : "Gratuit"));

        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DATE_FORMATTER.format(item));
            }
        });

        colHeure.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : TIME_FORMATTER.format(item));
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
            protected void updateItem(Evenement item, boolean empty) {
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
        filteredEvents = new FilteredList<>(allEvents, e -> true);
        sortedEvents = new SortedList<>(filteredEvents);
        sortedEvents.comparatorProperty().bind(eventsTable.comparatorProperty());
        eventsTable.setItems(sortedEvents);
    }

    private void setupFilters() {
        paiementFilter.getItems().setAll("All Payment", "Payant", "Gratuit");
        paiementFilter.setValue("All Payment");

        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        paiementFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
    }

    private void reloadData() {
        allEvents.setAll(service.findAll());
        refreshTypeFilter();
        applyFilters();
        updateStats();
    }

    private void refreshTypeFilter() {
        String old = typeFilter.getValue();
        typeFilter.getItems().setAll("All Types");
        allEvents.stream()
                .map(Evenement::getTypeEvenement)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .forEach(typeFilter.getItems()::add);

        if (old == null || !typeFilter.getItems().contains(old)) {
            typeFilter.setValue("All Types");
        } else {
            typeFilter.setValue(old);
        }
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String type = typeFilter.getValue();
        String pay = paiementFilter.getValue();

        filteredEvents.setPredicate(event -> {
            boolean matchesSearch = search.isBlank()
                    || safe(event.getNom()).contains(search)
                    || safe(event.getLieu()).contains(search)
                    || safe(event.getTypeEvenement()).contains(search)
                    || safe(event.getDescription()).contains(search);

            boolean matchesType = type == null || type.equals("All Types") || type.equals(event.getTypeEvenement());

            String payText = Boolean.TRUE.equals(event.getPaiement()) ? "Payant" : "Gratuit";
            boolean matchesPay = pay == null || pay.equals("All Payment") || pay.equals(payText);

            return matchesSearch && matchesType && matchesPay;
        });

        evtCountBadge.setText(String.valueOf(filteredEvents.size()));
        showingCount.setText(String.valueOf(filteredEvents.size()));
        totalEvtCount.setText(String.valueOf(allEvents.size()));
    }

    private void updateStats() {
        totalCountLabel.setText(String.valueOf(service.countAll()));
        upcomingCountLabel.setText(String.valueOf(service.countUpcoming()));
        paidCountLabel.setText(String.valueOf(service.countPaid()));
        totalAttendeesLabel.setText(String.valueOf(service.sumParticipants()));
    }

    @FXML
    private void onAddEvent() {
        openCreateModal();
    }

    @FXML
    private void onSearch() {
        applyFilters();
    }

    @FXML
    private void onReset() {
        searchField.clear();
        typeFilter.setValue("All Types");
        paiementFilter.setValue("All Payment");
        applyFilters();
    }

    @FXML
    private void onNavigateParticipations() {
        NavigationHelper.navigateTo(sidebarPartLink, NavigationHelper.ADMIN_PARTICIPATIONS);
    }

    private void openCreateModal() {
        try {
            FXMLLoader loader = NavigationHelper.getLoader(NavigationHelper.EVT_NEW);
            Parent root = loader.load();
            EvenementFormController controller = loader.getController();
            controller.setOnSaved(this::reloadData);
            showModal(root, "Ajouter un événement");
        } catch (IOException ex) {
            showError("Erreur", "Impossible d'ouvrir le formulaire événement.");
        }
    }

    private void openEditModal(int id) {
        try {
            FXMLLoader loader = NavigationHelper.getLoader(NavigationHelper.EVT_EDIT);
            Parent root = loader.load();
            EvenementEditController controller = loader.getController();
            controller.setEventId(id);
            controller.setOnSaved(this::reloadData);
            showModal(root, "Modifier l'événement #" + id);
        } catch (IOException ex) {
            showError("Erreur", "Impossible d'ouvrir l'édition de l'événement.");
        }
    }

    private void onDelete(Evenement event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'événement \"" + event.getNom() + "\" ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = service.deleteById(event.getId());
            if (ok) {
                showInfo("Succès", "Événement supprimé avec succès !");
                reloadData();
            } else {
                showError("Erreur", "Événement introuvable.");
            }
        }
    }

    private void showModal(Parent root, String title) {
        Stage owner = (Stage) eventsTable.getScene().getWindow();
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

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
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
