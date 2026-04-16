package com.pidev.controllers.blog;

import com.pidev.entities.*;
import com.pidev.services.*;
import com.pidev.tools.AuthSession;
import com.pidev.tools.BadWordsFilter;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller specialized for standard USER interaction.
 * Handles social features like posting comments, likes, and personal management.
 */
public class PublicationCardController {

    @FXML private VBox cardContainer;
    @FXML private Label usernameLabel;
    @FXML private Label avatarInitial;
    @FXML private Label titreLabel;
    @FXML private Label dateLabel;
    @FXML private Text descriptionText;
    @FXML private ImageView publicationImage;
    @FXML private Button likeBtn;
    @FXML private Button dislikeBtn;
    @FXML private Label commentCountLabel;
    @FXML private VBox commentsContainer;
    @FXML private TextField commentInput;
    @FXML private Button sendCommentBtn;
    @FXML private Label commentError;
    @FXML private MenuButton optionsBtn;
    @FXML private MenuItem modifierItem;
    @FXML private MenuItem supprimerItem;

    private Publication publication;
    private final PublicationService pubService = new PublicationService();
    private final CommentaireService comService = new CommentaireService();
    private final CrudService userService = new CrudService();
    private final PublicationReactionService reactionService = new PublicationReactionService();
    private User currentUser = AuthSession.getCurrentUser();

    @FXML
    public void initialize() {
        if (commentInput != null && commentError != null) {
            commentInput.textProperty().addListener((obs, oldVal, newVal) -> hideCommentError());
        }
    }

