package esprit.tn.controllers;

import esprit.tn.models.evaluation;
import esprit.tn.models.formation;
import esprit.tn.services.serviceevaluation;
import esprit.tn.services.serviceformation;
import esprit.tn.utils.myconnexion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le tableau de bord (Dashboard)
 * Gère l'affichage des statistiques et les actions principales
 *
 * @author ESPRIT
 * @version 1.0
 */
public class DashboardController implements Initializable {

    // Services
    private serviceformation formationService = new serviceformation();
    private serviceevaluation evaluationService = new serviceevaluation();

    // Éléments FXML - Header
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    // Éléments FXML - Statistiques
    @FXML private VBox formationsCard;
    @FXML private VBox evaluationsCard;
    @FXML private VBox averageRatingCard;
    @FXML private Label formationsCountLabel;
    @FXML private Label evaluationsCountLabel;
    @FXML private Label averageRatingLabel;
    @FXML private Label formationsStatusLabel;
    @FXML private Label evaluationsStatusLabel;

    // Éléments FXML - Actions
    @FXML private Button addFormationBtn;
    @FXML private Button viewFormationsBtn;
    @FXML private Button addEvaluationBtn;
    @FXML private Button viewEvaluationsBtn;

    // Éléments FXML - Activités récentes
    @FXML private VBox recentActivitiesContainer;
    @FXML private Label noActivitiesLabel;

    // Éléments FXML - État système
    @FXML private HBox databaseStatusContainer;
    @FXML private Label databaseStatusLabel;
    @FXML private Label databaseStatusIcon;

    // Éléments FXML - Footer
    @FXML private Label footerLabel;
    @FXML private Button refreshBtn;

