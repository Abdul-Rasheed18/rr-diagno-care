package com.rrdiagnocare.servlet;

import com.rrdiagnocare.exception.ResourceNotFoundException;
import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.service.BookingService;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.DateUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * GET /print/receipt?id={bookingId}
 * Renders a printable booking receipt. Without an id it falls back to the last booking
 * stored in the HttpSession right after checkout. Forwards to receipt.jsp with RequestDispatcher.
 */
public class BookingReceiptServlet extends BaseSpringServlet {
    private static final long serialVersionUID = 1L;

    private transient BookingService bookingService;

    @Override
    public void init() throws ServletException {
        bookingService = bean(BookingService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Long id = parseId(req.getParameter("id"));
        if (id == null) {
            Object last = session.getAttribute(AppConstants.SESSION_LAST_BOOKING_ID);
            id = last instanceof Long ? (Long) last : null;
        }
        if (id == null) {
            notFound(req, resp, "No booking selected for the receipt.");
            return;
        }
        try {
            Booking booking = bookingService.getBookingForUser(id, currentUser());
            req.setAttribute("booking", booking);
            req.setAttribute("printedAt", DateUtil.format(LocalDateTime.now(DateUtil.ZONE)));
            req.setAttribute("justBooked", id.equals(session.getAttribute(AppConstants.SESSION_LAST_BOOKING_ID)));
            resp.setHeader("Cache-Control", "no-store");
            forward(req, resp, "receipt.jsp");
        } catch (ResourceNotFoundException e) {
            notFound(req, resp, e.getMessage());
        }
    }
}
