package com.rrdiagnocare.controller;

import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.model.Booking;
import com.rrdiagnocare.model.TestResult;
import com.rrdiagnocare.security.AppUserPrincipal;
import com.rrdiagnocare.service.LabService;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.DateUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Lab technician worklist and result entry. */
@Controller
@RequestMapping("/staff")
public class StaffController {

    private final LabService labService;

    public StaffController(LabService labService) {
        this.labService = labService;
    }

    @GetMapping({"", "/", "/worklist"})
    public String worklist(@RequestParam(required = false) String date,
                           @RequestParam(required = false) String status, Model model) {
        LocalDate d = DateUtil.parseIso(date);
        if (d == null) d = DateUtil.today();
        String st = status != null && AppConstants.BOOKING_STATUSES.contains(status) ? status : "";
        List<Booking> bookings = labService.worklist(d, st);
        model.addAttribute("bookings", bookings);
        model.addAttribute("date", d.toString());
        model.addAttribute("dateText", DateUtil.formatWithDay(d));
        model.addAttribute("isToday", d.equals(DateUtil.today()));
        model.addAttribute("status", st);
        model.addAttribute("statuses", AppConstants.BOOKING_STATUSES);
        return "staff/worklist";
    }

    @PostMapping("/bookings/{id}/status")
    public String updateStatus(@PathVariable long id, @RequestParam String newStatus,
                               @RequestParam(required = false) String date,
                               @RequestParam(required = false) String filter, RedirectAttributes ra) {
        try {
            labService.advanceStatus(id, newStatus);
            ra.addFlashAttribute("success", "Status updated to " + newStatus.replace('_', ' ') + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + worklistUrl(date, filter);
    }

    private static String worklistUrl(String date, String filter) {
        UriComponentsBuilder b = UriComponentsBuilder.fromPath("/staff/worklist");
        if (DateUtil.parseIso(date) != null) b.queryParam("date", date);
        if (filter != null && AppConstants.BOOKING_STATUSES.contains(filter)) b.queryParam("status", filter);
        return b.build().toUriString();
    }

    @GetMapping("/results/{id}")
    public String resultForm(@PathVariable long id, Model model) {
        Booking b = labService.bookingForResultEntry(id);
        model.addAttribute("booking", b);
        model.addAttribute("editable", AppConstants.STATUS_SAMPLE_COLLECTED.equals(b.getStatus())
                || AppConstants.STATUS_PROCESSING.equals(b.getStatus()));
        return "staff/enter-result";
    }

    @PostMapping("/results/{id}")
    public String saveResults(@PathVariable long id, @RequestParam(defaultValue = "save") String action,
                              @RequestParam(required = false) String reportRemarks,
                              @AuthenticationPrincipal AppUserPrincipal user,
                              HttpServletRequest request, Model model, RedirectAttributes ra) {
        Booking booking = labService.bookingForResultEntry(id);
        Map<Long, String> values = new HashMap<>();
        Map<Long, String> remarks = new HashMap<>();
        for (TestResult r : booking.getResults()) {
            values.put(r.getTestId(), request.getParameter("value_" + r.getTestId()));
            remarks.put(r.getTestId(), request.getParameter("remark_" + r.getTestId()));
        }
        boolean complete = "complete".equals(action);
        try {
            labService.enterResults(id, values, remarks, reportRemarks, complete, user.getUsername());
        } catch (BusinessException e) {
            // re-render with what the technician typed
            for (TestResult r : booking.getResults()) {
                r.setResultValue(values.get(r.getTestId()));
                r.setRemarks(remarks.get(r.getTestId()));
            }
            booking.setReportRemarks(reportRemarks);
            model.addAttribute("booking", booking);
            model.addAttribute("editable", true);
            model.addAttribute("error", e.getMessage());
            return "staff/enter-result";
        }
        if (complete) {
            ra.addFlashAttribute("success", "Report for " + booking.getBookingRef() + " marked COMPLETED and is now visible to the patient.");
            return "redirect:/staff/worklist?date=" + booking.getAppointmentDateIso();
        }
        ra.addFlashAttribute("success", "Results saved as draft. Booking is now PROCESSING.");
        return "redirect:/staff/results/" + id;
    }
}
