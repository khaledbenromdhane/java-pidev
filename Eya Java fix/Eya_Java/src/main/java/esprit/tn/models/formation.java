package esprit.tn.models;


import java.sql.Date;

public class formation {

    private int id  ;
    private String nom_form,type,description;
    private Date date_form;

    public formation() {
    }

    public formation(int id, String nom_form, String type, String description, Date date_form) {
        this.id = id;
        this.nom_form = nom_form;
        this.type = type;
        this.description = description;
        this.date_form = date_form;
    }
    public formation(String nom_form, String type, String description, Date date_form) {
        this.nom_form = nom_form;
        this.type = type;
        this.description = description;
        this.date_form = date_form;
    }

    public int getId() {
        return id;
    }

    public String getNom_form() {
        return nom_form;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate_form() {
        return date_form;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom_form(String nom_form) {
        this.nom_form = nom_form;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate_form(Date date_form) {
        this.date_form = date_form;
    }

    @Override
    public String toString() {
        return "Formation{" +
                "id=" + id +
                ", nom_form='" + nom_form + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", date_form=" + date_form +
                '}';
    }
}
