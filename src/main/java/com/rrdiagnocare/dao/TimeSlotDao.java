package com.rrdiagnocare.dao;

import com.rrdiagnocare.model.TimeSlot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Time slots and their per-date availability. */
@Repository
public class TimeSlotDao {

    private static final RowMapper<TimeSlot> MAPPER = (rs, i) -> {
        TimeSlot s = new TimeSlot();
        s.setId(rs.getInt("id"));
        s.setLabel(rs.getString("slot_label"));
        s.setSortOrder(rs.getInt("sort_order"));
        s.setCapacity(rs.getInt("capacity"));
        s.setActive(rs.getBoolean("active"));
        s.setBooked(rs.getInt("booked"));
        return s;
    };

    private final JdbcTemplate jdbc;

    public TimeSlotDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Active slots with the number of non-cancelled bookings already taken on the given date. */
    public List<TimeSlot> findWithAvailability(LocalDate date) {
        return jdbc.query("SELECT s.id, s.slot_label, s.sort_order, s.capacity, s.active, "
                        + "(SELECT COUNT(*) FROM bookings b WHERE b.appointment_date = ? AND b.time_slot = s.slot_label "
                        + "  AND b.status <> 'CANCELLED') AS booked "
                        + "FROM time_slots s WHERE s.active = 1 ORDER BY s.sort_order",
                MAPPER, Date.valueOf(date));
    }

    /**
     * Locks the slot row (SELECT ... FOR UPDATE) inside the booking transaction so two patients
     * cannot both take the last seat, then returns capacity and current count.
     */
    public Optional<TimeSlot> lockSlotForDate(String label, LocalDate date) {
        List<TimeSlot> list = jdbc.query("SELECT s.id, s.slot_label, s.sort_order, s.capacity, s.active, 0 AS booked "
                + "FROM time_slots s WHERE s.slot_label = ? AND s.active = 1 FOR UPDATE", MAPPER, label);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        TimeSlot slot = list.get(0);
        Integer booked = jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE appointment_date = ? AND time_slot = ? "
                + "AND status <> 'CANCELLED'", Integer.class, Date.valueOf(date), label);
        slot.setBooked(booked == null ? 0 : booked);
        return Optional.of(slot);
    }
}
