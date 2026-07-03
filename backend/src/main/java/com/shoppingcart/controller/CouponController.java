package com.shoppingcart.controller;

import com.shoppingcart.dto.response.CouponValidationResponse;
import com.shoppingcart.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validate(@RequestParam String code,
                                                               @RequestParam BigDecimal subtotal) {
        return ResponseEntity.ok(couponService.validate(code, subtotal));
    }
}
