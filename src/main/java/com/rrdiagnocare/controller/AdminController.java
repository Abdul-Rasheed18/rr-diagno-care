package com.rrdiagnocare.controller;

import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.model.DailyRevenueReport;
import com.rrdiagnocare.model.DiagnosticTest;
import com.rrdiagnocare.model.HealthPackage;
import com.rrdiagnocare.model.User;
import com.rrdiagnocare.service.BookingService;
import com.rrdiagnocare.service.CatalogService;
import com.rrdiagnocare.service.DashboardService;
import com.rrdiagnocare.service.UserService;
import com.rrdiagnocare.servlet.ActiveSessionListener;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletContext;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Admin dashboard, catalog CRUD, booking management and staff accounts. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final CatalogService catalogService;
    private final BookingService bookingService;
    private final UserService userService;
    private final ServletContext servletContext;

    public AdminController(DashboardService dashboardService, CatalogService catalogService,
                           BookingService bookingService, UserService userService, ServletContext servletContext) {
        this.dashboardService = dashboardService;
        this.catalogService = catalogService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.servletContext = servletContext;
    }

    // ------------------------------------------------------------------ dashboard

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@RequestParam(required = false) String date, Model model) {
        LocalDate d = DateUtil.parseIso(date);
        if (d == null) d = DateUtil.today();
        DailyRevenueReport revenue = dashboardService.dailyRevenue(d);           // SimpleJdbcCall
        List<DailyRevenueReport> trend = dashboardService.revenueTrend(7);      // CallableStatement
        model.addAttribute("revenue", revenue);
        model.addAttribute("revenueDate", d.toString());
        model.addAttribute("isToday", d.equals(DateUtil.today()));
        model.addAttribute("trend", trend);
        model.addAttribute("maxTrend", dashboardService.maxRevenue(trend));
        model.addAttribute("totalBookings", dashboardService.totalBookings());
        model.addAttribute("pendingReports", dashboardService.pendingReports());
        model.addAttribute("todaysAppointments", dashboardService.todaysAppointments());
        model.addAttribute("activeSessions", ActiveSessionListener.counter(servletContext).get());
        model.addAttribute("totalRequests", servletContext.getAttribute(AppConstants.CTX_TOTAL_REQUESTS));
        model.addAttribute("statusCounts", dashboardService.statusCounts());
        model.addAttribute("patientCount", userService.countPatients());
        model.addAttribute("auditLogs", dashboardService.recentAuditLogs(10));
        model.addAttribute("recentBookings", dashboardService.recentBookings(6));
        return "admin/dashboard";
    }

    // ------------------------------------------------------------------ tests & packages

    @GetMapping("/tests")
    public String manageTests(@RequestParam(required = false) Long editTest,
                              @RequestParam(required = false) Long editPackage, Model model) {
        model.addAttribute("tests", catalogService.allTests(false));
        model.addAttribute("packages", catalogService.allPackages(false));
        model.addAttribute("activeTests", catalogService.allTests(true));
        model.addAttribute("categories", AppConstants.TEST_CATEGORIES);
        if (!model.containsAttribute("testForm")) {
            model.addAttribute("testForm", editTest != null ? catalogService.getTest(editTest) : new DiagnosticTest());
        }
        if (!model.containsAttribute("packageForm")) {
            model.addAttribute("packageForm", editPackage != null ? catalogService.getPackage(editPackage) : new HealthPackage());
        }
        return "admin/manage-tests";
    }

    @PostMapping("/tests/save")
    public String saveTest(@ModelAttribute("testForm") DiagnosticTest form, BindingResult binding, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            ra.addFlashAttribute("error", "Please enter valid numbers for price, reference range and turnaround hours.");
            return "redirect:/admin/tests" + (form.getId() == null ? "" : "?editTest=" + form.getId()) + "#test-form";
        }
        try {
            boolean isNew = form.getId() == null;
            catalogService.saveTest(form);
            ra.addFlashAttribute("success", "Test '" + form.getName() + "' " + (isNew ? "added." : "updated."));
            return "redirect:/admin/tests";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("testForm", form);
            return "redirect:/admin/tests#test-form";
        }
    }

    @PostMapping("/tests/{id}/toggle")
    public String toggleTest(@PathVariable long id, RedirectAttributes ra) {
        catalogService.toggleTest(id);
        ra.addFlashAttribute("success", "Test status updated.");
        return "redirect:/admin/tests";
    }

    @PostMapping("/tests/{id}/delete")
    public String deleteTest(@PathVariable long id, RedirectAttributes ra) {
        try {
            catalogService.deleteTest(id);
            ra.addFlashAttribute("success", "Test deleted.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tests";
    }

    @PostMapping("/packages/save")
    public String savePackage(@ModelAttribute("packageForm") HealthPackage form, BindingResult binding,
                              @RequestParam(value = "testIds", required = false) List<Long> testIds,
                              RedirectAttributes ra) {
        if (binding.hasErrors()) {
            ra.addFlashAttribute("error", "Please enter a valid package price.");
            return "redirect:/admin/tests" + (form.getId() == null ? "" : "?editPackage=" + form.getId()) + "#package-form";
        }
        try {
            boolean isNew = form.getId() == null;
            catalogService.savePackage(form, testIds);
            ra.addFlashAttribute("success", "Package '" + form.getName() + "' " + (isNew ? "created." : "updated."));
            return "redirect:/admin/tests#packages";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            if (testIds != null) {
                form.setTests(catalogService.allTests(false).stream()
                        .filter(t -> testIds.contains(t.getId())).collect(Collectors.toList()));
            }
            ra.addFlashAttribute("packageForm", form);
            return "redirect:/admin/tests#package-form";
        }
    }

    @PostMapping("/packages/{id}/toggle")
    public String togglePackage(@PathVariable long id, RedirectAttributes ra) {
        catalogService.togglePackage(id);
        ra.addFlashAttribute("success", "Package status updated.");
        return "redirect:/admin/tests#packages";
    }

    @PostMapping("/packages/{id}/delete")
    public String deletePackage(@PathVariable long id, RedirectAttributes ra) {
        catalogService.deletePackage(id);
        ra.addFlashAttribute("success", "Package deleted.");
        return "redirect:/admin/tests#packages";
    }

    // ------------------------------------------------------------------ bookings

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String payment,
                           @RequestParam(required = false) String from,
                           @RequestParam(required = false) String to,
                           @RequestParam(required = false) String q, Model model) {
        String st = status != null && AppConstants.BOOKING_STATUSES.contains(status) ? status : "";
        String pay = AppConstants.PAYMENT_PAID.equals(payment) || AppConstants.PAYMENT_PENDING.equals(payment) ? payment : "";
        LocalDate f = DateUtil.parseIso(from);
        LocalDate t = DateUtil.parseIso(to);
        model.addAttribute("bookings", bookingService.searchBookings(st, pay, f, t, q));
        model.addAttribute("statuses", AppConstants.BOOKING_STATUSES);
        model.addAttribute("status", st);
        model.addAttribute("payment", pay);
        model.addAttribute("from", f == null ? "" : f.toString());
        model.addAttribute("to", t == null ? "" : t.toString());
        model.addAttribute("q", q == null ? "" : q.trim());
        return "admin/bookings";
    }

    @PostMapping("/bookings/{id}/payment")
    public String togglePayment(@PathVariable long id, @RequestParam(required = false) String returnQuery, RedirectAttributes ra) {
        try {
            bookingService.togglePaymentStatus(id);
            ra.addFlashAttribute("success", "Payment status updated.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings" + safeQuery(returnQuery);
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable long id, @RequestParam(required = false) String returnQuery, RedirectAttributes ra) {
        try {
            bookingService.adminCancel(id);
            ra.addFlashAttribute("success", "Booking cancelled.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings" + safeQuery(returnQuery);
    }

    /** Keeps the current filters after a POST; only plain query-string characters are allowed. */
    private static String safeQuery(String q) {
        if (q == null || q.isBlank() || !q.matches("[A-Za-z0-9=&%+._\\-]*")) {
            return "";
        }
        return "?" + q;
    }

    // ------------------------------------------------------------------ staff accounts

    @GetMapping("/staff")
    public String staff(Model model) {
        model.addAttribute("staffList", userService.findStaff());
        if (!model.containsAttribute("staffForm")) {
            model.addAttribute("staffForm", new User());
        }
        return "admin/staff";
    }

    @PostMapping("/staff")
    public String createStaff(@ModelAttribute("staffForm") User form, BindingResult binding,
                              @RequestParam String password, RedirectAttributes ra) {
        try {
            userService.createStaff(form, password);
            ra.addFlashAttribute("success", "Lab Technician account '" + form.getUsername().trim().toLowerCase() + "' created.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("staffForm", form);
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/{id}/toggle")
    public String toggleStaff(@PathVariable long id, RedirectAttributes ra) {
        try {
            userService.toggleStaffEnabled(id);
            ra.addFlashAttribute("success", "Staff account status updated.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }
}
