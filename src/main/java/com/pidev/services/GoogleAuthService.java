package com.pidev.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.pidev.ModuleAccessBootstrap;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GoogleAuthService {

    private static final String CLIENT_ID = "GOOGLE_CLIENT_ID";
    private static final String CLIENT_SECRET = "GOOGLE_CLIENT_SECRET";
    private static final String APP_NAME = "java-pidev";
    private static final int CALLBACK_PORT = 8888;
    private static final int AUTH_TIMEOUT_SECONDS = 120;

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private static final File TOKENS_DIR = new File("tokens");

    public static Userinfo connecterAvecGoogle() throws Exception {
        ModuleAccessBootstrap.enableUnnamedModuleReads();
        deconnecterGoogle();

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(CLIENT_ID);
        details.setClientSecret(CLIENT_SECRET);
        clientSecrets.setInstalled(details);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, jsonFactory, clientSecrets, SCOPES)
                .setAccessType("online")
                .build();

        Credential credential = autoriserUtilisateur(flow);

        Oauth2 oauth2Service = new Oauth2.Builder(transport, jsonFactory, credential)
                .setApplicationName(APP_NAME)
                .build();

        return oauth2Service.userinfo().get().execute();
    }

    private static Credential autoriserUtilisateur(GoogleAuthorizationCodeFlow flow) throws Exception {
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(CALLBACK_PORT)
                .setCallbackPath("/Callback")
                .build();

        ExecutorService executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());

        try {
            String redirectUri = receiver.getRedirectUri();
            GoogleAuthorizationCodeRequestUrl authorizationRequest = flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri);
            authorizationRequest.set("prompt", "select_account");
            String authorizationUrl = authorizationRequest.build();

            ouvrirNavigateur(authorizationUrl);

            Future<String> codeFuture = executor.submit(receiver::waitForCode);
            String code = attendreCodeAutorisation(codeFuture, receiver, redirectUri);

            TokenResponse response = flow.newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();

            return flow.createAndStoreCredential(response, null);
        } finally {
            try {
                receiver.stop();
            } catch (IOException e) {
                System.err.println("Impossible d'arreter le serveur OAuth local : " + e.getMessage());
            }
            executor.shutdownNow();
        }
    }

    private static String attendreCodeAutorisation(
            Future<String> codeFuture,
            LocalServerReceiver receiver,
            String redirectUri
    ) throws Exception {
        try {
            String code = codeFuture.get(AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (code == null || code.isBlank()) {
                throw new IllegalStateException(
                        "Connexion Google interrompue avant le retour local sur " + redirectUri + "."
                );
            }
            return code;
        } catch (TimeoutException e) {
            receiver.stop();
            codeFuture.cancel(true);
            throw new IllegalStateException(
                    "Connexion Google expiree apres " + AUTH_TIMEOUT_SECONDS
                            + " secondes. Verifiez que la page Google s'est ouverte et que le redirect URI "
                            + redirectUri + " est autorise dans Google Cloud.",
                    e
            );
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("Erreur pendant l'attente du retour Google.", cause);
        } catch (InterruptedException e) {
            receiver.stop();
            Thread.currentThread().interrupt();
            throw new IOException("Connexion Google interrompue pendant l'attente du callback local.", e);
        }
    }

    private static void ouvrirNavigateur(String url) throws IOException {
        System.out.println("URL OAuth Google : " + url);

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI.create(url));
                    return;
                }
            } catch (Exception e) {
                System.err.println("Ouverture Desktop du navigateur impossible : " + e.getMessage());
            }
        }

        ouvrirNavigateurExterne(url);
    }

    private static void ouvrirNavigateurExterne(String url) throws IOException {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        try {
            if (osName.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
                return;
            }
            if (osName.contains("mac")) {
                new ProcessBuilder("open", url).start();
                return;
            }
            if (osName.contains("nix") || osName.contains("nux")) {
                new ProcessBuilder("xdg-open", url).start();
                return;
            }
        } catch (IOException e) {
            throw new IOException(
                    "Impossible d'ouvrir automatiquement le navigateur. Ouvrez l'URL affichee dans la console.",
                    e
            );
        }

        throw new IOException(
                "Navigateur non supporte automatiquement. Ouvrez l'URL affichee dans la console."
        );
    }

    public static void deconnecterGoogle() {
        try {
            if (TOKENS_DIR.exists()) {
                File[] fichiers = TOKENS_DIR.listFiles();
                if (fichiers == null) {
                    return;
                }
                for (File fichier : fichiers) {
                    if (!fichier.delete()) {
                        System.err.println("Impossible de supprimer le token Google : " + fichier.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur deconnexion Google : " + e.getMessage());
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "google-oauth-callback");
            thread.setDaemon(true);
            return thread;
        }
    }
}
