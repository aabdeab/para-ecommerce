package com.ecommerce.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stock", indexes = {
        @Index(name = "idx_stock_product", columnList = "productId", unique = true)
})
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockId;

    @OneToOne
    @JoinColumn(name="product_id",nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;

    public boolean isAvailable(Integer requestedQuantity) {
        return availableQuantity >= requestedQuantity;
    }

    public boolean isLowStock() {
        return availableQuantity <= lowStockThreshold;
    }

    public void reserveStock(Integer quantity) {
        if (!isAvailable(quantity)) {
            throw new IllegalStateException("Insufficient stock to reserve");
        }
        this.reservedQuantity += quantity;
        this.availableQuantity -= quantity;
    }

    public void releaseReservation(Integer quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
        this.availableQuantity = Math.min(totalQuantity, this.availableQuantity + quantity);
    }

    public void confirmSale(Integer quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
        this.totalQuantity = Math.max(0, this.totalQuantity - quantity);
    }

    public boolean canReserve(Integer requestedQuantity) {
        return availableQuantity >= requestedQuantity;
    }
}