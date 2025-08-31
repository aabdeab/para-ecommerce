package com.ecommerce.repositories;

import com.ecommerce.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface StockRepository extends JpaRepository<Stock,Long> {
    @Override
    Optional<Stock> findById(Long aLong);
    Optional<Stock> findByProduct_ProductId(Long productId);
}
