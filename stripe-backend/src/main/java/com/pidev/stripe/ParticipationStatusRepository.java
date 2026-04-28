package com.pidev.stripe;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Repository
public class ParticipationStatusRepository {
    private final JdbcTemplate jdbcTemplate;
    private String tableName;

    public ParticipationStatusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void updateStatus(int participationId, String status) {
        String table = resolveTableName();
        if (table == null) {
            return;
        }
        String sql = "UPDATE " + table + " SET statut=? WHERE id_participation=?";
        jdbcTemplate.update(sql, status, participationId);
    }

    private synchronized String resolveTableName() {
        if (tableName != null) {
            return tableName;
        }
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            if (tableExists(meta, "participation")) {
                tableName = "participation";
                return tableName;
            }
            if (tableExists(meta, "participations")) {
                tableName = "participations";
                return tableName;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean tableExists(DatabaseMetaData meta, String tableName) throws Exception {
        try (ResultSet rs = meta.getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }
}
