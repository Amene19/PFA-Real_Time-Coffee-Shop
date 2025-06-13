package com.coffeeshop.service;

import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderStatus;
import com.coffeeshop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderStatus;
import com.coffeeshop.model.TableStatus;
import com.coffeeshop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final TableService tableService;
    
    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, TableService tableService) {
        this.orderRepository = orderRepository;
        this.tableService = tableService;
    }

    @Override
    public Order createOrder(Order order) {
        Order savedOrder = orderRepository.save(order);
        // Update table status to OCCUPIED when order is created
        if (order.getTable() != null) {
            tableService.updateTableStatus(order.getTable().getId(), "OCCUPIED");
        }
        return savedOrder;
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        // Update table status to AVAILABLE when order is completed or cancelled
        if ((status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) && 
            (oldStatus == OrderStatus.PENDING || oldStatus == OrderStatus.IN_PROGRESS)) {
            if (order.getTable() != null) {
                tableService.updateTableStatus(order.getTable().getId(), "AVAILABLE");
            }
        }
        
        return updatedOrder;
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> findOrdersByCriteria(LocalDate startDate, LocalDate endDate, String status, Pageable pageable) {
        Specification<Order> spec = Specification.where(null);

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), startDate.atStartOfDay()));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), endDate.atTime(LocalTime.MAX)));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), OrderStatus.valueOf(status.toUpperCase())));
        }
        return orderRepository.findAll(spec, pageable);
    }

    @Override
    public List<Order> findRecentOrders(int limit) {
        return orderRepository.findAll(PageRequest.of(0, limit, Sort.by("createdAt").descending())).getContent();
    }

    @Override
    public List<Order> getOrdersForServer(String username) {
        return orderRepository.findByServerUsernameAndStatusIn(username, List.of(OrderStatus.PENDING, OrderStatus.IN_PROGRESS));
    }

    @Override
    public List<Order> getOrdersByStatusForBarman(List<OrderStatus> statuses) {
        return orderRepository.findByStatusIn(statuses, Sort.by("createdAt"));
    }

    @Override
    public long getTotalOrderCount() {
        return orderRepository.count();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderRepository.findTotalRevenue().orElse(BigDecimal.ZERO);
    }

    @Override
    public Map<String, BigDecimal> getRevenueByDay(LocalDate startDate, LocalDate endDate) {
        return orderRepository.findRevenueByDay(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX))
                .stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (BigDecimal) result[1],
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, Long> getOrderCountByCategory() {
        return orderRepository.countOrdersByProductCategory()
                .stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Map<String, Duration> getAveragePreparationTimesByProduct() {
        // Placeholder implementation
        return Collections.emptyMap();
    }

    @Override
    public void setOrderStartTime(Long orderId, java.time.LocalDateTime startTime) {
        // Placeholder implementation
    }

    @Override
    public void setOrderCompletionTime(Long orderId, java.time.LocalDateTime completionTime) {
        // Placeholder implementation
    }

    @Override
    public Order updateOrderPriority(Long orderId, int priority) {
        // Placeholder implementation
        return null;
    }

    @Override
    public Map<String, Duration> getAveragePreparationTimesByCategory() {
        // Placeholder implementation
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Integer> getOrderCountByProduct() {
        // Placeholder implementation
        return Collections.emptyMap();
    }

    @Override
    public List<Order> getActiveOrdersByServer(String username) {
        // Get active statuses (PENDING and IN_PROGRESS are considered active)
        List<OrderStatus> activeStatuses = Arrays.asList(
            OrderStatus.PENDING,
            OrderStatus.IN_PROGRESS
        );
        
        // Find all orders for this server with active statuses, ordered by creation date (newest first)
        return orderRepository.findByServerAndStatusInOrderByCreatedAtDesc(
            username,
            activeStatuses
        );
    }

    @Override
    public List<Order> getCompletedOrdersByServer(String username, LocalDate date) {
        // Placeholder implementation
        return Collections.emptyList();
    }

    @Override
    public Page<Order> getOrdersByServerAndDate(String username, LocalDate date, Pageable pageable) {
        // Placeholder implementation
        return Page.empty();
    }

    @Override
    public Page<Order> getOrdersByServer(String username, Pageable pageable) {
        // Placeholder implementation
        return Page.empty();
    }

    @Override
    public Order findCurrentOrderForTable(Long tableId) {
        List<OrderStatus> activeStatuses = java.util.List.of(OrderStatus.PENDING, OrderStatus.IN_PROGRESS, OrderStatus.READY);
        return orderRepository.findByTableIdAndStatusInOrderByCreatedAtDesc(tableId, activeStatuses)
                .stream().findFirst().orElse(null);
    }
} 