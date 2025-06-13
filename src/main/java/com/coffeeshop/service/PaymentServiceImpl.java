package com.coffeeshop.service;

import com.coffeeshop.model.Order;
import com.coffeeshop.model.Payment;
import com.coffeeshop.model.PaymentMethod;
import com.coffeeshop.model.PaymentStatus;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }
    
    @Override
    public Payment processPayment(Long orderId, Payment payment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        payment.setOrder(order);
        payment.setAmount(order.getTotal());
        payment.setStatus(PaymentStatus.COMPLETED);
        
        return paymentRepository.save(payment);
    }

    @Override
    public Payment updatePaymentStatus(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> getPaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public byte[] generateReceiptPDF(Long orderId) {
        // Placeholder for PDF generation
        return new byte[0];
    }

    @Override
    public Map<String, BigDecimal> getDailySummary() {
        return Collections.emptyMap();
    }

    @Override
    public Map<PaymentMethod, BigDecimal> getPaymentsByMethod(LocalDate date) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, BigDecimal> getHourlyRevenue(LocalDate date) {
        return Collections.emptyMap();
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDate date) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalTax(LocalDate date) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalTips(LocalDate date) {
        return BigDecimal.ZERO;
    }

    @Override
    public Page<Payment> getPaymentsByDate(LocalDate date, Pageable pageable) {
        return Page.empty(pageable);
    }

    @Override
    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }
} 