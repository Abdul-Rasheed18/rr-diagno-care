package com.rrdiagnocare.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/** Spring Security principal that also carries the database id and display name. */
public class AppUserPrincipal extends User {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String fullName;
    private final String phone;
    private final String address;

    public AppUserPrincipal(Long id, String fullName, String phone, String address, String username, String password,
                            boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    public boolean hasRole(String role) {
        return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
