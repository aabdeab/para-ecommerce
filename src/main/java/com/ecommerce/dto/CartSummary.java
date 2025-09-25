package com.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record CartSummary(
        Long cartId,
        @NotNull
        Long userId,
        String guestCartId,
        @NotNull
        List<CartItemSummary> items,
        @Min(0)
        Integer totalItems,
        @Min(0)
        Double totalAmount,
        Boolean isEmpty,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
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
