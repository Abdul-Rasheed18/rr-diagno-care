package com.rrdiagnocare.dao;

import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.model.BookingItem;
import com.rrdiagnocare.model.TestResult;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Bookings, booking items and test results. Multi-row inserts / updates use JdbcTemplate.batchUpdate. */
@Repository
public class BookingDao {

    private static final String SELECT = "SELECT b.*, u.full_name AS patient_name, u.email AS patient_email, "
            + "u.gender AS patient_gender, u.date_of_birth AS patient_dob, "
            + "(SELECT COUNT(*) FROM test_results tr WHERE tr.booking_id = b.id) AS test_count, "
            + "(SELECT GROUP_CONCAT(bi.item_name ORDER BY bi.id SEPARATOR ', ') FROM booking_items bi WHERE bi.booking_id = b.id) AS item_summary "
            + "FROM bookings b JOIN users u ON u.id = b.patient_id ";

    private static final RowMapper<Booking> MAPPER = (rs, i) -> {
        Booking b = new Booking();
        b.setId(rs.getLong("id"));
        b.setBookingRef(rs.getString("booking_ref"));
        b.setPatientId(rs.getLong("patient_id"));
        b.setPatientName(rs.getString("patient_name"));
        b.setPatientEmail(rs.getString("patient_email"));
        b.setPatientGender(rs.getString("patient_gender"));
        b.setPatientDob(rs.getObject("patient_dob", LocalDate.class));
        b.setAppointmentDate(rs.getObject("appointment_date", LocalDate.class));
        b.setTimeSlot(rs.getString("time_slot"));
        b.setVisitMode(rs.getString("visit_mode"));
        b.setCollectionAddress(rs.getString("collection_address"));
        b.setContactPhone(rs.getString("contact_phone"));
        b.setPaymentMode(rs.getString("payment_mode"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setStatus(rs.getString("status"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setReportRemarks(rs.getString("report_remarks"));
        b.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        b.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        b.setCompletedAt(rs.getObject("completed_at", LocalDateTime.class));
        b.setTestCount(rs.getInt("test_count"));
        b.setItemSummary(rs.getString("item_summary"));
        return b;
    };

    private static final RowMapper<BookingItem> ITEM_MAPPER = (rs, i) -> {
        BookingItem it = new BookingItem();
        it.setId(rs.getLong("id"));
        it.setBookingId(rs.getLong("booking_id"));
        it.setItemType(rs.getString("item_type"));
        it.setItemId(rs.getLong("item_id"));
        it.setItemName(rs.getString("item_name"));
        it.setPrice(rs.getBigDecimal("price"));
        return it;
    };

    private static final RowMapper<TestResult> RESULT_MAPPER = (rs, i) -> {
        TestResult r = new TestResult();
        r.setId(rs.getLong("id"));
        r.setBookingId(rs.getLong("booking_id"));
        r.setTestId(rs.getLong("test_id"));
        r.setTestCode(rs.getString("code"));
        r.setTestName(rs.getString("name"));
        r.setCategory(rs.getString("category"));
        r.setUnit(rs.getString("unit"));
        r.setNormalMin(rs.getBigDecimal("normal_min"));
        r.setNormalMax(rs.getBigDecimal("normal_max"));
        r.setResultValue(rs.getString("result_value"));
        r.setNumericValue(rs.getBigDecimal("numeric_value"));
        r.setRemarks(rs.getString("remarks"));
        r.setEnteredBy(rs.getString("entered_by"));
        r.setEnteredAt(rs.getObject("entered_at", LocalDateTime.class));
        return r;
    };

    private final JdbcTemplate jdbc;

    public BookingDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------ inserts

    public long insertBooking(Booking b) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bookings (booking_ref, patient_id, appointment_date, time_slot, visit_mode, collection_address, "
                            + "contact_phone, payment_mode, payment_status, status, total_amount) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, b.getBookingRef());
            ps.setLong(2, b.getPatientId());
            ps.setDate(3, Date.valueOf(b.getAppointmentDate()));
            ps.setString(4, b.getTimeSlot());
            ps.setString(5, b.getVisitMode());
            if (b.getCollectionAddress() == null) ps.setNull(6, Types.VARCHAR); else ps.setString(6, b.getCollectionAddress());
            ps.setString(7, b.getContactPhone());
            ps.setString(8, b.getPaymentMode());
            ps.setString(9, b.getPaymentStatus());
            ps.setString(10, b.getStatus());
            ps.setBigDecimal(11, b.getTotalAmount());
            return ps;
        }, keys);
        Number key = keys.getKey();
        return key == null ? 0 : key.longValue();
    }

    /** Inserts all purchased lines in one JDBC batch. */
    public int[] batchInsertItems(long bookingId, List<BookingItem> items) {
        return jdbc.batchUpdate("INSERT INTO booking_items (booking_id, item_type, item_id, item_name, price) VALUES (?,?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        BookingItem it = items.get(i);
                        ps.setLong(1, bookingId);
                        ps.setString(2, it.getItemType());
                        ps.setLong(3, it.getItemId());
                        ps.setString(4, it.getItemName());
                        ps.setBigDecimal(5, it.getPrice());
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                });
    }

    /** Creates one empty result row per (expanded, de-duplicated) test in one JDBC batch. */
    public int[] batchInsertResultRows(long bookingId, Collection<Long> testIds) {
        List<Object[]> rows = new ArrayList<>();
        for (Long testId : testIds) {
            rows.add(new Object[]{bookingId, testId});
        }
        return jdbc.batchUpdate("INSERT INTO test_results (booking_id, test_id) VALUES (?, ?)", rows);
    }

    // ------------------------------------------------------------------ queries

    public Optional<Booking> findById(long id) {
        return jdbc.query(SELECT + "WHERE b.id = ?", MAPPER, id).stream().findFirst();
    }

    /** Booking with items and results populated. */
    public Optional<Booking> findDetailedById(long id) {
        Optional<Booking> ob = findById(id);
        ob.ifPresent(b -> {
            b.setItems(findItems(id));
            b.setResults(findResults(id));
        });
        return ob;
    }

    public List<BookingItem> findItems(long bookingId) {
        return jdbc.query("SELECT * FROM booking_items WHERE booking_id = ? ORDER BY id", ITEM_MAPPER, bookingId);
    }

    public List<TestResult> findResults(long bookingId) {
        return jdbc.query("SELECT r.*, t.code, t.name, t.category, t.unit, t.normal_min, t.normal_max "
                + "FROM test_results r JOIN diagnostic_tests t ON t.id = r.test_id "
                + "WHERE r.booking_id = ? ORDER BY t.category, t.name", RESULT_MAPPER, bookingId);
    }

    public List<Booking> findByPatient(long patientId) {
        return jdbc.query(SELECT + "WHERE b.patient_id = ? ORDER BY b.appointment_date DESC, b.id DESC", MAPPER, patientId);
    }

    /** Lab worklist for one date, optionally filtered by status. */
    public List<Booking> findWorklist(LocalDate date, String status) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT + "WHERE b.appointment_date = ? ");
        args.add(Date.valueOf(date));
        if (status != null && !status.isBlank()) {
            sql.append("AND b.status = ? ");
            args.add(status);
        } else {
            sql.append("AND b.status <> 'CANCELLED' ");
        }
        sql.append("ORDER BY FIELD(b.status,'BOOKED','SAMPLE_COLLECTED','PROCESSING','COMPLETED','CANCELLED'), b.time_slot");
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    /** Admin search over all bookings. Every argument is optional. */
    public List<Booking> search(String status, String paymentStatus, LocalDate from, LocalDate to, String query) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT + "WHERE 1=1 ");
        if (status != null && !status.isBlank()) {
            sql.append("AND b.status = ? ");
            args.add(status);
        }
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            sql.append("AND b.payment_status = ? ");
            args.add(paymentStatus);
        }
        if (from != null) {
            sql.append("AND b.appointment_date >= ? ");
            args.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append("AND b.appointment_date <= ? ");
            args.add(Date.valueOf(to));
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (b.booking_ref LIKE ? OR u.full_name LIKE ? OR b.contact_phone LIKE ?) ");
            String like = "%" + query.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append("ORDER BY b.appointment_date DESC, b.id DESC LIMIT 500");
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    public List<Booking> findRecent(int limit) {
        return jdbc.query(SELECT + "ORDER BY b.created_at DESC, b.id DESC LIMIT ?", MAPPER, limit);
    }

    public boolean existsByRef(String ref) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE booking_ref = ?", Integer.class, ref);
        return n != null && n > 0;
    }

    // ------------------------------------------------------------------ updates

    /** Conditional status change - returns 0 if the booking was not in the expected status (optimistic guard). */
    public int updateStatus(long id, String expectedStatus, String newStatus) {
        return jdbc.update("UPDATE bookings SET status = ? WHERE id = ? AND status = ?", newStatus, id, expectedStatus);
    }

    public int updatePaymentStatus(long id, String paymentStatus) {
        return jdbc.update("UPDATE bookings SET payment_status = ? WHERE id = ? AND status <> 'CANCELLED'", paymentStatus, id);
    }

    /** Saves all entered result values for a booking in one JDBC batch. */
    public int[] batchUpdateResults(long bookingId, List<TestResult> results, String enteredBy) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return jdbc.batchUpdate("UPDATE test_results SET result_value = ?, numeric_value = ?, remarks = ?, entered_by = ?, entered_at = ? "
                        + "WHERE booking_id = ? AND test_id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TestResult r = results.get(i);
                        ps.setString(1, r.getResultValue());
                        ps.setBigDecimal(2, r.getNumericValue());
                        ps.setString(3, r.getRemarks());
                        if (r.isEntered()) {
                            ps.setString(4, enteredBy);
                            ps.setTimestamp(5, now);
                        } else {
                            ps.setNull(4, Types.VARCHAR);
                            ps.setNull(5, Types.TIMESTAMP);
                        }
                        ps.setLong(6, bookingId);
                        ps.setLong(7, r.getTestId());
                    }

                    @Override
                    public int getBatchSize() {
                        return results.size();
                    }
                });
    }

    public int updateRemarks(long id, String remarks) {
        return jdbc.update("UPDATE bookings SET report_remarks = ? WHERE id = ?", remarks, id);
    }

    public int markCompleted(long id, String remarks) {
        return jdbc.update("UPDATE bookings SET status = 'COMPLETED', report_remarks = ?, completed_at = NOW() "
                + "WHERE id = ? AND status IN ('SAMPLE_COLLECTED','PROCESSING')", remarks, id);
    }

    // ------------------------------------------------------------------ dashboard counts

    public int countAll() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM bookings", Integer.class);
        return n == null ? 0 : n;
    }

    /** Bookings still waiting for a report (not completed, not cancelled). */
    public int countPendingReports() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE status IN ('BOOKED','SAMPLE_COLLECTED','PROCESSING')",
                Integer.class);
        return n == null ? 0 : n;
    }

    public int countByStatus(String status) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE status = ?", Integer.class, status);
        return n == null ? 0 : n;
    }

    public int countForDate(LocalDate date) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE appointment_date = ? AND status <> 'CANCELLED'",
                Integer.class, Date.valueOf(date));
        return n == null ? 0 : n;
    }
}
