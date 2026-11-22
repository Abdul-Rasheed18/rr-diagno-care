package com.rrdiagnocare.dao;

import com.rrdiagnocare.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/** Persists and reads the AOP audit trail. */
@Repository
public class AuditLogDao {

    private static final RowMapper<AuditLog> MAPPER = (rs, i) -> {
        AuditLog a = new AuditLog();
        a.setId(rs.getLong("id"));
        a.setUsername(rs.getString("username"));
        a.setAction(rs.getString("action"));
        a.setEntityType(rs.getString("entity_type"));
        long entityId = rs.getLong("entity_id");
        a.setEntityId(rs.wasNull() ? null : entityId);
        a.setDetails(rs.getString("details"));
        a.setOutcome(rs.getString("outcome"));
        a.setExecutionMs(rs.getLong("execution_ms"));
        a.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return a;
    };

    private final JdbcTemplate jdbc;

    public AuditLogDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(AuditLog log) {
        String details = log.getDetails();
        if (details != null && details.length() > 500) {
            details = details.substring(0, 497) + "...";
        }
        jdbc.update("INSERT INTO audit_logs (username, action, entity_type, entity_id, details, outcome, execution_ms) "
                        + "VALUES (?,?,?,?,?,?,?)",
                log.getUsername(), log.getAction(), log.getEntityType(), log.getEntityId(), details,
                log.getOutcome(), log.getExecutionMs());
    }

    public List<AuditLog> findRecent(int limit) {
        return jdbc.query("SELECT * FROM audit_logs ORDER BY created_at DESC, id DESC LIMIT ?", MAPPER, limit);
    }
}
