package com.shoppingcart.service.pricing;

import com.shoppingcart.entity.DiscountType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class DiscountStrategyFactory {

    private final Map<DiscountType, DiscountStrategy> strategies;

    public DiscountStrategyFactory(PercentageDiscountStrategy percentageStrategy,
                                    FlatDiscountStrategy flatStrategy) {
        this.strategies = new EnumMap<>(DiscountType.class);
        strategies.put(DiscountType.PERCENTAGE, percentageStrategy);
        strategies.put(DiscountType.FLAT, flatStrategy);
    }

    public DiscountStrategy get(DiscountType type) {
        return strategies.get(type);
    }
}
