package com.pidev.services;

import com.pidev.entities.Commentaire;
import com.pidev.tools.myconnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IService<Commentaire> {

    private final Connection cnx = myconnexion.getInstance().getConnection();

    public void ajouter(Commentaire c) {
        String req = "INSERT INTO commentaire (id_user, id_publication, content, date_creation, status, nb_likes, nb_dislikes, parent_id, est_signale, raison_signalement) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            if (c.getIdUser() > 0) {
                ps.setInt(1, c.getIdUser());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, c.getIdPublication());
            ps.setString(3, c.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(c.getDateCreation()));
            ps.setString(5, c.getStatus());
            ps.setInt(6, c.getNbLikes());
            ps.setInt(7, c.getNbDislikes());
            ps.setInt(8, c.getParentId());
            ps.setBoolean(9, c.isEstSignale());
            ps.setString(10, c.getRaisonSignalement());
            ps.executeUpdate();
            System.out.println("✅ Commentaire ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de commentaire : " + e.getMessage());
        }
    }

    public void modifier(Commentaire c) {
        String req = "UPDATE commentaire SET id_user=?, id_publication=?, content=?, date_creation=?, status=?, nb_likes=?, nb_dislikes=?, parent_id=?, est_signale=?, raison_signalement=? WHERE id_commentaire=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            if (c.getIdUser() > 0) {
                ps.setInt(1, c.getIdUser());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, c.getIdPublication());
            ps.setString(3, c.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(c.getDateCreation()));
            ps.setString(5, c.getStatus());
            ps.setInt(6, c.getNbLikes());
            ps.setInt(7, c.getNbDislikes());
            ps.setInt(8, c.getParentId());
            ps.setBoolean(9, c.isEstSignale());
            ps.setString(10, c.getRaisonSignalement());
            ps.setInt(11, c.getIdCommentaire());
            ps.executeUpdate();
            System.out.println("✅ Commentaire modifié avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification de commentaire : " + e.getMessage());
        }
    }

    public void supprimer(int idCommentaire) {
        String req = "DELETE FROM commentaire WHERE id_commentaire=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idCommentaire);
            ps.executeUpdate();
            System.out.println("✅ Commentaire supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de commentaire : " + e.getMessage());
        }
    }

    public List<Commentaire> afficher() {
        List<Commentaire> liste = new ArrayList<>();
        String req = "SELECT * FROM commentaire";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Commentaire c = new Commentaire(
                        rs.getInt("id_commentaire"),
                        rs.getInt("id_user"),
                        rs.getInt("id_publication"),
                        rs.getString("content"),
                        rs.getTimestamp("date_creation").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getInt("nb_likes"),
                        rs.getInt("nb_dislikes"),
                        rs.getInt("parent_id"),
                        rs.getBoolean("est_signale"),
                        rs.getString("raison_signalement")
                );
                liste.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des commentaires : " + e.getMessage());
        }
        return liste;
    }

    public Commentaire afficherParId(int idCommentaire) {
        String req = "SELECT * FROM commentaire WHERE id_commentaire=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idCommentaire);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Commentaire(
                        rs.getInt("id_commentaire"),
                        rs.getInt("id_user"),
                        rs.getInt("id_publication"),
                        rs.getString("content"),
                        rs.getTimestamp("date_creation").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getInt("nb_likes"),
                        rs.getInt("nb_dislikes"),
                        rs.getInt("parent_id"),
                        rs.getBoolean("est_signale"),
                        rs.getString("raison_signalement")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par ID (commentaire) : " + e.getMessage());
        }
        return null;
    }

    public List<Commentaire> afficherParPublication(int idPublication) {
        List<Commentaire> liste = new ArrayList<>();
        String req = "SELECT * FROM commentaire WHERE id_publication=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idPublication);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Commentaire c = new Commentaire(
                        rs.getInt("id_commentaire"),
                        rs.getInt("id_user"),
                        rs.getInt("id_publication"),
                        rs.getString("content"),
                        rs.getTimestamp("date_creation").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getInt("nb_likes"),
                        rs.getInt("nb_dislikes"),
                        rs.getInt("parent_id"),
                        rs.getBoolean("est_signale"),
                        rs.getString("raison_signalement")
                );
                liste.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des commentaires par publication : " + e.getMessage());
        }
        return liste;
    }
}
