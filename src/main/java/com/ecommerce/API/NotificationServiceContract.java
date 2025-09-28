package com.ecommerce.API;

import com.ecommerce.models.Order;

/**
 * Contract for Notification operations
 * Handles all types of notifications (order, payment, shipping)
 */
public interface NotificationServiceContract {

    /**
     * Send order confirmation notification
     */
    void sendOrderConfirmation(Order order);

    /**
     * Send order cancellation notification
     */
    void sendOrderCancellation(Order order);

    /**
     * Send payment failure notification
     */
    void sendPaymentFailure(Order order, String reason);

    /**
     * Send order processing notification
     */
    void sendOrderProcessing(Order order);

    /**
     * Send order shipped notification
     */
    void sendOrderShipped(Order order);

    /**
     * Send order delivered notification
     */
    void sendOrderDelivered(Order order);

    /**
     * Send order completed notification
     */
    void sendOrderCompleted(Order order);
}
