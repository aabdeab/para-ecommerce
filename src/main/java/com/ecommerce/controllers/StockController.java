package com.ecommerce.controllers;

import com.ecommerce.models.Product;
import com.ecommerce.models.Stock;
import com.ecommerce.services.ProductService;
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
    private final ProductService productService;

    /**
     * Réapprovisionnement - INVENTORY_MANAGER ajoute du stock après livraison fournisseur
     */
    @PostMapping("/{productId}/restock")
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<?> restockProduct(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        log.info("Restocking product {}, adding quantity: {}", productId, quantity);
        Product product = productService.getProductById(productId);
        Stock currentStock = stockService.getStockForProduct(product);

        if (currentStock == null) {
            // Retourner une erreur si le stock n'existe pas
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Le stock pour ce produit n'existe pas. La création initiale du stock doit être faite par un ADMIN lors de la création du produit.");
        }

        // Ajouter au stock existant
        currentStock.setTotalQuantity(currentStock.getTotalQuantity() + quantity);
        currentStock.setAvailableQuantity(currentStock.getAvailableQuantity() + quantity);
        Stock updatedStock = stockService.updateStock(currentStock);
        return ResponseEntity.ok(updatedStock);
    }

    /**
     * Ajustement de stock - INVENTORY_MANAGER corrige les quantités (inventaire, casse, etc.)
     */
    @PutMapping("/{productId}/adjust")
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<?> adjustStock(
            @PathVariable Long productId,
            @RequestParam Integer newQuantity,
            @RequestParam(required = false) String reason) {

        log.info("Adjusting stock for product {}, new quantity: {}, reason: {}",
                productId, newQuantity, reason != null ? reason : "No reason provided");

        Product product = productService.getProductById(productId);
        Stock currentStock = stockService.getStockForProduct(product);

        if (currentStock == null) {
            // Retourner une erreur si le stock n'existe pas
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Le stock pour ce produit n'existe pas. La création initiale du stock doit être faite par un ADMIN lors de la création du produit.");
        }

        int difference = newQuantity - currentStock.getAvailableQuantity();
        currentStock.setTotalQuantity(currentStock.getTotalQuantity() + difference);
        currentStock.setAvailableQuantity(newQuantity);
        Stock updatedStock = stockService.updateStock(currentStock);
        return ResponseEntity.ok(updatedStock);
    }

    /**
     * Consultation du stock - accessible à ADMIN et INVENTORY_MANAGER
     */
    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Stock> getStock(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        Stock stock = stockService.getStockForProduct(product);

        if (stock == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stock);
    }
}
