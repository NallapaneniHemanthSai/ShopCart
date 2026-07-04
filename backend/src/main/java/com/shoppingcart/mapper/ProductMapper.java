package com.shoppingcart.mapper;

import com.shoppingcart.dto.response.ProductResponse;
import com.shoppingcart.entity.Product;

import java.math.BigDecimal;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        return toResponse(product, BigDecimal.ZERO, 0L);
    }

    public static ProductResponse toResponse(Product product, BigDecimal averageRating, long reviewCount) {
        return new ProductResponse(
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.isActive(),
                product.getImageUrl(),
                product.getVendor().getId(),
                product.getVendor().getName(),
                product.getVendor().getRating(),
                product.getVendor().isVerified(),
                averageRating == null ? BigDecimal.ZERO : averageRating,
                reviewCount
        );
    }
}
