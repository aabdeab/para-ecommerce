package com.ecommerce.controllers;

import com.ecommerce.DTOs.ApiResponse;
import com.ecommerce.DTOs.CartItemDto;
import com.ecommerce.DTOs.CartSummary;
import com.ecommerce.models.SecurityUser;
import com.ecommerce.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    @GetMapping
    public ResponseEntity<ApiResponse<CartSummary>> getCartSummary(
            @AuthenticationPrincipal SecurityUser userDetails) {

        CartSummary summary = cartService.getUserCartSummary(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(summary, "Cart retrieved successfully"));
    }
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartSummary>> addItemToCart(
            @Valid @RequestBody CartItemDto itemDto,
            @AuthenticationPrincipal SecurityUser userDetails) {

        cartService.addItemToUserCart(userDetails.getUserId(), itemDto);
        CartSummary summary = cartService.getUserCartSummary(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(summary, "Item added to cart successfully"));
    }
    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartSummary>> updateItemQuantity(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal SecurityUser userDetails) {

        cartService.updateUserCartItemQuantity(userDetails.getUserId(), productId, quantity);
        CartSummary summary = cartService.getUserCartSummary(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(summary, "Item quantity updated successfully"));
    }
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartSummary>> removeItemFromCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal SecurityUser userDetails) {

        cartService.removeItemFromUserCart(userDetails.getUserId(), productId);
        CartSummary summary = cartService.getUserCartSummary(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(summary, "Item removed from cart successfully"));
    }
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal SecurityUser userDetails) {

        cartService.clearUserCart(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared successfully"));
    }
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(
            @AuthenticationPrincipal SecurityUser userDetails) {

        Integer count = cartService.getUserCartItemCount(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(count, "Cart item count retrieved"));
    }
}