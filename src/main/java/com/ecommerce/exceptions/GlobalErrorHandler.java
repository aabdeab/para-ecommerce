package com.ecommerce.exceptions;

import com.ecommerce.DTOs.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex){
        return buildResponse(HttpStatus.FORBIDDEN, "Authentication Failed: " + ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException ex){
        return buildResponse(HttpStatus.CONFLICT, "Insufficient stock: " + ex.getMessage());
    }

    @ExceptionHandler(OrderNotFound.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFound ex){
        return buildResponse(HttpStatus.NOT_FOUND, "Order not found: " + ex.getMessage());
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailedException(PaymentFailedException ex){
        return buildResponse(HttpStatus.PAYMENT_REQUIRED, "Payment failed: " + ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex){
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found: " + ex.getMessage());
    }

    @ExceptionHandler(StockNotFound.class)
    public ResponseEntity<ErrorResponse> handleStockNotFound(StockNotFound ex){
        return buildResponse(HttpStatus.NOT_FOUND, "Stock not found: " + ex.getMessage());
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleTokenValidation(TokenValidationException ex){
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid token: " + ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex){
        return buildResponse(HttpStatus.NOT_FOUND, "User not found: " + ex.getMessage());
    }

    @ExceptionHandler(UserNotValidException.class)
    public ResponseEntity<ErrorResponse> handleUserNotValid(UserNotValidException ex){
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid user data: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex){
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return new ResponseEntity<>(new ErrorResponse(status, message), status);
    }
}



