package com.rrdiagnocare.service;

import com.rrdiagnocare.dao.BookingDao;
import com.rrdiagnocare.dao.TimeSlotDao;
import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.exception.ResourceNotFoundException;
import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.model.BookingItem;
import com.rrdiagnocare.model.CartItem;
import com.rrdiagnocare.model.CheckoutForm;
import com.rrdiagnocare.model.DiagnosticTest;
import com.rrdiagnocare.model.HealthPackage;
import com.rrdiagnocare.model.TimeSlot;
import com.rrdiagnocare.security.AppUserPrincipal;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.DateUtil;
import com.rrdiagnocare.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Patient bookings: slot availability, atomic booking creation, cancellation and admin booking management. */
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final DateTimeFormatter REF_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final BookingDao bookingDao;
    private final TimeSlotDao timeSlotDao;
    private final CatalogService catalogService;

    public BookingService(BookingDao bookingDao, TimeSlotDao timeSlotDao, CatalogService catalogService) {
        this.bookingDao = bookingDao;
        this.timeSlotDao = timeSlotDao;
        this.catalogService = catalogService;
    }

    public List<TimeSlot> slotsFor(LocalDate date) {
        return timeSlotDao.findWithAvailability(date);
    }

    /**
     * Creates the booking, its purchased items and the empty per-test result rows in ONE transaction.
     * Any exception (validation, full slot, SQL error) rolls back bookings + booking_items + test_results together.
     */
    @Transactional(rollbackFor = Exception.class)
    public Booking createBooking(AppUserPrincipal patient, List<CartItem> cart, CheckoutForm form) {
        if (cart == null || cart.isEmpty()) {
            throw new BusinessException("Your cart is empty. Please add at least one test or package.");
        }
        LocalDate date = DateUtil.parseIso(form.getAppointmentDate());
        if (!ValidationUtil.isValidAppointmentDate(date)) {
            throw new BusinessException("Please choose a future appointment date between tomorrow and "
                    + DateUtil.format(DateUtil.today().plusDays(AppConstants.MAX_DAYS_AHEAD)) + ".");
        }
        if (ValidationUtil.isBlank(form.getTimeSlot())) {
            throw new BusinessException("Please select a time slot.");
        }
        String visitMode = form.getVisitMode();
        if (!AppConstants.VISIT_WALK_IN.equals(visitMode) && !AppConstants.VISIT_HOME.equals(visitMode)) {
            throw new BusinessException("Please choose Walk-in or Home Sample Collection.");
        }
        String address = null;
        if (AppConstants.VISIT_HOME.equals(visitMode)) {
            address = form.getCollectionAddress() == null ? "" : form.getCollectionAddress().trim();
            if (address.length() < 10) {
                throw new BusinessException("Please enter the full address for home sample collection.");
            }
        }
        String paymentMode = form.getPaymentMode();
        if (!AppConstants.PAY_AT_CENTER.equals(paymentMode) && !AppConstants.PAY_ON_COLLECTION.equals(paymentMode)) {
            throw new BusinessException("Please choose an offline payment option.");
        }
        if (AppConstants.VISIT_WALK_IN.equals(visitMode) && AppConstants.PAY_ON_COLLECTION.equals(paymentMode)) {
            throw new BusinessException("'Pay on Home Collection' is only available for home sample collection.");
        }
        if (!ValidationUtil.isValidPhone(form.getContactPhone())) {
            throw new BusinessException("Please enter a valid 10-digit mobile number (starting with 6-9).");
        }

        // Re-price every cart line from the database and expand packages into individual tests
        List<BookingItem> items = new ArrayList<>();
        Set<Long> testIds = new LinkedHashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        List<CartItem> snapshot;
        synchronized (cart) {
            snapshot = new ArrayList<>(cart);
        }
        for (CartItem ci : snapshot) {
            if (ci.isPackage()) {
                HealthPackage p = catalogService.getPackage(ci.getItemId());
                if (!p.isActive()) throw new BusinessException("Package '" + p.getName() + "' is no longer available. Please remove it.");
                items.add(new BookingItem(AppConstants.ITEM_PACKAGE, p.getId(), p.getName(), p.getPrice()));
                testIds.addAll(p.getTestIds());
                total = total.add(p.getPrice());
            } else {
                DiagnosticTest t = catalogService.getTest(ci.getItemId());
                if (!t.isActive()) throw new BusinessException("Test '" + t.getName() + "' is no longer available. Please remove it.");
                items.add(new BookingItem(AppConstants.ITEM_TEST, t.getId(), t.getName(), t.getPrice()));
                testIds.add(t.getId());
                total = total.add(t.getPrice());
            }
        }

        // Slot capacity check under a row lock (SELECT ... FOR UPDATE)
        TimeSlot slot = timeSlotDao.lockSlotForDate(form.getTimeSlot(), date)
                .orElseThrow(() -> new BusinessException("The selected time slot is not valid."));
        if (slot.isFull()) {
            throw new BusinessException("Sorry, the " + slot.getLabel() + " slot on " + DateUtil.format(date)
                    + " is fully booked. Please choose another slot.");
        }

        Booking b = new Booking();
        b.setBookingRef(generateRef(date));
        b.setPatientId(patient.getId());
        b.setAppointmentDate(date);
        b.setTimeSlot(slot.getLabel());
        b.setVisitMode(visitMode);
        b.setCollectionAddress(address);
        b.setContactPhone(ValidationUtil.normalizePhone(form.getContactPhone()));
        b.setPaymentMode(paymentMode);
        b.setPaymentStatus(AppConstants.PAYMENT_PENDING);
        b.setStatus(AppConstants.STATUS_BOOKED);
        b.setTotalAmount(total);

        long id = bookingDao.insertBooking(b);
        bookingDao.batchInsertItems(id, items);
        bookingDao.batchInsertResultRows(id, testIds);
        b.setId(id);
        b.setItems(items);
        log.info("Booking {} created for patient {} ({} items, {} tests, Rs.{})", b.getBookingRef(),
                patient.getUsername(), items.size(), testIds.size(), total);
        return b;
    }

    private String generateRef(LocalDate date) {
        for (int attempt = 0; attempt < 20; attempt++) {
            String ref = "RRDC-" + date.format(REF_DATE) + "-" + (1000 + ThreadLocalRandom.current().nextInt(9000));
            if (!bookingDao.existsByRef(ref)) {
                return ref;
            }
        }
        return "RRDC-" + System.currentTimeMillis();
    }

    // ------------------------------------------------------------- patient views

    public List<Booking> bookingsForPatient(long patientId) {
        return bookingDao.findByPatient(patientId);
    }

    /** Detailed booking, visible to its patient, staff and admins only. */
    public Booking getBookingForUser(long bookingId, AppUserPrincipal user) {
        Booking b = bookingDao.findDetailedById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        boolean privileged = user != null && (user.hasRole(AppConstants.ROLE_ADMIN) || user.hasRole(AppConstants.ROLE_STAFF));
        if (!privileged && (user == null || !b.getPatientId().equals(user.getId()))) {
            throw new ResourceNotFoundException("Booking not found");
        }
        return b;
    }

    public Booking getBooking(long bookingId) {
        return bookingDao.findDetailedById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    @Transactional
    public Booking cancelBooking(long bookingId, AppUserPrincipal patient) {
        Booking b = getBookingForUser(bookingId, patient);
        if (!b.isCancellable()) {
            throw new BusinessException("Only bookings in BOOKED status can be cancelled. Current status: " + b.getStatusLabel());
        }
        if (bookingDao.updateStatus(bookingId, AppConstants.STATUS_BOOKED, AppConstants.STATUS_CANCELLED) == 0) {
            throw new BusinessException("This booking has just been updated by the lab and can no longer be cancelled.");
        }
        log.info("Booking {} cancelled by {}", b.getBookingRef(), patient.getUsername());
        return b;
    }

    // ------------------------------------------------------------- admin

    public List<Booking> searchBookings(String status, String paymentStatus, LocalDate from, LocalDate to, String q) {
        return bookingDao.search(status, paymentStatus, from, to, q);
    }

    @Transactional
    public void togglePaymentStatus(long bookingId) {
        Booking b = getBooking(bookingId);
        if (AppConstants.STATUS_CANCELLED.equals(b.getStatus())) {
            throw new BusinessException("Payment status cannot be changed for a cancelled booking.");
        }
        String next = b.isPaid() ? AppConstants.PAYMENT_PENDING : AppConstants.PAYMENT_PAID;
        bookingDao.updatePaymentStatus(bookingId, next);
    }

    @Transactional
    public void adminCancel(long bookingId) {
        Booking b = getBooking(bookingId);
        if (AppConstants.STATUS_COMPLETED.equals(b.getStatus()) || AppConstants.STATUS_CANCELLED.equals(b.getStatus())) {
            throw new BusinessException("Completed or already cancelled bookings cannot be cancelled.");
        }
        bookingDao.updateStatus(bookingId, b.getStatus(), AppConstants.STATUS_CANCELLED);
    }
}
