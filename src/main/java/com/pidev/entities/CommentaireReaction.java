package com.pidev.entities;

public class CommentaireReaction {
    private int idReaction;
    private int idUser;
    private int idCommentaire;
    private boolean isLike;

    public CommentaireReaction() {
    }

    public CommentaireReaction(int idReaction, int idUser, int idCommentaire, boolean isLike) {
        this.idReaction = idReaction;
        this.idUser = idUser;
        this.idCommentaire = idCommentaire;
        this.isLike = isLike;
    }

    public int getIdReaction() {
        return idReaction;
    }

    public void setIdReaction(int idReaction) {
        this.idReaction = idReaction;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdCommentaire() {
        return idCommentaire;
    }

    public void setIdCommentaire(int idCommentaire) {
        this.idCommentaire = idCommentaire;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    @Override
    public String toString() {
        return "CommentaireReaction{" +
                "idReaction=" + idReaction +
                ", idUser=" + idUser +
                ", idCommentaire=" + idCommentaire +
                ", isLike=" + isLike +
                '}';
    }
}
