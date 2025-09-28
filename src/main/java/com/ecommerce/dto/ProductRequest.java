package com.ecommerce.dto;

import com.ecommerce.models.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank(message = "name must not be empty")
        @NotNull(message = "must provide a product name")
        String name,
        String description,
        String brand,
        @NotNull @PositiveOrZero Double price,
        boolean withDiscount,
        @PositiveOrZero Double discountPrice,
        @NotBlank String sku,
        boolean isVisible,
        ProductStatus productStatus,
        String imageUrl,
        @NotNull(message = "Must specify a category for the product")
        String category,
        @PositiveOrZero(message = "Initial stock must be zero or positive")
        Integer initialStock
) {
    public ProductRequest {
        if (initialStock == null) {
            initialStock = 0;
        }
    }
}