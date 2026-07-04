package com.shoppingcart.repository;

/** Interface projection for a Review GROUP BY product_id aggregate query. */
public interface RatingAggregate {

    Long getProductId();

    Double getAvgRating();

    Long getReviewCount();
}
