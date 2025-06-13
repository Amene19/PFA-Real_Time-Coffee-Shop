package com.coffeeshop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coffeeshop.model.Product;
import com.coffeeshop.repository.ProductRepository;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Product updateProductAvailability(Long productId, boolean available) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setAvailable(available);
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, String category, Pageable pageable) {
        return productRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(keyword)) {
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
                Predicate descriptionMatch = cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
                predicate = cb.and(predicate, cb.or(nameMatch, descriptionMatch));
            }
            if (StringUtils.hasText(category)) {
                predicate = cb.and(predicate, cb.equal(root.get("category"), category));
            }
            return predicate;
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllCategories() {
        return productRepository.findAllCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(Pageable.ofSize(limit));
    }

    @Override
    @Transactional
    public Product updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        product.setStockQuantity(quantity);
        if (quantity == 0) {
            product.setAvailable(false);
        }
        
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product markAsAvailable(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setAvailable(true);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product markAsUnavailable(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setAvailable(false);
        return productRepository.save(product);
    }

    @Override
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThanEqual(threshold);
    }

    @Override
    public List<Product> getOutOfStockProducts() {
        return productRepository.findByStockQuantityEquals(0);
    }

    @Override
    public List<Product> getAvailableProducts() {
        return productRepository.findByAvailable(true);
    }

    @Override
    public boolean isProductAvailable(Long id) {
        return productRepository.findById(id)
            .map(Product::isAvailable)
            .orElse(false);
    }

    @Override
    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategory(categoryName);
    }

    @Override
    public Page<Product> getProductsByCategory(String categoryName, Pageable pageable) {
        return productRepository.findByCategory(categoryName, pageable);
    }

    @Override
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Override
    public List<Product> getProductsByCategories(List<String> categoryNames) {
        return productRepository.findByCategoryIn(categoryNames);
    }

    @Override
    public List<Product> getMostPopularProducts(int limit) {
        return productRepository.findMostPopularProducts(PageRequest.of(0, limit));
    }

    @Override
    public List<Product> getMostPopularProductsByCategory(String categoryName, int limit) {
        return productRepository.findMostPopularProductsByCategory(categoryName, PageRequest.of(0, limit));
    }

    @Override
    public List<Object[]> getProductsSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return productRepository.findProductsSalesByDateRange(startDate, endDate);
    }

    @Override
    public List<Object[]> getTopSellingProductsByRevenue() {
        return productRepository.findTopSellingProductsByRevenue();
    }

    @Override
    @Transactional
    public Product updatePrice(Long id, BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setPrice(newPrice);
        product.setOriginalPrice(newPrice);
        return productRepository.save(product);
    }

    @Override
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    @Transactional
    public void applyDiscountByCategory(String categoryName, double discountPercentage) {
        List<Product> products = productRepository.findByCategory(categoryName);
        for (Product product : products) {
            product.applyDiscount(discountPercentage);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public void applyDiscountToProduct(Long id, double discountPercentage) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        product.applyDiscount(discountPercentage);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void resetDiscounts() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            product.resetDiscount();
            productRepository.save(product);
        }
    }

    @Override
    public Page<Product> findProductsByCriteria(
        String name,
        List<String> categories,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean available,
        Pageable pageable
    ) {
        return productRepository.findAll((root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (StringUtils.hasText(name)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (categories != null && !categories.isEmpty()) {
                predicate = cb.and(predicate, root.get("category").in(categories));
            }
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (available != null) {
                predicate = cb.and(predicate, cb.equal(root.get("available"), available));
            }

            return predicate;
        }, pageable);
    }
} 