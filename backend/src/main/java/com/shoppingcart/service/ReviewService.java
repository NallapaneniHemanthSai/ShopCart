package com.shoppingcart.service;

import com.shoppingcart.dto.request.ReviewRequest;
import com.shoppingcart.dto.response.ReviewResponse;
import com.shoppingcart.repository.RatingAggregate;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    List<ReviewResponse> listForProduct(String sku);

    ReviewResponse addReview(Long userId, String sku, ReviewRequest request);

    /** productId -> aggregate, for merging avg rating/count into a batch of ProductResponses. */
    Map<Long, RatingAggregate> aggregateAllRatings();

    RatingAggregate aggregateRatingFor(Long productId);
}
