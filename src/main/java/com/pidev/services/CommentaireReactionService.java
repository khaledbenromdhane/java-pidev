package com.pidev.services;

import com.pidev.entities.CommentaireReaction;
import com.pidev.tools.myconnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireReactionService implements IService<CommentaireReaction> {

    private final Connection cnx = myconnexion.getInstance().getConnection();

    @Override
    public void ajouter(CommentaireReaction cr) {
        String req = "INSERT INTO commentaire_reaction (id_user, id_commentaire, is_like) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            if (cr.getIdUser() > 0) {
                ps.setInt(1, cr.getIdUser());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, cr.getIdCommentaire());
            ps.setBoolean(3, cr.isLike());
            ps.executeUpdate();
            System.out.println("✅ Réaction de commentaire ajoutée avec succès !");
            mettreAJourCompteurs(cr.getIdCommentaire());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la réaction : " + e.getMessage());
        }
    }

    @Override
    public void modifier(CommentaireReaction cr) {
        String req = "UPDATE commentaire_reaction SET id_user=?, id_commentaire=?, is_like=? WHERE id_reaction=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            if (cr.getIdUser() > 0) {
                ps.setInt(1, cr.getIdUser());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, cr.getIdCommentaire());
            ps.setBoolean(3, cr.isLike());
            ps.setInt(4, cr.getIdReaction());
            ps.executeUpdate();
            System.out.println("✅ Réaction de commentaire modifiée avec succès !");
            mettreAJourCompteurs(cr.getIdCommentaire());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification de la réaction : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int idReaction) {
        CommentaireReaction cr = afficherParId(idReaction);
        if (cr == null) return;

        String req = "DELETE FROM commentaire_reaction WHERE id_reaction=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idReaction);
            ps.executeUpdate();
            System.out.println("✅ Réaction de commentaire supprimée avec succès !");
            mettreAJourCompteurs(cr.getIdCommentaire());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la réaction : " + e.getMessage());
        }
    }

    @Override
    public List<CommentaireReaction> afficher() {
        List<CommentaireReaction> liste = new ArrayList<>();
        String req = "SELECT * FROM commentaire_reaction";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                CommentaireReaction cr = new CommentaireReaction(
                        rs.getInt("id_reaction"),
                        rs.getInt("id_user"),
                        rs.getInt("id_commentaire"),
                        rs.getBoolean("is_like")
                );
                liste.add(cr);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des réactions : " + e.getMessage());
        }
        return liste;
    }

    @Override
    public CommentaireReaction afficherParId(int idReaction) {
        String req = "SELECT * FROM commentaire_reaction WHERE id_reaction=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idReaction);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new CommentaireReaction(
                        rs.getInt("id_reaction"),
                        rs.getInt("id_user"),
                        rs.getInt("id_commentaire"),
                        rs.getBoolean("is_like")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par ID : " + e.getMessage());
        }
        return null;
    }

    public CommentaireReaction verifierReaction(int idUser, int idCommentaire) {
        String req = "SELECT * FROM commentaire_reaction WHERE id_user=? AND id_commentaire=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idCommentaire);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new CommentaireReaction(
                        rs.getInt("id_reaction"),
                        rs.getInt("id_user"),
                        rs.getInt("id_commentaire"),
                        rs.getBoolean("is_like")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de la réaction : " + e.getMessage());
        }
        return null;
    }

    private void mettreAJourCompteurs(int idCommentaire) {
        int likes = 0;
        int dislikes = 0;
        String reqSelect = "SELECT is_like, COUNT(*) as total FROM commentaire_reaction WHERE id_commentaire=? GROUP BY is_like";
        
        try (PreparedStatement psSelect = cnx.prepareStatement(reqSelect)) {
            psSelect.setInt(1, idCommentaire);
            ResultSet rs = psSelect.executeQuery();
            while (rs.next()) {
                if (rs.getBoolean("is_like")) {
                    likes = rs.getInt("total");
                } else {
                    dislikes = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des compteurs : " + e.getMessage());
        }

        String reqUpdate = "UPDATE commentaire SET nb_likes=?, nb_dislikes=? WHERE id_commentaire=?";
        try (PreparedStatement psUpdate = cnx.prepareStatement(reqUpdate)) {
            psUpdate.setInt(1, likes);
            psUpdate.setInt(2, dislikes);
            psUpdate.setInt(3, idCommentaire);
            psUpdate.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour des compteurs : " + e.getMessage());
        }
    }
}
