package com.shoppingcart.dto.response;

import java.math.BigDecimal;

public record CouponValidationResponse(
        boolean valid,
        String message,
        BigDecimal discountAmount
) {
}
