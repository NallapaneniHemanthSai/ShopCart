package com.shoppingcart.dto.request;

import jakarta.validation.constraints.NotNull;

public record StockAdjustRequest(
        @NotNull(message = "Delta is required") Integer delta
) {
}
