package com.ecommerce.API;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.models.Order;
import com.ecommerce.models.Shipment;

/**
 * Contract for Shipping operations
 * Handles shipment creation, updates, and failure management
 */
public interface ShippingServiceContract {

    /**
     * Create shipment when order status changes to SHIPPED
     */
    void createShipment(Order order);

    /**
     * Create initial pending shipment during order creation
     */
    Shipment createPendingShipment(Order order, CreateOrderRequest request);

    /**
     * Mark shipment as failed
     */
    void failShipment(Order order, String reason);
}
