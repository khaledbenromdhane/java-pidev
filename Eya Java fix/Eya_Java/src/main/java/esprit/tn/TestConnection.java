package esprit.tn;

import esprit.tn.utils.myconnexion;
import java.sql.Connection;

/**
 * Classe de test simple pour vérifier la connexion à la base de données
 */
public class TestConnection {
    public static void main(String[] args) {
        System.out.println("🔍 TEST DE CONNEXION À LA BASE DE DONNÉES");
        System.out.println("═══════════════════════════════════════════════\n");

        try {
            // Tester la connexion
            myconnexion conn = myconnexion.getInstance();
            Connection cnx = conn.getCnx();

            if (cnx != null && !cnx.isClosed()) {
                System.out.println("✅ CONNEXION RÉUSSIE!");
                System.out.println("   - URL: jdbc:mysql://localhost:3306/bd_formation");
                System.out.println("   - Utilisateur: root");
                System.out.println("   - Statut: Connecté");

                // Tester une requête simple
                try {
                    var stmt = cnx.createStatement();
                    var rs = stmt.executeQuery("SELECT COUNT(*) as total FROM formation");
                    if (rs.next()) {
                        int count = rs.getInt("total");
                        System.out.println("   - Formations dans la BD: " + count);
                    }
                    rs.close();
                    stmt.close();
                } catch (Exception e) {
                    System.out.println("⚠️  Base de données vide ou tables manquantes");
                    System.out.println("   Exécutez schema.sql pour créer les tables");
                }

                cnx.close();
            } else {
                System.out.println("❌ CONNEXION ÉCHOUÉE!");
                System.out.println("   Vérifiez:");
                System.out.println("   - Que MySQL est démarré");
                System.out.println("   - Que la base bd_formation existe");
                System.out.println("   - Les paramètres de connexion");
            }

        } catch (Exception e) {
            System.out.println("❌ ERREUR LORS DU TEST:");
            System.out.println("   " + e.getMessage());
            System.out.println("\n🔧 SOLUTIONS POSSIBLES:");
            System.out.println("   1. Installer et démarrer MySQL");
            System.out.println("   2. Créer la base bd_formation");
            System.out.println("   3. Vérifier les paramètres dans myconnexion.java");
            System.out.println("   4. Consulter MYSQL_SETUP.md pour l'installation");
        }

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("FIN DU TEST");
    }
}
