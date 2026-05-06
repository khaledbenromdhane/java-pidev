package esprit.tn.controllers;

import esprit.tn.models.formation;
import esprit.tn.services.QRCodeService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class QRCodeDisplayController {

    @FXML
    private Label formationNameLabel;

    @FXML
    private ImageView qrImageView;

    public void setFormationData(formation f) {
        formationNameLabel.setText(f.getNom_form());
        
        try {
            // Create a string representation of the formation to be encoded in the QR code
            // This could be a URL or just a formatted string
            String qrData = "Formation: " + f.getNom_form() + "\n" +
                           "Type: " + f.getType() + "\n" +
                           "Date: " + f.getDate_form().toString() + "\n" +
                           "Description: " + f.getDescription();
            
            Image qrImage = QRCodeService.generateQRCodeImage(qrData, 250, 250);
            qrImageView.setImage(qrImage);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error (maybe show an error image)
        }
    }
}
