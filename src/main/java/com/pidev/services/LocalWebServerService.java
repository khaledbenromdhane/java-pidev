package com.pidev.services;

import com.pidev.entities.Publication;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class LocalWebServerService {

    private static LocalWebServerService instance;

    public static synchronized LocalWebServerService getInstance() {
        if (instance == null) {
            instance = new LocalWebServerService();
        }
        return instance;
    }

    private HttpServer server;
    private int port = 8080;
    private String localIp = "127.0.0.1";
    private final PublicationService publicationService = new PublicationService();

    private LocalWebServerService() {
    }

    public synchronized void startServer() {
        if (server != null) {
            return;
        }

        try {
            localIp = getLocalIpAddress();

            int[] preferredPorts = new int[]{8080, 8081, 8082, 0};
            IOException lastBindError = null;
            for (int candidatePort : preferredPorts) {
                try {
                    server = HttpServer.create(new InetSocketAddress("0.0.0.0", candidatePort), 0);
                    break;
                } catch (IOException bindError) {
                    lastBindError = bindError;
                    server = null;
                }
            }

            if (server == null) {
                throw new IOException("No available port for local web server.", lastBindError);
            }

            server.createContext("/", new HealthHandler());
            server.createContext("/publication", new PublicationHandler());
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            port = server.getAddress().getPort();
            
            System.out.println("=================================================");
            System.out.println("[LocalWebServer] SERVER STARTED!");
            System.out.println("[LocalWebServer] Local IP: " + localIp);
            System.out.println("[LocalWebServer] Port: " + port);
            System.out.println("[LocalWebServer] Base URL: " + getServerBaseUrl());
            System.out.println("=================================================");
        } catch (Exception e) {
            server = null;
            System.err.println("[LocalWebServer] FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("[LocalWebServer] Stopped.");
        }
    }

    public String getServerBaseUrl() {
        // Force inclusion of port to avoid defaulting to port 80 on mobile devices
        return "http://" + localIp + ":" + port;
    }

    public String getPublicationUrl(int publicationId) {
        String url = getServerBaseUrl() + "/publication?id=" + publicationId;
        System.out.println("[LocalWebServer] Generated QR URL for ID " + publicationId + ": " + url);
        return url;
    }

    private String getLocalIpAddress() {
        try {
            // Best effort: pick interface used for outbound traffic.
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 53);
                InetAddress best = socket.getLocalAddress();
                if (best != null) {
                    String ip = best.getHostAddress();
                    if (!ip.contains(":") && !ip.startsWith("127.")) {
                        return ip;
                    }
                }
            } catch (Exception ignored) {
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                String name = ni.getDisplayName().toLowerCase();
                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()
                        || name.contains("virtual")
                        || name.contains("vmware")
                        || name.contains("vbox")
                        || name.contains("virtualbox")
                        || name.contains("hyper-v")
                        || name.contains("host-only")
                        || name.contains("docker")
                        || name.contains("wsl")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ip = addr.getHostAddress();
                    
                    // Skip IPv6
                    if (ip.contains(":")) continue;
                    
                    // Prefer common private ranges, avoid typical gateway suffix when possible.
                    if ((ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172."))
                            && !ip.endsWith(".1")) {
                        return ip;
                    }
                }
            }

            // Last pass: accept any private IPv4.
            interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ip = addr.getHostAddress();
                    if (ip.contains(":")) continue;
                    if (ip.startsWith("10.") || ip.startsWith("172.")) {
                        return ip;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[LocalWebServer] IP Detection Error: " + e.getMessage());
        }
        return "127.0.0.1";
    }

    private final class PublicationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().toString();
                System.out.println("[LocalWebServer] Request: " + path);

                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                int id = parseId(params.get("id"));
                Publication publication = publicationService.afficherParId(id);

                String response;
                int status;
                if (publication == null) {
                    response = "<h1>Publication introuvable</h1><p>Verifiez que l'ID existe: /publication?id=1</p>";
                    status = 404;
                } else {
                    response = buildMobileHtml(publication);
                    status = 200;
                }
                writeHtml(exchange, status, response);
            } catch (Exception e) {
                System.err.println("[LocalWebServer] Error while serving /publication: " + e.getMessage());
                writeHtml(
                        exchange,
                        500,
                        "<h1>Erreur serveur local</h1><p>" + html(safe(e.getMessage(), "Erreur inconnue")) + "</p>"
                );
            }
        }

        private int parseId(String rawId) {
            if (rawId == null || rawId.trim().isEmpty()) {
                return -1;
            }
            try {
                return Integer.parseInt(rawId);
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> map = new HashMap<>();
            if (query == null || query.trim().isEmpty()) {
                return map;
            }
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    String key = decode(pair.substring(0, idx));
                    String value = decode(pair.substring(idx + 1));
                    map.put(key, value);
                }
            }
            return map;
        }

        private String decode(String value) {
            try {
                return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                return value;
            }
        }

        private String buildMobileHtml(Publication publication) {
            String titre = html(safe(publication.getTitre(), "Sans titre"));
            String description = html(safe(publication.getDescription(), "Aucune description."));
            String likes = String.valueOf(publication.getNbLikes());
            String dislikes = String.valueOf(publication.getNbDislikes());
            String publishedAt = publication.getDateAct() == null
                    ? "Date inconnue"
                    : publication.getDateAct().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String imageUrl = safe(publication.getImage(), "");

            String heroStyle = "background: linear-gradient(135deg, #d4af37, #b8860b); display:flex; align-items:center; justify-content:center;";
            String heroContent = "<span style='font-size:60px;'>📰</span>";
            if (!imageUrl.trim().isEmpty() && !"NULL".equalsIgnoreCase(imageUrl)) {
                heroStyle = "background-image: url('" + html(imageUrl) + "'); background-size: cover; background-position: center;";
                heroContent = "";
            }

            return "<!DOCTYPE html>"
                    + "<html lang='fr'>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>"
                    + "<title>" + titre + " - PIDEV</title>"
                    + "<style>"
                    + "body { margin:0; padding:0; background:#f8f9fa; color:#2d3436; font-family:'Segoe UI', Roboto, Helvetica, sans-serif; }"
                    + ".hero { width:100%; height:250px; " + heroStyle + " position:relative; border-bottom:3px solid #d4af37; }"
                    + ".hero-overlay { position:absolute; bottom:0; left:0; right:0; padding:30px 20px 20px; background:linear-gradient(to top, rgba(255,255,255,0.9) 0%, transparent 100%); }"
                    + ".title { margin:0; font-size:28px; font-weight:800; color:#1a1a1a; text-shadow:0 1px 3px rgba(255,255,255,0.8); }"
                    + ".content { padding:25px 20px; background:white; border-radius:30px 30px 0 0; margin-top:-30px; position:relative; box-shadow: 0 -10px 20px rgba(0,0,0,0.05); }"
                    + ".desc { font-size:16px; line-height:1.7; color:#444; margin-bottom:25px; }"
                    + ".meta { color:#d4af37; font-size:14px; margin-bottom:15px; font-weight:700; text-transform: uppercase; letter-spacing: 1px; }"
                    + ".stats-grid { display:grid; grid-template-columns:1fr 1fr; gap:15px; }"
                    + ".stat-card { background:#ffffff; border:1px solid #eee; border-radius:15px; padding:15px; text-align:center; box-shadow:0 5px 15px rgba(0,0,0,0.05); }"
                    + ".stat-label { font-size:11px; text-transform:uppercase; color:#888; margin:0 0 5px 0; font-weight: bold; }"
                    + ".stat-value { font-size:18px; font-weight:bold; margin:0; color: #1a1a1a; }"
                    + ".footer { text-align:center; padding:30px 20px; color:#999; font-size:12px; background:#f8f9fa; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='hero'>"
                    + heroContent
                    + "<div class='hero-overlay'><h1 class='title'>" + titre + "</h1></div>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p class='meta'>Publié le " + html(publishedAt) + "</p>"
                    + "<p class='desc'>" + description + "</p>"
                    + "<div class='stats-grid'>"
                    + "<div class='stat-card'><p class='stat-label'>Likes</p><p class='stat-value' style='color:#2ecc71;'>" + html(likes) + " 👍</p></div>"
                    + "<div class='stat-card'><p class='stat-label'>Dislikes</p><p class='stat-value' style='color:#e74c3c;'>" + html(dislikes) + " 👎</p></div>"
                    + "</div>"
                    + "</div>"
                    + "<div class='footer'>© " + java.time.Year.now().getValue() + " PIDEV Art Site • Premium Client View</div>"
                    + "</body>"
                    + "</html>";
        }

        private String safe(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }

        private String html(String value) {
            return value
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }
    }

    private final class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = "<h1>Local server is running</h1>"
                    + "<p>Try: <a href='/publication?id=1'>/publication?id=1</a></p>"
                    + "<p>Base URL: " + html(getServerBaseUrl()) + "</p>";
            writeHtml(exchange, 200, body);
        }
    }

    private void writeHtml(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] body = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private String html(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
