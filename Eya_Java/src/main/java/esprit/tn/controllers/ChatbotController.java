package esprit.tn.controllers;

import esprit.tn.services.GeminiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatbotController {

    @FXML private VBox chatContainer;
    @FXML private TextField inputField;
    @FXML private ScrollPane scrollPane;
    @FXML private Button closeBtn;

    private final GeminiService geminiService = new GeminiService();

    @FXML
    public void initialize() {
        // Welcome message
        addMessage("Bonjour ! Je suis votre assistant IA. Comment puis-je vous aider aujourd'hui ?", false);
        
        // Auto-scroll to bottom
        chatContainer.heightProperty().addListener((observable, oldValue, newValue) -> 
            scrollPane.setVvalue(1.0));
    }

    @FXML
    private void handleSend() {
        String question = inputField.getText().trim();
        if (question.isEmpty()) return;

        addMessage(question, true);
        inputField.clear();

        // Add a "typing" indicator
        Label typingLabel = new Label("L'IA réfléchit...");
        typingLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic; -fx-font-size: 11px;");
        chatContainer.getChildren().add(typingLabel);

        geminiService.askQuestionAsync(question).thenAccept(response -> {
            Platform.runLater(() -> {
                chatContainer.getChildren().remove(typingLabel);
                addMessage(response, false);
            });
        });
    }

    private void addMessage(String text, boolean isUser) {
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        
        HBox container = new HBox();
        container.setPrefWidth(chatContainer.getWidth());
        
        if (isUser) {
            container.setAlignment(Pos.CENTER_RIGHT);
            messageLabel.setStyle("-fx-background-color: #d4af37; -fx-text-fill: black; -fx-padding: 10 15; -fx-background-radius: 15 15 0 15;");
        } else {
            container.setAlignment(Pos.CENTER_LEFT);
            messageLabel.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 15 15 15 0;");
        }
        
        container.getChildren().add(messageLabel);
        chatContainer.getChildren().add(container);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
