package com.pidev;

import com.pidev.tools.myconnexion;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Test connexion BD
        myconnexion.getInstance();

        // Lancement JavaFX
        Application.launch(HelloApplication.class, args);
    }
}
