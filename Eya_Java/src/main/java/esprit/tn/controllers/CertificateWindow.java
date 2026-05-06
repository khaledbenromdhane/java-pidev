package esprit.tn.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CertificateWindow extends Stage {

    private final String formationName;
    private final int score;
    private final String studentName = "Apprenant(e)";
    
    private final VBox certificatePane;

    public CertificateWindow(String formationName, int score) {
        this.formationName = formationName;
        this.score = score;
        
        setTitle("Votre Certificat de Réussite");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e1e1e;"); // Dark mode background
        
        // 1. The Visual Certificate
        certificatePane = createCertificatePane();
        
        // 2. The Buttons
        HBox buttonsBox = createButtonsBox();
        
        root.getChildren().addAll(certificatePane, buttonsBox);
        
        Scene scene = new Scene(root, 850, 650);
        setScene(scene);
    }

    private String getGrade() {
        if (score >= 19) return "Diamant 💎";
        if (score >= 16) return "Or 🥇";
        if (score >= 14) return "Argent 🥈";
        return "Bronze 🥉";
    }

    private String getGradeColor() {
        if (score >= 19) return "#00FFFF"; // Cyan for Diamond
        if (score >= 16) return "#FFD700"; // Gold
        if (score >= 14) return "#C0C0C0"; // Silver
        return "#CD7F32"; // Bronze
    }

    private VBox createCertificatePane() {
        VBox pane = new VBox(15);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(40));
        
        // Beautiful gradient background
        Stop[] stops = new Stop[] { new Stop(0, Color.web("#2c3e50")), new Stop(1, Color.web("#3498db")) };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        
        pane.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(15), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(Color.web(getGradeColor()), BorderStrokeStyle.SOLID, new CornerRadii(15), new BorderWidths(5))));
        
        Label title = new Label("CERTIFICAT DE RÉUSSITE");
        title.setFont(Font.font("System", FontWeight.BOLD, 36));
        title.setTextFill(Color.WHITE);
        
        Label subtitle = new Label("Ce certificat est fièrement décerné à");
        subtitle.setFont(Font.font("System", 18));
        subtitle.setTextFill(Color.LIGHTGRAY);
        
        Label name = new Label(studentName);
        name.setFont(Font.font("System", FontWeight.BOLD, 28));
        name.setTextFill(Color.web("#f1c40f"));
        
        Label reason = new Label("Pour avoir complété avec succès la formation :");
        reason.setFont(Font.font("System", 18));
        reason.setTextFill(Color.LIGHTGRAY);
        
        Label formation = new Label(formationName);
        formation.setFont(Font.font("System", FontWeight.BOLD, 24));
        formation.setTextFill(Color.WHITE);
        formation.setTextAlignment(TextAlignment.CENTER);
        formation.setWrapText(true);
        
        Label gradeLabel = new Label("Grade : " + getGrade());
        gradeLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        gradeLabel.setTextFill(Color.web(getGradeColor()));
        
        Label scoreLabel = new Label("Score : " + score + " / 20");
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        scoreLabel.setTextFill(Color.WHITE);
        
        pane.getChildren().addAll(title, subtitle, name, reason, formation, gradeLabel, scoreLabel);
        
        if (score > 15) {
            int montant = (score - 15) * 50; // Par exemple : 50 TND par point au-dessus de 15
            Label rewardLabel = new Label("🎁 Félicitations ! Vous gagnez une prime de " + montant + " TND ! 🎁");
            rewardLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
            rewardLabel.setTextFill(Color.web("#2ecc71")); // Vert émeraude
            rewardLabel.setStyle("-fx-background-color: #27ae6033; -fx-padding: 5 15; -fx-background-radius: 10;");
            pane.getChildren().add(rewardLabel);
        }
        
        String dateStr = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        Label dateLabel = new Label("Fait le " + dateStr);
        dateLabel.setFont(Font.font("System", 14));
        dateLabel.setTextFill(Color.LIGHTGRAY);
        
        pane.getChildren().addAll(new Region(), dateLabel);
        
        // Add some space before date
        VBox.setVgrow(pane.getChildren().get(pane.getChildren().size() - 2), Priority.ALWAYS);
        
        pane.setPrefSize(750, 500);
        return pane;
    }

    private HBox createButtonsBox() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        
        Button btnDownload = new Button("💾 Télécharger PNG");
        btnDownload.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20;");
        btnDownload.setOnAction(e -> downloadPNG());
        
        Button btnLinkedIn = new Button("🔗 Partager sur LinkedIn");
        btnLinkedIn.setStyle("-fx-background-color: #0077b5; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20;");
        btnLinkedIn.setOnAction(e -> shareOnLinkedIn());
        
        Button btnInstagram = new Button("📷 Partager sur Instagram");
        btnInstagram.setStyle("-fx-background-color: #e1306c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20;");
        btnInstagram.setOnAction(e -> shareOnInstagram());
        
        box.getChildren().addAll(btnDownload, btnLinkedIn, btnInstagram);
        return box;
    }



    private void downloadPNG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Certificat");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images PNG", "*.png"));
        fileChooser.setInitialFileName("Certificat_" + formationName.replaceAll("\\s+", "_") + ".png");
        
        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            try {
                WritableImage snapshot = certificatePane.snapshot(new SnapshotParameters(), null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le certificat a été sauvegardé avec succès !");
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Impossible de sauvegarder l'image : " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void shareOnLinkedIn() {
        try {
            String text = "Je suis fier d'annoncer que j'ai complété la formation '" + formationName + "' avec un score de " + score + "/20 (Grade : " + getGrade() + ") ! 🎓✨";
            String url = "https://www.linkedin.com/feed/?shareActive=true&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void shareOnInstagram() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instagram");
        alert.setHeaderText("Partager sur Instagram");
        alert.setContentText("Instagram ne permet pas de publier directement depuis une application bureau.\n\nVeuillez télécharger le certificat en format PNG, puis l'importer dans l'application Instagram sur votre téléphone ou via le navigateur !");
        alert.showAndWait();
        
        try {
            Desktop.getDesktop().browse(new URI("https://www.instagram.com/"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
