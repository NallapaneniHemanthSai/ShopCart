package com.shoppingcart.service.impl;

import com.shoppingcart.dto.response.WishlistItemResponse;
import com.shoppingcart.entity.Product;
import com.shoppingcart.entity.User;
import com.shoppingcart.entity.WishlistItem;
import com.shoppingcart.exception.ProductNotFoundException;
import com.shoppingcart.mapper.WishlistMapper;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.repository.UserRepository;
import com.shoppingcart.repository.WishlistItemRepository;
import com.shoppingcart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<WishlistItemResponse> list(Long userId) {
        return wishlistItemRepository.findByUserIdOrderByAddedAtDesc(userId).stream()
                .map(WishlistMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<WishlistItemResponse> add(Long userId, String sku) {
        if (wishlistItemRepository.findByUserIdAndProductSku(userId, sku).isPresent()) {
            return list(userId);
        }
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
        User userRef = userRepository.getReferenceById(userId);

        WishlistItem item = WishlistItem.builder()
                .user(userRef)
                .product(product)
                .build();
        wishlistItemRepository.save(item);
        return list(userId);
    }

    @Override
    @Transactional
    public List<WishlistItemResponse> remove(Long userId, String sku) {
        wishlistItemRepository.deleteByUserIdAndProductSku(userId, sku);
        return list(userId);
    }
}
