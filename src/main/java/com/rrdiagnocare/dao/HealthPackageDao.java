package com.rrdiagnocare.dao;

import com.rrdiagnocare.model.DiagnosticTest;
import com.rrdiagnocare.model.HealthPackage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** CRUD for health_packages and their package_tests links (links written with batchUpdate). */
@Repository
public class HealthPackageDao {

    private static final RowMapper<HealthPackage> MAPPER = (rs, i) -> {
        HealthPackage p = new HealthPackage();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setPreparationNotes(rs.getString("preparation_notes"));
        p.setFeatured(rs.getBoolean("featured"));
        p.setActive(rs.getBoolean("active"));
        return p;
    };

    private static final String SELECT = "SELECT id, name, description, price, preparation_notes, featured, active FROM health_packages ";

    private final JdbcTemplate jdbc;

    public HealthPackageDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<HealthPackage> findAll(boolean activeOnly) {
        return withTests(jdbc.query(SELECT + (activeOnly ? "WHERE active = 1 " : "") + "ORDER BY price", MAPPER));
    }

    public List<HealthPackage> findFeatured() {
        return withTests(jdbc.query(SELECT + "WHERE active = 1 AND featured = 1 ORDER BY price", MAPPER));
    }

    public List<HealthPackage> search(String query, boolean activeOnly) {
        if (query == null || query.isBlank()) {
            return findAll(activeOnly);
        }
        String like = "%" + query.trim() + "%";
        return withTests(jdbc.query(SELECT + "WHERE (name LIKE ? OR description LIKE ?) "
                + (activeOnly ? "AND active = 1 " : "") + "ORDER BY price", MAPPER, like, like));
    }

    public Optional<HealthPackage> findById(long id) {
        List<HealthPackage> list = withTests(jdbc.query(SELECT + "WHERE id = ?", MAPPER, id));
        return list.stream().findFirst();
    }

    /** Loads bundled tests for all given packages with a single query. */
    private List<HealthPackage> withTests(List<HealthPackage> packages) {
        if (packages.isEmpty()) {
            return packages;
        }
        Map<Long, HealthPackage> byId = new LinkedHashMap<>();
        for (HealthPackage p : packages) {
            byId.put(p.getId(), p);
        }
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < packages.size(); i++) {
            in.append(i == 0 ? "?" : ",?");
        }
        jdbc.query("SELECT pt.package_id, " + DiagnosticTestDao.COLUMNS
                        + " FROM package_tests pt JOIN diagnostic_tests t ON t.id = pt.test_id"
                        + " WHERE pt.package_id IN (" + in + ") ORDER BY t.category, t.name",
                rs -> {
                    DiagnosticTest t = DiagnosticTestDao.MAPPER.mapRow(rs, 0);
                    byId.get(rs.getLong("package_id")).getTests().add(t);
                },
                byId.keySet().toArray());
        return new ArrayList<>(byId.values());
    }

    public long insert(HealthPackage p, List<Long> testIds) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO health_packages (name, description, price, preparation_notes, featured, active) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrice());
            ps.setString(4, p.getPreparationNotes());
            ps.setBoolean(5, p.isFeatured());
            ps.setBoolean(6, p.isActive());
            return ps;
        }, keys);
        Number key = keys.getKey();
        long id = key == null ? 0 : key.longValue();
        insertLinks(id, testIds);
        return id;
    }

    public void update(HealthPackage p, List<Long> testIds) {
        jdbc.update("UPDATE health_packages SET name=?, description=?, price=?, preparation_notes=?, featured=?, active=? WHERE id=?",
                p.getName(), p.getDescription(), p.getPrice(), p.getPreparationNotes(), p.isFeatured(), p.isActive(), p.getId());
        jdbc.update("DELETE FROM package_tests WHERE package_id = ?", p.getId());
        insertLinks(p.getId(), testIds);
    }

    /** Batch insert of package -> test links. */
    private void insertLinks(long packageId, List<Long> testIds) {
        List<Object[]> rows = new ArrayList<>();
        for (Long testId : testIds) {
            rows.add(new Object[]{packageId, testId});
        }
        jdbc.batchUpdate("INSERT INTO package_tests (package_id, test_id) VALUES (?, ?)", rows);
    }

    public boolean existsByName(String name, Long excludeId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM health_packages WHERE name = ? AND id <> ?",
                Integer.class, name, excludeId == null ? -1L : excludeId);
        return n != null && n > 0;
    }

    public int toggleActive(long id) {
        return jdbc.update("UPDATE health_packages SET active = NOT active WHERE id = ?", id);
    }

    public int delete(long id) {
        return jdbc.update("DELETE FROM health_packages WHERE id = ?", id);
    }

    public int countActive() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM health_packages WHERE active = 1", Integer.class);
        return n == null ? 0 : n;
    }
}
