package com.pidev.controllers.blog;

import com.pidev.entities.Commentaire;
import com.pidev.entities.Publication;
import com.pidev.entities.PublicationReaction;
import com.pidev.entities.User;
import com.pidev.services.CommentaireService;
import com.pidev.services.CrudService;
import com.pidev.services.LocalWebServerService;
import com.pidev.services.PublicationReactionService;
import com.pidev.services.PublicationService;
import com.pidev.tools.AuthSession;
import com.pidev.tools.BadWordsFilter;
import com.pidev.tools.GeminiVisionUtil;
import com.pidev.tools.NotificationUtil;
import com.pidev.tools.QRCodeGenerator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    @FXML private Button showQrBtn;
    @FXML private Button translateAllBtn;
    @FXML private Button emojiSmileBtn;
    @FXML private Button emojiHeartBtn;
    @FXML private Button emojiFireBtn;
    @FXML private Button emojiSadBtn;

    private Publication publication;
    private final PublicationService pubService = new PublicationService();
    private final CommentaireService comService = new CommentaireService();
    private final CrudService userService = new CrudService();
    private final PublicationReactionService reactionService = new PublicationReactionService();
    private final User currentUser = AuthSession.getCurrentUser();
    private Image publicationQrImage;
    private String publicationQrUrl;

    @FXML
    public void initialize() {
        if (commentInput != null && commentError != null) {
            commentInput.textProperty().addListener((obs, oldVal, newVal) -> hideCommentError());
        }

        setEmojiButtonArt(emojiSmileBtn, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f600.png", "\uD83D\uDE00");
        setEmojiButtonArt(emojiHeartBtn, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/2764.png", "\u2764\uFE0F");
        setEmojiButtonArt(emojiFireBtn, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f525.png", "\uD83D\uDD25");
        setEmojiButtonArt(emojiSadBtn, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f622.png", "\uD83D\uDE22");

        if (emojiSmileBtn != null) emojiSmileBtn.setOnAction(e -> appendEmoji("\uD83D\uDE00"));
        if (emojiHeartBtn != null) emojiHeartBtn.setOnAction(e -> appendEmoji("\u2764\uFE0F"));
        if (emojiFireBtn != null) emojiFireBtn.setOnAction(e -> appendEmoji("\uD83D\uDD25"));
        if (emojiSadBtn != null) emojiSadBtn.setOnAction(e -> appendEmoji("\uD83D\uDE22"));
    }

    public void setData(Publication pub) {
        this.publication = pub;
        titreLabel.setText(pub.getTitre());
        dateLabel.setText(pub.getDateAct().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        descriptionText.setText(pub.getDescription());

        User poster = userService.afficherParId(pub.getIdUser());
        String displayInfo = poster != null ? poster.getEmail() : "Inconnu";
        usernameLabel.setText(displayInfo);
        avatarInitial.setText(displayInfo.substring(0, 1).toUpperCase());

        if (pub.getImage() != null && !pub.getImage().isEmpty()) {
            try {
                publicationImage.setImage(new Image(pub.getImage()));
                publicationImage.setManaged(true);
                publicationImage.setVisible(true);
            } catch (Exception e) {
                publicationImage.setManaged(false);
                publicationImage.setVisible(false);
            }
        } else {
            publicationImage.setManaged(false);
            publicationImage.setVisible(false);
        }

        optionsBtn.setVisible(currentUser != null && pub.getIdUser() == currentUser.getId_user());

        publicationQrUrl = LocalWebServerService.getInstance().getPublicationUrl(pub.getIdPublication());
        publicationQrImage = QRCodeGenerator.generateQRCode(publicationQrUrl, 320, 320);

        if (showQrBtn != null) showQrBtn.setOnAction(e -> showQrPopup());
        if (translateAllBtn != null) translateAllBtn.setOnAction(e -> handleTranslateAll());

        updateReactions();
        loadComments();
        hideCommentError();

        likeBtn.setOnAction(e -> handleReaction(true));
        dislikeBtn.setOnAction(e -> handleReaction(false));
        sendCommentBtn.setOnAction(e -> handleAddComment());

        modifierItem.setOnAction(e -> handleEditPublication());
        supprimerItem.setOnAction(e -> {
            pubService.supprimer(publication.getIdPublication());
            ((VBox) cardContainer.getParent()).getChildren().remove(cardContainer);
        });
    }

    private void setEmojiButtonArt(Button button, String imageUrl, String fallbackEmoji) {
        if (button == null) return;
        button.setText(fallbackEmoji);
        button.setStyle("-fx-background-color: transparent; -fx-padding: 3 6; -fx-cursor: hand;");
        try {
            Image icon = new Image(imageUrl, 20, 20, true, true, true);
            if (!icon.isError()) {
                ImageView view = new ImageView(icon);
                view.setFitWidth(20);
                view.setFitHeight(20);
                button.setText("");
                button.setGraphic(view);
            }
        } catch (Exception ignored) {
        }
    }

    private void appendEmoji(String emoji) {
        if (commentInput == null) return;
        String current = commentInput.getText() == null ? "" : commentInput.getText();
        if (!current.isEmpty() && !current.endsWith(" ")) current += " ";
        commentInput.setText(current + emoji + " ");
        commentInput.requestFocus();
        commentInput.positionCaret(commentInput.getText().length());
    }

    private void handleEditPublication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/blog/PublicationForm.fxml"));
            Parent root = loader.load();
            PublicationFormController controller = loader.getController();
            controller.setPublication(publication);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier ma Publication");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            setData(pubService.afficherParId(publication.getIdPublication()));
        } catch (IOException ev) {
            ev.printStackTrace();
        }
    }

    private void showQrPopup() {
        if (publicationQrImage == null || publicationQrUrl == null) {
            NotificationUtil.showError("QR Code", "Impossible de generer le QR code pour cette publication.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("QR Code Publication");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ImageView qrPreview = new ImageView(publicationQrImage);
        qrPreview.setFitWidth(320);
        qrPreview.setFitHeight(320);
        qrPreview.setPreserveRatio(true);
        
        // Add a clean border wrapper for the QR code
        VBox qrWrapper = new VBox(qrPreview);
        qrWrapper.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #f1f3f5; -fx-border-width: 1; -fx-border-radius: 15;");

        Label helpText = new Label("Scannez ce code pour ouvrir la publication.");
        helpText.setStyle("-fx-text-fill: #1a1a1a; -fx-font-size: 14; -fx-font-weight: bold;");

        Label urlText = new Label(publicationQrUrl);
        urlText.setWrapText(true);
        urlText.setMaxWidth(360);
        urlText.setStyle("-fx-text-fill: #d4af37; -fx-font-size: 11; -fx-font-weight: bold;");

        VBox content = new VBox(20, qrWrapper, helpText, urlText);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 25;");

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(content);
        pane.setMinHeight(Region.USE_PREF_SIZE);
        // Premium gold border and white background for the dialog pane
        pane.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #d4af37; -fx-border-width: 2; -fx-border-radius: 25; -fx-background-radius: 25;");

        dialog.showAndWait();
    }

    private void handleTranslateAll() {
        if (translateAllBtn == null) return;
        Optional<String> selectedLanguage = askTargetLanguage();
        if (!selectedLanguage.isPresent()) return;
        String targetLanguage = selectedLanguage.get();

        translateAllBtn.setDisable(true);
        translateAllBtn.setText("Traduction " + targetLanguage + "...");

        String titleSource = titreLabel.getText();
        String descSource = descriptionText.getText();
        List<Label> commentLabels = new ArrayList<>();
        for (javafx.scene.Node node : commentsContainer.lookupAll(".comment-content")) {
            if (node instanceof Label) commentLabels.add((Label) node);
        }
        List<String> commentSources = new ArrayList<>();
        for (Label label : commentLabels) commentSources.add(label.getText());

        new Thread(() -> {
            String translatedTitle = GeminiVisionUtil.translateText(titleSource, targetLanguage);
            String translatedDesc = GeminiVisionUtil.translateText(descSource, targetLanguage);
            List<String> translatedComments = new ArrayList<>();
            for (String text : commentSources) {
                translatedComments.add(GeminiVisionUtil.translateText(text, targetLanguage));
            }
            Platform.runLater(() -> {
                titreLabel.setText(translatedTitle);
                descriptionText.setText(translatedDesc);
                for (int i = 0; i < commentLabels.size() && i < translatedComments.size(); i++) {
                    commentLabels.get(i).setText(translatedComments.get(i));
                }
                translateAllBtn.setDisable(false);
                translateAllBtn.setText("Traduire tout");
                NotificationUtil.showSuccess("Traduction", "Le contenu visible a ete traduit en " + targetLanguage + ".");
            });
        }).start();
    }

    private Optional<String> askTargetLanguage() {
        List<String> languages = Arrays.asList("French", "English", "Arabic", "Spanish", "German", "Italian");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("French", languages);
        dialog.setTitle("Choisir une langue");
        dialog.setHeaderText("Traduire le contenu visible");
        dialog.setContentText("Langue:");
        return dialog.showAndWait();
    }

    private void handleReaction(boolean isLike) {
        if (currentUser == null) return;

        PublicationReaction existing = reactionService.verifierReaction(currentUser.getId_user(), publication.getIdPublication());
        if (existing != null) {
            if (existing.isLike() == isLike) {
                reactionService.supprimer(existing.getIdReaction());
                NotificationUtil.showSuccess("Reaction", "Reaction retiree");
            } else {
                existing.setLike(isLike);
                reactionService.modifier(existing);
                NotificationUtil.showSuccess("Reaction", isLike ? "Vous aimez cette publication" : "Vous n'aimez pas cette publication");
            }
        } else {
            PublicationReaction pr = new PublicationReaction(0, currentUser.getId_user(), publication.getIdPublication(), isLike);
            reactionService.ajouter(pr);
            NotificationUtil.showSuccess("Reaction", isLike ? "Vous aimez cette publication" : "Vous n'aimez pas cette publication");
        }

        this.publication = pubService.afficherParId(publication.getIdPublication());
        updateReactions();
    }

    private void updateReactions() {
        likeBtn.setText("👍 " + publication.getNbLikes());
        dislikeBtn.setText("👎 " + publication.getNbDislikes());

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

        for (Commentaire commentaire : comments) {
            if (commentaire.isEstSignale()) continue;

            VBox commentBox = new VBox(5);
            commentBox.getStyleClass().add("comment-card");
            commentBox.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-background-radius: 10; -fx-padding: 10;");

            User author = userService.afficherParId(commentaire.getIdUser());
            Label userLabel = new Label(author != null ? author.getEmail() : "Utilisateur");
            userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #B8860B;");

            Label contentLabel = new Label(commentaire.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setStyle("-fx-text-fill: #111111; -fx-font-size: 13;");
            contentLabel.getStyleClass().add("comment-content");
            commentBox.getChildren().addAll(userLabel, contentLabel);

            if (currentUser != null && commentaire.getIdUser() == currentUser.getId_user()) {
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);

                Button editBtn = new Button("Modifier");
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -fx-gold; -fx-font-size: 10; -fx-cursor: hand;");

                Button delBtn = new Button("Supprimer");
                delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-font-size: 10; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    TextField editField = new TextField(commentaire.getContent());
                    editField.getStyleClass().add("blog-input");
                    editField.setStyle("-fx-font-size: 11;");

                    Button saveEdit = new Button("💾");
                    saveEdit.setStyle("-fx-background-color: -fx-gold; -fx-text-fill: black; -fx-background-radius: 5;");

                    HBox editBox = new HBox(5, editField, saveEdit);
                    HBox.setHgrow(editField, Priority.ALWAYS);

                    int idx = commentBox.getChildren().indexOf(contentLabel);
                    commentBox.getChildren().set(idx, editBox);
                    actions.setVisible(false);

                    saveEdit.setOnAction(ev -> {
                        String freshText = editField.getText().trim();
                        if (freshText.isEmpty()) return;
                        if (freshText.length() < 3) {
                            NotificationUtil.showError("Saisie Invalide", "Le commentaire doit faire au moins 3 caracteres.");
                            return;
                        }
                        int badWordsCount = BadWordsFilter.countBadWords(freshText);
                        if (badWordsCount > 0) {
                            NotificationUtil.showModerationStorm(
                                    badWordsCount,
                                    "Commentaire refuse : vous avez utilise " + badWordsCount + " mot(s) interdit(s)."
                            );
                            return;
                        }
                        commentaire.setContent(freshText);
                        comService.modifier(commentaire);
                        loadComments();
                    });
                });

                delBtn.setOnAction(e -> {
                    comService.supprimer(commentaire.getIdCommentaire());
                    loadComments();
                });

                actions.getChildren().addAll(editBtn, delBtn);
                commentBox.getChildren().add(actions);
            }

            commentsContainer.getChildren().add(commentBox);
        }
    }

    private void handleAddComment() {
        String content = commentInput.getText().trim();
        if (currentUser == null || content.isEmpty()) return;

        if (content.length() < 3) {
            NotificationUtil.showError("Saisie Invalide", "Le commentaire doit faire au moins 3 caracteres.");
            showCommentError("Commentaire trop court.");
            return;
        }

        int badWordsCount = BadWordsFilter.countBadWords(content);
        if (badWordsCount > 0) {
            NotificationUtil.showModerationStorm(
                    badWordsCount,
                    "Commentaire refuse : vous avez utilise " + badWordsCount + " mot(s) interdit(s)."
            );
            showCommentError("Votre commentaire contient des mots inappropries.");
            return;
        }
        hideCommentError();

        Commentaire commentaire = new Commentaire();
        commentaire.setIdUser(currentUser.getId_user());
        commentaire.setIdPublication(publication.getIdPublication());
        commentaire.setContent(content);
        commentaire.setStatus("visible");

        comService.ajouter(commentaire);
        commentInput.clear();
        loadComments();
        NotificationUtil.showSuccess("Nouveau Commentaire", "Votre commentaire a ete ajoute avec succes !");
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
