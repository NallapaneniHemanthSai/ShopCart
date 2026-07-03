package com.shoppingcart.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GstUpdateRequest(
        @NotNull(message = "GST rate is required")
        @DecimalMin(value = "0.0", message = "GST rate cannot be negative")
        @DecimalMax(value = "100.0", message = "GST rate cannot exceed 100")
        BigDecimal ratePercent
) {
}
