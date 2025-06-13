package com.coffeeshop.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffeeshop.model.Role;
import com.coffeeshop.model.User;
import com.coffeeshop.repository.RoleRepository;
import com.coffeeshop.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidationService passwordValidationService;

    public UserServiceImpl(UserRepository userRepository, 
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder,
                         PasswordValidationService passwordValidationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidationService = passwordValidationService;
    }

    @Override
    @Transactional
    public User registerUser(User user, String roleName) {
        logger.info("Starting user registration process for username: {}", user.getUsername());
        
        // Validate username and email uniqueness
        if (existsByUsername(user.getUsername())) {
            logger.error("Username already exists: {}", user.getUsername());
            throw new RuntimeException("Username already exists");
        }
        if (existsByEmail(user.getEmail())) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new RuntimeException("Email already exists");
        }

        // Validate password
        var passwordErrors = passwordValidationService.validatePassword(user.getPassword());
        if (!passwordErrors.isEmpty()) {
            logger.error("Password validation failed for user {}: {}", user.getUsername(), passwordErrors);
            throw new RuntimeException(String.join(", ", passwordErrors));
        }

        // Encode password
        logger.debug("Encoding password for user: {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default values
        user.setEnabled(true);
        user.setRoles(new HashSet<>());
        
        // Assign role
        logger.debug("Finding role: {}", roleName);
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> {
                logger.error("Role not found: {}", roleName);
                return new RuntimeException("Role not found: " + roleName);
            });
            
        logger.debug("Adding role {} to user {}", roleName, user.getUsername());
        user.getRoles().add(role);
        
        // Save user
        logger.info("Saving user to database: {}", user.getUsername());
        try {
            User savedUser = userRepository.save(user);
            logger.info("Successfully saved user: {}", savedUser.getUsername());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error saving user {}: {}", user.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error saving user: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }

    @Override
    public List<User> getUsersByCreationDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.findUsersByCreationDateRange(startDate, endDate);
    }

    @Override
    public List<User> getRecentlyAddedUsers(int limit) {
        return userRepository.findAll(PageRequest.of(0, limit)).getContent();
    }

    @Override
    public List<Object[]> getUserStatsByRole() {
        return userRepository.countUsersByRole();
    }

    @Override
    public long countUsersByRole(String roleName) {
        return userRepository.countUsersByRole(roleName);
    }

    @Override
    public long countActiveUsers() {
        return userRepository.countByEnabled(true);
    }

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        var passwordErrors = passwordValidationService.validatePassword(newPassword);
        if (!passwordErrors.isEmpty()) {
            throw new RuntimeException(String.join(", ", passwordErrors));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public Page<User> findUsersByCriteria(String username, String email, Boolean active, String roleName, Pageable pageable) {
        Long roleId = null;
        if (roleName != null) {
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
            roleId = role.getId();
        }
        return userRepository.findUsersByCriteria(username, email, active, roleId, pageable);
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        // Generate a random temporary password
        String temporaryPassword = generateTemporaryPassword();
        
        // Update the user's password
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);
        
        // TODO: Send email with temporary password
        // This would typically involve an EmailService to send the temporary password to the user
    }
    
    private String generateTemporaryPassword() {
        // Generate a random 12-character password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password
        var passwordErrors = passwordValidationService.validatePassword(newPassword);
        if (!passwordErrors.isEmpty()) {
            throw new RuntimeException(String.join(", ", passwordErrors));
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<User> getInactiveUsers() {
        return userRepository.findByEnabled(false);
    }

    @Override
    public Page<User> getInactiveUsers(Pageable pageable) {
        return userRepository.findByEnabled(false, pageable);
    }

    @Override
    @Transactional
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsersByRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        return userRepository.findByRolesContaining(role);
    }

    @Override
    public Page<User> getUsersByRole(String roleName, Pageable pageable) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        return userRepository.findByRolesContaining(role, pageable);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public User removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            
        if (!user.getRoles().contains(role)) {
            throw new RuntimeException("User does not have role: " + roleName);
        }
        
        if (user.getRoles().size() <= 1) {
            throw new RuntimeException("Cannot remove the last role from user");
        }
        
        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User addRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            
        if (user.getRoles().contains(role)) {
            throw new RuntimeException("User already has role: " + roleName);
        }
        
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        // Check if username is being changed and if it's already taken
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) 
            && existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email is being changed and if it's already taken
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) 
            && existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Update user fields
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        
        // Only update password if it's provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            var passwordErrors = passwordValidationService.validatePassword(updatedUser.getPassword());
            if (!passwordErrors.isEmpty()) {
                throw new RuntimeException(String.join(", ", passwordErrors));
            }
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }
} 