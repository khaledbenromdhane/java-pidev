package com.example.java.entities;

public class Galerie {
    private int idGalerie;
    private String categorie;
    private String nom;
    private int nbOeuvresDispo;
    private int nbEmployes;

    public Galerie() {}

    public Galerie(String categorie, String nom, int nbOeuvresDispo, int nbEmployes) {
        this.categorie = categorie;
        this.nom = nom;
        this.nbOeuvresDispo = nbOeuvresDispo;
        this.nbEmployes = nbEmployes;
    }

    public Galerie(int idGalerie, String categorie, String nom, int nbOeuvresDispo, int nbEmployes) {
        this.idGalerie = idGalerie;
        this.categorie = categorie;
        this.nom = nom;
        this.nbOeuvresDispo = nbOeuvresDispo;
        this.nbEmployes = nbEmployes;
    }

    public int getIdGalerie() { return idGalerie; }
    public void setIdGalerie(int idGalerie) { this.idGalerie = idGalerie; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getNbOeuvresDispo() { return nbOeuvresDispo; }
    public void setNbOeuvresDispo(int nbOeuvresDispo) { this.nbOeuvresDispo = nbOeuvresDispo; }

    public int getNbEmployes() { return nbEmployes; }
    public void setNbEmployes(int nbEmployes) { this.nbEmployes = nbEmployes; }

    @Override
    public String toString() {
        return "Galerie{id=" + idGalerie + ", nom='" + nom + "', categorie='" + categorie + "'}";
    }
}
