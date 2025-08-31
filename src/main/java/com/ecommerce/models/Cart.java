package com.ecommerce.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "carts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    private Long cartId;

    @Column(nullable = true)
    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private Integer totalItems;
    private Double totalAmount;
    @Column(name="guest_CartId")
    private String guestCartId;
    // must not be null , initially it's the creation date
    @Column(name="last_updated",nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name ="session_id")
    private String sessionId;

    @Column(name = "is_temporary")
    @Builder.Default
    private Boolean isTemporary = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;


    public void addItem(CartItem item) {
        Objects.requireNonNull(item, "CartItem cannot be null");
        item.setCart(this);
        this.items.add(item);
    }

    public void removeItem(Long productId) {
        this.items.removeIf(item -> Objects.equals(item.getProductId(), productId));
    }

    public void clearItems() {
        this.items.clear();
        this.totalItems = 0;
        this.totalAmount = 0.0;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void updateTotals(Integer totalItems, Double totalAmount) {
        this.totalItems = totalItems;
        this.totalAmount = totalAmount;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean containsProduct(Long productId) {
        return items.stream()
                .anyMatch(item -> Objects.equals(item.getProductId(), productId));
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(cartId, cart.cartId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId);
    }


}