package com.ecommerce.configurations;

import com.ecommerce.API.JwtTokenService;
import com.ecommerce.DTOs.ErrorResponse;
import com.ecommerce.models.SecurityUser;
import com.ecommerce.models.User;
import com.ecommerce.models.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtTokenService.validateToken(token)) {
                    String email = jwtTokenService.getUserFromToken(token);
                    Long userId = jwtTokenService.getUserIdFromToken(token);
                    String roleName = jwtTokenService.getRoleFromToken(token);
                    Collection<? extends GrantedAuthority> authorities =
                            jwtTokenService.getAuthoritesFromToken(token);
                    User userEntity = new User();
                    userEntity.setUserId(userId);
                    userEntity.setEmail(email);
                    userEntity.setRole(UserRole.valueOf(roleName));

                    SecurityUser userDetails = new SecurityUser(userEntity);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    writeErrorResponse(response,
                            HttpStatus.UNAUTHORIZED,
                            "Invalid or expired JWT token");
                    return;
                }
            } catch (Exception ex) {
                writeErrorResponse(response,
                        HttpStatus.FORBIDDEN,
                        "Malformed or invalid JWT: " + ex.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/");
    }
    private void writeErrorResponse(HttpServletResponse response,
                                    HttpStatus status,
                                    String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(status, message);

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
