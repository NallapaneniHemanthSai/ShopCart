package com.shoppingcart.mapper;

import com.shoppingcart.dto.response.ProductResponse;
import com.shoppingcart.entity.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.isActive(),
                product.getImageUrl()
        );
    }
}
