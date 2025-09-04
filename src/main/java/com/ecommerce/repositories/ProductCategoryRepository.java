package com.ecommerce.repositories;

import com.ecommerce.models.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Long> {
    Optional<ProductCategory> findByName(String category);
}
