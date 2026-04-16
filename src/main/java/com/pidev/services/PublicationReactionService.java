package com.pidev.services;

import com.pidev.entities.PublicationReaction;
import com.pidev.tools.myconnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationReactionService implements IService<PublicationReaction> {

    private final Connection cnx = myconnexion.getInstance().getConnection();

    @Override
    public void ajouter(PublicationReaction pr) {
        String req = "INSERT INTO publication_reaction (id_user, id_publication, is_like) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            if (pr.getIdUser() > 0) {
                ps.setInt(1, pr.getIdUser());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, pr.getIdPublication());
            ps.setBoolean(3, pr.isLike());
            ps.executeUpdate();
            System.out.println("✅ Réaction de publication ajoutée avec succès !");
            mettreAJourCompteurs(pr.getIdPublication());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la réaction : " + e.getMessage());
        }
    }

    @Override
    public void modifier(PublicationReaction pr) {
        String req = "UPDATE publication_reaction SET id_user=?, id_publication=?, is_like=? WHERE id_reaction=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            if (pr.getIdUser() > 0) {
                ps.setInt(1, pr.getIdUser());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, pr.getIdPublication());
            ps.setBoolean(3, pr.isLike());
            ps.setInt(4, pr.getIdReaction());
            ps.executeUpdate();
            System.out.println("✅ Réaction de publication modifiée avec succès !");
            mettreAJourCompteurs(pr.getIdPublication());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification de la réaction : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int idReaction) {
        // We need to know which publication it was to update counters, but we can't easily getaway with 1 arg.
        // I'll fetch it first.
        PublicationReaction pr = afficherParId(idReaction);
        if (pr == null) return;
        
        String req = "DELETE FROM publication_reaction WHERE id_reaction=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idReaction);
            ps.executeUpdate();
            System.out.println("✅ Réaction de publication supprimée avec succès !");
            mettreAJourCompteurs(pr.getIdPublication());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la réaction : " + e.getMessage());
        }
    }

    @Override
    public List<PublicationReaction> afficher() {
        List<PublicationReaction> liste = new ArrayList<>();
        String req = "SELECT * FROM publication_reaction";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                PublicationReaction pr = new PublicationReaction(
                        rs.getInt("id_reaction"),
                        rs.getInt("id_user"),
                        rs.getInt("id_publication"),
                        rs.getBoolean("is_like")
                );
                liste.add(pr);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des réactions : " + e.getMessage());
        }
        return liste;
    }

    @Override
    public PublicationReaction afficherParId(int idReaction) {
        String req = "SELECT * FROM publication_reaction WHERE id_reaction=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idReaction);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PublicationReaction(
                        rs.getInt("id_reaction"),
                        rs.getInt("id_user"),
                        rs.getInt("id_publication"),
                        rs.getBoolean("is_like")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par ID : " + e.getMessage());
        }
        return null;
    }

    public PublicationReaction verifierReaction(int idUser, int idPublication) {
        String req = "SELECT * FROM publication_reaction WHERE id_user=? AND id_publication=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idPublication);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PublicationReaction(
                        rs.getInt("id_reaction"),
                        rs.getInt("id_user"),
                        rs.getInt("id_publication"),
                        rs.getBoolean("is_like")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de la réaction : " + e.getMessage());
        }
        return null;
    }

    private void mettreAJourCompteurs(int idPublication) {
        int likes = 0;
        int dislikes = 0;
        String reqSelect = "SELECT is_like, COUNT(*) as total FROM publication_reaction WHERE id_publication=? GROUP BY is_like";
        
        try (PreparedStatement psSelect = cnx.prepareStatement(reqSelect)) {
            psSelect.setInt(1, idPublication);
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

        String reqUpdate = "UPDATE publication SET nb_likes=?, nb_dislikes=? WHERE id_publication=?";
        try (PreparedStatement psUpdate = cnx.prepareStatement(reqUpdate)) {
            psUpdate.setInt(1, likes);
            psUpdate.setInt(2, dislikes);
            psUpdate.setInt(3, idPublication);
            psUpdate.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour des compteurs : " + e.getMessage());
        }
    }
}
