package com.shoppingcart.service;

import com.shoppingcart.dto.request.CouponRequest;
import com.shoppingcart.dto.response.CouponResponse;
import com.shoppingcart.dto.response.CouponValidationResponse;
import com.shoppingcart.entity.Coupon;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    List<CouponResponse> listAll();

    CouponValidationResponse validate(String code, BigDecimal cartSubtotal);

    Coupon requireValidCoupon(String code, BigDecimal cartSubtotal);

    CouponResponse create(CouponRequest request);

    CouponResponse update(String code, CouponRequest request);

    void delete(String code);
}
