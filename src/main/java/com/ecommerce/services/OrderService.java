package com.ecommerce.services;

import com.ecommerce.DTOs.CreateOrderRequest;
import com.ecommerce.DTOs.PaymentRequest;
import com.ecommerce.exceptions.OrderNotFound;
import com.ecommerce.exceptions.PaymentFailedException;
import com.ecommerce.models.*;
import com.ecommerce.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    /**
     * Create order for authenticated user
     */
    @Transactional
    public Order createUserOrder(Long userId, CreateOrderRequest request) {
        Cart cart = cartService.getCartForUser(userId);
        return createOrderFromCart(cart, request, userId, null, null);
    }
    /**
     * Process payment for an order
     */
    @Transactional
    public Order processPayment(Long orderId, PaymentRequest paymentRequest) {
        Order order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in pending state");
        }
        try {
            Payment payment = order.getPayment();
            paymentService.processPayment(payment, paymentRequest);

            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                confirmOrder(order);
            } else {
                handlePaymentFailure(order, payment.getFailureReason());
            }

        } catch (PaymentFailedException e) {
            handlePaymentFailure(order, e.getMessage());
            throw e;
        }

        return order;
    }

    /**
     * Confirm order after successful payment
     */
    @Transactional
    public Order confirmOrder(Order order) {
        order.setStatus(OrderStatus.CONFIRMED);
        stockService.confirmReservations(order.getStockReservations());

        if (order.getUserId() != null) {
            cartService.clearUserCart(order.getUserId());
        } else if (order.getGuestOrderId() != null) {
           cartService.clearGuestCart(order.getGuestOrderId());
        }

        Order savedOrder = orderRepository.save(order);

        notificationService.sendOrderConfirmation(savedOrder);

        log.info("Order confirmed: orderNumber={}", order.getOrderNumber());
        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = findOrderById(orderId);
        if (!order.canBeCanceled()) {
            throw new IllegalStateException("Order cannot be canceled in current status: " + order.getStatus());
        }
        stockService.releaseReservations(order.getStockReservations());
        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.SUCCEEDED) {
            paymentService.processRefund(order.getPayment());
        }
        if (order.getShipment() != null &&
                order.getShipment().getStatus() == ShipmentStatus.PENDING) {
            shippingService.failShipment(order, "Order canceled: " + reason);
        }

        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
        order.setCancelReason(reason);

        Order savedOrder = orderRepository.save(order);

        notificationService.sendOrderCancellation(savedOrder);

        log.info("Order canceled: orderNumber={}, reason={}", order.getOrderNumber(), reason);
        return savedOrder;
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


    private Order createOrderFromCart(Cart cart, CreateOrderRequest request, Long userId,
                                      String guestOrderId, String guestEmail) {

        List<StockReservation> reservations = stockService.reserveStockForCart(cart);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .guestOrderId(guestOrderId)
                .guestEmail(guestEmail)
                .status(OrderStatus.PENDING)
                .subtotal(cart.getTotalAmount())
                .taxAmount(calculateTax(cart.getTotalAmount()))
                .shippingCost(calculateShippingCost(request))
                .discountAmount(request.getDiscountAmount())
                .currencyCode("USD")
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .stockReservations(reservations)
                .build();

        order.setTotalAmount(order.getSubtotal() + order.getTaxAmount() +
                order.getShippingCost() - order.getDiscountAmount());

        List<OrderItem> orderItems = convertCartItemsToOrderItems(cart, order);
        order.setOrderItems(orderItems);

        reservations.forEach(reservation -> reservation.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        Payment payment = paymentService.createPendingPayment(savedOrder, request.getPaymentMethod());
        savedOrder.setPayment(payment);

        Shipment shipment = shippingService.createPendingShipment(savedOrder, request);
        savedOrder.setShipment(shipment);

        log.info("Order created successfully: orderNumber={}, userId={}, guestOrderId={}",
                savedOrder.getOrderNumber(), userId, guestEmail);

        return savedOrder;
    }
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getGuestOrderByGuestOrderId(String guestOrderId) {
        return  orderRepository.findByGuestOrderId(guestOrderId)
                .orElseThrow(() -> new OrderNotFound("Guest order not found: " + guestOrderId));
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Order findOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFound("Order not found: " + orderNumber));
    }

    // ===== PRIVATE HELPER METHODS =====

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound("Order not found: " + orderId));
    }

    private List<OrderItem> convertCartItemsToOrderItems(Cart cart, Order order) {
        return cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .productId(cartItem.getProductId())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getPrice())
                        .totalPrice(cartItem.calculateSubtotal())
                        .build())
                .collect(Collectors.toList());
    }

    private void handlePaymentFailure(Order order, String reason) {
        order.setStatus(OrderStatus.FAILED);
        stockService.releaseReservations(order.getStockReservations());
        if (order.getShipment() != null) {
            shippingService.failShipment(order, "Payment failed: " + reason);
        }
        orderRepository.save(order);
        notificationService.sendPaymentFailure(order, reason);
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
        switch (to) {
            case PROCESSING -> notificationService.sendOrderProcessing(order);
            case SHIPPED -> {
                shippingService.createShipment(order);
                notificationService.sendOrderShipped(order);
            }
            case DELIVERED -> notificationService.sendOrderDelivered(order);
            case COMPLETED -> notificationService.sendOrderCompleted(order);
        }
    }

    // ===== UTILITY METHODS =====

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private String generateGuestOrderId() {
        return "GUEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    private Double calculateTax(Double subtotal) {
        return subtotal * 0.08;
    }

    private Double calculateShippingCost(CreateOrderRequest request) {
        return request.getExpressShipping() ? 15.0 : 5.0;
    }



}