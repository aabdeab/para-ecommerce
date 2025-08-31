package com.ecommerce.DTOs;
import lombok.Data;
import lombok.Getter;

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