    public void setData(Publication pub) {
        this.publication = pub;
        titreLabel.setText(pub.getTitre());
        dateLabel.setText(pub.getDateAct().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        descriptionText.setText(pub.getDescription());
        
        User poster = userService.afficherParId(pub.getIdUser());
        String displayInfo = (poster != null) ? poster.getEmail() : "Inconnu";
        usernameLabel.setText(displayInfo);
        avatarInitial.setText(displayInfo.substring(0, 1).toUpperCase());
        
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

        // Standard User Options: Only show for owner
        if (currentUser != null && pub.getIdUser() == currentUser.getId_user()) {
            optionsBtn.setVisible(true);
        } else {
            optionsBtn.setVisible(false);
        }

        updateReactions();
        loadComments();
        hideCommentError();

        likeBtn.setOnAction(e -> handleReaction(true));
        dislikeBtn.setOnAction(e -> handleReaction(false));
        sendCommentBtn.setOnAction(e -> handleAddComment());
        
        modifierItem.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/pidev/blog/PublicationForm.fxml"));
                javafx.scene.Parent root = loader.load();
                
                PublicationFormController controller = loader.getController();
                controller.setPublication(publication);
                
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.setTitle("Modifier ma Publication");
                stage.setScene(new javafx.scene.Scene(root));
                stage.showAndWait();
                
                // Refresh data
                setData(pubService.afficherParId(publication.getIdPublication()));
            } catch (java.io.IOException ev) {
                ev.printStackTrace();
            }
        });

        supprimerItem.setOnAction(e -> {
            pubService.supprimer(publication.getIdPublication());
            ((VBox)cardContainer.getParent()).getChildren().remove(cardContainer);
        });
    }

    private void handleReaction(boolean isLike) {
        if (currentUser == null) return;
        
        PublicationReaction existing = reactionService.verifierReaction(currentUser.getId_user(), publication.getIdPublication());
        if (existing != null) {
            if (existing.isLike() == isLike) {
                reactionService.supprimer(existing.getIdReaction());
            } else {
                existing.setLike(isLike);
                reactionService.modifier(existing);
            }
        } else {
            PublicationReaction pr = new PublicationReaction(0, currentUser.getId_user(), publication.getIdPublication(), isLike);
            reactionService.ajouter(pr);
        }
        
        // Fetch fresh data from DB after service update
        this.publication = pubService.afficherParId(publication.getIdPublication());
        updateReactions();
    }

    private void updateReactions() {
        likeBtn.setText("👍 " + publication.getNbLikes());
        dislikeBtn.setText("👎 " + publication.getNbDislikes());
        
        // Highlight active reaction
        if (currentUser != null) {
            PublicationReaction pr = reactionService.verifierReaction(currentUser.getId_user(), publication.getIdPublication());
            if (pr != null) {
                if (pr.isLike()) {
                    likeBtn.setStyle("-fx-background-color: rgba(212,175,55,0.2); -fx-text-fill: -fx-gold; -fx-border-color: -fx-gold;");
                    dislikeBtn.setStyle("");
                } else {
                    dislikeBtn.setStyle("-fx-background-color: rgba(255,107,107,0.2); -fx-text-fill: #ff6b6b; -fx-border-color: #ff6b6b;");
                    likeBtn.setStyle("");
                }
            } else {
                likeBtn.setStyle("");
                dislikeBtn.setStyle("");
            }
        }
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        List<Commentaire> comments = comService.afficherParPublication(publication.getIdPublication());
        commentCountLabel.setText(comments.size() + " Commentaires");
        
        for (Commentaire c : comments) {
            // Respect Moderation: Hidden if signaled
            if (c.isEstSignale()) continue;

            VBox comBox = new VBox(5);
            comBox.getStyleClass().add("comment-card");
            
            User commentAuthor = userService.afficherParId(c.getIdUser());
            Label userLabel = new Label(commentAuthor != null ? commentAuthor.getEmail() : "Utilisateur");
            userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-gold;");
            
            Label contentLabel = new Label(c.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setStyle("-fx-text-fill: white;");

            comBox.getChildren().addAll(userLabel, contentLabel);

            // ─── USER CONTROLS (Only for owner) ─────────────────────────────────────
            if (currentUser != null && c.getIdUser() == currentUser.getId_user()) {
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);
                
                Button editBtn = new Button("Modifier");
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -fx-gold; -fx-font-size: 10; -fx-cursor: hand;");
                
                Button delBtn = new Button("Supprimer");
                delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-font-size: 10; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    TextField editField = new TextField(c.getContent());
                    editField.getStyleClass().add("blog-input");
                    editField.setStyle("-fx-font-size: 11;");
                    
                    Button saveEdit = new Button("💾");
                    saveEdit.setStyle("-fx-background-color: -fx-gold; -fx-text-fill: black; -fx-background-radius: 5;");
                    
                    HBox editBox = new HBox(5, editField, saveEdit);
                    HBox.setHgrow(editField, Priority.ALWAYS);
                    
                    int idx = comBox.getChildren().indexOf(contentLabel);
                    comBox.getChildren().set(idx, editBox);
                    actions.setVisible(false);
                    
                    saveEdit.setOnAction(ev -> {
                        String freshText = editField.getText().trim();
                        if (freshText.isEmpty()) {
                            return;
                        }
                        if (BadWordsFilter.containsBadWords(freshText)) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Commentaire refusé");
                            alert.setHeaderText(null);
                            alert.setContentText("Votre commentaire contient des mots inappropriés. Veuillez reformuler.");
                            alert.showAndWait();
                            return;
                        }
                        c.setContent(freshText);
                        comService.modifier(c);
                        loadComments();
                    });
                });

                delBtn.setOnAction(e -> {
                    comService.supprimer(c.getIdCommentaire());
                    loadComments();
                });

                actions.getChildren().addAll(editBtn, delBtn);
                comBox.getChildren().add(actions);
            }
            // ─────────────────────────────────────────────────────────────────────────

            commentsContainer.getChildren().add(comBox);
        }
    }

    private void handleAddComment() {
        String content = commentInput.getText().trim();
        if (currentUser == null || content.isEmpty()) return;

        if (BadWordsFilter.containsBadWords(content)) {
            showCommentError("Votre commentaire contient des mots inappropriés. Veuillez reformuler.");
            return;
        }
        hideCommentError();

        Commentaire c = new Commentaire();
        c.setIdUser(currentUser.getId_user());
        c.setIdPublication(publication.getIdPublication());
        c.setContent(content);
        c.setStatus("visible");
        
        comService.ajouter(c);
        commentInput.clear();
        loadComments();
    }

    private void showCommentError(String message) {
        commentError.setText("⚠️ " + message);
        commentError.setVisible(true);
        commentError.setManaged(true);
    }

    private void hideCommentError() {
        commentError.setText("");
        commentError.setVisible(false);
        commentError.setManaged(false);
    }
}
