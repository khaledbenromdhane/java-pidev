package com.pidev;

import com.pidev.tools.GestureServer;
import com.pidev.tools.myconnexion;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Test connexion BD
        myconnexion.getInstance();

        GestureServer gestureServer = new GestureServer();
        gestureServer.startServer();
        try {
            Application.launch(HelloApplication.class, args);
        } finally {
            gestureServer.stopServer();
        }
    }
}
