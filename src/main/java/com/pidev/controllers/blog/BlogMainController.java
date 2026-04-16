package com.pidev.controllers.blog;

import com.pidev.entities.Publication;
import com.pidev.entities.User;
import com.pidev.services.PublicationService;
import com.pidev.tools.AuthSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BlogMainController {

    @FXML private VBox feedContainer;
    @FXML private HBox adminBanner;
    @FXML private Button allPostsBtn;
    @FXML private Button myPostsBtn;
    @FXML private Button addPostBtn;
    @FXML private TextField searchField;

    private final PublicationService pubService = new PublicationService();
    private User currentUser = AuthSession.getCurrentUser();

    private javafx.animation.PauseTransition searchDebounce = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));

    @FXML
    public void initialize() {
        // Show admin banner if applicable
        if (currentUser != null && ("admin".equalsIgnoreCase(currentUser.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
            adminBanner.setVisible(true);
            adminBanner.setManaged(true);
        }

        // Initial load
        loadFeed(false);
        
        allPostsBtn.setOnAction(e -> loadFeed(false));
        myPostsBtn.setOnAction(e -> loadFeed(true));
        addPostBtn.setOnAction(e -> handleAddPost());
        
        searchDebounce.setOnFinished(e -> loadFeed(false));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDebounce.playFromStart();
        });
    }

    private void loadFeed(boolean myPostsOnly) {
        feedContainer.getChildren().removeIf(node -> node != adminBanner);
        List<Publication> posts = pubService.afficher();
        User user = AuthSession.getCurrentUser();
        String query = searchField.getText().toLowerCase().trim();

        for (Publication pub : posts) {
            // Filter by ownership if requested
            if (myPostsOnly && (user == null || pub.getIdUser() != user.getId_user())) continue;
            
            // Filter by search query
            if (!query.isEmpty()) {
                boolean matches = pub.getTitre().toLowerCase().contains(query) || 
                                pub.getDescription().toLowerCase().contains(query);
                if (!matches) continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/blog/PublicationCard.fxml"));
                Parent card = loader.load();
                PublicationCardController controller = loader.getController();
                controller.setData(pub);
                feedContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAddPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/blog/PublicationForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouvelle Publication");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadFeed(false); // Refresh after closing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
