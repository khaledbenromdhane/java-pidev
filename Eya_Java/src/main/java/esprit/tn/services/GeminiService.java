package esprit.tn.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class GeminiService {

    // L'API Groq (Llama 3) est une excellente alternative gratuite et extrêmement rapide.
    private static final String API_KEY = "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";
    private final HttpClient httpClient;
    private final Random random = new Random();

    public GeminiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<List<String>> generateDiverseQuestionsAsync(String titre, String contenu) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Utilisation du temps actuel + random pour une variabilité maximale
                long seed = System.currentTimeMillis() + random.nextInt(1000);
                String prompt = "Tu es un professeur expert. Crée un examen de 5 questions basé UNIQUEMENT sur ce contenu : '" + titre + " - " + contenu + "'.\n" +
                        "TU DOIS DISTRIBUER EXACTEMENT 20 POINTS AU TOTAL (ex: 4pts par question).\n\n" +
                        "FORMAT STRICT PAR LIGNE :\n" +
                        "[TYPE - X pts] : La question\n\n" +
                        "Types autorisés : VRAI/FAUX, OUVERTE, DEFINITION.\n" +
                        "Exemple :\n" +
                        "[VRAI/FAUX - 4 pts] : Java est-il un langage compilé ?\n" +
                        "[OUVERTE - 4 pts] : Expliquez le polymorphisme.\n\n" +
                        "CONSIGNES :\n" +
                        "- Pas de numéros (1, 2, 3...).\n" +
                        "- Pas d'introduction ni de conclusion.\n" +
                        "- Chaque question doit avoir ses points [X pts].\n" +
                        "- Variabilité : " + seed;

                String result = callApi(prompt, 0.9);
                System.out.println("DEBUG GROQ QUESTIONS: \n" + result);
                return result;
            } catch (Exception e) { 
                System.err.println("ERREUR GENERATION API: " + e.getMessage());
                e.printStackTrace();
                return ""; 
            }
        }).thenApply(this::parseQuestions);
    }

    public CompletableFuture<Integer> calculateScoreOnlyAsync(String titre, String contenu, String reponsesUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = "Tu es un correcteur d'examen rigoureux.\n" +
                        "CONTENU DE RÉFÉRENCE : " + contenu + "\n\n" +
                        "TRAVAIL DE L'ÉTUDIANT (Questions et Réponses) :\n" + reponsesUser + "\n\n" +
                        "CONSIGNES :\n" +
                        "1. Analyse chaque réponse de l'étudiant.\n" +
                        "2. Pour chaque question, attribue des points selon le barème indiqué dans le crochet [X pts].\n" +
                        "3. Somme tous les points pour obtenir une note sur 20.\n\n" +
                        "FORMAT DE RÉPONSE OBLIGATOIRE :\n" +
                        "Donne uniquement le score final sous la forme <SCORE>X</SCORE>.\n" +
                        "Exemple: <SCORE>15.5</SCORE>";
                
                String res = callApi(prompt, 0.1);
                System.out.println("DEBUG SCORE - Réponse IA brute: [" + res + "]");
                
                int score = 0;
                // Extraction via les balises <SCORE>
                java.util.regex.Pattern tagPattern = java.util.regex.Pattern.compile("<SCORE>(.*?)</SCORE>");
                java.util.regex.Matcher tagMatcher = tagPattern.matcher(res);
                
                if (tagMatcher.find()) {
                    String scoreStr = tagMatcher.group(1).replaceAll("[^0-9.]", "").trim();
                    if (!scoreStr.isEmpty()) {
                        double doubleScore = Double.parseDouble(scoreStr);
                        score = (int) Math.round(doubleScore);
                        System.out.println("DEBUG SCORE - Score extrait (balises): " + score);
                        return Math.max(0, Math.min(score, 20));
                    }
                }

                // Fallback: ancienne méthode si les balises manquent
                String cleanedRes = res.replaceAll("[^0-9./,]", " ").trim();
                if (cleanedRes.contains("/")) cleanedRes = cleanedRes.split("/")[0].trim();
                cleanedRes = cleanedRes.replace(",", ".");
                
                java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("(\\d+(\\.\\d+)?)");
                java.util.regex.Matcher numMatcher = numPattern.matcher(cleanedRes);
                
                // On cherche le DERNIER nombre si les balises ont échoué (souvent le total)
                double lastFound = -1;
                while (numMatcher.find()) {
                    lastFound = Double.parseDouble(numMatcher.group(1));
                }
                
                if (lastFound != -1) {
                    score = (int) Math.round(lastFound);
                    System.out.println("DEBUG SCORE - Score extrait (fallback dernier nombre): " + score);
                }
                
                return Math.max(0, Math.min(score, 20));
            } catch (Exception e) {
                System.err.println("DEBUG SCORE - Erreur critique: " + e.getMessage());
                return 0;
            }
        });
    }

    public CompletableFuture<String> askQuestionAsync(String userQuestion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = "Tu es un assistant intelligent intégré à une plateforme d'apprentissage (e-learning). " +
                        "Réponds de manière concise et professionnelle à la question suivante de l'étudiant : '" + userQuestion + "'";
                String response = callApi(prompt, 0.7);
                return response.isEmpty() ? "Je n'ai pas pu obtenir de réponse. Veuillez réessayer." : response;
            } catch (Exception e) {
                return "Désolé, je rencontre une erreur technique : " + e.getMessage();
            }
        });
    }

    private String callApi(String prompt, double temperature) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("model", "llama-3.1-8b-instant");
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);
        
        payload.put("messages", messages);
        payload.put("temperature", temperature);

        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject res = new JSONObject(response.body());
            if (res.has("choices") && !res.getJSONArray("choices").isEmpty()) {
                return res.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content").trim();
            }
        } else {
            System.err.println("Erreur API Groq: " + response.statusCode() + " - " + response.body());
        }
        return "";
    }

    private List<String> parseQuestions(String text) {
        List<String> questions = new ArrayList<>();
        if (text == null || text.isEmpty()) return getDefaultQuestions("Formation (API vide)");
        
        for (String line : text.split("\n")) {
            // Nettoyer les listes (1., 2., *, -)
            line = line.replaceAll("^[0-9]+\\.\\s*", "").trim();
            line = line.replaceAll("^[-*]\\s*", "").trim();
            
            if (!line.isEmpty() && line.length() > 5 && !line.toLowerCase().startsWith("voici") && !line.toLowerCase().startsWith("ces questions")) {
                if (!line.contains("[")) {
                    line = "[OUVERTE - 4 pts] : " + line; // Forcer un type et points par défaut
                } else if (!line.contains("pts")) {
                    // Si le type est là mais pas les points, on les ajoute proprement
                    int closingBracket = line.indexOf("]");
                    if (closingBracket != -1) {
                        String type = line.substring(1, closingBracket);
                        String rest = line.substring(closingBracket + 1);
                        line = "[" + type + " - 4 pts]" + rest;
                    }
                }
                questions.add(line);
            }
        }
        
        System.out.println("DEBUG PARSED QUESTIONS: " + questions.size() + " trouvées.");
        return questions.size() >= 3 ? questions : getDefaultQuestions("Formation (Parse echoué)");
    }

    private List<String> getDefaultQuestions(String titre) {
        List<String> q = new ArrayList<>();
        q.add("[OUVERTE - 4 pts] : Objectif de " + titre);
        q.add("[VRAI/FAUX - 4 pts] : Est-ce utile ?");
        q.add("[DEFINITION - 4 pts] : Terme clé ?");
        q.add("[OUVERTE - 4 pts] : Avantage ?");
        q.add("[VRAI/FAUX - 4 pts] : Simple ?");
        return q;
    }
}
