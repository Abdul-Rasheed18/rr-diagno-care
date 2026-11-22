package com.rrdiagnocare.service;

import com.rrdiagnocare.aspect.LabAudited;
import com.rrdiagnocare.dao.BookingDao;
import com.rrdiagnocare.dao.UserDao;
import com.rrdiagnocare.model.User;
import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.exception.ResourceNotFoundException;
import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.model.TestResult;
import com.rrdiagnocare.util.AppConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Lab technician operations. Every mutating method is audited by LabAuditAspect. */
@Service
public class LabService {

    private final BookingDao bookingDao;
    private final UserDao userDao;

    public LabService(BookingDao bookingDao, UserDao userDao) {
        this.bookingDao = bookingDao;
        this.userDao = userDao;
    }

    public List<Booking> worklist(LocalDate date, String status) {
        return bookingDao.findWorklist(date, status);
    }

    public Booking bookingForResultEntry(long bookingId) {
        return bookingDao.findDetailedById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    /** BOOKED -> SAMPLE_COLLECTED -> PROCESSING. */
    @LabAudited(action = "SAMPLE_STATUS_UPDATE")
    @Transactional
    public String advanceStatus(Long bookingId, String newStatus) {
        Booking b = bookingForResultEntry(bookingId);
        String expected;
        if (AppConstants.STATUS_SAMPLE_COLLECTED.equals(newStatus)) {
            expected = AppConstants.STATUS_BOOKED;
        } else if (AppConstants.STATUS_PROCESSING.equals(newStatus)) {
            expected = AppConstants.STATUS_SAMPLE_COLLECTED;
        } else {
            throw new BusinessException("Unsupported status change.");
        }
        if (!expected.equals(b.getStatus()) || bookingDao.updateStatus(bookingId, expected, newStatus) == 0) {
            throw new BusinessException("Booking " + b.getBookingRef() + " is " + b.getStatusLabel()
                    + "; it cannot be moved to " + newStatus.replace('_', ' ') + ".");
        }
        return "Booking " + b.getBookingRef() + ": " + b.getStatus() + " -> " + newStatus;
    }

    /**
     * Saves measured values (batch update) and optionally marks the report COMPLETED.
     *
     * @param values  testId -> entered value (numeric or text such as "Negative")
     * @param remarks testId -> per-test remark
     */
    @LabAudited(action = "RESULT_ENTRY")
    @Transactional(rollbackFor = Exception.class)
    public String enterResults(Long bookingId, Map<Long, String> values, Map<Long, String> remarks,
                               String reportRemarks, boolean complete, String technician) {
        Booking b = bookingForResultEntry(bookingId);
        if (!AppConstants.STATUS_SAMPLE_COLLECTED.equals(b.getStatus()) && !AppConstants.STATUS_PROCESSING.equals(b.getStatus())) {
            throw new BusinessException("Results can only be entered after the sample is collected (current status: "
                    + b.getStatusLabel() + ").");
        }
        List<TestResult> results = b.getResults();
        int entered = 0;
        int abnormal = 0;
        for (TestResult r : results) {
            String raw = values.get(r.getTestId());
            String value = raw == null ? null : raw.trim();
            if (value != null && value.isEmpty()) value = null;
            if (value != null && value.length() > 50) {
                throw new BusinessException("Result for " + r.getTestName() + " is too long (max 50 characters).");
            }
            r.setResultValue(value);
            r.setNumericValue(parseNumber(value));
            String remark = remarks.get(r.getTestId());
            r.setRemarks(remark == null || remark.isBlank() ? null : remark.trim());
            if (value != null) {
                entered++;
                String flag = r.getFlag();
                if ("LOW".equals(flag) || "HIGH".equals(flag)) abnormal++;
            }
        }
        if (complete && entered < results.size()) {
            throw new BusinessException("Please enter all " + results.size() + " results before marking the report COMPLETED ("
                    + entered + " entered).");
        }
        bookingDao.batchUpdateResults(bookingId, results, technician);
        String remarksClean = reportRemarks == null || reportRemarks.isBlank() ? null : reportRemarks.trim();
        if (complete) {
            if (bookingDao.markCompleted(bookingId, remarksClean) == 0) {
                throw new BusinessException("Could not complete the report - booking status changed meanwhile.");
            }
        } else {
            bookingDao.updateRemarks(bookingId, remarksClean);
            if (AppConstants.STATUS_SAMPLE_COLLECTED.equals(b.getStatus())) {
                bookingDao.updateStatus(bookingId, AppConstants.STATUS_SAMPLE_COLLECTED, AppConstants.STATUS_PROCESSING);
            }
        }
        return "Booking " + b.getBookingRef() + ": " + entered + "/" + results.size() + " results saved, "
                + abnormal + " abnormal" + (complete ? ", report marked COMPLETED" : " (draft)");
    }

    private static BigDecimal parseNumber(String value) {
        if (value == null) return null;
        try {
            BigDecimal n = new BigDecimal(value.replace(",", ""));
            return n.abs().compareTo(new BigDecimal("9999999999")) > 0 ? null : n;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String technicianName(String username) {
        return userDao.findByUsername(username).map(User::getFullName).orElse(username);
    }
}
