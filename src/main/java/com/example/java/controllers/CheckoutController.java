package com.example.java.controllers;

import com.example.java.entities.CustomerInfo;
import com.example.java.entities.Oeuvre;
import com.example.java.services.EmailService;
import com.example.java.services.InvoicePdfService;
import com.example.java.services.OeuvreService;
import com.example.java.services.PanierService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CheckoutController {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String DEFAULT_STRIPE_LINK = "https://buy.stripe.com/test_7sYfZa4rC1Am3xf7mB1wY02";

    @FXML private VBox itemsContainer;
    @FXML private Label totalLabel;
    @FXML private Label messageLabel;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private WebView stripeWebView;
    @FXML private Button sendInvoiceButton;
    @FXML private Button downloadInvoiceButton;
    @FXML private Button stripeCheckoutButton;
    @FXML private Label stripeInfoLabel;

    private Scene previousScene;
    private Path lastInvoicePath;

    private final PanierService panierService = PanierService.getInstance();
    private final EmailService emailService = new EmailService();
    private final InvoicePdfService invoicePdfService = new InvoicePdfService();
    private final OeuvreService oeuvreService = new OeuvreService();
    private final Map<String, String> localEnv = loadLocalEnv();

    @FXML
    public void initialize() {
        if (emailField != null && !emailService.defaultRecipient().isBlank()) {
            emailField.setText(emailService.defaultRecipient());
        }
        renderCart();
        loadStripePaymentLink();
    }

    public void init(Scene previousScene) {
        this.previousScene = previousScene;
    }

    @FXML
    public void goBack() {
        if (previousScene != null && itemsContainer != null && itemsContainer.getScene() != null) {
            ((Stage) itemsContainer.getScene().getWindow()).setScene(previousScene);
        }
    }

    @FXML
    public void reloadStripe() {
        loadStripePaymentLink();
    }
// hedhy focbtion strip
    @FXML
    public void openStripeCheckout() {
        loadStripePaymentLink();
    }

    @FXML
    public void clearCart() {
        panierService.clear();
        lastInvoicePath = null;
        renderCart();
        loadStripePaymentLink();
        setMessage("Panier vide.", false);
    }

    @FXML
    public void downloadInvoice() {
        List<Oeuvre> items = panierService.getItems();
        double total = panierService.total();
        CustomerInfo customer = customerInfo();

        if (items.isEmpty()) {
            setMessage("Le panier est vide.", true);
            return;
        }
        if (!validateCustomer(customer)) {
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Telecharger la facture PDF");
        chooser.setInitialFileName("facture-artgalerie.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File selectedFile = chooser.showSaveDialog(itemsContainer.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            invoicePdfService.generateTo(items, total, customer, selectedFile.toPath());
            lastInvoicePath = selectedFile.toPath();
            setMessage("Facture PDF telechargee : " + selectedFile.getAbsolutePath(), false);
        } catch (Exception e) {
            setMessage("Echec generation PDF : " + e.getMessage(), true);
        }
    }
// hedhy feha email , pdf , facture
    @FXML
    public void sendInvoice() {
        List<Oeuvre> items = panierService.getItems();
        double total = panierService.total();
        CustomerInfo customer = customerInfo();

        if (items.isEmpty()) {
            setMessage("Le panier est vide.", true);
            return;
        }
        if (!validateCustomer(customer)) {
            return;
        }
        if (!emailService.isConfigured()) {
            setMessage("Configuration Gmail manquante.", true);
            return;
        }

        sendInvoiceButton.setDisable(true);
        setMessage("Envoi de la facture en cours...", false);
// hedhy partie  l pdf
        Task<Path> task = new Task<>() {
            @Override
            protected Path call() throws Exception {
                Path invoicePdf = invoicePdfService.generate(items, total, customer);
                // hedhy ligne tabeeth l email
                emailService.sendInvoice(customer.getEmail(), items, total, customer, invoicePdf);
                for (Oeuvre item : items) {
                    oeuvreService.markAsSold(item.getId());
                }
                return invoicePdf;
            }
        };

        task.setOnSucceeded(event -> {
            lastInvoicePath = task.getValue();
            panierService.clear();
            renderCart();
            loadStripePaymentLink();
            sendInvoiceButton.setDisable(false);
            setMessage("Facture PDF envoyee et paiement valide : " + lastInvoicePath.toAbsolutePath(), false);
        });

        task.setOnFailed(event -> {
            sendInvoiceButton.setDisable(false);
            Throwable error = task.getException();
            setMessage("Echec facture : " + (error != null ? error.getMessage() : "erreur inconnue"), true);
        });

        Thread thread = new Thread(task, "artgalerie-invoice-mailer");
        thread.setDaemon(true);
        thread.start();
    }

    private void renderCart() {
        if (itemsContainer == null) return;

        List<Oeuvre> items = panierService.getItems();
        itemsContainer.getChildren().clear();

        if (items.isEmpty()) {
            Label empty = new Label("Aucune oeuvre dans le panier.");
            empty.setStyle("-fx-text-fill:rgba(255,255,255,0.45); -fx-font-size:13px;");
            itemsContainer.getChildren().add(empty);
            if (sendInvoiceButton != null) sendInvoiceButton.setDisable(true);
            if (downloadInvoiceButton != null) downloadInvoiceButton.setDisable(true);
        } else {
            for (Oeuvre item : items) {
                itemsContainer.getChildren().add(cartRow(item));
            }
            if (sendInvoiceButton != null) sendInvoiceButton.setDisable(false);
            if (downloadInvoiceButton != null) downloadInvoiceButton.setDisable(false);
        }

        if (totalLabel != null) {
            totalLabel.setText(String.format("Total : %.2f TND", panierService.total()));
        }
        setStripeInfo(stripeInfoText(), false);
    }

    private HBox cartRow(Oeuvre item) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color:rgba(255,255,255,0.04);"
                + "-fx-background-radius:10; -fx-border-color:rgba(212,175,55,0.12);"
                + "-fx-border-radius:10; -fx-border-width:1;");

        VBox texts = new VBox(3);
        Label title = new Label(item.getTitre());
        title.setStyle("-fx-text-fill:#f2f2f6; -fx-font-size:13px; -fx-font-weight:700;");
        title.setWrapText(true);

        Label price = new Label(String.format("%.2f TND", item.getPrix()));
        price.setStyle("-fx-text-fill:#d4af37; -fx-font-size:12px; -fx-font-weight:700;");
        texts.getChildren().addAll(title, price);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button remove = new Button("Retirer");
        remove.setStyle("-fx-background-color:rgba(239,68,68,0.15); -fx-text-fill:#ff9b9b;"
                + "-fx-background-radius:8; -fx-border-color:rgba(239,68,68,0.30);"
                + "-fx-border-radius:8; -fx-padding:7 10; -fx-cursor:HAND;");
        remove.setOnAction(event -> {
            panierService.remove(item.getId());
            renderCart();
            loadStripePaymentLink();
        });

        row.getChildren().addAll(texts, spacer, remove);
        return row;
    }

    private void setMessage(String message, boolean error) {
        if (messageLabel == null) return;
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size:12px; -fx-text-fill:" + (error ? "#ff9b9b" : "#7fe5a8") + ";");
    }

    private void loadStripePaymentLink() {
        if (stripeWebView == null) return;

        String url = stripePaymentUrl();
        setStripeInfo("Lien Stripe charge dans la WebView.", false);
        stripeWebView.getEngine().load(url);
    }

    private String stripeInfoText() {
        return "Paiement avec le lien Stripe configure. Le montant affiche par Stripe vient du lien Payment Link.";
    }

    private void setStripeInfo(String message, boolean error) {
        if (stripeInfoLabel == null) return;
        stripeInfoLabel.setText(message);
        stripeInfoLabel.setStyle("-fx-font-size:12px; -fx-text-fill:" + (error ? "#ff9b9b" : "rgba(255,255,255,0.55)") + ";");
    }

    private String stripePaymentUrl() {
        String baseUrl = value("STRIPE_PAYMENT_LINK");
        if (baseUrl.isBlank()) {
            baseUrl = DEFAULT_STRIPE_LINK;
        }

        String email = emailField != null && emailField.getText() != null ? emailField.getText().trim() : "";
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return baseUrl;
        }

        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "prefilled_email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
    }

    private CustomerInfo customerInfo() {
        return new CustomerInfo(
                text(prenomField),
                text(nomField),
                text(emailField),
                text(telephoneField),
                text(adresseField)
        );
    }

    private boolean validateCustomer(CustomerInfo customer) {
        if (customer.getPrenom().isBlank()) {
            setMessage("Prenom obligatoire.", true);
            return false;
        }
        if (customer.getNom().isBlank()) {
            setMessage("Nom obligatoire.", true);
            return false;
        }
        if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            setMessage("Email client invalide.", true);
            return false;
        }
        if (customer.getTelephone().isBlank() || customer.getTelephone().length() < 6) {
            setMessage("Telephone obligatoire et valide.", true);
            return false;
        }
        if (customer.getAdresse().isBlank() || customer.getAdresse().length() < 6) {
            setMessage("Adresse complete obligatoire.", true);
            return false;
        }
        return true;
    }

    private String text(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private String value(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String environment = System.getenv(key);
        if (environment != null && !environment.isBlank()) {
            return environment.trim();
        }

        return localEnv.getOrDefault(key, "").trim();
    }

    private Map<String, String> loadLocalEnv() {
        Map<String, String> values = new HashMap<>();
        Path path = Path.of(".env");

        if (!Files.exists(path)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isBlank() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                values.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
            }
        } catch (IOException ignored) {
        }

        return values;
    }
}
