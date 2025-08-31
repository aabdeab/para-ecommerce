package com.ecommerce.API;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Service interface for JWT token operations
 */
public interface JwtTokenService {

    /**
     * Generate a JWT token from an Authentication object
     * @param authentication the authentication object containing user details
     * @return the generated JWT token as a string
     */
    String generateToken(Authentication authentication);

    /**
     * Extract username (email) from a JWT token
     * @param token the JWT token
     * @return the username/email from the token
     */
    String getUserFromToken(String token);

    /**
     * Validate a JWT token
     * @param token the JWT token to validate
     * @return true if the token is valid, throws exception if invalid
     */
    boolean validateToken(String token);

    Collection<? extends GrantedAuthority> getAuthoritesFromToken(String token);
    public Long getUserIdFromToken(String token);
}