package com.pidev.services;

import com.pidev.entities.Evenement;
import com.pidev.tools.myconnexion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class EvenementJdbcService implements CrudService<Evenement, Integer> {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long IMAGE_MAX_SIZE = 780L * 1024L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Connection connection;

    public EvenementJdbcService() {
        this.connection = myconnexion.getInstance().getConnection();
    }

    @Override
    public Evenement create(Evenement entity) {
        String sql = """
                INSERT INTO evenement
                (nom, paiement, type_evenement, nbr_participant, date, lieu, description, heure, image, prix)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindEvent(ps, entity);
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
    public Evenement update(Evenement entity) {
        String sql = """
                UPDATE evenement
                   SET nom=?, paiement=?, type_evenement=?, nbr_participant=?, date=?, lieu=?, description=?, heure=?, image=?, prix=?
                 WHERE id_evenement=?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindEvent(ps, entity);
            ps.setInt(11, entity.getId());
            ps.executeUpdate();
            return entity;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        Optional<Evenement> existing = findById(id);
        existing.ifPresent(evt -> {
            if (evt.getImage() != null && !evt.getImage().isBlank()) {
                deleteImageFile(evt.getImage());
            }
        });

        String sql = "DELETE FROM evenement WHERE id_evenement=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Optional<Evenement> findById(Integer id) {
        String sql = "SELECT * FROM evenement WHERE id_evenement=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapEvent(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Evenement> findAll() {
        return searchAndSort("", "", "", "date", "DESC");
    }

    public List<Evenement> findByDateAsc() {
        return searchAndSort("", "", "", "date", "ASC");
    }

    public List<Evenement> searchAndSort(String q, String type, String paiement, String sort, String order) {
        String sortColumn = sanitizeSort(sort);
        String sortOrder = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

        StringBuilder sql = new StringBuilder("SELECT * FROM evenement WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append(" AND (nom LIKE ? OR lieu LIKE ? OR description LIKE ? OR type_evenement LIKE ?)");
            String keyword = "%" + q.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND type_evenement=?");
            params.add(type);
        }

        if (paiement != null && !paiement.isBlank()) {
            sql.append(" AND paiement=?");
            params.add("1".equals(paiement) || "true".equalsIgnoreCase(paiement));
        }

        sql.append(" ORDER BY ").append(sortColumn).append(' ').append(sortOrder);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            List<Evenement> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapEvent(rs));
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int countAll() {
        return scalarInt("SELECT COUNT(*) FROM evenement");
    }

    public int countUpcoming() {
        return scalarInt("SELECT COUNT(*) FROM evenement WHERE date >= CURDATE()");
    }

    public int countPaid() {
        return scalarInt("SELECT COUNT(*) FROM evenement WHERE paiement=1");
    }

    public int sumParticipants() {
        return scalarInt("SELECT COALESCE(SUM(nbr_participant),0) FROM evenement");
    }

    public Map<String, String> validate(Map<String, Object> data, boolean skipDateValidation) {
        Map<String, String> errors = new HashMap<>();

        String nom = str(data.get("nom"));
        if (nom.isBlank()) {
            errors.put("nom", "Le nom de l'événement est obligatoire.");
        } else if (nom.length() < 3) {
            errors.put("nom", "Le nom doit contenir au moins 3 caractères.");
        } else if (nom.length() > 255) {
            errors.put("nom", "Le nom ne doit pas dépasser 255 caractères.");
        } else if (!nom.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-'&\\.,]+$")) {
            errors.put("nom", "Le nom contient des caractères non autorisés.");
        }

        String type = str(data.get("type_evenement"));
        if (type.isBlank()) {
            errors.put("type_evenement", "Le type d'événement est obligatoire.");
        } else if (!Evenement.TYPES.contains(type)) {
            errors.put("type_evenement", "Type d'événement invalide. Les types autorisés sont : " + String.join(", ", Evenement.TYPES) + ".");
        }

        String nbrRaw = str(data.get("nbr_participant"));
        if (nbrRaw.isBlank()) {
            errors.put("nbr_participant", "Le nombre de participants est obligatoire.");
        } else if (!isInteger(nbrRaw)) {
            errors.put("nbr_participant", "Le nombre de participants doit être un nombre entier.");
        } else {
            int nbr = Integer.parseInt(nbrRaw);
            if (nbr < 1) {
                errors.put("nbr_participant", "Le nombre de participants doit être au moins 1.");
            } else if (nbr > 100000) {
                errors.put("nbr_participant", "Le nombre de participants ne peut pas dépasser 100 000.");
            }
        }

        String dateRaw = str(data.get("date"));
        if (!skipDateValidation) {
            if (dateRaw.isBlank()) {
                errors.put("date", "La date de l'événement est obligatoire.");
            } else {
                try {
                    LocalDate date = LocalDate.parse(dateRaw);
                    if (date.isBefore(LocalDate.now())) {
                        errors.put("date", "La date de l'événement doit être aujourd'hui ou dans le futur.");
                    }
                } catch (DateTimeParseException ex) {
                    errors.put("date", "La date n'est pas valide (format attendu : AAAA-MM-JJ).");
                }
            }
        }

        String heureRaw = str(data.get("heure"));
        if (heureRaw.isBlank()) {
            errors.put("heure", "L'heure de l'événement est obligatoire.");
        } else {
            try {
                LocalTime.parse(heureRaw, TIME_FORMATTER);
            } catch (DateTimeParseException ex) {
                errors.put("heure", "L'heure n'est pas valide (format attendu : HH:MM).");
            }
        }

        String lieu = str(data.get("lieu"));
        if (lieu.isBlank()) {
            errors.put("lieu", "Le lieu de l'événement est obligatoire.");
        } else if (lieu.length() < 3) {
            errors.put("lieu", "Le lieu doit contenir au moins 3 caractères.");
        } else if (lieu.length() > 255) {
            errors.put("lieu", "Le lieu ne doit pas dépasser 255 caractères.");
        }

        String description = str(data.get("description"));
        if (description.isBlank()) {
            errors.put("description", "La description est obligatoire.");
        } else if (description.length() < 10) {
            errors.put("description", "La description doit contenir au moins 10 caractères.");
        } else if (description.length() > 2000) {
            errors.put("description", "La description ne doit pas dépasser 2000 caractères.");
        }

        boolean paid = bool(data.get("paiement"));
        String prixRaw = str(data.get("prix"));
        if (paid) {
            if (prixRaw.isBlank()) {
                errors.put("prix", "Le prix est obligatoire pour un événement payant.");
            } else if (!isDecimal(prixRaw)) {
                errors.put("prix", "Le prix doit être un nombre valide.");
            } else {
                double prix = Double.parseDouble(prixRaw);
                if (prix < 0.01d) {
                    errors.put("prix", "Le prix doit être supérieur à 0.");
                } else if (prix > 99999d) {
                    errors.put("prix", "Le prix ne peut pas dépasser 99 999 €.");
                }
            }
        }

        Object imageObject = data.get("image_file");
        if (imageObject instanceof File imageFile) {
            if (!imageFile.exists() || !imageFile.isFile()) {
                errors.put("image", "Fichier image invalide.");
            } else {
                String ext = extensionOf(imageFile.getName());
                String mime = detectMime(imageFile);
                if (!IMAGE_EXTENSIONS.contains(ext) || (mime != null && !IMAGE_MIME_TYPES.contains(mime))) {
                    errors.put("image", "Format d'image non autorisé. Formats acceptés : JPG, PNG, GIF, WEBP.");
                } else if (imageFile.length() > IMAGE_MAX_SIZE) {
                    errors.put("image", "L'image ne doit pas dépasser 780 Ko.");
                }
            }
        } else if (imageObject != null) {
            errors.put("image", "Fichier image invalide.");
        }

        return errors;
    }

    public String storeImage(File imageFile, String oldImageName) {
        if (imageFile == null) {
            return oldImageName;
        }

        try {
            Path uploadDir = Path.of(System.getProperty("user.dir"), "uploads", "evenements");
            Files.createDirectories(uploadDir);

            String ext = extensionOf(imageFile.getName());
            String fileName = UUID.randomUUID() + "." + ext;
            Path destination = uploadDir.resolve(fileName);

            Files.copy(imageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            if (oldImageName != null && !oldImageName.isBlank()) {
                deleteImageFile(oldImageName);
            }

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void deleteImageFile(String imageName) {
        try {
            Path old = Path.of(System.getProperty("user.dir"), "uploads", "evenements", imageName);
            Files.deleteIfExists(old);
        } catch (IOException ignored) {
        }
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

    private String sanitizeSort(String sort) {
        Map<String, String> mapping = Map.of(
                "nom", "nom",
                "date", "date",
                "lieu", "lieu",
                "typeEvenement", "type_evenement",
                "type_evenement", "type_evenement",
                "nbrParticipant", "nbr_participant",
                "nbr_participant", "nbr_participant",
                "paiement", "paiement",
                "heure", "heure"
        );
        return mapping.getOrDefault(sort, "date");
    }

    private void bindEvent(PreparedStatement ps, Evenement entity) throws SQLException {
        ps.setString(1, entity.getNom());
        ps.setBoolean(2, Boolean.TRUE.equals(entity.getPaiement()));
        ps.setString(3, entity.getTypeEvenement());
        ps.setInt(4, entity.getNbrParticipant());
        ps.setDate(5, Date.valueOf(entity.getDate()));
        ps.setString(6, entity.getLieu());
        ps.setString(7, entity.getDescription());
        ps.setTime(8, Time.valueOf(entity.getHeure()));
        ps.setString(9, entity.getImage());
        if (entity.getPrix() == null) {
            ps.setNull(10, java.sql.Types.FLOAT);
        } else {
            ps.setFloat(10, entity.getPrix());
        }
    }

    private Evenement mapEvent(ResultSet rs) throws SQLException {
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
        return evt;
    }

    private String str(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private boolean bool(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        String raw = str(value).toLowerCase(Locale.ROOT);
        return "true".equals(raw) || "1".equals(raw) || "on".equals(raw);
    }

    private boolean isInteger(String raw) {
        try {
            Integer.parseInt(raw);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isDecimal(String raw) {
        try {
            Double.parseDouble(raw.replace(',', '.'));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String extensionOf(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private String detectMime(File file) {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException ex) {
            return null;
        }
    }
}
