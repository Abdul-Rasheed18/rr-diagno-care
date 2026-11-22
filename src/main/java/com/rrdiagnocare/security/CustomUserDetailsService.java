package com.rrdiagnocare.security;

import com.rrdiagnocare.dao.UserDao;
import com.rrdiagnocare.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/** Loads users and their single role from MySQL; passwords are verified with BCrypt by the provider. */
@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username is required");
        }
        User u = userDao.findByUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        return new AppUserPrincipal(u.getId(), u.getFullName(), u.getPhone(), u.getAddress(), u.getUsername(),
                u.getPasswordHash(), u.isEnabled(), Collections.singletonList(new SimpleGrantedAuthority(u.getRole())));
    }
}
