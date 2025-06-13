package com.coffeeshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coffeeshop.model.Role;
import com.coffeeshop.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic queries
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Role-based queries
    List<User> findByRolesContaining(Role role);
    Page<User> findByRolesContaining(Role role, Pageable pageable);
    
    // Active status queries
    List<User> findByEnabled(boolean enabled);
    Page<User> findByEnabled(boolean enabled, Pageable pageable);
    
    // Combined queries
    List<User> findByRolesContainingAndEnabled(Role role, boolean enabled);
    Page<User> findByRolesContainingAndEnabled(Role role, boolean enabled, Pageable pageable);

    // Custom queries with JPQL
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    // Statistics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countUsersByRole(@Param("roleName") String roleName);

    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsersByRole();

    // Complex queries with multiple conditions
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r " +
           "WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled) " +
           "AND (:roleId IS NULL OR r.id = :roleId)")
    Page<User> findUsersByCriteria(
            @Param("username") String username,
            @Param("email") String email,
            @Param("enabled") Boolean enabled,
            @Param("roleId") Long roleId,
            Pageable pageable
    );

    // Recent users query
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    Page<User> findRecentUsers(Pageable pageable);

    // Users by creation date range
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByCreationDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    long countByEnabled(boolean enabled);
} 