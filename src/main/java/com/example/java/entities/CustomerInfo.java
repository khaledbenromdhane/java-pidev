package com.example.java.entities;

public class CustomerInfo {
    private final String prenom;
    private final String nom;
    private final String email;
    private final String telephone;
    private final String adresse;

    public CustomerInfo(String prenom, String nom, String email, String telephone, String adresse) {
        this.prenom = clean(prenom);
        this.nom = clean(nom);
        this.email = clean(email);
        this.telephone = clean(telephone);
        this.adresse = clean(adresse);
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getFullName() {
        String fullName = (prenom + " " + nom).trim();
        return fullName.isBlank() ? "-" : fullName;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
