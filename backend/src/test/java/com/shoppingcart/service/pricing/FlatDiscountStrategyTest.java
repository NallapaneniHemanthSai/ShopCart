package com.shoppingcart.service.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FlatDiscountStrategyTest {

    private final FlatDiscountStrategy strategy = new FlatDiscountStrategy();

    @Test
    void appliesFlatAmountWhenSubtotalExceedsIt() {
        BigDecimal discount = strategy.calculate(new BigDecimal("1000.00"), new BigDecimal("100.00"));
        assertThat(discount).isEqualByComparingTo("100.00");
    }

    @Test
    void capsDiscountAtSubtotalWhenFlatValueExceedsSubtotal() {
        // A flat 500 coupon on a 300 cart should never make the order "negative" -
        // discount is capped to the subtotal itself.
        BigDecimal discount = strategy.calculate(new BigDecimal("300.00"), new BigDecimal("500.00"));
        assertThat(discount).isEqualByComparingTo("300.00");
    }
}
