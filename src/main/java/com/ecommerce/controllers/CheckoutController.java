package com.ecommerce.controllers;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.models.Order;
import com.ecommerce.models.SecurityUser;
import com.ecommerce.services.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller principal pour toutes les opérations de checkout.
 *
 * Ce controller orchestre le processus complet via CheckoutService:
 * - Création de commandes
 * - Traitement des paiements
 * - Annulation de commandes
 *
 * Pour les opérations de consultation des commandes, voir OrderController.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Créer une commande pour un utilisateur authentifié
     * Orchestre: panier -> réservation stock -> commande -> paiement/expédition pending
     */
    @PostMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        log.info("[Checkout] Creating order for userId: {}", userId);
        Order order = checkoutService.createOrderForUser(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Traiter le paiement d'une commande
     * Orchestre: paiement -> confirmation commande -> confirmation stock -> notifications
     */
    @PostMapping("/orders/{orderId}/payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> processPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        log.info("[Checkout] Processing payment for orderId: {}", orderId);
        Order order = checkoutService.processPaymentByOrderId(orderId, paymentRequest);
        return ResponseEntity.ok(order);
    }

    /**
     * Annuler une commande
     * Orchestre: libération stock -> remboursement -> annulation expédition -> notifications
     */
    @PostMapping("/orders/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "Cancelled by user") String reason) {
        log.info("[Checkout] Cancelling orderId: {}, reason: {}", orderId, reason);
        Order order = checkoutService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(order);
    }

    private Long getUserId(Authentication authentication) {
        Authentication auth = authentication != null ? authentication : SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getUserId();
        }
        throw new IllegalStateException("User not authenticated for checkout operation");
    }
}
