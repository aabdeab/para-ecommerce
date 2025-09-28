package com.ecommerce.API;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.models.Order;

/**
 * Contract for Checkout orchestration operations
 * Coordinates the complete checkout process between multiple services
 */
public interface CheckoutServiceContract {

    /**
     * Create order for authenticated user
     */
    Order createOrderForUser(Long userId, CreateOrderRequest request);

    /**
     * Main orchestration method that coordinates:
     * 1. Stock reservation
     * 2. Order creation
     * 3. Payment preparation
     * 4. Shipment preparation
     */
    Order createOrderFromCart(com.ecommerce.models.Cart cart, CreateOrderRequest request,
                             Long userId, String guestOrderId, String guestEmail);

    /**
     * Process payment orchestration:
     * 1. Process payment
     * 2. Confirm or fail order based on result
     * 3. Manage consistent states (stock, shipment, notifications)
     */
    Order processPaymentByOrderId(Long orderId, PaymentRequest paymentRequest);

    /**
     * Orchestrate cancellation process:
     * 1. Release stock reservations
     * 2. Handle refunds if necessary
     * 3. Cancel shipments
     * 4. Update order state
     */
    Order cancelOrder(Long orderId, String reason);
}
