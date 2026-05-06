package com.pidev.tools;

import javafx.application.Platform;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GestureServer {

    private static final int PORT = 7654;

    private volatile boolean running;
    private Thread serverThread;
    private ServerSocket serverSocket;
    private Robot robot;

    public GestureServer() {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            System.err.println("GestureServer: Robot init failed: " + ex.getMessage());
            robot = null;
        }
    }

    public void startServer() {
        if (running) {
            return;
        }
        running = true;
        serverThread = new Thread(this::runServer, "GestureServer");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public void stopServer() {
        running = false;
        if (serverThread != null) {
            serverThread.interrupt();
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void runServer() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            serverSocket = server;
            System.out.println("GestureServer listening on port " + PORT);

            while (running) {
                try (Socket client = server.accept();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {

                    System.out.println("GestureServer client connected");

                    String line;
                    while (running && (line = reader.readLine()) != null) {
                        String message = line.trim();
                        if (message.isEmpty()) {
                            continue;
                        }
                        System.out.println("Gesture message: " + message);
                        handleMessage(message);
                    }
                } catch (IOException ex) {
                    if (running) {
                        System.err.println("GestureServer client error: " + ex.getMessage());
                    }
                }
            }
        } catch (IOException ex) {
            if (running) {
                System.err.println("GestureServer server error: " + ex.getMessage());
            }
        }
    }

    private void handleMessage(String message) {
        if (message.startsWith("MOUSE_MOVE")) {
            String[] parts = message.split("\\s+");
            if (parts.length >= 3) {
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    if (robot != null) {
                        robot.mouseMove(x, y);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            return;
        }

        if ("CLICK".equalsIgnoreCase(message)) {
            if (robot != null) {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            }
            return;
        }

        if ("NAVIGATE_NEXT".equalsIgnoreCase(message)) {
            safeRunLater(() -> {
                System.out.println("Next Page");
                // TODO: call your navigation method here.
            });
            return;
        }

        if ("NAVIGATE_PREV".equalsIgnoreCase(message)) {
            safeRunLater(() -> {
                System.out.println("Previous Page");
                // TODO: call your navigation method here.
            });
        }
    }

    private void safeRunLater(Runnable action) {
        try {
            Platform.runLater(action);
        } catch (IllegalStateException ex) {
            System.err.println("GestureServer: JavaFX not ready for runLater");
        }
    }
}
