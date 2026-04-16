package com.pidev.entities;

public class User {

    private int id_user;
    private String nom;
    private String prenom;
    private String password;
    private String email;
    private String telephone;
    private String role;
    private int est_signale = 0; // 0 = active, 1 = banned
    private String raison_signalement;

    // ─── Constructeurs ───────────────────────────────────────────────────────────

    public User() {}

    public User(String nom, String prenom, String password, String email,
                String telephone, String role) {
        this(nom, prenom, password, email, telephone, role, 0, null);
    }

    public User(String nom, String prenom, String password, String email,
                String telephone, String role, int est_signale, String raison_signalement) {
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
        this.role = role;
        this.est_signale = est_signale;
        this.raison_signalement = raison_signalement;
    }

    public User(int id_user, String nom, String prenom, String password,
                String email, String telephone, String role) {
        this(id_user, nom, prenom, password, email, telephone, role, 0, null);
    }

    public User(int id_user, String nom, String prenom, String password,
                String email, String telephone, String role, int est_signale, String raison_signalement) {
        this.id_user = id_user;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
        this.role = role;
        this.est_signale = est_signale;
        this.raison_signalement = raison_signalement;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────────

    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getEst_signale() { return est_signale; }
    public void setEst_signale(int est_signale) { this.est_signale = est_signale; }

    public String getRaison_signalement() { return raison_signalement; }
    public void setRaison_signalement(String raison_signalement) { this.raison_signalement = raison_signalement; }

    public String getStatus() { return est_signale == 1 ? "banned" : "active"; }
    public void setStatus(String status) { this.est_signale = "banned".equals(status) ? 1 : 0; }

    // ─── toString ─────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "User{" +
                "id_user=" + id_user +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
