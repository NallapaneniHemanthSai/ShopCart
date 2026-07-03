package com.shoppingcart.service.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculate(BigDecimal subtotal, BigDecimal couponValue) {
        return subtotal.multiply(couponValue)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
