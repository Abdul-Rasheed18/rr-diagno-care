package com.rrdiagnocare.model;

import com.rrdiagnocare.util.DateUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/** A patient appointment with its purchased items and per-test results. */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String bookingRef;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientGender;
    private LocalDate patientDob;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String visitMode;
    private String collectionAddress;
    private String contactPhone;
    private String paymentMode;
    private String paymentStatus;
    private String status;
    private BigDecimal totalAmount;
    private String reportRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private int testCount;
    private String itemSummary;
    private List<BookingItem> items = new ArrayList<>();
    private List<TestResult> results = new ArrayList<>();

    // ---------- presentation helpers ----------
    public String getAppointmentDateText() { return DateUtil.formatWithDay(appointmentDate); }
    public String getAppointmentDateIso() { return appointmentDate == null ? "" : appointmentDate.toString(); }
    public String getCreatedAtText() { return DateUtil.format(createdAt); }
    public String getCompletedAtText() { return DateUtil.format(completedAt); }

    public String getVisitModeLabel() {
        return "HOME_COLLECTION".equals(visitMode) ? "Home Sample Collection" : "Walk-in at RR Diagno Care";
    }

    public String getPaymentModeLabel() {
        return "PAY_ON_COLLECTION".equals(paymentMode) ? "Pay on Home Collection" : "Pay at Center";
    }

    public String getStatusLabel() {
        return status == null ? "" : status.replace('_', ' ');
    }

    /** Bootstrap colour for the status badge. */
    public String getStatusBadge() {
        if (status == null) return "secondary";
        switch (status) {
            case "BOOKED": return "primary";
            case "SAMPLE_COLLECTED": return "info";
            case "PROCESSING": return "warning";
            case "COMPLETED": return "success";
            case "CANCELLED": return "danger";
            default: return "secondary";
        }
    }

    /** 1..4 position on the progress tracker (0 when cancelled). */
    public int getProgressStep() {
        if (status == null) return 0;
        switch (status) {
            case "BOOKED": return 1;
            case "SAMPLE_COLLECTED": return 2;
            case "PROCESSING": return 3;
            case "COMPLETED": return 4;
            default: return 0;
        }
    }

    public boolean isCancellable() { return "BOOKED".equals(status); }
    public boolean isCompleted() { return "COMPLETED".equals(status); }
    public boolean isPaid() { return "PAID".equals(paymentStatus); }
    public boolean isHomeCollection() { return "HOME_COLLECTION".equals(visitMode); }

    public int getAbnormalCount() {
        int n = 0;
        for (TestResult r : results) {
            if ("LOW".equals(r.getFlag()) || "HIGH".equals(r.getFlag())) n++;
        }
        return n;
    }

    public int getPatientAge() {
        if (patientDob == null) return 0;
        return Period.between(patientDob, DateUtil.today()).getYears();
    }

    // ---------- getters / setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }
    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }
    public LocalDate getPatientDob() { return patientDob; }
    public void setPatientDob(LocalDate patientDob) { this.patientDob = patientDob; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getVisitMode() { return visitMode; }
    public void setVisitMode(String visitMode) { this.visitMode = visitMode; }
    public String getCollectionAddress() { return collectionAddress; }
    public void setCollectionAddress(String collectionAddress) { this.collectionAddress = collectionAddress; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getReportRemarks() { return reportRemarks; }
    public void setReportRemarks(String reportRemarks) { this.reportRemarks = reportRemarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public int getTestCount() { return testCount; }
    public void setTestCount(int testCount) { this.testCount = testCount; }
    public String getItemSummary() { return itemSummary; }
    public void setItemSummary(String itemSummary) { this.itemSummary = itemSummary; }
    public List<BookingItem> getItems() { return items; }
    public void setItems(List<BookingItem> items) { this.items = items; }
    public List<TestResult> getResults() { return results; }
    public void setResults(List<TestResult> results) { this.results = results; }
}
