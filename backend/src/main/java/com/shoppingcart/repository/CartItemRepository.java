package com.shoppingcart.repository;

import com.shoppingcart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByAddedAtAsc(Long userId);

    Optional<CartItem> findByUserIdAndProductSku(Long userId, String sku);

    void deleteByUserId(Long userId);
}
