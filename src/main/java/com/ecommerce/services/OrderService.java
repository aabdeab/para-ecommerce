package com.ecommerce.services;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.exceptions.OrderNotFound;
import com.ecommerce.models.*;
import com.ecommerce.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsable uniquement de la gestion CRUD des commandes.
 * Ne contient aucune logique d'orchestration.
 * Toute orchestration doit être déléguée à CheckoutService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Create a basic order entity (called by CheckoutService during orchestration)
     */
    @Transactional
    public Order createOrder(String orderNumber, Long userId, String guestOrderId, String guestEmail,
                           Double subtotal, Double taxAmount, Double shippingCost, Double discountAmount,
                           String shippingAddress, String billingAddress, String currencyCode) {
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .guestOrderId(guestOrderId)
                .guestEmail(guestEmail)
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .shippingCost(shippingCost)
                .discountAmount(discountAmount)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .currencyCode(currencyCode)
                .build();

        order.setTotalAmount(order.getSubtotal() + order.getTaxAmount() +
                order.getShippingCost() - order.getDiscountAmount());

        return orderRepository.save(order);
    }

    /**
     * Update order status
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = findOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();

        validateStatusTransition(oldStatus, newStatus);

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        // Handle status-specific actions
        handleStatusChange(savedOrder, oldStatus, newStatus);

        log.info("Order status updated: orderNumber={}, from={}, to={}",
                order.getOrderNumber(), oldStatus, newStatus);

        return savedOrder;
    }

    /**
     * Mark order as confirmed - doit être appelé par CheckoutService après un paiement réussi
     */
    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = findOrderById(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    /**
     * Mark order as failed - doit être appelé par CheckoutService après un échec de paiement
     */
    @Transactional
    public Order failOrder(Long orderId, String reason) {
        Order order = findOrderById(orderId);
        order.setStatus(OrderStatus.FAILED);
        return orderRepository.save(order);
    }

    /**
     * Cancel order - simple changement de statut, l'orchestration est gérée par CheckoutService
     */
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = findOrderById(orderId);
        if (!order.canBeCanceled()) {
            throw new IllegalStateException("Order cannot be canceled in current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        return orderRepository.save(order);
    }

    /**
     * Save a complete order with all its related entities (OrderItems, reservations, etc.)
     * Used by CheckoutService after order construction
     */
    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    // Query methods
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Order findOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFound("Order not found: " + orderNumber));
    }

    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound("Order not found: " + orderId));
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        boolean isValidTransition = switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELED || to == OrderStatus.FAILED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELED;
            case PROCESSING -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            case DELIVERED -> to == OrderStatus.COMPLETED || to == OrderStatus.REFUNDED;
            case CANCELED, FAILED, COMPLETED, REFUNDED -> false;
        };

        if (!isValidTransition) {
            throw new IllegalStateException("Invalid status transition from " + from + " to " + to);
        }
    }

    private void handleStatusChange(Order order, OrderStatus from, OrderStatus to) {
        // OrderService ne gère plus les side effects - c'est la responsabilité de CheckoutService
        log.info("Order status changed: orderNumber={}, from={}, to={}",
                order.getOrderNumber(), from, to);
    }
}
