package com.pidev.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Participation {
    public static final List<String> STATUTS = List.of(
            "En attente",
            "Confirmée",
            "Annulée"
    );

    public static final List<String> MODES_PAIEMENT = List.of(
            "Carte",
            "Cash"
    );

    private Integer id;
    private Integer userId;
    private Evenement evenement;
    private LocalDate dateParticipation;
    private String statut;
    private Integer nbrParticipation;
    private String modePaiement;
    private boolean scanned = false;
    private LocalDateTime scannedAt;

    public Participation() {
    }

    public Participation(Integer id, Integer userId, Evenement evenement, LocalDate dateParticipation, String statut,
                         Integer nbrParticipation, String modePaiement, boolean scanned, LocalDateTime scannedAt) {
        this.id = id;
        this.userId = userId;
        this.evenement = evenement;
        this.dateParticipation = dateParticipation;
        this.statut = statut;
        this.nbrParticipation = nbrParticipation;
        this.modePaiement = modePaiement;
        this.scanned = scanned;
        this.scannedAt = scannedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    public LocalDate getDateParticipation() {
        return dateParticipation;
    }

    public void setDateParticipation(LocalDate dateParticipation) {
        this.dateParticipation = dateParticipation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getNbrParticipation() {
        return nbrParticipation;
    }

    public void setNbrParticipation(Integer nbrParticipation) {
        this.nbrParticipation = nbrParticipation;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }
}
