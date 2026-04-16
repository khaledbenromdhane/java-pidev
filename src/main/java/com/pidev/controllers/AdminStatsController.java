package com.pidev.controllers;

import com.pidev.entities.*;
import com.pidev.services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminStatsController {

    @FXML private Label totalPubsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label totalCommentsLabel;
    
    @FXML private BarChart<String, Number> contributorsChart;
    @FXML private PieChart userRatioChart;
    @FXML private BarChart<String, Number> engagementChart;

    private final PublicationService pubService = new PublicationService();
    private final CrudService userService = new CrudService();
    private final CommentaireService comService = new CommentaireService();

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    public void refresh() {
        List<Publication> pubs = pubService.afficher();
        List<User> users = userService.afficher();
        List<Commentaire> allComments = comService.afficher();

        // 1. Update Labels
        totalPubsLabel.setText(String.valueOf(pubs.size()));
        totalUsersLabel.setText(String.valueOf(users.stream().filter(u -> !"banned".equals(u.getStatus())).count()));
        bannedUsersLabel.setText(String.valueOf(users.stream().filter(u -> "banned".equals(u.getStatus())).count()));
        totalCommentsLabel.setText(String.valueOf(allComments.size()));

        // 2. User Ratio PieChart
        userRatioChart.getData().clear();
        long activeCount = users.stream().filter(u -> !"banned".equals(u.getStatus())).count();
        long bannedCount = users.stream().filter(u -> "banned".equals(u.getStatus())).count();
        userRatioChart.getData().add(new PieChart.Data("Actifs", activeCount));
        userRatioChart.getData().add(new PieChart.Data("Bannis", bannedCount));

        // 3. Top Contributors BarChart
        contributorsChart.getData().clear();
        XYChart.Series<String, Number> contributorSeries = new XYChart.Series<>();
        contributorSeries.setName("Publications par utilisateur");
        
        Map<Integer, Long> pubCountByUser = pubs.stream()
                .collect(Collectors.groupingBy(Publication::getIdUser, Collectors.counting()));
        
        pubCountByUser.entrySet().stream()
                .limit(7)
                .forEach(entry -> {
                    User u = userService.afficherParId(entry.getKey());
                    String name = (u != null) ? u.getNom() : "ID: " + entry.getKey();
                    contributorSeries.getData().add(new XYChart.Data<>(name, entry.getValue()));
                });
        contributorsChart.getData().add(contributorSeries);

        // 4. Engagement BarChart (Likes vs Dislikes for top 5 pubs)
        engagementChart.getData().clear();
        XYChart.Series<String, Number> likesSeries = new XYChart.Series<>();
        likesSeries.setName("Likes");
        XYChart.Series<String, Number> dislikesSeries = new XYChart.Series<>();
        dislikesSeries.setName("Dislikes");

        pubs.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getNbLikes() + p2.getNbDislikes(), p1.getNbLikes() + p1.getNbDislikes()))
                .limit(5)
                .forEach(p -> {
                    String title = p.getTitre();
                    if (title.length() > 10) title = title.substring(0, 10) + "...";
                    likesSeries.getData().add(new XYChart.Data<>(title, p.getNbLikes()));
                    dislikesSeries.getData().add(new XYChart.Data<>(title, p.getNbDislikes()));
                });
        
        engagementChart.getData().addAll(likesSeries, dislikesSeries);
    }

    @FXML
    private void retour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/pidev/AdminDashboard.fxml"));
            Stage stage = (Stage) totalPubsLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
