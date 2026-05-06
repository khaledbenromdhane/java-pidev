package esprit.tn.services;

import esprit.tn.models.evaluation;
import esprit.tn.models.EvaluationWithFormation;
import esprit.tn.utils.myconnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class serviceevaluation {

    private final Connection cnx = myconnexion.getInstance().getCnx();

    // CREATE: Add a new evaluation
    public boolean addEvaluation(evaluation e) {
        String req = "INSERT INTO evaluation (note, formation_id, titre, commentaire) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, e.getNote());
            pst.setInt(2, e.getFormation_id());
            pst.setString(3, e.getTitre());
            pst.setString(4, e.getCommentaire());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Évaluation ajoutée avec succès!");
                return true;
            } else {
                System.out.println("❌ Échec de l'ajout de l'évaluation");
                return false;
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de l'ajout de l'évaluation: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // READ: Get all evaluations
    public List<evaluation> getAllEvaluations() {
        List<evaluation> evaluations = new ArrayList<>();
        String req = "SELECT * FROM evaluation";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                evaluation e = new evaluation(
                    rs.getInt("id"),
                    rs.getInt("note"),
                    rs.getInt("formation_id"),
                    rs.getString("titre"),
                    rs.getString("commentaire")
                );
                evaluations.add(e);
            }
            System.out.println("📖 " + evaluations.size() + " évaluation(s) trouvée(s)");
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération des évaluations: " + ex.getMessage());
            ex.printStackTrace();
        }
        return evaluations;
    }

    // READ: Get evaluation by ID
    public evaluation getEvaluationById(int id) {
        String req = "SELECT * FROM evaluation WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    evaluation e = new evaluation(
                        rs.getInt("id"),
                        rs.getInt("note"),
                        rs.getInt("formation_id"),
                        rs.getString("titre"),
                        rs.getString("commentaire")
                    );
                    System.out.println("🔍 Évaluation trouvée: " + e.getTitre());
                    return e;
                } else {
                    System.out.println("❌ Aucune évaluation trouvée avec l'ID: " + id);
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la recherche de l'évaluation: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    // UPDATE: Update an existing evaluation
    public boolean updateEvaluation(evaluation e) {
        String req = "UPDATE evaluation SET note = ?, formation_id = ?, titre = ?, commentaire = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, e.getNote());
            pst.setInt(2, e.getFormation_id());
            pst.setString(3, e.getTitre());
            pst.setString(4, e.getCommentaire());
            pst.setInt(5, e.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Évaluation mise à jour avec succès!");
                return true;
            } else {
                System.out.println("❌ Aucune évaluation trouvée avec l'ID: " + e.getId());
                return false;
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la mise à jour de l'évaluation: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // DELETE: Delete an evaluation by ID
    public boolean deleteEvaluation(int id) {
        String req = "DELETE FROM evaluation WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Évaluation supprimée avec succès!");
                return true;
            } else {
                System.out.println("❌ Aucune évaluation trouvée avec l'ID: " + id);
                return false;
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la suppression de l'évaluation: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // Vérifier la connexion
    public boolean isConnected() {
        try {
            return cnx != null && !cnx.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // Compter le nombre d'évaluations
    public int countEvaluations() {
        String req = "SELECT COUNT(*) as total FROM evaluation";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage: " + e.getMessage());
        }
        return 0;
    }

    // Obtenir les évaluations d'une formation
    public List<evaluation> getEvaluationsByFormation(int formationId) {
        List<evaluation> evaluations = new ArrayList<>();
        String req = "SELECT * FROM evaluation WHERE formation_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, formationId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    evaluation e = new evaluation(
                        rs.getInt("id"),
                        rs.getInt("note"),
                        rs.getInt("formation_id"),
                        rs.getString("titre"),
                        rs.getString("commentaire")
                    );
                    evaluations.add(e);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des évaluations: " + e.getMessage());
            e.printStackTrace();
        }
        return evaluations;
    }

    // Obtenir les évaluations avec les noms de formation
    public List<EvaluationWithFormation> getAllEvaluationsWithFormationName() {
        List<EvaluationWithFormation> results = new ArrayList<>();
        String req = "SELECT e.id, e.note, e.formation_id, e.titre, e.commentaire, f.nom_form " +
                     "FROM evaluation e " +
                     "LEFT JOIN formation f ON e.formation_id = f.id " +
                     "ORDER BY e.formation_id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                EvaluationWithFormation eval = new EvaluationWithFormation(
                    rs.getInt("id"),
                    rs.getInt("note"),
                    rs.getInt("formation_id"),
                    rs.getString("titre"),
                    rs.getString("commentaire"),
                    rs.getString("nom_form") != null ? rs.getString("nom_form") : "?"
                );
                results.add(eval);
            }
            System.out.println("📖 " + results.size() + " évaluation(s) trouvée(s) avec formation");
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération des évaluations avec formation: " + ex.getMessage());
            ex.printStackTrace();
        }
        return results;
    }
}
