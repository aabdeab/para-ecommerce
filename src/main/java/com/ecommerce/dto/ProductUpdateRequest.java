package com.ecommerce.dto;

import com.ecommerce.models.ProductStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductUpdateRequest(
        String name,
        String description,
        String brand,
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,
        Boolean withDiscount,
        @DecimalMin(value = "0.0", message = "Discount price must be positive or zero")
        BigDecimal discountPrice,
        String sku,
        Boolean isVisible,
        ProductStatus productStatus,
        String imageUrl,
        String category,
        Double weight,
        Double height
) {
    // Validation logic similar to ProductRequest
    public ProductUpdateRequest {
        if (Boolean.TRUE.equals(withDiscount) && (discountPrice == null || discountPrice.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Discount price must be provided and positive when discount is enabled");
        }

        if (Boolean.TRUE.equals(withDiscount) && discountPrice != null && price != null && discountPrice.compareTo(price) >= 0) {
            throw new IllegalArgumentException("Discount price must be less than regular price");
        }
    }
}