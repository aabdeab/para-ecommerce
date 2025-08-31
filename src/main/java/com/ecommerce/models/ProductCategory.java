package com.ecommerce.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long categoryId;
    @Column(nullable = false)
    private String name;
    private String description;
    @Builder.Default
    private Boolean isActive = true;
    @CreationTimestamp
    @Column(updatable = false)
    private Date createdAt;
    @OneToMany(mappedBy = "category",fetch=FetchType.LAZY)
    private List<Product> products;

}
