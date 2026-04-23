package com.pidev.controllers;

import com.pidev.entities.User;
import com.pidev.services.CrudService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class ForgotPasswordController {

    @FXML private VBox step1, step2;
    @FXML private TextField tfEmail, tfCode;
    @FXML private PasswordField pfNewPassword;
    @FXML private Label lblMessage;

    private final CrudService service = new CrudService();
    private String generatedCode;
    private String userEmail;

    @FXML
    private void envoyerCode() {
        String email = tfEmail.getText().trim();
        if (email.isEmpty() || !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            afficherErreur("⚠️ Veuillez entrer un email valide.");
            return;
        }

        User user = service.getByEmail(email);
        if (user == null) {
            afficherErreur("❌ Aucun compte associé à cet email.");
            return;
        }

        userEmail = email;
        generatedCode = String.format("%06d", new Random().nextInt(999999));

        if (sendEmail(email, generatedCode)) {
            afficherSucces("✅ Code envoyé à " + email);
            step1.setVisible(false);
            step1.setManaged(false);
            step2.setVisible(true);
            step2.setManaged(true);
        } else {
            afficherErreur("❌ Erreur lors de l'envoi de l'email.");
        }
    }

    @FXML
    private void reinitialiserMdp() {
        String code = tfCode.getText().trim();
        String newPassword = pfNewPassword.getText().trim();

        if (!code.equals(generatedCode)) {
            afficherErreur("❌ Code de vérification incorrect.");
            return;
        }

        if (newPassword.length() < 4) {
            afficherErreur("⚠️ Mot de passe trop court (min. 4 caractères).");
            return;
        }

        service.modifierMotDePasse(userEmail, newPassword);
        afficherSucces("✅ Mot de passe réinitialisé ! Redirection...");
        
        // Redirection après 2 secondes
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(this::retourConnexion);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void retourConnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pidev/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean sendEmail(String to, String code) {
        // CONFIGURATION SMTP (GMAIL)
        final String username = "mootez.ouri@esprit.tn"; 
        final String from     = "mootezouri090@gmail.com"; 
        final String password = "yytkasakpwczznsp"; 

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Récupération de mot de passe - Tun'Arche");
            message.setText("Votre code de vérification est : " + code + "\n\nSi vous n'avez pas demandé ce code, ignorez cet email.");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void afficherErreur(String msg) {
        lblMessage.setStyle("-fx-text-fill: #e84040;");
        lblMessage.setText(msg);
    }

    private void afficherSucces(String msg) {
        lblMessage.setStyle("-fx-text-fill: #40e840;");
        lblMessage.setText(msg);
    }
}
