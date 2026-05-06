package com.pidev.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * Utility class to handle FXML navigation across the application.
 * Loads a new FXML file and replaces the current scene.
 */
public class NavigationHelper {

    // ── Admin Backoffice paths ──
    public static final String ADMIN_EVENEMENTS = "/com/pidev/backoffice/admin-evenements.fxml";
    public static final String ADMIN_PARTICIPATIONS = "/com/pidev/backoffice/admin-participations.fxml";

    // ── Frontoffice Événement paths ──
    public static final String EVT_LIST = "/com/pidev/views/evenement-list.fxml";
    public static final String EVT_SHOW = "/com/pidev/evenement/evenement-show.fxml";
    public static final String EVT_NEW = "/com/pidev/evenement/evenement-form.fxml";
    public static final String EVT_EDIT = "/com/pidev/evenement/evenement-edit.fxml";

    // ── Frontoffice Participation paths ──
    public static final String PART_LIST = "/com/pidev/participation/participation-list.fxml";
    public static final String PART_SHOW = "/com/pidev/participation/participation-show.fxml";
    public static final String PART_NEW = "/com/pidev/participation/participation-new.fxml";
    public static final String PART_EDIT = "/com/pidev/participation/participation-edit.fxml";

    /**
     * Navigate to a new FXML view, replacing the current scene.
     *
     * @param sourceNode any Node in the current scene (used to get the Stage)
     * @param fxmlPath   the resource path of the FXML to load
     */
    public static void navigateTo(Node sourceNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationHelper.class.getResource(fxmlPath)
            );
            Parent root = loader.load();
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.setFill(javafx.scene.paint.Color.web("#0b0b12"));
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Navigation error → " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Navigate from an ActionEvent (button click).
     */
    public static void navigateTo(ActionEvent event, String fxmlPath) {
        Node source = (Node) event.getSource();
        navigateTo(source, fxmlPath);
    }

    /**
     * Get the FXMLLoader for a path (useful when you need to pass data to controller).
     */
    public static FXMLLoader getLoader(String fxmlPath) {
        return new FXMLLoader(NavigationHelper.class.getResource(fxmlPath));
    }
}
