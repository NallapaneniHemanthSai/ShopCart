package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.CouponRequest;
import com.shoppingcart.dto.response.CouponResponse;
import com.shoppingcart.dto.response.CouponValidationResponse;
import com.shoppingcart.entity.Coupon;
import com.shoppingcart.exception.InvalidCouponException;
import com.shoppingcart.mapper.CouponMapper;
import com.shoppingcart.repository.CouponRepository;
import com.shoppingcart.service.CouponService;
import com.shoppingcart.service.pricing.DiscountStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final DiscountStrategyFactory discountStrategyFactory;

    @Override
    public List<CouponResponse> listAll() {
        return couponRepository.findAll().stream()
                .map(CouponMapper::toResponse)
                .toList();
    }

    @Override
    public CouponValidationResponse validate(String code, BigDecimal cartSubtotal) {
        try {
            Coupon coupon = requireValidCoupon(code, cartSubtotal);
            BigDecimal discount = discountStrategyFactory.get(coupon.getDiscountType())
                    .calculate(cartSubtotal, coupon.getValue());
            return new CouponValidationResponse(true, "Coupon applied successfully", discount);
        } catch (InvalidCouponException ex) {
            return new CouponValidationResponse(false, ex.getMessage(), BigDecimal.ZERO);
        }
    }

    @Override
    public Coupon requireValidCoupon(String code, BigDecimal cartSubtotal) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new InvalidCouponException("Coupon code not found: " + code));

        if (!coupon.isActive()) {
            throw new InvalidCouponException("Coupon is no longer active: " + code);
        }
        if (cartSubtotal.compareTo(coupon.getMinCartValue()) < 0) {
            throw new InvalidCouponException(
                    "Cart subtotal must be at least " + coupon.getMinCartValue() + " to use coupon " + code);
        }
        return coupon;
    }

    @Override
    @Transactional
    public CouponResponse create(CouponRequest request) {
        Coupon coupon = Coupon.builder()
                .code(request.code().toUpperCase())
                .discountType(request.discountType())
                .value(request.value())
                .minCartValue(request.minCartValue())
                .active(request.active())
                .build();
        return CouponMapper.toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse update(String code, CouponRequest request) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new InvalidCouponException("Coupon code not found: " + code));
        coupon.setDiscountType(request.discountType());
        coupon.setValue(request.value());
        coupon.setMinCartValue(request.minCartValue());
        coupon.setActive(request.active());
        return CouponMapper.toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public void delete(String code) {
        couponRepository.findByCodeIgnoreCase(code)
                .ifPresent(couponRepository::delete);
    }
}
