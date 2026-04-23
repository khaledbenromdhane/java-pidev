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
            String req = "INSERT INTO user (Nom, Prenom, Password, Email, Telephone, Role, photo) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(req)) {
                ps.setString(1, user.getNom());
                ps.setString(2, user.getPrenom());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getTelephone());
                ps.setString(6, user.getRole());
                ps.setString(7, user.getPhoto());
                ps.executeUpdate();
                System.out.println("✅ Utilisateur ajouté avec succès !");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            }
        }

        // ─── Modifier ────────────────────────────────────────────────────────────────

        public void modifier(User user) {
            String req = "UPDATE user SET Nom=?, Prenom=?, Password=?, Email=?, Telephone=?, Role=?, photo=? WHERE id_user=?";
            try (PreparedStatement ps = cnx.prepareStatement(req)) {
                ps.setString(1, user.getNom());
                ps.setString(2, user.getPrenom());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getTelephone());
                ps.setString(6, user.getRole());
                ps.setString(7, user.getPhoto());
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
                while (rs.next()) {
                    User u = new User(
                            rs.getInt("id_user"),
                            rs.getString("Nom"),
                            rs.getString("Prenom"),
                            rs.getString("Password"),
                            rs.getString("Email"),
                            rs.getString("Telephone"),
                            rs.getString("Role"),
                            rs.getString("photo")
                    );
                    liste.add(u);
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de l'affichage : " + e.getMessage());
            }
            return liste;
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
                            rs.getString("photo")
                    );
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la recherche par ID : " + e.getMessage());
            }
            return null;
        }

        // ─── Rechercher par Email ────────────────────────────────────────────────────

        public User getByEmail(String email) {
            String req = "SELECT * FROM user WHERE Email=?";
            try (PreparedStatement ps = cnx.prepareStatement(req)) {
                ps.setString(1, email);
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
                            rs.getString("photo")
                    );
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur getByEmail : " + e.getMessage());
            }
            return null;
        }

        // ─── Modifier mot de passe ───────────────────────────────────────────────────

        public void modifierMotDePasse(String email, String newPassword) {
            String req = "UPDATE user SET Password=? WHERE Email=?";
            try (PreparedStatement ps = cnx.prepareStatement(req)) {
                ps.setString(1, newPassword);
                ps.setString(2, email);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("✅ Mot de passe mis à jour pour : " + email);
                } else {
                    System.err.println("⚠️ Aucun utilisateur trouvé avec l'email : " + email);
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur update password : " + e.getMessage());
            }
        }
    }