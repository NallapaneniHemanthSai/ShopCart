package com.shoppingcart.dto.response;

import com.shoppingcart.entity.Category;

import java.math.BigDecimal;

public record CartItemResponse(
        String sku,
        String name,
        Category category,
        BigDecimal unitPrice,
        Integer quantity,
        Integer availableStock,
        BigDecimal lineTotal
) {
}
