package com.ecommerce.API;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.models.Order;
import com.ecommerce.models.Payment;
import com.ecommerce.models.PaymentMethod;

/**
 * Contract for Payment processing operations
 * Handles payment processing, refunds, and payment creation
 */
public interface PaymentServiceContract {

    /**
     * Process payment with validation and gateway integration
     */
    void processPayment(Payment payment, PaymentRequest paymentRequest);

    /**
     * Process refund for a successful payment
     */
    void processRefund(Payment payment);

    /**
     * Create pending payment for an order
     */
    Payment createPendingPayment(Order order, PaymentMethod method);
}
