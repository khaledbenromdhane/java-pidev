package com.pidev.entities;

public class User {

    private int id_user;
    private String nom;
    private String prenom;
    private String password;
    private String email;
    private String telephone;
    private String role;

    // ─── Constructeurs ───────────────────────────────────────────────────────────

    public User() {}

    public User(String nom, String prenom, String password, String email,
                String telephone, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
        this.role = role;
    }

    public User(int id_user, String nom, String prenom, String password,
                String email, String telephone, String role) {
        this.id_user = id_user;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
        this.role = role;
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
