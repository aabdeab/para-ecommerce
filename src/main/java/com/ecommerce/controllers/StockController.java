package com.ecommerce.controllers;

import com.ecommerce.models.Product;
import com.ecommerce.models.Stock;
import com.ecommerce.services.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * Create stock for a product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Stock> createStock(
            @RequestBody Product product,
            @RequestParam Integer totalQuantity) {

        log.info("Creating stock for product {}, quantity: {}", product.getProductId(), totalQuantity);
        Stock stock = stockService.createStock(product, totalQuantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }
}