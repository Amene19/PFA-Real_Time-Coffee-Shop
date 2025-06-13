package com.coffeeshop.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coffeeshop.model.Order;
import com.coffeeshop.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByOrderId(Long orderId);
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE DATE(p.paidAt) = :date")
    BigDecimal sumPaymentsByDate(@Param("date") LocalDate date);
    @Query("SELECT p FROM Payment p WHERE p.paidAt BETWEEN :start AND :end")
    List<Payment> findPaymentsBetweenDates(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
} 