package com.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;



@Data
@Builder
@AllArgsConstructor
@Value
public class CartItemDto {
    @NotNull(message= "product id must not be null")
    Long productId;
    @NotNull(message= "quantity must not be null")
    Integer quantity;

}
