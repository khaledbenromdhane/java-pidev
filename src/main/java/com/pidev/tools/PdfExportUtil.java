package com.pidev.tools;

import com.pidev.controllers.AdminParticipationsController.ParticipationRow;
import com.pidev.entities.Evenement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PdfExportUtil {

    private PdfExportUtil() {
    }

    public static void exportEvents(File file,
                                    List<Evenement> events,
                                    String total,
                                    String upcoming,
                                    String paid,
                                    String attendees) throws IOException {
        String title = "Evenements Report";
        String subtitle = "Generated from Backoffice";
        String[] statLabels = {"Total", "Upcoming", "Paid", "Attendees"};
        String[] statValues = {total, upcoming, paid, attendees};

        String[] headers = {"ID", "Name", "Type", "Participants", "Date", "Time", "Location", "Payment"};
        float[] widths = {40, 130, 90, 70, 70, 55, 110, 70};

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = drawHeader(cs, page, title, subtitle, statLabels, statValues);
            y -= 18;

            y = drawTableHeader(cs, page, y, headers, widths);
            y -= 2;

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

            int rowIndex = 0;
            for (Evenement e : events) {
                if (y < 90) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = drawHeader(cs, page, title, subtitle, statLabels, statValues);
                    y -= 18;
                    y = drawTableHeader(cs, page, y, headers, widths);
                    y -= 2;
                }

                boolean stripe = rowIndex % 2 == 1;
                String payment = Boolean.TRUE.equals(e.getPaiement()) ? "Paid" : "Free";
                String date = e.getDate() == null ? "" : df.format(e.getDate());
                String time = e.getHeure() == null ? "" : tf.format(e.getHeure());

                String[] row = {
                        String.valueOf(e.getId()),
                        safe(e.getNom()),
                        safe(e.getTypeEvenement()),
                        String.valueOf(e.getNbrParticipant() == null ? 0 : e.getNbrParticipant()),
                        date,
                        time,
                        safe(e.getLieu()),
                        payment
                };

                y = drawTableRow(cs, page, y, row, widths, stripe);
                rowIndex++;
            }

            cs.close();
            doc.save(file);
        }
    }

    public static void exportParticipations(File file,
                                            List<ParticipationRow> rows,
                                            String total,
                                            String confirmed,
                                            String pending,
                                            String cancelled) throws IOException {
        String title = "Participations Report";
        String subtitle = "Generated from Backoffice";
        String[] statLabels = {"Total", "Confirmed", "Pending", "Cancelled"};
        String[] statValues = {total, confirmed, pending, cancelled};

        String[] headers = {"ID", "User", "Event", "Date", "Status", "Nbr", "Payment"};
        float[] widths = {40, 110, 150, 70, 80, 40, 70};

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = drawHeader(cs, page, title, subtitle, statLabels, statValues);
            y -= 18;

            y = drawTableHeader(cs, page, y, headers, widths);
            y -= 2;

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int rowIndex = 0;
            for (ParticipationRow r : rows) {
                if (y < 90) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = drawHeader(cs, page, title, subtitle, statLabels, statValues);
                    y -= 18;
                    y = drawTableHeader(cs, page, y, headers, widths);
                    y -= 2;
                }

                boolean stripe = rowIndex % 2 == 1;
                String date = r.getDate() == null ? "" : df.format(r.getDate());

                String[] row = {
                        String.valueOf(r.getId()),
                        safe(r.getUser()),
                        safe(r.getEvent()),
                        date,
                        safe(r.getStatus()),
                        String.valueOf(r.getNbr()),
                        safe(r.getPaiement())
                };

                y = drawTableRow(cs, page, y, row, widths, stripe);
                rowIndex++;
            }

            cs.close();
            doc.save(file);
        }
    }

    private static float drawHeader(PDPageContentStream cs,
                                    PDPage page,
                                    String title,
                                    String subtitle,
                                    String[] statLabels,
                                    String[] statValues) throws IOException {
        float width = page.getMediaBox().getWidth();
        float height = page.getMediaBox().getHeight();

        cs.setNonStrokingColor(new Color(14, 24, 39));
        cs.addRect(0, height - 80, width, 80);
        cs.fill();

        cs.setNonStrokingColor(Color.WHITE);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
        cs.beginText();
        cs.newLineAtOffset(40, height - 45);
        cs.showText(title);
        cs.endText();

        cs.setNonStrokingColor(new Color(200, 200, 200));
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.beginText();
        cs.newLineAtOffset(40, height - 62);
        cs.showText(subtitle);
        cs.endText();

        float statsY = height - 110;
        float statX = 40;
        float statW = (width - 80) / 4f;
        for (int i = 0; i < statLabels.length; i++) {
            cs.setNonStrokingColor(new Color(245, 246, 248));
            cs.addRect(statX + (statW * i), statsY - 22, statW - 6, 36);
            cs.fill();

            cs.setNonStrokingColor(new Color(30, 41, 59));
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.beginText();
            cs.newLineAtOffset(statX + (statW * i) + 8, statsY);
            cs.showText(statLabels[i] + ": " + statValues[i]);
            cs.endText();
        }

        return height - 150;
    }

    private static float drawTableHeader(PDPageContentStream cs,
                                         PDPage page,
                                         float y,
                                         String[] headers,
                                         float[] widths) throws IOException {
        float x = 40;
        cs.setNonStrokingColor(new Color(230, 233, 238));
        cs.addRect(x, y - 18, totalWidth(widths), 20);
        cs.fill();

        cs.setNonStrokingColor(new Color(33, 37, 41));
        cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(x + 4, y - 12);
            cs.showText(headers[i]);
            cs.endText();
            x += widths[i];
        }
        return y - 20;
    }

    private static float drawTableRow(PDPageContentStream cs,
                                      PDPage page,
                                      float y,
                                      String[] row,
                                      float[] widths,
                                      boolean stripe) throws IOException {
        float x = 40;

        if (stripe) {
            cs.setNonStrokingColor(new Color(248, 249, 251));
            cs.addRect(x, y - 18, totalWidth(widths), 20);
            cs.fill();
        }

        cs.setNonStrokingColor(new Color(55, 65, 81));
        cs.setFont(PDType1Font.HELVETICA, 9);

        for (int i = 0; i < row.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(x + 4, y - 12);
            cs.showText(trim(row[i], 26));
            cs.endText();
            x += widths[i];
        }
        return y - 20;
    }

    private static float totalWidth(float[] widths) {
        float sum = 0;
        for (float w : widths) {
            sum += w;
        }
        return sum;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + ".";
    }
}
