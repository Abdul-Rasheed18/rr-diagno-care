package com.rrdiagnocare.security;

import com.rrdiagnocare.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/** Sends each role to its own dashboard after login. Patients return to the page they were heading to (e.g. checkout). */
@Component("customAuthSuccessHandler")
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthSuccessHandler.class);
    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String target = resolveTarget(request, response, authentication.getAuthorities());
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        }
        log.info("Login success: user='{}' -> {}", authentication.getName(), target);
        response.sendRedirect(request.getContextPath() + target);
    }

    private String resolveTarget(HttpServletRequest request, HttpServletResponse response,
                                 Collection<? extends GrantedAuthority> authorities) {
        if (has(authorities, AppConstants.ROLE_ADMIN)) {
            return "/admin/dashboard";
        }
        if (has(authorities, AppConstants.ROLE_STAFF)) {
            return "/staff/worklist";
        }
        // Patient: honour a saved request inside /patient/**, else checkout if the cart has items, else My Bookings
        SavedRequest saved = requestCache.getRequest(request, response);
        if (saved != null) {
            requestCache.removeRequest(request, response);
            String url = saved.getRedirectUrl();
            String marker = request.getContextPath() + "/patient/";
            int idx = url.indexOf(marker);
            if (idx >= 0 && "GET".equalsIgnoreCase(saved.getMethod())) {
                return url.substring(idx + request.getContextPath().length());
            }
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AppConstants.SESSION_CART) instanceof List
                && !((List<?>) session.getAttribute(AppConstants.SESSION_CART)).isEmpty()) {
            return "/patient/checkout";
        }
        return "/patient/bookings";
    }

    private static boolean has(Collection<? extends GrantedAuthority> authorities, String role) {
        return authorities.stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
