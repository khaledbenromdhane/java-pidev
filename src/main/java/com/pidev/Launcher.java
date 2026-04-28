package com.pidev;

import com.pidev.tools.myconnexion;
import com.pidev.services.LocalWebServerService;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Test connexion BD
        myconnexion.getInstance();
        LocalWebServerService.getInstance().startServer();

        // Lancement JavaFX
        Application.launch(HelloApplication.class, args);
    }
}
