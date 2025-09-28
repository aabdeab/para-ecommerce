package com.ecommerce.controllers;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductWithStockResponse;
import com.ecommerce.mappers.ProductMapper;
import com.ecommerce.models.Product;
import com.ecommerce.services.ProductService;
import com.ecommerce.services.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final StockService stockService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductWithStockResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("Creating product: {} with initial stock: {}", request.name(), request.initialStock());

        Product created = productService.createProduct(request, request.category());

        // Récupérer la quantité de stock créée
        Integer stockQuantity = null;
        if (request.initialStock() != null && request.initialStock() > 0) {
            stockQuantity = stockService.getAvailableStock(created);
        }

        ProductWithStockResponse response = ProductWithStockResponse.fromProduct(created, stockQuantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductWithStockResponse> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        Integer stockQuantity = stockService.getAvailableStock(product);
        ProductWithStockResponse response = ProductWithStockResponse.fromProduct(product, stockQuantity);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // Seul ADMIN peut modifier les infos produit (pas le stock)
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        Product product = ProductMapper.mapToProduct(request);
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hardDeleteProduct(@PathVariable Long id) {
        productService.hardDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
