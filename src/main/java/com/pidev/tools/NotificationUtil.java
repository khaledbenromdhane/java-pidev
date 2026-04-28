package com.pidev.tools;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class NotificationUtil {

    private static final String CARD_STYLE =
            "-fx-background-color: rgba(255,255,255,0.96);"
                    + "-fx-border-color: #D4AF37;"
                    + "-fx-border-width: 2;"
                    + "-fx-background-radius: 10;"
                    + "-fx-border-radius: 10;"
                    + "-fx-padding: 10 14;";

    private static final String TITLE_STYLE =
            "-fx-text-fill: #6c5600;"
                    + "-fx-font-size: 14;"
                    + "-fx-font-weight: bold;";

    private static final String MESSAGE_STYLE =
            "-fx-text-fill: #222222;"
                    + "-fx-font-size: 12;";

    public static void showSuccess(String title, String message) {
        showToast(title, message, Duration.seconds(5), false, 0, false);
    }

    public static void showError(String title, String message) {
        showToast(title, message, Duration.seconds(5), false, 0, true);
    }

    public static void showModerationStorm(int badWordsCount, String message) {
        int totalNotifs;
        if (badWordsCount >= 5) {
            totalNotifs = 10;
        } else if (badWordsCount >= 2) {
            totalNotifs = 3;
        } else {
            totalNotifs = 1;
        }

        for (int i = 0; i < totalNotifs; i++) {
            int delayMs = i * 350;
            showToast("Moderation", message, Duration.seconds(5), true, delayMs, true);
        }
    }

    private static void showToast(
            String title,
            String message,
            Duration duration,
            boolean randomPosition,
            int delayMs,
            boolean warning
    ) {
        Runnable showTask = () -> {
            Label titleLabel = new Label((warning ? "⚠ " : "✓ ") + title);
            titleLabel.setStyle(TITLE_STYLE);

            Label messageLabel = new Label(message);
            messageLabel.setStyle(MESSAGE_STYLE);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(280);

            VBox root = new VBox(6, titleLabel, messageLabel);
            root.setStyle(CARD_STYLE);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            Stage toast = new Stage(StageStyle.TRANSPARENT);
            toast.setAlwaysOnTop(true);
            toast.setScene(scene);
            toast.setResizable(false);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double x;
            double y;
            if (randomPosition) {
                x = bounds.getMinX() + 40 + Math.random() * Math.max(1, bounds.getWidth() - 360);
                y = bounds.getMinY() + 40 + Math.random() * Math.max(1, bounds.getHeight() - 220);
            } else {
                x = bounds.getMaxX() - 350;
                y = bounds.getMinY() + 40;
            }

            toast.setX(x);
            toast.setY(y);
            toast.show();

            PauseTransition hide = new PauseTransition(duration);
            hide.setOnFinished(evt -> toast.close());
            hide.play();
        };

        Runnable execute = () -> {
            if (delayMs <= 0) {
                showTask.run();
                return;
            }
            PauseTransition delay = new PauseTransition(Duration.millis(delayMs));
            delay.setOnFinished(e -> showTask.run());
            delay.play();
        };

        if (Platform.isFxApplicationThread()) {
            execute.run();
        } else {
            Platform.runLater(execute);
        }
    }
}
