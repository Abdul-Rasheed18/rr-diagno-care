package com.rrdiagnocare.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Formatting helpers - JSTL fmt:formatDate does not understand java.time, so models expose pre-formatted text. */
public final class DateUtil {

    public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_DAY = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private DateUtil() { }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static String format(LocalDate date) {
        return date == null ? "" : date.format(DATE);
    }

    public static String formatWithDay(LocalDate date) {
        return date == null ? "" : date.format(DATE_DAY);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME);
    }

    /** Parses yyyy-MM-dd; returns null for blank or invalid input. */
    public static LocalDate parseIso(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
