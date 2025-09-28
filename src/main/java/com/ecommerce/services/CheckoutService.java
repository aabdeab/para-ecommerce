package com.ecommerce.services;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.exceptions.PaymentFailedException;
import com.ecommerce.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service orchestrateur principal qui coordonne tout le processus de checkout.
 * CheckoutService est le seul service qui:
 * - Orchestre le flux complet entre différents services
 * - Coordonne les appels aux services OrderService, StockService, PaymentService, ShippingService
 * - Gère les transactions et les états cohérents entre tous les composants
 * - S'occupe des rollbacks en cas d'échec
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrderService orderService;
    private final CartService cartService;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    /**
     * Point d'entrée principal pour créer une commande pour un utilisateur
     */
    @Transactional
    public Order createOrderForUser(Long userId, CreateOrderRequest request) {
        // Validation d'entrée
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Create order request cannot be null");
        }

        Cart cart = cartService.getCartForUser(userId);

        // Validation du panier
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order: cart is empty for user " + userId);
        }

        // Validation des montants
        if (cart.getTotalAmount() == null || cart.getTotalAmount() <= 0) {
            throw new IllegalStateException("Cannot create order: invalid cart total amount");
        }

        return createOrderFromCart(cart, request, userId, null, null);
    }

    /**
     * Méthode d'orchestration principale qui coordonne:
     * 1. Réservation du stock
     * 2. Création de la commande
     * 3. Préparation du paiement
     * 4. Préparation de l'expédition
     */
    @Transactional
    public Order createOrderFromCart(Cart cart, CreateOrderRequest request, Long userId,
                                     String guestOrderId, String guestEmail) {
        // 1. Réserver le stock d'abord
        List<StockReservation> reservations = stockService.reserveStockForCart(cart);

        try {
            // 2. Créer la commande via OrderService
            Order order = orderService.createOrder(
                    generateOrderNumber(),
                    userId,
                    guestOrderId,
                    guestEmail,
                    cart.getTotalAmount(),
                    calculateTax(cart.getTotalAmount()),
                    calculateShippingCost(request),
                    request.getDiscountAmount(),
                    request.getShippingAddress(),
                    request.getBillingAddress(),
                    "USD"
            );

            // 3. Ajouter les items à la commande
            List<OrderItem> orderItems = cart.getItems().stream()
                    .map(cartItem -> OrderItem.builder()
                            .order(order)
                            .productId(cartItem.getProductId())
                            .quantity(cartItem.getQuantity())
                            .unitPrice(cartItem.getPrice())
                            .totalPrice(cartItem.calculateSubtotal())
                            .build())
                    .collect(Collectors.toList());
            order.setOrderItems(orderItems);

            // 4. Lier les réservations à la commande et persister
            reservations.forEach(reservation -> reservation.setOrder(order));
            order.setStockReservations(reservations);

            // 5. Créer paiement et expédition pending
            Payment payment = paymentService.createPendingPayment(order, request.getPaymentMethod());
            order.setPayment(payment);

            Shipment shipment = shippingService.createPendingShipment(order, request);
            order.setShipment(shipment);

            // CRITIQUE: Sauvegarder l'ordre complet avec tous ses éléments
            Order savedOrder = orderService.saveOrder(order);

            log.info("Checkout: Order created successfully: orderNumber={}, userId={}, items={}",
                    savedOrder.getOrderNumber(), userId, savedOrder.getOrderItems().size());
            return savedOrder;

        } catch (Exception e) {
            // Rollback: libérer les réservations en cas d'échec
            stockService.releaseReservations(reservations);
            throw new RuntimeException("Order creation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Orchestre le processus de paiement:
     * 1. Traite le paiement
     * 2. Confirme ou échoue la commande selon le résultat
     * 3. Gère les états cohérents (stock, expédition, notifications)
     */
    @Transactional
    public Order processPaymentByOrderId(Long orderId, PaymentRequest paymentRequest) {
        // Validations d'entrée
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (paymentRequest == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }

        Order order = orderService.findOrderById(orderId);

        // Validation de l'état de la commande
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in pending state: " + order.getStatus());
        }

        // Validation de l'existence du paiement
        if (order.getPayment() == null) {
            throw new IllegalStateException("No payment found for order: " + order.getOrderNumber());
        }

        try {
            // Traiter le paiement
            Payment payment = order.getPayment();
            paymentService.processPayment(payment, paymentRequest);

            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                // Confirmer la commande et finaliser
                orderService.confirmOrder(orderId);

                // Vérifier que les réservations existent avant de les confirmer
                if (order.getStockReservations() != null && !order.getStockReservations().isEmpty()) {
                    stockService.confirmReservations(order.getStockReservations());
                } else {
                    log.warn("No stock reservations found for order: {}", order.getOrderNumber());
                }

                // Nettoyer le panier
                if (order.getUserId() != null) {
                    cartService.clearUserCart(order.getUserId());
                } else if (order.getGuestOrderId() != null) {
                    cartService.clearGuestCart(order.getGuestOrderId());
                }

                notificationService.sendOrderConfirmation(order);
                log.info("Checkout: Payment successful for order: {}", order.getOrderNumber());
            } else {
                handlePaymentFailure(order, payment.getFailureReason());
            }

        } catch (PaymentFailedException e) {
            handlePaymentFailure(order, e.getMessage());
            throw e;
        } catch (Exception e) {
            // Catch any unexpected errors and handle them as payment failures
            handlePaymentFailure(order, "Unexpected error during payment processing: " + e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }

        return order;
    }

    /**
     * Orchestre le processus d'annulation:
     * 1. Libère les réservations de stock
     * 2. Gère les remboursements si nécessaire
     * 3. Annule les expéditions
     * 4. Met à jour l'état de la commande
     */
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = orderService.findOrderById(orderId);

        // Libérer les réservations de stock
        if (order.getStockReservations() != null) {
            stockService.releaseReservations(order.getStockReservations());
        }

        // Rembourser si paiement réussi
        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.SUCCEEDED) {
            paymentService.processRefund(order.getPayment());
        }

        // Échouer l'expédition si pending
        if (order.getShipment() != null && order.getShipment().getStatus() == ShipmentStatus.PENDING) {
            shippingService.failShipment(order, "Order canceled: " + reason);
        }

        // Mettre à jour la commande
        Order canceledOrder = orderService.cancelOrder(orderId, reason);
        notificationService.sendOrderCancellation(canceledOrder);

        log.info("Checkout: Order canceled: orderNumber={}, reason={}",
                canceledOrder.getOrderNumber(), reason);
        return canceledOrder;
    }

    /**
     * Gestion cohérente des échecs de paiement
     */
    private void handlePaymentFailure(Order order, String reason) {
        // Marquer la commande comme échouée
        orderService.failOrder(order.getOrderId(), reason);

        // Libérer les réservations
        stockService.releaseReservations(order.getStockReservations());

        // Échouer l'expédition
        if (order.getShipment() != null) {
            shippingService.failShipment(order, "Payment failed: " + reason);
        }

        // Notifier l'échec
        notificationService.sendPaymentFailure(order, reason);
        log.warn("Checkout: Payment failed for order: {}, reason: {}",
                order.getOrderNumber(), reason);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private Double calculateTax(Double subtotal) {
        return subtotal * 0.08;
    }

    private Double calculateShippingCost(CreateOrderRequest request) {
        return request.getExpressShipping() ? 15.0 : 5.0;
    }
}
