package com.rrdiagnocare.servlet;

import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.service.BookingService;
import com.rrdiagnocare.util.DateUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GET /admin/export/bookings.csv?status=&payment=&from=&to=&q=
 * Streams the (filtered) bookings as an Excel-compatible UTF-8 CSV download. Admin only (URL secured by Spring Security).
 */
public class ExportBookingsServlet extends BaseSpringServlet {
    private static final long serialVersionUID = 1L;

    private transient BookingService bookingService;

    @Override
    public void init() throws ServletException {
        bookingService = bean(BookingService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LocalDate from = DateUtil.parseIso(req.getParameter("from"));
        LocalDate to = DateUtil.parseIso(req.getParameter("to"));
        List<Booking> bookings = bookingService.searchBookings(req.getParameter("status"), req.getParameter("payment"),
                from, to, req.getParameter("q"));

        String fileName = "rr-diagno-care-bookings-" + DateUtil.today().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setHeader("Cache-Control", "no-store");

        PrintWriter out = resp.getWriter();
        out.write('﻿'); // BOM so Excel opens UTF-8 correctly
        out.println("Booking Ref,Patient,Phone,Email,Appointment Date,Time Slot,Visit Mode,Collection Address,"
                + "Items,Tests,Total Amount (INR),Payment Mode,Payment Status,Status,Booked On");
        for (Booking b : bookings) {
            out.println(String.join(",",
                    csv(b.getBookingRef()), csv(b.getPatientName()), csv(b.getContactPhone()), csv(b.getPatientEmail()),
                    csv(b.getAppointmentDateIso()), csv(b.getTimeSlot()), csv(b.getVisitModeLabel()),
                    csv(b.getCollectionAddress()), csv(b.getItemSummary()), String.valueOf(b.getTestCount()),
                    b.getTotalAmount() == null ? "0.00" : b.getTotalAmount().toPlainString(),
                    csv(b.getPaymentModeLabel()), csv(b.getPaymentStatus()), csv(b.getStatus()), csv(b.getCreatedAtText())));
        }
        out.flush();
    }

    /** RFC 4180 quoting plus a guard against spreadsheet formula injection. */
    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        String v = value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
