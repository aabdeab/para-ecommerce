package com.ecommerce.API;

//import com.ecommerce.dto.LoginRequestDTO;
//import com.ecommerce.dto.RegisterRequest;
//import com.ecommerce.dto.AuthResponse;
import com.ecommerce.models.User;

/**
 * Contract for Authentication operations
 * Handles user registration, login, and token management
 */
public interface AuthServiceContract {

    /**
     * Register new user
     */
    //AuthResponse register(RegisterRequest registerRequest);

    /**
     * Authenticate user login
     */
   // AuthResponse login(LoginRequest loginRequest);

    /**
     * Refresh JWT token
     */
   // AuthResponse refreshToken(String refreshToken);

    /**
     * Logout user (invalidate token)
     */
    void logout(String token);

    /**
     * Validate token and get user
     */
    User validateTokenAndGetUser(String token);

    /**
     * Change user password
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * Reset password request
     */
    void requestPasswordReset(String email);

    /**
     * Reset password with token
     */
    void resetPassword(String resetToken, String newPassword);
}
