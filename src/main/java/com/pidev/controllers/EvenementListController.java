package com.pidev.controllers;

import com.pidev.entities.Evenement;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EvenementListController {
    @FXML private TextField searchField;
    @FXML private TableView<Evenement> table;
    @FXML private TableColumn<Evenement, Integer> colId;
    @FXML private TableColumn<Evenement, String> colNom;
    @FXML private TableColumn<Evenement, String> colType;
    @FXML private TableColumn<Evenement, Integer> colParticipants;
    @FXML private TableColumn<Evenement, LocalDate> colDate;
    @FXML private TableColumn<Evenement, LocalTime> colHeure;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, Boolean> colPaiement;
    @FXML private TableColumn<Evenement, String> colDescription;
    @FXML private TableColumn<Evenement, Void> colActions;

    private final ObservableList<Evenement> master = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        seedData();

        colId.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getId));
        colNom.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getNom));
        colType.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getTypeEvenement));
        colParticipants.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getNbrParticipant));
        colDate.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getDate));
        colHeure.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getHeure));
        colLieu.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getLieu));
        colPaiement.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getPaiement));
        colDescription.setCellValueFactory(data -> Bindings.createObjectBinding(data.getValue()::getDescription));

        colDate.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : fmt.format(item));
            }
        });
        colHeure.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            @Override protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : fmt.format(item));
            }
        });
        colPaiement.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("cell-paid", "cell-free");
                if (empty || item == null) {
                    setText("");
                    return;
                }
                if (item) {
                    setText("Payant");
                    getStyleClass().add("cell-paid");
                } else {
                    setText("Gratuit");
                    getStyleClass().add("cell-free");
                }
            }
        });

        colActions.setCellFactory(actionButtons());

        FilteredList<Evenement> filtered = new FilteredList<>(master, e -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(evt -> {
                if (q.isEmpty()) return true;
                return contains(evt.getNom(), q)
                        || contains(evt.getLieu(), q)
                        || contains(evt.getTypeEvenement(), q)
                        || contains(evt.getDescription(), q);
            });
        });

        SortedList<Evenement> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private Callback<TableColumn<Evenement, Void>, TableCell<Evenement, Void>> actionButtons() {
        return col -> new TableCell<>() {
            private final Button view = new Button("Voir");
            private final Button edit = new Button("Mod");
            private final Button delete = new Button("Suppr");
            private final HBox box = new HBox(6, view, edit, delete);

            {
                view.setOnAction(e -> openForm(getTableRow().getItem(), false));
                edit.setOnAction(e -> openForm(getTableRow().getItem(), true));
                delete.setOnAction(e -> master.remove(getTableRow().getItem()));
                view.getStyleClass().addAll("action-btn", "action-view");
                edit.getStyleClass().addAll("action-btn", "action-edit");
                delete.getStyleClass().addAll("action-btn", "action-delete");
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        };
    }

    @FXML
    private void onAdd() {
        openForm(null, true);
    }

    @FXML
    private void onSearch() {
        // Filtering is live via text listener.
    }

    @FXML
    private void onReset() {
        searchField.clear();
    }

    private void openForm(Evenement evenement, boolean editable) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/evenement/evenement-form.fxml"));
            Scene scene = new Scene(loader.load(), 900, 650);
            Stage stage = new Stage();
            stage.setTitle(editable ? "Événement" : "Détails Événement");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            EvenementFormController controller = loader.getController();
            controller.setEvenement(evenement);
            controller.setEditable(editable);
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void seedData() {
        master.addAll(
                new Evenement(1, "Gala Night", true, "Concerts", 150,
                        LocalDate.now().plusDays(10), "Palais des Arts",
                        "Soirée musicale avec artistes invités.", LocalTime.of(20, 0), null, 45f),
                new Evenement(2, "Expo Moderne", false, "Expositions d'art", 200,
                        LocalDate.now().plusDays(20), "Galerie Centrale",
                        "Exposition d'art contemporain.", LocalTime.of(10, 30), null, null),
                new Evenement(3, "Festival Street", true, "Festivals", 500,
                        LocalDate.now().plusDays(45), "Centre Ville",
                        "Festival urbain et animations.", LocalTime.of(16, 0), null, 25f)
        );
    }
}
