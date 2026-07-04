package com.shoppingcart.repository;

import com.shoppingcart.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.product.sku = :sku ORDER BY r.createdAt DESC")
    List<Review> findByProductSkuOrderByCreatedAtDesc(@Param("sku") String sku);

    boolean existsByProduct_SkuAndUser_Id(String sku, Long userId);

    @Query("SELECT r.product.id AS productId, AVG(r.rating) AS avgRating, COUNT(r) AS reviewCount " +
            "FROM Review r GROUP BY r.product.id")
    List<RatingAggregate> aggregateRatings();

    @Query("SELECT r.product.id AS productId, AVG(r.rating) AS avgRating, COUNT(r) AS reviewCount " +
            "FROM Review r WHERE r.product.id = :productId GROUP BY r.product.id")
    Optional<RatingAggregate> aggregateRatingsForProduct(@Param("productId") Long productId);
}
