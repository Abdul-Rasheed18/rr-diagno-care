package com.rrdiagnocare.dao;

import com.rrdiagnocare.model.DailyRevenueReport;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Invokes the MySQL stored procedure sp_get_daily_revenue:
 *  - {@link #getDailyRevenue(LocalDate)} uses Spring's SimpleJdbcCall
 *  - {@link #getRevenueTrend(LocalDate, int)} uses an explicit JDBC CallableStatement
 */
@Repository
public class RevenueReportDao {

    private static final String CALL = "{call sp_get_daily_revenue(?, ?, ?, ?, ?, ?)}";

    private final JdbcTemplate jdbc;
    private final SimpleJdbcCall dailyRevenueCall;

    public RevenueReportDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.dailyRevenueCall = new SimpleJdbcCall(jdbc)
                .withProcedureName("sp_get_daily_revenue")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_date", Types.DATE),
                        new SqlOutParameter("p_total_bookings", Types.INTEGER),
                        new SqlOutParameter("p_cancelled", Types.INTEGER),
                        new SqlOutParameter("p_total_revenue", Types.DECIMAL),
                        new SqlOutParameter("p_collected_revenue", Types.DECIMAL),
                        new SqlOutParameter("p_pending_revenue", Types.DECIMAL));
    }

    /** SimpleJdbcCall variant. */
    public DailyRevenueReport getDailyRevenue(LocalDate date) {
        Map<String, Object> out = dailyRevenueCall.execute(new MapSqlParameterSource("p_date", Date.valueOf(date)));
        DailyRevenueReport r = new DailyRevenueReport();
        r.setReportDate(date);
        r.setTotalBookings(toInt(out.get("p_total_bookings")));
        r.setCancelledBookings(toInt(out.get("p_cancelled")));
        r.setTotalRevenue(toDecimal(out.get("p_total_revenue")));
        r.setCollectedRevenue(toDecimal(out.get("p_collected_revenue")));
        r.setPendingRevenue(toDecimal(out.get("p_pending_revenue")));
        return r;
    }

    /** Explicit CallableStatement variant: one prepared call re-executed for each of the last N days (oldest first). */
    public List<DailyRevenueReport> getRevenueTrend(LocalDate endDate, int days) {
        return jdbc.execute(CALL, (CallableStatementCallback<List<DailyRevenueReport>>) (CallableStatement cs) -> {
            List<DailyRevenueReport> list = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                LocalDate d = endDate.minusDays(i);
                cs.setDate(1, Date.valueOf(d));
                cs.registerOutParameter(2, Types.INTEGER);
                cs.registerOutParameter(3, Types.INTEGER);
                cs.registerOutParameter(4, Types.DECIMAL);
                cs.registerOutParameter(5, Types.DECIMAL);
                cs.registerOutParameter(6, Types.DECIMAL);
                cs.execute();
                DailyRevenueReport r = new DailyRevenueReport();
                r.setReportDate(d);
                r.setTotalBookings(cs.getInt(2));
                r.setCancelledBookings(cs.getInt(3));
                r.setTotalRevenue(nz(cs.getBigDecimal(4)));
                r.setCollectedRevenue(nz(cs.getBigDecimal(5)));
                r.setPendingRevenue(nz(cs.getBigDecimal(6)));
                list.add(r);
            }
            return list;
        });
    }

    private static int toInt(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    private static BigDecimal toDecimal(Object o) {
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        return BigDecimal.ZERO;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
