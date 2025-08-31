package com.ecommerce.DTOs;

import lombok.Builder;

@Builder
public record CartItemSummary (
    Long productId,
    String productName,
    String productBrand,
    Integer quantity,
    Double price,
    Double subtotal,
    String imageId,
    Boolean isAvailable
){

    public static CartItemSummary unavailable(Long productId) {
        return CartItemSummary.builder()
                .productId(productId)
                .productName("Product Unavailable")
                .isAvailable(false)
                .build();
    }
    }

