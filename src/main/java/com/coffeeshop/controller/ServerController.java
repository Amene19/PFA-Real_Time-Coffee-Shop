package com.coffeeshop.controller;

import com.coffeeshop.dto.OrderCreateRequest;
import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderItem;
import com.coffeeshop.model.OrderStatus;
import com.coffeeshop.model.Product;
import com.coffeeshop.model.DiningTable;
import com.coffeeshop.model.User;
import com.coffeeshop.dto.TableView;
import com.coffeeshop.service.OrderService;
import com.coffeeshop.service.ProductService;
import com.coffeeshop.service.TableService;
import com.coffeeshop.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/server")
@PreAuthorize("hasRole('SERVER')")
public class ServerController {

    private final OrderService orderService;
    private final ProductService productService;
    private final TableService tableService;
    private final UserService userService;

    public ServerController(OrderService orderService,
                          ProductService productService,
                          TableService tableService,
                          UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.tableService = tableService;
        this.userService = userService;
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        
        // Get assigned tables and wrap them in TableView objects
        List<DiningTable> tables = tableService.getTablesByServer(username);
        List<TableView> assignedTables = tables.stream()
            .map(TableView::new)
            .toList();
        model.addAttribute("assignedTables", assignedTables);
        
        // Get active orders
        List<Order> activeOrders = orderService.getActiveOrdersByServer(username);
        model.addAttribute("activeOrders", activeOrders);
        
        // Get today's completed orders
        List<Order> completedOrders = orderService.getCompletedOrdersByServer(
            username, LocalDate.now());
        model.addAttribute("completedOrders", completedOrders);
        
        return "server/dashboard";
    }

    // Table Management
    @GetMapping("/tables")
    public String viewTables(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<DiningTable> tables = tableService.getTablesByServer(username);
        model.addAttribute("tables", tables);
        return "server/tables";
    }

    @PostMapping("/tables/{tableId}/status")
    @ResponseBody
    public ResponseEntity<?> updateTableStatus(@PathVariable Long tableId,
                                             @RequestParam String status) {
        try {
            DiningTable table = tableService.updateTableStatus(tableId, status);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Order Management
    @GetMapping("/orders/new")
    public String newOrder(@RequestParam(required = true) Long tableId, Model model) {
        try {
            DiningTable table = tableService.getTableById(tableId);
            if (table == null) {
                return "redirect:/server/dashboard?error=Table not found";
            }
            
            model.addAttribute("table", table);
            model.addAttribute("products", productService.getAllProducts(PageRequest.of(0, Integer.MAX_VALUE)));
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("newOrder", new Order()); // Add empty order object for form binding
            return "server/order";
        } catch (Exception e) {
            return "redirect:/server/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/orders")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateRequest orderRequest,
                                       Authentication authentication) {
        try {
            // Get the server user
            User server = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Server not found"));

            // Get the table
            DiningTable table = tableService.getTableById(orderRequest.getTableId());
            if (table == null) {
                throw new RuntimeException("Table not found");
            }

            // Create new order
            Order order = new Order();
            order.setServer(server);
            order.setTable(table);
            order.setNotes(orderRequest.getNotes());
            order.setStatus(OrderStatus.PENDING);

            // Add order items
            for (OrderCreateRequest.OrderItemRequest itemRequest : orderRequest.getItems()) {
                Product product = productService.getProductById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));
                
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(product.getPrice());
                order.addOrderItem(orderItem);
            }

            // Save the order
            Order savedOrder = orderService.createOrder(order);
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrder(@PathVariable Long orderId, Model model) {
        try {
            Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
            model.addAttribute("order", order);
            return "server/order-details";
        } catch (Exception e) {
            return "redirect:/server/dashboard?error=Order+not+found+or+you+don't+have+permission+to+view+it";
        }
    }

    @PutMapping("/orders/{orderId}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                             @RequestParam String status) {
        try {
            Order order = orderService.updateOrderStatus(orderId, OrderStatus.valueOf(status.toUpperCase()));
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Order History
    @GetMapping("/orders/history")
    public String orderHistory(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) 
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Authentication authentication,
                             Model model) {
        String username = authentication.getName();
        Page<Order> orders;
        
        if (date != null) {
            orders = orderService.getOrdersByServerAndDate(
                username, date, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            orders = orderService.getOrdersByServer(
                username, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentDate", date != null ? date : LocalDate.now());
        return "server/order-history";
    }

    // Product Catalog
    @GetMapping("/products")
    public String viewProducts(@RequestParam(required = false) String category,
                             @RequestParam(required = false) String search,
                             Model model) {
        List<Product> products;
        
        if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search, null, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        } else {
            products = productService.getAllProducts(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        }
        
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        return "server/products";
    }

    @GetMapping("/products/{productId}")
    @ResponseBody
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId).orElseThrow(() -> 
            new RuntimeException("Product not found")));
    }
} 