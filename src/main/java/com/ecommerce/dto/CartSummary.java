package com.ecommerce.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Builder
public record CartSummary(
        Long cartId,
        Long userId,
        String guestCartId,
        List<CartItemSummary> items,
        Integer totalItems,
        Double totalAmount,
        Boolean isEmpty,
        LocalDateTime lastUpdatedAt
) {
    public static CartSummary emptyUser(Long userId) {
        return new CartSummary(
                null,
                userId,
                null,
                Collections.emptyList(),
                0,
                0.0,
                true,
                LocalDateTime.now()
        );
    }

    public static CartSummary emptyGuest(String guestCartId) {
        return new CartSummary(
                null,
                null,
                guestCartId,
                Collections.emptyList(),
                0,
                0.0,
                true,
                LocalDateTime.now()
        );
    }
}
