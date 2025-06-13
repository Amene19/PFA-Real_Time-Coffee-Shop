package com.coffeeshop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.coffeeshop.model.Role;
import com.coffeeshop.model.User;

public interface UserService {
    // User registration and basic CRUD operations
    User registerUser(User user, String role);
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    Page<User> getAllUsers(Pageable pageable);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Role management
    User addRoleToUser(Long userId, String roleName);
    User removeRoleFromUser(Long userId, String roleName);
    List<Role> getAllRoles();
    List<User> getUsersByRole(String roleName);
    Page<User> getUsersByRole(String roleName, Pageable pageable);

    // User activation/deactivation
    User activateUser(Long userId);
    User deactivateUser(Long userId);
    List<User> getInactiveUsers();
    Page<User> getInactiveUsers(Pageable pageable);

    // Password management
    void changePassword(Long userId, String currentPassword, String newPassword);
    void resetPassword(String email);
    void updatePassword(Long userId, String newPassword);

    // User statistics for admin dashboard
    long countTotalUsers();
    long countActiveUsers();
    long countUsersByRole(String roleName);
    List<Object[]> getUserStatsByRole();
    List<User> getRecentlyAddedUsers(int limit);
    List<User> getUsersByCreationDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Search and filtering
    Page<User> searchUsers(String keyword, Pageable pageable);
    Page<User> findUsersByCriteria(String username, String email, Boolean active, String roleName, Pageable pageable);
} 
 