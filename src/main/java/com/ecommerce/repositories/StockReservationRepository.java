package com.ecommerce.repositories;

import com.ecommerce.models.ReservationStatus;
import com.ecommerce.models.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation,Long> {
    List<StockReservation> findByStatusAndExpiresAtBefore(ReservationStatus reservationStatus, LocalDateTime now);
}
