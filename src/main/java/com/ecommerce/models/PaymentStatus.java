package com.ecommerce.models;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    REFUNDED,
    PARTIALLY_REFUNDED
}