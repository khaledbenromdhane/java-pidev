package com.pidev.controllers;

import com.pidev.entities.Evenement;
import com.pidev.entities.Participation;
import com.pidev.services.CrudService;
import com.pidev.services.ParticipationJdbcService;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminScanController implements Initializable {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static class ScanRow {
        private final int id;
        private final String eventName;
        private final String eventMeta;
        private final int places;
        private final String paymentMode;
        private final String status;
        private final String scannedAtLabel;
        private final LocalDateTime scannedAt;

        public ScanRow(int id, String eventName, String eventMeta, int places, String paymentMode, String status,
                       String scannedAtLabel, LocalDateTime scannedAt) {
            this.id = id;
            this.eventName = eventName;
            this.eventMeta = eventMeta;
            this.places = places;
            this.paymentMode = paymentMode;
            this.status = status;
            this.scannedAtLabel = scannedAtLabel;
            this.scannedAt = scannedAt;
        }

        public int getId() {
            return id;
        }

        public String getEventName() {
            return eventName;
        }

        public String getEventMeta() {
            return eventMeta;
        }

        public int getPlaces() {
            return places;
        }

        public String getPaymentMode() {
            return paymentMode;
        }

        public String getStatus() {
            return status;
        }

        public String getScannedAtLabel() {
            return scannedAtLabel;
        }

        public LocalDateTime getScannedAt() {
            return scannedAt;
        }
    }

    @FXML private Button sidebarEvtLink;
    @FXML private Button sidebarPartLink;
    @FXML private Button toggleScannerBtn;
    @FXML private Button refreshHistoryBtn;
    @FXML private Button statusActionBtn;
    @FXML private TextField manualIdField;
    @FXML private Label historyMetricLabel;
    @FXML private Label cameraMetricLabel;
    @FXML private Label lastScanMetricLabel;
    @FXML private Label cameraStatePill;
    @FXML private Label historyCountBadge;
    @FXML private Label statusIconLabel;
    @FXML private Label statusTitleLabel;
    @FXML private Label statusMessageLabel;
    @FXML private HBox statusBanner;
    @FXML private ImageView cameraPreview;
    @FXML private VBox cameraPlaceholder;
    @FXML private TableView<ScanRow> scanTable;
    @FXML private TableColumn<ScanRow, Integer> colId;
    @FXML private TableColumn<ScanRow, ScanRow> colEvent;
    @FXML private TableColumn<ScanRow, Integer> colPlaces;
    @FXML private TableColumn<ScanRow, String> colPayment;
    @FXML private TableColumn<ScanRow, String> colStatus;
    @FXML private TableColumn<ScanRow, String> colScannedAt;

    private final ParticipationJdbcService service = new ParticipationJdbcService();
    private final CrudService<Participation, Integer> crudService = service;
    private final ObservableList<ScanRow> scanRows = FXCollections.observableArrayList();

    private boolean scannerActive;
    private Webcam webcam;
    private ScheduledExecutorService cameraExecutor;
    private final AtomicBoolean processingScan = new AtomicBoolean(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTable();
        configurePlaceholder();
        scanTable.setItems(scanRows);
        setScannerState(false);
        showCameraPlaceholder(true);
        showStatus("idle", "Ready for scanning",
            "Paste a QR payload or enter a participation ID to validate a scan.");
        loadScannedHistory();
    }

    private void configureTable() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colPlaces.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPlaces()).asObject());
        colPayment.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPaymentMode()));
        colStatus.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus()));
        colScannedAt.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getScannedAtLabel()));
        colEvent.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));

        colEvent.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ScanRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                Label name = new Label(item.getEventName());
                name.getStyleClass().add("scan-event-name");

                Label meta = new Label(item.getEventMeta());
                meta.getStyleClass().add("scan-event-meta");
                meta.setWrapText(true);

                VBox box = new VBox(3, name, meta);
                box.getStyleClass().add("scan-event-cell");
                setGraphic(box);
            }
        });

        colPayment.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(buildBadge(item, paymentBadgeClass(item)));
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(buildBadge(item, statusBadgeClass(item)));
            }
        });
    }

    private void configurePlaceholder() {
        Label placeholder = new Label("No scanned participations yet.");
        placeholder.getStyleClass().add("subtitle");
        scanTable.setPlaceholder(placeholder);
    }

    private Label buildBadge(String text, String extraClass) {
        Label badge = new Label(text);
        badge.getStyleClass().add("scan-badge");
        badge.getStyleClass().add(extraClass);
        return badge;
    }

    private String paymentBadgeClass(String paymentMode) {
        String mode = paymentMode == null ? "" : paymentMode.trim().toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "carte" -> "scan-badge-card";
            case "cash" -> "scan-badge-cash";
            default -> "scan-badge-free";
        };
    }

    private String statusBadgeClass(String status) {
        String normalized = status == null ? "" : status.toLowerCase(Locale.ROOT);
        if (normalized.contains("confirm")) {
            return "scan-badge-success";
        }
        if (normalized.contains("attente")) {
            return "scan-badge-warning";
        }
        if (normalized.contains("annul")) {
            return "scan-badge-error";
        }
        return "scan-badge-info";
    }

    private void loadScannedHistory() {
        List<ScanRow> rows = crudService.findAll().stream()
                .filter(Participation::isScanned)
                .map(this::toRow)
                .sorted(Comparator.comparing(ScanRow::getScannedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        scanRows.setAll(rows);
        historyMetricLabel.setText(String.valueOf(rows.size()));
        historyCountBadge.setText(String.valueOf(rows.size()));
        lastScanMetricLabel.setText(rows.isEmpty() ? "No scan yet" : rows.get(0).getScannedAtLabel());
    }

    private ScanRow toRow(Participation participation) {
        Evenement evenement = participation.getEvenement();
        String eventName = evenement == null ? "Unknown event" : safe(evenement.getNom(), "Unknown event");

        String eventDate = evenement == null || evenement.getDate() == null
                ? "No date"
                : DATE_FORMATTER.format(evenement.getDate());
        String eventTime = evenement == null || evenement.getHeure() == null
                ? "--:--"
                : TIME_FORMATTER.format(evenement.getHeure());
        String eventPlace = evenement == null ? "No location" : safe(evenement.getLieu(), "No location");
        String eventType = evenement == null ? "Event" : safe(evenement.getTypeEvenement(), "Event");
        String eventMeta = eventType + " | " + eventDate + " | " + eventTime + " | " + eventPlace;

        String paymentMode = safe(participation.getModePaiement(), "Gratuit");
        String scannedAtLabel = participation.getScannedAt() == null
                ? "Pending scan"
                : DATE_TIME_FORMATTER.format(participation.getScannedAt());

        return new ScanRow(
                participation.getId(),
                eventName,
                eventMeta,
                participation.getNbrParticipation(),
                paymentMode,
                safe(participation.getStatut(), "Inconnu"),
                scannedAtLabel,
                participation.getScannedAt()
        );
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void setScannerState(boolean active) {
        scannerActive = active;
        cameraMetricLabel.setText(active ? "Live" : "Offline");
        cameraStatePill.setText(active ? "Camera active" : "Camera inactive");

        cameraStatePill.getStyleClass().removeAll("scan-state-pill-live", "scan-state-pill-idle");
        cameraStatePill.getStyleClass().add(active ? "scan-state-pill-live" : "scan-state-pill-idle");

        toggleScannerBtn.getStyleClass().remove("btn-scan-stop");
        if (active) {
            toggleScannerBtn.getStyleClass().add("btn-scan-stop");
            toggleScannerBtn.setText("Stop scanner");
        } else {
            toggleScannerBtn.setText("Start scanner");
        }
    }

    private void showStatus(String variant, String title, String message) {
        statusBanner.getStyleClass().removeAll(
                "scan-status-idle",
                "scan-status-info",
                "scan-status-success",
                "scan-status-warning",
                "scan-status-error"
        );
        statusBanner.getStyleClass().add("scan-status-" + variant);

        statusIconLabel.setText(statusIconFor(variant));
        statusTitleLabel.setText(title);
        statusMessageLabel.setText(message);
    }

    private String statusIconFor(String variant) {
        return switch (variant) {
            case "success" -> "OK";
            case "warning" -> "!";
            case "error" -> "X";
            case "info" -> "i";
            default -> "~";
        };
    }

    @FXML
    private void onToggleScanner() {
        if (scannerActive) {
            stopScanner();
            showStatus("idle", "Scanner paused",
                    "You can still verify by entering a participation ID manually.");
        } else {
            startScanner();
        }
    }

    @FXML
    private void onManualPreview() {
        String raw = manualIdField.getText() == null ? "" : manualIdField.getText().trim();
        if (raw.isBlank()) {
            showStatus("warning", "Participation ID required",
                    "Enter a participation ID or paste the QR payload to verify.");
            return;
        }

        Integer id = parseParticipationId(raw);
        if (id == null) {
            showStatus("error", "Invalid QR payload",
                    "Paste a valid QR payload or a numeric participation ID.");
            return;
        }
        verifyParticipation(id);
    }

    private Integer parseParticipationId(String raw) {
        if (raw == null) {
            return null;
        }

        String trimmed = raw.trim();
        if (trimmed.matches("\\d+")) {
            return Integer.parseInt(trimmed);
        }

        String[] lines = trimmed.split("\\R");
        for (String line : lines) {
            String normalized = line.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("participation:")) {
                String value = line.substring(line.indexOf(':') + 1).trim();
                if (value.matches("\\d+")) {
                    return Integer.parseInt(value);
                }
            }
        }

        return null;
    }

    private boolean isCancelled(String statut) {
        String value = statut == null ? "" : statut.toLowerCase(Locale.ROOT);
        return value.contains("annul");
    }

    private boolean isConfirmed(String statut) {
        String value = statut == null ? "" : statut.toLowerCase(Locale.ROOT);
        return value.contains("confirm");
    }

    @FXML
    private void onReloadHistory() {
        loadScannedHistory();
        showStatus("info", "History refreshed",
                "The scanned participation table has been reloaded from the current Java data source.");
    }

    @FXML
    private void onStatusAction() {
        manualIdField.requestFocus();
    }

    @FXML
    private void onNavigateEvenements() {
        NavigationHelper.navigateTo(sidebarEvtLink, NavigationHelper.ADMIN_EVENEMENTS);
    }

    @FXML
    private void onNavigateParticipations() {
        NavigationHelper.navigateTo(sidebarPartLink, NavigationHelper.ADMIN_PARTICIPATIONS);
    }

    @FXML
    private void onViewParticipations(ActionEvent event) {
        NavigationHelper.navigateTo(event, NavigationHelper.ADMIN_PARTICIPATIONS);
    }

    @FXML
    private void onReturnToEvents(ActionEvent event) {
        NavigationHelper.navigateTo(event, NavigationHelper.ADMIN_EVENEMENTS);
    }

    private void startScanner() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            setScannerState(false);
            showStatus("error", "No camera detected",
                    "Connect a camera or use the manual verification field.");
            return;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        if (!webcam.open()) {
            setScannerState(false);
            showStatus("error", "Camera unavailable",
                    "The camera could not be opened. Try again or use manual verification.");
            return;
        }

        setScannerState(true);
        showCameraPlaceholder(false);
        showStatus("info", "Scanner running",
                "Point the camera at a QR code to verify a participation.");

        cameraExecutor = Executors.newSingleThreadScheduledExecutor();
        cameraExecutor.scheduleAtFixedRate(this::captureFrame, 0, 200, TimeUnit.MILLISECONDS);
    }

    private void stopScanner() {
        setScannerState(false);
        processingScan.set(false);

        if (cameraExecutor != null) {
            cameraExecutor.shutdownNow();
            cameraExecutor = null;
        }

        if (webcam != null) {
            if (webcam.isOpen()) {
                webcam.close();
            }
            webcam = null;
        }

        if (cameraPreview != null) {
            cameraPreview.setImage(null);
        }
        showCameraPlaceholder(true);
    }

    private void captureFrame() {
        if (!scannerActive || webcam == null || !webcam.isOpen()) {
            return;
        }

        BufferedImage image = webcam.getImage();
        if (image == null) {
            return;
        }

        Platform.runLater(() -> cameraPreview.setImage(SwingFXUtils.toFXImage(image, null)));

        if (!processingScan.compareAndSet(false, true)) {
            return;
        }

        Result result = decodeQr(image);
        if (result == null || result.getText() == null || result.getText().isBlank()) {
            processingScan.set(false);
            return;
        }

        String payload = result.getText();
        Platform.runLater(() -> handleScanPayload(payload));
    }

    private Result decodeQr(BufferedImage image) {
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            return new MultiFormatReader().decode(bitmap);
        } catch (NotFoundException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private void handleScanPayload(String payload) {
        Integer id = parseParticipationId(payload);
        if (id == null) {
            showStatus("error", "Invalid QR payload",
                    "The QR code does not contain a participation ID.");
            processingScan.set(false);
            return;
        }

        verifyParticipation(id);
        stopScanner();
    }

    private void verifyParticipation(Integer id) {
        Optional<Participation> optional = crudService.findById(id);
        if (optional.isEmpty()) {
            showStatus("error", "Participation not found",
                    "No participation matches #" + id + ".");
            processingScan.set(false);
            return;
        }

        Participation participation = optional.get();
        if (participation.isScanned()) {
            String when = participation.getScannedAt() == null
                    ? "already scanned"
                    : "already scanned on " + DATE_TIME_FORMATTER.format(participation.getScannedAt());
            showStatus("warning", "QR already used",
                    "Participation #" + id + " is " + when + ".");
            processingScan.set(false);
            return;
        }

        if (isCancelled(participation.getStatut())) {
            showStatus("error", "Cancelled participation",
                    "Participation #" + id + " is cancelled and cannot be scanned.");
            processingScan.set(false);
            return;
        }

        participation.setScanned(true);
        participation.setScannedAt(LocalDateTime.now());
        if (!isConfirmed(participation.getStatut())) {
            participation.setStatut("Confirmée");
        }
        crudService.update(participation);
        loadScannedHistory();
        showStatus("success", "Scan confirmed",
                "Participation #" + id + " has been validated successfully.");
        manualIdField.clear();
        processingScan.set(false);
    }

    private void showCameraPlaceholder(boolean show) {
        if (cameraPlaceholder != null) {
            cameraPlaceholder.setVisible(show);
            cameraPlaceholder.setManaged(show);
        }
        if (cameraPreview != null) {
            cameraPreview.setVisible(!show);
            cameraPreview.setManaged(!show);
        }
    }
}
