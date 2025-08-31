package com.ecommerce.exceptions;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(String message, Exception e) {
        super(message,e);
    }
}
