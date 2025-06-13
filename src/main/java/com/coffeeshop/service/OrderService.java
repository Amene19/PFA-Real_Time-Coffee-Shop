package com.coffeeshop.service;

import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderStatus;
import com.coffeeshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {

    // Core Order Management
    Order createOrder(Order order);
    Optional<Order> getOrderById(Long id);
    Order updateOrderStatus(Long orderId, OrderStatus status);
    Page<Order> getAllOrders(Pageable pageable);

    // Order searching and filtering
    Page<Order> findOrdersByCriteria(LocalDate startDate, LocalDate endDate, String status, Pageable pageable);
    List<Order> findRecentOrders(int limit);
    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);

    // Server-specific order management
    List<Order> getOrdersForServer(String username);
    List<Order> getOrdersByStatusForBarman(List<OrderStatus> statuses);
    List<Order> getActiveOrdersByServer(String username);
    List<Order> getCompletedOrdersByServer(String username, java.time.LocalDate date);
    Page<Order> getOrdersByServerAndDate(String username, java.time.LocalDate date, Pageable pageable);
    Page<Order> getOrdersByServer(String username, Pageable pageable);

    // Dashboard & Statistics
    long getTotalOrderCount();
    BigDecimal getTotalRevenue();
    Map<String, BigDecimal> getRevenueByDay(LocalDate startDate, LocalDate endDate);
    Map<String, Long> getOrderCountByCategory();

    // Barman-specific methods
    java.util.Map<String, java.time.Duration> getAveragePreparationTimesByProduct();
    void setOrderStartTime(Long orderId, java.time.LocalDateTime startTime);
    void setOrderCompletionTime(Long orderId, java.time.LocalDateTime completionTime);
    Order updateOrderPriority(Long orderId, int priority);
    java.util.Map<String, java.time.Duration> getAveragePreparationTimesByCategory();
    java.util.Map<String, Integer> getOrderCountByProduct();
} 