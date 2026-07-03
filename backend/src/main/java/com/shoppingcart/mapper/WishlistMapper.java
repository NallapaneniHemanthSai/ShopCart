package com.shoppingcart.mapper;

import com.shoppingcart.dto.response.WishlistItemResponse;
import com.shoppingcart.entity.Product;
import com.shoppingcart.entity.WishlistItem;

public final class WishlistMapper {

    private WishlistMapper() {
    }

    public static WishlistItemResponse toResponse(WishlistItem item) {
        Product product = item.getProduct();
        return new WishlistItemResponse(
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl()
        );
    }
}
