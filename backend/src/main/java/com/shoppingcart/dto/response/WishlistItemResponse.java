package com.shoppingcart.dto.response;

import com.shoppingcart.entity.Category;

import java.math.BigDecimal;

public record WishlistItemResponse(
        String sku,
        String name,
        Category category,
        BigDecimal price,
        Integer stock,
        String imageUrl
) {
}
