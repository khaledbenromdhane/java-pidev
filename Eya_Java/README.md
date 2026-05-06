# 🎓 E-Learning Dashboard Management System

Ce projet est une application de bureau performante développée en **JavaFX**, conçue pour la gestion complète des formations et des évaluations. Elle intègre des fonctionnalités avancées d'intelligence artificielle pour automatiser la création de quiz et l'évaluation des étudiants, ainsi que des outils modernes comme les QR Codes et un Assistant IA.

---

## 🚀 APIs Utilisées

Le projet s'appuie sur plusieurs APIs et bibliothèques clés :

1.  **Groq API (Llama 3.1)** : 
    - Utilisation de l'intelligence artificielle pour la génération dynamique de questions.
    - Analyse automatique des réponses des étudiants pour le calcul des notes.
    - **Nouveau :** Moteur de réponse pour l'Assistant Chatbot IA.
2.  **iText PDF API** :
    - Génération de certificats de réussite professionnels au format PDF.
3.  **ZXing API (QR Code)** :
    - **Nouveau :** Génération de QR Codes dynamiques pour accéder aux détails des formations.
4.  **MySQL Connector** :
    - Gestion de la persistance des données pour les formations et les évaluations.
5.  **JavaFX (FXML, Controls, Graphics)** :
    - Interface utilisateur moderne, réactive et ergonomique (Thème Dark & Gold).

---

## 🛠️ Guide d'Intégration Technique des APIs

Voici comment les différentes APIs sont techniquement intégrées dans le code source :

### 1. Intelligence Artificielle (Groq / Llama 3.1)
L'intégration est faite via le service **[`GeminiService.java`](file:///c:/Users/HP/OneDrive%20-%20ESPRIT/Desktop/Eya%20Java%20fix/Eya_Java/src/main/java/esprit/tn/services/GeminiService.java)** :
*   **Technologie** : Utilisation de `java.net.http.HttpClient` pour des requêtes HTTP asynchrones.
*   **Format** : Les requêtes sont envoyées en JSON (via `org.json`) au endpoint de Groq.
*   **Asynchronisme** : Utilisation de `CompletableFuture` pour ne pas bloquer l'interface JavaFX pendant les appels réseaux.

### 2. QR Codes (ZXing)
L'intégration se trouve dans **[`QRCodeService.java`](file:///c:/Users/HP/OneDrive%20-%20ESPRIT/Desktop/Eya%20Java%20fix/Eya_Java/src/main/java/esprit/tn/services/QRCodeService.java)** :
*   **Génération** : Utilisation de `MultiFormatWriter` pour encoder le texte en `BitMatrix`.
*   **Conversion** : La matrice est convertie en `BufferedImage` puis en `javafx.scene.image.Image` via `SwingFXUtils` pour être affichée directement dans une `ImageView`.

### 3. Certificats PDF (iText)
L'intégration est gérée par **[`CertificateGenerator.java`](file:///c:/Users/HP/OneDrive%20-%20ESPRIT/Desktop/Eya%20Java%20fix/Eya_Java/src/main/java/esprit/tn/services/CertificateGenerator.java)** :
*   **Création** : Utilisation de la classe `Document` d'iText.
*   **Stylisation** : Ajout de paragraphes formatés, de polices personnalisées et de marges pour créer un rendu professionnel.
*   **Système de fichiers** : Création automatique du dossier `Certificats_Eya/` sur le bureau de l'utilisateur via `System.getProperty("user.home")`.

---

## 🛠️ Fonctions Principales (Services)

### 🤖 GeminiService (Intégration IA Groq)
Ce service gère toute l'intelligence du système :
*   `generateDiverseQuestionsAsync(String titre, String contenu)` : Génère instantanément 5 questions variées basées sur le contenu d'une formation.
*   `calculateScoreOnlyAsync(String titre, String contenu, String reponsesUser)` : Évalue la pertinence des réponses et retourne une note précise sur 20.
*   **Nouveau :** `askQuestionAsync(String userQuestion)` : Permet au chatbot de répondre à n'importe quelle question de l'utilisateur de manière fluide.

### 📱 QRCodeService
*   **Nouveau :** `generateQRCodeImage(String text, int width, int height)` : Transforme les données d'une formation en une image QR Code pour un accès rapide via mobile.

### 📜 CertificateGenerator
*   `generateCertificate(String studentName, String formationName, int score)` : Crée un fichier PDF personnalisé sur le bureau (`Certificats_Eya/`).

### 📚 ServiceFormation (CRUD Formations)
*   `addFormation(formation f)` / `getAllFormations()` / `updateFormation(formation f)` / `deleteFormation(int id)`.

---

## 🆕 Nouvelles Fonctionnalités Premium

1.  **Assistant Chatbot IA** : Accessible via l'icône 💬 dans le tableau de bord, il offre une assistance interactive en temps réel aux utilisateurs.
2.  **Accès par QR Code** : Chaque formation peut désormais générer son propre QR Code unique (icône 📱 dans la liste), facilitant le partage et l'accès aux informations.
3.  **UI Modernisée** : Design haut de gamme avec effets de survol (hover), gestion asynchrone des tâches et thématique cohérente.

---

## 💻 Stack Technique
*   **Langage** : Java 17
*   **Interface** : JavaFX 17
*   **Gestionnaire** : Maven
*   **Base de données** : MySQL 8.0

---

## 📂 Structure du Projet
*   `esprit.tn.controllers` : Logique de l'interface utilisateur.
*   `esprit.tn.models` : Classes de données (Formation, Evaluation).
*   `esprit.tn.services` : Logique métier et appels API.
*   `esprit.tn.utils` : Utilitaires (Connexion DB).
*   `src/main/resources` : Fichiers FXML et styles CSS.

---

## 🏃 Comment lancer le projet ?

### Prérequis
*   Java JDK 17
*   MySQL Server
*   Maven

### Étapes
1.  **Base de données** : Importez le schéma SQL.
2.  **Installation** : `mvn clean install`
3.  **Lancement UI** : `mvn javafx:run`
4.  **Mode Console** : `mvn exec:java -Dexec.mainClass="esprit.tn.Main"`
