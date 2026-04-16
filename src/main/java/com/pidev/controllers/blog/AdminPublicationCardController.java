package com.pidev.controllers.blog;

import com.pidev.entities.*;
import com.pidev.services.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller specialized for ADMIN moderation of publications.
 * Contains logic only accessible to administrators.
 */
public class AdminPublicationCardController {

    @FXML private VBox cardContainer;
    @FXML private Label usernameLabel;
    @FXML private Label avatarInitial;
    @FXML private Label titreLabel;
    @FXML private Label dateLabel;
    @FXML private Text descriptionText;
    @FXML private ImageView publicationImage;
    @FXML private Label statsLabel;
    @FXML private VBox commentsContainer;
    @FXML private VBox adminCommentsSection;

    private Publication publication;
    private final PublicationService pubService = new PublicationService();
    private final CommentaireService comService = new CommentaireService();
    private final CrudService userService = new CrudService();

    public void setData(Publication pub) {
        this.publication = pub;
        titreLabel.setText(pub.getTitre());
        dateLabel.setText(pub.getDateAct().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        descriptionText.setText(pub.getDescription());
        
        User poster = userService.afficherParId(pub.getIdUser());
        String displayInfo = (poster != null) ? poster.getEmail() : "Utilisateur Inconnu";
        usernameLabel.setText(displayInfo);
        avatarInitial.setText(displayInfo.substring(0, 1).toUpperCase());
        
        statsLabel.setText("Performance: " + pub.getNbLikes() + " 👍 | " + pub.getNbDislikes() + " 👎");

        if (pub.getImage() != null && !pub.getImage().isEmpty()) {
            try {
                publicationImage.setImage(new Image(pub.getImage()));
            } catch (Exception e) {
                publicationImage.setManaged(false);
                publicationImage.setVisible(false);
            }
        } else {
            publicationImage.setManaged(false);
            publicationImage.setVisible(false);
        }
        
        loadAdminComments();
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette publication définitivement ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                pubService.supprimer(publication.getIdPublication());
                // Remove from parent container
                ((VBox)cardContainer.getParent()).getChildren().remove(cardContainer);
            }
        });
    }

    @FXML
    private void handleBanAuthor() {
        User author = userService.afficherParId(publication.getIdUser());
        if (author == null) return;

        TextInputDialog dialog = new TextInputDialog("Violation des règles communautaires");
        dialog.setTitle("Bannissement de l'auteur");
        dialog.setHeaderText("Action de Modération sur " + author.getEmail());
        dialog.setContentText("Raison du signalement :");
        
        dialog.showAndWait().ifPresent(reason -> {
            author.setEst_signale(1);
            author.setRaison_signalement(reason);
            userService.modifier(author);
        });
    }

    @FXML
    private void handleEdit() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/pidev/blog/PublicationForm.fxml"));
            javafx.scene.Parent root = loader.load();
            
            PublicationFormController controller = loader.getController();
            controller.setPublication(publication);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("MODÉRATION: Modifier Publication");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            
            // Refresh this card
            setData(pubService.afficherParId(publication.getIdPublication()));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleComments() {
        boolean isVisible = adminCommentsSection.isVisible();
        adminCommentsSection.setVisible(!isVisible);
        adminCommentsSection.setManaged(!isVisible);
    }

    private void loadAdminComments() {
        commentsContainer.getChildren().clear();
        List<Commentaire> comments = comService.afficherParPublication(publication.getIdPublication());
        
        for (Commentaire c : comments) {
            VBox comBox = new VBox(5);
            comBox.getStyleClass().add("comment-card");
            if (c.isEstSignale()) comBox.setStyle("-fx-border-color: #ff6b6b;");

            Label authorLabel = new Label(userService.afficherParId(c.getIdUser()).getEmail());
            authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-gold;");
            
            Label content = new Label(c.getContent());
            content.setWrapText(true);
            content.setStyle("-fx-text-fill: white;");

            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_RIGHT);
            
            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-text-fill: #ff6b6b; -fx-background-color: transparent; -fx-border-color: #ff6b6b; -fx-font-size: 10;");
            deleteBtn.setOnAction(e -> {
                comService.supprimer(c.getIdCommentaire());
                loadAdminComments();
            });

            if (!c.isEstSignale()) {
                Button hideBtn = new Button("Masquer");
                hideBtn.setStyle("-fx-text-fill: grey; -fx-background-color: transparent; -fx-border-color: grey; -fx-font-size: 10;");
                hideBtn.setOnAction(e -> {
                    c.setEstSignale(true);
                    c.setRaisonSignalement("Modéré par l'administrateur");
                    comService.modifier(c);
                    loadAdminComments();
                });
                actions.getChildren().add(hideBtn);
            } else {
                Label bannedLabel = new Label("🚫 SIGNALÉ");
                bannedLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                actions.getChildren().add(bannedLabel);
            }

            actions.getChildren().add(deleteBtn);
            comBox.getChildren().addAll(authorLabel, content, actions);
            commentsContainer.getChildren().add(comBox);
        }
    }
}
