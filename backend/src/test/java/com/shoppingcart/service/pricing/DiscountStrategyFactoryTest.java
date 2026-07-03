package com.shoppingcart.service.pricing;

import com.shoppingcart.entity.DiscountType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountStrategyFactoryTest {

    private final DiscountStrategyFactory factory =
            new DiscountStrategyFactory(new PercentageDiscountStrategy(), new FlatDiscountStrategy());

    @Test
    void resolvesPercentageStrategy() {
        assertThat(factory.get(DiscountType.PERCENTAGE)).isInstanceOf(PercentageDiscountStrategy.class);
    }

    @Test
    void resolvesFlatStrategy() {
        assertThat(factory.get(DiscountType.FLAT)).isInstanceOf(FlatDiscountStrategy.class);
    }
}
