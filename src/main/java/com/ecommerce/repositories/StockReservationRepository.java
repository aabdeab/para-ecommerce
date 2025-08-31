package com.ecommerce.repositories;

import com.ecommerce.models.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation,Long> {
}
