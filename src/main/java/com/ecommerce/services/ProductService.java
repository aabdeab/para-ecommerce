package com.ecommerce.services;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.exceptions.CategoryNotFoundException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.mappers.ProductMapper;
import com.ecommerce.models.*;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.repositories.ProductCategoryRepository;
import io.jsonwebtoken.lang.Assert;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final StockService stockService;

    @Transactional
    public Product createProduct(ProductRequest request, String categoryName) {
        ProductCategory category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Product newProduct = ProductMapper.mapToProduct(request);
        newProduct.setCategory(category);
        newProduct.setIsVisible(true);
        newProduct.setProductStatus(ProductStatus.AVAILABLE);

        // Sauvegarder le produit d'abord
        Product savedProduct = productRepository.save(newProduct);
        
        // Créer automatiquement le stock si initialStock > 0
        if (request.initialStock() != null && request.initialStock() > 0) {
            log.info("Creating initial stock of {} for product {}", request.initialStock(), savedProduct.getName());
            stockService.createStock(savedProduct, request.initialStock());
        }

        return savedProduct;
    }

    /**
     *
     * @param id the product id
     * @return product requested by id
     * @Throws IllegalArgumentException if id is null
     */
    public Product getProductById(Long id) {
        Assert.notNull(id, "Product ID must not be null");
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    public List<Product> getProductsByCategory(Long categoryId) {
        Assert.notNull(categoryId, "Category ID must not be null");
        return productRepository.findByCategoryCategoryId(categoryId);
    }
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Objects.requireNonNull(updatedProduct, "Updated product must not be null");
        Product existing = getProductById(id);
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setBrand(updatedProduct.getBrand());
        existing.setPrice(updatedProduct.getPrice());
        existing.setWithDiscount(updatedProduct.isWithDiscount());
        existing.setDiscountPrice(updatedProduct.getDiscountPrice());
        existing.setSku(updatedProduct.getSku());
        existing.setIsVisible(updatedProduct.getIsVisible());
        existing.setProductStatus(updatedProduct.getProductStatus());
        existing.setImageUrl(updatedProduct.getImageUrl());
        existing.setUpdatedAt(new Date());
        return productRepository.save(existing);
    }

    @Transactional
    public void softDeleteProduct(Long id) {
        Product existing = getProductById(id);
        existing.setIsVisible(false);
        existing.setDeletedAt(new Date());
        existing.setProductStatus(ProductStatus.ARCHIVED);
        productRepository.save(existing);

    }

    @Transactional
    public void hardDeleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }


}
