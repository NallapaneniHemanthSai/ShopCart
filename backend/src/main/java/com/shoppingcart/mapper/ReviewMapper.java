package com.shoppingcart.mapper;

import com.shoppingcart.dto.response.ReviewResponse;
import com.shoppingcart.entity.Review;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getComment(),
                true,
                review.getCreatedAt()
        );
    }
}
