package com.ecommerce.API;

import com.ecommerce.models.User;
import com.ecommerce.models.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Contract for User management operations
 * Handles user CRUD operations and user-related queries
 */
public interface UserServiceContract {

    /**
     * Create new user
     */
    User createUser(String email, String password, String firstName, String lastName, UserRole role);

    /**
     * Find user by ID
     */
    Optional<User> findById(Long userId);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Update user information
     */
    User updateUser(Long userId, String firstName, String lastName);

    /**
     * Update user password
     */
    void updatePassword(Long userId, String newPassword);

    /**
     * Update user role (admin operation)
     */
    User updateUserRole(Long userId, UserRole newRole);

    /**
     * Deactivate user account
     */
    void deactivateUser(Long userId);

    /**
     * Activate user account
     */
    void activateUser(Long userId);

    /**
     * Get all users (admin operation)
     */
    List<User> getAllUsers();

    /**
     * Get users by role
     */
    List<User> getUsersByRole(UserRole role);

    /**
     * Check if email exists
     */
    boolean emailExists(String email);
}
