package esprit.tn;

import esprit.tn.models.formation;
import esprit.tn.models.evaluation;
import esprit.tn.services.serviceformation;
import esprit.tn.services.serviceevaluation;
import esprit.tn.utils.myconnexion;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Classe Main - Gestion CRUD des Formations et Évaluations
 * Cette classe teste toutes les opérations CRUD pour les formations et les évaluations
 * 
 * @author ESPRIT
 * @version 1.0
 */
public class Main {
    
    private static Scanner scanner = new Scanner(System.in);
    private static serviceformation serviceFormation = new serviceformation();
    private static serviceevaluation serviceEvaluation = new serviceevaluation();

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   SYSTÈME DE GESTION DES FORMATIONS ET ÉVALUATIONS         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Test de connexion
        if (!testConnection()) {
            System.out.println("\n❌ Erreur: Impossible de se connecter à la base de données");
            return;
        }

        // Menu principal
        boolean running = true;
        while (running) {
            afficherMenuPrincipal();
            try {
                int choix = scanner.nextInt();
                scanner.nextLine(); // Consommer le retour à la ligne
                
                switch (choix) {
                    case 1:
                        menuFormation();
                        break;
                    case 2:
                        menuEvaluation();
                        break;
                    case 3:
                        executerTestsAuto();
                        break;
                    case 4:
                        lancerDashboard();
                        break;
                    case 0:
                        running = false;
                        System.out.println("\n👋 Au revoir!");
                        break;
                    default:
                        System.out.println("\n⚠️  Option invalide. Veuillez réessayer.");
                }
            } catch (Exception e) {
                System.out.println("\n❌ Erreur d'entrée: " + e.getMessage());
                scanner.nextLine();
            }
            System.out.println();
        }
        
