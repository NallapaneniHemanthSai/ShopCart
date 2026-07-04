package com.shoppingcart.dto.response;

import com.shoppingcart.entity.Category;

import java.math.BigDecimal;

public record ProductResponse(
        String sku,
        String name,
        String description,
        Category category,
        BigDecimal price,
        Integer stock,
        boolean active,
        String imageUrl,
        Long vendorId,
        String vendorName,
        BigDecimal vendorRating,
        boolean vendorVerified,
        BigDecimal averageRating,
        long reviewCount
) {
}
