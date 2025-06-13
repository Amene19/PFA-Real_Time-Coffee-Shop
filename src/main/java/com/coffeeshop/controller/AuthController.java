package com.coffeeshop.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coffeeshop.model.User;
import com.coffeeshop.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                             BindingResult result,
                             Model model,
                             @RequestParam("confirmPassword") String confirmPassword,
                             @RequestParam("role") String role) {
        logger.info("Processing registration for user: {}", user.getUsername());
        
        // Validate form
        if (result.hasErrors()) {
            logger.warn("Validation errors during registration for user: {}", user.getUsername());
            return "auth/signup";
        }

        // Validate password match
        if (!user.getPassword().equals(confirmPassword)) {
            logger.warn("Password mismatch during registration for user: {}", user.getUsername());
            model.addAttribute("error", "Passwords do not match");
            return "auth/signup";
        }

        // Validate role
        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_CASHIER") && 
            !role.equals("ROLE_SERVER") && !role.equals("ROLE_BARMAN")) {
            logger.warn("Invalid role selected during registration: {}", role);
            model.addAttribute("error", "Invalid role selected");
            return "auth/signup";
        }

        try {
            userService.registerUser(user, role);
            logger.info("Successfully registered user: {} with role: {}", user.getUsername(), role);
            return "redirect:/login?registered";
        } catch (Exception e) {
            logger.error("Error during registration for user: {}", user.getUsername(), e);
            model.addAttribute("error", e.getMessage());
            return "auth/signup";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        String username = authentication.getName();
        logger.info("User {} accessing dashboard", username);
        
        // Redirect to role-specific dashboard
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            logger.debug("Redirecting admin user {} to admin dashboard", username);
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CASHIER"))) {
            logger.debug("Redirecting cashier user {} to cashier dashboard", username);
            return "redirect:/cashier/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SERVER"))) {
            logger.debug("Redirecting server user {} to server dashboard", username);
            return "redirect:/server/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_BARMAN"))) {
            logger.debug("Redirecting barman user {} to barman dashboard", username);
            return "redirect:/barman/dashboard";
        }
        
        logger.warn("User {} has no valid role for dashboard access", username);
        return "redirect:/login";
    }
} 