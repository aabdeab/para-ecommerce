package com.ecommerce.controllers;

import com.ecommerce.models.Order;
import com.ecommerce.models.OrderStatus;
import com.ecommerce.models.SecurityUser;
import com.ecommerce.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

/**
 * Controller responsable uniquement des opérations de consultation et d'administration des commandes.
 *
 * Pour les opérations de création, paiement et annulation de commandes,
 * utilisez CheckoutController qui orchestre correctement ces processus.
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Get user's orders (consultation seulement)
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by ID (consultation seulement)
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.findOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Get order by order number (consultation seulement)
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize( "hasRole('ADMIN')" )
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        Order order = orderService.findOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * Get orders by status (admin/inventory manager only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') ")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status (admin/inventory manager only)
     * Cette opération ne fait que changer le statut, sans orchestration
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') ")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {

        log.info("Admin updating order {} to status: {}", orderId, newStatus);
        Order order = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(order);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getUserId();
        }
        throw new IllegalStateException("User not authenticated");
    }

}