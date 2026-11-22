package com.rrdiagnocare.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Static access to the logged-in principal for services, aspects and plain servlets. */
public final class SecurityUtil {

    private SecurityUtil() { }

    public static AppUserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserPrincipal) {
            return (AppUserPrincipal) auth.getPrincipal();
        }
        return null;
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "anonymous" : auth.getName();
    }

    public static boolean hasRole(String role) {
        AppUserPrincipal p = currentUser();
        return p != null && p.hasRole(role);
    }
}
