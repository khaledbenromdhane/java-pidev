package esprit.tn.controllers;

import esprit.tn.models.formation;
import esprit.tn.services.serviceformation;
import esprit.tn.services.GeminiService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class AddEvaluationController implements Initializable {
    @FXML private ComboBox<formation> formationComboBox;
    @FXML private VBox questionsContainer;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Label timerLabel;

    private final serviceformation formationService = new serviceformation();
    private final GeminiService geminiService = new GeminiService();
    private Stage dialogStage;
    private final Map<String, Control> inputMap = new HashMap<>();
    
    private Timeline timeline;
    private int secondsRemaining = 180;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<formation> formations = formationService.getAllFormations();
        formationComboBox.setItems(FXCollections.observableArrayList(formations));
        
        formationComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(formation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_form());
            }
        });
        
        formationComboBox.setOnAction(event -> {
            formation f = formationComboBox.getValue();
            if (f != null) generateDiverseQuestions(f);
        });
        
        initTimer();
    }

    private void initTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsRemaining--;
            updateTimerLabel();
            if (secondsRemaining <= 0) handleTimeUp();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateTimerLabel() {
        int m = secondsRemaining / 60, s = secondsRemaining % 60;
        timerLabel.setText(String.format("⏱️ %02d:%02d", m, s));
        
        if (secondsRemaining <= 10 && secondsRemaining > 0) {
            java.awt.Toolkit.getDefaultToolkit().beep(); // Bip d'avertissement
            timerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            // Effet de clignotement simple
            if (secondsRemaining % 2 == 0) {
                timerLabel.setOpacity(0.5);
            } else {
                timerLabel.setOpacity(1.0);
            }
        } else {
            timerLabel.setOpacity(1.0);
            timerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + (secondsRemaining <= 30 ? "#e74c3c;" : "#e67e22;"));
        }
    }

    private void startTimer() {
        secondsRemaining = 180;
        timerLabel.setVisible(true);
        timeline.playFromStart();
    }

    private void stopTimer() { if (timeline != null) timeline.stop(); }

    private void handleTimeUp() {
        stopTimer();
        // Faire une petite "sonnerie" (3 bips)
        new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            }
        }).start();
        
        inputMap.values().forEach(c -> c.setDisable(true));
        saveBtn.setDisable(true);
        showError("Temps écoulé", "Le temps est terminé.");
    }

    private void generateDiverseQuestions(formation f) {
        stopTimer();
        questionsContainer.getChildren().clear();
        inputMap.clear();
        saveBtn.setDisable(true);
        
        Label loadingLabel = new Label("🤖 Gemini prépare l'examen...");
        loadingLabel.setStyle("-fx-text-fill: #3498db;");
        questionsContainer.getChildren().add(new VBox(10, new ProgressIndicator(), loadingLabel));

        geminiService.generateDiverseQuestionsAsync(f.getNom_form(), f.getDescription())
            .thenAccept(questions -> {
                Platform.runLater(() -> {
                    questionsContainer.getChildren().clear();
                    questions.forEach(qRaw -> {
                        String type = "[OUVERTE]";
                        String text = qRaw;
                        if (qRaw.contains("]")) {
                            type = qRaw.substring(0, qRaw.indexOf("]") + 1);
                            text = qRaw.substring(qRaw.indexOf("]") + 1).trim();
                            if (text.startsWith(":")) text = text.substring(1).trim();
                        }
                        if (text.isEmpty()) text = "Complétez ou répondez :";
                        
                        Label qLabel = new Label(text);
                        qLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                        qLabel.setWrapText(true);
                        
                        Label typeLabel = new Label(type);
                        typeLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
                        
                        Control input = type.contains("VRAI/FAUX") ? new ComboBox<>(FXCollections.observableArrayList("Vrai", "Faux")) : new TextArea();
                        input.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white;");
                        if (input instanceof TextArea) {
                            ((TextArea)input).setPrefRowCount(2);
                            ((TextArea)input).setPromptText("Saisissez votre réponse ici...");
                        }
                        
                        VBox qBox = new VBox(8, typeLabel, qLabel, input);
                        qBox.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 15; -fx-background-radius: 8; -fx-border-left: 5px solid #3498db;");
                        questionsContainer.getChildren().add(qBox);
                        inputMap.put(qRaw, input);
                    });
                    saveBtn.setDisable(false);
                    startTimer();
                });
            });
    }

    @FXML
    private void handleSave() {
        formation f = formationComboBox.getValue();
        if (f == null || inputMap.isEmpty()) return;
        
        StringBuilder userContent = new StringBuilder();
        for (Map.Entry<String, Control> entry : inputMap.entrySet()) {
            String ans = (entry.getValue() instanceof TextArea) ? ((TextArea)entry.getValue()).getText() : (String)((ComboBox)entry.getValue()).getValue();
            if (ans == null || ans.trim().isEmpty()) { showError("Incomplet", "Répondez à tout !"); return; }
            userContent.append("Q: ").append(entry.getKey()).append("\nR: ").append(ans).append("\n\n");
        }
        
        stopTimer();
        saveBtn.setDisable(true);
        saveBtn.setText("Calcul du score...");

        geminiService.calculateScoreOnlyAsync(f.getNom_form(), f.getDescription(), userContent.toString())
            .thenAccept(score -> Platform.runLater(() -> {
                try {
                    if (new esprit.tn.services.serviceevaluation().addEvaluation(new esprit.tn.models.evaluation(
                            score, f.getId(), "Examen Dynamique", userContent.toString()))) {
                        
                        showResultAlert(score, f.getNom_form());
                        dialogStage.close();
                    } else {
                        showError("Erreur", "Impossible d'enregistrer l'évaluation dans la base de données.");
                        saveBtn.setDisable(false);
                        saveBtn.setText("Valider l'examen");
                    }
                } catch (Exception ex) {
                    showError("Erreur", "Une erreur est survenue lors de l'enregistrement : " + ex.getMessage());
                    saveBtn.setDisable(false);
                    saveBtn.setText("Valider l'examen");
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError("Erreur API", "Le calcul du score a échoué : " + ex.getMessage());
                    saveBtn.setDisable(false);
                    saveBtn.setText("Valider l'examen");
                });
                return null;
            });
    }

    private void showResultAlert(int score, String formationName) {
        
        if (score >= 10) {
            esprit.tn.controllers.CertificateWindow certWindow = new esprit.tn.controllers.CertificateWindow(formationName, score);
            certWindow.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Résultat Final");
            alert.setHeaderText("Examen non validé.");
            alert.setContentText("Votre score pour '" + formationName + "' est de : " + score + "/20");
            alert.showAndWait();
        }
    }

    @FXML private void handleCancel() { stopTimer(); dialogStage.close(); }
    public void setSelectedFormation(formation f) {
        if (f != null) {
            formationComboBox.setValue(f);
            formationComboBox.setDisable(true); // Disable selection since it's already set
            generateDiverseQuestions(f);
        }
    }

    public void setDialogStage(Stage s) { this.dialogStage = s; }

    private void showSuccess(String t, String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(m); a.showAndWait(); }
    private void showError(String t, String m) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(m); a.showAndWait(); }
}
