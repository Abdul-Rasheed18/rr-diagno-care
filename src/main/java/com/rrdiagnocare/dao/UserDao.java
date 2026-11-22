package com.rrdiagnocare.dao;

import com.rrdiagnocare.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** CRUD for the users table using parameterized PreparedStatements via JdbcTemplate. */
@Repository
public class UserDao {

    private static final String SELECT = "SELECT id, full_name, username, email, phone, password_hash, role, gender, "
            + "date_of_birth, address, enabled, created_at FROM users ";

    private static final RowMapper<User> MAPPER = (rs, i) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setFullName(rs.getString("full_name"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setGender(rs.getString("gender"));
        u.setDateOfBirth(rs.getObject("date_of_birth", LocalDate.class));
        u.setAddress(rs.getString("address"));
        u.setEnabled(rs.getBoolean("enabled"));
        u.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return u;
    };

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByUsername(String username) {
        return jdbc.query(SELECT + "WHERE username = ?", MAPPER, username).stream().findFirst();
    }

    public Optional<User> findById(long id) {
        return jdbc.query(SELECT + "WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public List<User> findByRole(String role) {
        return jdbc.query(SELECT + "WHERE role = ? ORDER BY created_at DESC", MAPPER, role);
    }

    public boolean existsByUsername(String username) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        return n != null && n > 0;
    }

    public boolean existsByEmail(String email) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return n != null && n > 0;
    }

    public int countByRole(String role) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE role = ?", Integer.class, role);
        return n == null ? 0 : n;
    }

    public long insert(User u) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (full_name, username, email, phone, password_hash, role, gender, date_of_birth, address, enabled) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getPasswordHash());
            ps.setString(6, u.getRole());
            if (u.getGender() == null) ps.setNull(7, Types.VARCHAR); else ps.setString(7, u.getGender());
            if (u.getDateOfBirth() == null) ps.setNull(8, Types.DATE); else ps.setDate(8, Date.valueOf(u.getDateOfBirth()));
            ps.setString(9, u.getAddress());
            ps.setBoolean(10, u.isEnabled());
            return ps;
        }, keys);
        Number key = keys.getKey();
        return key == null ? 0 : key.longValue();
    }

    public int setEnabled(long id, boolean enabled) {
        return jdbc.update("UPDATE users SET enabled = ? WHERE id = ?", enabled, id);
    }
}
