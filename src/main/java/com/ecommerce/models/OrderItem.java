package com.ecommerce.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long OrderItemId;
    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;
    private long productId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private Double discount;
}
