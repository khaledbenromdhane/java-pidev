package com.example.java.controllers;

import com.example.java.entities.Galerie;
import com.example.java.services.GalerieService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class GalerieController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button btnGaleries, btnOeuvres, btnFront;
    @FXML private FlowPane galerieContainer;
    @FXML private TextField searchField;
    @FXML private Label totalLabel;
    @FXML private Label heroTotalLabel;

    private boolean isFrontMode = false;
    private GalerieController parentDashboard;
    private final GalerieService service = new GalerieService();

    private static final String NAV_ACTIVE =
            "-fx-background-color:rgba(212,175,55,0.10); -fx-text-fill:#d4af37; -fx-font-weight:700;" +
                    "-fx-font-size:13px; -fx-alignment:CENTER_LEFT; -fx-padding:12 20; -fx-background-radius:0;" +
                    "-fx-cursor:HAND; -fx-border-color:#d4af37 transparent transparent transparent; -fx-border-width:0 0 0 3;";

    private static final String NAV_INACTIVE =
            "-fx-background-color:transparent; -fx-text-fill:rgba(255,255,255,0.45);" +
                    "-fx-font-size:13px; -fx-alignment:CENTER_LEFT; -fx-padding:12 20; -fx-background-radius:0; -fx-cursor:HAND;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (contentArea != null) {
            Platform.runLater(this::loadGaleriesAdminPage);
        } else if (heroTotalLabel != null) {
            isFrontMode = true;
            loadData();
        } else if (galerieContainer != null) {
            isFrontMode = false;
            loadData();
        }
    }

    @FXML
    public void loadGaleriesAdminPage() {
        if (contentArea == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/java/back/GalerieAdmin.fxml")
            );
            Parent node = loader.load();
            GalerieController ctrl = loader.getController();
            ctrl.parentDashboard = this;
            contentArea.getChildren().setAll(node);
            setActiveNav(btnGaleries);
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void loadOeuvresAdminPage() {
        loadOeuvresForGalerie(null);
    }

    public void loadOeuvresForGalerie(Galerie galerie) {
        if (contentArea == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/java/back/OeuvreAdmin.fxml")
            );
            Parent node = loader.load();
            OeuvreController ctrl = loader.getController();
            ctrl.initAdmin(galerie, this);
            contentArea.getChildren().setAll(node);
            setActiveNav(btnOeuvres);
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void openFront() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/java/front/GalerieFront.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 1100, 760);
            URL cssUrl = getClass().getResource("/com/example/java/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("🎨 ArtGalerie");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void setActiveNav(Button active) {
        for (Button b : new Button[]{btnGaleries, btnOeuvres, btnFront}) {
            if (b != null) b.setStyle(NAV_INACTIVE);
        }
        if (active != null) active.setStyle(NAV_ACTIVE);
    }

    private void loadData() {
        if (galerieContainer == null) return;

        galerieContainer.getChildren().clear();
        try {
            List<Galerie> list = service.getAllGaleries();

            if (totalLabel != null) totalLabel.setText(list.size() + " galerie(s) au total");
            if (heroTotalLabel != null) heroTotalLabel.setText("🖼️  " + list.size() + " galeries disponibles");

            if (list.isEmpty()) {
                Label e = new Label("Aucune galerie. " + (isFrontMode ? "" : "Cliquez sur '＋ Nouvelle Galerie'."));
                e.setStyle("-fx-font-size:14px; -fx-text-fill:rgba(212,175,55,0.45);");
                galerieContainer.getChildren().add(e);
            } else {
                for (Galerie g : list) {
                    galerieContainer.getChildren().add(isFrontMode ? buildFrontCard(g) : buildAdminCard(g));
                }
            }
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    private VBox buildAdminCard(Galerie g) {
        VBox card = new VBox(0);
        card.setPrefWidth(292);
        baseCardStyle(card, 14);

        VBox header = new VBox(6);
        header.setPadding(new Insets(15, 17, 13, 17));
        header.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 100%,"
                + "rgba(212,175,55,0.18),rgba(168,85,247,0.12));"
                + "-fx-background-radius:14 14 0 0;"
                + "-fx-border-color:rgba(212,175,55,0.12) transparent transparent transparent;"
                + "-fx-border-width:0 0 1 0;");

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("🖼️");
        icon.setStyle("-fx-font-size:22;");

        VBox tb = new VBox(3);
        Label nom = new Label(g.getNom());
        nom.setStyle("-fx-text-fill:#f7d777; -fx-font-size:14px; -fx-font-weight:700;");
        nom.setWrapText(true);
        nom.setMaxWidth(200);

        Label id = new Label(" #" + g.getIdGalerie() + " ");
        id.setStyle("-fx-background-color:rgba(212,175,55,0.12); -fx-text-fill:rgba(212,175,55,0.65);"
                + "-fx-font-size:10px; -fx-background-radius:6; -fx-padding:2 6;");

        tb.getChildren().addAll(nom, id);
        HBox.setHgrow(tb, Priority.ALWAYS);
        row.getChildren().addAll(icon, tb);
        header.getChildren().add(row);

        VBox body = new VBox(10);
        body.setPadding(new Insets(13, 17, 13, 17));

        Label cat = new Label("  📁  " + g.getCategorie() + "  ");
        cat.setStyle("-fx-background-color:rgba(212,175,55,0.10); -fx-text-fill:#d4af37; -fx-font-size:11px;"
                + "-fx-background-radius:20; -fx-padding:4 10; -fx-border-color:rgba(212,175,55,0.22);"
                + "-fx-border-radius:20; -fx-border-width:1;");

        HBox stats = new HBox(10);
        stats.getChildren().addAll(
                statCell("🖼️", "Œuvres", String.valueOf(g.getNbOeuvresDispo()), "#7fe5a8"),
                statCell("👥", "Employés", String.valueOf(g.getNbEmployes()), "#c8a6ff")
        );

        body.getChildren().addAll(cat, stats);

        Separator sep = goldSep();

        HBox actions = new HBox(8);
        actions.setPadding(new Insets(11, 14, 13, 14));
        actions.setAlignment(Pos.CENTER);

        Button bVoir = goldBtn("👁  Voir Œuvres");
        bVoir.setOnAction(e -> {
            GalerieController d = parentDashboard != null ? parentDashboard : this;
            if (d.contentArea != null) d.loadOeuvresForGalerie(g);
        });
        HBox.setHgrow(bVoir, Priority.ALWAYS);
        bVoir.setMaxWidth(Double.MAX_VALUE);

        Button bEdit = aBtn("✏️", "rgba(212,175,55,0.20)", "#f7d777", "rgba(212,175,55,0.35)");
        bEdit.setOnAction(e -> showDialog(g));

        Button bDel = aBtn("🗑️", "rgba(239,68,68,0.18)", "#ff9b9b", "rgba(239,68,68,0.35)");
        bDel.setOnAction(e -> deleteGalerie(g));

        actions.getChildren().addAll(bVoir, bEdit, bDel);
        card.getChildren().addAll(header, body, sep, actions);
        hoverCard(card, "rgba(212,175,55,0.12)", 14);

        return card;
    }

    private VBox buildFrontCard(Galerie g) {
        VBox card = new VBox(0);
        card.setPrefWidth(308);
        baseCardStyle(card, 16);

        StackPane img = new StackPane();
        img.setPrefHeight(155);
        img.setStyle("-fx-background-color:" + gradientFor(g.getCategorie()) + "; -fx-background-radius:16 16 0 0;");

        Label bigIcon = new Label(emojiFor(g.getCategorie()));
        bigIcon.setStyle("-fx-font-size:54; -fx-opacity:0.65;");

        Label badge = new Label("  " + g.getCategorie() + "  ");
        badge.setStyle("-fx-background-color:rgba(0,0,0,0.50); -fx-text-fill:#d4af37; -fx-font-size:10px;"
                + "-fx-background-radius:20; -fx-padding:4 10; -fx-border-color:rgba(212,175,55,0.30);"
                + "-fx-border-radius:20; -fx-border-width:1;");

        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10, 10, 0, 0));
        img.getChildren().addAll(bigIcon, badge);

        VBox body = new VBox(9);
        body.setPadding(new Insets(14, 17, 14, 17));

        Label nom = new Label(g.getNom());
        nom.setStyle("-fx-font-size:15px; -fx-font-weight:700; -fx-text-fill:#e9e9ee;");
        nom.setWrapText(true);

        HBox pills = new HBox(8);
        pills.getChildren().addAll(
                fpill("🖼️ " + g.getNbOeuvresDispo() + " œuvres"),
                fpill("👥 " + g.getNbEmployes() + " emp.")
        );

        body.getChildren().addAll(nom, pills);

        Separator sep = goldSep();

        HBox bb = new HBox();
        bb.setPadding(new Insets(0, 14, 14, 14));

        Button bv = goldBtn("👁  Voir les Œuvres");
        bv.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bv, Priority.ALWAYS);
        bv.setStyle(bv.getStyle() + "-fx-padding:10 0; -fx-background-radius:10; -fx-font-size:13px;");
        bv.setOnAction(e -> openOeuvreFront(g));

        bb.getChildren().add(bv);

        card.getChildren().addAll(img, body, sep, bb);
        hoverCard(card, "rgba(212,175,55,0.15)", 16);

        return card;
    }

    private void openOeuvreFront(Galerie galerie) {
        try {
            Scene cur = galerieContainer.getScene();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/java/front/OeuvreFront.fxml")
            );
            Parent root = loader.load();

            OeuvreController ctrl = loader.getController();
            ctrl.initFront(galerie, cur);

            Scene scene = new Scene(root, cur.getWidth(), cur.getHeight());
            URL cssUrl = getClass().getResource("/com/example/java/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            ((Stage) cur.getWindow()).setScene(scene);
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void openAddDialog() {
        showDialog(null);
    }

    private void showDialog(Galerie ex) {
        boolean edit = ex != null;
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(edit ? "✏️ Modifier la Galerie" : "➕ Nouvelle Galerie");
        dlg.setResizable(false);

        VBox root = new VBox(12);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color:#0f0f1e;");
        root.setPrefWidth(440);

        Label title = new Label(edit ? "✏️ Modifier la Galerie" : "➕ Nouvelle Galerie");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:700; -fx-text-fill:#d4af37;");

        VBox fc = new VBox(10);
        fc.setStyle("-fx-background-color:#151521; -fx-background-radius:14;"
                + "-fx-border-color:rgba(212,175,55,0.18); -fx-border-radius:14; -fx-border-width:1; -fx-padding:18;");

        TextField tfN = fld("Nom de la galerie *");
        ComboBox<String> cbC = new ComboBox<>();
        cbC.getItems().addAll("Général", "Moderne", "Classique", "Contemporain", "Abstrait", "Sculpture", "Photographie");
        cbC.setValue("Général");
        cbC.setMaxWidth(Double.MAX_VALUE);
        cbC.setStyle("-fx-background-color:rgba(255,255,255,0.05); -fx-border-color:rgba(255,255,255,0.12);"
                + "-fx-background-radius:10; -fx-border-radius:10; -fx-padding:2 8; -fx-font-size:13px;");
        TextField tfO = fld("Nombre d'œuvres disponibles");
        TextField tfE = fld("Nombre d'employés");

        if (edit) {
            tfN.setText(ex.getNom());
            if (ex.getCategorie() != null && !cbC.getItems().contains(ex.getCategorie())) {
                cbC.getItems().add(ex.getCategorie());
            }
            cbC.setValue(ex.getCategorie() != null && !ex.getCategorie().isBlank() ? ex.getCategorie() : "Général");
            tfO.setText("" + ex.getNbOeuvresDispo());
            tfE.setText("" + ex.getNbEmployes());
        }

        fc.getChildren().addAll(flbl("Nom *"), tfN, flbl("Catégorie *"), cbC,
                flbl("Nb Œuvres"), tfO, flbl("Nb Employés"), tfE);

        Label err = new Label();
        err.setStyle("-fx-text-fill:#ff9b9b; -fx-font-size:12px;");

        HBox bb = new HBox(10);
        bb.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = ghostBtn("Annuler");
        cancel.setOnAction(e -> dlg.close());

        Button save = goldBtn(edit ? "💾 Sauvegarder" : "✅ Créer la galerie");
        save.setPrefWidth(170);

        save.setOnAction(e -> {
            if (tfN.getText().trim().isEmpty()) {
                err.setText("⚠️ Nom obligatoire.");
                return;
            }
            if (cbC.getValue() == null || cbC.getValue().isBlank()) {
                err.setText("⚠️ Catégorie obligatoire.");
                return;
            }

            try {
                int nbO = tfO.getText().trim().isEmpty() ? 0 : Integer.parseInt(tfO.getText().trim());
                int nbE = tfE.getText().trim().isEmpty() ? 0 : Integer.parseInt(tfE.getText().trim());
                String cat = cbC.getValue().trim();

                if (nbO < 0 || nbE < 0) {
                    err.setText("⚠️ Les nombres doivent être positifs ou nuls.");
                    return;
                }

                if (edit) {
                    ex.setNom(tfN.getText().trim());
                    ex.setCategorie(cat);
                    ex.setNbOeuvresDispo(nbO);
                    ex.setNbEmployes(nbE);
                    service.updateGalerie(ex);
                } else {
                    service.addGalerie(new Galerie(cat, tfN.getText().trim(), nbO, nbE));
                }

                dlg.close();
                loadData();
            } catch (NumberFormatException ex2) {
                err.setText("⚠️ Valeurs numériques invalides.");
            } catch (SQLException ex2) {
                err.setText("⚠️ Erreur BD : " + ex2.getMessage());
            }
        });

        bb.getChildren().addAll(cancel, save);
        root.getChildren().addAll(title, new Separator(), fc, err, bb);

        Scene scene = new Scene(root);
        URL cssUrl = getClass().getResource("/com/example/java/styles/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        dlg.setScene(scene);
        dlg.showAndWait();
    }

    private void deleteGalerie(Galerie g) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + g.getNom() + "\" ?\n⚠️ Irréversible.",
                ButtonType.YES, ButtonType.NO);
        conf.setTitle("Confirmer");
        conf.setHeaderText(null);

        if (conf.showAndWait().filter(r -> r == ButtonType.YES).isPresent()) {
            try {
                service.deleteGalerie(g.getIdGalerie());
                loadData();
            } catch (SQLException e) {
                alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    @FXML
    public void onSearch() {
        String q = searchField != null ? searchField.getText().toLowerCase() : "";
        if (galerieContainer == null) return;

        galerieContainer.getChildren().clear();
        try {
            service.getAllGaleries().stream()
                    .filter(g -> g.getNom().toLowerCase().contains(q)
                            || g.getCategorie().toLowerCase().contains(q))
                    .forEach(g -> galerieContainer.getChildren()
                            .add(isFrontMode ? buildFrontCard(g) : buildAdminCard(g)));
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void clearSearch() {
        if (searchField != null) searchField.clear();
        loadData();
    }

    private void baseCardStyle(VBox c, int r) {
        c.setStyle("-fx-background-color:#151521; -fx-background-radius:" + r
                + "; -fx-border-color:rgba(212,175,55,0.18); -fx-border-radius:" + r
                + "; -fx-border-width:1; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.40),18,0.15,0,6);");
    }

    private VBox statCell(String em, String lb, String val, String color) {
        VBox v = new VBox(3);
        v.setPadding(new Insets(8, 14, 8, 14));
        v.setStyle("-fx-background-color:rgba(255,255,255,0.03); -fx-background-radius:8;"
                + "-fx-border-color:rgba(255,255,255,0.06); -fx-border-radius:8; -fx-border-width:1; -fx-min-width:118;");

        Label l1 = new Label(em + " " + lb);
        l1.setStyle("-fx-font-size:10px; -fx-text-fill:rgba(255,255,255,0.35);");

        Label l2 = new Label(val);
        l2.setStyle("-fx-font-size:17px; -fx-font-weight:700; -fx-text-fill:" + color + ";");

        v.getChildren().addAll(l1, l2);
        return v;
    }

    private Label fpill(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-background-color:rgba(212,175,55,0.08); -fx-text-fill:rgba(212,175,55,0.65); -fx-font-size:11px;"
                + "-fx-background-radius:20; -fx-padding:4 10; -fx-border-color:rgba(212,175,55,0.15);"
                + "-fx-border-radius:20; -fx-border-width:1;");
        return l;
    }

    private Button goldBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 100%,#d4af37,#b8860b);"
                + "-fx-text-fill:#0a0a0f; -fx-font-weight:700; -fx-background-radius:8;"
                + "-fx-padding:9 18; -fx-font-size:12px; -fx-cursor:HAND;");
        return b;
    }

    private Button ghostBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:rgba(255,255,255,0.06); -fx-text-fill:rgba(255,255,255,0.75);"
                + "-fx-background-radius:8; -fx-padding:9 14; -fx-font-size:12px;"
                + "-fx-border-color:rgba(255,255,255,0.12); -fx-border-radius:8; -fx-border-width:1; -fx-cursor:HAND;");
        return b;
    }

    private Button aBtn(String t, String bg, String fg, String bd) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; -fx-background-radius:8;"
                + "-fx-padding:8 11; -fx-font-size:13px; -fx-border-color:" + bd + ";"
                + "-fx-border-radius:8; -fx-border-width:1; -fx-cursor:HAND;");
        return b;
    }

    private TextField fld(String p) {
        TextField tf = new TextField();
        tf.setPromptText(p);
        tf.setStyle("-fx-background-color:rgba(255,255,255,0.05); -fx-border-color:rgba(255,255,255,0.12);"
                + "-fx-text-fill:#f2f2f6; -fx-prompt-text-fill:rgba(255,255,255,0.25);"
                + "-fx-background-radius:10; -fx-border-radius:10; -fx-padding:9 12; -fx-font-size:13px;");
        return tf;
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
        String b = "-fx-background-color:#151521; -fx-background-radius:" + r + "; -fx-border-color:rgba(212,175,55,0.18);"
                + "-fx-border-radius:" + r + "; -fx-border-width:1; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.40),18,0.15,0,6); -fx-translate-y:0;";
        String hv = "-fx-background-color:#151521; -fx-background-radius:" + r + "; -fx-border-color:rgba(212,175,55,0.40);"
                + "-fx-border-radius:" + r + "; -fx-border-width:1; -fx-effect:dropshadow(gaussian," + sc + ",24,0.22,0,8); -fx-translate-y:-3;";

        c.setOnMouseEntered(e -> c.setStyle(hv));
        c.setOnMouseExited(e -> c.setStyle(b));
    }

    private String gradientFor(String cat) {
        if (cat == null) return "linear-gradient(from 0% 0% to 100% 100%,#1a0a2e,#2d1b69)";
        return switch (cat.toLowerCase()) {
            case "moderne" -> "linear-gradient(from 0% 0% to 100% 100%,#0f0f1e,#1e1b4b)";
            case "contemporain" -> "linear-gradient(from 0% 0% to 100% 100%,#052e16,#064e3b)";
            case "classique" -> "linear-gradient(from 0% 0% to 100% 100%,#1c0a00,#451a03)";
            case "abstrait" -> "linear-gradient(from 0% 0% to 100% 100%,#2d0a1e,#5b0a38)";
            case "sculpture" -> "linear-gradient(from 0% 0% to 100% 100%,#0a1628,#1e3a5f)";
            default -> "linear-gradient(from 0% 0% to 100% 100%,#1a0a2e,#2d1b69)";
        };
    }

    private String emojiFor(String cat) {
        if (cat == null) return "🖼️";
        return switch (cat.toLowerCase()) {
            case "sculpture" -> "🗿";
            case "contemporain" -> "✨";
            case "classique" -> "🏛️";
            case "abstrait" -> "🎭";
            case "photographie" -> "📷";
            default -> "🖼️";
        };
    }

    private void alert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
