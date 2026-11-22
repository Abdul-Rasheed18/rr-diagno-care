package com.rrdiagnocare.model;

import com.rrdiagnocare.util.DateUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Output of the MySQL stored procedure sp_get_daily_revenue for one date. */
public class DailyRevenueReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate reportDate;
    private int totalBookings;
    private int cancelledBookings;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal collectedRevenue = BigDecimal.ZERO;
    private BigDecimal pendingRevenue = BigDecimal.ZERO;

    public String getReportDateText() { return DateUtil.format(reportDate); }

    public String getShortDayLabel() {
        return reportDate == null ? "" : reportDate.format(DateTimeFormatter.ofPattern("EEE dd"));
    }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }
    public int getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(int cancelledBookings) { this.cancelledBookings = cancelledBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public BigDecimal getCollectedRevenue() { return collectedRevenue; }
    public void setCollectedRevenue(BigDecimal collectedRevenue) { this.collectedRevenue = collectedRevenue; }
    public BigDecimal getPendingRevenue() { return pendingRevenue; }
    public void setPendingRevenue(BigDecimal pendingRevenue) { this.pendingRevenue = pendingRevenue; }
}