    // Root pane
    @FXML private javafx.scene.layout.BorderPane rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation du dashboard
        setupUI();
        loadStatistics();
        checkDatabaseConnection();
    }

    /**
     * Configuration initiale de l'interface
     */
    private void setupUI() {
        // Configuration des tooltips pour les boutons
        addFormationBtn.setTooltip(new Tooltip("Ajouter une nouvelle formation"));
        viewFormationsBtn.setTooltip(new Tooltip("Voir toutes les formations"));
        addEvaluationBtn.setTooltip(new Tooltip("Ajouter une nouvelle évaluation"));
        viewEvaluationsBtn.setTooltip(new Tooltip("Voir toutes les évaluations"));
        refreshBtn.setTooltip(new Tooltip("Actualiser les données"));

        // Configuration des effets hover pour les cartes
        setupCardHoverEffects();
    }

    /**
     * Configure les effets de survol pour les cartes statistiques
     */
    private void setupCardHoverEffects() {
        formationsCard.setOnMouseEntered(e -> formationsCard.setStyle(formationsCard.getStyle() + " -fx-background-color: #f8f9fa;"));
        formationsCard.setOnMouseExited(e -> formationsCard.setStyle(formationsCard.getStyle().replace(" -fx-background-color: #f8f9fa;", "")));

        evaluationsCard.setOnMouseEntered(e -> evaluationsCard.setStyle(evaluationsCard.getStyle() + " -fx-background-color: #f8f9fa;"));
        evaluationsCard.setOnMouseExited(e -> evaluationsCard.setStyle(evaluationsCard.getStyle().replace(" -fx-background-color: #f8f9fa;", "")));

        averageRatingCard.setOnMouseEntered(e -> averageRatingCard.setStyle(averageRatingCard.getStyle() + " -fx-background-color: #f8f9fa;"));
        averageRatingCard.setOnMouseExited(e -> averageRatingCard.setStyle(averageRatingCard.getStyle().replace(" -fx-background-color: #f8f9fa;", "")));
    }

    /**
     * Charge et affiche les statistiques
     */
    private void loadStatistics() {
        try {
            // Charger les formations
            List<formation> formations = formationService.getAllFormations();
            int formationsCount = formations.size();
            formationsCountLabel.setText(String.valueOf(formationsCount));
            formationsStatusLabel.setText("Total formations");

            // Charger les évaluations
            List<evaluation> evaluations = evaluationService.getAllEvaluations();
            int evaluationsCount = evaluations.size();
            evaluationsCountLabel.setText(String.valueOf(evaluationsCount));
            evaluationsStatusLabel.setText("Total évaluations");

            // Calculer la note moyenne
            double averageRating = calculateAverageRating(evaluations);
            averageRatingLabel.setText(String.format("%.1f", averageRating));

            // Mettre à jour les activités récentes
            updateRecentActivities(formations, evaluations);

        } catch (Exception e) {
            showError("Erreur lors du chargement des statistiques", e.getMessage());
        }
    }

    /**
     * Calcule la note moyenne des évaluations
     */
    private double calculateAverageRating(List<evaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (evaluation eval : evaluations) {
            sum += eval.getNote();
        }

        return sum / evaluations.size();
    }

    /**
     * Met à jour la section des activités récentes
     */
    private void updateRecentActivities(List<formation> formations, List<evaluation> evaluations) {
        recentActivitiesContainer.getChildren().clear();

        if (formations.isEmpty() && evaluations.isEmpty()) {
            recentActivitiesContainer.getChildren().add(noActivitiesLabel);
            return;
        }

        // Afficher les 5 dernières formations
        int count = 0;
        for (int i = formations.size() - 1; i >= 0 && count < 3; i--) {
            formation f = formations.get(i);
            Label activityLabel = new Label("📚 Nouvelle formation: " + f.getNom_form());
            activityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");
            recentActivitiesContainer.getChildren().add(activityLabel);
            count++;
        }

        // Afficher les 2 dernières évaluations
        count = 0;
        for (int i = evaluations.size() - 1; i >= 0 && count < 2; i--) {
            evaluation e = evaluations.get(i);
            Label activityLabel = new Label("⭐ Nouvelle évaluation: Note " + e.getNote() + "/5");
            activityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");
            recentActivitiesContainer.getChildren().add(activityLabel);
            count++;
        }
    }

    /**
     * Vérifie la connexion à la base de données
     */
    private void checkDatabaseConnection() {
        try {
            Connection conn = myconnexion.getInstance().getCnx();
            if (conn != null && !conn.isClosed()) {
                databaseStatusLabel.setText("✅ Base de données connectée");
                databaseStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                databaseStatusIcon.setText("🟢");
            } else {
                databaseStatusLabel.setText("❌ Base de données déconnectée");
                databaseStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
                databaseStatusIcon.setText("🔴");
            }
        } catch (Exception e) {
            databaseStatusLabel.setText("❌ Erreur de connexion: " + e.getMessage());
            databaseStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
            databaseStatusIcon.setText("🔴");
        }
    }

    // ========== GESTIONNAIRES D'ÉVÉNEMENTS ==========

    /**
     * Gestionnaire pour ajouter une formation
     */
    @FXML
    private void handleAddFormation() {
        try {
            // Charger le fichier FXML du formulaire d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addFormation.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter une Formation");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(rootPane.getScene().getWindow());
            dialogStage.setResizable(false);

            // Obtenir le contrôleur et lui passer la référence à la fenêtre
            AddFormationController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Afficher la fenêtre
            dialogStage.showAndWait();

            // Actualiser les statistiques après la fermeture de la fenêtre
            loadStatistics();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    /**
     * Gestionnaire pour voir les formations
     */
    @FXML
    private void handleViewFormations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/viewFormations.fxml"));
            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Liste des Formations");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(rootPane.getScene().getWindow());
            dialogStage.setResizable(true);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'afficher la liste des formations: " + e.getMessage());
        }
    }

    /**
     * Gestionnaire pour ajouter une évaluation
     */
    @FXML
    private void handleAddEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addEvaluation.fxml"));
            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouvelle Évaluation");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(rootPane.getScene().getWindow());
            dialogStage.setResizable(false);
            AddEvaluationController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            loadStatistics();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire d'évaluation: " + e.getMessage());
        }
    }

    /**
     * Gestionnaire pour voir les évaluations
     */
    @FXML
    private void handleViewEvaluations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/viewEvaluations.fxml"));
            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Résultats des Évaluations");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(rootPane.getScene().getWindow());
            dialogStage.setResizable(true);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'afficher les résultats des évaluations: " + e.getMessage());
        }
    }

    /**
     * Gestionnaire pour actualiser les données
     */
    @FXML
    private void handleRefresh() {
        loadStatistics();
        checkDatabaseConnection();
        showInfo("Actualisation", "Les données ont été mises à jour");
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Affiche une boîte de dialogue d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
