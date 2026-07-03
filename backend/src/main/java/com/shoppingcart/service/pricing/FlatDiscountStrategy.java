package com.shoppingcart.service.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FlatDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculate(BigDecimal subtotal, BigDecimal couponValue) {
        return couponValue.min(subtotal);
    }
}
