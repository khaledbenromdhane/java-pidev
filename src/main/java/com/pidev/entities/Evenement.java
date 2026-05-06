package com.pidev.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Evenement {
    public static final List<String> TYPES = List.of(
            "Concerts",
            "Expositions d'art",
            "Festivals",
            "Spectacles de danse",
            "Théâtre",
            "Tournois",
            "Formations"
    );

    private Integer id;
    private String nom;
    private Boolean paiement = false;
    private String typeEvenement;
    private Integer nbrParticipant;
    private LocalDate date;
    private String lieu;
    private String description;
    private LocalTime heure;
    private String image;
    private Float prix;

    public Evenement() {
    }

    public Evenement(Integer id, String nom, Boolean paiement, String typeEvenement, Integer nbrParticipant,
                     LocalDate date, String lieu, String description, LocalTime heure, String image, Float prix) {
        this.id = id;
        this.nom = nom;
        this.paiement = paiement;
        this.typeEvenement = typeEvenement;
        this.nbrParticipant = nbrParticipant;
        this.date = date;
        this.lieu = lieu;
        this.description = description;
        this.heure = heure;
        this.image = image;
        this.prix = prix;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Boolean getPaiement() {
        return paiement;
    }

    public void setPaiement(Boolean paiement) {
        this.paiement = paiement;
    }

    public String getTypeEvenement() {
        return typeEvenement;
    }

    public void setTypeEvenement(String typeEvenement) {
        this.typeEvenement = typeEvenement;
    }

    public Integer getNbrParticipant() {
        return nbrParticipant;
    }

    public void setNbrParticipant(Integer nbrParticipant) {
        this.nbrParticipant = nbrParticipant;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Float getPrix() {
        return prix;
    }

    public void setPrix(Float prix) {
        this.prix = prix;
    }
}
