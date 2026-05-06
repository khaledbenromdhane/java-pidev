package com.pidev.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service to generate event descriptions using Groq API.
 */
public class GeminiService {
    private static final String API_KEY = System.getenv("GROQ_API_KEY");
private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
private static final String MODEL = "llama3-8b-8192";

    

    private final HttpClient httpClient;

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Generates an event description based on the provided event details.
     */
    public String generateDescription(String nom, String type, String lieu,
                                       String date, String heure, int nbParticipants) throws Exception {

        String prompt = buildPrompt(nom, type, lieu, date, heure, nbParticipants);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("messages", new JSONArray().put(message));
        requestBody.put("max_tokens", 500);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API error (HTTP " + response.statusCode() + "): " + response.body());
        }

        return extractText(response.body());
    }

    private String buildPrompt(String nom, String type, String lieu,
                                String date, String heure, int nbParticipants) {
        StringBuilder sb = new StringBuilder();
        sb.append("Génère une description professionnelle et attrayante pour un événement avec les détails suivants:\n");
        if (nom != null && !nom.isBlank()) {
            sb.append("- Nom: ").append(nom).append("\n");
        }
        if (type != null && !type.isBlank()) {
            sb.append("- Type: ").append(type).append("\n");
        }
        if (lieu != null && !lieu.isBlank()) {
            sb.append("- Lieu: ").append(lieu).append("\n");
        }
        if (date != null && !date.isBlank()) {
            sb.append("- Date: ").append(date).append("\n");
        }
        if (heure != null && !heure.isBlank()) {
            sb.append("- Heure: ").append(heure).append("\n");
        }
        if (nbParticipants > 0) {
            sb.append("- Nombre de participants: ").append(nbParticipants).append("\n");
        }
        sb.append("\nLa description doit être en français, entre 3 et 5 phrases, engageante et professionnelle. ");
        sb.append("Ne retourne QUE la description, sans titre ni formatage markdown.");
        return sb.toString();
    }

    private String extractText(String jsonResponse) {
        JSONObject root = new JSONObject(jsonResponse);
        JSONArray choices = root.getJSONArray("choices");
        if (choices.isEmpty()) {
            throw new RuntimeException("No choices returned from API.");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        return message.getString("content").trim();
    }
}
