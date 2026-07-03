package com.shoppingcart.service.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PercentageDiscountStrategyTest {

    private final PercentageDiscountStrategy strategy = new PercentageDiscountStrategy();

    @Test
    void calculatesTenPercentOfSubtotal() {
        BigDecimal discount = strategy.calculate(new BigDecimal("1000.00"), new BigDecimal("10"));
        assertThat(discount).isEqualByComparingTo("100.00");
    }

    @Test
    void roundsToTwoDecimalPlacesHalfUp() {
        BigDecimal discount = strategy.calculate(new BigDecimal("999.99"), new BigDecimal("10"));
        // 999.99 * 10 / 100 = 99.999 -> rounds to 100.00
        assertThat(discount).isEqualByComparingTo("100.00");
    }

    @Test
    void zeroPercentYieldsZeroDiscount() {
        BigDecimal discount = strategy.calculate(new BigDecimal("500.00"), BigDecimal.ZERO);
        assertThat(discount).isEqualByComparingTo("0.00");
    }
}
