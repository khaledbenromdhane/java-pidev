package com.pidev.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.pidev.entities.Evenement;
import com.pidev.tools.myconnexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class EvenementListController implements Initializable {

    private enum SortField { DATE, NOM, TYPE, PLACES, LIEU }

    private record ReservationDraft(int places, String paymentMethod) {}

    private static final class ReservationState {
        private int participationId;
        private int places;
        private String paymentMethod;

        private ReservationState(int participationId, int places, String paymentMethod) {
            this.participationId = participationId;
            this.places = places;
            this.paymentMethod = paymentMethod;
        }
    }

    @FXML private TextField searchField;
    @FXML private Button resetBtn;
    @FXML private Label searchInfoLabel;
    @FXML private BorderPane rootPane;
    @FXML private HBox headerTop;
    @FXML private HBox headerNav;
    @FXML private HBox navButtonsBox;
    @FXML private HBox accountActions;
    @FXML private Label topQuestionLabel;
    @FXML private Label topEmailLabel;
    @FXML private Label topHoursLabel;
    @FXML private Label logoLabel;
    @FXML private MenuButton accountMenuButton;
    @FXML private Button voirAdminBtn;
    @FXML private Button ticketsBtn;
    @FXML private Button navAccueilBtn;
    @FXML private Button navOeuvresBtn;
    @FXML private Button navEvenementsBtn;
    @FXML private Button navBlogBtn;
    @FXML private Button navFormationBtn;
    @FXML private ToggleButton sortDateBtn;
    @FXML private ToggleButton sortNomBtn;
    @FXML private ToggleButton sortTypeBtn;
    @FXML private ToggleButton sortPlaceBtn;
    @FXML private ToggleButton sortLieuBtn;
    @FXML private ToggleButton orderToggle;
    @FXML private FlowPane evtGrid;
    @FXML private HBox paymentBanner;
    @FXML private Label paymentBannerTitle;
    @FXML private Label paymentBannerMessage;

    private final List<Evenement> allEvents = new ArrayList<>();
    private final Map<Integer, ReservationState> reservationsByEvent = new HashMap<>();
    private List<Evenement> renderedEvents = new ArrayList<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private SortField sortField = SortField.DATE;
    private boolean descending = true;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH'h'mm");
    private static final String BACKEND_BASE_URL = "http://localhost:8081";
    private static final String BACKEND_CHECKOUT_URL = BACKEND_BASE_URL + "/api/stripe/checkout";
    private static final String BACKEND_VERIFY_URL = BACKEND_BASE_URL + "/api/stripe/verify";
    private static final String BACKEND_CANCEL_URL = BACKEND_BASE_URL + "/api/stripe/cancel";
    private static final String STRIPE_SUCCESS_URL = BACKEND_BASE_URL + "/stripe/return/success";
    private static final String STRIPE_CANCEL_URL = BACKEND_BASE_URL + "/stripe/return/cancel";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadEventsFromDatabase();
        loadReservationsFromDatabase();
        hidePaymentBanner();

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            resetBtn.setVisible(newV != null && !newV.isBlank());
            resetBtn.setManaged(newV != null && !newV.isBlank());
        });

        resetBtn.setVisible(false);
        resetBtn.setManaged(false);

        evtGrid.widthProperty().addListener((obs, oldV, newV) -> applyResponsiveCardWidth());
        rootPane.widthProperty().addListener((obs, oldV, newV) -> applyResponsiveLayout(newV.doubleValue()));

        selectSortButton(sortDateBtn);
        updateOrderToggleText();
        refreshGrid();
        applyResponsiveLayout(rootPane.getWidth());
    }

    private void applyResponsiveLayout(double width) {
        headerTop.getStyleClass().removeAll("compact-top");
        headerNav.getStyleClass().removeAll("compact-nav", "mobile-nav");

        if (width <= 0) {
            return;
        }

        boolean compact = width < 1280;
        boolean mobile = width < 980;

        if (compact) {
            if (!headerTop.getStyleClass().contains("compact-top")) {
                headerTop.getStyleClass().add("compact-top");
            }
            if (!headerNav.getStyleClass().contains("compact-nav")) {
                headerNav.getStyleClass().add("compact-nav");
            }
        }

        topQuestionLabel.setVisible(!mobile);
        topQuestionLabel.setManaged(!mobile);

        ticketsBtn.setVisible(!mobile);
        ticketsBtn.setManaged(!mobile);

        if (mobile) {
            if (!headerNav.getStyleClass().contains("mobile-nav")) {
                headerNav.getStyleClass().add("mobile-nav");
            }
            topEmailLabel.setText("✉ info@artvista.com");
            topHoursLabel.setText("🕒 8:00 - 18:00");
            navEvenementsBtn.setText("📅 ÉVÉNEMENTS");
            navFormationBtn.setText("FORMATION");
            navButtonsBox.setSpacing(8);
            accountActions.setSpacing(6);
        } else {
            topEmailLabel.setText("✉ info@artvista.com");
            topHoursLabel.setText("🕒 Lun - Ven: 8:00 - 18:00");
            navEvenementsBtn.setText("📅 ÉVÉNEMENTS");
            navFormationBtn.setText("FORMATION");
            navButtonsBox.setSpacing(16);
            accountActions.setSpacing(10);
        }

        if (width < 760) {
            navOeuvresBtn.setVisible(false);
            navOeuvresBtn.setManaged(false);
            navBlogBtn.setVisible(false);
            navBlogBtn.setManaged(false);
            topHoursLabel.setVisible(false);
            topHoursLabel.setManaged(false);
        } else {
            navOeuvresBtn.setVisible(true);
            navOeuvresBtn.setManaged(true);
            navBlogBtn.setVisible(true);
            navBlogBtn.setManaged(true);
            topHoursLabel.setVisible(true);
            topHoursLabel.setManaged(true);
        }

        if (width < 620) {
            accountMenuButton.setText("👤 Compte");
            voirAdminBtn.setText("Admin");
            topEmailLabel.setVisible(false);
            topEmailLabel.setManaged(false);
        } else {
            accountMenuButton.setText("👤 ben romdhane");
            voirAdminBtn.setText("Voir admin");
            topEmailLabel.setVisible(true);
            topEmailLabel.setManaged(true);
        }
    }

    private void loadEventsFromDatabase() {
        allEvents.clear();

        try {
            Connection connection = myconnexion.getInstance().getConnection();
            if (connection == null) {
                return;
            }

            String tableName = resolveEvenementTable(connection);
            if (tableName == null) {
                return;
            }

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName)) {

                ResultSetMetaData metaData = rs.getMetaData();
                Set<String> columns = new HashSet<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.add(metaData.getColumnName(i).toLowerCase(Locale.ROOT));
                }

                while (rs.next()) {
                    Evenement event = new Evenement();
                    event.setId(intValue(rs, columns, "id_evenement", "id"));
                    event.setNom(stringValue(rs, columns, "nom"));
                    event.setPaiement(booleanValue(rs, columns, "paiement"));
                    event.setTypeEvenement(stringValue(rs, columns, "typeevenement", "type_evenement"));
                    event.setNbrParticipant(intValue(rs, columns, "nbrparticipant", "nbr_participant"));
                    event.setDate(dateValue(rs, columns, "date"));
                    event.setLieu(stringValue(rs, columns, "lieu"));
                    event.setDescription(stringValue(rs, columns, "description"));
                    event.setHeure(timeValue(rs, columns, "heure"));
                    event.setImage(stringValue(rs, columns, "image"));
                    event.setPrix(floatValue(rs, columns, "prix"));
                    allEvents.add(event);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void loadReservationsFromDatabase() {
        reservationsByEvent.clear();

        try {
            Connection connection = myconnexion.getInstance().getConnection();
            if (connection == null) {
                return;
            }

            String tableName = resolveParticipationTable(connection);
            if (tableName == null) {
                return;
            }

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName + " ORDER BY id_participation DESC")) {

                ResultSetMetaData metaData = rs.getMetaData();
                Set<String> columns = new HashSet<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.add(metaData.getColumnName(i).toLowerCase(Locale.ROOT));
                }

                while (rs.next()) {
                    int eventId = intValue(rs, columns, "id_evenement", "event_id", "evenement_id");
                    if (eventId <= 0 || reservationsByEvent.containsKey(eventId)) {
                        continue;
                    }

                    String statut = stringValue(rs, columns, "statut", "status");
                    if (statut != null && statut.toLowerCase(Locale.ROOT).contains("annul")) {
                        continue;
                    }

                    int participationId = intValue(rs, columns, "id_participation", "id");
                    int places = intValue(rs, columns, "nbr_participation", "nbrparticipation", "places");
                    String payment = stringValue(rs, columns, "mode_paiement", "modepaiement", "payment_mode");

                    reservationsByEvent.put(eventId, new ReservationState(participationId, Math.max(1, places), payment));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void refreshGrid() {
        renderedEvents = filterAndSort();

        evtGrid.getChildren().clear();
        if (renderedEvents.isEmpty()) {
            Label emptyState = new Label("Aucun événement trouvé");
            emptyState.getStyleClass().add("empty-state");
            evtGrid.getChildren().add(emptyState);
            updateSearchInfo();
            return;
        }

        for (Evenement event : renderedEvents) {
            evtGrid.getChildren().add(buildCard(event));
        }

        updateSearchInfo();
        applyResponsiveCardWidth();
    }

    private List<Evenement> filterAndSort() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Evenement> filtered = allEvents.stream()
                .filter(evt -> query.isBlank() || contains(evt, query))
                .toList();

        Comparator<Evenement> comparator = switch (sortField) {
            case DATE -> Comparator.comparing(Evenement::getDate);
            case NOM -> Comparator.comparing(Evenement::getNom, String.CASE_INSENSITIVE_ORDER);
            case TYPE -> Comparator.comparing(Evenement::getTypeEvenement, String.CASE_INSENSITIVE_ORDER);
            case PLACES -> Comparator.comparing(Evenement::getNbrParticipant);
            case LIEU -> Comparator.comparing(Evenement::getLieu, String.CASE_INSENSITIVE_ORDER);
        };

        if (descending) {
            comparator = comparator.reversed();
        }

        return filtered.stream().sorted(comparator).toList();
    }

    private boolean contains(Evenement evt, String query) {
        return safe(evt.getNom()).contains(query)
                || safe(evt.getTypeEvenement()).contains(query)
                || safe(evt.getLieu()).contains(query)
                || safe(evt.getDescription()).contains(query);
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private VBox buildCard(Evenement event) {
        VBox card = new VBox();
        card.getStyleClass().add("evt-card");
        card.setPrefWidth(390);
        card.setFillWidth(true);

        StackPane thumb = new StackPane();
        thumb.getStyleClass().add("evt-thumb");
        thumb.setPrefHeight(250);
        thumb.setMinHeight(250);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(false);
        imageView.fitWidthProperty().bind(card.widthProperty());
        Image image = new Image(resolveImage(event.getImage()), true);
        imageView.setImage(image);

        Label typeOverlay = new Label((event.getTypeEvenement() == null ? "ÉVÉNEMENT" : event.getTypeEvenement()).toUpperCase(Locale.ROOT));
        typeOverlay.getStyleClass().add("overlay-type");
        StackPane.setAlignment(typeOverlay, Pos.BOTTOM_LEFT);
        StackPane.setMargin(typeOverlay, new Insets(0, 0, 12, 14));

        float prix = event.getPrix() == null ? 0f : event.getPrix();
        Label badge = new Label(Boolean.TRUE.equals(event.getPaiement()) ? String.format(Locale.US, "☕ %.2f €", prix) : "👜 Gratuit");
        badge.getStyleClass().addAll("badge-pay", Boolean.TRUE.equals(event.getPaiement()) ? "paid" : "free");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(12, 12, 0, 0));

        thumb.getChildren().addAll(imageView, typeOverlay, badge);

        VBox body = new VBox(8);
        body.getStyleClass().add("evt-body");

        Label dateRow = new Label("🗓 " + DATE_FORMATTER.format(event.getDate()) + "   🕓 " + TIME_FORMATTER.format(event.getHeure()));
        dateRow.getStyleClass().add("evt-date-row");

        Label title = new Label(event.getNom());
        title.getStyleClass().add("evt-title");
        title.setWrapText(true);

        Label meta = new Label("📍 " + event.getLieu() + "    👥 " + event.getNbrParticipant() + " places");
        meta.getStyleClass().add("evt-meta");
        meta.setWrapText(true);

        Label desc = new Label(truncate(event.getDescription(), 120));
        desc.getStyleClass().add("evt-desc");
        desc.setWrapText(true);

        HBox actions = new HBox(7);
        actions.getStyleClass().add("evt-actions");

        Button participateBtn = actionButton("Participer", true);
        Button qrBtn = actionButton("QR", false);
        Button editBtn = actionButton("Modifier", false);

        configureCardActions(event, participateBtn, qrBtn, editBtn);

        actions.getChildren().addAll(participateBtn, qrBtn, editBtn);

        VBox.setVgrow(desc, Priority.ALWAYS);
        body.getChildren().addAll(dateRow, title, meta, desc, actions);

        card.getChildren().addAll(thumb, body);
        return card;
    }

    private void configureCardActions(Evenement event, Button participateBtn, Button qrBtn, Button editBtn) {
        ReservationState existing = reservationsByEvent.get(event.getId());
        boolean reserved = existing != null;

        qrBtn.setVisible(reserved);
        qrBtn.setManaged(reserved);
        editBtn.setVisible(reserved);
        editBtn.setManaged(reserved);

        participateBtn.setOnAction(e -> {
            Optional<ReservationDraft> draft = showReservationDialog(event, null);
            if (draft.isEmpty()) {
                return;
            }

            try {
                ReservationDraft value = draft.get();
                int participationId = insertReservation(event, value.places(), value.paymentMethod());
                if (Boolean.TRUE.equals(event.getPaiement()) && "Carte".equalsIgnoreCase(value.paymentMethod())) {
                    launchStripeCheckout(event, participationId, value.places(),
                            () -> {
                                reservationsByEvent.put(event.getId(), new ReservationState(participationId, value.places(), value.paymentMethod()));
                                qrBtn.setVisible(true);
                                qrBtn.setManaged(true);
                                editBtn.setVisible(true);
                                editBtn.setManaged(true);
                                showPaymentBanner("success", "Paiement reussi", "Votre reservation est confirmee et visible dans vos participations.");
                                refreshGrid();
                            },
                            () -> {
                                reservationsByEvent.remove(event.getId());
                                qrBtn.setVisible(false);
                                qrBtn.setManaged(false);
                                editBtn.setVisible(false);
                                editBtn.setManaged(false);
                                showPaymentBanner("error", "Paiement echoue", "Le paiement a ete annule ou a echoue. Veuillez reessayer.");
                                refreshGrid();
                            });
                } else {
                    reservationsByEvent.put(event.getId(), new ReservationState(participationId, value.places(), value.paymentMethod()));
                    qrBtn.setVisible(true);
                    qrBtn.setManaged(true);
                    editBtn.setVisible(true);
                    editBtn.setManaged(true);
                    showInfo("Succès", "Réservation enregistrée avec succès.");
                }
            } catch (Exception ex) {
                showError("Erreur", "Impossible d'enregistrer la réservation.");
            }
        });

        qrBtn.setOnAction(e -> {
            ReservationState state = reservationsByEvent.get(event.getId());
            if (state == null) {
                return;
            }
            showQrDialog(event, state);
        });

        editBtn.setOnAction(e -> {
            ReservationState state = reservationsByEvent.get(event.getId());
            if (state == null) {
                return;
            }

            Optional<ReservationDraft> draft = showReservationDialog(event, state);
            if (draft.isEmpty()) {
                return;
            }

            try {
                ReservationDraft value = draft.get();
                updateReservation(state.participationId, event, value.places(), value.paymentMethod());
                state.places = value.places();
                state.paymentMethod = value.paymentMethod();
                showInfo("Succès", "Réservation modifiée avec succès.");
            } catch (Exception ex) {
                showError("Erreur", "Impossible de modifier la réservation.");
            }
        });
    }

    private Optional<ReservationDraft> showReservationDialog(Evenement event, ReservationState existingState) {
        boolean paidEvent = Boolean.TRUE.equals(event.getPaiement());

        int alreadyReserved = existingState == null ? 0 : existingState.places;
        int reservedByOthers = sumReservedPlaces(event.getId(), existingState == null ? null : existingState.participationId);
        int available = Math.max(0, event.getNbrParticipant() - reservedByOthers);
        int maxSelectable = available;

        Dialog<ReservationDraft> dialog = new Dialog<>();
        dialog.setTitle(existingState == null ? "Réserver" : "Modifier la réservation");
        dialog.setHeaderText(event.getNom());

        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType confirmType = new ButtonType(existingState == null ? "Confirmer" : "Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelType, confirmType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 5, 5, 5));

        Label capacityLabel = new Label("Places disponibles : " + available + " / " + event.getNbrParticipant());
        capacityLabel.setStyle("-fx-text-fill: #334155; -fx-font-weight: 700;");

        Label placesLabel = new Label("Nombre de participations");
        int initial = existingState == null ? (maxSelectable > 0 ? 1 : 0) : Math.min(existingState.places, Math.max(1, maxSelectable));
        Spinner<Integer> placesSpinner = new Spinner<>();
        if (maxSelectable > 0) {
            placesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Math.max(1, maxSelectable), Math.max(1, initial)));
            placesSpinner.setEditable(true);
        } else {
            placesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            placesSpinner.setDisable(true);
        }

        Label paymentLabel = new Label("Mode de paiement");
        ComboBox<String> paymentBox = new ComboBox<>();
        paymentBox.getItems().setAll("Cash", "Card");
        if (existingState != null && existingState.paymentMethod != null) {
            String normalized = existingState.paymentMethod.equalsIgnoreCase("Carte") ? "Card" : existingState.paymentMethod;
            paymentBox.setValue(normalized);
        }

        paymentLabel.setVisible(paidEvent);
        paymentLabel.setManaged(paidEvent);
        paymentBox.setVisible(paidEvent);
        paymentBox.setManaged(paidEvent);

        Label validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);

        grid.add(capacityLabel, 0, 0, 2, 1);
        grid.add(placesLabel, 0, 1);
        grid.add(placesSpinner, 1, 1);
        grid.add(paymentLabel, 0, 2);
        grid.add(paymentBox, 1, 2);
        grid.add(validationLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmType);

        Runnable revalidate = () -> {
            int selected = placesSpinner.getValue();
            boolean invalidPlaces = selected <= 0 || selected > maxSelectable;
            boolean invalidPayment = paidEvent && (paymentBox.getValue() == null || paymentBox.getValue().isBlank());
            boolean invalidPrice = paidEvent && (event.getPrix() == null || event.getPrix() <= 0);

            if (invalidPrice) {
                validationLabel.setText("Prix invalide pour cet evenement.");
                validationLabel.setVisible(true);
                validationLabel.setManaged(true);
            } else if (invalidPlaces) {
                validationLabel.setText("Le nombre de participations ne peut pas dépasser les places disponibles.");
                validationLabel.setVisible(true);
                validationLabel.setManaged(true);
            } else if (invalidPayment) {
                validationLabel.setText("Veuillez sélectionner un mode de paiement.");
                validationLabel.setVisible(true);
                validationLabel.setManaged(true);
            } else {
                validationLabel.setVisible(false);
                validationLabel.setManaged(false);
            }

            confirmButton.setDisable(invalidPrice || invalidPlaces || invalidPayment || maxSelectable <= 0);
            if (paidEvent && "Card".equalsIgnoreCase(paymentBox.getValue())) {
                confirmButton.setText(existingState == null ? "Payer par Stripe" : "Mettre a jour + Stripe");
            } else {
                confirmButton.setText(existingState == null ? "Confirmer" : "Enregistrer");
            }
        };

        placesSpinner.valueProperty().addListener((obs, oldV, newV) -> revalidate.run());
        paymentBox.valueProperty().addListener((obs, oldV, newV) -> revalidate.run());

        revalidate.run();

        dialog.setResultConverter(buttonType -> {
            if (buttonType != confirmType) {
                return null;
            }

            int selected = placesSpinner.getValue();
            String paymentMode = null;
            if (paidEvent) {
                paymentMode = "Card".equalsIgnoreCase(paymentBox.getValue()) ? "Carte" : paymentBox.getValue();
            }
            return new ReservationDraft(selected, paymentMode);
        });

        return dialog.showAndWait();
    }

    private void launchStripeCheckout(Evenement event, int participationId, int places, Runnable onSuccess, Runnable onCancel) {
        try {
            String checkoutUrl = createCheckoutSessionUrl(event, participationId, places);
            openCheckoutWindow(checkoutUrl, onSuccess, onCancel);
        } catch (Exception ex) {
            System.err.println("Stripe checkout error: " + ex.getMessage());
            String details = ex.getMessage() == null ? "" : ex.getMessage();
            showPaymentBanner("error", "Paiement indisponible", "Checkout failed: " + details);
            if (onCancel != null) {
                onCancel.run();
            }
        }
    }

    private String createCheckoutSessionUrl(Evenement event, int participationId, int places) throws IOException, InterruptedException {
        float prix = event.getPrix() == null ? 0f : event.getPrix();
        if (prix <= 0) {
            throw new IllegalArgumentException("Montant invalide");
        }

        JSONObject payload = new JSONObject();
        payload.put("participationId", participationId);
        payload.put("eventId", event.getId());
        payload.put("eventName", event.getNom());
        payload.put("unitPrice", prix);
        payload.put("places", places);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BACKEND_CHECKOUT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Checkout HTTP " + response.statusCode() + ": " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        return json.getString("url");
    }

    private void openCheckoutWindow(String checkoutUrl, Runnable onSuccess, Runnable onCancel) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Paiement Stripe");

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        AtomicBoolean handled = new AtomicBoolean(false);

        engine.locationProperty().addListener((obs, oldV, newV) -> {
            if (newV == null || handled.get()) {
                return;
            }

            if (newV.startsWith(STRIPE_SUCCESS_URL)) {
                handled.set(true);
                boolean paid = verifyStripePayment(newV);
                stage.close();
                if (paid) {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    if (onCancel != null) {
                        onCancel.run();
                    }
                }
            } else if (newV.startsWith(STRIPE_CANCEL_URL)) {
                handled.set(true);
                stage.close();
                notifyBackendCancel(newV);
                if (onCancel != null) {
                    onCancel.run();
                }
            }
        });

        stage.setOnCloseRequest(event -> {
            if (handled.compareAndSet(false, true)) {
                if (onCancel != null) {
                    onCancel.run();
                }
            }
        });

        engine.load(checkoutUrl);
        stage.setScene(new Scene(webView, 980, 720));
        stage.show();
    }

    private boolean verifyStripePayment(String url) {
        String sessionId = extractQueryParam(url, "session_id");
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(BACKEND_VERIFY_URL + "?sessionId=" + sessionId);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return false;
            }
            JSONObject json = new JSONObject(response.body());
            return json.optBoolean("paid", false);
        } catch (Exception ex) {
            return false;
        }
    }

    private void notifyBackendCancel(String url) {
        String pid = extractQueryParam(url, "pid");
        if (pid == null || pid.isBlank()) {
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("participationId", Integer.parseInt(pid));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_CANCEL_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }
    }

    private String extractQueryParam(String url, String key) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query == null || query.isBlank()) {
                return null;
            }
            for (String part : query.split("&")) {
                String[] tokens = part.split("=", 2);
                if (tokens.length == 2 && key.equals(tokens[0])) {
                    return URLDecoder.decode(tokens[1], StandardCharsets.UTF_8);
                }
            }
        } catch (URISyntaxException ignored) {
        }
        return null;
    }

    private void showQrDialog(Evenement event, ReservationState state) {
        StringBuilder payload = new StringBuilder();
        payload.append("Participation: ").append(state.participationId).append('\n');
        payload.append("Event: ").append(event.getNom()).append('\n');
        payload.append("Date: ").append(event.getDate()).append(' ').append(event.getHeure()).append('\n');
        payload.append("Places: ").append(state.places).append('\n');
        if (Boolean.TRUE.equals(event.getPaiement()) && state.paymentMethod != null) {
            payload.append("Payment: ").append(state.paymentMethod);
        }

        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(280);
        imageView.setPreserveRatio(true);

        try {
            imageView.setImage(generateQrImage(payload.toString(), 280, 280));
        } catch (WriterException ex) {
            showError("QR", "Impossible de générer le QR code.");
            return;
        }

        Label details = new Label(payload.toString());
        details.setWrapText(true);

        VBox box = new VBox(10, imageView, details);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("QR Code");
        alert.setHeaderText("QR de réservation");
        alert.getDialogPane().setContent(box);
        alert.showAndWait();
    }

    private Image generateQrImage(String content, int width, int height) throws WriterException {
        EnumMap<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setArgb(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return image;
    }

    private int insertReservation(Evenement event, int places, String paymentMethod) throws SQLException {
        Connection connection = myconnexion.getInstance().getConnection();
        String table = resolveParticipationTable(connection);
        if (table == null) {
            throw new SQLException("Table participation introuvable.");
        }

        String sql = "INSERT INTO " + table + " (id_user, id_evenement, date_participation, statut, nbr_participation, mode_paiement, scanned, scanned_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNull(1, Types.INTEGER);
            ps.setInt(2, event.getId());
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, "En attente");
            ps.setInt(5, places);
            if (Boolean.TRUE.equals(event.getPaiement())) {
                ps.setString(6, paymentMethod);
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            ps.setBoolean(7, false);
            ps.setNull(8, Types.TIMESTAMP);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        int fallbackId = findLastInsertedParticipationId(connection, table, event.getId());
        if (fallbackId <= 0) {
            throw new SQLException("Insertion participation échouée.");
        }
        return fallbackId;
    }

    private void updateReservation(int participationId, Evenement event, int places, String paymentMethod) throws SQLException {
        Connection connection = myconnexion.getInstance().getConnection();
        String table = resolveParticipationTable(connection);
        if (table == null) {
            throw new SQLException("Table participation introuvable.");
        }

        String sql = "UPDATE " + table + " SET nbr_participation=?, mode_paiement=? WHERE id_participation=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, places);
            if (Boolean.TRUE.equals(event.getPaiement())) {
                ps.setString(2, paymentMethod);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setInt(3, participationId);
            ps.executeUpdate();
        }
    }

    private void updateReservationStatus(int participationId, String status) {
        try {
            Connection connection = myconnexion.getInstance().getConnection();
            String table = resolveParticipationTable(connection);
            if (table == null) {
                return;
            }

            String sql = "UPDATE " + table + " SET statut=? WHERE id_participation=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, participationId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }

    private int sumReservedPlaces(int eventId, Integer excludeParticipationId) {
        try {
            Connection connection = myconnexion.getInstance().getConnection();
            String table = resolveParticipationTable(connection);
            if (table == null) {
                return 0;
            }

            String sql = "SELECT COALESCE(SUM(nbr_participation),0) FROM " + table + " WHERE id_evenement=? AND statut <> 'Annulée'";
            if (excludeParticipationId != null) {
                sql += " AND id_participation<>?";
            }

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, eventId);
                if (excludeParticipationId != null) {
                    ps.setInt(2, excludeParticipationId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return 0;
    }

    private int findLastInsertedParticipationId(Connection connection, String table, int eventId) throws SQLException {
        String sql = "SELECT id_participation FROM " + table + " WHERE id_evenement=? ORDER BY id_participation DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private String resolveParticipationTable(Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        if (tableExists(meta, "participation")) {
            return "participation";
        }
        if (tableExists(meta, "participations")) {
            return "participations";
        }
        return null;
    }

    private Button actionButton(String text, boolean primary) {
        Button btn = new Button(text);
        btn.getStyleClass().add("act-btn");
        if (primary) {
            btn.getStyleClass().add("primary");
        }
        return btn;
    }

    private String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "…";
    }

    private void updateSearchInfo() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        boolean visible = !query.isBlank();
        searchInfoLabel.setVisible(visible);
        searchInfoLabel.setManaged(visible);

        if (visible) {
            searchInfoLabel.setText("Résultats pour \"" + query + "\" — " + renderedEvents.size() + " trouvé(s)");
        }
    }

    private void applyResponsiveCardWidth() {
        double availableWidth = evtGrid.getWidth();
        if (availableWidth <= 0 || evtGrid.getChildren().isEmpty()) {
            return;
        }

        int columns;
        if (availableWidth < 600) {
            columns = 1;
        } else if (availableWidth < 1020) {
            columns = 2;
        } else {
            columns = 3;
        }

        double cardWidth = (availableWidth - ((columns - 1) * evtGrid.getHgap())) / columns;
        cardWidth = Math.max(310, cardWidth);
        final double finalCardWidth = cardWidth;

        evtGrid.getChildren().forEach(node -> {
            if (node instanceof VBox card) {
                card.setPrefWidth(finalCardWidth);
                card.setMinWidth(finalCardWidth);
                card.setMaxWidth(finalCardWidth);
            }
        });
    }

    private void selectSortButton(ToggleButton selected) {
        sortDateBtn.setSelected(selected == sortDateBtn);
        sortNomBtn.setSelected(selected == sortNomBtn);
        sortTypeBtn.setSelected(selected == sortTypeBtn);
        sortPlaceBtn.setSelected(selected == sortPlaceBtn);
        sortLieuBtn.setSelected(selected == sortLieuBtn);
    }

    private void updateOrderToggleText() {
        orderToggle.setText(descending ? "DESC" : "ASC");
        orderToggle.setSelected(descending);
    }

    @FXML
    private void onSearch() {
        refreshGrid();
    }

    @FXML
    private void onReset() {
        searchField.clear();
        refreshGrid();
    }

    @FXML
    private void onSortDate() {
        sortField = SortField.DATE;
        selectSortButton(sortDateBtn);
        refreshGrid();
    }

    @FXML
    private void onSortNom() {
        sortField = SortField.NOM;
        selectSortButton(sortNomBtn);
        refreshGrid();
    }

    @FXML
    private void onSortType() {
        sortField = SortField.TYPE;
        selectSortButton(sortTypeBtn);
        refreshGrid();
    }

    @FXML
    private void onSortPlaces() {
        sortField = SortField.PLACES;
        selectSortButton(sortPlaceBtn);
        refreshGrid();
    }

    @FXML
    private void onSortLieu() {
        sortField = SortField.LIEU;
        selectSortButton(sortLieuBtn);
        refreshGrid();
    }

    @FXML
    private void onToggleOrder() {
        descending = !descending;
        updateOrderToggleText();
        refreshGrid();
    }

    @FXML
    private void onNavAccueil(ActionEvent event) {
        NavigationHelper.navigateTo(event, "/com/pidev/hello-view.fxml");
    }

    @FXML
    private void onNavOeuvres(ActionEvent event) {
        NavigationHelper.navigateTo(event, "/com/pidev/hello-view.fxml");
    }

    @FXML
    private void onNavEvenements(ActionEvent event) {
        NavigationHelper.navigateTo(event, NavigationHelper.EVT_LIST);
    }

    @FXML
    private void onNavBlog(ActionEvent event) {
        NavigationHelper.navigateTo(event, "/com/pidev/hello-view.fxml");
    }

    @FXML
    private void onNavFormation(ActionEvent event) {
        NavigationHelper.navigateTo(event, NavigationHelper.PART_LIST);
    }

    @FXML
    private void onVoirAdmin(ActionEvent event) {
        NavigationHelper.navigateTo(event, NavigationHelper.ADMIN_EVENEMENTS);
    }

    @FXML
    private void onAccountProfile(ActionEvent event) {
        NavigationHelper.navigateTo(rootPane, "/com/pidev/hello-view.fxml");
    }

    @FXML
    private void onAccountLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Compte");
        alert.setContentText("Déconnexion simulée (à relier à votre auth).");
        alert.showAndWait();
    }

    @FXML
    private void onClosePaymentBanner() {
        hidePaymentBanner();
    }

    private void showPaymentBanner(String variant, String title, String message) {
        if (paymentBanner == null) {
            return;
        }
        paymentBanner.getStyleClass().removeAll("payment-banner-success", "payment-banner-error");
        if ("success".equalsIgnoreCase(variant)) {
            paymentBanner.getStyleClass().add("payment-banner-success");
        } else {
            paymentBanner.getStyleClass().add("payment-banner-error");
        }
        paymentBannerTitle.setText(title);
        paymentBannerMessage.setText(message);
        paymentBanner.setVisible(true);
        paymentBanner.setManaged(true);
    }

    private void hidePaymentBanner() {
        if (paymentBanner == null) {
            return;
        }
        paymentBanner.setVisible(false);
        paymentBanner.setManaged(false);
    }

    private String resolveImage(String rawImage) {
        if (rawImage == null || rawImage.isBlank()) {
            return "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=1200&auto=format&fit=crop";
        }

        String value = rawImage.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }

        return "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=1200&auto=format&fit=crop";
    }

    private String resolveEvenementTable(Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        if (tableExists(meta, "evenement")) {
            return "evenement";
        }
        if (tableExists(meta, "evenements")) {
            return "evenements";
        }
        return null;
    }

    private boolean tableExists(DatabaseMetaData meta, String tableName) throws SQLException {
        try (ResultSet rs = meta.getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    private String stringValue(ResultSet rs, Set<String> cols, String... names) throws SQLException {
        for (String name : names) {
            if (cols.contains(name.toLowerCase(Locale.ROOT))) {
                return rs.getString(name);
            }
        }
        return "";
    }

    private Integer intValue(ResultSet rs, Set<String> cols, String... names) throws SQLException {
        for (String name : names) {
            if (cols.contains(name.toLowerCase(Locale.ROOT))) {
                int value = rs.getInt(name);
                return rs.wasNull() ? 0 : value;
            }
        }
        return 0;
    }

    private Float floatValue(ResultSet rs, Set<String> cols, String... names) throws SQLException {
        for (String name : names) {
            if (cols.contains(name.toLowerCase(Locale.ROOT))) {
                float value = rs.getFloat(name);
                return rs.wasNull() ? 0f : value;
            }
        }
        return 0f;
    }

    private Boolean booleanValue(ResultSet rs, Set<String> cols, String... names) throws SQLException {
        for (String name : names) {
            if (cols.contains(name.toLowerCase(Locale.ROOT))) {
                boolean value = rs.getBoolean(name);
                return !rs.wasNull() && value;
            }
        }
        return false;
    }

    private LocalDate dateValue(ResultSet rs, Set<String> cols, String... names) throws SQLException {
        for (String name : names) {
            if (cols.contains(name.toLowerCase(Locale.ROOT))) {
                Object object = rs.getObject(name);
                if (object instanceof Date d) {
                    return d.toLocalDate();
                }
                if (object instanceof LocalDate d) {
                    return d;
                }
            }
        }
        return LocalDate.now();
    }

    private LocalTime timeValue(ResultSet rs, Set<String> cols, String... names) throws SQLException {
        for (String name : names) {
            if (cols.contains(name.toLowerCase(Locale.ROOT))) {
                Object object = rs.getObject(name);
                if (object instanceof Time t) {
                    return t.toLocalTime();
                }
                if (object instanceof LocalTime t) {
                    return t;
                }
            }
        }
        return LocalTime.of(10, 0);
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
