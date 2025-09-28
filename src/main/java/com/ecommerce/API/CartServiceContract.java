package com.ecommerce.API;

import com.ecommerce.models.Cart;
import com.ecommerce.models.CartItem;

/**
 * Contract for Cart management operations
 * Handles cart operations for both authenticated users and guests
 */
public interface CartServiceContract {

    /**
     * Get cart for authenticated user
     */
    Cart getCartForUser(Long userId);

    /**
     * Get cart for guest user
     */
    Cart getCartForGuest(String guestId);

    /**
     * Add item to user cart
     */
    Cart addItemToUserCart(Long userId, CartItem cartItem);

    /**
     * Add item to guest cart
     */
    Cart addItemToGuestCart(String guestId, CartItem cartItem);

    /**
     * Update item quantity in cart
     */
    Cart updateCartItemQuantity(Long cartId, Long productId, Integer newQuantity);

    /**
     * Remove item from cart
     */
    Cart removeItemFromCart(Long cartId, Long productId);

    /**
     * Clear user cart after successful order
     */
    void clearUserCart(Long userId);

    /**
     * Clear guest cart after successful order
     */
    void clearGuestCart(String guestId);

    /**
     * Calculate cart total
     */
    Double calculateCartTotal(Cart cart);
}
