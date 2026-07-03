package com.shoppingcart.service;

import com.shoppingcart.dto.response.WishlistItemResponse;

import java.util.List;

public interface WishlistService {

    List<WishlistItemResponse> list(Long userId);

    List<WishlistItemResponse> add(Long userId, String sku);

    List<WishlistItemResponse> remove(Long userId, String sku);
}
