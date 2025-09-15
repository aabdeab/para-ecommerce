package com.ecommerce.dto;

import java.math.BigDecimal;



public record PaymentRequest (
    BigDecimal amount,
    String currencyCode,
    String cardNumber,
    String expiryMonth,
    String expiryYear,
    String cvv,
    String paypalEmail,
    String bankAccount
){};
