package com.pidev.entities;

import java.time.LocalDateTime;

public class Commentaire {
    private int idCommentaire;
    private int idUser;
    private int idPublication;
    private String content;
    private LocalDateTime dateCreation;
    private String status;
    private int nbLikes;
    private int nbDislikes;
    private int parentId;
    private boolean estSignale;
    private String raisonSignalement;

    public Commentaire() {
        this.dateCreation = LocalDateTime.now();
        this.nbLikes = 0;
        this.nbDislikes = 0;
        this.parentId = 0;
        this.estSignale = false;
    }

    public Commentaire(int idCommentaire, int idUser, int idPublication, String content, LocalDateTime dateCreation, String status, int nbLikes, int nbDislikes, int parentId, boolean estSignale, String raisonSignalement) {
        this.idCommentaire = idCommentaire;
        this.idUser = idUser;
        this.idPublication = idPublication;
        this.content = content;
        this.dateCreation = dateCreation;
        this.status = status;
        this.nbLikes = nbLikes;
        this.nbDislikes = nbDislikes;
        this.parentId = parentId;
        this.estSignale = estSignale;
        this.raisonSignalement = raisonSignalement;
    }

    public int getIdCommentaire() {
        return idCommentaire;
    }

    public void setIdCommentaire(int idCommentaire) {
        this.idCommentaire = idCommentaire;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdPublication() {
        return idPublication;
    }

    public void setIdPublication(int idPublication) {
        this.idPublication = idPublication;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public boolean isEstSignale() {
        return estSignale;
    }

    public void setEstSignale(boolean estSignale) {
        this.estSignale = estSignale;
    }

    public String getRaisonSignalement() {
        return raisonSignalement;
    }

    public void setRaisonSignalement(String raisonSignalement) {
        this.raisonSignalement = raisonSignalement;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "idCommentaire=" + idCommentaire +
                ", content='" + content + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
