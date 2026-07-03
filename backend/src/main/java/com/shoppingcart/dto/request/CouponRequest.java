package com.shoppingcart.dto.request;

import com.shoppingcart.entity.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CouponRequest(
        @NotBlank(message = "Coupon code is required") String code,
        @NotNull(message = "Discount type is required") DiscountType discountType,
        @NotNull(message = "Value is required") @DecimalMin(value = "0.01", message = "Value must be positive") BigDecimal value,
        @NotNull(message = "Minimum cart value is required") @DecimalMin(value = "0.0", message = "Minimum cart value cannot be negative") BigDecimal minCartValue,
        boolean active
) {
}
