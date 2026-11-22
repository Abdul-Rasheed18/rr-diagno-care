package com.rrdiagnocare.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Shared constants for statuses, categories and session keys. */
public final class AppConstants {

    private AppConstants() { }

    // Session / context keys
    public static final String SESSION_CART = "cart";
    public static final String SESSION_LAST_BOOKING_ID = "lastBookingId";
    public static final String CTX_ACTIVE_SESSIONS = "activeSessions";
    public static final String CTX_TOTAL_REQUESTS = "totalRequests";

    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_STAFF = "ROLE_STAFF";
    public static final String ROLE_PATIENT = "ROLE_PATIENT";

    // Booking status workflow
    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_SAMPLE_COLLECTED = "SAMPLE_COLLECTED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final List<String> BOOKING_STATUSES = Collections.unmodifiableList(Arrays.asList(
            STATUS_BOOKED, STATUS_SAMPLE_COLLECTED, STATUS_PROCESSING, STATUS_COMPLETED, STATUS_CANCELLED));

    // Visit / payment
    public static final String VISIT_WALK_IN = "WALK_IN";
    public static final String VISIT_HOME = "HOME_COLLECTION";
    public static final String PAY_AT_CENTER = "PAY_AT_CENTER";
    public static final String PAY_ON_COLLECTION = "PAY_ON_COLLECTION";
    public static final String PAYMENT_PENDING = "PENDING";
    public static final String PAYMENT_PAID = "PAID";

    // Cart item types
    public static final String ITEM_TEST = "TEST";
    public static final String ITEM_PACKAGE = "PACKAGE";

    // Catalog categories (packages live under "Full Body Packages")
    public static final String CATEGORY_PACKAGES = "Full Body Packages";
    public static final List<String> TEST_CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            "Blood", "Thyroid", "Diabetes", "Liver & Kidney"));
    public static final List<String> ALL_CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            "Blood", "Thyroid", "Diabetes", "Liver & Kidney", CATEGORY_PACKAGES));

    // Booking window
    public static final int MAX_DAYS_AHEAD = 30;
}
