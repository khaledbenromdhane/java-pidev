package com.pidev.tools;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {
    private static final String SENDER_EMAIL = "asmajleli74@gmail.com";
    private static final String SENDER_PASSWORD = "vjgbgvluukskfoks";

    public static void sendPublicationEmail(String recipientEmail, String userName, String postTitle) {
        // Run email sending in a background thread to prevent UI freezing
        new Thread(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL, "Blog Platform"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Félicitations pour votre nouvelle publication !");

                String htmlTemplate = "<div style='font-family: \"Outfit\", \"Inter\", \"Segoe UI\", sans-serif; max-width: 600px; margin: 20px auto; padding: 40px; border-radius: 20px; background-color: #ffffff; border: 1px solid #f1f3f5; box-shadow: 0 10px 30px rgba(0,0,0,0.08);'>"
                        + "<div style='text-align: center; margin-bottom: 35px;'>"
                        + "<div style='display: inline-block; padding: 12px 25px; background: linear-gradient(135deg, #d4af37, #b8860b); border-radius: 50px; color: white; font-weight: bold; font-size: 14px; letter-spacing: 1px; text-transform: uppercase;'>Confirmation de Publication</div>"
                        + "</div>"
                        + "<h1 style='color: #1a1a1a; text-align: center; font-size: 28px; margin-bottom: 25px;'>Félicitations ! \u2728</h1>"
                        + "<p style='font-size: 16px; color: #444444; line-height: 1.8; text-align: center;'>"
                        + "Bonjour <span style='color: #d4af37; font-weight: bold;'>" + userName + "</span>,<br><br>"
                        + "Votre article vient d'être mis en ligne avec succès sur notre plateforme d'art."
                        + "</p>"
                        + "<div style='background-color: #fcfbf4; border-left: 4px solid #d4af37; padding: 25px; margin: 30px 0; border-radius: 8px;'>"
                        + "<p style='margin: 0; font-size: 13px; color: #999; text-transform: uppercase; letter-spacing: 0.5px;'>Titre de la publication</p>"
                        + "<p style='margin: 10px 0 0 0; font-size: 20px; font-weight: bold; color: #1a1a1a;'>\"" + postTitle + "\"</p>"
                        + "</div>"
                        + "<div style='text-align: center; margin-top: 40px;'>"
                        + "<p style='font-size: 15px; color: #666;'>Merci de partager votre talent avec nous. Votre contenu est désormais visible par toute la communauté.</p>"
                        + "</div>"
                        + "<hr style='border: none; border-top: 1px solid #f1f3f5; margin: 40px 0;' />"
                        + "<div style='text-align: center; font-size: 11px; color: #aaaaaa;'>"
                        + "<p>© " + java.time.Year.now().getValue() + " PIDEV Art Site • Le luxe de la création</p>"
                        + "<p style='margin-top: 10px;'>Ceci est une notification automatique. Merci de ne pas répondre à cet email.</p>"
                        + "</div>"
                        + "</div>";

                message.setContent(htmlTemplate, "text/html; charset=utf-8");

                Transport.send(message);
                javafx.application.Platform.runLater(() -> 
                    NotificationUtil.showSuccess("Email Envoyé", "Un email de confirmation a été envoyé à " + recipientEmail)
                );
                System.out.println("✅ Email envoyé avec succès à " + recipientEmail);

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> 
                    NotificationUtil.showError("Erreur Email", "Impossible d'envoyer l'email de confirmation.")
                );
                System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
