package com.pidev.entities;

public class PublicationReaction {
    private int idReaction;
    private int idUser;
    private int idPublication;
    private boolean isLike;

    public PublicationReaction() {
    }

    public PublicationReaction(int idReaction, int idUser, int idPublication, boolean isLike) {
        this.idReaction = idReaction;
        this.idUser = idUser;
        this.idPublication = idPublication;
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

    public int getIdPublication() {
        return idPublication;
    }

    public void setIdPublication(int idPublication) {
        this.idPublication = idPublication;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    @Override
    public String toString() {
        return "PublicationReaction{" +
                "idReaction=" + idReaction +
                ", idUser=" + idUser +
                ", idPublication=" + idPublication +
                ", isLike=" + isLike +
                '}';
    }
}
