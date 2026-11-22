package com.rrdiagnocare.controller;

import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.model.CartItem;
import com.rrdiagnocare.model.CheckoutForm;
import com.rrdiagnocare.model.TimeSlot;
import com.rrdiagnocare.security.AppUserPrincipal;
import com.rrdiagnocare.service.BookingService;
import com.rrdiagnocare.service.CartService;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.DateUtil;
import com.rrdiagnocare.util.ValidationUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

/** Session cart, slot checkout, My Bookings, report view and cancellation. */
@Controller
public class CartBookingController {

    private final CartService cartService;
    private final BookingService bookingService;

    public CartBookingController(CartService cartService, BookingService bookingService) {
        this.cartService = cartService;
        this.bookingService = bookingService;
    }

    // ------------------------------------------------------------------ cart (public, HttpSession)

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        List<CartItem> cart = cartService.getCart(session);
        model.addAttribute("cartItems", cart);
        model.addAttribute("cartTotal", cartService.total(cart));
        model.addAttribute("overlaps", cartService.overlapWarnings(cart));
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String type, @RequestParam long id,
                            @RequestParam(required = false) String back,
                            HttpSession session, RedirectAttributes ra) {
        try {
            String name = cartService.add(session, type, id);
            ra.addFlashAttribute("success", "'" + name + "' added to your cart.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + safeBack(back, "/tests");
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam String key, HttpSession session, RedirectAttributes ra) {
        cartService.remove(session, key);
        ra.addFlashAttribute("success", "Item removed from cart.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes ra) {
        cartService.clear(session);
        ra.addFlashAttribute("success", "Cart cleared.");
        return "redirect:/cart";
    }

    /** Only allow redirects back to local catalog/home pages. */
    private static String safeBack(String back, String fallback) {
        if (back != null && (back.equals("/") || back.startsWith("/tests") || back.equals("/cart"))
                && !back.contains("//") && !back.contains("\r") && !back.contains("\n")) {
            return back;
        }
        return fallback;
    }

    // ------------------------------------------------------------------ checkout (ROLE_PATIENT)

    @GetMapping("/patient/checkout")
    public String checkoutForm(@AuthenticationPrincipal AppUserPrincipal user, HttpSession session,
                               Model model, RedirectAttributes ra) {
        List<CartItem> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            ra.addFlashAttribute("error", "Your cart is empty. Add tests or packages before checkout.");
            return "redirect:/tests";
        }
        CheckoutForm form = (CheckoutForm) model.asMap().get("form");
        if (form == null) {
            form = new CheckoutForm();
            form.setAppointmentDate(DateUtil.today().plusDays(1).toString());
            form.setContactPhone(user.getPhone());
            form.setCollectionAddress(user.getAddress());
        }
        populateCheckout(model, cart, form);
        return "checkout";
    }

    private void populateCheckout(Model model, List<CartItem> cart, CheckoutForm form) {
        LocalDate date = DateUtil.parseIso(form.getAppointmentDate());
        if (!ValidationUtil.isValidAppointmentDate(date)) {
            date = DateUtil.today().plusDays(1);
        }
        model.addAttribute("form", form);
        model.addAttribute("cartItems", cart);
        model.addAttribute("cartTotal", cartService.total(cart));
        model.addAttribute("slots", bookingService.slotsFor(date));
        model.addAttribute("slotDate", DateUtil.formatWithDay(date));
        model.addAttribute("minDate", DateUtil.today().plusDays(1).toString());
        model.addAttribute("maxDate", DateUtil.today().plusDays(AppConstants.MAX_DAYS_AHEAD).toString());
    }

    /** Lightweight JSON used by checkout.jsp to refresh slot availability when the date changes. */
    @GetMapping(value = "/patient/slots", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String slots(@RequestParam String date) {
        LocalDate d = DateUtil.parseIso(date);
        if (!ValidationUtil.isValidAppointmentDate(d)) {
            return "{\"valid\":false,\"message\":\"Please choose a date between tomorrow and "
                    + AppConstants.MAX_DAYS_AHEAD + " days ahead.\",\"slots\":[]}";
        }
        StringBuilder json = new StringBuilder("{\"valid\":true,\"dateText\":\"")
                .append(escape(DateUtil.formatWithDay(d))).append("\",\"slots\":[");
        List<TimeSlot> slots = bookingService.slotsFor(d);
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot s = slots.get(i);
            if (i > 0) json.append(',');
            json.append("{\"label\":\"").append(escape(s.getLabel()))
                    .append("\",\"available\":").append(s.getAvailable())
                    .append(",\"capacity\":").append(s.getCapacity()).append('}');
        }
        return json.append("]}").toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @PostMapping("/patient/checkout")
    public String checkout(@ModelAttribute("form") CheckoutForm form,
                           @AuthenticationPrincipal AppUserPrincipal user,
                           HttpSession session, Model model, RedirectAttributes ra) {
        List<CartItem> cart = cartService.getCart(session);
        try {
            Booking booking = bookingService.createBooking(user, cart, form);
            cartService.clear(session);
            session.setAttribute(AppConstants.SESSION_LAST_BOOKING_ID, booking.getId());
            ra.addFlashAttribute("success", "Booking confirmed! Your booking reference is " + booking.getBookingRef() + ".");
            return "redirect:/print/receipt?id=" + booking.getId();
        } catch (BusinessException e) {
            if (cart.isEmpty()) {
                ra.addFlashAttribute("error", e.getMessage());
                return "redirect:/tests";
            }
            model.addAttribute("error", e.getMessage());
            populateCheckout(model, cart, form);
            return "checkout";
        }
    }

    // ------------------------------------------------------------------ my bookings & reports

    @GetMapping("/patient/bookings")
    public String myBookings(@AuthenticationPrincipal AppUserPrincipal user, Model model) {
        model.addAttribute("bookings", bookingService.bookingsForPatient(user.getId()));
        return "my-bookings";
    }

    @GetMapping("/patient/bookings/{id}")
    public String viewReport(@PathVariable long id, @AuthenticationPrincipal AppUserPrincipal user, Model model) {
        model.addAttribute("booking", bookingService.getBookingForUser(id, user));
        return "view-report";
    }

    @PostMapping("/patient/bookings/{id}/cancel")
    public String cancel(@PathVariable long id, @AuthenticationPrincipal AppUserPrincipal user, RedirectAttributes ra) {
        try {
            Booking b = bookingService.cancelBooking(id, user);
            ra.addFlashAttribute("success", "Booking " + b.getBookingRef() + " has been cancelled.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patient/bookings";
    }
}
