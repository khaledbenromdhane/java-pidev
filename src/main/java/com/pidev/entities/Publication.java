package com.pidev.entities;

import java.time.LocalDateTime;

public class Publication {
    private int idPublication;
    private LocalDateTime dateAct;
    private String description;
    private String titre;
    private String slug;
    private String image;
    private String imageAnalysis;
    private int idUser;
    private int nbLikes;
    private int nbDislikes;

    public Publication() {
        this.dateAct = LocalDateTime.now();
        this.nbLikes = 0;
        this.nbDislikes = 0;
    }

    public Publication(int idPublication, LocalDateTime dateAct, String description, String titre, String slug, String image, String imageAnalysis, int idUser, int nbLikes, int nbDislikes) {
        this.idPublication = idPublication;
        this.dateAct = dateAct;
        this.description = description;
        this.titre = titre;
        this.slug = slug;
        this.image = image;
        this.imageAnalysis = imageAnalysis;
        this.idUser = idUser;
        this.nbLikes = nbLikes;
        this.nbDislikes = nbDislikes;
    }

    public int getIdPublication() {
        return idPublication;
    }

    public void setIdPublication(int idPublication) {
        this.idPublication = idPublication;
    }

    public LocalDateTime getDateAct() {
        return dateAct;
    }

    public void setDateAct(LocalDateTime dateAct) {
        this.dateAct = dateAct;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageAnalysis() {
        return imageAnalysis;
    }

    public void setImageAnalysis(String imageAnalysis) {
        this.imageAnalysis = imageAnalysis;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getNbLikes() {
        return nbLikes;
    }

    public void setNbLikes(int nbLikes) {
        this.nbLikes = nbLikes;
    }

    public int getNbDislikes() {
        return nbDislikes;
    }

    public void setNbDislikes(int nbDislikes) {
        this.nbDislikes = nbDislikes;
    }

    @Override
    public String toString() {
        return "Publication{" +
                "idPublication=" + idPublication +
                ", titre='" + titre + '\'' +
                ", dateAct=" + dateAct +
                '}';
    }
}
