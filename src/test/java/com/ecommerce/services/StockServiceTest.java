package com.ecommerce.services;

import com.ecommerce.models.*;
import com.ecommerce.repositories.StockRepository;
import com.ecommerce.repositories.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class StockServiceTest {

    private StockReservationRepository stockReservationRepository;
    private StockRepository stockRepository;

    private StockService stockService;

    @BeforeEach
    void setup() {
        stockReservationRepository = mock(StockReservationRepository.class);
        stockRepository = mock(StockRepository.class);
        stockService = new StockService(stockReservationRepository, stockRepository);

        when(stockReservationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void reserveStockForCart_createsReservations_and_updatesStock() {
        Cart cart = new Cart();
        CartItem item1 = new CartItem();
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setPrice(10.0);
        cart.addItem(item1);

        Stock stock1 = Stock.builder()
                .product(new Product())
                .totalQuantity(100)
                .availableQuantity(100)
                .reservedQuantity(0)
                .build();
        when(stockRepository.findByProduct_ProductId(1L)).thenReturn(Optional.of(stock1));

        List<StockReservation> reservations = stockService.reserveStockForCart(cart);

        assertEquals(1, reservations.size());
        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(stockReservationRepository, times(1)).saveAll(anyList());
        assertEquals(98, stock1.getAvailableQuantity());
        assertEquals(2, stock1.getReservedQuantity());
    }

    @Test
    void confirmReservations_movesReservedToSold_and_updatesReservations() {
        StockReservation res = new StockReservation();
        res.setProductId(1L);
        res.setQuantity(3);
        res.setStatus(ReservationStatus.ACTIVE);
        List<StockReservation> list = new ArrayList<>();
        list.add(res);

        Stock stock = Stock.builder()
                .product(new Product())
                .totalQuantity(50)
                .availableQuantity(40)
                .reservedQuantity(10)
                .build();
        when(stockRepository.findByProduct_ProductId(1L)).thenReturn(Optional.of(stock));

        stockService.confirmReservations(list);

        assertEquals(7, stock.getReservedQuantity());
        assertEquals(47, stock.getTotalQuantity());
        assertEquals(ReservationStatus.CONFIRMED, res.getStatus());
        assertNotNull(res.getConfirmedAt());
        verify(stockReservationRepository).saveAll(list);
        verify(stockRepository).save(stock);
    }

    @Test
    void releaseReservations_whenActive_releasesStock_and_updatesStatus() {
        StockReservation res = new StockReservation();
        res.setProductId(2L);
        res.setQuantity(4);
        res.setStatus(ReservationStatus.ACTIVE);
        List<StockReservation> list = List.of(res);

        Stock stock = Stock.builder()
                .product(new Product())
                .totalQuantity(30)
                .availableQuantity(10)
                .reservedQuantity(8)
                .build();
        when(stockRepository.findByProduct_ProductId(2L)).thenReturn(Optional.of(stock));

        stockService.releaseReservations(list);

        assertEquals(14, stock.getAvailableQuantity());
        assertEquals(4, stock.getReservedQuantity());
        assertEquals(ReservationStatus.RELEASED, res.getStatus());
        assertNotNull(res.getReleasedAt());
        verify(stockReservationRepository).saveAll(list);
        verify(stockRepository).save(stock);
    }
}

