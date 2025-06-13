package com.coffeeshop.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.coffeeshop.model.User;
import com.coffeeshop.service.OrderService;
import com.coffeeshop.service.ProductService;
import com.coffeeshop.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    public AdminController(UserService userService,
                         OrderService orderService,
                         ProductService productService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalOrders = orderService.getTotalOrderCount();
        BigDecimal totalRevenue = orderService.getTotalRevenue();
        BigDecimal averageOrderValue = totalOrders > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("activeUsers", userService.countActiveUsers());
        model.addAttribute("averageOrderValue", averageOrderValue);

        // Placeholder growth values
        model.addAttribute("revenueGrowth", 0);
        model.addAttribute("orderGrowth", 0);
        model.addAttribute("userGrowth", 0);
        model.addAttribute("aovGrowth", 0);
        
        // Chart data
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();
        model.addAttribute("revenueChart", orderService.getRevenueByDay(startDate, endDate));
        model.addAttribute("categoryChart", orderService.getOrderCountByCategory());
        
        // Table data
        model.addAttribute("recentOrders", orderService.findRecentOrders(5));
        model.addAttribute("topProducts", productService.findTopSellingProducts(5));

        return "admin/dashboard";
    }

    // User Management
    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String search,
                          Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username"));
        Page<User> users;
        
        if (search != null && !search.isEmpty()) {
            users = userService.searchUsers(search, pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("roles", userService.getAllRoles());
        return "admin/users";
    }

    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<?> createUser(@Valid @RequestBody User user,
                                      @RequestParam String role) {
        try {
            User savedUser = userService.registerUser(user, role);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                      @Valid @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // System Settings
    @GetMapping("/settings")
    public String showSettings(Model model) {
        return "admin/settings";
    }

    @PostMapping("/settings")
    @ResponseBody
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settings) {
        try {
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
} 