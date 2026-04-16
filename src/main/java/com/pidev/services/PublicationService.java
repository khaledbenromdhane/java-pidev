package com.pidev.services;

import com.pidev.entities.Publication;
import com.pidev.tools.myconnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PublicationService implements IService<Publication> {

    private final Connection cnx = myconnexion.getInstance().getConnection();

    public void ajouter(Publication p) {
        String req = "INSERT INTO publication (date_act, description, titre, slug, image, image_analysis, id_user, nb_likes, nb_dislikes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setTimestamp(1, Timestamp.valueOf(p.getDateAct()));
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getTitre());
            ps.setString(4, p.getSlug());
            ps.setString(5, p.getImage());
            ps.setString(6, p.getImageAnalysis());
            if (p.getIdUser() > 0) {
                ps.setInt(7, p.getIdUser());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, p.getNbLikes());
            ps.setInt(9, p.getNbDislikes());
            ps.executeUpdate();
            System.out.println("✅ Publication ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de publication : " + e.getMessage());
        }
    }

    public void modifier(Publication p) {
        String req = "UPDATE publication SET date_act=?, description=?, titre=?, slug=?, image=?, image_analysis=?, id_user=?, nb_likes=?, nb_dislikes=? WHERE id_publication=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setTimestamp(1, Timestamp.valueOf(p.getDateAct()));
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getTitre());
            ps.setString(4, p.getSlug());
            ps.setString(5, p.getImage());
            ps.setString(6, p.getImageAnalysis());
            if (p.getIdUser() > 0) {
                ps.setInt(7, p.getIdUser());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, p.getNbLikes());
            ps.setInt(9, p.getNbDislikes());
            ps.setInt(10, p.getIdPublication());
            ps.executeUpdate();
            System.out.println("✅ Publication modifiée avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification de publication : " + e.getMessage());
        }
    }

    public void supprimer(int idPublication) {
        String req = "DELETE FROM publication WHERE id_publication=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idPublication);
            ps.executeUpdate();
            System.out.println("✅ Publication supprimée avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de publication : " + e.getMessage());
        }
    }

    public List<Publication> afficher() {
        List<Publication> liste = new ArrayList<>();
        String req = "SELECT * FROM publication";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            
            // Debug: Print columns once
            if (rs.isBeforeFirst()) {
                ResultSetMetaData metaData = rs.getMetaData();
                System.out.print("DEBUG: Colonnes trouvées dans 'publication' : ");
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    System.out.print(metaData.getColumnName(i) + " ");
                }
                System.out.println();
            }

            while (rs.next()) {
                Publication p = new Publication(
                        rs.getInt("id_publication"),
                        rs.getTimestamp("date_act").toLocalDateTime(),
                        rs.getString("description"),
                        rs.getString("titre"),
                        rs.getString("slug"),
                        rs.getString("image"),
                        rs.getString("image_analysis"),
                        rs.getInt("id_user"),
                        rs.getInt("nb_likes"),
                        rs.getInt("nb_dislikes")
                );
                liste.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des publications : " + e.getMessage());
        }
        return liste;
    }

    public Publication afficherParId(int idPublication) {
        String req = "SELECT * FROM publication WHERE id_publication=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idPublication);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Publication(
                        rs.getInt("id_publication"),
                        rs.getTimestamp("date_act").toLocalDateTime(),
                        rs.getString("description"),
                        rs.getString("titre"),
                        rs.getString("slug"),
                        rs.getString("image"),
                        rs.getString("image_analysis"),
                        rs.getInt("id_user"),
                        rs.getInt("nb_likes"),
                        rs.getInt("nb_dislikes")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par ID (publication) : " + e.getMessage());
        }
        return null;
    }
}
