package com.rrdiagnocare.model;

import com.rrdiagnocare.util.DateUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Measured value of one test inside a booking, joined with its reference range. */
public class TestResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bookingId;
    private Long testId;
    private String testCode;
    private String testName;
    private String category;
    private String unit;
    private BigDecimal normalMin;
    private BigDecimal normalMax;
    private String resultValue;
    private BigDecimal numericValue;
    private String remarks;
    private String enteredBy;
    private LocalDateTime enteredAt;

    public String getReferenceRange() {
        if (normalMin == null && normalMax == null) return "-";
        return DiagnosticTest.plain(normalMin) + " - " + DiagnosticTest.plain(normalMax);
    }

    public boolean isEntered() { return resultValue != null && !resultValue.isBlank(); }

    /** Used by the CSV export and summary counts; JSPs use c:choose for the highlighting. */
    public String getFlag() {
        if (numericValue == null) return "";
        if (normalMin != null && numericValue.compareTo(normalMin) < 0) return "LOW";
        if (normalMax != null && numericValue.compareTo(normalMax) > 0) return "HIGH";
        return "NORMAL";
    }

    public String getEnteredAtText() { return DateUtil.format(enteredAt); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getTestId() { return testId; }
    public void setTestId(Long testId) { this.testId = testId; }
    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getNormalMin() { return normalMin; }
    public void setNormalMin(BigDecimal normalMin) { this.normalMin = normalMin; }
    public BigDecimal getNormalMax() { return normalMax; }
    public void setNormalMax(BigDecimal normalMax) { this.normalMax = normalMax; }
    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }
    public BigDecimal getNumericValue() { return numericValue; }
    public void setNumericValue(BigDecimal numericValue) { this.numericValue = numericValue; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getEnteredBy() { return enteredBy; }
    public void setEnteredBy(String enteredBy) { this.enteredBy = enteredBy; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
    public void setEnteredAt(LocalDateTime enteredAt) { this.enteredAt = enteredAt; }
}
