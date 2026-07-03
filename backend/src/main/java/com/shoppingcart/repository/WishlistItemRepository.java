package com.shoppingcart.repository;

import com.shoppingcart.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserIdOrderByAddedAtDesc(Long userId);

    Optional<WishlistItem> findByUserIdAndProductSku(Long userId, String sku);

    void deleteByUserIdAndProductSku(Long userId, String sku);
}