        scanner.close();
    }

    /**
     * Teste la connexion à la base de données
     */
    private static boolean testConnection() {
        try {
            myconnexion.getInstance();
            System.out.println("✅ Connexion à la base de données établie avec succès!\n");
            return true;
        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Affiche le menu principal
     */
    private static void afficherMenuPrincipal() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              MENU PRINCIPAL                                ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Gestion des Formations                                 ║");
        System.out.println("║  2. Gestion des Évaluations                                ║");
        System.out.println("║  3. Exécuter les tests automatisés                         ║");
        System.out.println("║  4. Ouvrir le Dashboard (Interface Graphique)              ║");
        System.out.println("║  0. Quitter                                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.print("Choisissez une option: ");
    }

    /**
     * Menu de gestion des formations
     */
    private static void menuFormation() {
        boolean menu = true;
        while (menu) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║         GESTION DES FORMATIONS - MENU CRUD                ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  1. Ajouter une formation                                  ║");
            System.out.println("║  2. Afficher toutes les formations                         ║");
            System.out.println("║  3. Rechercher une formation par ID                        ║");
            System.out.println("║  4. Modifier une formation                                 ║");
            System.out.println("║  5. Supprimer une formation                                ║");
            System.out.println("║  0. Retour au menu principal                              ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.print("Choisissez une option: ");
            
            try {
                int choix = scanner.nextInt();
                scanner.nextLine();
                
                switch (choix) {
                    case 1:
                        ajouterFormation();
                        break;
                    case 2:
                        afficherToutesFormations();
                        break;
                    case 3:
                        rechercherFormationParId();
                        break;
                    case 4:
                        modifierFormation();
                        break;
                    case 5:
                        supprimerFormation();
                        break;
                    case 0:
                        menu = false;
                        break;
                    default:
                        System.out.println("\n⚠️  Option invalide.");
                }
            } catch (Exception e) {
                System.out.println("\n❌ Erreur: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    /**
     * Menu de gestion des évaluations
     */
    private static void menuEvaluation() {
        boolean menu = true;
        while (menu) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║        GESTION DES ÉVALUATIONS - MENU CRUD                ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  1. Ajouter une évaluation                                 ║");
            System.out.println("║  2. Afficher toutes les évaluations                        ║");
            System.out.println("║  3. Rechercher une évaluation par ID                       ║");
            System.out.println("║  4. Modifier une évaluation                                ║");
            System.out.println("║  5. Supprimer une évaluation                               ║");
            System.out.println("║  0. Retour au menu principal                              ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.print("Choisissez une option: ");
            
            try {
                int choix = scanner.nextInt();
                scanner.nextLine();
                
                switch (choix) {
                    case 1:
                        ajouterEvaluation();
                        break;
                    case 2:
                        afficherToutesEvaluations();
                        break;
                    case 3:
                        rechercherEvaluationParId();
                        break;
                    case 4:
                        modifierEvaluation();
                        break;
                    case 5:
                        supprimerEvaluation();
                        break;
                    case 0:
                        menu = false;
                        break;
                    default:
                        System.out.println("\n⚠️  Option invalide.");
                }
            } catch (Exception e) {
                System.out.println("\n❌ Erreur: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    // ============== OPÉRATIONS SUR LES FORMATIONS ==============

    private static void ajouterFormation() {
        System.out.println("\n--- Ajouter une formation ---");
        try {
            System.out.print("Nom de la formation: ");
            String nom = scanner.nextLine();

            // Validation du nom
            if (nom == null || nom.trim().isEmpty()) {
                System.out.println("❌ Erreur: Le nom de la formation est obligatoire et ne peut pas être vide.");
                return;
            }

            System.out.print("Type: ");
            String type = scanner.nextLine();

            // Validation du type
            if (type == null || type.trim().isEmpty()) {
                System.out.println("❌ Erreur: Le type de la formation est obligatoire et ne peut pas être vide.");
                return;
            }

            System.out.print("Description: ");
            String description = scanner.nextLine();

            // Validation de la description
            if (description == null || description.trim().isEmpty()) {
                System.out.println("❌ Erreur: La description de la formation est obligatoire et ne peut pas être vide.");
                return;
            }

            System.out.print("Date (YYYY-MM-DD): ");
            String dateStr = scanner.nextLine();

            // Validation de la date
            if (dateStr == null || dateStr.trim().isEmpty()) {
                System.out.println("❌ Erreur: La date de la formation est obligatoire et ne peut pas être vide.");
                return;
            }

            Date date = Date.valueOf(dateStr);

            formation f = new formation(nom, type, description, date);
            serviceFormation.addFormation(f);
            System.out.println("✅ Formation ajoutée avec succès!");
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private static void afficherToutesFormations() {
        System.out.println("\n--- Toutes les formations ---");
        try {
            List<formation> formations = serviceFormation.getAllFormations();
            if (formations.isEmpty()) {
                System.out.println("Aucune formation trouvée.");
            } else {
                formations.forEach(f -> System.out.println(f));
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private static void rechercherFormationParId() {
        System.out.println("\n--- Rechercher une formation ---");
        try {
            System.out.print("ID de la formation: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            formation f = serviceFormation.getFormationById(id);
            if (f != null) {
                System.out.println("✅ Formation trouvée:");
                System.out.println(f);
            } else {
                System.out.println("❌ Aucune formation trouvée avec cet ID.");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private static void modifierFormation() {
        System.out.println("\n--- Modifier une formation ---");
        try {
            System.out.print("ID de la formation: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            formation f = serviceFormation.getFormationById(id);
            if (f != null) {
                System.out.println("Formation actuelle: " + f);
                
                System.out.print("Nouveau nom (ou [Entrée] pour ignorer): ");
                String nom = scanner.nextLine();
                if (!nom.isEmpty()) f.setNom_form(nom);
                
                System.out.print("Nouveau type (ou [Entrée] pour ignorer): ");
                String type = scanner.nextLine();
                if (!type.isEmpty()) f.setType(type);
                
                System.out.print("Nouvelle description (ou [Entrée] pour ignorer): ");
                String description = scanner.nextLine();
                if (!description.isEmpty()) f.setDescription(description);
                
                serviceFormation.updateFormation(f);
                System.out.println("✅ Formation mise à jour avec succès!");
            } else {
                System.out.println("❌ Aucune formation trouvée avec cet ID.");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private static void supprimerFormation() {
        System.out.println("\n--- Supprimer une formation ---");
        try {
            System.out.print("ID de la formation: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            System.out.print("Êtes-vous sûr? (oui/non): ");
            String confirmation = scanner.nextLine();
            
            if (confirmation.equalsIgnoreCase("oui")) {
                serviceFormation.deleteFormation(id);
                System.out.println("✅ Formation supprimée avec succès!");
            } else {
                System.out.println("❌ Suppression annulée.");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    // ============== OPÉRATIONS SUR LES ÉVALUATIONS ==============

    private static void ajouterEvaluation() {
        System.out.println("\n--- Ajouter une évaluation ---");
        try {
            afficherToutesFormations();
            
            System.out.print("ID de la formation: ");
            int formationId = scanner.nextInt();
            scanner.nextLine();
            
            System.out.print("Note (0-5): ");
            int note = scanner.nextInt();
            scanner.nextLine();
            
            System.out.print("Titre: ");
            String titre = scanner.nextLine();
            
            System.out.print("Commentaire: ");
            String commentaire = scanner.nextLine();
            
            if (note < 0 || note > 5) {
                System.out.println("❌ La note doit être entre 0 et 5.");
                return;
            }
            
            evaluation e = new evaluation(note, formationId, titre, commentaire);
            serviceEvaluation.addEvaluation(e);
            System.out.println("✅ Évaluation ajoutée avec succès!");
        } catch (Exception ex) {
            System.out.println("❌ Erreur: " + ex.getMessage());
        }
    }

    private static void afficherToutesEvaluations() {
        System.out.println("\n--- Toutes les évaluations ---");
        try {
            List<evaluation> evaluations = serviceEvaluation.getAllEvaluations();
            if (evaluations.isEmpty()) {
                System.out.println("Aucune évaluation trouvée.");
            } else {
                evaluations.forEach(e -> System.out.println(e));
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private static void rechercherEvaluationParId() {
        System.out.println("\n--- Rechercher une évaluation ---");
        try {
            System.out.print("ID de l'évaluation: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            evaluation e = serviceEvaluation.getEvaluationById(id);
            if (e != null) {
                System.out.println("✅ Évaluation trouvée:");
                System.out.println(e);
            } else {
                System.out.println("❌ Aucune évaluation trouvée avec cet ID.");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private static void modifierEvaluation() {
        System.out.println("\n--- Modifier une évaluation ---");
        try {
            System.out.print("ID de l'évaluation: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            evaluation e = serviceEvaluation.getEvaluationById(id);
            if (e != null) {
                System.out.println("Évaluation actuelle: " + e);
                
                System.out.print("Nouvelle note (0-5, ou -1 pour ignorer): ");
                int note = scanner.nextInt();
                scanner.nextLine();
                if (note >= 0 && note <= 5) e.setNote(note);
                
                System.out.print("Nouveau titre (ou [Entrée] pour ignorer): ");
                String titre = scanner.nextLine();
                if (!titre.isEmpty()) e.setTitre(titre);
                
                System.out.print("Nouveau commentaire (ou [Entrée] pour ignorer): ");
                String commentaire = scanner.nextLine();
                if (!commentaire.isEmpty()) e.setCommentaire(commentaire);
                
                serviceEvaluation.updateEvaluation(e);
                System.out.println("✅ Évaluation mise à jour avec succès!");
            } else {
                System.out.println("❌ Aucune évaluation trouvée avec cet ID.");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private static void supprimerEvaluation() {
        System.out.println("\n--- Supprimer une évaluation ---");
        try {
            System.out.print("ID de l'évaluation: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            
            System.out.print("Êtes-vous sûr? (oui/non): ");
            String confirmation = scanner.nextLine();
            
            if (confirmation.equalsIgnoreCase("oui")) {
                serviceEvaluation.deleteEvaluation(id);
                System.out.println("✅ Évaluation supprimée avec succès!");
            } else {
                System.out.println("❌ Suppression annulée.");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    // ============== TESTS AUTOMATISÉS ==============

    /**
     * Exécute les tests automatisés CRUD
     */
    private static void executerTestsAuto() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         EXÉCUTION DES TESTS AUTOMATISÉS                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        testFormationCRUD();
        testEvaluationCRUD();
        
        System.out.println("\n✅ Tous les tests sont terminés!");
    }

    private static void testFormationCRUD() {
        System.out.println("\n▶ TEST FORMATION CRUD");
        System.out.println("═══════════════════════════════════════");
        
        try {
            // CREATE
            System.out.println("\n📝 CREATE - Ajout de formations");
            formation f1 = new formation("Java Basics", "Programming", "Introduction to Java", Date.valueOf("2023-10-01"));
            serviceFormation.addFormation(f1);
            formation f2 = new formation("Spring Boot", "Framework", "Advanced Java framework", Date.valueOf("2023-11-01"));
            serviceFormation.addFormation(f2);
            System.out.println("✅ 2 formations ajoutées");

            // READ ALL
            System.out.println("\n📖 READ - Lecture de toutes les formations");
            List<formation> formations = serviceFormation.getAllFormations();
            System.out.println("Formations trouvées: " + formations.size());
            formations.forEach(f -> System.out.println("  - " + f));

            // READ BY ID
            if (!formations.isEmpty()) {
                System.out.println("\n🔍 READ BY ID - Lecture d'une formation par ID");
                int id = formations.get(0).getId();
                formation f = serviceFormation.getFormationById(id);
                if (f != null) {
                    System.out.println("Formation ID " + id + ": " + f);

                    // UPDATE
                    System.out.println("\n✏️  UPDATE - Modification");
                    f.setDescription("Updated description - " + System.currentTimeMillis());
                    serviceFormation.updateFormation(f);
                    formation fUpdated = serviceFormation.getFormationById(id);
                    System.out.println("Mise à jour confirmée: " + fUpdated);
                }
            }

            // DELETE
            if (formations.size() > 1) {
                System.out.println("\n🗑️  DELETE - Suppression");
                int idDelete = formations.get(formations.size() - 1).getId();
                serviceFormation.deleteFormation(idDelete);
                System.out.println("Formation ID " + idDelete + " supprimée");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du test Formation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testEvaluationCRUD() {
        System.out.println("\n\n▶ TEST EVALUATION CRUD");
        System.out.println("═══════════════════════════════════════");
        
        try {
            // Vérifier qu'il existe des formations
            List<formation> formations = serviceFormation.getAllFormations();
            if (formations.isEmpty()) {
                System.out.println("❌ Aucune formation trouvée. Test d'évaluation annulé.");
                return;
            }

            int formationId = formations.get(0).getId();

            // CREATE
            System.out.println("\n📝 CREATE - Ajout d'évaluations");
            evaluation e1 = new evaluation(5, formationId, "Excellent", "Great course!");
            serviceEvaluation.addEvaluation(e1);
            evaluation e2 = new evaluation(4, formationId, "Good", "Very informative");
            serviceEvaluation.addEvaluation(e2);
            System.out.println("✅ 2 évaluations ajoutées");

            // READ ALL
            System.out.println("\n📖 READ - Lecture de toutes les évaluations");
            List<evaluation> evaluations = serviceEvaluation.getAllEvaluations();
            System.out.println("Évaluations trouvées: " + evaluations.size());
            evaluations.forEach(e -> System.out.println("  - " + e));

            // READ BY ID
            if (!evaluations.isEmpty()) {
                System.out.println("\n🔍 READ BY ID - Lecture d'une évaluation par ID");
                int id = evaluations.get(0).getId();
                evaluation e = serviceEvaluation.getEvaluationById(id);
                if (e != null) {
                    System.out.println("Évaluation ID " + id + ": " + e);

                    // UPDATE
                    System.out.println("\n✏️  UPDATE - Modification");
                    e.setCommentaire("Updated comment - " + System.currentTimeMillis());
                    serviceEvaluation.updateEvaluation(e);
                    evaluation eUpdated = serviceEvaluation.getEvaluationById(id);
                    System.out.println("Mise à jour confirmée: " + eUpdated);
                }
            }

            // DELETE
            if (evaluations.size() > 1) {
                System.out.println("\n🗑️  DELETE - Suppression");
                int idDelete = evaluations.get(evaluations.size() - 1).getId();
                serviceEvaluation.deleteEvaluation(idDelete);
                System.out.println("Évaluation ID " + idDelete + " supprimée");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du test Évaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lance le Dashboard (Interface Graphique)
     */
    private static void lancerDashboard() {
        System.out.println("\n--- Lancement du Dashboard ---");
        System.out.println("Tentative de lancement de l'interface graphique...");
        System.out.println("(Assurez-vous que JavaFX est correctement configuré)");
        System.out.println("");
        
        try {
            // Lancer le Dashboard dans une autre thread
            Thread dashboardThread = new Thread(() -> {
                try {
                    DashboardTest.main(new String[]{});
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors du lancement du Dashboard: " + e.getMessage());
                    System.err.println("\n💡 Solutions:");
                    System.err.println("   1. Utilisez le fichier run_dashboard.bat");
                    System.err.println("   2. Vérifiez que JavaFX SDK 17.0.2 est installé");
                    System.err.println("   3. Lancez via Maven: mvn javafx:run");
                }
            });
            dashboardThread.start();
            
            // Attendre un peu
            Thread.sleep(2000);
            
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }
}
