package com.shoppingcart.service.pricing;

import java.math.BigDecimal;

/**
 * Strategy interface for coupon discount calculation. New discount types
 * (e.g. tiered, buy-X-get-Y) can be added without touching OrderService,
 * satisfying the Open/Closed principle.
 */
public interface DiscountStrategy {

    BigDecimal calculate(BigDecimal subtotal, BigDecimal couponValue);
}
