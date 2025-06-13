package com.coffeeshop.service;

import com.coffeeshop.model.Payment;
import com.coffeeshop.model.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public interface PaymentService {
    Payment processPayment(Long orderId, Payment payment);
    Payment updatePaymentStatus(Long paymentId, String status);
    Optional<Payment> getPaymentByOrder(Long orderId);
    byte[] generateReceiptPDF(Long orderId);
    Map<String, BigDecimal> getDailySummary();
    Map<PaymentMethod, BigDecimal> getPaymentsByMethod(LocalDate date);
    Map<String, BigDecimal> getHourlyRevenue(LocalDate date);
    BigDecimal getTotalRevenue(LocalDate date);
    BigDecimal getTotalTax(LocalDate date);
    BigDecimal getTotalTips(LocalDate date);
    Page<Payment> getPaymentsByDate(LocalDate date, Pageable pageable);
    Page<Payment> getAllPayments(Pageable pageable);
} 
 