package com.ecommerce.API;

import com.ecommerce.models.Product;

import java.util.List;

/**
 * Contract for Search operations
 * Handles product search and filtering functionality
 */
public interface SearchServiceContract {

    /**
     * Search products by keyword
     */
    List<Product> searchProducts(String keyword);

    /**
     * Search products by keyword with pagination
     */
    List<Product> searchProducts(String keyword, int page, int size);

    /**
     * Search products by category
     */
    List<Product> searchProductsByCategory(Long categoryId);

    /**
     * Search products by category with pagination
     */
    List<Product> searchProductsByCategory(Long categoryId, int page, int size);

    /**
     * Search products by price range
     */
    List<Product> searchProductsByPriceRange(Double minPrice, Double maxPrice);

    /**
     * Advanced search with multiple filters
     */
    List<Product> advancedSearch(String keyword, Long categoryId, Double minPrice, Double maxPrice,
                                int page, int size, String sortBy, String sortDirection);

    /**
     * Get search suggestions for autocomplete
     */
    List<String> getSearchSuggestions(String partialKeyword);

    /**
     * Get popular search terms
     */
    List<String> getPopularSearchTerms();
}
