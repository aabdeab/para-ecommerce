package com.ecommerce.mappers;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.models.Product;

public  class ProductMapper {

    private ProductMapper(){
        throw new UnsupportedOperationException("a utility class should never be instantiated");
    }
    public static  Product mapToProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setBrand(request.brand());
        product.setPrice(request.price());
        product.setWithDiscount(request.withDiscount());
        product.setDiscountPrice(request.discountPrice());
        product.setSku(request.sku());
        product.setIsVisible(request.isVisible());
        product.setProductStatus(request.productStatus());
        product.setImageUrl(request.imageUrl());
        return product;
    }
}
