package com.ecommerce.services;

import com.ecommerce.exceptions.InsufficientStockException;
import com.ecommerce.exceptions.StockNotFound;
import com.ecommerce.models.*;
import com.ecommerce.repositories.StockRepository;
import com.ecommerce.repositories.StockReservationRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {
    private static final Logger logger = Logger.getLogger(StockService.class.getName());
    private final StockReservationRepository stockReservationRepository;
    private final StockRepository stockRepository;

    @Value("${ecommerce.stock.reservation.expiry.minutes:30}")
    private int stockReservationExpiryMinutes;

    /**
     * Create a stock record for a specific product
     */
    public Stock createStock(Product product, int totalQuantity) {
        if (totalQuantity == 0) {
            logger.info("Creating stock for product with id=" + product.getProductId() + " with zero quantity");
        }
        return stockRepository.save(
                Stock.builder()
                        .product(product)
                        .totalQuantity(totalQuantity)
                        .availableQuantity(totalQuantity)
                        .reservedQuantity(0)
                        .build()
        );
    }

    /**
     * Reserve stock for entire cart - creates reservations and updates stock
     */
    @Transactional
    public List<StockReservation> reserveStockForCart(Cart cart) {
        logger.info("Reserving stock for cartId=" + cart.getCartId());

        List<StockReservation> reservations = cart.getItems().stream()
                .map(this::createStockReservation)
                .collect(Collectors.toList());

        reservations.forEach(reservation -> {
            reserveStock(reservation.getProductId(), reservation.getQuantity());
        });

        return stockReservationRepository.saveAll(reservations);
    }

    /**
     * Reserve a quantity of a product's stock during checkout to prevent overselling
     */
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProduct_ProductId(productId)
                //this should NOT happen
                .orElseThrow(() -> new StockNotFound("Stock not found for product: " + productId));

        if (stock.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + productId +
                            ". Available: " + stock.getAvailableQuantity() +
                            ", Requested: " + quantity
            );
        }

        // Move quantity from available to reserved
        stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);

        stockRepository.save(stock);

        logger.info("Reserved " + quantity + " units for product " + productId);
    }

    /**
     * Release individual reservation (used internally)
     */
    @Transactional
    public void releaseReservation(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found for product: " + productId));

        // Move quantity from reserved back to available
        stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
        stock.setReservedQuantity(Math.max(0, stock.getReservedQuantity() - quantity));

        stockRepository.save(stock);

        logger.info("Released " + quantity + " units for product " + productId);
    }

    /**
     * Confirm reservations after successful payment - converts reserved to sold
     */
    @Transactional
    public void confirmReservations(List<StockReservation> stockReservations) {
        stockReservations.forEach(reservation -> {
            Stock stock = stockRepository.findByProduct_ProductId(reservation.getProductId())
                    .orElseThrow(() -> new RuntimeException("Stock not found for product: " + reservation.getProductId()));

            // Move from reserved to sold (reduce total quantity, keep available same)
            stock.setReservedQuantity(stock.getReservedQuantity() - reservation.getQuantity());
            stock.setTotalQuantity(stock.getTotalQuantity() - reservation.getQuantity());

            stockRepository.save(stock);

            // Update reservation status
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setConfirmedAt(LocalDateTime.now());
        });

        stockReservationRepository.saveAll(stockReservations);
        logger.info("Confirmed " + stockReservations.size() + " stock reservations");
    }

    /**
     * Release multiple reservations (for cancellations/failures)
     */
    @Transactional
    public void releaseReservations(List<StockReservation> stockReservations) {
        stockReservations.forEach(reservation -> {
            if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                releaseReservation(reservation.getProductId(), reservation.getQuantity());
                reservation.setStatus(ReservationStatus.RELEASED);
                reservation.setReleasedAt(LocalDateTime.now());
            }
        });
        stockReservationRepository.saveAll(stockReservations);
    }
    /**
     * Bulk release reservations (same as above, for compatibility)
     */
    @Transactional
    public void releaseStockReservations(List<StockReservation> reservations) {
        releaseReservations(reservations);
    }

    /**
     * Create a stock reservation record for a cart item
     */
    private StockReservation createStockReservation(CartItem cartItem) {
        return StockReservation.builder()
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .status(ReservationStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(stockReservationExpiryMinutes))
                .build();
    }
}