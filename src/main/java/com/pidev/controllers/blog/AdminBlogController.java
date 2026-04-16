package com.pidev.controllers.blog;

import com.pidev.entities.Publication;
import com.pidev.entities.User;
import com.pidev.services.PublicationService;
import com.pidev.tools.AuthSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminBlogController {

    @FXML private VBox feedContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private final PublicationService pubService = new PublicationService();
    private User currentUser = AuthSession.getCurrentUser();
    private javafx.animation.PauseTransition searchDebounce = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));

    @FXML
    public void initialize() {
        System.out.println("🛡️ Initialisation de l'espace MODÉRATION");

        // Populate sort options
        sortCombo.setItems(FXCollections.observableArrayList(
                "Plus récent",
                "Plus ancien",
                "Titre A → Z",
                "Titre Z → A",
                "Plus de Likes",
                "Plus de Dislikes",
                "Plus commenté"
        ));
        sortCombo.getSelectionModel().selectFirst();

        loadFeed();

        // Debounced search
        searchDebounce.setOnFinished(e -> loadFeed());
        searchField.textProperty().addListener((obs, old, nv) -> searchDebounce.playFromStart());

        // Re-sort on combo change
        sortCombo.setOnAction(e -> loadFeed());
    }

    private void loadFeed() {
        feedContainer.getChildren().clear();
        List<Publication> posts = pubService.afficher();
        String query = searchField.getText().toLowerCase().trim();
        String sortOption = sortCombo.getValue();

        // Filter
        List<Publication> filtered = posts.stream()
                .filter(p -> query.isEmpty()
                        || p.getTitre().toLowerCase().contains(query)
                        || p.getDescription().toLowerCase().contains(query))
                .collect(Collectors.toList());

        // Sort
        switch (sortOption != null ? sortOption : "Plus récent") {
            case "Plus récent":
                filtered.sort(Comparator.comparing(Publication::getDateAct).reversed());
                break;
            case "Plus ancien":
                filtered.sort(Comparator.comparing(Publication::getDateAct));
                break;
            case "Titre A → Z":
                filtered.sort(Comparator.comparing(p -> p.getTitre().toLowerCase()));
                break;
            case "Titre Z → A":
                filtered.sort(Comparator.comparing((Publication p) -> p.getTitre().toLowerCase()).reversed());
                break;
            case "Plus de Likes":
                filtered.sort(Comparator.comparingInt(Publication::getNbLikes).reversed());
                break;
            case "Plus de Dislikes":
                filtered.sort(Comparator.comparingInt(Publication::getNbDislikes).reversed());
                break;
            case "Plus commenté":
                // Sort by total engagement (likes + dislikes as proxy for comment activity)
                filtered.sort(Comparator.comparingInt((Publication p) -> p.getNbLikes() + p.getNbDislikes()).reversed());
                break;
        }

        // Render
        for (Publication pub : filtered) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/blog/AdminPublicationCard.fxml"));
                Parent card = loader.load();
                AdminPublicationCardController controller = loader.getController();
                controller.setData(pub);
                feedContainer.getChildren().add(card);
            } catch (IOException e) {
                System.err.println("❌ Erreur chargement Admin Card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void ouvrirAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/blog/PublicationForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Nouvelle Publication (ADMIN)");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadFeed(); // Refresh after closing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/pidev/AdminDashboard.fxml"));
            Stage stage = (Stage) feedContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
