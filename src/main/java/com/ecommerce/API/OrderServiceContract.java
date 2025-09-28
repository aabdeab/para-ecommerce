package com.ecommerce.API;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.models.Order;
import com.ecommerce.models.OrderStatus;

import java.util.List;

/**
 * Contract for Order management operations
 * Handles CRUD operations for orders without orchestration logic
 */
public interface OrderServiceContract {

    /**
     * Create a basic order entity
     */
    Order createOrder(String orderNumber, Long userId, String guestOrderId, String guestEmail,
                     Double subtotal, Double taxAmount, Double shippingCost, Double discountAmount,
                     String shippingAddress, String billingAddress, String currencyCode);

    /**
     * Update order status with validation
     */
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);

    /**
     * Mark order as confirmed
     */
    Order confirmOrder(Long orderId);

    /**
     * Mark order as failed
     */
    Order failOrder(Long orderId, String reason);

    /**
     * Cancel an order
     */
    Order cancelOrder(Long orderId, String reason);

    /**
     * Save a complete order with all related entities
     */
    Order saveOrder(Order order);

    /**
     * Find order by ID
     */
    Order findOrderById(Long orderId);

    /**
     * Find order by order number
     */
    Order findOrderByNumber(String orderNumber);

    /**
     * Get orders by user ID
     */
    List<Order> getOrdersByUserId(Long userId);

    /**
     * Get orders by status
     */
    List<Order> getOrdersByStatus(OrderStatus status);
}
