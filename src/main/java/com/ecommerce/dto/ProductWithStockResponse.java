package com.ecommerce.dto;

import com.ecommerce.models.Product;
import com.ecommerce.models.ProductStatus;
import jakarta.validation.constraints.*;

public record ProductWithStockResponse(
        @NotNull(message = "Product ID cannot be null")
        @Positive(message = "Product ID must be positive")
        Long productId,

        @NotBlank(message = "Product name cannot be blank")
        @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @Size(max = 100, message = "Brand name cannot exceed 100 characters")
        String brand,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @DecimalMax(value = "999999.99", message = "Price cannot exceed 999999.99")
        Double price,

        boolean withDiscount,

        @DecimalMin(value = "0.0", message = "Discount price must be positive")
        @DecimalMax(value = "999999.99", message = "Discount price cannot exceed 999999.99")
        Double discountPrice,

        @NotBlank(message = "SKU cannot be blank")
        @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "SKU must contain only uppercase letters, numbers, hyphens and underscores")
        String sku,

        boolean isVisible,

        @NotNull(message = "Product status cannot be null")
        ProductStatus productStatus,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        String imageUrl,

        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        String categoryName,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        @Max(value = 999999, message = "Stock quantity cannot exceed 999999")
        Integer currentStock,

        boolean hasStock
) {
    // Validation personnalisée dans le constructeur compact
    public ProductWithStockResponse {
        // Validation de cohérence entre prix et prix de remise
        if (withDiscount && discountPrice != null && price != null && discountPrice >= price) {
            throw new IllegalArgumentException("Discount price must be less than regular price");
        }

        // Validation de cohérence entre stock et hasStock
        if (currentStock != null && currentStock > 0 && !hasStock) {
            throw new IllegalArgumentException("hasStock should be true when currentStock > 0");
        }

        if (currentStock != null && currentStock <= 0 && hasStock) {
            throw new IllegalArgumentException("hasStock should be false when currentStock <= 0");
        }

        // Validation SKU non null si fourni
        if (sku != null && sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be empty or whitespace only");
        }
    }

    public static ProductWithStockResponse fromProduct(Product product, Integer stockQuantity) {
        return new ProductWithStockResponse(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.isWithDiscount(),
                product.getDiscountPrice(),
                product.getSku(),
                product.getIsVisible(),
                product.getProductStatus(),
                product.getImageUrl(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                stockQuantity != null ? stockQuantity : 0,
                stockQuantity != null && stockQuantity > 0
        );
    }
}
