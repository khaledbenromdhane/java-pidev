package com.pidev.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeminiVisionUtil {
    private static final String API_KEY = "xxxxxxxxxxxxxxx-xxxxxxxxxxxxxxxxxxxxx";
    // Using gemini-1.5-flash as it is the correct stable vision model
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
            + API_KEY;

    public static String analyzeImage(String base64Image) {
        String prompt = "Analyze this image and provide a Title and a Description for a blog post. Format your response exactly like this:\\nTITLE: [your title here]\\nDESCRIPTION: [your description here]";
        return analyzeImageWithPrompt(base64Image, prompt);
    }

    public static String analyzeImageWithPrompt(String base64Image, String prompt) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                URL url = new URL(ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(15000); // 15 seconds timeout
                conn.setReadTimeout(15000); // 15 seconds timeout
                conn.setDoOutput(true);

                String jsonPayload = "{\n" +
                        "  \"contents\": [\n" +
                        "    {\n" +
                        "      \"parts\": [\n" +
                        "        {\n" +
                        "          \"text\": \"" + escapeJson(prompt) + "\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"inline_data\": {\n" +
                        "            \"mime_type\": \"image/jpeg\",\n" +
                        "            \"data\": \"" + base64Image + "\"\n" +
                        "          }\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("Gemini API Error (" + responseCode + ") on attempt " + attempt);
                    if (conn.getErrorStream() != null) {
                        Scanner errorScanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8.name());
                        String errorResponse = errorScanner.useDelimiter("\\A").hasNext()
                                ? errorScanner.useDelimiter("\\A").next()
                                : "";
                        System.err.println(errorResponse);
                        errorScanner.close();
                    }
                    
                    if (responseCode == 503 && attempt < maxRetries) {
                        System.out.println("High demand... retrying in 2 seconds.");
                        Thread.sleep(2000);
                        continue;
                    }
                    return null;
                }

                Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                JsonArray candidates = jsonObject.getAsJsonArray("candidates");
                if (candidates != null && candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject content = firstCandidate.getAsJsonObject("content");
                    JsonArray parts = content.getAsJsonArray("parts");
                    if (parts != null && parts.size() > 0) {
                        return parts.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
                return null;

            } catch (Exception e) {
                System.err.println("Exception on attempt " + attempt + ": " + e.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    public static boolean isSensitiveOrViolentImage(String base64Image) {
        String moderationPrompt =
                "You are a strict moderation assistant. Analyze this image for sensitive content. " +
                "If it contains violence, blood, gore, weapons in threatening context, nudity, sexual content, " +
                "abuse, hate symbols, or shocking content, answer exactly UNSAFE. Otherwise answer exactly SAFE.";
        String result = analyzeImageWithPrompt(base64Image, moderationPrompt);
        if (result == null) {
            return true;
        }
        return result.trim().toUpperCase().contains("UNSAFE");
    }

    public static String getBase64FromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            if (conn.getResponseCode() != 200) return null;
            
            try (java.io.InputStream is = conn.getInputStream();
                 java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (Exception e) {
            System.err.println("Error downloading image: " + e.getMessage());
            return null;
        }
    }

    public static String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        int maxRetries = 2;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                URL url = new URL(ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);

                String prompt = "Translate the following text to " + targetLanguage
                        + ". Return only the translated text with no explanation.\n\n" + text;
                String jsonPayload = "{\n" +
                        "  \"contents\": [\n" +
                        "    {\n" +
                        "      \"parts\": [\n" +
                        "        {\n" +
                        "          \"text\": \"" + escapeJson(prompt) + "\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                if (conn.getResponseCode() != 200) {
                    if (attempt == maxRetries) {
                        return text;
                    }
                    continue;
                }

                Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                JsonArray candidates = jsonObject.getAsJsonArray("candidates");
                if (candidates != null && candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject content = firstCandidate.getAsJsonObject("content");
                    JsonArray parts = content.getAsJsonArray("parts");
                    if (parts != null && parts.size() > 0) {
                        return parts.get(0).getAsJsonObject().get("text").getAsString().trim();
                    }
                }
            } catch (Exception ignored) {
                if (attempt == maxRetries) {
                    return text;
                }
            }
        }
        return text;
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
