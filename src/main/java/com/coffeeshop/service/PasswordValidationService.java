package com.coffeeshop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidationService {

    private final PasswordEncoder passwordEncoder;
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 32;
    private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
    private static final Pattern HAS_NUMBER = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    public PasswordValidationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            errors.add("Password must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
        }

        if (!HAS_UPPER.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter");
        }

        if (!HAS_LOWER.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter");
        }

        if (!HAS_NUMBER.matcher(password).find()) {
            errors.add("Password must contain at least one number");
        }

        if (!HAS_SPECIAL.matcher(password).find()) {
            errors.add("Password must contain at least one special character");
        }

        return errors;
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
} 
 