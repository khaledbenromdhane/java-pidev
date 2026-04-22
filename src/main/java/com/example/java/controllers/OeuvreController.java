package com.example.java.controllers;

import com.example.java.entities.Galerie;
import com.example.java.entities.Oeuvre;
import com.example.java.entities.User;
import com.example.java.services.GalerieService;
import com.example.java.services.OeuvreService;
import com.example.java.services.PanierService;
import com.example.java.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OeuvreController implements Initializable {

    // ── Admin (OeuvreAdmin.fxml) ──
    @FXML private FlowPane oeuvreContainer;
    @FXML private TextField searchField;
    @FXML private Label totalLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private Button btnRetour;
    @FXML private ComboBox<String> filterStatut;

    // ── Front (OeuvreFront.fxml) ──
    @FXML private Label galerieNomLabel;
    @FXML private Label galerieCatLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private Label galerieEmployesLabel;
    @FXML private Button cartButton;

    private Galerie currentGalerie;
    private boolean isFrontMode = false;
    private GalerieController dashboardController;
    private Scene previousScene;
    private List<Oeuvre> allOeuvres;

    private final OeuvreService service = new OeuvreService();
    private final GalerieService galerieService = new GalerieService();
    private final UserService userService = new UserService();
    private final PanierService panierService = PanierService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (filterStatut != null) {
            filterStatut.getItems().addAll("Tous", "disponible", "réservé", "vendu");
            filterStatut.setValue("Tous");
        }
        updateCartButton();
    }

    public void initAdmin(Galerie galerie, GalerieController dashCtrl) {
        this.currentGalerie = galerie;
        this.dashboardController = dashCtrl;
        this.isFrontMode = false;

        if (galerie != null) {
            if (pageTitleLabel != null) {
                pageTitleLabel.setText("🎨  Œuvres — " + galerie.getNom());
            }
            if (pageSubtitleLabel != null) {
                pageSubtitleLabel.setText("Galerie : " + galerie.getNom() + " | " + galerie.getCategorie());
            }
            if (btnRetour != null) {
                btnRetour.setVisible(true);
            }
        } else {
            if (pageTitleLabel != null) {
                pageTitleLabel.setText("🎨  Toutes les Œuvres");
            }
            if (pageSubtitleLabel != null) {
                pageSubtitleLabel.setText("Catalogue complet des œuvres d'art");
            }
            if (btnRetour != null) {
                btnRetour.setVisible(false);
            }
        }

        loadData();
    }

    public void initFront(Galerie galerie, Scene prevScene) {
        this.currentGalerie = galerie;
        this.previousScene = prevScene;
        this.isFrontMode = true;

        if (galerieNomLabel != null) {
            galerieNomLabel.setText(galerie.getNom());
        }
        if (galerieCatLabel != null) {
            galerieCatLabel.setText("Catégorie : " + galerie.getCategorie());
        }
        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText(galerie.getNom());
        }
        if (galerieEmployesLabel != null) {
            galerieEmployesLabel.setText("👥 " + galerie.getNbEmployes() + " employés");
        }
        updateCartButton();

        loadData();
    }

    private void loadData() {
        if (oeuvreContainer == null) return;

        oeuvreContainer.getChildren().clear();

        try {
            allOeuvres = currentGalerie != null
                    ? service.getOeuvresByGalerie(currentGalerie.getIdGalerie())
                    : service.getAllOeuvres();

            updateTotal(allOeuvres.size());
            renderCards(allOeuvres);
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    private void renderCards(List<Oeuvre> list) {
        if (oeuvreContainer == null) return;

        oeuvreContainer.getChildren().clear();

        if (list == null || list.isEmpty()) {
            Label empty = new Label("Aucune œuvre " + (isFrontMode ? "disponible." : ". Cliquez sur '＋ Nouvelle Œuvre'."));
            empty.setStyle("-fx-font-size:14px; -fx-text-fill:rgba(212,175,55,0.45);");
            oeuvreContainer.getChildren().add(empty);
            updateTotal(0);
            return;
        }

        for (Oeuvre o : list) {
            oeuvreContainer.getChildren().add(isFrontMode ? buildFrontCard(o) : buildAdminCard(o));
        }

        updateTotal(list.size());
    }

    private void updateTotal(int n) {
        if (totalLabel != null) {
            totalLabel.setText(n + " œuvre(s)" + (currentGalerie != null ? " dans cette galerie" : " au total"));
        }
    }

    // ─────────────────────────────────────────────
    // Cards Admin
    // ─────────────────────────────────────────────
    private VBox buildAdminCard(Oeuvre o) {
        VBox card = new VBox(0);
        card.setPrefWidth(278);
        baseCardStyle(card, 14);

        String[] sc = statusColors(o.getStatut());

        StackPane imagePane = buildImagePane(
                o.getImage(),
                278,
                150,
                "-fx-background-color:" + statusGradient(o.getStatut()) + "; -fx-background-radius:14 14 0 0;"
        );

        VBox headerInfo = new VBox(5);
        headerInfo.setPadding(new Insets(13, 16, 11, 16));
        headerInfo.setStyle(
                "-fx-background-color:" + sc[0] + ";" +
                        "-fx-border-color:rgba(255,255,255,0.06) transparent transparent transparent;" +
                        "-fx-border-width:0 0 1 0;"
        );

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label art = new Label("🎨");
        art.setStyle("-fx-font-size:20;");

        Label tit = new Label(o.getTitre());
        tit.setStyle("-fx-text-fill:" + sc[1] + "; -fx-font-size:13px; -fx-font-weight:700;");
        tit.setWrapText(true);
        tit.setMaxWidth(200);
        HBox.setHgrow(tit, Priority.ALWAYS);

        row.getChildren().addAll(art, tit);

        Label sb = new Label("  " + statLabel(o.getStatut()) + "  ");
        sb.setStyle(
                "-fx-background-color:" + sc[2] + ";" +
                        "-fx-text-fill:" + sc[1] + ";" +
                        "-fx-font-size:10px;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:3 8;" +
                        "-fx-border-color:" + sc[3] + ";" +
                        "-fx-border-radius:20;" +
                        "-fx-border-width:1;"
        );

        headerInfo.getChildren().addAll(row, sb);

        VBox body = new VBox(8);
        body.setPadding(new Insets(12, 16, 12, 16));

        HBox prixRow = new HBox(6);
        prixRow.setAlignment(Pos.CENTER_LEFT);
        prixRow.setStyle(
                "-fx-background-color:rgba(212,175,55,0.06);" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:8 12;" +
                        "-fx-border-color:rgba(212,175,55,0.12);" +
                        "-fx-border-radius:8;" +
                        "-fx-border-width:1;"
        );

        Label prixV = new Label(String.format("💰  %.2f TND", o.getPrix()));
        prixV.setStyle("-fx-font-size:16px; -fx-font-weight:700; -fx-text-fill:#f7d777;");
        prixRow.getChildren().add(prixV);

        body.getChildren().addAll(
                prixRow,
                infoRow("🎭", "État", o.getEtat() != null && !o.getEtat().isBlank() ? o.getEtat() : "-"),
                infoRow("📅", "Année", String.valueOf(o.getAnneeRealisation())),
                infoRow("🆔", "Artiste", "#" + o.getIdArtiste())
        );

        if (o.getDescription() != null && !o.getDescription().isBlank()) {
            String d = o.getDescription().length() > 65
                    ? o.getDescription().substring(0, 65) + "..."
                    : o.getDescription();

            Label dl = new Label(d);
            dl.setStyle("-fx-font-size:11px; -fx-text-fill:rgba(255,255,255,0.30);");
            dl.setWrapText(true);
            body.getChildren().add(dl);
        }

        Separator sep = goldSep();

        HBox actions = new HBox(8);
        actions.setPadding(new Insets(10, 14, 12, 14));
        actions.setAlignment(Pos.CENTER);

        Button be = aBtn("✏️  Modifier", "rgba(212,175,55,0.18)", "#f7d777", "rgba(212,175,55,0.30)");
        be.setOnAction(e -> showDialog(o));
        HBox.setHgrow(be, Priority.ALWAYS);
        be.setMaxWidth(Double.MAX_VALUE);

        Button bd = aBtn("🗑️", "rgba(239,68,68,0.18)", "#ff9b9b", "rgba(239,68,68,0.30)");
        bd.setOnAction(e -> deleteOeuvre(o));

        actions.getChildren().addAll(be, bd);

        card.getChildren().addAll(imagePane, headerInfo, body, sep, actions);
        hoverCard(card, sc[4], 14);

        return card;
    }

    // ─────────────────────────────────────────────
    // Cards Front
    // ─────────────────────────────────────────────
    private VBox buildFrontCard(Oeuvre o) {
        VBox card = new VBox(0);
        card.setPrefWidth(268);
        baseCardStyle(card, 14);

        String[] sc = statusColors(o.getStatut());

        StackPane imagePane = buildImagePane(
                o.getImage(),
                268,
                138,
                "-fx-background-color:" + statusGradient(o.getStatut()) + "; -fx-background-radius:14 14 0 0;"
        );

        Label sb = new Label("  " + statLabel(o.getStatut()) + "  ");
        sb.setStyle(
                "-fx-background-color:rgba(0,0,0,0.55);" +
                        "-fx-text-fill:" + sc[1] + ";" +
                        "-fx-font-size:10px;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:3 10;" +
                        "-fx-border-color:" + sc[3] + ";" +
                        "-fx-border-radius:20;" +
                        "-fx-border-width:1;"
        );
        StackPane.setAlignment(sb, Pos.TOP_RIGHT);
        StackPane.setMargin(sb, new Insets(10, 10, 0, 0));
        imagePane.getChildren().add(sb);

        VBox body = new VBox(8);
        body.setPadding(new Insets(12, 16, 14, 16));

        Label tit = new Label(o.getTitre());
        tit.setStyle("-fx-font-size:14px; -fx-font-weight:700; -fx-text-fill:#e9e9ee;");
        tit.setWrapText(true);

        Label px = new Label(String.format("💰  %.2f TND", o.getPrix()));
        px.setStyle("-fx-font-size:15px; -fx-font-weight:700; -fx-text-fill:#d4af37;");

        HBox meta = new HBox(8);
        meta.getChildren().addAll(
                mpill("🎭 " + (o.getEtat() != null && !o.getEtat().isBlank() ? o.getEtat() : "-")),
                mpill("📅 " + o.getAnneeRealisation())
        );

        body.getChildren().addAll(tit, px, meta);

        if (o.getDescription() != null && !o.getDescription().isBlank()) {
            String d = o.getDescription().length() > 60
                    ? o.getDescription().substring(0, 60) + "..."
                    : o.getDescription();

            Label dl = new Label(d);
            dl.setStyle("-fx-font-size:11px; -fx-text-fill:rgba(255,255,255,0.28);");
            dl.setWrapText(true);
            body.getChildren().add(dl);
        }

        Separator sep = goldSep();

        HBox actions = new HBox();
        actions.setPadding(new Insets(0, 14, 14, 14));
        Button addCart = goldBtn("Ajouter au panier");
        addCart.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addCart, Priority.ALWAYS);
        addCart.setDisable(!isDisponible(o.getStatut()));
        addCart.setOnAction(e -> addToCart(o));
        actions.getChildren().add(addCart);

        card.getChildren().addAll(imagePane, body, sep, actions);
        hoverCard(card, "rgba(168,85,247,0.12)", 14);

        return card;
    }

    // ─────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────
    @FXML
    public void goBack() {
        if (isFrontMode) {
            if (previousScene != null && oeuvreContainer != null) {
                ((Stage) oeuvreContainer.getScene().getWindow()).setScene(previousScene);
            }
        } else {
            if (dashboardController != null) {
                dashboardController.loadGaleriesAdminPage();
            }
        }
    }

    @FXML
    public void goToGaleries() {
        goBack();
    }

    private void addToCart(Oeuvre oeuvre) {
        if (!isDisponible(oeuvre.getStatut())) {
            alert(Alert.AlertType.WARNING, "Oeuvre indisponible", "Cette oeuvre ne peut pas etre ajoutee au panier.");
            return;
        }

        boolean added = panierService.add(oeuvre);
        updateCartButton();

        if (added) {
            alert(Alert.AlertType.INFORMATION, "Panier", "Oeuvre ajoutee au panier.");
        } else {
            alert(Alert.AlertType.INFORMATION, "Panier", "Cette oeuvre est deja dans le panier.");
        }
    }

    private void updateCartButton() {
        if (cartButton != null) {
            cartButton.setText("Panier (" + panierService.size() + ")");
        }
    }

    @FXML
    public void openCheckout() {
        if (oeuvreContainer == null || oeuvreContainer.getScene() == null) return;

        try {
            Scene current = oeuvreContainer.getScene();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/java/front/Checkout.fxml")
            );
            Parent root = loader.load();

            CheckoutController ctrl = loader.getController();
            ctrl.init(current);

            Scene scene = new Scene(root, current.getWidth(), current.getHeight());
            URL cssUrl = getClass().getResource("/com/example/java/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            ((Stage) current.getWindow()).setScene(scene);
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────
    @FXML
    public void openAddDialog() {
        showDialog(null);
    }

    private void showDialog(Oeuvre ex) {
        boolean edit = ex != null;

        List<Galerie> galeries;
        try {
            galeries = galerieService.getAllGaleries();
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de charger les galeries : " + e.getMessage());
            return;
        }

        if (galeries.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Galerie requise", "Veuillez creer une galerie avant d'ajouter une oeuvre.");
            return;
        }

        List<User> users;
        try {
            users = userService.getAllUsers();
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de charger les artistes : " + e.getMessage());
            return;
        }

        if (users.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Artiste requis", "Veuillez creer un utilisateur avant d'ajouter une oeuvre.");
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(edit ? "✏️ Modifier l'Œuvre" : "➕ Nouvelle Œuvre");
        dlg.setResizable(false);

        VBox root = new VBox(10);
        root.setPadding(new Insets(26));
        root.setStyle("-fx-background-color:#0f0f1e;");
        root.setPrefWidth(500);

        Label title = new Label(edit ? "✏️ Modifier l'Œuvre" : "➕ Nouvelle Œuvre");
        title.setStyle("-fx-font-size:17px; -fx-font-weight:700; -fx-text-fill:#d4af37;");

        VBox fc = new VBox(10);
        fc.setStyle(
                "-fx-background-color:#151521;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:rgba(212,175,55,0.18);" +
                        "-fx-border-radius:14;" +
                        "-fx-border-width:1;" +
                        "-fx-padding:18;"
        );

        TextField tfT = fld("Titre de l'œuvre *");
        TextField tfPx = fld("Prix en TND *");
        TextField tfDs = fld("Description *");

        ComboBox<User> cbArtiste = new ComboBox<>();
        cbArtiste.getItems().addAll(users);
        cbArtiste.setPromptText("Selectionner un artiste");
        cbArtiste.setMaxWidth(Double.MAX_VALUE);
        cbArtiste.setStyle(comboStyle());
        cbArtiste.setConverter(new StringConverter<>() {
            @Override
            public String toString(User user) {
                if (user == null) return "";
                String prenom = user.getPrenomuser() != null ? user.getPrenomuser().trim() : "";
                String nom = user.getNomuser() != null ? user.getNomuser().trim() : "";
                String fullName = (prenom + " " + nom).trim();
                return fullName.isBlank() ? "Utilisateur #" + user.getIduser() : fullName;
            }

            @Override
            public User fromString(String value) {
                return null;
            }
        });

        ComboBox<Galerie> cbGalerie = new ComboBox<>();
        cbGalerie.getItems().addAll(galeries);
        cbGalerie.setPromptText("Selectionner une galerie");
        cbGalerie.setMaxWidth(Double.MAX_VALUE);
        cbGalerie.setStyle(comboStyle());
        cbGalerie.setConverter(new StringConverter<>() {
            @Override
            public String toString(Galerie galerie) {
                return galerie == null ? "" : galerie.getNom() + " - " + galerie.getCategorie();
            }

            @Override
            public Galerie fromString(String value) {
                return null;
            }
        });

        ComboBox<String> cbEt = new ComboBox<>();
        cbEt.getItems().addAll("Neuf", "Excellent", "Bon etat", "Correct", "A restaurer");
        cbEt.setPromptText("Selectionner l'etat");
        cbEt.setMaxWidth(Double.MAX_VALUE);
        cbEt.setStyle(comboStyle());

        DatePicker dpDate = new DatePicker();
        dpDate.setPromptText("Date de realisation");
        dpDate.setMaxWidth(Double.MAX_VALUE);
        dpDate.setStyle(comboStyle());

        TextField tfIm = fld("Image sélectionnée...");
        tfIm.setEditable(false);

        Button btnBrowseImage = ghostBtn("📁 Parcourir");
        btnBrowseImage.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
            );

            File selectedFile = fileChooser.showOpenDialog(dlg);
            if (selectedFile != null) {
                tfIm.setText(selectedFile.toURI().toString());
            }
        });

        HBox imageBox = new HBox(10, tfIm, btnBrowseImage);
        HBox.setHgrow(tfIm, Priority.ALWAYS);

        ComboBox<String> cbSt = new ComboBox<>();
        cbSt.getItems().addAll("disponible", "réservé", "vendue");
        cbSt.setValue("disponible");
        cbSt.setMaxWidth(Double.MAX_VALUE);
        cbSt.setStyle(comboStyle());

        if (edit) {
            tfT.setText(ex.getTitre());
            tfPx.setText(String.valueOf(ex.getPrix()));
            users.stream()
                    .filter(user -> user.getIduser() == ex.getIdArtiste())
                    .findFirst()
                    .ifPresent(user -> cbArtiste.getSelectionModel().select(user));
            if (ex.getEtat() != null && !ex.getEtat().isBlank() && !cbEt.getItems().contains(ex.getEtat())) {
                cbEt.getItems().add(ex.getEtat());
            }
            cbEt.setValue(ex.getEtat() != null && !ex.getEtat().isBlank() ? ex.getEtat() : null);
            if (ex.getAnneeRealisation() > 0) {
                dpDate.setValue(LocalDate.of(ex.getAnneeRealisation(), 1, 1));
            }
            tfIm.setText(ex.getImage() != null ? ex.getImage() : "");
            tfDs.setText(ex.getDescription() != null ? ex.getDescription() : "");
            cbSt.setValue(statusValueForForm(ex.getStatut()));
        }

        if (currentGalerie != null) {
            cbGalerie.getSelectionModel().select(galeries.stream()
                    .filter(g -> g.getIdGalerie() == currentGalerie.getIdGalerie())
                    .findFirst()
                    .orElse(currentGalerie));
        } else if (edit && ex.getIdGalerie() != null) {
            galeries.stream()
                    .filter(g -> g.getIdGalerie() == ex.getIdGalerie())
                    .findFirst()
                    .ifPresent(g -> cbGalerie.getSelectionModel().select(g));
        }

        fc.getChildren().addAll(
                flbl("Titre *"), tfT,
                flbl("Artiste *"), cbArtiste,
                flbl("Galerie *"), cbGalerie,
                flbl("Prix TND *"), tfPx,
                flbl("Etat *"), cbEt,
                flbl("Date de realisation *"), dpDate,
                flbl("Image"), imageBox,
                flbl("Description *"), tfDs,
                flbl("Statut *"), cbSt
        );

        Label err = new Label();
        err.setStyle("-fx-text-fill:#ff9b9b; -fx-font-size:12px;");

        HBox bb = new HBox(10);
        bb.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = ghostBtn("Annuler");
        cancel.setOnAction(e -> dlg.close());

        Button save = goldBtn(edit ? "💾 Sauvegarder" : "✅ Créer l'œuvre");
        save.setPrefWidth(170);

        save.setOnAction(e -> {
            String titre = tfT.getText().trim();
            String description = tfDs.getText().trim();
            User selectedArtiste = cbArtiste.getValue();
            Galerie selectedGalerie = cbGalerie.getValue();
            LocalDate dateRealisation = dpDate.getValue();

            if (titre.isEmpty()) {
                err.setText("⚠️ Titre obligatoire.");
                return;
            }
            if (selectedArtiste == null) {
                err.setText("⚠️ Selectionnez un artiste.");
                return;
            }
            if (selectedGalerie == null) {
                err.setText("⚠️ Selectionnez une galerie.");
                return;
            }
            if (cbEt.getValue() == null || cbEt.getValue().isBlank()) {
                err.setText("⚠️ Selectionnez l'etat de l'oeuvre.");
                return;
            }
            if (dateRealisation == null) {
                err.setText("⚠️ Date de realisation obligatoire.");
                return;
            }
            if (dateRealisation.isAfter(LocalDate.now())) {
                err.setText("⚠️ La date de realisation ne peut pas etre future.");
                return;
            }
            if (description.isEmpty()) {
                err.setText("⚠️ Description obligatoire.");
                return;
            }
            if (cbSt.getValue() == null || cbSt.getValue().isBlank()) {
                err.setText("⚠️ Selectionnez le statut.");
                return;
            }

            try {
                double px = Double.parseDouble(tfPx.getText().trim().replace(',', '.'));
                int an = dateRealisation.getYear();
                int idArtiste = selectedArtiste.getIduser();
                int idGal = selectedGalerie.getIdGalerie();

                if (px <= 0) {
                    err.setText("⚠️ Le prix doit etre strictement positif.");
                    return;
                }

                String img = tfIm.getText().trim().isEmpty() ? null : tfIm.getText().trim();
                String etat = cbEt.getValue();

                if (edit) {
                    ex.setTitre(titre);
                    ex.setIdArtiste(idArtiste);
                    ex.setPrix(px);
                    ex.setEtat(etat);
                    ex.setAnneeRealisation(an);
                    ex.setImage(img);
                    ex.setDescription(description);
                    ex.setIdGalerie(idGal);
                    ex.setStatut(statusValueForDatabase(cbSt.getValue()));
                    service.updateOeuvre(ex);
                } else {
                    service.addOeuvre(new Oeuvre(
                            idArtiste,
                            titre,
                            px,
                            etat,
                            an,
                            img,
                            description,
                            idGal,
                            statusValueForDatabase(cbSt.getValue())
                    ));
                }

                dlg.close();
                loadData();

            } catch (NumberFormatException ex2) {
                err.setText("⚠️ Prix invalide. Exemple attendu : 120.50");
            } catch (SQLException ex2) {
                err.setText("⚠️ Erreur BD : " + ex2.getMessage());
            }
        });

        bb.getChildren().addAll(cancel, save);

        ScrollPane sp = new ScrollPane(new VBox(10, title, new Separator(), fc, err, bb));
        ((VBox) sp.getContent()).setPadding(new Insets(26));
        ((VBox) sp.getContent()).setStyle("-fx-background-color:#0f0f1e;");
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:#0f0f1e;");

        dlg.setScene(new Scene(sp, 520, 620));
        dlg.showAndWait();
    }

    private void deleteOeuvre(Oeuvre o) {
        Alert conf = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + o.getTitre() + "\" ?\n⚠️ Irréversible.",
                ButtonType.YES,
                ButtonType.NO
        );
        conf.setTitle("Confirmer");
        conf.setHeaderText(null);

        if (conf.showAndWait().filter(r -> r == ButtonType.YES).isPresent()) {
            try {
                service.deleteOeuvre(o.getId());
                loadData();
            } catch (SQLException e) {
                alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────
    // Recherche / filtres
    // ─────────────────────────────────────────────
    @FXML
    public void onSearch() {
        if (allOeuvres == null) return;

        String q = searchField != null ? searchField.getText().toLowerCase().trim() : "";

        List<Oeuvre> filtered = allOeuvres.stream()
                .filter(o ->
                        containsIgnoreCase(o.getTitre(), q) ||
                                containsIgnoreCase(o.getEtat(), q) ||
                                containsIgnoreCase(o.getDescription(), q) ||
                                containsIgnoreCase(o.getStatut(), q) ||
                                String.valueOf(o.getIdArtiste()).contains(q)
                )
                .collect(Collectors.toList());

        applyStatusFilterAndRender(filtered);
    }

    @FXML
    public void clearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
        applyStatusFilterAndRender(allOeuvres);
    }

    @FXML
    public void onFilterChange() {
        applyStatusFilterAndRender(allOeuvres);
    }

    @FXML
    public void filterTous() {
        if (filterStatut != null) filterStatut.setValue("Tous");
        applyStatusFilterAndRender(allOeuvres);
    }

    @FXML
    public void filterDisponible() {
        if (filterStatut != null) filterStatut.setValue("disponible");
        applyStatusFilterAndRender(allOeuvres);
    }

    @FXML
    public void filterReserve() {
        if (filterStatut != null) filterStatut.setValue("réservé");
        applyStatusFilterAndRender(allOeuvres);
    }

    @FXML
    public void filterVendu() {
        if (filterStatut != null) filterStatut.setValue("vendu");
        applyStatusFilterAndRender(allOeuvres);
    }

    private void applyStatusFilterAndRender(List<Oeuvre> source) {
        if (source == null) return;

        String selected = filterStatut != null ? filterStatut.getValue() : "Tous";
        if (selected == null || selected.equals("Tous")) {
            renderCards(source);
            return;
        }

        String normalizedSelected = normalizeStatus(selected);
        List<Oeuvre> filtered = source.stream()
                .filter(o -> normalizedSelected.equals(normalizeStatus(o.getStatut())))
                .collect(Collectors.toList());

        renderCards(filtered);
    }

    private boolean containsIgnoreCase(String value, String query) {
        if (query == null || query.isBlank()) return true;
        return value != null && value.toLowerCase().contains(query);
    }

    // ─────────────────────────────────────────────
    // Image helpers
    // ─────────────────────────────────────────────
    private StackPane buildImagePane(String imagePath, double width, double height, String fallbackStyle) {
        StackPane container = new StackPane();
        container.setPrefSize(width, height);
        container.setMinSize(width, height);
        container.setMaxSize(width, height);
        container.setStyle(fallbackStyle);

        if (imagePath != null && !imagePath.isBlank()) {
            try {
                Image image = new Image(imagePath, false);
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(width);
                    imageView.setFitHeight(height);
                    imageView.setPreserveRatio(false);
                    imageView.setSmooth(true);
                    container.getChildren().add(imageView);
                    return container;
                }
            } catch (Exception ignored) {
            }
        }

        Label fallback = new Label("🖼️");
        fallback.setStyle("-fx-font-size:42; -fx-opacity:0.65;");
        container.getChildren().add(fallback);

        return container;
    }

    // ─────────────────────────────────────────────
    // Helpers visuels
    // ─────────────────────────────────────────────
    private void baseCardStyle(VBox c, int r) {
        c.setStyle(
                "-fx-background-color:#151521;" +
                        "-fx-background-radius:" + r + ";" +
                        "-fx-border-color:rgba(212,175,55,0.18);" +
                        "-fx-border-radius:" + r + ";" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.40),18,0.15,0,6);"
        );
    }

    private HBox infoRow(String em, String lb, String val) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        Label e = new Label(em);
        e.setMinWidth(20);
        e.setStyle("-fx-font-size:12;");

        Label l = new Label(lb + " :");
        l.setStyle("-fx-font-size:11px; -fx-text-fill:rgba(255,255,255,0.30);");
        HBox.setHgrow(l, Priority.ALWAYS);

        Label v = new Label(val);
        v.setStyle("-fx-font-size:12px; -fx-font-weight:700; -fx-text-fill:rgba(255,255,255,0.70);");

        r.getChildren().addAll(e, l, v);
        return r;
    }

    private Label mpill(String t) {
        Label l = new Label(t);
        l.setStyle(
                "-fx-background-color:rgba(212,175,55,0.07);" +
                        "-fx-text-fill:rgba(212,175,55,0.55);" +
                        "-fx-font-size:10px;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:3 8;" +
                        "-fx-border-color:rgba(212,175,55,0.12);" +
                        "-fx-border-radius:20;" +
                        "-fx-border-width:1;"
        );
        return l;
    }

    private Button goldBtn(String t) {
        Button b = new Button(t);
        b.setStyle(
                "-fx-background-color:linear-gradient(from 0% 0% to 100% 100%,#d4af37,#b8860b);" +
                        "-fx-text-fill:#0a0a0f;" +
                        "-fx-font-weight:700;" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:9 18;" +
                        "-fx-font-size:12px;" +
                        "-fx-cursor:HAND;"
        );
        return b;
    }

    private Button ghostBtn(String t) {
        Button b = new Button(t);
        b.setStyle(
                "-fx-background-color:rgba(255,255,255,0.06);" +
                        "-fx-text-fill:rgba(255,255,255,0.75);" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:9 14;" +
                        "-fx-font-size:12px;" +
                        "-fx-border-color:rgba(255,255,255,0.12);" +
                        "-fx-border-radius:8;" +
                        "-fx-border-width:1;" +
                        "-fx-cursor:HAND;"
        );
        return b;
    }

    private Button aBtn(String t, String bg, String fg, String bd) {
        Button b = new Button(t);
        b.setStyle(
                "-fx-background-color:" + bg + ";" +
                        "-fx-text-fill:" + fg + ";" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:8 11;" +
                        "-fx-font-size:12px;" +
                        "-fx-border-color:" + bd + ";" +
                        "-fx-border-radius:8;" +
                        "-fx-border-width:1;" +
                        "-fx-cursor:HAND;"
        );
        return b;
    }

    private TextField fld(String p) {
        TextField tf = new TextField();
        tf.setPromptText(p);
        tf.setStyle(
                "-fx-background-color:rgba(255,255,255,0.05);" +
                        "-fx-border-color:rgba(255,255,255,0.12);" +
                        "-fx-text-fill:#f2f2f6;" +
                        "-fx-prompt-text-fill:rgba(255,255,255,0.25);" +
                        "-fx-background-radius:10;" +
                        "-fx-border-radius:10;" +
                        "-fx-padding:9 12;" +
                        "-fx-font-size:13px;"
        );
        return tf;
    }

    private String comboStyle() {
        return "-fx-background-color:rgba(255,255,255,0.05);"
                + "-fx-border-color:rgba(255,255,255,0.12);"
                + "-fx-background-radius:10;"
                + "-fx-border-radius:10;"
                + "-fx-padding:2 8;"
                + "-fx-font-size:13px;";
    }

    private Label flbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:11px; -fx-text-fill:rgba(255,255,255,0.75); -fx-font-weight:700;");
        VBox.setMargin(l, new Insets(4, 0, -4, 0));
        return l;
    }

    private Separator goldSep() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color:rgba(212,175,55,0.08);");
        return s;
    }

    private void hoverCard(VBox c, String sc, int r) {
        String b =
                "-fx-background-color:#151521;" +
                        "-fx-background-radius:" + r + ";" +
                        "-fx-border-color:rgba(212,175,55,0.18);" +
                        "-fx-border-radius:" + r + ";" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.40),18,0.15,0,6);" +
                        "-fx-translate-y:0;";

        String hv =
                "-fx-background-color:#151521;" +
                        "-fx-background-radius:" + r + ";" +
                        "-fx-border-color:rgba(212,175,55,0.35);" +
                        "-fx-border-radius:" + r + ";" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian," + sc + ",24,0.22,0,8);" +
                        "-fx-translate-y:-3;";

        c.setOnMouseEntered(e -> c.setStyle(hv));
        c.setOnMouseExited(e -> c.setStyle(b));
    }

    /** [bg, fgColor, badgeBg, badgeBorder, hoverShadow] */
    private String[] statusColors(String s) {
        String normalized = normalizeStatus(s);

        return switch (normalized) {
            case "vendu" -> new String[]{
                    "rgba(239,68,68,0.10)",
                    "#ff9b9b",
                    "rgba(239,68,68,0.18)",
                    "rgba(239,68,68,0.35)",
                    "rgba(239,68,68,0.12)"
            };
            case "reserve" -> new String[]{
                    "rgba(245,158,11,0.10)",
                    "#fbbf24",
                    "rgba(245,158,11,0.18)",
                    "rgba(245,158,11,0.35)",
                    "rgba(245,158,11,0.12)"
            };
            default -> new String[]{
                    "rgba(34,197,94,0.10)",
                    "#7fe5a8",
                    "rgba(34,197,94,0.18)",
                    "rgba(34,197,94,0.35)",
                    "rgba(34,197,94,0.12)"
            };
        };
    }

    private String statusGradient(String s) {
        String normalized = normalizeStatus(s);

        return switch (normalized) {
            case "vendu" -> "linear-gradient(from 0% 0% to 100% 100%,#7f1d1d,#991b1b)";
            case "reserve" -> "linear-gradient(from 0% 0% to 100% 100%,#78350f,#92400e)";
            default -> "linear-gradient(from 0% 0% to 100% 100%,#14532d,#166534)";
        };
    }

    private String statLabel(String s) {
        String normalized = normalizeStatus(s);

        return switch (normalized) {
            case "vendu" -> "● Vendu";
            case "reserve" -> "● Réservé";
            default -> "● Disponible";
        };
    }

    private boolean isDisponible(String status) {
        return "disponible".equals(normalizeStatus(status));
    }

    private String statusValueForForm(String status) {
        return switch (normalizeStatus(status)) {
            case "vendu" -> "vendue";
            case "reserve" -> "réservé";
            default -> "disponible";
        };
    }

    private String statusValueForDatabase(String status) {
        return switch (normalizeStatus(status)) {
            case "vendu" -> "vendue";
            case "reserve" -> "réservé";
            default -> "disponible";
        };
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "disponible";
        }

        String value = Normalizer.normalize(status.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        if (value.startsWith("vend")) {
            return "vendu";
        }
        if (value.startsWith("reserv")) {
            return "reserve";
        }
        if (value.startsWith("dispo")) {
            return "disponible";
        }
        return "disponible";
    }

    private void alert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
