package esprit.tn.models;



public class evaluation {

    private int id, note, formation_id;
    private String titre, commentaire;

    public evaluation() {
    }

    public evaluation(int id, int note, int formation_id, String titre, String commentaire) {
        this.id = id;
        this.note = note;
        this.formation_id = formation_id;
        this.titre = titre;
        this.commentaire = commentaire;
    }

    public evaluation(int note, int formation_id, String titre, String commentaire) {
        this.note = note;
        this.formation_id = formation_id;
        this.titre = titre;
        this.commentaire = commentaire;
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

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", note=" + note +
                ", formation_id=" + formation_id +
                ", titre='" + titre + '\'' +
                ", commentaire='" + commentaire + '\'' +
                '}';
    }

}


