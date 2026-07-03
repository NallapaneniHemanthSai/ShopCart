package com.shoppingcart.mapper;

import com.shoppingcart.dto.response.CartItemResponse;
import com.shoppingcart.entity.CartItem;
import com.shoppingcart.entity.Product;

import java.math.BigDecimal;

public final class CartMapper {

    private CartMapper() {
    }

    public static CartItemResponse toResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                item.getQuantity(),
                product.getStock(),
                lineTotal
        );
    }
}
