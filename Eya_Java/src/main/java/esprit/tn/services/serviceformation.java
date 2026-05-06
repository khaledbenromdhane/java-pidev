package esprit.tn.services;

import esprit.tn.models.formation;
import esprit.tn.utils.myconnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class serviceformation {

    private final Connection cnx = myconnexion.getInstance().getCnx();

    // CREATE: Add a new formation
    public boolean addFormation(formation f) {
        String req = "INSERT INTO formation (nom_form, type, description, date_form) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, f.getNom_form());
            pst.setString(2, f.getType());
            pst.setString(3, f.getDescription());
            pst.setDate(4, f.getDate_form());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Formation ajoutée avec succès!");
                return true;
            } else {
                System.out.println("❌ Échec de l'ajout de la formation");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la formation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // READ: Get all formations
    public List<formation> getAllFormations() {
        List<formation> formations = new ArrayList<>();
        String req = "SELECT * FROM formation";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                formation f = new formation(
                    rs.getInt("id"),
                    rs.getString("nom_form"),
                    rs.getString("type"),
                    rs.getString("description"),
                    rs.getDate("date_form")
                );
                formations.add(f);
            }
            System.out.println("📖 " + formations.size() + " formation(s) trouvée(s)");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des formations: " + e.getMessage());
            e.printStackTrace();
        }
        return formations;
    }

    // READ: Get formation by ID
    public formation getFormationById(int id) {
        String req = "SELECT * FROM formation WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    formation f = new formation(
                        rs.getInt("id"),
                        rs.getString("nom_form"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getDate("date_form")
                    );
                    System.out.println("🔍 Formation trouvée: " + f.getNom_form());
                    return f;
                } else {
                    System.out.println("❌ Aucune formation trouvée avec l'ID: " + id);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche de la formation: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE: Update an existing formation
    public boolean updateFormation(formation f) {
        String req = "UPDATE formation SET nom_form = ?, type = ?, description = ?, date_form = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, f.getNom_form());
            pst.setString(2, f.getType());
            pst.setString(3, f.getDescription());
            pst.setDate(4, f.getDate_form());
            pst.setInt(5, f.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Formation mise à jour avec succès!");
                return true;
            } else {
                System.out.println("❌ Aucune formation trouvée avec l'ID: " + f.getId());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de la formation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // DELETE: Delete a formation by ID
    public boolean deleteFormation(int id) {
        // Supprimer d'abord les évaluations liées (cascade manuelle)
        String reqEval = "DELETE FROM evaluation WHERE formation_id = ?";
        try (PreparedStatement pstEval = cnx.prepareStatement(reqEval)) {
            pstEval.setInt(1, id);
            pstEval.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression des évaluations liées: " + e.getMessage());
        }

        String req = "DELETE FROM formation WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Formation supprimée avec succès!");
                return true;
            } else {
                System.out.println("❌ Aucune formation trouvée avec l'ID: " + id);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la formation: " + e.getMessage());
            e.printStackTrace();
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

    // Compter le nombre de formations
    public int countFormations() {
        String req = "SELECT COUNT(*) as total FROM formation";
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
}
