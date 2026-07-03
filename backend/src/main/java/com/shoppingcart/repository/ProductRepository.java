package com.shoppingcart.repository;

import com.shoppingcart.entity.Category;
import com.shoppingcart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByActiveTrue();

    List<Product> findByCategoryAndActiveTrue(Category category);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    List<Product> findByStockLessThanEqualAndActiveTrue(Integer threshold);
}
