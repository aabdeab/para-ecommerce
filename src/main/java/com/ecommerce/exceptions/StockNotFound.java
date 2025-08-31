package com.ecommerce.exceptions;

public class StockNotFound extends RuntimeException {
    public StockNotFound(String message) {
        super(message);
    }
}
