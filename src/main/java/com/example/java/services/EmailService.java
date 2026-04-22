package com.example.java.services;

import com.example.java.entities.CustomerInfo;
import com.example.java.entities.Oeuvre;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailService {
    private static final String HOST = "smtp.gmail.com";
    private static final int SSL_PORT = 465;

    private final Map<String, String> localEnv;

    public EmailService() {
        this.localEnv = loadLocalEnv();
    }

    public String defaultRecipient() {
        return value("GMAIL_USER");
    }

    public boolean isConfigured() {
        return !value("GMAIL_USER").isBlank() && !value("GMAIL_APP_PASSWORD").isBlank();
    }

    public void sendInvoice(String recipient, List<Oeuvre> items, double total) throws IOException {
        sendInvoice(recipient, items, total, null);
    }

    public void sendInvoice(String recipient, List<Oeuvre> items, double total, Path invoicePdf) throws IOException {
        sendInvoice(recipient, items, total, new CustomerInfo("", "", recipient, "", ""), invoicePdf);
    }

    public void sendInvoice(String recipient, List<Oeuvre> items, double total, CustomerInfo customer, Path invoicePdf)
            throws IOException {
        String sender = value("GMAIL_USER");
        String password = value("GMAIL_APP_PASSWORD").replaceAll("\\s+", "");

        if (sender.isBlank() || password.isBlank()) {
            throw new IOException("Configuration Gmail manquante. Ajoutez GMAIL_USER et GMAIL_APP_PASSWORD.");
        }

        String subject = "Facture ArtGalerie - paiement Stripe";
        String body = buildInvoiceHtml(items, total, customer);
        sendSmtp(sender, password, recipient, subject, body, invoicePdf);
    }

    private String buildInvoiceHtml(List<Oeuvre> items, double total, CustomerInfo customer) {
        StringBuilder rows = new StringBuilder();

        for (Oeuvre item : items) {
            rows.append("<tr>")
                    .append("<td style=\"padding:12px;border-bottom:1px solid #2a2a3a;color:#f2f2f6;\">")
                    .append(escapeHtml(item.getTitre()))
                    .append("</td>")
                    .append("<td style=\"padding:12px;border-bottom:1px solid #2a2a3a;color:#d4af37;text-align:right;font-weight:700;\">")
                    .append(String.format("%.2f TND", item.getPrix()))
                    .append("</td>")
                    .append("</tr>");
        }

        return """
                <!doctype html>
                <html>
                <body style="margin:0;background:#0b0b12;font-family:Arial,sans-serif;color:#e9e9ee;">
                  <div style="max-width:680px;margin:0 auto;padding:28px;">
                    <div style="background:#151521;border:1px solid rgba(212,175,55,.28);border-radius:14px;overflow:hidden;">
                      <div style="background:#1a1a2e;padding:28px;border-bottom:4px solid #d4af37;">
                        <div style="color:#f7d777;font-size:26px;font-weight:700;">ArtGalerie</div>
                        <div style="color:#9696a5;font-size:13px;margin-top:4px;">Facture de paiement</div>
                      </div>
                      <div style="padding:26px;">
                        <p style="margin:0 0 18px 0;color:#e9e9ee;">Bonjour %s,</p>
                        <p style="margin:0 0 22px 0;color:#b8b8c6;">Votre paiement Stripe a ete confirme. La facture PDF officielle est jointe a cet email.</p>
                        <div style="background:#0f0f1e;border:1px solid rgba(212,175,55,.16);border-radius:10px;padding:14px;margin-bottom:18px;color:#b8b8c6;">
                          <div style="color:#f7d777;font-weight:700;margin-bottom:8px;">Informations client</div>
                          <div>Nom complet : %s</div>
                          <div>Email : %s</div>
                          <div>Telephone : %s</div>
                          <div>Adresse : %s</div>
                        </div>
                        <table style="width:100%%;border-collapse:collapse;background:#0f0f1e;border-radius:10px;overflow:hidden;">
                          <thead>
                            <tr>
                              <th style="padding:12px;text-align:left;color:#f7d777;background:#0b0b12;">Oeuvre</th>
                              <th style="padding:12px;text-align:right;color:#f7d777;background:#0b0b12;">Prix</th>
                            </tr>
                          </thead>
                          <tbody>
                            %s
                          </tbody>
                        </table>
                        <div style="text-align:right;margin-top:22px;color:#f7d777;font-size:20px;font-weight:700;">Total : %.2f TND</div>
                        <p style="margin:26px 0 0 0;color:#9696a5;font-size:12px;">Merci pour votre achat.<br>ArtGalerie</p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(customer.getPrenom().isBlank() ? customer.getFullName() : customer.getPrenom()),
                escapeHtml(customer.getFullName()),
                escapeHtml(customer.getEmail()),
                escapeHtml(customer.getTelephone()),
                escapeHtml(customer.getAdresse()),
                rows,
                total
        );
    }

    private void sendSmtp(String sender, String password, String recipient, String subject, String htmlBody, Path invoicePdf)
            throws IOException {
        try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(HOST, SSL_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            socket.setSoTimeout(20000);

            expect(in, 220);
            send(out, "EHLO localhost");
            expect(in, 250);

            send(out, "AUTH LOGIN");
            expect(in, 334);
            send(out, Base64.getEncoder().encodeToString(sender.getBytes(StandardCharsets.UTF_8)));
            expect(in, 334);
            send(out, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
            expect(in, 235);

            send(out, "MAIL FROM:<" + sender + ">");
            expect(in, 250);
            send(out, "RCPT TO:<" + recipient + ">");
            expect(in, 250, 251);
            send(out, "DATA");
            expect(in, 354);

            out.write(message(sender, recipient, subject, htmlBody, invoicePdf));
            out.flush();
            expect(in, 250);

            send(out, "QUIT");
        }
    }

    private String message(String sender, String recipient, String subject, String htmlBody, Path invoicePdf)
            throws IOException {
        String boundary = "ARTGALERIE-" + System.currentTimeMillis();
        StringBuilder message = new StringBuilder();
        message.append("From: ").append(sender).append("\r\n")
                .append("To: ").append(recipient).append("\r\n")
                .append("Subject: ").append(encodeHeader(subject)).append("\r\n")
                .append("MIME-Version: 1.0\r\n")
                .append("Content-Type: multipart/mixed; boundary=\"").append(boundary).append("\"\r\n")
                .append("\r\n")
                .append("--").append(boundary).append("\r\n")
                .append("Content-Type: text/html; charset=UTF-8\r\n")
                .append("Content-Transfer-Encoding: 8bit\r\n")
                .append("\r\n")
                .append(htmlBody).append("\r\n");

        if (invoicePdf != null && Files.exists(invoicePdf)) {
            String filename = invoicePdf.getFileName().toString();
            message.append("--").append(boundary).append("\r\n")
                    .append("Content-Type: application/pdf; name=\"").append(filename).append("\"\r\n")
                    .append("Content-Disposition: attachment; filename=\"").append(filename).append("\"\r\n")
                    .append("Content-Transfer-Encoding: base64\r\n")
                    .append("\r\n")
                    .append(Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII))
                            .encodeToString(Files.readAllBytes(invoicePdf)))
                    .append("\r\n");
        }

        message.append("--").append(boundary).append("--\r\n");
        return dotStuff(message.toString()) + "\r\n.\r\n";
    }

    private String legacyMessage(String sender, String recipient, String subject, String body) {
        return "From: " + sender + "\r\n"
                + "To: " + recipient + "\r\n"
                + "Subject: " + encodeHeader(subject) + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Transfer-Encoding: 8bit\r\n"
                + "\r\n"
                + dotStuff(body).replace("\n", "\r\n")
                + "\r\n.\r\n";
    }

    private String encodeHeader(String value) {
        return "=?UTF-8?B?"
                + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8))
                + "?=";
    }

    private String dotStuff(String body) {
        return body.replace("\n.", "\n..");
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void send(BufferedWriter out, String command) throws IOException {
        out.write(command);
        out.write("\r\n");
        out.flush();
    }

    private void expect(BufferedReader in, int... allowedCodes) throws IOException {
        String line = in.readLine();
        if (line == null || line.length() < 3) {
            throw new IOException("Reponse SMTP invalide.");
        }

        String lastLine = line;
        while (line.length() > 3 && line.charAt(3) == '-') {
            lastLine = line;
            line = in.readLine();
            if (line == null) break;
        }
        if (line != null) {
            lastLine = line;
        }

        int code;
        try {
            code = Integer.parseInt(lastLine.substring(0, 3));
        } catch (NumberFormatException e) {
            throw new IOException("Code SMTP invalide : " + lastLine);
        }

        for (int allowed : allowedCodes) {
            if (code == allowed) {
                return;
            }
        }
        throw new IOException("Erreur SMTP : " + lastLine);
    }

    private String value(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String environment = System.getenv(key);
        if (environment != null && !environment.isBlank()) {
            return environment.trim();
        }

        return localEnv.getOrDefault(key, "").trim();
    }

    private Map<String, String> loadLocalEnv() {
        Map<String, String> values = new HashMap<>();
        Path path = Path.of(".env");

        if (!Files.exists(path)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isBlank() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                values.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
            }
        } catch (IOException ignored) {
        }

        return values;
    }
}
