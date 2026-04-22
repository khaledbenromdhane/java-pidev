package com.example.java.entities;

public class User {
    private int iduser;
    private String nomuser;
    private String prenomuser;
    private String role;

    public User() {}

    public User(int iduser, String nomuser, String prenomuser, String role) {
        this.iduser = iduser;
        this.nomuser = nomuser;
        this.prenomuser = prenomuser;
        this.role = role;
    }

    public User(String nomuser, String prenomuser, String role) {
        this.nomuser = nomuser;
        this.prenomuser = prenomuser;
        this.role = role;
    }

    public int getIduser() { return iduser; }
    public void setIduser(int iduser) { this.iduser = iduser; }

    public String getNomuser() { return nomuser; }
    public void setNomuser(String nomuser) { this.nomuser = nomuser; }

    public String getPrenomuser() { return prenomuser; }
    public void setPrenomuser(String prenomuser) { this.prenomuser = prenomuser; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" +
                "iduser=" + iduser +
                ", nomuser='" + nomuser + '\'' +
                ", prenomuser='" + prenomuser + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
