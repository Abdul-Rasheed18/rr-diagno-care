package com.rrdiagnocare.controller;

import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.model.CartItem;
import com.rrdiagnocare.model.User;
import com.rrdiagnocare.security.AppUserPrincipal;
import com.rrdiagnocare.security.SecurityUtil;
import com.rrdiagnocare.service.CartService;
import com.rrdiagnocare.service.CatalogService;
import com.rrdiagnocare.service.UserService;
import com.rrdiagnocare.util.AppConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Home, catalog, registration and login pages. */
@Controller
public class PublicController {

    private final CatalogService catalogService;
    private final CartService cartService;
    private final UserService userService;

    public PublicController(CatalogService catalogService, CartService cartService, UserService userService) {
        this.catalogService = catalogService;
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredPackages", catalogService.featuredPackages());
        model.addAttribute("categories", AppConstants.ALL_CATEGORIES);
        model.addAttribute("testCount", catalogService.countActiveTests());
        model.addAttribute("packageCount", catalogService.countActivePackages());
        return "index";
    }

    @GetMapping("/tests")
    public String catalog(@RequestParam(required = false) String category,
                          @RequestParam(required = false) String q,
                          HttpSession session, Model model) {
        String cat = category != null && AppConstants.ALL_CATEGORIES.contains(category) ? category : "";
        String query = q == null ? "" : q.trim();
        model.addAttribute("tests", catalogService.searchTests(cat, query));
        model.addAttribute("packages", catalogService.searchPackages(cat, query));
        model.addAttribute("categories", AppConstants.ALL_CATEGORIES);
        model.addAttribute("selectedCategory", cat);
        model.addAttribute("q", query);
        Set<String> inCart = new HashSet<>();
        List<CartItem> cart = cartService.getCart(session);
        synchronized (cart) {
            for (CartItem i : cart) inCart.add(i.getKey());
        }
        model.addAttribute("cartKeys", inCart);
        return "tests";
    }

    @GetMapping("/login")
    public String login() {
        String home = roleHome();
        return home == null ? "login" : "redirect:" + home;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (roleHome() != null) {
            return "redirect:/";
        }
        model.addAttribute("form", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") User form, BindingResult binding,
                           @RequestParam String password, @RequestParam String confirmPassword,
                           Model model, RedirectAttributes ra) {
        if (binding.hasFieldErrors("dateOfBirth")) {
            model.addAttribute("error", "Please enter a valid date of birth.");
            return "register";
        }
        try {
            userService.registerPatient(form, password, confirmPassword);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        ra.addFlashAttribute("success", "Registration successful! Please log in with your new username.");
        ra.addFlashAttribute("registeredUsername", form.getUsername().trim().toLowerCase());
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("errorTitle", "Access denied");
        model.addAttribute("errorMessage", "You do not have permission to open this page with your current role.");
        model.addAttribute("statusCode", 403);
        return "error";
    }

    /** Dashboard of the logged-in user, or null if anonymous. */
    private static String roleHome() {
        AppUserPrincipal u = SecurityUtil.currentUser();
        if (u == null) return null;
        if (u.hasRole(AppConstants.ROLE_ADMIN)) return "/admin/dashboard";
        if (u.hasRole(AppConstants.ROLE_STAFF)) return "/staff/worklist";
        return "/patient/bookings";
    }
}
