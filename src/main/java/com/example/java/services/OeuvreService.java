package com.example.java.services;

import com.example.java.entities.Oeuvre;
import com.example.java.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OeuvreService {

    private final Connection connection;

    public OeuvreService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────
    //  AJOUTER
    // ─────────────────────────────────────────────
    public void addOeuvre(Oeuvre oeuvre) throws SQLException {
        String sql = "INSERT INTO oeuvre (id_artiste, titre, prix, etat, annee_realisation, image, description, id_galerie, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, oeuvre.getIdArtiste());
            ps.setString(2, oeuvre.getTitre());
            ps.setDouble(3, oeuvre.getPrix());
            ps.setString(4, oeuvre.getEtat());
            ps.setInt(5, oeuvre.getAnneeRealisation());
            ps.setString(6, oeuvre.getImage());
            ps.setString(7, oeuvre.getDescription());
            if (oeuvre.getIdGalerie() != null)
                ps.setInt(8, oeuvre.getIdGalerie());
            else
                ps.setNull(8, Types.INTEGER);
            ps.setString(9, oeuvre.getStatut() != null ? oeuvre.getStatut() : "disponible");
            ps.executeUpdate();
            System.out.println("✅ Oeuvre ajoutée : " + oeuvre.getTitre());
        }
    }

    // ─────────────────────────────────────────────
    //  MODIFIER
    // ─────────────────────────────────────────────
    public void updateOeuvre(Oeuvre oeuvre) throws SQLException {
        String sql = "UPDATE oeuvre SET id_artiste=?, titre=?, prix=?, etat=?, annee_realisation=?, " +
                "image=?, description=?, id_galerie=?, statut=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, oeuvre.getIdArtiste());
            ps.setString(2, oeuvre.getTitre());
            ps.setDouble(3, oeuvre.getPrix());
            ps.setString(4, oeuvre.getEtat());
            ps.setInt(5, oeuvre.getAnneeRealisation());
            ps.setString(6, oeuvre.getImage());
            ps.setString(7, oeuvre.getDescription());
            if (oeuvre.getIdGalerie() != null)
                ps.setInt(8, oeuvre.getIdGalerie());
            else
                ps.setNull(8, Types.INTEGER);
            ps.setString(9, oeuvre.getStatut());
            ps.setInt(10, oeuvre.getId());
            ps.executeUpdate();
            System.out.println("✅ Oeuvre modifiée : " + oeuvre.getTitre());
        }
    }

    // ─────────────────────────────────────────────
    //  SUPPRIMER
    // ─────────────────────────────────────────────
    public void deleteOeuvre(int id) throws SQLException {
        String sql = "DELETE FROM oeuvre WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Oeuvre supprimée (id=" + id + ")");
        }
    }

    public void markAsSold(int id) throws SQLException {
        String sql = "UPDATE oeuvre SET statut='vendue', date_vente=NOW() WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────
    //  AFFICHER TOUTES
    // ─────────────────────────────────────────────
    public List<Oeuvre> getAllOeuvres() throws SQLException {
        List<Oeuvre> list = new ArrayList<>();
        String sql = "SELECT * FROM oeuvre ORDER BY id DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ─────────────────────────────────────────────
    //  AFFICHER PAR ID
    // ─────────────────────────────────────────────
    public Oeuvre getOeuvreById(int id) throws SQLException {
        String sql = "SELECT * FROM oeuvre WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────
    //  AFFICHER PAR GALERIE (jointure)
    // ─────────────────────────────────────────────
    public List<Oeuvre> getOeuvresByGalerie(int idGalerie) throws SQLException {
        List<Oeuvre> list = new ArrayList<>();
        String sql = "SELECT * FROM oeuvre WHERE id_galerie=? ORDER BY id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idGalerie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ─────────────────────────────────────────────
    //  HELPER MAPPING
    // ─────────────────────────────────────────────
    private Oeuvre mapRow(ResultSet rs) throws SQLException {
        LocalDateTime dateVente = null;
        Timestamp ts = rs.getTimestamp("date_vente");
        if (ts != null) dateVente = ts.toLocalDateTime();

        int idGalerie = rs.getInt("id_galerie");
        Integer idGalOpt = rs.wasNull() ? null : idGalerie;

        return new Oeuvre(
                rs.getInt("id"),
                rs.getInt("id_artiste"),
                rs.getString("titre"),
                rs.getDouble("prix"),
                rs.getString("etat"),
                rs.getInt("annee_realisation"),
                rs.getString("image"),
                rs.getString("description"),
                idGalOpt,
                rs.getString("statut"),
                dateVente
        );
    }
}
