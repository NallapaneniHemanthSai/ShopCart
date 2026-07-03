package com.shoppingcart.mapper;

import com.shoppingcart.dto.response.CouponResponse;
import com.shoppingcart.entity.Coupon;

public final class CouponMapper {

    private CouponMapper() {
    }

    public static CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getValue(),
                coupon.getMinCartValue(),
                coupon.isActive()
        );
    }
}
