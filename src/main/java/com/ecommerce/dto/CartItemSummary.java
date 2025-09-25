package com.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record CartItemSummary(
        @NotNull
        Long productId,

        @NotBlank
        String productName,

        String productBrand,

        @Min(0)
        Integer quantity,

        @Min(0)
        Double price,

        @Min(0)
        Double subtotal,

        String imageId,

        @NotNull
        Boolean isAvailable
) {

    public static CartItemSummary unavailable(Long productId) {
        return CartItemSummary.builder()
                .productId(productId)
                .productName("Product Unavailable")
                .productBrand(null)
                .quantity(0)
                .price(0.0)
                .subtotal(0.0)
                .imageId(null)
                .isAvailable(false)
                .build();
    }
}
