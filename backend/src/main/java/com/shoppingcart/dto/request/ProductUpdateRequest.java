package com.shoppingcart.dto.request;

import com.shoppingcart.entity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Category is required") Category category,
        @NotNull(message = "Vendor is required") Long vendorId,
        @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be positive") BigDecimal price,
        @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative") Integer stock,
        boolean active,
        String imageUrl
) {
}
