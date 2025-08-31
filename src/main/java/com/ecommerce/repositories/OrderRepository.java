package com.ecommerce.repositories;

import com.ecommerce.models.Order;
import com.ecommerce.models.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserId(Long userId);

    Optional<Order> findByOrderId(Long orderId);

    List<Order> findByStatus(OrderStatus status);

    Optional<Order> findByGuestOrderId(String guestOrderId);

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
