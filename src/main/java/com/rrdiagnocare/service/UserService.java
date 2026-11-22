package com.rrdiagnocare.service;

import com.rrdiagnocare.dao.UserDao;
import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.exception.ResourceNotFoundException;
import com.rrdiagnocare.model.User;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.DateUtil;
import com.rrdiagnocare.util.ValidationUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Patient registration and lab-technician account management. */
@Service
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    /** Self-registration of a patient. */
    @Transactional
    public long registerPatient(User form, String rawPassword, String confirmPassword) {
        return createUser(form, rawPassword, confirmPassword, AppConstants.ROLE_PATIENT);
    }

    /** Admin creates a lab technician account. */
    @Transactional
    public long createStaff(User form, String rawPassword) {
        return createUser(form, rawPassword, rawPassword, AppConstants.ROLE_STAFF);
    }

    private long createUser(User form, String rawPassword, String confirmPassword, String role) {
        if (ValidationUtil.isBlank(form.getFullName()) || form.getFullName().trim().length() < 3) {
            throw new BusinessException("Please enter the full name (at least 3 characters).");
        }
        if (!ValidationUtil.isValidUsername(form.getUsername())) {
            throw new BusinessException("Username must be 3-30 characters, start with a letter and use only letters, digits, '.' or '_'.");
        }
        if (!ValidationUtil.isValidEmail(form.getEmail())) {
            throw new BusinessException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidPhone(form.getPhone())) {
            throw new BusinessException("Please enter a valid 10-digit Indian mobile number (starting with 6-9).");
        }
        if (!ValidationUtil.isStrongPassword(rawPassword)) {
            throw new BusinessException("Password must be at least 8 characters and contain letters and digits.");
        }
        if (!rawPassword.equals(confirmPassword)) {
            throw new BusinessException("Password and confirm password do not match.");
        }
        if (form.getDateOfBirth() != null && form.getDateOfBirth().isAfter(DateUtil.today())) {
            throw new BusinessException("Date of birth cannot be in the future.");
        }
        String username = form.getUsername().trim().toLowerCase();
        String email = form.getEmail().trim().toLowerCase();
        if (userDao.existsByUsername(username)) {
            throw new BusinessException("Username '" + username + "' is already taken.");
        }
        if (userDao.existsByEmail(email)) {
            throw new BusinessException("An account with this email already exists.");
        }
        User u = new User();
        u.setFullName(form.getFullName().trim());
        u.setUsername(username);
        u.setEmail(email);
        u.setPhone(ValidationUtil.normalizePhone(form.getPhone()));
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setGender(ValidationUtil.isBlank(form.getGender()) ? null : form.getGender());
        u.setDateOfBirth(form.getDateOfBirth());
        u.setAddress(ValidationUtil.isBlank(form.getAddress()) ? null : form.getAddress().trim());
        u.setEnabled(true);
        return userDao.insert(u);
    }

    public User findById(long id) {
        return userDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<User> findStaff() {
        return userDao.findByRole(AppConstants.ROLE_STAFF);
    }

    public int countPatients() {
        return userDao.countByRole(AppConstants.ROLE_PATIENT);
    }

    @Transactional
    public void toggleStaffEnabled(long id) {
        User u = findById(id);
        if (!AppConstants.ROLE_STAFF.equals(u.getRole())) {
            throw new BusinessException("Only lab technician accounts can be enabled/disabled here.");
        }
        userDao.setEnabled(id, !u.isEnabled());
    }
}
