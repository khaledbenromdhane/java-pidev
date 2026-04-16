    package com.pidev.services;

    import com.pidev.entities.User;
    import com.pidev.tools.myconnexion;

    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;

    public class CrudService {

        private final Connection cnx = myconnexion.getInstance().getConnection();

        // ─── Ajouter ─────────────────────────────────────────────────────────────────

    public void ajouter(User user) {
        String req = "INSERT INTO user (Nom, Prenom, Password, Email, Telephone, Role, est_signale, raison_signalement) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getTelephone());
            ps.setString(6, user.getRole());
            ps.setInt(7, user.getEst_signale());
            ps.setString(8, user.getRaison_signalement());
            ps.executeUpdate();
            System.out.println("✅ Utilisateur ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    // ─── Modifier ────────────────────────────────────────────────────────────────

    public void modifier(User user) {
        String req = "UPDATE user SET Nom=?, Prenom=?, Email=?, Telephone=?, Role=?, est_signale=?, raison_signalement=? WHERE id_user=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getTelephone());
            ps.setString(5, user.getRole());
            ps.setInt(6, user.getEst_signale());
            ps.setString(7, user.getRaison_signalement());
            ps.setInt(8, user.getId_user());
            ps.executeUpdate();
            System.out.println("✅ Utilisateur modifié avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    // ─── Supprimer ───────────────────────────────────────────────────────────────

    public void supprimer(int id_user) {
        String req = "DELETE FROM user WHERE id_user=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id_user);
            ps.executeUpdate();
            System.out.println("✅ Utilisateur supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ─── Afficher tous ───────────────────────────────────────────────────────────

    public List<User> afficher() {
        List<User> liste = new ArrayList<>();
        String req = "SELECT * FROM user";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            
            // Debug: Print columns once
            if (rs.isBeforeFirst()) {
                ResultSetMetaData metaData = rs.getMetaData();
                System.out.print("DEBUG: Colonnes trouvées dans 'user' : ");
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    System.out.print(metaData.getColumnName(i) + " ");
                }
                System.out.println();
            }

            while (rs.next()) {
                User u = new User(
                        rs.getInt("id_user"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Password"),
                        rs.getString("Email"),
                        rs.getString("Telephone"),
                        rs.getString("Role"),
                        safeGetInt(rs, "est_signale"),
                        safeGetString(rs, "raison_signalement")
                );
                liste.add(u);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage : " + e.getMessage());
        }
        return liste;
    }

    private int safeGetInt(ResultSet rs, String col) {
        try { return rs.getInt(col); } catch (Exception e) { return 0; }
    }

    private String safeGetString(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (Exception e) { return null; }
    }

    // ─── Afficher par ID ─────────────────────────────────────────────────────────

    public User afficherParId(int id_user) {
        String req = "SELECT * FROM user WHERE id_user=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id_user);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id_user"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Password"),
                        rs.getString("Email"),
                        rs.getString("Telephone"),
                        rs.getString("Role"),
                        safeGetInt(rs, "est_signale"),
                        safeGetString(rs, "raison_signalement")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par ID : " + e.getMessage());
        }
        return null;
    }
    }