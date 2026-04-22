package com.example.java.entities;

import java.time.LocalDateTime;

public class Oeuvre {
    private int id;
    private int idArtiste;
    private String titre;
    private double prix;
    private String etat;
    private int anneeRealisation;
    private String image;
    private String description;
    private Integer idGalerie;
    private String statut;
    private LocalDateTime dateVente;

    public Oeuvre() {}

    public Oeuvre(int idArtiste, String titre, double prix, String etat,
                  int anneeRealisation, String image, String description,
                  Integer idGalerie, String statut) {
        this.idArtiste = idArtiste;
        this.titre = titre;
        this.prix = prix;
        this.etat = etat;
        this.anneeRealisation = anneeRealisation;
        this.image = image;
        this.description = description;
        this.idGalerie = idGalerie;
        this.statut = statut;
    }

    public Oeuvre(int id, int idArtiste, String titre, double prix, String etat,
                  int anneeRealisation, String image, String description,
                  Integer idGalerie, String statut, LocalDateTime dateVente) {
        this.id = id;
        this.idArtiste = idArtiste;
        this.titre = titre;
        this.prix = prix;
        this.etat = etat;
        this.anneeRealisation = anneeRealisation;
        this.image = image;
        this.description = description;
        this.idGalerie = idGalerie;
        this.statut = statut;
        this.dateVente = dateVente;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdArtiste() { return idArtiste; }
    public void setIdArtiste(int idArtiste) { this.idArtiste = idArtiste; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public int getAnneeRealisation() { return anneeRealisation; }
    public void setAnneeRealisation(int anneeRealisation) { this.anneeRealisation = anneeRealisation; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getIdGalerie() { return idGalerie; }
    public void setIdGalerie(Integer idGalerie) { this.idGalerie = idGalerie; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateVente() { return dateVente; }
    public void setDateVente(LocalDateTime dateVente) { this.dateVente = dateVente; }

    @Override
    public String toString() {
        return "Oeuvre{id=" + id + ", titre='" + titre + "', prix=" + prix + ", statut='" + statut + "'}";
    }
}
