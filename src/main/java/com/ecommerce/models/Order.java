package com.ecommerce.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user_id", columnList = "userId"),
        @Index(name = "idx_order_guest_id", columnList = "guestOrderId"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_number", columnList = "orderNumber", unique = true)
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = true)
    private String guestOrderId;

    @Column(nullable = true)
    private String guestEmail;

    private Double subtotal;
    private Double taxAmount;
    private Double shippingCost;
    private Double discountAmount;
    private Double totalAmount;

    @Builder.Default
    private String currencyCode = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(columnDefinition = "TEXT")
    private String billingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipment shipment;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StockReservation> stockReservations = new ArrayList<>();
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime canceledAt;
    private String cancelReason;

    public boolean isGuestOrder() {
        return userId == null && guestOrderId != null;
    }

    public boolean canBeCanceled() {
        return status == OrderStatus.PENDING ||
                status == OrderStatus.CONFIRMED ||
                status == OrderStatus.PROCESSING;
    }

    public boolean canBeRefunded() {
        return status == OrderStatus.DELIVERED ||
                status == OrderStatus.COMPLETED;
    }
}
