package com.shoppingcart.dto.response;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        String reviewerName,
        Integer rating,
        String comment,
        boolean verifiedPurchase,
        Instant createdAt
) {
}
