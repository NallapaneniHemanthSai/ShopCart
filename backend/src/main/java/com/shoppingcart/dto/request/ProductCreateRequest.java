package com.shoppingcart.dto.request;

import com.shoppingcart.entity.Category;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank(message = "SKU is required") String sku,
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Category is required") Category category,
        @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be positive") BigDecimal price,
        @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative") Integer stock,
        String imageUrl
) {
}
