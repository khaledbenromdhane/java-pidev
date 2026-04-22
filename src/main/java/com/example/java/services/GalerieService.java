package com.example.java.services;

import com.example.java.entities.Galerie;
import com.example.java.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GalerieService {

    private final Connection connection;

    public GalerieService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────
    //  AJOUTER
    // ─────────────────────────────────────────────
    public void addGalerie(Galerie galerie) throws SQLException {
        String sql = "INSERT INTO galerie (categorie, nom, nb_oeuvres_dispo, nb_employes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, galerie.getCategorie());
            ps.setString(2, galerie.getNom());
            ps.setInt(3, galerie.getNbOeuvresDispo());
            ps.setInt(4, galerie.getNbEmployes());
            ps.executeUpdate();
            System.out.println("✅ Galerie ajoutée : " + galerie.getNom());
        }
    }

    // ─────────────────────────────────────────────
    //  MODIFIER
    // ─────────────────────────────────────────────
    public void updateGalerie(Galerie galerie) throws SQLException {
        String sql = "UPDATE galerie SET categorie=?, nom=?, nb_oeuvres_dispo=?, nb_employes=? WHERE id_galerie=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, galerie.getCategorie());
            ps.setString(2, galerie.getNom());
            ps.setInt(3, galerie.getNbOeuvresDispo());
            ps.setInt(4, galerie.getNbEmployes());
            ps.setInt(5, galerie.getIdGalerie());
            ps.executeUpdate();
            System.out.println("✅ Galerie modifiée : " + galerie.getNom());
        }
    }

    // ─────────────────────────────────────────────
    //  SUPPRIMER
    // ─────────────────────────────────────────────
    public void deleteGalerie(int idGalerie) throws SQLException {
        String sql = "DELETE FROM galerie WHERE id_galerie=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idGalerie);
            ps.executeUpdate();
            System.out.println("✅ Galerie supprimée (id=" + idGalerie + ")");
        }
    }

    // ─────────────────────────────────────────────
    //  AFFICHER TOUTES
    // ─────────────────────────────────────────────
    public List<Galerie> getAllGaleries() throws SQLException {
        List<Galerie> list = new ArrayList<>();
        String sql = "SELECT * FROM galerie ORDER BY id_galerie DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ─────────────────────────────────────────────
    //  AFFICHER PAR ID
    // ─────────────────────────────────────────────
    public Galerie getGalerieById(int idGalerie) throws SQLException {
        String sql = "SELECT * FROM galerie WHERE id_galerie=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idGalerie);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────
    //  HELPER MAPPING
    // ─────────────────────────────────────────────
    private Galerie mapRow(ResultSet rs) throws SQLException {
        return new Galerie(
                rs.getInt("id_galerie"),
                rs.getString("categorie"),
                rs.getString("nom"),
                rs.getInt("nb_oeuvres_dispo"),
                rs.getInt("nb_employes")
        );
    }
}
