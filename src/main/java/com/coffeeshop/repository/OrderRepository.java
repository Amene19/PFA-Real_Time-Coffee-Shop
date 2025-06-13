package com.coffeeshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coffeeshop.model.Order;
import com.coffeeshop.model.OrderStatus;
import com.coffeeshop.model.User;

import java.math.BigDecimal;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Server-specific queries
    List<Order> findByServer(User server);
    Page<Order> findByServer(User server, Pageable pageable);
    List<Order> findByServerAndStatus(User server, OrderStatus status);
    
    // Status-based queries
    List<Order> findByStatus(OrderStatus status);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    // Time-based queries
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE")
    List<Order> findTodaysOrders();
    
    // Statistics queries
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND DATE(o.createdAt) = CURRENT_DATE")
    long countTodaysOrdersByStatus(@Param("status") OrderStatus status);
    
    // Find orders by server and status list, ordered by creation date
    @Query("SELECT o FROM Order o WHERE o.server.username = :username AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findByServerAndStatusInOrderByCreatedAtDesc(
        @Param("username") String username,
        @Param("statuses") List<OrderStatus> statuses
    );
    
    @Query("SELECT o.status, COUNT(o) FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY o.status")
    List<Object[]> getOrderStatsByStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT o.server.username, COUNT(o) FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY o.server")
    List<Object[]> getOrderStatsByServer(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT HOUR(o.createdAt), COUNT(o) FROM Order o " +
           "WHERE DATE(o.createdAt) = CURRENT_DATE " +
           "GROUP BY HOUR(o.createdAt) " +
           "ORDER BY HOUR(o.createdAt)")
    List<Object[]> getHourlyOrderStats();
    
    // Complex queries
    @Query("SELECT o FROM Order o " +
           "WHERE (:server IS NULL OR o.server = :server) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    Page<Order> findOrdersByCriteria(
        @Param("server") User server,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(DISTINCT o.table.number) FROM Order o WHERE o.createdAt BETWEEN ?1 AND ?2")
    int countDistinctTableNumberByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByCreatedAtBetweenAndTotalGreaterThan(LocalDateTime startDate, LocalDateTime endDate, double amount);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN ?1 AND ?2 AND o.server.id = ?3")
    List<Order> findByCreatedAtBetweenAndServerId(LocalDateTime startDate, LocalDateTime endDate, Long serverId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN ?1 AND ?2 AND o.server.id = ?3")
    long countByCreatedAtBetweenAndServerId(LocalDateTime startDate, LocalDateTime endDate, Long serverId);
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.createdAt BETWEEN ?1 AND ?2 AND o.server.id = ?3")
    Double sumTotalByCreatedAtBetweenAndServerId(LocalDateTime startDate, LocalDateTime endDate, Long serverId);

    List<Order> findByServerUsernameAndStatusIn(String username, List<OrderStatus> statuses);

    List<Order> findByStatusIn(List<OrderStatus> statuses, Sort sort);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'COMPLETED'")
    Optional<BigDecimal> findTotalRevenue();

    @Query("SELECT CONCAT(YEAR(o.createdAt), '-', MONTH(o.createdAt), '-', DAY(o.createdAt)), SUM(o.total) " +
           "FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "AND o.status = 'COMPLETED' " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt), DAY(o.createdAt) " +
           "ORDER BY o.createdAt")
    List<Object[]> findRevenueByDay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT p.category, COUNT(o.id) " +
           "FROM orders o " +
           "JOIN order_items oi ON o.id = oi.order_id " +
           "JOIN products p ON oi.product_id = p.id " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY p.category", nativeQuery = true)
    List<Object[]> countOrdersByProductCategory();
} 