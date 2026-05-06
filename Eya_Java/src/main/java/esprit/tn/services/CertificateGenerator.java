package esprit.tn.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CertificateGenerator {

    public static String generateCertificate(String studentName, String formationName, int score) {
        Document document = new Document(PageSize.A4.rotate());
        
        // Define directory to save certificates
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, "Desktop" + File.separator + "Certificats_Eya");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String fileName = "Certificat_" + formationName.replaceAll("\\s+", "_") + ".pdf";
        String filePath = new File(dir, fileName).getAbsolutePath();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Add border
            Rectangle rect = new Rectangle(577, 825, 18, 15);
            rect.enableBorderSide(1);
            rect.enableBorderSide(2);
            rect.enableBorderSide(4);
            rect.enableBorderSide(8);
            rect.setBorderColor(BaseColor.BLACK);
            rect.setBorderWidth(1);
            document.add(rect);

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 45, BaseColor.BLUE);
            Paragraph title = new Paragraph("Certificat de Réussite", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(30);
            title.setSpacingAfter(20);
            document.add(title);

            // Line Separator
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(BaseColor.LIGHT_GRAY);
            document.add(new Chunk(ls));
            document.add(new Paragraph("\n\n"));

            // Text
            Font textFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 22, BaseColor.BLACK);
            Paragraph p1 = new Paragraph("Ce certificat est fièrement décerné à", textFont);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);

            Font nameFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 35, BaseColor.DARK_GRAY);
            Paragraph pName = new Paragraph(studentName, nameFont);
            pName.setAlignment(Element.ALIGN_CENTER);
            pName.setSpacingBefore(10);
            pName.setSpacingAfter(10);
            document.add(pName);

            Paragraph p2 = new Paragraph("Pour avoir complété avec succès la formation :", textFont);
            p2.setAlignment(Element.ALIGN_CENTER);
            document.add(p2);

            Font courseFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 30, BaseColor.RED);
            Paragraph pCourse = new Paragraph(formationName, courseFont);
            pCourse.setAlignment(Element.ALIGN_CENTER);
            pCourse.setSpacingBefore(10);
            pCourse.setSpacingAfter(10);
            document.add(pCourse);

            // Score and Date
            Font scoreFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 20, BaseColor.BLACK);
            Paragraph pScore = new Paragraph("Avec le score exceptionnel de : " + score + "/20", scoreFont);
            pScore.setAlignment(Element.ALIGN_CENTER);
            pScore.setSpacingAfter(40);
            document.add(pScore);

            String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            Paragraph pDate = new Paragraph("Fait le : " + date, textFont);
            pDate.setAlignment(Element.ALIGN_RIGHT);
            document.add(pDate);

            document.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
