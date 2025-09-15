package com.ecommerce.dto;

import com.ecommerce.models.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
@Value
public class CreateOrderRequest {
    @NotBlank(message = "Shipping address is required")
    String shippingAddress;

    @NotBlank(message = "Billing address is required")
    String billingAddress;

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod;

    @Email(message = "Valid email is required for guest orders")
    String guestEmail;

    @Builder.Default
    Boolean expressShipping = false;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    Double discountAmount = 0.0;

    String deliveryInstructions;
    String promoCode;
}