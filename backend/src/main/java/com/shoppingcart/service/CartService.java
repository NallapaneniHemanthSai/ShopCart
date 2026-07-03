package com.shoppingcart.service;

import com.shoppingcart.dto.request.AddToCartRequest;
import com.shoppingcart.dto.request.UpdateCartItemRequest;
import com.shoppingcart.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(Long userId, String sortBy);

    CartResponse addItem(Long userId, AddToCartRequest request);

    CartResponse updateItem(Long userId, String sku, UpdateCartItemRequest request);

    CartResponse removeItem(Long userId, String sku);

    CartResponse clearCart(Long userId);

    CartResponse undo(Long userId);

    void clearHistory(Long userId);
}
