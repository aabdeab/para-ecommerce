package com.ecommerce.API;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.models.Order;

/**
 * Contract for high-level orchestration operations
 * Coordinates complex business workflows across multiple services
 */
public interface OrchestratorServiceContract {

    /**
     * Complete order workflow orchestration
     * Coordinates: cart validation -> stock check -> order creation -> payment setup
     */
    Order orchestrateCompleteOrderWorkflow(Long userId, CreateOrderRequest request);

    /**
     * Payment completion orchestration
     * Coordinates: payment processing -> order confirmation -> stock finalization -> notifications
     */
    Order orchestratePaymentCompletion(Long orderId, PaymentRequest paymentRequest);

    /**
     * Order cancellation orchestration
     * Coordinates: order validation -> payment refund -> stock release -> notifications
     */
    Order orchestrateOrderCancellation(Long orderId, String reason);

    /**
     * Inventory replenishment orchestration
     * Coordinates: stock analysis -> supplier orders -> inventory updates
     */
    void orchestrateInventoryReplenishment();

    /**
     * Abandoned cart recovery orchestration
     * Coordinates: cart analysis -> customer outreach -> incentive offers
     */
    void orchestrateAbandonedCartRecovery();
}
