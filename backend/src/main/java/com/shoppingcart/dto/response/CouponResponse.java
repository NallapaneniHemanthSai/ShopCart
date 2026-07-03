package com.shoppingcart.dto.response;

import com.shoppingcart.entity.DiscountType;

import java.math.BigDecimal;

public record CouponResponse(
        String code,
        DiscountType discountType,
        BigDecimal value,
        BigDecimal minCartValue,
        boolean active
) {
}
