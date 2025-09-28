package com.ecommerce.API;

import com.ecommerce.models.*;

import java.util.List;

/**
 * Contract for Stock management operations
 * Handles stock reservations, confirmations, and releases
 */
public interface StockServiceContract {

    /**
     * Create stock record for a product
     */
    Stock createStock(Product product, int totalQuantity);

    /**
     * Reserve stock for entire cart
     */
    List<StockReservation> reserveStockForCart(Cart cart);

    /**
     * Reserve specific quantity for a product
     */
    void reserveStock(Long productId, Integer quantity);

    /**
     * Release individual reservation
     */
    void releaseReservation(Long productId, Integer quantity);

    /**
     * Confirm reservations after successful payment
     */
    void confirmReservations(List<StockReservation> stockReservations);

    /**
     * Release multiple reservations (for cancellations/failures)
     */
    void releaseReservations(List<StockReservation> stockReservations);

    /**
     * Clean up expired reservations
     */
    void cleanupExpiredReservations();

    /**
     * Check if stock is available for reservation
     */
    boolean isStockAvailable(Long productId, Integer quantity);
}
