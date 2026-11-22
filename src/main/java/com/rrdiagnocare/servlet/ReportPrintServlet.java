package com.rrdiagnocare.servlet;

import com.rrdiagnocare.exception.ResourceNotFoundException;
import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.security.AppUserPrincipal;
import com.rrdiagnocare.service.BookingService;
import com.rrdiagnocare.service.LabService;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.DateUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * GET /print/report?id={bookingId}
 * Print-ready pathology report. Patients can print only their own COMPLETED reports;
 * staff and admins may also preview reports that are still in progress.
 */
public class ReportPrintServlet extends BaseSpringServlet {
    private static final long serialVersionUID = 1L;

    private transient BookingService bookingService;
    private transient LabService labService;

    @Override
    public void init() throws ServletException {
        bookingService = bean(BookingService.class);
        labService = bean(LabService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = parseId(req.getParameter("id"));
        if (id == null) {
            notFound(req, resp, "No report selected.");
            return;
        }
        try {
            AppUserPrincipal user = currentUser();
            Booking booking = bookingService.getBookingForUser(id, user);
            boolean patientOnly = user != null && user.hasRole(AppConstants.ROLE_PATIENT);
            if (patientOnly && !booking.isCompleted()) {
                notFound(req, resp, "The report for " + booking.getBookingRef() + " is not ready yet (status: "
                        + booking.getStatusLabel() + ").");
                return;
            }
            String technician = booking.getResults().stream()
                    .map(r -> r.getEnteredBy()).filter(u -> u != null).findFirst()
                    .map(labService::technicianName).orElse("-");
            req.setAttribute("booking", booking);
            req.setAttribute("technician", technician);
            req.setAttribute("printedAt", DateUtil.format(LocalDateTime.now(DateUtil.ZONE)));
            req.setAttribute("draft", !booking.isCompleted());
            resp.setHeader("Cache-Control", "no-store");
            forward(req, resp, "report-print.jsp");
        } catch (ResourceNotFoundException e) {
            notFound(req, resp, e.getMessage());
        }
    }
}
