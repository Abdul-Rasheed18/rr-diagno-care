package com.rrdiagnocare.service;

import com.rrdiagnocare.dao.AuditLogDao;
import com.rrdiagnocare.dao.BookingDao;
import com.rrdiagnocare.dao.RevenueReportDao;
import com.rrdiagnocare.model.AuditLog;
import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.model.DailyRevenueReport;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.DateUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Aggregates for the admin dashboard (stored-procedure revenue, counts, audit trail). */
@Service
public class DashboardService {

    private final BookingDao bookingDao;
    private final RevenueReportDao revenueReportDao;
    private final AuditLogDao auditLogDao;

    public DashboardService(BookingDao bookingDao, RevenueReportDao revenueReportDao, AuditLogDao auditLogDao) {
        this.bookingDao = bookingDao;
        this.revenueReportDao = revenueReportDao;
        this.auditLogDao = auditLogDao;
    }

    /** Revenue for one day via SimpleJdbcCall -> sp_get_daily_revenue. */
    public DailyRevenueReport dailyRevenue(LocalDate date) {
        return revenueReportDao.getDailyRevenue(date == null ? DateUtil.today() : date);
    }

    /** Last N days via explicit CallableStatement -> sp_get_daily_revenue. */
    public List<DailyRevenueReport> revenueTrend(int days) {
        return revenueReportDao.getRevenueTrend(DateUtil.today(), days);
    }

    public BigDecimal maxRevenue(List<DailyRevenueReport> trend) {
        return trend.stream().map(DailyRevenueReport::getTotalRevenue).reduce(BigDecimal.ONE, BigDecimal::max);
    }

    public Map<String, Integer> statusCounts() {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (String s : AppConstants.BOOKING_STATUSES) {
            m.put(s, bookingDao.countByStatus(s));
        }
        return m;
    }

    public int totalBookings() {
        return bookingDao.countAll();
    }

    public int pendingReports() {
        return bookingDao.countPendingReports();
    }

    public int todaysAppointments() {
        return bookingDao.countForDate(DateUtil.today());
    }

    public List<AuditLog> recentAuditLogs(int limit) {
        return auditLogDao.findRecent(limit);
    }

    public List<Booking> recentBookings(int limit) {
        return bookingDao.findRecent(limit);
    }
}
