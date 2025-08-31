package com.ecommerce.services;

import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.ProductCategory;
import com.ecommerce.repositories.ProductCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {
    private final ProductCategoryRepository categoryRepository;
    @Transactional
    public ProductCategory createCategory(ProductCategory category) {
        category.setCreatedAt(new Date());
        category.setIsActive(true);
        return categoryRepository.save(category);
    }

    public ProductCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public ProductCategory updateCategory(Long id, ProductCategory updatedCategory) {
        ProductCategory existing = getCategoryById(id);
        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());
        existing.setIsActive(updatedCategory.getIsActive());
        return categoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
