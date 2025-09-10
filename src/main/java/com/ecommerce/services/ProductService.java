package com.ecommerce.services;

import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.*;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.repositories.ProductCategoryRepository;
import io.jsonwebtoken.lang.Assert;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @Transactional
    public Product createProduct(Product product, String categoryName) {
        validateProduct(product);
        ProductCategory category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setCategory(category);
        product.setIsVisible(true);
        product.setProductStatus(ProductStatus.AVAILABLE);
        return productRepository.save(product);
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
        validateProduct(updatedProduct);

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

    /** ======= Validation Helpers ======= **/

    private void validateProduct(Product product) {
        Assert.notNull(product.getName(), "Product name must not be null");
        if (product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name must not be empty");
        }
        if (product.getPrice() == null || product.getPrice() < 0) {
            throw new IllegalArgumentException("Price must be non-null and >= 0");
        }
        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new IllegalArgumentException("SKU must not be empty");
        }
    }
}
