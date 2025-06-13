package com.coffeeshop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.coffeeshop.model.Product;

public interface ProductService {
    // Core Product Management
    Optional<Product> findProductById(Long id);
    default Optional<Product> getProductById(Long id) {
        return findProductById(id);
    }
    Page<Product> findAllProducts(Pageable pageable);
    default Page<Product> getAllProducts(Pageable pageable) {
        return findAllProducts(pageable);
    }
    Product createProduct(Product product);
    Product updateProduct(Long id, Product productDetails);
    void deleteProduct(Long id);

    // Product Availability
    Product updateProductAvailability(Long productId, boolean available);

    // Search and Filtering
    Page<Product> searchProducts(String keyword, String category, Pageable pageable);

    // Categories
    List<String> findAllCategories();
    default List<String> getAllCategories() {
        return findAllCategories();
    }
    
    // Statistics
    List<Product> findTopSellingProducts(int limit);

    // Availability management
    Product updateStock(Long id, int quantity);
    Product markAsAvailable(Long id);
    Product markAsUnavailable(Long id);
    List<Product> getLowStockProducts(int threshold);
    List<Product> getOutOfStockProducts();
    List<Product> getAvailableProducts();
    boolean isProductAvailable(Long id);

    // Category management
    List<Product> getProductsByCategory(String categoryName);
    Page<Product> getProductsByCategory(String categoryName, Pageable pageable);
    List<Product> getProductsByCategories(List<String> categoryNames);
    
    // Popular products analysis
    List<Product> getMostPopularProducts(int limit);
    List<Product> getMostPopularProductsByCategory(String categoryName, int limit);
    List<Object[]> getProductsSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Object[]> getTopSellingProductsByRevenue();
    
    // Price management
    Product updatePrice(Long id, BigDecimal newPrice);
    List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    void applyDiscountByCategory(String categoryName, double discountPercentage);
    void applyDiscountToProduct(Long id, double discountPercentage);
    void resetDiscounts();
    
    // Search and filtering
    Page<Product> findProductsByCriteria(
        String name,
        List<String> categories,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean available,
        Pageable pageable
    );
} 