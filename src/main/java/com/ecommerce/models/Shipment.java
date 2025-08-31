package com.ecommerce.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentId;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(unique = true)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private ShippingProvider provider;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    private String serviceName; // e.g., "Express", "Standard"
    private Double shippingCost;

    @Column(columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(columnDefinition = "TEXT")
    private String deliveryAddress;

    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private LocalDateTime shippedAt;

    @Column(length = 1000)
    private String deliveryInstructions;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
