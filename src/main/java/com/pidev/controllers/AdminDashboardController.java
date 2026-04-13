package com.pidev.controllers;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.pidev.SessionManager;
import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    // ── Tableau ───────────────────────────────────────────────────────────────────
    @FXML private TableView<User>            tableUser;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colPrenom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colTelephone;
    @FXML private TableColumn<User, String>  colRole;

    // ── Outils ───────────────────────────────────────────────────────────────────
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbTri;
    @FXML private Label            lblCompteur;

    // ── Stats ─────────────────────────────────────────────────────────────────────
    @FXML private Label    lblBienvenue;
    @FXML private Label    lblDate;
    @FXML private Label    lblAdminNom;
    @FXML private Label    lblStatTotal;
    @FXML private Label    lblStatAdmin;
    @FXML private Label    lblStatUser;
    @FXML private Label    lblStatAvecTel;
    @FXML private Label    lblStatGmail;

    // ── Pourcentages ──────────────────────────────────────────────────────────────
    @FXML private Label    lblPctAdmin;
    @FXML private Label    lblPctUser;
    @FXML private Label    lblPctTel;
    @FXML private Label    lblPctGmail;

    // ── Barres ────────────────────────────────────────────────────────────────────
    @FXML private Region   barAdmin;
    @FXML private Region   barUser;
    @FXML private Region   barTel;
    @FXML private Region   barGmail;

    // ── PieChart ──────────────────────────────────────────────────────────────────
    @FXML private PieChart pieChart;

    private final CrudService service = new CrudService();
    private List<User> tousLesUsers;
    private static final double BAR_MAX_WIDTH = 350.0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        User admin = SessionManager.getInstance().getUserConnecte();
        if (admin != null) {
            lblAdminNom.setText(admin.getNom() + " " + admin.getPrenom());
            lblBienvenue.setText("Tableau de bord — " + admin.getNom());
        }

        lblDate.setText("Aujourd'hui : " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        cbTri.getItems().addAll("ID (croissant)", "Nom (A-Z)", "Nom (Z-A)",
                "Prenom (A-Z)", "Role (ADMIN en premier)");
        cbTri.setValue("ID (croissant)");

        chargerDonnees();

        tfRecherche.textProperty().addListener((obs, o, n) -> filtrerEtTrier());
        cbTri.valueProperty().addListener((obs, o, n) -> filtrerEtTrier());
    }

    // ── Charger ───────────────────────────────────────────────────────────────────
    private void chargerDonnees() {
        tousLesUsers = service.afficher();
        majStats();
        majPieChart();
        filtrerEtTrier();
    }

    // ── Stats + Barres ────────────────────────────────────────────────────────────
    private void majStats() {
        long total   = tousLesUsers.size();
        long admins  = tousLesUsers.stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).count();
        long simples = tousLesUsers.stream()
                .filter(u -> "USER".equalsIgnoreCase(u.getRole())).count();
        long avecTel = tousLesUsers.stream()
                .filter(u -> u.getTelephone() != null && !u.getTelephone().isEmpty()).count();
        long gmail   = tousLesUsers.stream()
                .filter(u -> u.getEmail() != null &&
                        u.getEmail().toLowerCase().contains("@gmail")).count();

        lblStatTotal.setText(String.valueOf(total));
        lblStatAdmin.setText(String.valueOf(admins));
        lblStatUser.setText(String.valueOf(simples));
        lblStatAvecTel.setText(String.valueOf(avecTel));
        lblStatGmail.setText(String.valueOf(gmail));

        if (total == 0) return;

        double pctAdmin = (admins  * 100.0) / total;
        double pctUser  = (simples * 100.0) / total;
        double pctTel   = (avecTel * 100.0) / total;
        double pctGmail = (gmail   * 100.0) / total;

        lblPctAdmin.setText(String.format("%.1f%%", pctAdmin));
        lblPctUser.setText(String.format("%.1f%%", pctUser));
        lblPctTel.setText(String.format("%.1f%%", pctTel));
        lblPctGmail.setText(String.format("%.1f%%", pctGmail));

        barAdmin.setPrefWidth((pctAdmin / 100.0) * BAR_MAX_WIDTH);
        barUser.setPrefWidth((pctUser   / 100.0) * BAR_MAX_WIDTH);
        barTel.setPrefWidth((pctTel     / 100.0) * BAR_MAX_WIDTH);
        barGmail.setPrefWidth((pctGmail / 100.0) * BAR_MAX_WIDTH);
    }

    // ── PieChart ──────────────────────────────────────────────────────────────────
    private void majPieChart() {
        long total   = tousLesUsers.size();
        long admins  = tousLesUsers.stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).count();
        long simples = tousLesUsers.stream()
                .filter(u -> "USER".equalsIgnoreCase(u.getRole())).count();
        long avecTel = tousLesUsers.stream()
                .filter(u -> u.getTelephone() != null && !u.getTelephone().isEmpty()).count();
        long gmail   = tousLesUsers.stream()
                .filter(u -> u.getEmail() != null &&
                        u.getEmail().toLowerCase().contains("@gmail")).count();

        pieChart.getData().clear();

        if (total == 0) return;

        // Créer les tranches avec pourcentage dans le label
        PieChart.Data sliceAdmin = new PieChart.Data(
                String.format("Admins\n%.1f%%", (admins * 100.0) / total), admins);
        PieChart.Data sliceUser = new PieChart.Data(
                String.format("Users\n%.1f%%", (simples * 100.0) / total), simples);
        PieChart.Data sliceTel = new PieChart.Data(
                String.format("Avec Tel\n%.1f%%", (avecTel * 100.0) / total), avecTel);
        PieChart.Data sliceGmail = new PieChart.Data(
                String.format("Gmail\n%.1f%%", (gmail * 100.0) / total), gmail);

        pieChart.getData().addAll(sliceAdmin, sliceUser, sliceTel, sliceGmail);

        // Couleurs des tranches
        pieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #f59e0b;"); // Admin  → orange
        pieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #1a56db;"); // User   → bleu
        pieChart.getData().get(2).getNode().setStyle("-fx-pie-color: #8b5cf6;"); // Tel    → violet
        pieChart.getData().get(3).getNode().setStyle("-fx-pie-color: #ef4444;"); // Gmail  → rouge

        pieChart.setTitle("Repartition des utilisateurs");
    }

    // ── Filtrer + Trier ───────────────────────────────────────────────────────────
    private void filtrerEtTrier() {
        String recherche = tfRecherche.getText().trim().toLowerCase();
        List<User> filtre = tousLesUsers.stream()
                .filter(u -> u.getNom().toLowerCase().contains(recherche)
                        || u.getPrenom().toLowerCase().contains(recherche)
                        || u.getEmail().toLowerCase().contains(recherche)
                        || u.getRole().toLowerCase().contains(recherche))
                .collect(Collectors.toList());

        switch (Objects.requireNonNullElse(cbTri.getValue(), "")) {
            case "Nom (A-Z)"               -> filtre.sort(Comparator.comparing(User::getNom));
            case "Nom (Z-A)"               -> filtre.sort(Comparator.comparing(User::getNom).reversed());
            case "Prenom (A-Z)"            -> filtre.sort(Comparator.comparing(User::getPrenom));
            case "Role (ADMIN en premier)" -> filtre.sort(Comparator.comparing(User::getRole));
            default                         -> filtre.sort(Comparator.comparing(User::getId_user));
        }

        tableUser.setItems(FXCollections.observableArrayList(filtre));
        lblCompteur.setText(filtre.size() + " utilisateur(s) trouve(s)");
    }

    // ── Export PDF ────────────────────────────────────────────────────────────────
    @FXML
    private void exporterPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.setInitialFileName("rapport_utilisateurs.pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(tableUser.getScene().getWindow());
        if (file == null) return;

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font fontTitre     = new Font(Font.HELVETICA, 22, Font.BOLD,   new Color(26, 86, 219));
            Font fontSousTitre = new Font(Font.HELVETICA, 11, Font.NORMAL,  new Color(100, 116, 139));
            Font fontSection   = new Font(Font.HELVETICA, 13, Font.BOLD,   new Color(15, 23, 42));
            Font fontNormal    = new Font(Font.HELVETICA, 10, Font.NORMAL,  new Color(55, 65, 81));
            Font fontBlanc     = new Font(Font.HELVETICA, 10, Font.BOLD,   Color.WHITE);

            Paragraph titre = new Paragraph("PIDEV - Rapport Utilisateurs", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);

            Paragraph date = new Paragraph("Genere le : " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    "   |   Admin : " +
                    SessionManager.getInstance().getUserConnecte().getNom(), fontSousTitre);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Stats
            long total   = tousLesUsers.size();
            long admins  = tousLesUsers.stream()
                    .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).count();
            long simples = tousLesUsers.stream()
                    .filter(u -> "USER".equalsIgnoreCase(u.getRole())).count();
            long avecTel = tousLesUsers.stream()
                    .filter(u -> u.getTelephone() != null && !u.getTelephone().isEmpty()).count();
            long gmail   = tousLesUsers.stream()
                    .filter(u -> u.getEmail() != null &&
                            u.getEmail().toLowerCase().contains("@gmail")).count();

            document.add(new Paragraph("Statistiques generales", fontSection));

            PdfPTable tableStats = new PdfPTable(5);
            tableStats.setWidthPercentage(100);
            tableStats.setSpacingAfter(15);

            String[] statsLabels = {"Total", "Admins", "Utilisateurs", "Avec Tel", "Gmail"};
            long[]   statsVals   = {total, admins, simples, avecTel, gmail};
            Color[]  statsColors = {
                    new Color(26, 86, 219), new Color(245, 158, 11),
                    new Color(34, 197, 94), new Color(139, 92, 246), new Color(239, 68, 68)
            };

            for (int i = 0; i < statsLabels.length; i++) {
                PdfPCell cell = new PdfPCell();
                cell.setBackgroundColor(statsColors[i]);
                cell.setPadding(12);
                cell.setBorder(0);
                String pct = total > 0 ?
                        String.format("%.1f%%", statsVals[i] * 100.0 / total) : "0%";
                Paragraph val = new Paragraph(statsVals[i] + " (" + pct + ")", fontBlanc);
                val.setAlignment(Element.ALIGN_CENTER);
                Paragraph lab = new Paragraph(statsLabels[i],
                        new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE));
                lab.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(val);
                cell.addElement(lab);
                tableStats.addCell(cell);
            }
            document.add(tableStats);

            // Tableau utilisateurs
            document.add(new Paragraph("Liste des utilisateurs", fontSection));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 1.5f, 1.5f, 2.5f, 1.5f, 1f});
            table.setSpacingBefore(10);

            for (String h : new String[]{"ID", "Nom", "Prenom", "Email", "Telephone", "Role"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontBlanc));
                cell.setBackgroundColor(new Color(15, 23, 42));
                cell.setPadding(8);
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            boolean pair = false;
            for (User u : tableUser.getItems()) {
                Color bgRow = pair ? new Color(248, 250, 255) : Color.WHITE;
                for (String data : new String[]{
                        String.valueOf(u.getId_user()), u.getNom(), u.getPrenom(),
                        u.getEmail(),
                        u.getTelephone() != null ? u.getTelephone() : "-",
                        u.getRole()}) {
                    PdfPCell cell = new PdfPCell(new Phrase(data, fontNormal));
                    cell.setBackgroundColor(bgRow);
                    cell.setPadding(7);
                    cell.setBorder(Rectangle.BOTTOM);
                    cell.setBorderColor(new Color(226, 232, 240));
                    table.addCell(cell);
                }
                pair = !pair;
            }
            document.add(table);

            Paragraph footer = new Paragraph(
                    "\nTotal exporte : " + tableUser.getItems().size() + " utilisateur(s).",
                    fontSousTitre);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);
            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export PDF reussi");
            alert.setHeaderText(null);
            alert.setContentText("PDF genere !\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur PDF");
            alert.setContentText("Erreur : " + e.getMessage());
            alert.showAndWait();
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────────
    @FXML public void actualiser()     { chargerDonnees(); }
    @FXML private void afficherStats() { chargerDonnees(); }
    @FXML private void afficherUsers() { chargerDonnees(); }
    @FXML private void ouvrirAjouter() { ouvrirFormulaire(null); }

    @FXML private void ouvrirModifier() {
        User sel = tableUser.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherAlerte("Selectionnez un utilisateur."); return; }
        ouvrirFormulaire(sel);
    }

    private void ouvrirFormulaire(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/pidev/UserForm.fxml"));
            Parent root = loader.load();
            UserFormController ctrl = loader.getController();
            ctrl.setMode(user == null ? "AJOUTER" : "MODIFIER", user, null);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> chargerDonnees());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void supprimer() {
        User sel = tableUser.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherAlerte("Selectionnez un utilisateur."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer " + sel.getNom() + " " + sel.getPrenom() + " ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) { service.supprimer(sel.getId_user()); chargerDonnees(); }
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────────────
    @FXML private void allerProfil()   { naviguerVers("/com/pidev/Profil.fxml", "Mon Profil", 1100, 700); }
    @FXML private void retourAccueil() { naviguerVers("/com/pidev/UserHome.fxml", "Espace Utilisateur", 1100, 700); }

    @FXML private void deconnecter() {
        SessionManager.getInstance().deconnecter();
        naviguerVers("/com/pidev/Login.fxml", "Connexion", 420, 420);
    }

    private void naviguerVers(String fxml, String titre, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tableUser.getScene().getWindow();
            stage.setTitle(titre);
            stage.setScene(new Scene(root, w, h));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void afficherAlerte(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setContentText(msg);
        a.showAndWait();
    }
}
