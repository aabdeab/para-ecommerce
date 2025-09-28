package com.ecommerce.API;

import com.ecommerce.models.ProductCategory;

import java.util.List;
import java.util.Optional;

/**
 * Contract for Product Category management operations
 * Handles category CRUD operations and category hierarchy
 */
public interface ProductCategoryServiceContract {

    /**
     * Create new product category
     */
    ProductCategory createCategory(String name, String description, Long parentCategoryId);

    /**
     * Update existing category
     */
    ProductCategory updateCategory(Long categoryId, String name, String description);

    /**
     * Delete category
     */
    void deleteCategory(Long categoryId);

    /**
     * Find category by ID
     */
    Optional<ProductCategory> findById(Long categoryId);

    /**
     * Find category by name
     */
    Optional<ProductCategory> findByName(String name);

    /**
     * Get all categories
     */
    List<ProductCategory> getAllCategories();

    /**
     * Get root categories (no parent)
     */
    List<ProductCategory> getRootCategories();

    /**
     * Get subcategories of a parent category
     */
    List<ProductCategory> getSubcategories(Long parentCategoryId);

    /**
     * Get category hierarchy tree
     */
    List<ProductCategory> getCategoryHierarchy();

    /**
     * Move category to different parent
     */
    ProductCategory moveCategory(Long categoryId, Long newParentId);
}
