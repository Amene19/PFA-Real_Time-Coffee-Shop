package com.coffeeshop.controller;

import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderStatus;
import com.coffeeshop.model.Product;
import com.coffeeshop.service.OrderService;
import com.coffeeshop.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/barman")
@PreAuthorize("hasRole('BARMAN')")
public class BarmanController {

    private final OrderService orderService;
    private final ProductService productService;

    public BarmanController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    // Dashboard with Order Queue
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get all relevant orders
        List<Order> orders = orderService.getOrdersByStatusForBarman(
            List.of(OrderStatus.PENDING, OrderStatus.IN_PROGRESS, OrderStatus.READY)
        );
        
        // Calculate average preparation times
        Map<String, Duration> avgPrepTimes = orderService.getAveragePreparationTimesByProduct();
        
        model.addAttribute("orders", orders);
        model.addAttribute("avgPrepTimes", avgPrepTimes);
        
        return "barman/dashboard";
    }

    // Order Status Management
    @PutMapping("/orders/{orderId}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestBody Map<String, String> body) {
        try {
            OrderStatus status = OrderStatus.valueOf(body.get("status"));
            // Record start time when moving to IN_PROGRESS
            if (status == OrderStatus.IN_PROGRESS) {
                orderService.setOrderStartTime(orderId, LocalDateTime.now());
            } else if (status == OrderStatus.READY) {
                orderService.setOrderCompletionTime(orderId, LocalDateTime.now());
            }
            Order order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Product Availability Management
    @GetMapping("/products")
    public String viewProducts(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             Model model) {
        Page<Product> products = productService.getAllProducts(
            PageRequest.of(page, size, Sort.by("category", "name")));
        
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        return "barman/products";
    }

    @PutMapping("/products/{productId}/availability")
    @ResponseBody
    public ResponseEntity<?> updateProductAvailability(@PathVariable Long productId,
                                                     @RequestParam boolean available) {
        try {
            Product product = productService.updateProductAvailability(productId, available);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Order Queue Management
    @PutMapping("/orders/{orderId}/priority")
    @ResponseBody
    public ResponseEntity<?> updateOrderPriority(@PathVariable Long orderId,
                                               @RequestParam int priority) {
        try {
            Order order = orderService.updateOrderPriority(orderId, priority);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Preparation Time Tracking
    @GetMapping("/statistics")
    public String viewStatistics(@RequestParam(required = false) String category,
                               Model model) {
        // Get preparation time statistics
        Map<String, Duration> avgPrepTimesByProduct = orderService.getAveragePreparationTimesByProduct();
        Map<String, Duration> avgPrepTimesByCategory = orderService.getAveragePreparationTimesByCategory();
        Map<String, Integer> orderCountByProduct = orderService.getOrderCountByProduct();
        
        model.addAttribute("avgPrepTimesByProduct", avgPrepTimesByProduct);
        model.addAttribute("avgPrepTimesByCategory", avgPrepTimesByCategory);
        model.addAttribute("orderCountByProduct", orderCountByProduct);
        model.addAttribute("selectedCategory", category);
        
        return "barman/statistics";
    }
} 