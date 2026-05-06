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
    private final serviceformation formationService = new serviceformation();
    private final serviceevaluation evaluationService = new serviceevaluation();


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
    @FXML private Button viewScoresBtn;
    @FXML private Button viewEvaluationsBtn;
    @FXML private Button dashboardMenuBtn;
    @FXML private Button chatbotBtn;

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
        viewScoresBtn.setTooltip(new Tooltip("Voir les certificats et scores"));
        viewEvaluationsBtn.setTooltip(new Tooltip("Voir toutes les évaluations"));
        refreshBtn.setTooltip(new Tooltip("Actualiser les données"));

        // Configuration des effets hover pour les cartes
        setupCardHoverEffects();
    }

    /**
     * Configure les effets de survol pour les cartes statistiques
     */
    private void setupCardHoverEffects() {
        formationsCard.setOnMouseEntered(e -> formationsCard.setStyle(formationsCard.getStyle() + " -fx-background-color: #353535;"));
        formationsCard.setOnMouseExited(e -> formationsCard.setStyle(formationsCard.getStyle().replace(" -fx-background-color: #353535;", "")));

        evaluationsCard.setOnMouseEntered(e -> evaluationsCard.setStyle(evaluationsCard.getStyle() + " -fx-background-color: #353535;"));
        evaluationsCard.setOnMouseExited(e -> evaluationsCard.setStyle(evaluationsCard.getStyle().replace(" -fx-background-color: #353535;", "")));

        averageRatingCard.setOnMouseEntered(e -> averageRatingCard.setStyle(averageRatingCard.getStyle() + " -fx-background-color: #353535;"));
        averageRatingCard.setOnMouseExited(e -> averageRatingCard.setStyle(averageRatingCard.getStyle().replace(" -fx-background-color: #353535;", "")));
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

        // Add header for activities
        Label activityHeader = new Label("Dernières activités système");
        activityHeader.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");
        recentActivitiesContainer.getChildren().add(activityHeader);

        // Afficher les dernières formations avec style
        int count = 0;
        for (int i = formations.size() - 1; i >= 0 && count < 3; i--) {
            formation f = formations.get(i);
            HBox item = createActivityItem("📚", "Formation ajoutée", f.getNom_form(), "#3498db");
            recentActivitiesContainer.getChildren().add(item);
            count++;
        }

        // Afficher les dernières évaluations avec style
        count = 0;
        for (int i = evaluations.size() - 1; i >= 0 && count < 2; i--) {
            evaluation e = evaluations.get(i);
            HBox item = createActivityItem("⭐", "Nouvelle note", "Note: " + e.getNote() + "/5 - " + e.getTitre(), "#f1c40f");
            recentActivitiesContainer.getChildren().add(item);
            count++;
        }
    }

    private HBox createActivityItem(String icon, String action, String detail, String accentColor) {
        HBox container = new HBox(15);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        container.setStyle("-fx-padding: 10; -fx-background-color: #333333; -fx-background-radius: 5; -fx-border-left: 3px solid " + accentColor + ";");
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        VBox texts = new VBox(2);
        Label actionLabel = new Label(action);
        actionLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");
        
        texts.getChildren().addAll(actionLabel, detailLabel);
        container.getChildren().addAll(iconLabel, texts);
        
        return container;
    }

    /**
     * Vérifie la connexion à la base de données
     */
    private void checkDatabaseConnection() {
        try {
            Connection conn = myconnexion.getInstance().getCnx();
            if (conn != null && !conn.isClosed()) {
                databaseStatusLabel.setText("✅ Base de données connectée");
                databaseStatusLabel.setStyle("-fx-text-fill: #2ecc71;");
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

     /**
      * Gestionnaire pour le menu dashboard (reste sur le dashboard)
      */
     @FXML
     private void handleDashboardMenu() {
         loadStatistics();
         checkDatabaseConnection();
     }

    @FXML
    private void handleChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatbot.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Assistant IA - Formation Manager");
            stage.setScene(new Scene(root));
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED); 
            stage.initModality(javafx.stage.Modality.NONE);
            stage.initOwner(rootPane.getScene().getWindow());

            stage.show();
            stage.setX(rootPane.getScene().getWindow().getX() + rootPane.getScene().getWindow().getWidth() - 470);
            stage.setY(rootPane.getScene().getWindow().getY() + rootPane.getScene().getWindow().getHeight() - 650);

        } catch (IOException e) {
            showError("Erreur Chatbot", "Impossible d'ouvrir l'assistant IA : " + e.getMessage());
        }
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
