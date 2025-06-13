package com.coffeeshop.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coffeeshop.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    boolean existsByName(String name);
    
    // Stock queries
    List<Product> findByStockQuantityLessThanEqual(int threshold);
    List<Product> findByStockQuantityEquals(int quantity);
    List<Product> findByAvailable(boolean available);
    
    // Category queries
    List<Product> findByCategory(String categoryName);
    Page<Product> findByCategory(String categoryName, Pageable pageable);
    List<Product> findByCategoryIn(List<String> categoryNames);
    
    // Popular products queries
    @Query("SELECT p FROM Product p ORDER BY p.salesCount DESC")
    List<Product> findMostPopularProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p " +
           "WHERE p.category = :categoryName ORDER BY p.salesCount DESC")
    List<Product> findMostPopularProductsByCategory(
        @Param("categoryName") String categoryName,
        Pageable pageable
    );
    
    // Sales analysis queries
    @Query("SELECT p.name, SUM(oi.quantity), SUM(oi.quantity * oi.price) " +
           "FROM OrderItem oi JOIN oi.product p " +
           "WHERE oi.order.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.name")
    List<Object[]> findProductsSalesByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT p.name, p.salesCount, (p.price * p.salesCount) as calculatedRevenue " +
           "FROM Product p ORDER BY calculatedRevenue DESC")
    List<Object[]> findTopSellingProductsByRevenue();
    
    // Price range queries
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();

    @Query("SELECT oi.product FROM OrderItem oi GROUP BY oi.product ORDER BY SUM(oi.quantity) DESC")
    List<Product> findTopSellingProducts(Pageable pageable);

    // Search queries
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(CAST(p.description AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // Complex criteria queries
    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE (:name IS NULL OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:categories IS NULL OR p.category IN :categories) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:available IS NULL OR p.available = :available)")
    Page<Product> findProductsByCriteria(
        @Param("name") String name,
        @Param("categories") List<String> categories,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("available") Boolean available,
        Pageable pageable
    );
} 