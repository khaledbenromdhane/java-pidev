package esprit.tn;

import esprit.tn.controllers.DashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Classe de test pour charger et afficher le dashboard FXML
 * Permet de vérifier que le fichier dashboard.fxml se charge correctement
 *
 * @author ESPRIT
 * @version 1.0
 */
public class DashboardTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML
            System.out.println("🔄 Chargement du fichier dashboard.fxml...");

            FXMLLoader loader = new FXMLLoader();
            
            // Essayer plusieurs emplacements possibles du fichier FXML
            URL fxmlLocation = null;
            
            // Essai 1: Chemin standard Maven
            fxmlLocation = getClass().getResource("/dashboard.fxml");
            if (fxmlLocation != null) {
                System.out.println("✓ FXML trouvé dans /com/pidev/");
            }
            
            // Essai 2: Chemin alternatif
            if (fxmlLocation == null) {
                fxmlLocation = getClass().getResource("/dashboard.fxml");
                if (fxmlLocation != null) {
                    System.out.println("✓ FXML trouvé dans racine");
                }
            }
            
            // Essai 3: Depuis le classpath
            if (fxmlLocation == null) {
                fxmlLocation = ClassLoader.getSystemResource("dashboard.fxml");
                if (fxmlLocation != null) {
                    System.out.println("✓ FXML trouvé via SystemClassLoader");
                }
            }
            
            if (fxmlLocation == null) {
                throw new Exception("Impossible de trouver le fichier dashboard.fxml");
            }
            
            loader.setLocation(fxmlLocation);
            Parent root = loader.load();

            // Récupérer le contrôleur
            DashboardController controller = loader.getController();
            System.out.println("✅ Contrôleur DashboardController chargé avec succès");

            // Créer la scène
            Scene scene = new Scene(root, 1000, 700);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Dashboard - Gestion des Formations et Évaluations");

            // Configuration de la fenêtre
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Afficher la fenêtre
            primaryStage.show();

            System.out.println("✅ Dashboard FXML chargé et affiché avec succès!");
            System.out.println("📊 Le dashboard affiche :");
            System.out.println("   - Statistiques des formations et évaluations");
            System.out.println("   - Boutons d'actions rapides");
            System.out.println("   - Section des dernières activités");
            System.out.println("   - État de la base de données");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement du dashboard FXML:");
            System.err.println("   Détails: " + e.getMessage());
            e.printStackTrace();

            // Afficher une boîte de dialogue d'erreur
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Erreur de chargement FXML");
                alert.setHeaderText("Impossible de charger le dashboard");
                alert.setContentText("Erreur: " + e.getMessage() + "\n\nAssurez-vous que:\n" +
                    "1. Le fichier dashboard.fxml existe\n" +
                    "2. Les ressources sont correctement emballées\n" +
                    "3. Le projet est compilé avec Maven: mvn clean compile");
                alert.showAndWait();
            } catch (Exception ex) {
                System.err.println("Impossible d'afficher la boîte de dialogue d'erreur: " + ex.getMessage());
            }

            // Fermer l'application en cas d'erreur
            System.exit(1);
        }
    }

    /**
     * Méthode principale pour lancer le test
     */
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage du test de chargement du dashboard FXML");
        System.out.println("   Vérification de la validité du fichier dashboard.fxml");
        System.out.println();

        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du lancement JavaFX: " + e.getMessage());
            System.err.println("\n💡 Solutions:");
            System.err.println("   1. Assurez-vous que Java 17+ est installé");
            System.err.println("   2. Compilez le projet: mvn clean compile");
            System.err.println("   3. Exécutez avec Maven: mvn javafx:run -Djavafx.mainClass=esprit.tn.DashboardTest");
            System.err.println("   4. Ou utilisez le fichier run_dashboard.bat");
            e.printStackTrace();
        }
    }
}
