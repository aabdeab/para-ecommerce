package com.ecommerce.DTOs;

import com.ecommerce.models.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank String name,
        String description,
        String brand,
        @NotNull @PositiveOrZero Double price,
        boolean withDiscount,
        @PositiveOrZero Double discountPrice,
        @NotBlank String sku,
        boolean isVisible,
        ProductStatus productStatus,
        String imageUrl,
        @Positive
        @NotNull String category
) {
    public ProductRequest {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be empty");
        }
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price must be positive or zero");
        }
        if (sku != null && sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be empty");
        }
    }
}