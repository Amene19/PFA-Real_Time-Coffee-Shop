package com.coffeeshop.controller;

import com.coffeeshop.model.DiningTable;
import com.coffeeshop.model.Order;
import com.coffeeshop.model.Payment;
import com.coffeeshop.model.PaymentMethod;
import com.coffeeshop.service.OrderService;
import com.coffeeshop.service.PaymentService;
import com.coffeeshop.service.TableService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cashier")
@PreAuthorize("hasRole('CASHIER')")
public class CashierController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final TableService tableService;

    public CashierController(OrderService orderService,
                           PaymentService paymentService,
                           TableService tableService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.tableService = tableService;
    }

    // Dashboard with Tables Overview
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<DiningTable> tables = tableService.getAllTables();
        Map<String, BigDecimal> dailySummary = paymentService.getDailySummary();
        
        model.addAttribute("tables", tables);
        model.addAttribute("dailySummary", dailySummary);
        return "cashier/dashboard";
    }

    // View Order Details for Payment
    @GetMapping("/orders/{orderId}")
    public String viewOrder(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        model.addAttribute("order", order);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "cashier/order-details";
    }

    // Process Payment
    @PostMapping("/orders/{orderId}/payment")
    @ResponseBody
    public ResponseEntity<?> processPayment(@PathVariable Long orderId,
                                          @RequestBody Payment payment) {
        try {
            Payment processedPayment = paymentService.processPayment(orderId, payment);
            return ResponseEntity.ok(processedPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Update Payment Status
    @PutMapping("/payments/{paymentId}/status")
    @ResponseBody
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long paymentId,
                                               @RequestParam String status) {
        try {
            Payment payment = paymentService.updatePaymentStatus(paymentId, status);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Generate Receipt
    @GetMapping("/orders/{orderId}/receipt")
    public String generateReceipt(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        Payment payment = paymentService.getPaymentByOrder(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        return "cashier/receipt";
    }

    // Download Receipt PDF
    @GetMapping("/orders/{orderId}/receipt/pdf")
    public ResponseEntity<?> downloadReceipt(@PathVariable Long orderId) {
        try {
            byte[] pdfContent = paymentService.generateReceiptPDF(orderId);
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"receipt-" + orderId + ".pdf\"")
                .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Cash Flow Summary
    @GetMapping("/reports/cash-flow")
    public String cashFlowSummary(@RequestParam(required = false) 
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                                 LocalDate date,
                                 Model model) {
        date = date != null ? date : LocalDate.now();
        
        Map<PaymentMethod, BigDecimal> paymentsByMethod = paymentService.getPaymentsByMethod(date);
        Map<String, BigDecimal> hourlyRevenue = paymentService.getHourlyRevenue(date);
        BigDecimal totalRevenue = paymentService.getTotalRevenue(date);
        BigDecimal totalTax = paymentService.getTotalTax(date);
        BigDecimal totalTips = paymentService.getTotalTips(date);
        
        model.addAttribute("date", date);
        model.addAttribute("paymentsByMethod", paymentsByMethod);
        model.addAttribute("hourlyRevenue", hourlyRevenue);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalTax", totalTax);
        model.addAttribute("totalTips", totalTips);
        
        return "cashier/cash-flow";
    }

    // View Payment History
    @GetMapping("/payments")
    public String viewPayments(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             @RequestParam(required = false) 
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                             LocalDate date,
                             Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, 
            Sort.by("createdAt").descending());
        
        Page<Payment> payments = date != null ?
            paymentService.getPaymentsByDate(date, pageRequest) :
            paymentService.getAllPayments(pageRequest);
        
        model.addAttribute("payments", payments);
        model.addAttribute("currentDate", date != null ? date : LocalDate.now());
        
        return "cashier/payments";
    }
} 