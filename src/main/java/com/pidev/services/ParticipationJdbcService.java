package com.pidev.services;

import com.pidev.entities.Evenement;
import com.pidev.entities.Participation;
import com.pidev.tools.myconnexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParticipationJdbcService implements CrudService<Participation, Integer> {

    private final Connection connection;
    private final EvenementJdbcService evenementService;

    public ParticipationJdbcService() {
        this.connection = myconnexion.getInstance().getConnection();
        this.evenementService = new EvenementJdbcService();
    }

    @Override
    public Participation create(Participation entity) {
        String sql = """
                INSERT INTO participation
                (id_user, id_evenement, date_participation, statut, nbr_participation, mode_paiement, scanned, scanned_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParticipation(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Participation update(Participation entity) {
        String sql = """
                UPDATE participation
                   SET id_user=?, id_evenement=?, date_participation=?, statut=?, nbr_participation=?, mode_paiement=?, scanned=?, scanned_at=?
                 WHERE id_participation=?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParticipation(ps, entity);
            ps.setInt(9, entity.getId());
            ps.executeUpdate();
            return entity;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM participation WHERE id_participation=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Optional<Participation> findById(Integer id) {
        String sql = """
                SELECT p.id_participation, p.id_user, p.id_evenement, p.date_participation, p.statut, p.nbr_participation, p.mode_paiement,
                       p.scanned, p.scanned_at,
                       e.nom, e.paiement, e.type_evenement, e.nbr_participant, e.date, e.lieu, e.description, e.heure, e.image, e.prix
                  FROM participation p
                  JOIN evenement e ON e.id_evenement = p.id_evenement
                 WHERE p.id_participation=?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapParticipation(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Participation> findAll() {
        return searchAndSort("", "", "", "dateParticipation", "DESC");
    }

    public List<Participation> searchAndSort(String q, String statut, String paiement, String sort, String order) {
        String orderBy = switch (sort) {
            case "nbrParticipation" -> "p.nbr_participation";
            case "statut" -> "p.statut";
            case "modePaiement" -> "p.mode_paiement";
            case "evenement" -> "e.nom";
            default -> "p.date_participation";
        };
        String orderDir = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

        StringBuilder sql = new StringBuilder("""
                SELECT p.id_participation, p.id_user, p.id_evenement, p.date_participation, p.statut, p.nbr_participation, p.mode_paiement,
                       p.scanned, p.scanned_at,
                       e.nom, e.paiement, e.type_evenement, e.nbr_participant, e.date, e.lieu, e.description, e.heure, e.image, e.prix
                  FROM participation p
                  JOIN evenement e ON e.id_evenement = p.id_evenement
                 WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append(" AND (e.nom LIKE ? OR p.statut LIKE ? OR p.mode_paiement LIKE ?)");
            String keyword = "%" + q.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (statut != null && !statut.isBlank()) {
            sql.append(" AND p.statut=?");
            params.add(statut);
        }

        if (paiement != null && !paiement.isBlank()) {
            if ("free".equalsIgnoreCase(paiement)) {
                sql.append(" AND p.mode_paiement IS NULL");
            } else {
                sql.append(" AND p.mode_paiement=?");
                params.add(paiement);
            }
        }

        sql.append(" ORDER BY ").append(orderBy).append(' ').append(orderDir);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            List<Participation> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapParticipation(rs));
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int countAll() {
        return scalarInt("SELECT COUNT(*) FROM participation");
    }

    public int countConfirmed() {
        return scalarInt("SELECT COUNT(*) FROM participation WHERE statut='Confirmée'");
    }

    public int countPending() {
        return scalarInt("SELECT COUNT(*) FROM participation WHERE statut='En attente'");
    }

    public int countCancelled() {
        return scalarInt("SELECT COUNT(*) FROM participation WHERE statut='Annulée'");
    }

    public int sumAllPlaces() {
        return scalarInt("SELECT COALESCE(SUM(nbr_participation),0) FROM participation WHERE statut<>'Annulée'");
    }

    public int sumPlacesReservees(int evenementId) {
        String sql = """
                SELECT COALESCE(SUM(nbr_participation),0)
                  FROM participation
                 WHERE id_evenement=? AND statut<>'Annulée'
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int sumPlacesReserveesExcluding(int evenementId, int excludeParticipationId) {
        String sql = """
                SELECT COALESCE(SUM(nbr_participation),0)
                  FROM participation
                 WHERE id_evenement=? AND statut<>'Annulée' AND id_participation<>?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            ps.setInt(2, excludeParticipationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Map<String, String> validate(Map<String, Object> data, Integer excludeParticipationId, boolean skipDateValidation) {
        Map<String, String> errors = new HashMap<>();

        String idEvenementRaw = str(data.get("id_evenement"));
        Evenement evenement = null;

        if (idEvenementRaw.isBlank()) {
            errors.put("id_evenement", "L'événement est obligatoire.");
        } else if (!isInteger(idEvenementRaw)) {
            errors.put("id_evenement", "L'événement sélectionné n'existe pas.");
        } else {
            int idEvenement = Integer.parseInt(idEvenementRaw);
            evenement = evenementService.findById(idEvenement).orElse(null);
            if (evenement == null) {
                errors.put("id_evenement", "L'événement sélectionné n'existe pas.");
            }
        }

        String dateRaw = str(data.get("date_participation"));
        if (!skipDateValidation) {
            if (dateRaw.isBlank()) {
                errors.put("date_participation", "La date de participation est obligatoire.");
            } else {
                try {
                    LocalDate date = LocalDate.parse(dateRaw);
                    if (date.isBefore(LocalDate.now())) {
                        errors.put("date_participation", "La date de participation ne peut pas être dans le passé.");
                    } else if (evenement != null && evenement.getDate() != null && date.isAfter(evenement.getDate())) {
                        errors.put("date_participation", "La date de participation ne peut pas dépasser la date de l'événement (" + evenement.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").");
                    }
                } catch (DateTimeParseException ex) {
                    errors.put("date_participation", "La date n'est pas valide (format attendu : AAAA-MM-JJ).");
                }
            }
        }

        String nbrRaw = str(data.get("nbr_participation"));
        if (nbrRaw.isBlank()) {
            errors.put("nbr_participation", "Le nombre de places réservées est obligatoire.");
        } else if (!isInteger(nbrRaw)) {
            errors.put("nbr_participation", "Le nombre de places doit être un nombre entier.");
        } else {
            int nbr = Integer.parseInt(nbrRaw);
            if (nbr < 1) {
                errors.put("nbr_participation", "Le nombre de places doit être au moins 1.");
            } else if (nbr > 100000) {
                errors.put("nbr_participation", "Le nombre de places ne peut pas dépasser 100 000.");
            }
        }

        if (!errors.containsKey("id_evenement") && !errors.containsKey("nbr_participation")) {
            int idEvenement = Integer.parseInt(idEvenementRaw);
            int currentReserved = excludeParticipationId == null
                    ? sumPlacesReservees(idEvenement)
                    : sumPlacesReserveesExcluding(idEvenement, excludeParticipationId);

            int remaining = Math.max(0, evenement.getNbrParticipant() - currentReserved);
            int requested = Integer.parseInt(nbrRaw);

            if (remaining <= 0) {
                errors.put("nbr_participation", "Cet événement est complet. Aucune place disponible.");
            } else if (requested > remaining) {
                errors.put("nbr_participation", "Il ne reste que " + remaining + " place(s) disponible(s) pour cet événement.");
            }
        }

        String statut = str(data.get("statut"));
        if (statut.isBlank()) {
            errors.put("statut", "Le statut est obligatoire.");
        } else if (!Participation.STATUTS.contains(statut)) {
            errors.put("statut", "Statut invalide. Les statuts autorisés sont : " + String.join(", ", Participation.STATUTS) + ".");
        }

        if (evenement != null && Boolean.TRUE.equals(evenement.getPaiement())) {
            String mode = str(data.get("mode_paiement"));
            if (mode.isBlank()) {
                errors.put("mode_paiement", "Le mode de paiement est obligatoire pour un événement payant.");
            } else if (!Participation.MODES_PAIEMENT.contains(mode)) {
                errors.put("mode_paiement", "Mode de paiement invalide. Les modes autorisés sont : " + String.join(", ", Participation.MODES_PAIEMENT) + ".");
            }
        }

        return errors;
    }

    private int scalarInt(String sql) {
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void bindParticipation(PreparedStatement ps, Participation entity) throws SQLException {
        if (entity.getUserId() == null) {
            ps.setNull(1, java.sql.Types.INTEGER);
        } else {
            ps.setInt(1, entity.getUserId());
        }
        ps.setInt(2, entity.getEvenement().getId());
        ps.setDate(3, Date.valueOf(entity.getDateParticipation()));
        ps.setString(4, entity.getStatut());
        ps.setInt(5, entity.getNbrParticipation());
        if (entity.getModePaiement() == null || entity.getModePaiement().isBlank()) {
            ps.setNull(6, java.sql.Types.VARCHAR);
        } else {
            ps.setString(6, entity.getModePaiement());
        }
        ps.setBoolean(7, entity.isScanned());
        if (entity.getScannedAt() == null) {
            ps.setNull(8, java.sql.Types.TIMESTAMP);
        } else {
            ps.setTimestamp(8, Timestamp.valueOf(entity.getScannedAt()));
        }
    }

    private Participation mapParticipation(ResultSet rs) throws SQLException {
        Evenement evt = new Evenement();
        evt.setId(rs.getInt("id_evenement"));
        evt.setNom(rs.getString("nom"));
        evt.setPaiement(rs.getBoolean("paiement"));
        evt.setTypeEvenement(rs.getString("type_evenement"));
        evt.setNbrParticipant(rs.getInt("nbr_participant"));
        evt.setDate(rs.getDate("date").toLocalDate());
        evt.setLieu(rs.getString("lieu"));
        evt.setDescription(rs.getString("description"));
        evt.setHeure(rs.getTime("heure").toLocalTime());
        evt.setImage(rs.getString("image"));
        float prix = rs.getFloat("prix");
        evt.setPrix(rs.wasNull() ? null : prix);

        Participation p = new Participation();
        p.setId(rs.getInt("id_participation"));
        int userId = rs.getInt("id_user");
        p.setUserId(rs.wasNull() ? null : userId);
        p.setEvenement(evt);
        p.setDateParticipation(rs.getDate("date_participation").toLocalDate());
        p.setStatut(rs.getString("statut"));
        p.setNbrParticipation(rs.getInt("nbr_participation"));
        p.setModePaiement(rs.getString("mode_paiement"));
        p.setScanned(rs.getBoolean("scanned"));
        Timestamp scannedAt = rs.getTimestamp("scanned_at");
        p.setScannedAt(scannedAt == null ? null : scannedAt.toLocalDateTime());
        return p;
    }

    private boolean isInteger(String raw) {
        try {
            Integer.parseInt(raw);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String str(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
