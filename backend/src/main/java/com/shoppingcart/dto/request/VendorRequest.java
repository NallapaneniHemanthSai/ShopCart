package com.shoppingcart.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VendorRequest(
        @NotBlank(message = "Vendor name is required") String name,
        @Email(message = "Email must be valid") String email,
        String phone,
        @NotNull(message = "Rating is required")
        @DecimalMin(value = "0.0", message = "Rating cannot be negative")
        @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
        BigDecimal rating,
        boolean verified
) {
}
