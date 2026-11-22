package com.rrdiagnocare.dao;

import com.rrdiagnocare.model.DiagnosticTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** CRUD and catalog search for diagnostic_tests. */
@Repository
public class DiagnosticTestDao {

    static final String COLUMNS = "t.id, t.code, t.name, t.category, t.price, t.sample_type, t.preparation_notes, t.unit, "
            + "t.normal_min, t.normal_max, t.turnaround_hours, t.description, t.active";

    static final RowMapper<DiagnosticTest> MAPPER = (rs, i) -> {
        DiagnosticTest t = new DiagnosticTest();
        t.setId(rs.getLong("id"));
        t.setCode(rs.getString("code"));
        t.setName(rs.getString("name"));
        t.setCategory(rs.getString("category"));
        t.setPrice(rs.getBigDecimal("price"));
        t.setSampleType(rs.getString("sample_type"));
        t.setPreparationNotes(rs.getString("preparation_notes"));
        t.setUnit(rs.getString("unit"));
        t.setNormalMin(rs.getBigDecimal("normal_min"));
        t.setNormalMax(rs.getBigDecimal("normal_max"));
        t.setTurnaroundHours(rs.getInt("turnaround_hours"));
        t.setDescription(rs.getString("description"));
        t.setActive(rs.getBoolean("active"));
        return t;
    };

    private final JdbcTemplate jdbc;

    public DiagnosticTestDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DiagnosticTest> findAll(boolean activeOnly) {
        String sql = "SELECT " + COLUMNS + " FROM diagnostic_tests t "
                + (activeOnly ? "WHERE t.active = 1 " : "") + "ORDER BY t.category, t.name";
        return jdbc.query(sql, MAPPER);
    }

    /** Catalog search: optional category and free-text (name / code / description). */
    public List<DiagnosticTest> search(String category, String query, boolean activeOnly) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM diagnostic_tests t WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        if (activeOnly) {
            sql.append("AND t.active = 1 ");
        }
        if (category != null && !category.isBlank()) {
            sql.append("AND t.category = ? ");
            args.add(category);
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (t.name LIKE ? OR t.code LIKE ? OR t.description LIKE ?) ");
            String like = "%" + query.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append("ORDER BY t.category, t.name");
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    public Optional<DiagnosticTest> findById(long id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM diagnostic_tests t WHERE t.id = ?", MAPPER, id)
                .stream().findFirst();
    }

    public List<DiagnosticTest> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String in = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        return jdbc.query("SELECT " + COLUMNS + " FROM diagnostic_tests t WHERE t.id IN (" + in + ") ORDER BY t.category, t.name",
                MAPPER, ids.toArray());
    }

    public boolean existsByCode(String code, Long excludeId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM diagnostic_tests WHERE code = ? AND id <> ?",
                Integer.class, code, excludeId == null ? -1L : excludeId);
        return n != null && n > 0;
    }

    public long insert(DiagnosticTest t) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO diagnostic_tests (code, name, category, price, sample_type, preparation_notes, unit, "
                            + "normal_min, normal_max, turnaround_hours, description, active) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            bind(ps, t);
            ps.setBoolean(12, t.isActive());
            return ps;
        }, keys);
        Number key = keys.getKey();
        return key == null ? 0 : key.longValue();
    }

    public int update(DiagnosticTest t) {
        return jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE diagnostic_tests SET code=?, name=?, category=?, price=?, sample_type=?, preparation_notes=?, unit=?, "
                            + "normal_min=?, normal_max=?, turnaround_hours=?, description=?, active=? WHERE id=?");
            bind(ps, t);
            ps.setBoolean(12, t.isActive());
            ps.setLong(13, t.getId());
            return ps;
        });
    }

    private static void bind(PreparedStatement ps, DiagnosticTest t) throws java.sql.SQLException {
        ps.setString(1, t.getCode());
        ps.setString(2, t.getName());
        ps.setString(3, t.getCategory());
        ps.setBigDecimal(4, t.getPrice());
        ps.setString(5, t.getSampleType());
        ps.setString(6, t.getPreparationNotes());
        ps.setString(7, t.getUnit());
        ps.setBigDecimal(8, t.getNormalMin());
        ps.setBigDecimal(9, t.getNormalMax());
        ps.setInt(10, t.getTurnaroundHours());
        ps.setString(11, t.getDescription());
    }

    public int toggleActive(long id) {
        return jdbc.update("UPDATE diagnostic_tests SET active = NOT active WHERE id = ?", id);
    }

    /** Hard delete; fails with DataIntegrityViolationException if the test is used by a package or booking. */
    public int delete(long id) {
        return jdbc.update("DELETE FROM diagnostic_tests WHERE id = ?", id);
    }

    public int countActive() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM diagnostic_tests WHERE active = 1", Integer.class);
        return n == null ? 0 : n;
    }
}
