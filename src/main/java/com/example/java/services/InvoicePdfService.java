package com.example.java.services;

import com.example.java.entities.CustomerInfo;
import com.example.java.entities.Oeuvre;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoicePdfService {
    private static final Color BG = new Color(11, 11, 18);
    private static final Color PANEL = new Color(21, 21, 33);
    private static final Color PANEL_2 = new Color(26, 26, 46);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color GOLD_LIGHT = new Color(247, 215, 119);
    private static final Color TEXT = new Color(233, 233, 238);
    private static final Color MUTED = new Color(150, 150, 165);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public Path generate(List<Oeuvre> items, double total, String customerEmail) throws IOException {
        return generate(items, total, new CustomerInfo("", "", customerEmail, "", ""));
    }

    public Path generate(List<Oeuvre> items, double total, CustomerInfo customer) throws IOException {
        Path invoicesDir = Path.of("invoices");
        Files.createDirectories(invoicesDir);
        Path output = invoicesDir.resolve("facture-artgalerie-" + LocalDateTime.now().format(FILE_DATE) + ".pdf");
        generateTo(items, total, customer, output);
        return output;
    }

    public void generateTo(List<Oeuvre> items, double total, String customerEmail, Path output) throws IOException {
        generateTo(items, total, new CustomerInfo("", "", customerEmail, "", ""), output);
    }

    public void generateTo(List<Oeuvre> items, double total, CustomerInfo customer, Path output) throws IOException {
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        try (PDDocument document = new PDDocument()) {
            PDFont regular = loadFont(document, false);
            PDFont bold = loadFont(document, true);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                drawBackground(cs, page);
                drawHeader(cs, bold, regular);
                drawCustomerBlock(cs, bold, regular, customer);
                drawItems(cs, bold, regular, items);
                drawTotal(cs, bold, total);
                drawFooter(cs, regular);
            }

            document.save(output.toFile());
        }
    }

    private PDFont loadFont(PDDocument document, boolean bold) {
        String[] candidates = bold
                ? new String[]{"C:/Windows/Fonts/arialbd.ttf", "C:/Windows/Fonts/segoeuib.ttf"}
                : new String[]{"C:/Windows/Fonts/arial.ttf", "C:/Windows/Fonts/segoeui.ttf"};

        for (String candidate : candidates) {
            File font = new File(candidate);
            if (font.exists()) {
                try {
                    return PDType0Font.load(document, font);
                } catch (IOException ignored) {
                }
            }
        }

        return new PDType1Font(bold
                ? Standard14Fonts.FontName.HELVETICA_BOLD
                : Standard14Fonts.FontName.HELVETICA);
    }

    private void drawBackground(PDPageContentStream cs, PDPage page) throws IOException {
        PDRectangle box = page.getMediaBox();
        cs.setNonStrokingColor(BG);
        cs.addRect(0, 0, box.getWidth(), box.getHeight());
        cs.fill();

        cs.setNonStrokingColor(PANEL);
        roundRect(cs, 40, 50, box.getWidth() - 80, box.getHeight() - 100, 14);
        cs.fill();

        cs.setNonStrokingColor(PANEL_2);
        roundRect(cs, 40, box.getHeight() - 190, box.getWidth() - 80, 140, 14);
        cs.fill();

        cs.setNonStrokingColor(GOLD);
        cs.addRect(40, box.getHeight() - 62, box.getWidth() - 80, 4);
        cs.fill();
    }

    private void drawHeader(PDPageContentStream cs, PDFont bold, PDFont regular) throws IOException {
        text(cs, bold, 28, GOLD_LIGHT, 64, 780, "ArtGalerie");
        text(cs, regular, 10, MUTED, 66, 762, "Votre espace artistique");

        text(cs, bold, 28, TEXT, 360, 778, "FACTURE");
        text(cs, regular, 10, MUTED, 362, 760, "Paiement Stripe");
        text(cs, regular, 10, MUTED, 362, 744, "Date : " + LocalDateTime.now().format(DATE_FORMAT));
    }

    private void drawCustomerBlock(PDPageContentStream cs, PDFont bold, PDFont regular, CustomerInfo customer)
            throws IOException {
        text(cs, bold, 13, GOLD, 64, 705, "Client");
        text(cs, regular, 11, TEXT, 64, 684, truncate(customer.getFullName(), 38));
        text(cs, regular, 10, MUTED, 64, 666, truncate(customer.getEmail(), 44));
        text(cs, regular, 10, MUTED, 64, 650, "Tel : " + truncate(customer.getTelephone(), 28));
        text(cs, regular, 10, MUTED, 64, 634, "Adresse : " + truncate(customer.getAdresse(), 50));

        text(cs, bold, 13, GOLD, 330, 705, "Statut");
        text(cs, regular, 11, TEXT, 330, 684, "Paiement confirme");
    }

    private void drawItems(PDPageContentStream cs, PDFont bold, PDFont regular, List<Oeuvre> items) throws IOException {
        float x = 64;
        float y = 625;
        float w = 466;

        cs.setNonStrokingColor(BG);
        roundRect(cs, x, y, w, 30, 8);
        cs.fill();
        text(cs, bold, 10, GOLD_LIGHT, x + 14, y + 11, "Oeuvre");
        text(cs, bold, 10, GOLD_LIGHT, x + 285, y + 11, "Etat");
        text(cs, bold, 10, GOLD_LIGHT, x + 390, y + 11, "Prix");

        float rowY = y - 38;
        int index = 1;
        for (Oeuvre item : items) {
            if (rowY < 145) {
                break;
            }

            cs.setNonStrokingColor(index % 2 == 0 ? PANEL_2 : new Color(18, 18, 29));
            roundRect(cs, x, rowY, w, 30, 8);
            cs.fill();

            text(cs, regular, 10, TEXT, x + 14, rowY + 12, truncate(item.getTitre(), 38));
            text(cs, regular, 9, MUTED, x + 285, rowY + 12, truncate(item.getEtat(), 16));
            text(cs, bold, 10, GOLD_LIGHT, x + 390, rowY + 12, String.format("%.2f TND", item.getPrix()));

            rowY -= 36;
            index++;
        }
    }

    private void drawTotal(PDPageContentStream cs, PDFont bold, double total) throws IOException {
        cs.setNonStrokingColor(new Color(212, 175, 55, 38));
        roundRect(cs, 330, 95, 200, 44, 10);
        cs.fill();
        text(cs, bold, 12, GOLD, 350, 120, "Total");
        text(cs, bold, 17, GOLD_LIGHT, 410, 117, String.format("%.2f TND", total));
    }

    private void drawFooter(PDPageContentStream cs, PDFont regular) throws IOException {
        text(cs, regular, 9, MUTED, 64, 86, "Merci pour votre achat. Cette facture PDF est generee par ArtGalerie.");
    }

    private void text(PDPageContentStream cs, PDFont font, float size, Color color, float x, float y, String value)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(safe(value));
        cs.endText();
    }

    private String truncate(String value, int max) {
        String safe = safe(value);
        return safe.length() <= max ? safe : safe.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private void roundRect(PDPageContentStream cs, float x, float y, float width, float height, float radius)
            throws IOException {
        float k = 0.552284749831f;
        float c = radius * k;

        cs.moveTo(x + radius, y);
        cs.lineTo(x + width - radius, y);
        cs.curveTo(x + width - radius + c, y, x + width, y + radius - c, x + width, y + radius);
        cs.lineTo(x + width, y + height - radius);
        cs.curveTo(x + width, y + height - radius + c, x + width - radius + c, y + height, x + width - radius, y + height);
        cs.lineTo(x + radius, y + height);
        cs.curveTo(x + radius - c, y + height, x, y + height - radius + c, x, y + height - radius);
        cs.lineTo(x, y + radius);
        cs.curveTo(x, y + radius - c, x + radius - c, y, x + radius, y);
        cs.closePath();
    }
}
