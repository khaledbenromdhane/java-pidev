package esprit.tn.models;

public class EvaluationWithFormation {
    private int id;
    private int note;
    private int formation_id;
    private String titre;
    private String commentaire;
    private String nom_formation;

    public EvaluationWithFormation(int id, int note, int formation_id, String titre, String commentaire, String nom_formation) {
        this.id = id;
        this.note = note;
        this.formation_id = formation_id;
        this.titre = titre;
        this.commentaire = commentaire;
        this.nom_formation = nom_formation;
    }

    public int getId() {
        return id;
    }

    public int getNote() {
        return note;
    }

    public int getFormation_id() {
        return formation_id;
    }

    public String getTitre() {
        return titre;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public String getNom_formation() {
        return nom_formation;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public void setFormation_id(int formation_id) {
        this.formation_id = formation_id;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public void setNom_formation(String nom_formation) {
        this.nom_formation = nom_formation;
    }
}

