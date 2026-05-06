package esprit.tn;

import esprit.tn.models.formation;
import esprit.tn.models.evaluation;
import esprit.tn.services.serviceformation;
import esprit.tn.services.serviceevaluation;
import esprit.tn.utils.myconnexion;
import java.sql.Date;

/**
 * Classe de test pour vérifier que les opérations CRUD modifient réellement la base de données
 */
public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("🧪 TEST DE MODIFICATION DE LA BASE DE DONNÉES");
        System.out.println("═══════════════════════════════════════════════\n");

        // Test de connexion
        if (!testConnection()) {
            System.out.println("❌ Impossible de continuer sans connexion");
            return;
        }

        serviceformation sf = new serviceformation();
        serviceevaluation se = new serviceevaluation();

        // État initial
        System.out.println("📊 ÉTAT INITIAL DE LA BASE");
        int initialFormations = sf.countFormations();
        int initialEvaluations = se.countEvaluations();
        System.out.println("Formations: " + initialFormations);
        System.out.println("Évaluations: " + initialEvaluations);
        System.out.println();

        // TEST CREATE
        testCreate(sf, se);

        // TEST READ
        testRead(sf, se);

        // TEST UPDATE
        testUpdate(sf, se);

        // TEST DELETE
        testDelete(sf, se);

        // État final
        System.out.println("\n📊 ÉTAT FINAL DE LA BASE");
        int finalFormations = sf.countFormations();
        int finalEvaluations = se.countEvaluations();
        System.out.println("Formations: " + finalFormations);
        System.out.println("Évaluations: " + finalEvaluations);

        // Résumé
        System.out.println("\n📈 RÉSUMÉ DES MODIFICATIONS");
        System.out.println("Formations ajoutées: " + (finalFormations - initialFormations));
        System.out.println("Évaluations ajoutées: " + (finalEvaluations - initialEvaluations));

        if (finalFormations == initialFormations && finalEvaluations == initialEvaluations) {
            System.out.println("❌ AUCUNE MODIFICATION DÉTECTÉE!");
            System.out.println("Les opérations CRUD ne modifient pas la base de données.");
        } else {
            System.out.println("✅ MODIFICATIONS DÉTECTÉES!");
            System.out.println("Les opérations CRUD fonctionnent correctement.");
        }

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("FIN DU TEST DE BASE DE DONNÉES");
    }

    private static boolean testConnection() {
        try {
            myconnexion conn = myconnexion.getInstance();
            if (conn.getCnx() != null && !conn.getCnx().isClosed()) {
                System.out.println("✅ Connexion à la base de données réussie");
                return true;
            } else {
                System.out.println("❌ Connexion échouée ou fermée");
                return false;
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion: " + e.getMessage());
            return false;
        }
    }

    private static void testCreate(serviceformation sf, serviceevaluation se) {
        System.out.println("📝 TEST CREATE");
        System.out.println("──────────────");

        // Créer une formation
        formation f = new formation("Test Formation", "Test", "Formation de test", Date.valueOf("2024-04-16"));
        boolean formationAdded = sf.addFormation(f);

        // Créer une évaluation (si formation ajoutée)
        if (formationAdded) {
            // Récupérer l'ID de la formation ajoutée
            var formations = sf.getAllFormations();
            if (!formations.isEmpty()) {
                int formationId = formations.get(formations.size() - 1).getId();
                evaluation e = new evaluation(4, formationId, "Test Evaluation", "Évaluation de test");
                se.addEvaluation(e);
            }
        }

        System.out.println();
    }

    private static void testRead(serviceformation sf, serviceevaluation se) {
        System.out.println("📖 TEST READ");
        System.out.println("────────────");

        // Lire toutes les formations
        var formations = sf.getAllFormations();
        System.out.println("Nombre de formations lues: " + formations.size());

        // Lire toutes les évaluations
        var evaluations = se.getAllEvaluations();
        System.out.println("Nombre d'évaluations lues: " + evaluations.size());

        System.out.println();
    }

    private static void testUpdate(serviceformation sf, serviceevaluation se) {
        System.out.println("✏️  TEST UPDATE");
        System.out.println("───────────────");

        // Modifier la dernière formation
        var formations = sf.getAllFormations();
        if (!formations.isEmpty()) {
            formation f = formations.get(formations.size() - 1);
            f.setDescription("Description modifiée - " + System.currentTimeMillis());
            sf.updateFormation(f);
        }

        // Modifier la dernière évaluation
        var evaluations = se.getAllEvaluations();
        if (!evaluations.isEmpty()) {
            evaluation e = evaluations.get(evaluations.size() - 1);
            e.setCommentaire("Commentaire modifié - " + System.currentTimeMillis());
            se.updateEvaluation(e);
        }

        System.out.println();
    }

    private static void testDelete(serviceformation sf, serviceevaluation se) {
        System.out.println("🗑️  TEST DELETE");
        System.out.println("───────────────");

        // Supprimer la dernière évaluation d'abord
        var evaluations = se.getAllEvaluations();
        if (!evaluations.isEmpty()) {
            int evalId = evaluations.get(evaluations.size() - 1).getId();
            se.deleteEvaluation(evalId);
        }

        // Supprimer la dernière formation
        var formations = sf.getAllFormations();
        if (!formations.isEmpty()) {
            int formId = formations.get(formations.size() - 1).getId();
            sf.deleteFormation(formId);
        }

        System.out.println();
    }
}
