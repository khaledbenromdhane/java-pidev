package esprit.tn.controllers;

import esprit.tn.models.formation;
import esprit.tn.services.serviceformation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la vue catalogue (Front) des formations.
 * Permet aux utilisateurs de consulter les formations et de passer des tests.
 */
public class FrontFormationsController implements Initializable {

    @FXML private FlowPane formationsFlowPane;
    @FXML private TextField searchField;
    @FXML private Button voirAdminBtn;

    private List<formation> allFormations;
    private final serviceformation formationService = new serviceformation();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFormations();
        setupSearch();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFormations(newValue);
        });
    }

    private void filterFormations(String query) {
        formationsFlowPane.getChildren().clear();
        String lowerQuery = query.toLowerCase();
        for (formation f : allFormations) {
            if (f.getNom_form().toLowerCase().contains(lowerQuery) || 
                f.getType().toLowerCase().contains(lowerQuery)) {
                formationsFlowPane.getChildren().add(createFormationCard(f));
            }
        }
    }

    private void loadFormations() {
        try {
            allFormations = formationService.getAllFormations();
            
            // If no formations are found, add some sample data to "fill the void"
            if (allFormations == null || allFormations.isEmpty()) {
                System.out.println("No formations found in DB, adding samples...");
                allFormations = new java.util.ArrayList<>();
                allFormations.add(new formation(1, "Développement Web Fullstack", "Technique", "Apprenez HTML, CSS, JS, et Node.js pour devenir un développeur complet.", new java.sql.Date(System.currentTimeMillis())));
                allFormations.add(new formation(2, "Design UI/UX Moderne", "Technique", "Maîtrisez Figma et les principes du design pour créer des interfaces wôw.", new java.sql.Date(System.currentTimeMillis())));
                allFormations.add(new formation(3, "Communication & Soft Skills", "Soft Skills", "Améliorez votre aisance à l'oral et votre leadership en entreprise.", new java.sql.Date(System.currentTimeMillis())));
                allFormations.add(new formation(4, "Gestion de Projet Agile", "Management", "Découvrez Scrum et Kanban pour gérer vos projets efficacement.", new java.sql.Date(System.currentTimeMillis())));
            }

            formationsFlowPane.getChildren().clear();
            for (formation f : allFormations) {
                formationsFlowPane.getChildren().add(createFormationCard(f));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement formations: " + e.getMessage());
        }
    }

    private VBox createFormationCard(formation f) {
        VBox card = new VBox(0);
        card.setPrefSize(350, 480); 
        card.getStyleClass().add("card");

        // Image Container
        VBox imageBox = new VBox();
        imageBox.setPrefHeight(200);
        imageBox.getStyleClass().add("card-image-placeholder");
        imageBox.setAlignment(Pos.CENTER);
        
        try {
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
            URL imageUrl = getClass().getResource("/images/hero_banner.png");
            if (imageUrl != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(imageUrl.toExternalForm());
                imageView.setImage(img);
                imageView.setFitHeight(200);
                imageView.setFitWidth(350);
                imageView.setPreserveRatio(false); 
                imageBox.getChildren().add(imageView);

            } else {
                Label placeholderIcon = new Label("🖼️");
                placeholderIcon.setStyle("-fx-font-size: 50px;");
                imageBox.getChildren().add(placeholderIcon);
            }
        } catch (Exception e) {
            System.err.println("Could not load card image: " + e.getMessage());
        }
        
        VBox content = new VBox(15);
        content.getStyleClass().add("card-content");
        content.setPadding(new Insets(20));
        
        String type = (f.getType() == null || f.getType().isEmpty()) ? "FORMATION" : f.getType().toUpperCase();
        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("card-type");

        String title = (f.getNom_form() == null || f.getNom_form().isEmpty()) ? "Formation Sans Nom" : f.getNom_form();
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(50);

        String desc = (f.getDescription() == null || f.getDescription().isEmpty()) ? "Aucune description disponible pour cette formation." : f.getDescription();
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("card-description");
        descLabel.setWrapText(true);
        descLabel.setMinHeight(80);
        descLabel.setMaxHeight(80);

        Button avisBtn = new Button("S'INSCRIRE & PASSER LE TEST");
        avisBtn.setMaxWidth(Double.MAX_VALUE);
        avisBtn.getStyleClass().add("btn-card-action");
        avisBtn.setCursor(javafx.scene.Cursor.HAND);
        avisBtn.setOnAction(event -> handleStartAvis(f));

        Button qrBtn = new Button("📱 QR CODE");
        qrBtn.setMaxWidth(Double.MAX_VALUE);
        qrBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d4af37; -fx-border-color: #d4af37; -fx-border-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");
        qrBtn.setOnAction(event -> handleShowQR(f));

        content.getChildren().addAll(typeLabel, titleLabel, descLabel, avisBtn, qrBtn);
        card.getChildren().addAll(imageBox, content);

        return card;
    }

    private void handleStartAvis(formation f) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addEvaluation.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Examen : " + f.getNom_form());
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            AddEvaluationController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setSelectedFormation(f); 
            
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture du test: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) voirAdminBtn.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleHome() {
        handleBack(); // Simply return to dashboard for now
    }


    private void handleShowQR(formation f) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/qrCodeDisplay.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("QR Code - " + f.getNom_form());
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            QRCodeDisplayController controller = loader.getController();
            controller.setFormationData(f);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Impossible d'afficher le QR Code : " + e.getMessage());
        }
    }
}
