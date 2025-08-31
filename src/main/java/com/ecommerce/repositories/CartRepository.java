package com.ecommerce.repositories;

import com.ecommerce.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findByGuestCartId(String guestCartId);

    boolean existsByGuestCartId(String guestCartId);

    boolean existsByUserId(Long userId);
}
