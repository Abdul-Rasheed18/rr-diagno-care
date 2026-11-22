package com.rrdiagnocare.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/** Input validation helpers shared by services and controllers. */
public final class ValidationUtil {

    /** Indian mobile number: 10 digits starting with 6-9 (optional +91 / 0 prefix is stripped first). */
    private static final Pattern PHONE = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9._]{2,29}$");

    private ValidationUtil() { }

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String p = phone.replaceAll("[\\s-]", "");
        if (p.startsWith("+91")) {
            p = p.substring(3);
        } else if (p.length() == 11 && p.startsWith("0")) {
            p = p.substring(1);
        }
        return p;
    }

    public static boolean isValidPhone(String phone) {
        String p = normalizePhone(phone);
        return p != null && PHONE.matcher(p).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username.trim()).matches();
    }

    /** Password: at least 8 characters with a letter and a digit. */
    public static boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8
                && password.chars().anyMatch(Character::isLetter)
                && password.chars().anyMatch(Character::isDigit);
    }

    /** Appointment must be strictly in the future (from tomorrow) and within the booking window. */
    public static boolean isValidAppointmentDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        LocalDate today = DateUtil.today();
        return date.isAfter(today) && !date.isAfter(today.plusDays(AppConstants.MAX_DAYS_AHEAD));
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
