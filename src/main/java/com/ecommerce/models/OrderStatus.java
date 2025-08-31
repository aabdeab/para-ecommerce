package com.ecommerce.models;

public enum OrderStatus {
    PENDING,        // Order created, payment pending
    CONFIRMED,      // Payment confirmed, processing can begin
    PROCESSING,     // Order being prepared
    SHIPPED,        // Order shipped
    DELIVERED,      // Order delivered
    COMPLETED,      // Order completed (after return window)
    CANCELED,       // Order canceled
    REFUNDED,       // Order refunded
    FAILED          // Payment or processing failed
}