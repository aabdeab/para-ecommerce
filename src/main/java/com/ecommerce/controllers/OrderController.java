package com.ecommerce.controllers;

import com.ecommerce.DTOs.CreateOrderRequest;
import com.ecommerce.DTOs.PaymentRequest;
import com.ecommerce.models.Order;
import com.ecommerce.models.OrderStatus;
import com.ecommerce.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {


    private final OrderService orderService;

    /**
     * Create order for authenticated user
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> createUserOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Creating order for userId: {}", userId);

        Order order = orderService.createUserOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Process payment for an order
     */
    @PostMapping("/{orderId}/payment")
    public ResponseEntity<Order> processPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest paymentRequest) {

        log.info("Processing payment for orderId: {}", orderId);
        Order order = orderService.processPayment(orderId, paymentRequest);
        return ResponseEntity.ok(order);
    }
    /**
     * Cancel an order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "Cancelled by user") String reason) {

        log.info("Cancelling order: {}, reason: {}", orderId, reason);
        Order order = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(order);
    }

    /**
     * Update order status (admin/inventory manager only)
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {

        log.info("Updating order {} to status: {}", orderId, newStatus);
        Order order = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(order);
    }

    /**
     * Get user's orders
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get guest order by guest order ID
     */
    @GetMapping("/guest/{guestOrderId}")
    public ResponseEntity<Order> getGuestOrder(@PathVariable String guestOrderId) {
        log.info("Guest order lookup for guestOrderId: {}", guestOrderId);
        Order order = orderService.getGuestOrderByGuestOrderId(guestOrderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Get orders by status (admin/inventory manager only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Find order by order number
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> findOrderByNumber(@PathVariable String orderNumber) {
        Order order = orderService.findOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * Get orders by user ID (admin/inventory manager only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // ===== HELPER METHODS =====

    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Simple implementation - adjust based on your authentication setup
        return Long.parseLong(authentication.getName());
    }
}